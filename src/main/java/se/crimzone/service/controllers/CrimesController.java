package se.crimzone.service.controllers;

import io.swagger.inflector.models.RequestContext;
import io.swagger.inflector.models.ResponseContext;
import se.crimzone.service.dao.CrimesDao;
import se.crimzone.service.models.Crime;
import se.crimzone.service.models.Crimes;

import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("unused")
public class CrimesController {

	private final CrimesDao dao;
	private int lastCount = -1;
	private Crimes cachedCrimes;

	public CrimesController(CrimesDao dao) {
		this.dao = checkNotNull(dao);
	}

	public ResponseContext getAll(RequestContext request, List<Double> bounds, Integer zoom, String text,
								  Date minTime, Date maxTime) {
		return new ResponseContext().status(Response.Status.SERVICE_UNAVAILABLE);
		// TODO Implement getAll
	}

	public ResponseContext findById(RequestContext request, Integer id) {
		Optional<Crime> maybeCrime = dao.find(id);
		if (maybeCrime.isPresent()) {
			return new ResponseContext().entity(maybeCrime.get());
		} else {
			return new ResponseContext().status(Response.Status.NOT_FOUND);
		}
	}
}
