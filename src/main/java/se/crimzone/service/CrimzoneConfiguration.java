package se.crimzone.service;

import com.commercehub.dropwizard.mongo.MongoClientFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

class CrimzoneConfiguration extends Configuration {

	@Valid
	@NotNull
	private MongoClientFactory mongo;

	@JsonProperty
	public MongoClientFactory getMongo() {
		return mongo;
	}

	@JsonProperty
	public void setMongo(MongoClientFactory mongo) {
		this.mongo = mongo;
	}
}
