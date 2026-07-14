package gui;

import Exceptions.ValidationException;
import model.Airport;
import model.Flight;
import model.Map;
import java.awt.Choice;
import java.awt.Label;
import java.awt.TextField;
import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;

public class AirportFlightService {

    private final TextField airportNameField;
    private final TextField airportCodeField;
    private final TextField airportXField;
    private final TextField airportYField;

    private final Choice fromAirportChoice;
    private final Choice toAirportChoice;
    private final TextField departureTimeField;
    private final TextField flightDurationField;

    private final DefaultTableModel airportTableModel;
    private final DefaultTableModel flightTableModel;

    private final ArrayList<String> airportCodes;
    private final ArrayList<Airport> airports;
    private final ArrayList<Flight> flights;

    private final Map map;
    private final Label statusLabel;
    public ArrayList<Flight> getFlights()
    {
    	return flights;
    }
    AirportFlightService(
            TextField airportNameField,
            TextField airportCodeField,
            TextField airportXField,
            TextField airportYField,
            Choice fromAirportChoice,
            Choice toAirportChoice,
            TextField departureTimeField,
            TextField flightDurationField,
            DefaultTableModel airportTableModel,
            DefaultTableModel flightTableModel,
            ArrayList<String> airportCodes,
            ArrayList<Airport> airports,
            ArrayList<Flight> flights,
            Map map,
            Label statusLabel) {

        this.airportNameField = airportNameField;
        this.airportCodeField = airportCodeField;
        this.airportXField = airportXField;
        this.airportYField = airportYField;
        this.fromAirportChoice = fromAirportChoice;
        this.toAirportChoice = toAirportChoice;
        this.departureTimeField = departureTimeField;
        this.flightDurationField = flightDurationField;
        this.airportTableModel = airportTableModel;
        this.flightTableModel = flightTableModel;
        this.airportCodes = airportCodes;
        this.airports = airports;
        this.flights = flights;
        this.map = map;
        this.statusLabel = statusLabel;
    }

    void addAirport() throws ValidationException {

        String airportName = airportNameField.getText().trim();
        String airportCode = airportCodeField.getText().trim();
        String airportXString = airportXField.getText().trim();
        String airportYString = airportYField.getText().trim();

        if (airportName.isEmpty()) {
            throw new ValidationException(
                "Airport name cannot be empty."
            );
        }

        if (airportCode.isEmpty()) {
            throw new ValidationException(
                "Airport code cannot be empty."
            );
        }

        if (airportXString.isEmpty()) {
            throw new ValidationException(
                "X coordinate cannot be empty."
            );
        }

        if (airportYString.isEmpty()) {
            throw new ValidationException(
                "Y coordinate cannot be empty."
            );
        }

        if (!airportCode.matches("[A-Z]{3}")) {
            throw new ValidationException(
                "Airport code must contain exactly "
                + "3 uppercase letters, for example BEG."
            );
        }


        double airportX;
        double airportY;

        try {
            airportX = Double.parseDouble(airportXString);
            airportY = Double.parseDouble(airportYString);
        }
        catch (NumberFormatException e) {
            throw new ValidationException(
                "X and Y coordinates must be valid numbers."
            );
        }

        if (airportCodes.contains(airportCode)) {
            throw new ValidationException(
                "Airport with code "
                + airportCode
                + " already exists."
            );
        }

        if (airportX < -180 || airportX > 180) {
            throw new ValidationException(
                "X coordinate must be between -180 and 180."
            );
        }

        if (airportY < -90 || airportY > 90) {
            throw new ValidationException(
                "Y coordinate must be between -90 and 90."
            );
        }
        if(isCoordinateOccupied(airportX,airportY))
        {
        	throw new ValidationException("Two airports cant be that close.");
        }
        Airport airport = new Airport(
            airportName,
            airportCode,
            airportX,
            airportY
        );

        airports.add(airport);
        airportCodes.add(airportCode);

        fromAirportChoice.add(airportCode);
        toAirportChoice.add(airportCode);

        airportTableModel.addRow(new Object[]{
            airportCode,
            airportName,
            airportX,
            airportY,
            true
        });

        airportNameField.setText("");
        airportCodeField.setText("");
        airportXField.setText("");
        airportYField.setText("");

        map.repaint();

        statusLabel.setText(
            "Status: Airport added."
        );
    }

    void addFlight() throws ValidationException {

        if (fromAirportChoice.getItemCount() < 2
                || toAirportChoice.getItemCount() < 2) {

            throw new ValidationException(
                "You must add at least two airports first."
            );
        }
        String fromAirportCode = fromAirportChoice.getSelectedItem();
        String toAirportCode = toAirportChoice.getSelectedItem();
        String departureTime = departureTimeField.getText().trim();
        String flightDurationString = flightDurationField.getText().trim();
        if (departureTime.isEmpty()) {
            throw new ValidationException(
                "Departure time cannot be empty."
            );
        }

        if (flightDurationString.isEmpty()) {
            throw new ValidationException(
                "Flight duration cannot be empty."
            );
        }
        int flightDuration;

        try {
            flightDuration =
                    Integer.parseInt(
                        flightDurationString
                    );
        }
        catch (NumberFormatException e) {

            throw new ValidationException(
                "Flight duration must be an integer."
            );
        }


        if (fromAirportCode.equals(
                toAirportCode)) {

            throw new ValidationException(
                "From and To airports "
                + "cannot be the same."
            );
        }


        if (!departureTime.matches(
                "([01]\\d|2[0-3]):[0-5]\\d")) {

            throw new ValidationException(
                "Departure time must be "
                + "in HH:mm format, "
                + "for example 13:20."
            );
        }


        if (flightDuration <= 0) {

            throw new ValidationException(
                "Flight duration must "
                + "be a positive number."
            );
        }


        Airport from =
                findAirportByCode(
                    fromAirportCode
                );

        Airport to =
                findAirportByCode(
                    toAirportCode
                );


        if (from == null || to == null) {

            throw new ValidationException(
                "Selected airport "
                + "does not exist."
            );
        }


        Flight flight = new Flight(from,to,departureTime,flightDuration);

        flights.add(flight);
        flightTableModel.addRow(
            new Object[]{
                fromAirportCode,
                toAirportCode,
                departureTime,
                flightDuration
            }
        );
        departureTimeField.setText("");
        flightDurationField.setText("");
        map.repaint();
        statusLabel.setText("Status: Flight added.");
    }

    private Airport findAirportByCode(String code) {
        return AirportLookup.findByCode(airports, code);
    }

    private boolean isCoordinateOccupied(double x, double y) {

    	for(Airport a : airports)
    	{
    		if(Math.abs(a.getX()-x)==0 && Math.abs(a.getY()-y)==0)
    		{
    			return true;
    		}
    	}
    	return false;
    }
}
