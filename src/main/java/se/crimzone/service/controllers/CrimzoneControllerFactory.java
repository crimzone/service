package se.crimzone.service.controllers;

import io.swagger.inflector.config.ControllerFactory;
import se.crimzone.service.dao.CrimesDao;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

public class CrimzoneControllerFactory implements ControllerFactory {

	private final Map<Class, Supplier> controllers = new HashMap<>();

	public CrimzoneControllerFactory(CrimesDao crimesDao) {
		checkNotNull(crimesDao);
		controllers.put(CrimesController.class, (() -> new CrimesController(crimesDao)));
	}

	@Override
	public Object instantiateController(Class clazz) throws IllegalAccessException, InstantiationException {
		Supplier supplier = controllers.get(clazz);
		if (supplier == null) {
			throw new NullPointerException("supplier");
		}
		return supplier.get();
	}
}
