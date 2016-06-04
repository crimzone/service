package se.crimzone.service.commands;

import com.commercehub.dropwizard.mongo.ManagedMongoClient;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.slf4j.Logger;
import se.crimzone.service.CrimzoneApplication;
import se.crimzone.service.CrimzoneConfiguration;

import static org.slf4j.LoggerFactory.getLogger;

@Singleton
public class DeleteAllCrimesCommand extends ConfiguredCommand<CrimzoneConfiguration> {
	private static final Logger log = getLogger(DeleteAllCrimesCommand.class);

	private static final String COMMAND_NAME = "delete-all-crimes";
	private static final String COMMAND_DESCRIPTION = "Deletes all crimes from the database. Must be confirmed with the --confirm flag";

	@Inject
	public DeleteAllCrimesCommand() {
		super(COMMAND_NAME, COMMAND_DESCRIPTION);
	}

	@Override
	protected void run(Bootstrap<CrimzoneConfiguration> bootstrap, Namespace arguments, CrimzoneConfiguration config) throws Exception {
		log.info("Connecting to mongo");
		ManagedMongoClient mongo = config.getMongo().build();
		DB db = mongo.getDB(config.getMongo().getDbName());
		DBCollection collection = db.getCollection(CrimzoneApplication.CRIMES_COLLECTION_NAME);
		log.info("Dropping collection '{}'", collection);
		collection.drop();
		log.info("Successfully dropped collection");

		log.info("Closing mongo");
		mongo.close();
	}

	@Override
	public void configure(Subparser parser) {
		super.configure(parser);
		parser.addArgument("--confirm")
				.required(true)
				.action(Arguments.storeConst())
				.setConst(true)
				.help("You must set this flag to actually run the command");
	}
}
