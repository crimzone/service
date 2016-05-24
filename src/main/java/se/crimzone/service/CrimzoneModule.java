package se.crimzone.service;

import com.commercehub.dropwizard.mongo.ManagedMongoClient;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.mongodb.DB;
import io.dropwizard.setup.Environment;

import java.net.UnknownHostException;

public class CrimzoneModule extends AbstractModule {
	@Override
	protected void configure() {
		// bind your guice stuff here
	}

	@Provides
	public DB provideMongoDb(CrimzoneConfiguration config, Environment env) throws UnknownHostException {
		ManagedMongoClient mongoClient = config.getMongo().build();
		env.lifecycle().manage(mongoClient);
		return mongoClient.getDB(config.getMongo().getDbName());
	}
}
