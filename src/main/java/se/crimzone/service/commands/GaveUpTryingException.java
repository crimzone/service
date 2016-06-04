package se.crimzone.service.commands;

class GaveUpTryingException extends Exception {

	GaveUpTryingException(String url, int attempts) {
		super(String.format("Gave up trying to get %s after %d attempts", url, attempts));
	}
}
