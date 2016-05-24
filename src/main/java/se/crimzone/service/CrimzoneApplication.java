package se.crimzone.service;

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

	private GuiceBundle<CrimzoneConfiguration> guiceBundle;

	public static void main(String[] args) throws Exception {
		new CrimzoneApplication().run(args);
	}

	@Override
	public String getName() {
		return "Inflector Dropwizard Sample";
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

		configureSwagger(config, env);
	}

	private void configureSwagger(CrimzoneConfiguration config, Environment env) throws Exception {
		Configuration swaggerConfig = Configuration.read(config.getInflectorFile());
		swaggerConfig.setControllerFactory(new GuiceControllerFactory(guiceBundle.getInjector()));
		SwaggerInflector inflector = new SwaggerInflector(swaggerConfig);
		env.jersey().getResourceConfig().registerResources(inflector.getResources());

		// add serializers for swagger
		env.jersey().register(SwaggerSerializers.class);

		// example serializers
		env.jersey().register(XMLExampleProvider.class);

		// mappers
		SimpleModule simpleModule = new SimpleModule();
		simpleModule.addSerializer(new JsonNodeExampleSerializer());
		Json.mapper().registerModule(simpleModule);
		Yaml.mapper().registerModule(simpleModule);

		env.healthChecks().register("mongoDbExists", guiceBundle.getInjector().getInstance(MongoDbExistsHealthCheck.class));
	}

	private static class GuiceControllerFactory implements ControllerFactory {
		private final Injector injector;

		public GuiceControllerFactory(Injector injector) {
			this.injector = injector;
		}

		public Object instantiateController(Class cls) throws IllegalAccessException, InstantiationException {
			return this.injector.getInstance(cls);
		}
	}
}
