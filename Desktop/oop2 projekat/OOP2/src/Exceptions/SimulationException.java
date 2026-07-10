package Exceptions;

public class SimulationException extends AppException{
    public SimulationException(String message) {
        super("Simulation exception: " + message);
    }

    public SimulationException(String message, Throwable cause) {
        super("Simulation exception: " + message, cause);
    }
}
