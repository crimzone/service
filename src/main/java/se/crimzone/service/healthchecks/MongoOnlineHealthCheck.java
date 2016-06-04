package se.crimzone.service.healthchecks;

import com.codahale.metrics.health.HealthCheck;
import com.mongodb.DB;

public class MongoOnlineHealthCheck extends HealthCheck {

	public static final String NAME = "mongo-online";

	private final DB db;

	public MongoOnlineHealthCheck(DB db) {
		this.db = db;
	}

	@Override
	protected Result check() throws Exception {
		db.getCollectionNames();
		return Result.healthy();
	}
}
