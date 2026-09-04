package duoc.cn1.ms_config.exception;

public class FilamentNotFoundException extends RuntimeException {

	public FilamentNotFoundException(Long id) {
		super("No se encontró el filamento solicitado con ID: " + id);
	}

	public FilamentNotFoundException(String message) {
		super(message);
	}
}
