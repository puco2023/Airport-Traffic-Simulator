package gui;

import Exceptions.ValidationException;
import model.Map;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import javax.swing.JScrollPane;
import javax.swing.JTable;

class PanelFactory {

    private final TextField airportNameField;
    private final TextField airportCodeField;
    private final TextField airportXField;
    private final TextField airportYField;

    private final Choice fromAirportChoice;
    private final Choice toAirportChoice;
    private final TextField departureTimeField;
    private final TextField flightDurationField;

    private final JTable airportTable;
    private final JTable flightTable;

    private final Map map;
    private final Label statusLabel;
    private final Frame owner;

    private final AirportFlightService dataService;
    private final FileStorageService fileService;

    PanelFactory(
            TextField airportNameField,
            TextField airportCodeField,
            TextField airportXField,
            TextField airportYField,
            Choice fromAirportChoice,
            Choice toAirportChoice,
            TextField departureTimeField,
            TextField flightDurationField,
            JTable airportTable,
            JTable flightTable,
            Map map,
            Label statusLabel,
            Frame owner,
            AirportFlightService dataService,
            FileStorageService fileService) {

        this.airportNameField = airportNameField;
        this.airportCodeField = airportCodeField;
        this.airportXField = airportXField;
        this.airportYField = airportYField;
        this.fromAirportChoice = fromAirportChoice;
        this.toAirportChoice = toAirportChoice;
        this.departureTimeField = departureTimeField;
        this.flightDurationField = flightDurationField;
        this.airportTable = airportTable;
        this.flightTable = flightTable;
        this.map = map;
        this.statusLabel = statusLabel;
        this.owner = owner;
        this.dataService = dataService;
        this.fileService = fileService;
    }

    Panel createTopPanel() {
        Panel panel = new Panel();
        panel.setLayout(
            new FlowLayout()
        );
        Button load = new Button("Load");

        Button save = new Button("Save");

        Button start = new Button("Start");

        Button pause = new Button("Pause");

        Button reset = new Button("Reset");
        load.addActionListener(
            e -> fileService.loadFile()
        );

        save.addActionListener(
            e -> fileService.saveFile()
        );

        start.addActionListener(
            e -> map.startSimulation()
        );
        reset.addActionListener(e -> {

            map.resetSimulation();

            pause.setLabel("Pause");

            statusLabel.setText(
                "Status: Simulation reset."
            );
        });

        pause.addActionListener(e -> {

            if (!map.isSimulationRunning()) {

                DialogUtil.showError(
                    owner,
                    "Simulation is not running."
                );

                return;
            }


            map.togglePause();


            if (map.isSimulationPaused()) {

                pause.setLabel(
                    "Resume"
                );

            }
            else {

                pause.setLabel(
                    "Pause"
                );
            }
        });
        panel.add(load);
        panel.add(save);
        panel.add(start);
        panel.add(pause);
        panel.add(reset);
        return panel;
    }

    Panel createMainPanel() {
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
        airportButton.addActionListener(e -> {
            try {
                dataService.addAirport();
            } catch (ValidationException ve) {
                DialogUtil.showError(owner, ve.getMessage());
            }
        });
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
        addFlightButton.addActionListener(e-> {
        		try
        		{
        			dataService.addFlight();
        		}
        		catch(ValidationException ve)
        		{
        			DialogUtil.showError(owner, ve.getMessage());
        		}
        	}
        );
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
}
