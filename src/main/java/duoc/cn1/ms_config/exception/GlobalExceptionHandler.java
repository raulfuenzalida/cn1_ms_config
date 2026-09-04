package duoc.cn1.ms_config.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(FilamentNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleFilamentNotFoundException(
			FilamentNotFoundException ex, WebRequest request) {
		ErrorResponse error = new ErrorResponse(
			LocalDateTime.now(),
			HttpStatus.NOT_FOUND.value(),
			HttpStatus.NOT_FOUND.getReasonPhrase(),
			"FILAMENT_NOT_FOUND",
			ex.getMessage(),
			request.getDescription(false).replace("uri=", "")
		);
		return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(FilamentDuplicateException.class)
	public ResponseEntity<ErrorResponse> handleFilamentDuplicateException(
			FilamentDuplicateException ex, WebRequest request) {
		ErrorResponse error = new ErrorResponse(
			LocalDateTime.now(),
			HttpStatus.CONFLICT.value(),
			HttpStatus.CONFLICT.getReasonPhrase(),
			"FILAMENT_DUPLICATE",
			ex.getMessage(),
			request.getDescription(false).replace("uri=", "")
		);
		return new ResponseEntity<>(error, HttpStatus.CONFLICT);
	}

	@ExceptionHandler(PrintingConfigNotFoundException.class)
	public ResponseEntity<ErrorResponse> handlePrintingConfigNotFoundException(
			PrintingConfigNotFoundException ex, WebRequest request) {
		ErrorResponse error = new ErrorResponse(
			LocalDateTime.now(),
			HttpStatus.NOT_FOUND.value(),
			HttpStatus.NOT_FOUND.getReasonPhrase(),
			"PRINTING_CONFIG_NOT_FOUND",
			ex.getMessage(),
			request.getDescription(false).replace("uri=", "")
		);
		return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(
			MethodArgumentNotValidException ex, WebRequest request) {
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach(error -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
		});

		ErrorResponse error = new ErrorResponse(
			LocalDateTime.now(),
			HttpStatus.BAD_REQUEST.value(),
			HttpStatus.BAD_REQUEST.getReasonPhrase(),
			"VALIDATION_ERROR",
			"Error de validación en los campos enviados",
			request.getDescription(false).replace("uri=", ""),
			errors
		);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
			IllegalArgumentException ex, WebRequest request) {
		ErrorResponse error = new ErrorResponse(
			LocalDateTime.now(),
			HttpStatus.BAD_REQUEST.value(),
			HttpStatus.BAD_REQUEST.getReasonPhrase(),
			"INVALID_OPERATION",
			ex.getMessage(),
			request.getDescription(false).replace("uri=", "")
		);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGlobalException(
			Exception ex, WebRequest request) {
		ErrorResponse error = new ErrorResponse(
			LocalDateTime.now(),
			HttpStatus.INTERNAL_SERVER_ERROR.value(),
			HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
			"INTERNAL_ERROR",
			"Error interno del servidor",
			request.getDescription(false).replace("uri=", "")
		);
		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
