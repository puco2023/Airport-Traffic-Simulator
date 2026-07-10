package Exceptions;

public class ValidationException extends AppException {

	public ValidationException(String message) {
		super("Validation exception: " + message);
	}
	
	public ValidationException(String message, Throwable cause) {
		super("Validation exception: " + message, cause);
	}
}
