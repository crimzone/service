package se.crimzone.service.controllers;

import io.swagger.inflector.models.RequestContext;
import io.swagger.inflector.models.ResponseContext;
import se.crimzone.service.dao.CrimesDao;
import se.crimzone.service.models.Crime;
import se.crimzone.service.models.Crimes;

import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
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

	public ResponseContext getAll(RequestContext request) {
		int count = dao.count();
		if (count == lastCount && cachedCrimes != null) {
			return new ResponseContext().entity(cachedCrimes);
		}
		lastCount = count;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			try (ObjectOutputStream ous = new ObjectOutputStream(baos)) {
				dao.streamAll().forEachOrdered((crime) -> writeCrime(crime, ous));
				byte[] bytes = baos.toByteArray();
				Crimes crimes = new Crimes();
				crimes.setData(bytes);
				cachedCrimes = crimes;
				return new ResponseContext().entity(crimes);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public ResponseContext findById(RequestContext request, Integer id) {
		Optional<Crime> maybeCrime = dao.find(id);
		if (maybeCrime.isPresent()) {
			return new ResponseContext().entity(maybeCrime.get());
		} else {
			return new ResponseContext().status(Response.Status.NOT_FOUND);
		}
	}

	private void writeCrime(Crime crime, ObjectOutputStream stream) {
		try {
			stream.writeInt(crime.getId());
			stream.writeInt(crime.getTime());
			stream.writeFloat(crime.getLatitude());
			stream.writeFloat(crime.getLongitude());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
