package se.crimzone.service.controllers;

import io.swagger.inflector.models.RequestContext;
import io.swagger.inflector.models.ResponseContext;
import org.joda.time.DateTime;

public class CrimesController {

	public ResponseContext find(RequestContext request, Integer zoom, String text, String query, DateTime minTime, DateTime maxTime) {
		throw new UnsupportedOperationException("Not yet implemented"); // TODO: Implement
	}

	public ResponseContext findById(RequestContext request, Long id) {
		throw new UnsupportedOperationException("Not yet implemented"); // TODO: Implement
	}
}
