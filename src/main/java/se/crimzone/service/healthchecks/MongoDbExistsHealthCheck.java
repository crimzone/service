package se.crimzone.service.healthchecks;

import com.codahale.metrics.health.HealthCheck;
import com.google.inject.Inject;
import com.mongodb.DB;

public class MongoDbExistsHealthCheck extends HealthCheck {

	private final DB mongo;

	@Inject
	public MongoDbExistsHealthCheck(DB mongo) {
		this.mongo = mongo;
	}

	@Override
	protected Result check() throws Exception {
		mongo.getName();
		return Result.healthy();
	}
}
