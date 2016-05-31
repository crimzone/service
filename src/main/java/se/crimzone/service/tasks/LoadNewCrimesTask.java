package se.crimzone.service.tasks;

import com.google.common.collect.ImmutableMultimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.dropwizard.servlets.tasks.Task;
import se.crimzone.service.collector.CrimeCollector;

import java.io.PrintWriter;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class LoadNewCrimesTask extends Task {

	public static final String LOAD_NEW_CRIMES_TASK_NAME = "LoadNewCrimesTaskName";

	private final CrimeCollector crimeCollector;

	@Inject
	protected LoadNewCrimesTask(@Named(LOAD_NEW_CRIMES_TASK_NAME) String name, CrimeCollector crimeCollector) {
		super(name);
		this.crimeCollector = checkNotNull(crimeCollector);
	}

	@Override
	public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
		boolean started = crimeCollector.startCollecting();
		output.append(started ? "started" : "already running");
		output.flush();
	}
}
