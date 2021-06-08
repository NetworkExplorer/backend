package at.networkexplorer.backend.exceptions;

public class InsufficientPermissionsException  extends RuntimeException {

    public InsufficientPermissionsException(String message) {
        super(message);
    }

    public InsufficientPermissionsException(String message, Throwable cause) {
        super(message, cause);
    }
}
