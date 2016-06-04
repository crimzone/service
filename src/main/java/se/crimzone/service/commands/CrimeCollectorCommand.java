package se.crimzone.service.commands;

import com.commercehub.dropwizard.mongo.ManagedMongoClient;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mongojack.JacksonDBCollection;
import org.slf4j.Logger;
import se.crimzone.service.CrimzoneConfiguration;
import se.crimzone.service.dao.CrimesDao;
import se.crimzone.service.models.Crime;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Thread.yield;
import static org.slf4j.LoggerFactory.getLogger;

public class CrimeCollectorCommand extends ConfiguredCommand<CrimzoneConfiguration> {
	private static final Logger log = getLogger(CrimeCollectorCommand.class);

	private static final String COMMAND_NAME = "collect";
	private static final String COMMAND_DESCRIPTION = "Collects crimes from Brottsplatskartan and saves them to the database";

	private CrimesDao dao;

	public CrimeCollectorCommand() {
		super(COMMAND_NAME, COMMAND_DESCRIPTION);
	}

	@Override
	protected void run(Bootstrap<CrimzoneConfiguration> bootstrap, Namespace arguments, CrimzoneConfiguration config) throws Exception {
		ManagedMongoClient mongo = config.getMongo().build();
		DB db = mongo.getDB(config.getMongo().getDbName());
		DBCollection mongoCollection = db.getCollection(CrimesDao.CRIMES_COLLECTION_NAME);
		JacksonDBCollection<Crime, Integer> collection = JacksonDBCollection.wrap(mongoCollection, Crime.class, Integer.class);
		dao = new CrimesDao(collection);

		Integer start = arguments.getInt("start");
		log.debug("start: {}", start);
		checkArgument(start >= 0, "start must be non-negative");

		Integer end = arguments.getInt("end");
		log.debug("end: {}", end);
		if (end != null) {
			checkArgument(end >= 0, "end must be non-negative");
		} else {
			log.info("Finding last page");
			end = findLastPage();
			log.info("Last page is {}", end);
		}

		Integer parallelism = arguments.getInt("parallelism");
		log.debug("parallelism: {}", parallelism);
		checkArgument(parallelism > 0, "parallelism must be positive");

		log.debug("Starting collection");
		Instant startTime = Instant.now();
		int count = collect(start, end, parallelism);
		Duration duration = Duration.between(startTime, Instant.now());
		long s = duration.getSeconds();
		String durationString = String.format("%d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60));
		log.info("Processed {} pages in {}", count, durationString);
		mongo.close();
		log.debug("Closing mongo");
	}

	@Override
	public void configure(Subparser parser) {
		super.configure(parser);
		parser.addArgument("-s", "--start")
				.metavar("PAGE")
				.type(Integer.class)
				.nargs("?")
				.setDefault(0)
				.help("The page index to start collecting from");
		parser.addArgument("-e", "--end")
				.metavar("PAGE")
				.type(Integer.class)
				.nargs("?")
				.setDefault((Integer) null)
				.help("The page index to end collecting from (default: looked up using an algorithm)");
		parser.addArgument("-p", "--parallelism")
				.metavar("THREADS")
				.type(Integer.class)
				.nargs("?")
				.setDefault(100)
				.help("The number of pages that will be processed in parallel at a time");
	}

	private int findLastPage() throws InterruptedException {
		throw new UnsupportedOperationException("Not yet implemented");
		// TODO Implement findLastPage
	}

	private int collect(int startPage, int endPage, int parallelism) {
		OrderedJobProcessor<List<Crime>> saver = new OrderedJobProcessor<>(startPage, endPage,
				(dao::insert));
		log.debug("Creating {} worker threads", parallelism);
		List<CrimeCollectorWorker> workers = IntStream.range(0, parallelism)
				.mapToObj(i -> new CrimeCollectorWorker(String.valueOf(i), saver))
				.collect(Collectors.toList());
		log.debug("Starting worker threads");
		workers.forEach(Thread::start);
		for (CrimeCollectorWorker worker : workers) {
			try {
				log.debug("Waiting for {} to die...", worker);
				worker.join();
				log.debug("{} has died", worker);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		return saver.getCount();
	}

	private class CrimeCollectorWorker extends Thread {
		private final Logger log = getLogger(CrimeCollectorWorker.class);

		private static final String URL_FORMAT = "http://brottsplatskartan.se/lan/alla-lan/sida/%d0/";
		private static final String WORKER_NAME_PREFIX = "CrimeCollectorWorker ";
		private static final int HTTP_GET_TIMEOUT_MILLIS = 3000;
		private static final int HTTP_GET_RETRIES = 5;

		private final OrderedJobProcessor<List<Crime>> saver;

		CrimeCollectorWorker(String workerId, OrderedJobProcessor<List<Crime>> saver) {
			super(WORKER_NAME_PREFIX + workerId);
			this.saver = saver;
		}

		@Override
		public void run() {
			try {
				while (true) {
					Optional<Integer> job = saver.getJob();
					if (!job.isPresent()) {
						log.debug("No more jobs");
						break;
					}
					int jobIndex = job.get();
					log.debug("jobIndex: {}", jobIndex);
					Document page = fetchPage(jobIndex);
					List<Crime> crimes = parseCrimes(page);
					saver.handInJob(jobIndex, crimes);
				}
			} catch (IOException | GaveUpTryingException | InterruptedException e) {
				log.error("Failed to fetch page", e);
			}
		}

		private Document fetchPage(int pageIndex) throws IOException, GaveUpTryingException, InterruptedException {
			String url = String.format(URL_FORMAT, pageIndex);
			log.debug("URL: {}", url);
			int attempts = 0;
			int waitSeconds = 1;
			while (attempts <= HTTP_GET_RETRIES + 1) {
				try {
					attempts++;
					log.debug("Fetching data. Attempt: {}", attempts);
					return Jsoup.connect(url).timeout(HTTP_GET_TIMEOUT_MILLIS).get();
				} catch (SocketTimeoutException e) {
					log.warn("Request timed out. Retrying in {} seconds...", waitSeconds);
					sleep(waitSeconds * 1000);
					waitSeconds *= 2;
				}
			}
			throw new GaveUpTryingException(url, attempts);
		}

		private List<Crime> parseCrimes(Document document) {
			Elements events = document.select("body > div.wrap > section > div > article.event");
			return events.stream()
					.map(this::parseCrime)
					.collect(Collectors.toList());
		}

		private Crime parseCrime(Element element) {
			String title = element.select("header > h2 > a").first().text();
			String description = element.select("div.description").first().text();
			String dateString = element.select("header > div.meta > p.when > span").attr("title");
			TemporalAccessor parsedTime = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ").parse(dateString);
			Instant instant = Instant.from(parsedTime);
			int time = Math.toIntExact((instant.toEpochMilli() / 1000));
			String latitudeString = element.select("header > div.meta > p.where > meta[itemprop=\"latitude\"]").attr("content");
			float latitude = Float.valueOf(latitudeString);
			String longitudeString = element.select("header > div.meta > p.where > meta[itemprop=\"longitude\"]").attr("content");
			float longitude = Float.valueOf(longitudeString);

			Crime crime = new Crime();
			crime.setTitle(title);
			crime.setDescription(description);
			crime.setTime(time);
			crime.setLatitude(latitude);
			crime.setLongitude(longitude);
			return crime;
		}


	}

	private class OrderedJobProcessor<T> {
		private final Logger log = getLogger(OrderedJobProcessor.class);

		private final Consumer<T> saver;
		private final int start;
		private final int end;
		private final Function<Integer, Integer> nextOperation;
		private int currentJob;
		private int upcomingJob;
		private int count = 0;

		OrderedJobProcessor(int start, int end, Consumer<T> saver) {
			checkNotNull(saver);
			checkArgument(start >= 0, "start must be non-negative");
			checkArgument(end >= 0, "end must be non-negative");
			this.saver = saver;
			this.start = start;
			this.end = end;
			this.nextOperation = start <= end ? (i -> ++i) : (i -> --i);
			this.currentJob = start;
			this.upcomingJob = start;
		}

		boolean handInJob(int i, T element) {
			while (i != currentJob) {
				yield();
			}
			saver.accept(element);
			count++;
			currentJob = nextOperation.apply(currentJob);
			return true;
		}

		synchronized Optional<Integer> getJob() {
			if (!upcomingJobIsWithinRange()) {
				log.debug("Upcoming job is outside of range");
				return Optional.empty();
			}
			Optional<Integer> job = Optional.of(upcomingJob);
			log.info("Handing out job {}", job);
			upcomingJob = nextOperation.apply(this.upcomingJob);
			return job;
		}

		private boolean upcomingJobIsWithinRange() {
			int lowest = Math.min(start, end);
			int highest = Math.max(start, end);
			return lowest <= upcomingJob && upcomingJob <= highest;
		}

		int getCount() {
			return count;
		}
	}
}
