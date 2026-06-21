package model;

public class SimulationFlight {
    private Flight flight;

    private int plannedDepartureMinutes;
    private int realDepartureMinutes;
    private int arrivalMinutes;
    
    private boolean active = false;
    private boolean finished = false;

    public SimulationFlight(Flight flight, int plannedDepartureMinutes, int realDepartureMinutes) {
        this.flight = flight;
        this.plannedDepartureMinutes = plannedDepartureMinutes;
        this.realDepartureMinutes = realDepartureMinutes;
        this.arrivalMinutes = realDepartureMinutes + flight.getDuration();
    }

    public Flight getFlight() {
        return flight;
    }

    public int getRealDepartureMinutes() {
        return realDepartureMinutes;
    }

    public int getArrivalMinutes() {
        return arrivalMinutes;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

}