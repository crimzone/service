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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

@Singleton
public class BrottsplatskartanCrimeCollector implements CrimeCollector, Managed {
	private static final Logger log = getLogger(BrottsplatskartanCrimeCollector.class);

	public static final String WORKER_COUNT_NAME = "workerCount";

	private final int workerCount;
	private final DB db;

	private Status status = Status.STOPPED;
	private CrimeCollectorMaster crimeCollectorMaster;

	@Inject
	public BrottsplatskartanCrimeCollector(@Named(WORKER_COUNT_NAME) int workerCount, DB db) {
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
				Integer lastPageIndex = null;
				try {
					lastPageIndex = findLastPageIndex();
				} catch (InterruptedException e) {
					log.debug("interrupted");
					return;
				}
				log.debug("lastPageIndex: {}", lastPageIndex);
				collect(lastPageIndex, 0);
				log.debug("Finished collecting");
			} catch (RuntimeException e) {
				log.error("Unexpected error", e);
			}
		}

		private Integer findLastPageIndex() throws InterruptedException {
			log.trace("Called findLastPageIndex");
			int lastPageIndex = 0;
			while (status == Status.RUNNING) {
				// FIXME implement findLastPageIndex
				if (foundLastPage()) {
					return lastPageIndex;
				}
			}
			return null;
		}

		private boolean foundLastPage() {
			log.trace("Called foundLastPage");
			// FIXME implement foundLastPage
			return true;
		}

		private void collect(int startPageIndex, int endPageIndex) {
			log.trace("Called collect");
			Counter counter = new Counter(startPageIndex, endPageIndex);
			log.debug("Creating {} worker threads", workerCount);
			workers = IntStream.range(0, workerCount)
					.mapToObj(i -> new CrimeCollectorWorker(i, workerCount, counter))
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

		private static final String THREAD_NAME_PREFIX = "CrimeCollectorWorker ";
		private static final int HTTP_GET_TIMEOUT_MILLIS = 3000;
		private static final int HTTP_GET_RETRIES = 5;

		private final int threadIndex;
		private final int threadCount;
		private final Counter counter;

		CrimeCollectorWorker(int threadIndex, int threadCount, Counter counter) {
			super(THREAD_NAME_PREFIX + threadIndex);
			log.trace("Called CrimeCollectorWorker");
			this.threadIndex = threadIndex;
			this.threadCount = threadCount;
			this.counter = counter;
		}

		@Override
		public void run() {
			try {
				log.trace("Called run");
				int nextPageIndex = counter.getStart() + getOffset();
				while (status == Status.RUNNING && counter.hasIndex(nextPageIndex)) {
					Collection<Crime> crimes;
					try {
						Document page = fetchPage(nextPageIndex);
						crimes = parseCrimes(page);
					} catch (InterruptedException e) {
						log.debug("Interrupted. Aborting");
						return;
					} catch (IOException | GaveUpTryingException e) {
						log.error("Failed to fetch page", e);
						log.debug("setting status to STOPPING");
						status = Status.STOPPING;
						return;
					}
					while (!isThisThreadsTurn()) {
						if (status != Status.RUNNING) {
							return;
						}
						yield();
					}
					store(crimes);
					nextPageIndex += getOffset();
					counter.step();
				}
			} catch (RuntimeException e) {
				log.error("Unexpected error", e);
			}
		}

		private Collection<Crime> parseCrimes(Document document) {
			log.trace("Called parseCrimes");
			Elements events = document.select("body > div.wrap > section > div > article.event");
			return events.stream()
					.map(this::parseCrime)
					.collect(Collectors.toSet());
		}

		private Crime parseCrime(Element element) {
			// FIXME implement parseCrime
			return null;
		}

		private Document fetchPage(int pageIndex) throws InterruptedException, IOException, GaveUpTryingException {
			log.trace("Called fetchPage");
			String url = String.format("http://brottsplatskartan.se/lan/alla-lan/sida/%d0/", pageIndex);
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

		private void store(Collection<Crime> crimes) {
			log.trace("Called store");
			// TODO implement store
			log.info("stored {}", crimes);
		}

		private int getOffset() {
			log.trace("Called getOffset");
			return counter.shouldIncrement() ? threadIndex : -threadIndex;
		}

		private boolean isThisThreadsTurn() {
			log.trace("Called isThisThreadsTurn");
			return counter.getCurrent() % threadCount == threadIndex;
		}


	}

	private enum Status {
		STOPPED, RUNNING, STOPPING
	}

	private static class Counter {
		private static final Consumer<AtomicInteger> increment = AtomicInteger::incrementAndGet;
		private static final Consumer<AtomicInteger> decrement = AtomicInteger::decrementAndGet;

		private final int start;
		private final int end;
		private final AtomicInteger current;

		Counter(int start, int end) {
			checkArgument(start >= 0, "start must be non-negative");
			checkArgument(end >= 0, "end must be non-negative");
			this.start = start;
			this.end = end;
			this.current = new AtomicInteger(start);
		}

		int getStart() {
			return start;
		}

		int getCurrent() {
			return current.get();
		}

		void step() {
			nextOperation().accept(current);
		}

		boolean hasIndex(int index) {
			int lowerIndex = Math.min(start, end);
			int higherIndex = Math.max(start, end);
			return lowerIndex <= index && index <= higherIndex;
		}

		boolean shouldIncrement() {
			return start <= end;
		}

		private Consumer<AtomicInteger> nextOperation() {
			return shouldIncrement() ? increment : decrement;
		}
	}
}
