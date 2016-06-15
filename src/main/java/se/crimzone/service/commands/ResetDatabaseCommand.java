package se.crimzone.service.commands;

import com.commercehub.dropwizard.mongo.ManagedMongoClient;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.slf4j.Logger;
import se.crimzone.service.CrimzoneConfiguration;
import se.crimzone.service.dao.CrimesDao;

import static org.slf4j.LoggerFactory.getLogger;

public class ResetDatabaseCommand extends ConfiguredCommand<CrimzoneConfiguration> {
	private static final Logger log = getLogger(ResetDatabaseCommand.class);

	private static final String COMMAND_NAME = "reset-database";
	private static final String COMMAND_DESCRIPTION = "Clears and re-indexes the whole database. Must be confirmed with the --confirm flag";

	public ResetDatabaseCommand() {
		super(COMMAND_NAME, COMMAND_DESCRIPTION);
	}

	@Override
	protected void run(Bootstrap<CrimzoneConfiguration> bootstrap, Namespace arguments, CrimzoneConfiguration config) throws Exception {
		log.debug("Connecting to mongo");
		String dbName = config.getMongo().getDbName();
		ManagedMongoClient mongo = config.getMongo().build();
		DB db = mongo.getDB(dbName);

		log.info("Dropping database: '{}'", dbName);
		db.dropDatabase();
		log.info("Successfully dropped database");

		log.info("Indexing database");
		reIndex(mongo, dbName, CrimesDao.CRIMES_COLLECTION_NAME);
		log.info("Successfully indexed database");

		log.debug("Closing mongo");
		mongo.close();
	}

	private static void reIndex(ManagedMongoClient mongo, String dbName, String collectionName) {
		DB db = mongo.getDB(dbName);
		DBCollection collection = db.getCollection(collectionName);

		collection.dropIndexes();

		DBObject textIndex = new BasicDBObject();
		textIndex.put("title", "text");
		textIndex.put("description", "text");
		collection.createIndex(textIndex);

		DBObject locationIndex = new BasicDBObject("location", "2dsphere");
		collection.createIndex(locationIndex);

		DBObject timeIndex = new BasicDBObject("time", -1);
		collection.createIndex(timeIndex);
	}

	@Override
	public void configure(Subparser parser) {
		super.configure(parser);
		// TODO Fix double print of help text where -h is used
		parser.addArgument("--confirm")
				.required(true)
				.action(Arguments.storeConst())
				.setConst(true)
				.help("You must set this flag to actually run the command");
	}
}
