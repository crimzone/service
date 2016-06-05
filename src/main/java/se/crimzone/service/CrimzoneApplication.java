package se.crimzone.service;

import com.commercehub.dropwizard.mongo.ManagedMongoClient;
import com.commercehub.dropwizard.mongo.MongoClientFactory;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.swagger.inflector.SwaggerInflector;
import io.swagger.inflector.config.Configuration;
import io.swagger.inflector.processors.JsonNodeExampleSerializer;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import io.swagger.util.Json;
import org.mongojack.JacksonDBCollection;
import se.crimzone.service.commands.ClearDatabaseCommand;
import se.crimzone.service.commands.CrimeCollectorCommand;
import se.crimzone.service.controllers.CrimzoneControllerFactory;
import se.crimzone.service.dao.CrimesDao;
import se.crimzone.service.healthchecks.MongoOnlineHealthCheck;
import se.crimzone.service.models.Crime;

import java.net.UnknownHostException;

public class CrimzoneApplication extends Application<CrimzoneConfiguration> {

	private static final String INFLECTOR_FILE_PATH = "inflector.yaml";

	public static void main(String[] args) throws Exception {
		new CrimzoneApplication().run(args);
	}

	@Override
	public String getName() {
		return "Crimzone service";
	}

	@Override
	public void initialize(Bootstrap<CrimzoneConfiguration> bootstrap) {
		bootstrap.addCommand(new CrimeCollectorCommand());
		bootstrap.addCommand(new ClearDatabaseCommand());
	}

	@Override
	public void run(CrimzoneConfiguration config, Environment env) throws Exception {
		DB db = setupMongo(env, config);
		registerInflectorResources(env, db);
		configureSwaggerDataTypes(env);

		env.healthChecks().register(MongoOnlineHealthCheck.NAME, new MongoOnlineHealthCheck(db));
	}

	private DB setupMongo(Environment env, CrimzoneConfiguration config) throws UnknownHostException {
		MongoClientFactory mongoFactory = config.getMongo();
		ManagedMongoClient mongo = mongoFactory.build();
		env.lifecycle().manage(mongo);
		return mongo.getDB(mongoFactory.getDbName());
	}

	private void registerInflectorResources(Environment env, DB db) throws Exception {
		Configuration swaggerConfig = Configuration.read(INFLECTOR_FILE_PATH);
		DBCollection mongoCollection = db.getCollection(CrimesDao.CRIMES_COLLECTION_NAME);
		JacksonDBCollection<Crime, Integer> collection = JacksonDBCollection.wrap(mongoCollection, Crime.class, Integer.class);
		CrimesDao crimesDao = new CrimesDao(collection);
		swaggerConfig.setControllerFactory(new CrimzoneControllerFactory(crimesDao));
		SwaggerInflector inflector = new SwaggerInflector(swaggerConfig);
		env.jersey().getResourceConfig().registerResources(inflector.getResources());
	}

	private void configureSwaggerDataTypes(Environment env) throws Exception {
		// add serializers for swagger
		env.jersey().register(SwaggerSerializers.class);

		// mappers
		SimpleModule jacksonModule = new SimpleModule();
		jacksonModule.addSerializer(new JsonNodeExampleSerializer());
		Json.mapper().registerModule(jacksonModule);
	}
}
