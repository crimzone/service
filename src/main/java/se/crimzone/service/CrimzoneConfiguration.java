package se.crimzone.service;

import com.commercehub.dropwizard.mongo.MongoClientFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

class CrimzoneConfiguration extends Configuration {

	@NotBlank
	private String inflectorFile;

	@Valid
	@NotNull
	private MongoClientFactory mongo;

	@JsonProperty
	public String getInflectorFile() {
		return inflectorFile;
	}

	public void setInflectorFile(String inflectorFile) {
		this.inflectorFile = inflectorFile;
	}

	@JsonProperty
	public MongoClientFactory getMongo() {
		return mongo;
	}

	@JsonProperty
	public void setMongo(MongoClientFactory mongo) {
		this.mongo = mongo;
	}
}
