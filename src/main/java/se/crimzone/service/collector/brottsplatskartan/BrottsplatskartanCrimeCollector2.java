package se.crimzone.service.collector.brottsplatskartan;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mongodb.DB;
import io.dropwizard.lifecycle.Managed;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import se.crimzone.service.collector.CrimeCollector;
import se.crimzone.service.models.Crime;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Collection;
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

@Singleton
public class BrottsplatskartanCrimeCollector2 implements CrimeCollector, Managed {
	private static final Logger log = getLogger(BrottsplatskartanCrimeCollector2.class);

	public static final String WORKER_COUNT_NAME = "workerCount";

	private final int workerCount;
	private final DB db;

	private Status status = Status.STOPPED;
	private CrimeCollectorMaster crimeCollectorMaster;

	@Inject
	public BrottsplatskartanCrimeCollector2(@Named(WORKER_COUNT_NAME) int workerCount, DB db) {
		log.trace("Called BrottsplatskartanCrimeCollector");
		checkArgument(workerCount > 0, "workerCount must be > 0");
		this.workerCount = workerCount;
		this.db = checkNotNull(db);
	}

	@Override
	public synchronized boolean startCollecting() {
		log.trace("Called startCollecting");
		if (status != Status.STOPPED) {
			log.debug("status is {}, aborting", status);
			return false;
		}
		log.debug("setting status to RUNNING");
		status = Status.RUNNING;
		crimeCollectorMaster = new CrimeCollectorMaster();
		log.debug("starting crimeCollectorMaster");
		crimeCollectorMaster.start();
		return true;
	}

	@Override
	public synchronized boolean stopCollecting() {
		log.trace("Called stopCollecting");
		if (status != Status.RUNNING) {
			log.debug("status is {}, aborting", status);
			return false;
		}
		log.debug("setting status to STOPPING");
		status = Status.STOPPING;
		log.debug("Interrupting all");
		crimeCollectorMaster.interruptAll();
		try {
			log.debug("Waiting for {} to die...", crimeCollectorMaster);
			crimeCollectorMaster.join();
			log.debug("{} has died", crimeCollectorMaster);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		crimeCollectorMaster = null;
		log.debug("setting status to STOPPED");
		status = Status.STOPPED;
		return true;
	}

	@Override
	public void start() throws Exception {
		log.trace("Called start");
	}

	@Override
	public void stop() throws Exception {
		log.trace("Called stop");
		stopCollecting();
	}

	private class CrimeCollectorMaster extends Thread {
		private final Logger log = getLogger(CrimeCollectorMaster.class);

		private static final String THREAD_NAME = "CrimeCollectorMaster";
		private List<CrimeCollectorWorker> workers;

		CrimeCollectorMaster() {
			super(THREAD_NAME);
			log.trace("Called CrimeCollectorMaster");
		}

		@Override
		public void run() {
			try {
				log.trace("Called run");
				int lastPageIndex = findLastPageIndex();
				log.debug("lastPageIndex: {}", lastPageIndex);
				collect(lastPageIndex, 0);
				log.debug("Finished collecting");
			} catch (RuntimeException e) {
				log.error("Unexpected error", e);
			} catch (InterruptedException e) {
				log.debug("Interrupted. Aborting");
			}
		}

		private int findLastPageIndex() throws InterruptedException {
			throw new UnsupportedOperationException("Not yet implemented");
			// TODO Implement findLastPageIndex
		}

		private void collect(int startPageIndex, int endPageIndex) {
			log.trace("Called collect");
			OrderedJobProcessor<Collection<Crime>> saver = new OrderedJobProcessor<>(startPageIndex, endPageIndex,
					(e -> System.out.println("Saving " + e))); // TODO Implement saving
			log.debug("Creating {} worker threads", workerCount);
			workers = IntStream.range(0, workerCount)
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
		}

		void interruptAll() {
			log.trace("Called interruptAll");
			log.debug("interrupting workers: {}", workers);
			if (workers != null) {
				workers.forEach(Thread::interrupt);
			} else {
				log.debug("No workers running. Interrupting: {}", this);
				interrupt();
			}
		}
	}

	private class CrimeCollectorWorker extends Thread {
		private final Logger log = getLogger(CrimeCollectorWorker.class);

		private static final String URL_FORMAT = "http://brottsplatskartan.se/lan/alla-lan/sida/%d0/";
		private static final String WORKER_NAME_PREFIX = "CrimeCollectorWorker ";
		private static final int HTTP_GET_TIMEOUT_MILLIS = 3000;
		private static final int HTTP_GET_RETRIES = 5;

		private final OrderedJobProcessor<Collection<Crime>> saver;

		CrimeCollectorWorker(String workerId, OrderedJobProcessor<Collection<Crime>> saver) {
			super(WORKER_NAME_PREFIX + workerId);
			log.trace("Called CrimeCollectorWorker");
			this.saver = saver;
		}

		@Override
		public void run() {
			try {
				log.trace("Called run");
				while (status == Status.RUNNING) {
					log.debug("Getting new job");
					Optional<Integer> job = saver.getJob();
					if (!job.isPresent()) {
						log.debug("No more jobs");
						return;
					}
					int jobIndex = job.get();
					log.debug("jobIndex: {}", jobIndex);
					Document page = fetchPage(jobIndex);
					Collection<Crime> crimes = parseCrimes(page);
					saver.handInJob(jobIndex, crimes);
				}
			} catch (InterruptedException e) {
				log.debug("Interrupted. Aborting");
			} catch (RuntimeException | IOException | GaveUpTryingException e) {
				log.error("Failed to fetch page", e);
				log.debug("setting status to STOPPING");
				status = Status.STOPPING;
			}
		}

		private Document fetchPage(int pageIndex) throws InterruptedException, IOException, GaveUpTryingException {
			log.trace("Called fetchPage");
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

		private Collection<Crime> parseCrimes(Document document) {
			log.trace("Called parseCrimes");
			Elements events = document.select("body > div.wrap > section > div > article.event");
			return events.stream()
					.map(this::parseCrime)
					.collect(Collectors.toSet());
		}

		private Crime parseCrime(Element element) {
			log.trace("Called parseCrime");
			// FIXME implement parseCrime
			return null;
		}


	}

	private enum Status {
		STOPPED, RUNNING, STOPPING
	}

	private class OrderedJobProcessor<T> {
		private final Logger log = getLogger(OrderedJobProcessor.class);

		private final Consumer<T> saver;
		private final int start;
		private final int end;
		private final Function<Integer, Integer> nextOperation;
		private int currentJob;
		private int upcomingJob;

		OrderedJobProcessor(int start, int end, Consumer<T> saver) {
			log.trace("Called OrderedJobProcessor");
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
			log.trace("Called handInJob");
			while (i != currentJob) {
				yield();
				if (status != Status.RUNNING) {
					log.debug("Stopping");
					return false;
				}
			}
			saver.accept(element);
			currentJob = nextOperation.apply(currentJob);
			return true;
		}

		synchronized Optional<Integer> getJob() {
			log.trace("Called getJob");
			if (!upcomingJobIsWithinRange()) {
				log.debug("Upcoming job is outside of range");
				return Optional.empty();
			}
			Optional<Integer> job = Optional.of(upcomingJob);
			log.debug("job: {}", job);
			upcomingJob = nextOperation.apply(this.upcomingJob);
			return job;
		}

		private boolean upcomingJobIsWithinRange() {
			int lowest = Math.min(start, end);
			int highest = Math.max(start, end);
			return lowest <= upcomingJob && upcomingJob <= highest;
		}
	}
}
