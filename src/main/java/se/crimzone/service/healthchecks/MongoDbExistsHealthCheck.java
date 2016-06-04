package se.crimzone.service.healthchecks;

import com.codahale.metrics.health.HealthCheck;
import com.mongodb.DB;

public class MongoDbExistsHealthCheck extends HealthCheck {

	public static final String NAME = "mongoDbExists";

	private final DB db;

	public MongoDbExistsHealthCheck(DB db) {
		this.db = db;
	}

	@Override
	protected Result check() throws Exception {
		db.getCollectionNames();
		return Result.healthy();
	}
}
