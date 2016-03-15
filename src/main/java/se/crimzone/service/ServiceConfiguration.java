package se.crimzone.service;

import io.dropwizard.Configuration;

class ServiceConfiguration extends Configuration {
	private String config;

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}
}
