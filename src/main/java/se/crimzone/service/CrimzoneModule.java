package se.crimzone.service;

import com.commercehub.dropwizard.mongo.ManagedMongoClient;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import com.mongodb.DB;
import io.dropwizard.setup.Environment;
import se.crimzone.service.tasks.LoadNewCrimesTask;

import java.net.UnknownHostException;

class CrimzoneModule extends AbstractModule {
	@Override
	protected void configure() {
		bindConstant().annotatedWith(Names.named(LoadNewCrimesTask.LOAD_NEW_CRIMES_TASK_NAME)).to("load-new-crimes");
	}

	@Provides
	public DB provideMongoDb(CrimzoneConfiguration config, Environment env) throws UnknownHostException {
		ManagedMongoClient mongoClient = config.getMongo().build();
		env.lifecycle().manage(mongoClient);
		return mongoClient.getDB(config.getMongo().getDbName());
	}
}
