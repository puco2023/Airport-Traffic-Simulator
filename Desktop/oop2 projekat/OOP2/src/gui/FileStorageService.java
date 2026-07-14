package gui;

import Exceptions.FileException;
import model.Airport;
import model.Flight;
import model.Map;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.table.DefaultTableModel;

class FileStorageService {

    private final Frame owner;

    private final ArrayList<Airport> airports;
    private final ArrayList<String> airportCodes;
    private final ArrayList<Flight> flights;

    private final Choice fromAirportChoice;
    private final Choice toAirportChoice;

    private final DefaultTableModel airportTableModel;
    private final DefaultTableModel flightTableModel;

    private final Map map;
    private final Label statusLabel;

    FileStorageService(
            Frame owner,
            ArrayList<Airport> airports,
            ArrayList<String> airportCodes,
            ArrayList<Flight> flights,
            Choice fromAirportChoice,
            Choice toAirportChoice,
            DefaultTableModel airportTableModel,
            DefaultTableModel flightTableModel,
            Map map,
            Label statusLabel) {

        this.owner = owner;
        this.airports = airports;
        this.airportCodes = airportCodes;
        this.flights = flights;
        this.fromAirportChoice = fromAirportChoice;
        this.toAirportChoice = toAirportChoice;
        this.airportTableModel = airportTableModel;
        this.flightTableModel = flightTableModel;
        this.map = map;
        this.statusLabel = statusLabel;
    }

    void loadFile() {
        FileDialog fd = new FileDialog(owner, "Load file", FileDialog.LOAD);
        fd.setVisible(true);

        if (fd.getFile() == null) {
            statusLabel.setText("Status: Load cancelled.");
            return;
        }
        Path path = Path.of(fd.getDirectory(), fd.getFile());
        try {
        	String text = Files.readString(path);
        	if(fd.getFile().endsWith(".csv"))
        	{
        		loadCsv(text);
        	}
        	else if(fd.getFile().endsWith(".json"))
        	{
        		loadJson(text);
        	}
        	else
        	{
        		throw new FileException("unsuported file format.");
        	}
        }
        catch (IOException ioe)
        {
        	DialogUtil.showError(owner,"Error while loading.");
        }
        catch(FileException fe)
        {
        	 DialogUtil.showError(owner, fe.getMessage());
        }
        


    }

    private void loadJson(String text) throws FileException {
        text = text.trim();

        if (!text.startsWith("{")) {
            throw new FileException("Not valid file");
        }

        ArrayList<Airport> loadedAirports = new ArrayList<>();
        ArrayList<String> loadedAirportCodes = new ArrayList<>();
        ArrayList<Flight> loadedFlights = new ArrayList<>();

        Pattern airportPattern = Pattern.compile(
                "\\{\\s*\"code\"\\s*:\\s*\"([^\"]+)\"\\s*," +
                "\\s*\"name\"\\s*:\\s*\"([^\"]+)\"\\s*," +
                "\\s*\"x\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)\\s*," +
                "\\s*\"y\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)\\s*\\}"
        );

        Matcher airportMatcher = airportPattern.matcher(text);

        while (airportMatcher.find()) {
            String code = airportMatcher.group(1).trim();
            String name = airportMatcher.group(2).trim();
            double x = Double.parseDouble(airportMatcher.group(3).trim());
            double y = Double.parseDouble(airportMatcher.group(4).trim());

            if (!code.matches("[A-Z]{3}")) {
                throw new FileException("Invalid airport code: " + code);
            }

            if (loadedAirportCodes.contains(code)) {
                throw new FileException("Duplicate airport code: " + code);
            }

            Airport airport = new Airport(name, code, x, y);

            loadedAirports.add(airport);
            loadedAirportCodes.add(code);
        }

        if (loadedAirports.isEmpty()) {
            throw new FileException("file does not contain airports.");
        }

        Pattern flightPattern = Pattern.compile(
                "\\{\\s*\"from\"\\s*:\\s*\"([^\"]+)\"\\s*," +
                "\\s*\"to\"\\s*:\\s*\"([^\"]+)\"\\s*," +
                "\\s*\"departure\"\\s*:\\s*\"([^\"]+)\"\\s*," +
                "\\s*\"duration\"\\s*:\\s*(\\d+)\\s*\\}"
        );

        Matcher flightMatcher = flightPattern.matcher(text);

        while (flightMatcher.find()) {
            String fromCode = flightMatcher.group(1).trim();
            String toCode = flightMatcher.group(2).trim();
            String departure = flightMatcher.group(3).trim();
            int duration = Integer.parseInt(flightMatcher.group(4).trim());

            Airport from = AirportLookup.findByCode(loadedAirports, fromCode);
            Airport to = AirportLookup.findByCode(loadedAirports, toCode);

            if (from == null) {
                throw new FileException("Unknown from airport: " + fromCode);
            }

            if (to == null) {
                throw new FileException("Unknown to airport: " + toCode);
            }

            if (fromCode.equals(toCode)) {
                throw new FileException("Flight cannot have same from and to airport.");
            }

            if (!departure.matches("([01]\\d|2[0-3]):[0-5]\\d")) {
                throw new FileException("Invalid departure time: " + departure);
            }

            if (duration <= 0) {
                throw new FileException("Invalid flight duration.");
            }

            Flight flight = new Flight(from, to, departure, duration);
            loadedFlights.add(flight);
        }

        clearAllData();

        for (Airport airport : loadedAirports) {
            airports.add(airport);
            airportCodes.add(airport.getCode());

            fromAirportChoice.add(airport.getCode());
            toAirportChoice.add(airport.getCode());

            airportTableModel.addRow(new Object[]{
                    airport.getCode(),
                    airport.getName(),
                    airport.getX(),
                    airport.getY(),
                    true
            });
        }

        for (Flight flight : loadedFlights) {
            flights.add(flight);

            flightTableModel.addRow(new Object[]{
                    flight.getFrom().getCode(),
                    flight.getTo().getCode(),
                    flight.getDepartureTime(),
                    flight.getDuration()
            });
        }

        map.repaint();
    }

    private void loadCsv(String text) throws FileException {
        String[] lines = text.split("\\R");

        if (lines.length == 0 || text.trim().isEmpty()) {
            throw new FileException("Empty CSV file");
        }

        ArrayList<Airport> loadedAirports = new ArrayList<>();
        ArrayList<String> loadedAirportCodes = new ArrayList<>();
        ArrayList<Flight> loadedFlights = new ArrayList<>();

        String section = "";

        for (String rawLine : lines) {
            String line = rawLine.trim();

            if (line.isEmpty()) {
                continue;
            }

            String sectionMarker = line.startsWith("#") ? line.substring(2).trim() : line;

            if (sectionMarker.equalsIgnoreCase("AIRPORTS")) {
                section = "AIRPORTS";
                continue;
            }

            if (sectionMarker.equalsIgnoreCase("FLIGHTS")) {
                section = "FLIGHTS";
                continue;
            }

            if (line.equalsIgnoreCase("code,name,x,y")) {
                continue;
            }

            if (line.equalsIgnoreCase("from,to,departure,duration")) {
                continue;
            }

            String[] parts = line.split(",", -1);

            if (section.equals("AIRPORTS")) {
                if (parts.length != 4) {
                    throw new FileException("Invalid airport CSV line: " + line);
                }

                String code = parts[0].trim();
                String name = parts[1].trim();
                double x = Double.parseDouble(parts[2].trim());
                double y = Double.parseDouble(parts[3].trim());

                if (!code.matches("[A-Z]{3}")) {
                    throw new FileException("Invalid airport code: " + code);
                }

                if (loadedAirportCodes.contains(code)) {
                    throw new FileException("Duplicate airport code: " + code);
                }

                Airport airport = new Airport(name, code, x, y);

                loadedAirports.add(airport);
                loadedAirportCodes.add(code);

            } else if (section.equals("FLIGHTS")) {
                if (parts.length != 4) {
                    throw new FileException("Invalid flight CSV line: " + line);
                }

                String fromCode = parts[0].trim();
                String toCode = parts[1].trim();
                String departure = parts[2].trim();
                int duration = Integer.parseInt(parts[3].trim());

                Airport from = AirportLookup.findByCode(loadedAirports, fromCode);
                Airport to = AirportLookup.findByCode(loadedAirports, toCode);

                if (from == null || to == null) {
                    throw new FileException("Flight uses unknown airport.");
                }

                if (fromCode.equals(toCode)) {
                    throw new FileException("Flight cannot have same from and to airport.");
                }

                if (!departure.matches("([01]\\d|2[0-3]):[0-5]\\d")) {
                    throw new FileException("Invalid departure time: " + departure);
                }

                if (duration <= 0) {
                    throw new FileException("Invalid flight duration.");
                }

                Flight flight = new Flight(from, to, departure, duration);
                loadedFlights.add(flight);

            } else {
                throw new FileException("CSV must start with AIRPORTS section.");
            }
        }

        if (loadedAirports.isEmpty()) {
            throw new FileException("CSV does not contain airports.");
        }

        clearAllData();

        for (Airport airport : loadedAirports) {
            airports.add(airport);
            airportCodes.add(airport.getCode());

            fromAirportChoice.add(airport.getCode());
            toAirportChoice.add(airport.getCode());

            airportTableModel.addRow(new Object[]{
                    airport.getCode(),
                    airport.getName(),
                    airport.getX(),
                    airport.getY(),
                    true
            });
        }

        for (Flight flight : loadedFlights) {
            flights.add(flight);

            flightTableModel.addRow(new Object[]{
                    flight.getFrom().getCode(),
                    flight.getTo().getCode(),
                    flight.getDepartureTime(),
                    flight.getDuration()

            });
        }

        map.repaint();
    }

    private void clearAllData() {
        airports.clear();
        flights.clear();
        airportCodes.clear();

        fromAirportChoice.removeAll();
        toAirportChoice.removeAll();

        airportTableModel.setRowCount(0);
        flightTableModel.setRowCount(0);
        map.clearSelection();
    }

    void saveFile() {
        String format = askSaveFormat();

        if (format == null) {
            statusLabel.setText("Status: Save cancelled.");
            return;
        }

        FileDialog fd = new FileDialog(owner, "Save file", FileDialog.SAVE);
        fd.setVisible(true);

        if (fd.getFile() == null) {
            statusLabel.setText("Status: Save cancelled.");
            return;
        }

        File file = new File(fd.getDirectory(), fd.getFile());

        try {
            String text;

            if (format.equals("CSV")) {
                if (!file.getName().toLowerCase().endsWith(".csv")) {
                    file = new File(file.getAbsolutePath() + ".csv");
                }

                text = makeCsvText();

            } else {
                if (!file.getName().toLowerCase().endsWith(".json")) {
                    file = new File(file.getAbsolutePath() + ".json");
                }

                text = makeJsonText();
            }

            Files.writeString(file.toPath(), text);

            statusLabel.setText("Status: File saved successfully.");

        } catch (Exception ex) {
            statusLabel.setText("Status: Save error.");
        }
    }

    private String askSaveFormat() {
        final String[] selectedFormat = { null };

        Dialog dialog = new Dialog(owner, "Choose format", true);
        dialog.setLayout(new FlowLayout());
        dialog.setSize(250, 120);

        Label label = new Label("Save as:");

        Choice choice = new Choice();
        choice.add("CSV");
        choice.add("JSON");

        Button okButton = new Button("OK");
        Button cancelButton = new Button("Cancel");

        okButton.addActionListener(e -> {
            selectedFormat[0] = choice.getSelectedItem();
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> {
            selectedFormat[0] = null;
            dialog.dispose();
        });

        dialog.add(label);
        dialog.add(choice);
        dialog.add(okButton);
        dialog.add(cancelButton);

        dialog.setVisible(true);

        return selectedFormat[0];
    }

    private String makeCsvText() {
        StringBuilder sb = new StringBuilder();

        sb.append("AIRPORTS\n");
        sb.append("code,name,x,y\n");

        for (Airport airport : airports) {
            sb.append(airport.getCode()).append(",");
            sb.append(airport.getName()).append(",");
            sb.append(airport.getX()).append(",");
            sb.append(airport.getY()).append("\n");
        }

        sb.append("\nFLIGHTS\n");
        sb.append("from,to,departure,duration\n");

        for (Flight flight : flights) {
            sb.append(flight.getFrom().getCode()).append(",");
            sb.append(flight.getTo().getCode()).append(",");
            sb.append(flight.getDepartureTime()).append(",");
            sb.append(flight.getDuration()).append("\n");
        }

        return sb.toString();
    }

    private String makeJsonText() {
        StringBuilder sb = new StringBuilder();

        sb.append("{\n");

        sb.append("  \"airports\": [\n");

        for (int i = 0; i < airports.size(); i++) {
            Airport airport = airports.get(i);

            sb.append("    {");
            sb.append("\"code\":\"").append(escapeJson(airport.getCode())).append("\",");
            sb.append("\"name\":\"").append(escapeJson(airport.getName())).append("\",");
            sb.append("\"x\":").append(airport.getX()).append(",");
            sb.append("\"y\":").append(airport.getY());
            sb.append("}");

            if (i < airports.size() - 1) {
                sb.append(",");
            }

            sb.append("\n");
        }

        sb.append("  ],\n");

        sb.append("  \"flights\": [\n");

        for (int i = 0; i < flights.size(); i++) {
            Flight flight = flights.get(i);

            sb.append("    {");
            sb.append("\"from\":\"").append(escapeJson(flight.getFrom().getCode())).append("\",");
            sb.append("\"to\":\"").append(escapeJson(flight.getTo().getCode())).append("\",");
            sb.append("\"departure\":\"").append(escapeJson(flight.getDepartureTime())).append("\",");
            sb.append("\"duration\":").append(flight.getDuration());
            sb.append("}");

            if (i < flights.size() - 1) {
                sb.append(",");
            }

            sb.append("\n");
        }

        sb.append("  ]\n");
        sb.append("}");

        return sb.toString();
    }

    private String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}
