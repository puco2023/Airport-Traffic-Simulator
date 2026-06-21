package model;

public class Flight {
    private Airport from;
    private Airport to;
    private String departureTime;
    private int duration;

    public Flight(Airport fromAirport,Airport toAirport, String departureTime, int duration) {
        this.from = fromAirport;
        this.to = toAirport;
        this.departureTime = departureTime;
        this.duration = duration;
    }

    public Airport getFrom() {
        return from;
    }

    public Airport getTo() {
        return to;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public int getDuration() {
        return duration;
    }
}