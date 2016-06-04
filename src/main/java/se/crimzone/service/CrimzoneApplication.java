package se.crimzone.service;

import com.commercehub.dropwizard.mongo.ManagedMongoClient;
import com.commercehub.dropwizard.mongo.MongoClientFactory;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mongodb.DB;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.swagger.inflector.SwaggerInflector;
import io.swagger.inflector.config.Configuration;
import io.swagger.inflector.config.DefaultControllerFactory;
import io.swagger.inflector.processors.JsonNodeExampleSerializer;
import io.swagger.inflector.processors.XMLExampleProvider;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import se.crimzone.service.commands.CrimeCollectorCommand;
import se.crimzone.service.commands.DeleteAllCrimesCommand;
import se.crimzone.service.healthchecks.MongoDbExistsHealthCheck;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.net.UnknownHostException;
import java.util.EnumSet;

public class CrimzoneApplication extends Application<CrimzoneConfiguration> {

	public static final String CRIMES_COLLECTION_NAME = "crimes";

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
		bootstrap.addCommand(new DeleteAllCrimesCommand());
	}

	@Override
	public void run(CrimzoneConfiguration config, Environment env) throws Exception {
		DB db = setupMongo(config, env);
		setupCors(env);
		registerInflectorResources(env);
		configureSwaggerDataTypes(env);

		env.healthChecks().register(MongoDbExistsHealthCheck.NAME, new MongoDbExistsHealthCheck(db));
	}

	private DB setupMongo(CrimzoneConfiguration config, Environment env) throws UnknownHostException {
		MongoClientFactory mongoFactory = config.getMongo();
		ManagedMongoClient mongo = mongoFactory.build();
		env.lifecycle().manage(mongo);
		return mongo.getDB(mongoFactory.getDbName());
	}

	private void setupCors(Environment env) {
		final FilterRegistration.Dynamic cors = env.servlets().addFilter("crossOriginRequsts", CrossOriginFilter.class);
		cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
	}

	private void registerInflectorResources(Environment env) throws Exception {
		Configuration swaggerConfig = Configuration.read(INFLECTOR_FILE_PATH);
		swaggerConfig.setControllerFactory(new DefaultControllerFactory());
		SwaggerInflector inflector = new SwaggerInflector(swaggerConfig);
		env.jersey().getResourceConfig().registerResources(inflector.getResources());
	}

	private void configureSwaggerDataTypes(Environment env) throws Exception {
		// add serializers for swagger
		env.jersey().register(SwaggerSerializers.class);

		// example serializers
		env.jersey().register(XMLExampleProvider.class);

		// mappers
		SimpleModule jacksonModule = new SimpleModule();
		jacksonModule.addSerializer(new JsonNodeExampleSerializer());
		Json.mapper().registerModule(jacksonModule);
		Yaml.mapper().registerModule(jacksonModule);
	}
}
