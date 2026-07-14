package gui;

import model.Airport;
import model.Flight;
import model.Map;

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Frame;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.EventQueue;
public class Window extends Frame {

    private TextField airportNameField = new TextField(20);
    private TextField airportCodeField = new TextField(4);
    private TextField airportXField = new TextField(5);
    private TextField airportYField = new TextField(5);

    private Choice fromAirportChoice = new Choice();
    private Choice toAirportChoice = new Choice();
    private TextField departureTimeField = new TextField(6);
    private TextField flightDurationField = new TextField(5);
    private Thread simulationClockThread;
    private volatile boolean simulationClockThreadRunning = true;
    private TextField simulationTimeField =
            new TextField(
                "Day: 1   Time: 00:00",
                22
            );
    
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
    
    private final AirportFlightService dataService;
    private final FileStorageService fileService;
    private final InactivityMonitor inactivityMonitor;

    public Window() {
        super("Air Traffic Manager - Phase C");
        
        simulationTimeField.setEditable(false);
        simulationTimeField.setFocusable(false);
        
        setSize(1000, 600);
        setLayout(new BorderLayout());

        dataService = new AirportFlightService(
                airportNameField, airportCodeField, airportXField, airportYField,
                fromAirportChoice, toAirportChoice, departureTimeField, flightDurationField,
                airportTableModel, flightTableModel,
                airportCodes, airports, flights,
                map, statusLabel
        );

        fileService = new FileStorageService(
                this,
                airports, airportCodes, flights,
                fromAirportChoice, toAirportChoice,
                airportTableModel, flightTableModel,
                map, statusLabel
        );

        inactivityMonitor = new InactivityMonitor(this, this::closeApplication,map::isBlinking,map::isSimulationPaused,map::isSimulationRunning);

        PanelFactory panelFactory = new PanelFactory(
                airportNameField, airportCodeField, airportXField, airportYField,
                fromAirportChoice, toAirportChoice, departureTimeField, flightDurationField,
                airportTable, flightTable,
                map, statusLabel, this,
                dataService, fileService
        );

        add(panelFactory.createTopPanel(), BorderLayout.NORTH);
        add(panelFactory.createMainPanel(), BorderLayout.CENTER);
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
                closeApplication();
            }
        });

        inactivityMonitor.start();
        startSimulationClockThread();
        setVisible(true);
    }

    private void closeApplication() {
        inactivityMonitor.stop();
        map.stopBlinking();
        dispose();
        System.exit(0);
    }
    private void updateSimulationTime() {
    	
        int totalMinutes = map.getSimulationMinutes();
        int day = totalMinutes / 1440 + 1;
        int minutesInCurrentDay = totalMinutes % 1440;
        int hours = minutesInCurrentDay / 60;
        int minutes = minutesInCurrentDay % 60;
        simulationTimeField.setText(
            String.format(
                "Day: %d   Time: %02d:%02d",
                day,
                hours,
                minutes
            )
        );
    }
    private void startSimulationClockThread() {
        simulationClockThread = new Thread(() -> {
            while (simulationClockThreadRunning) {
                EventQueue.invokeLater(() -> {
                	
                    updateSimulationTime();
                });
                try {
                    Thread.sleep(200);
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        simulationClockThread.setName(
            "Simulation clock thread"
        );

        simulationClockThread.start();
    }
    public TextField getSimulationTimeField() {
        return simulationTimeField;
    }
}
