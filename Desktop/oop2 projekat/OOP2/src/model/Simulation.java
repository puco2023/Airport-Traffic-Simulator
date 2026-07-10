package model;

import java.util.ArrayList;
import java.util.HashMap;

public class Simulation {
    private ArrayList<SimulationFlight> simFlights = new ArrayList<>();
    private volatile int simulationMinutes = 0;
    private boolean simulationRunning = false;
    private boolean simulationPaused = false;
    private Thread simulationThread;
    public void togglePause() {

        if (!simulationRunning) {
            return;
        }

        simulationPaused = !simulationPaused;
    }
    public boolean isSimulationPaused() {
        return simulationPaused;
    }
    public boolean isSimulationRunning() {
    	return simulationRunning;
    }
    public ArrayList<SimulationFlight> getSimFlights() {
        return simFlights;
    }

    public int getSimulationMinutes() {
        return simulationMinutes;
    }

    private int timeToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    private void prepare(ArrayList<Flight> flights) {
        simFlights.clear();
        ArrayList<Flight> sorted = new ArrayList<>(flights);
        sorted.sort((f1, f2) -> timeToMinutes(f1.getDepartureTime()) - timeToMinutes(f2.getDepartureTime()));
        HashMap<Airport, Integer> nextFreeDeparture = new HashMap<>();
        for (Flight f : sorted) {
            Airport from = f.getFrom();
            int planned = timeToMinutes(f.getDepartureTime());
            int freeAt = nextFreeDeparture.getOrDefault(from, 0);
            int real = Math.max(planned, freeAt);
            nextFreeDeparture.put(from, real + 10);
            simFlights.add(new SimulationFlight(f, planned, real));
        }
    }

    public void start(ArrayList<Flight> flights, Runnable onTick) {
        if (simulationRunning) return;
        prepare(flights);
        simulationMinutes = 0;
        simulationRunning = true;
        simulationPaused = false;
        simulationThread = new Thread(() -> {
            while (simulationRunning) {
                if (!simulationPaused) {
                    update();
                    onTick.run();
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                }
            }
        });
        simulationThread.start();
    }

    private void update() {
        simulationMinutes += 2;
        for (SimulationFlight sf : simFlights) {
            if (sf.isFinished()) continue;
            if (!sf.isActive() && sf.getRealDepartureMinutes() <= simulationMinutes)
                sf.setActive(true);
            if (sf.isActive() && sf.getArrivalMinutes() >= simulationMinutes) {
                sf.setActive(false);
                sf.setFinished(true);
            }
        }
    }
    public void reset() {

        simulationRunning = false;
        simulationPaused = false;

        simulationMinutes = 0;

        if (simulationThread != null) {
            simulationThread.interrupt();
            simulationThread = null;
        }

        simFlights.clear();
    }
}
