package se.crimzone.service.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;
import se.crimzone.service.models.Crime;

import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkNotNull;

public class CrimesDao {
	public static final String CRIMES_COLLECTION_NAME = "crimes";
	private static final int MONGO_DUPLICATE_KEY_ERROR_CODE = 11000;

	private final JacksonDBCollection<Crime, Integer> collection;

	public CrimesDao(JacksonDBCollection<Crime, Integer> collection) {
		this.collection = checkNotNull(collection);
	}

	public Optional<Crime> find(int id) {
		try (DBCursor<Crime> cursor = collection.find(new BasicDBObject("_id", id))) {
			return cursor.hasNext() ? Optional.of(cursor.next()) : Optional.empty();
		}
	}

	public Stream<Crime> streamAll() {
		try (DBCursor<Crime> cursor = collection.find()) {
			return StreamSupport.stream(cursor.spliterator(), false).onClose(cursor::close);
		}
	}

	public boolean insert(Crime crime) {
		// TODO add mongodb validation for fields (especially time > 0)
		if (exists(crime)) { // TODO replace with mongodb unique index on all fields
			return false;
		}

		while (true) {
			try (DBCursor<Crime> cursor = nextIdCursor()) {
				int nextId = nextIdFromCursor(cursor);
				crime.setId(nextId);
				collection.insert(crime);
				return true;
			} catch (MongoException e) {
				if (e.getCode() == MONGO_DUPLICATE_KEY_ERROR_CODE) {
					continue;
				}
				throw e;
			}
		}
	}

	public int count() {
		try (DBCursor<Crime> cursor = collection.find()) {
			return cursor.count();
		}
	}

	private boolean exists(Crime crime) {
		// TODO use json serialization instead
		BasicDBObject query = new BasicDBObject();
		query.put("time", crime.getTime());
		query.put("latitude", crime.getLatitude());
		query.put("longitude", crime.getLongitude());
		query.put("title", crime.getTitle());
		query.put("description", crime.getDescription());
		try (DBCursor<Crime> cursor = collection.find(query)) {
			if (cursor.hasNext()) {
				return true;
			}
		}
		return false;
	}

	private DBCursor<Crime> nextIdCursor() {
		return collection.find(new BasicDBObject(), new BasicDBObject())
				.sort(new BasicDBObject("_id", -1))
				.limit(1);
	}

	private int nextIdFromCursor(DBCursor<Crime> cursor) {
		return cursor.hasNext() ? cursor.next().getId() + 1 : 1;
	}
}
