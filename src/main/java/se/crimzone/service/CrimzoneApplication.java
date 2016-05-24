package se.crimzone.service;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.hubspot.dropwizard.guice.GuiceBundle;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.swagger.inflector.SwaggerInflector;
import io.swagger.inflector.config.Configuration;
import io.swagger.inflector.config.ControllerFactory;
import io.swagger.inflector.processors.JsonNodeExampleSerializer;
import io.swagger.inflector.processors.XMLExampleProvider;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.crimzone.service.healthchecks.MongoDbExistsHealthCheck;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.util.EnumSet;

public class CrimzoneApplication extends Application<CrimzoneConfiguration> {

	private static final Logger LOG = LoggerFactory.getLogger(CrimzoneApplication.class);

	private static final String INFLECTOR_FILE_PATH = "inflector.yaml";

	private GuiceBundle<CrimzoneConfiguration> guiceBundle;

	public static void main(String[] args) throws Exception {
		new CrimzoneApplication().run(args);
	}

	@Override
	public String getName() {
		return "Crimzone service";
	}

	@Override
	public void initialize(Bootstrap<CrimzoneConfiguration> bootstrap) {
		guiceBundle = GuiceBundle.<CrimzoneConfiguration>newBuilder()
				.addModule(new CrimzoneModule())
				.setConfigClass(CrimzoneConfiguration.class)
				.build(Stage.DEVELOPMENT);

		bootstrap.addBundle(guiceBundle);
	}

	@Override
	public void run(CrimzoneConfiguration config, Environment env) throws Exception {
		final FilterRegistration.Dynamic cors = env.servlets().addFilter("crossOriginRequsts", CrossOriginFilter.class);
		cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");

		registerInflectorResources(env);
		configureSwaggerDataTypes(env);
		registerHealthchecks(env.healthChecks());
	}

	private void registerInflectorResources(Environment env) throws Exception {
		Configuration swaggerConfig = Configuration.read(INFLECTOR_FILE_PATH);
		swaggerConfig.setControllerFactory(new GuiceControllerFactory(guiceBundle.getInjector()));
		SwaggerInflector inflector = new SwaggerInflector(swaggerConfig);
		env.jersey().getResourceConfig().registerResources(inflector.getResources());
		LOG.debug("Registered inflector resources: {}", env.jersey().getResourceConfig().getResources());
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

	private void registerHealthchecks(HealthCheckRegistry healthChecks) {
		healthChecks.register("mongoDbExists", guiceBundle.getInjector().getInstance(MongoDbExistsHealthCheck.class));
		LOG.debug("Health checks: {}", healthChecks.getNames());
	}

	private static class GuiceControllerFactory implements ControllerFactory {
		private final Injector injector;

		GuiceControllerFactory(Injector injector) {
			this.injector = injector;
		}

		public Object instantiateController(Class cls) throws IllegalAccessException, InstantiationException {
			return this.injector.getInstance(cls);
		}
	}
}
