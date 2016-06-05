package se.crimzone.service.commands;

import com.commercehub.dropwizard.mongo.ManagedMongoClient;
import com.mongodb.DB;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.slf4j.Logger;
import se.crimzone.service.CrimzoneConfiguration;

import static org.slf4j.LoggerFactory.getLogger;

public class ClearDatabaseCommand extends ConfiguredCommand<CrimzoneConfiguration> {
	private static final Logger log = getLogger(ClearDatabaseCommand.class);

	private static final String COMMAND_NAME = "clear-database";
	private static final String COMMAND_DESCRIPTION = "Clears the whole database. Must be confirmed with the --confirm flag";

	public ClearDatabaseCommand() {
		super(COMMAND_NAME, COMMAND_DESCRIPTION);
	}

	@Override
	protected void run(Bootstrap<CrimzoneConfiguration> bootstrap, Namespace arguments, CrimzoneConfiguration config) throws Exception {
		log.debug("Connecting to mongo");
		String dbName = config.getMongo().getDbName();
		log.info("Dropping database: '{}'", dbName);
		ManagedMongoClient mongo = config.getMongo().build();
		DB db = mongo.getDB(dbName);
		db.dropDatabase();
		log.info("Successfully dropped database");

		log.debug("Closing mongo");
		mongo.close();
	}

	@Override
	public void configure(Subparser parser) {
		super.configure(parser);
		// TODO Fix double print of help text wheh -h is used
		parser.addArgument("--confirm")
				.required(true)
				.action(Arguments.storeConst())
				.setConst(true)
				.help("You must set this flag to actually run the command");
	}
}
