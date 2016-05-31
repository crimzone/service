package se.crimzone.service.collector.brottsplatskartan;

public class GaveUpTryingException extends Exception {

	public GaveUpTryingException(String url, int attempts) {
		super(String.format("Gave up trying to get %s after %d attempts", url, attempts));
	}
}
