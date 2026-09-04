package duoc.cn1.ms_config.exception;

public class FilamentDuplicateException extends RuntimeException {

	public FilamentDuplicateException(String name, String color) {
		super("Ya existe un filamento con el nombre '" + name + "' y color '" + color + "'");
	}

	public FilamentDuplicateException(String message) {
		super(message);
	}
}
