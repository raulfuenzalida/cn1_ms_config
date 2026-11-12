package duoc.cn1.ms_config.exception;

import java.math.BigDecimal;

public class InsufficientStockException extends RuntimeException {

	public InsufficientStockException(Long filamentId, BigDecimal requested, BigDecimal available) {
		super("Stock insuficiente para filamento ID " + filamentId
			+ ": solicitado " + requested + " g, disponible " + available + " g");
	}
}
