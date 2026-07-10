package Exceptions;

public class FileException extends AppException{
    public FileException(String message) {
        super("File exception: " + message);
    }

    public FileException(String message, Throwable cause) {
        super("File exception: " + message, cause);
    }
}
