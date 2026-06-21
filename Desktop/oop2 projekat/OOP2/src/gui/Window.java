package gui;

import model.Airport;
import model.Flight;
import model.Map;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.File;
import java.nio.file.Files;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.Dimension;
import javax.swing.event.TableModelEvent;
public class Window extends Frame {

    private TextField airportNameField = new TextField(20);
    private TextField airportCodeField = new TextField(4);
    private TextField airportXField = new TextField(5);
    private TextField airportYField = new TextField(5);

    private Choice fromAirportChoice = new Choice();
    private Choice toAirportChoice = new Choice();
    private TextField departureTimeField = new TextField(6);
    private TextField flightDurationField = new TextField(5);

    private DefaultTableModel airportTableModel = new DefaultTableModel(
            new Object[]{"Code", "Name", "X", "Y", "Visible"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 4;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 4) {
                return Boolean.class;
            }

            return String.class;
        }
    };
    private JTable airportTable = new JTable(airportTableModel);

    private DefaultTableModel flightTableModel = new DefaultTableModel(
            new Object[]{"From", "To", "Departure", "Duration"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private JTable flightTable = new JTable(flightTableModel);

    private Label statusLabel = new Label("Status: Ready.");


    private ArrayList<String> airportCodes = new ArrayList<>();
    private ArrayList<Airport> airports = new ArrayList<>();
    private ArrayList<Flight> flights = new ArrayList<>();

    private Map map = new Map(airports, flights);
    public Window() {
        super("Air Traffic Manager - Phase A");

        setSize(1000, 600);
        setLayout(new BorderLayout());

        add(createTopPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
        airportTableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 4) {
                int row = e.getFirstRow();

                if (row >= 0 && row < airports.size()) {
                    boolean visible = (Boolean) airportTableModel.getValueAt(row, 4);
                    airports.get(row).setVisible(visible);
                    map.repaint();
                }
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                map.stopBlinking();
                dispose();
            }
        });

        setVisible(true);
    }
    
    private Panel createTopPanel() {
        Panel panel = new Panel();
        panel.setLayout(new FlowLayout());

        Button load = new Button("Load");
        Button save = new Button("Save");

        load.addActionListener(e -> loadFile());
        save.addActionListener(e -> saveFile());

        panel.add(load);
        panel.add(save);

        return panel;
    }

    private Panel createMainPanel() {
        Panel mainPanel = new Panel(new BorderLayout());

        Panel topPanel = new Panel(new GridLayout(1, 2));

        topPanel.add(createAirportPanel());
        topPanel.add(createFlightPanel());
        topPanel.setPreferredSize(new Dimension(1000, 350));

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(map, BorderLayout.CENTER);

        return mainPanel;
    }
    private Panel createAirportPanel() {
        Panel mainPanel = new Panel();
        mainPanel.setLayout(new BorderLayout());

        Label titleLabel = new Label("Airports");
        titleLabel.setAlignment(Label.CENTER);

        Panel inputPanel = new Panel();
        inputPanel.setLayout(new GridLayout(5, 2));

        inputPanel.add(new Label("Name:"));
        inputPanel.add(airportNameField);

        inputPanel.add(new Label("Code:"));
        inputPanel.add(airportCodeField);

        inputPanel.add(new Label("X:"));
        inputPanel.add(airportXField);

        inputPanel.add(new Label("Y:"));
        inputPanel.add(airportYField);

        Button airportButton = new Button("Add airport");
        airportButton.addActionListener(e -> addAirport());

        inputPanel.add(new Label(""));
        inputPanel.add(airportButton);

        Panel tablePanel = new Panel();
        tablePanel.setLayout(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane(airportTable);
        scrollPane.setPreferredSize(new Dimension(450, 180));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(tablePanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    private Panel createFlightPanel() {
        Panel mainPanel = new Panel();
        mainPanel.setLayout(new BorderLayout());

        Label titleLabel = new Label("Flights");
        titleLabel.setAlignment(Label.CENTER);

        Panel inputPanel = new Panel();
        inputPanel.setLayout(new GridLayout(5, 2));

        inputPanel.add(new Label("From:"));
        inputPanel.add(fromAirportChoice);

        inputPanel.add(new Label("To:"));
        inputPanel.add(toAirportChoice);

        inputPanel.add(new Label("Departure:"));
        inputPanel.add(departureTimeField);

        inputPanel.add(new Label("Duration:"));
        inputPanel.add(flightDurationField);

        Button addFlightButton = new Button("Add flight");
        addFlightButton.addActionListener(e -> addFlight());

        inputPanel.add(new Label(""));
        inputPanel.add(addFlightButton);

        Panel tablePanel = new Panel();
        tablePanel.setLayout(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane(flightTable);
        scrollPane.setPreferredSize(new Dimension(450, 180));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(tablePanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    private void addAirport() {
        try {
            String airportName = airportNameField.getText().trim();
            String airportCode = airportCodeField.getText().trim();

            double airportX = Double.parseDouble(airportXField.getText().trim());
            double airportY = Double.parseDouble(airportYField.getText().trim());

            if (airportName.isEmpty()) {
                throw new IllegalArgumentException("Airport name cannot be empty.");
            }

            if (!airportCode.matches("[A-Z]{3}")) {
                throw new IllegalArgumentException("Airport code must contain exactly 3 uppercase letters, for example BEG.");
            }

            if (airportCodes.contains(airportCode)) {
                throw new IllegalArgumentException("Airport with code " + airportCode + " already exists.");
            }

            if (airportX < -180 || airportX > 180) {
                throw new IllegalArgumentException("X coordinate must be between -180 and 180.");
            }

            if (airportY < -90 || airportY > 90) {
                throw new IllegalArgumentException("Y coordinate must be between -90 and 90.");
            }

            Airport airport = new Airport(airportName, airportCode, airportX, airportY);

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
            statusLabel.setText("Status: Airport added.");

        } catch (NumberFormatException e) {
            statusLabel.setText("Error: X and Y coordinates must be numbers.");
        } catch (IllegalArgumentException e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private void addFlight() {
        try {
            if (fromAirportChoice.getItemCount() < 2 || toAirportChoice.getItemCount() < 2) {
                throw new IllegalArgumentException("You must add at least two airports first.");
            }

            String fromAirportCode = fromAirportChoice.getSelectedItem();
            String toAirportCode = toAirportChoice.getSelectedItem();

            String departureTime = departureTimeField.getText().trim();
            int flightDuration = Integer.parseInt(flightDurationField.getText().trim());

            if (fromAirportCode.equals(toAirportCode)) {
                throw new IllegalArgumentException("From and To airports cannot be the same.");
            }

            if (!departureTime.matches("([01]\\d|2[0-3]):[0-5]\\d")) {
                throw new IllegalArgumentException("Departure time must be in HH:mm format, for example 13:20.");
            }

            if (flightDuration <= 0) {
                throw new IllegalArgumentException("Flight duration must be a positive number.");
            }

            Airport from = findAirportByCode(fromAirportCode);
            Airport to = findAirportByCode(toAirportCode);

            if (from == null || to == null) {
                throw new IllegalArgumentException("Selected airport does not exist.");
            }

            Flight flight = new Flight(from, to, departureTime, flightDuration);
            flights.add(flight);

            flightTableModel.addRow(new Object[]{
                    fromAirportCode,
                    toAirportCode,
                    departureTime,
                    flightDuration
            });

            departureTimeField.setText("");
            flightDurationField.setText("");

            map.repaint();
            statusLabel.setText("Status: Flight added.");
            

        } catch (NumberFormatException e) {
            statusLabel.setText("Error: Flight duration must be an integer.");
        } catch (IllegalArgumentException e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private Airport findAirportByCode(String code) {
        for (Airport airport : airports) {
            if (airport.getCode().equals(code)) {
                return airport;
            }
        }

        return null;
    }

    private Airport findAirportByCodeInList(ArrayList<Airport> list, String code) {
        for (Airport airport : list) {
            if (airport.getCode().equals(code)) {
                return airport;
            }
        }

        return null;
    }

    private void loadFile() {
        FileDialog fd = new FileDialog(this, "Load file", FileDialog.LOAD);
        fd.setVisible(true);

        if (fd.getFile() == null) {
            statusLabel.setText("Status: Load cancelled.");
            return;
        }

        File file = new File(fd.getDirectory(), fd.getFile());

        try {
            String text = Files.readString(file.toPath());

            try {
                loadCsv(text);
                statusLabel.setText("Status: CSV loaded successfully.");
            } catch (Exception csvError) {
                try {
                    loadJson(text);
                    statusLabel.setText("Status: JSON loaded successfully.");
                } catch (Exception jsonError) {
                    csvError.printStackTrace();
                    jsonError.printStackTrace();
                    statusLabel.setText("Status: File is not valid CSV or JSON.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Status: Cannot read file.");
        }
    }

    private void loadJson(String text) throws Exception {
        text = text.trim();

        if (!text.startsWith("{")) {
            throw new Exception("Not valid JSON");
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
                throw new Exception("Invalid airport code: " + code);
            }

            if (loadedAirportCodes.contains(code)) {
                throw new Exception("Duplicate airport code: " + code);
            }

            Airport airport = new Airport(name, code, x, y);

            loadedAirports.add(airport);
            loadedAirportCodes.add(code);
        }

        if (loadedAirports.isEmpty()) {
            throw new Exception("JSON does not contain airports.");
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

            Airport from = findAirportByCodeInList(loadedAirports, fromCode);
            Airport to = findAirportByCodeInList(loadedAirports, toCode);

            if (from == null) {
                throw new Exception("Unknown from airport: " + fromCode);
            }

            if (to == null) {
                throw new Exception("Unknown to airport: " + toCode);
            }

            if (fromCode.equals(toCode)) {
                throw new Exception("Flight cannot have same from and to airport.");
            }

            if (!departure.matches("([01]\\d|2[0-3]):[0-5]\\d")) {
                throw new Exception("Invalid departure time: " + departure);
            }

            if (duration <= 0) {
                throw new Exception("Invalid flight duration.");
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

    private void loadCsv(String text) throws Exception {
        String[] lines = text.split("\\R");

        if (lines.length == 0 || text.trim().isEmpty()) {
            throw new Exception("Empty CSV file");
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

            if (line.equalsIgnoreCase("AIRPORTS")) {
                section = "AIRPORTS";
                continue;
            }

            if (line.equalsIgnoreCase("FLIGHTS")) {
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
                    throw new Exception("Invalid airport CSV line: " + line);
                }

                String code = parts[0].trim();
                String name = parts[1].trim();
                double x = Double.parseDouble(parts[2].trim());
                double y = Double.parseDouble(parts[3].trim());

                if (!code.matches("[A-Z]{3}")) {
                    throw new Exception("Invalid airport code: " + code);
                }

                if (loadedAirportCodes.contains(code)) {
                    throw new Exception("Duplicate airport code: " + code);
                }

                Airport airport = new Airport(name, code, x, y);

                loadedAirports.add(airport);
                loadedAirportCodes.add(code);

            } else if (section.equals("FLIGHTS")) {
                if (parts.length != 4) {
                    throw new Exception("Invalid flight CSV line: " + line);
                }

                String fromCode = parts[0].trim();
                String toCode = parts[1].trim();
                String departure = parts[2].trim();
                int duration = Integer.parseInt(parts[3].trim());

                Airport from = findAirportByCodeInList(loadedAirports, fromCode);
                Airport to = findAirportByCodeInList(loadedAirports, toCode);

                if (from == null || to == null) {
                    throw new Exception("Flight uses unknown airport.");
                }

                if (fromCode.equals(toCode)) {
                    throw new Exception("Flight cannot have same from and to airport.");
                }

                if (!departure.matches("([01]\\d|2[0-3]):[0-5]\\d")) {
                    throw new Exception("Invalid departure time: " + departure);
                }

                if (duration <= 0) {
                    throw new Exception("Invalid flight duration.");
                }

                Flight flight = new Flight(from, to, departure, duration);
                loadedFlights.add(flight);

            } else {
                throw new Exception("CSV must start with AIRPORTS section.");
            }
        }

        if (loadedAirports.isEmpty()) {
            throw new Exception("CSV does not contain airports.");
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
                    airport.getY()
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

    

    private void saveFile() {
        String format = askSaveFormat();

        if (format == null) {
            statusLabel.setText("Status: Save cancelled.");
            return;
        }

        FileDialog fd = new FileDialog(this, "Save file", FileDialog.SAVE);
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
            ex.printStackTrace();
            statusLabel.setText("Status: Save error.");
        }
    }

    private String askSaveFormat() {
        final String[] selectedFormat = { null };

        Dialog dialog = new Dialog(this, "Choose format", true);
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