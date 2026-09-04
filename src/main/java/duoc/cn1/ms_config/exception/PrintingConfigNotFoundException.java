package duoc.cn1.ms_config.exception;

public class PrintingConfigNotFoundException extends RuntimeException {

	public PrintingConfigNotFoundException() {
		super("No se encontró la configuración de impresión");
	}

	public PrintingConfigNotFoundException(String message) {
		super(message);
	}
}
