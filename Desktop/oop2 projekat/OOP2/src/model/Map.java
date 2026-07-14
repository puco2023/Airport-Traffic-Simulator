package model;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import Exceptions.SimulationException;
public class Map extends Canvas implements Runnable {
    private ArrayList<Flight> flights;
    private ArrayList<Airport> airports;
    private Airport selectedAirport = null;
    private boolean blinkOn = true;
    private Thread blinkThread;
    private boolean running = true;
    public static final int AIRPORT_SIZE = 10;
    private static final int CLICK_RADIUS = 8;
    private Simulation simulation = new Simulation();
    public boolean isBlinking()
    {
    	return selectedAirport!=null;
    }
    public Map(ArrayList<Airport> a, ArrayList<Flight> f) {
        flights = f;
        airports = a;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e.getPoint());
            }
        });
        blinkThread = new Thread(this);
        blinkThread.start();
    }
    public int getSimulationMinutes() {
        return simulation.getSimulationMinutes();
    }
    public void clearSelection() {
        selectedAirport = null;
        blinkOn = true;
        repaint();
    }

    private Airport findAirportAtPoint(Point p) {
        int width = getWidth();
        int height = getHeight();
        for (Airport a : airports) {
            int x = mapX(a.getX(), width);
            int y = mapY(a.getY(), height);
            int dx = p.x - x;
            int dy = p.y - y;
            if (dx * dx + dy * dy <= CLICK_RADIUS * CLICK_RADIUS)
                return a;
        }
        return null;
    }

    private void handleMouseClick(Point p) {
        Airport clicked = findAirportAtPoint(p);
        if (clicked == null) return;
        selectedAirport = (selectedAirport == clicked) ? null : clicked;
        blinkOn = true;
        repaint();
    }

    ArrayList<Flight> getFlights() {
        return flights;
    }

    ArrayList<Airport> getAirports() {
        return airports;
    }


    @Override
    public void paint(Graphics g) {
        super.paint(g);
        int width = getWidth();
        int height = getHeight();

        g.setColor(new Color(220, 235, 250));
        g.fillRect(0, 0, width, height);
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, width - 1, height - 1);

        drawFlights(g, width, height);
        drawAirports(g, width, height);

    }

    private void drawAirports(Graphics g, int width, int height) {
        int size = AIRPORT_SIZE;
        int half = size / 2;
        int gap = 3;
        FontMetrics fm = g.getFontMetrics();
        for (Airport a : airports) {
            if (!a.isVisible()) continue;
            int x = mapX(a.getX(), width);
            int y = mapY(a.getY(), height);

            g.setColor(a == selectedAirport && blinkOn ? Color.RED : Color.GRAY);
            g.fillRect(x - half, y - half, size, size);

            String code = a.getCode();
            int textWidth = fm.stringWidth(code);
            int textHeight = fm.getHeight();
            int textX = x + half + gap;
            int textY = y - half - gap;

            if (textX + textWidth > width) textX = x - half - gap - textWidth;
            if (textX < 0) textX = 0;
            if (textY - textHeight < 0) textY = y + half + gap + textHeight;
            if (textY > height) textY = y - half - gap;

            g.setColor(Color.BLACK);
            g.drawString(code, textX, textY);
        }
    }

    private void drawFlights(Graphics g, int width, int height) {
        g.setColor(Color.LIGHT_GRAY);
        for (Flight f : flights) {
            Airport from = f.getFrom();
            Airport to = f.getTo();
            if (!from.isVisible() || !to.isVisible()) continue;
            g.drawLine(mapX(from.getX(), width), mapY(from.getY(), height),
                       mapX(to.getX(), width), mapY(to.getY(), height));
        }

        g.setColor(Color.BLUE);
        for (SimulationFlight sf : simulation.getSimFlights()) {
            Flight f = sf.getFlight();
            Airport from = f.getFrom();
            Airport to = f.getTo();
            if (!from.isVisible() || !to.isVisible()) continue;

            int start = sf.getRealDepartureMinutes();
            int end = sf.getArrivalMinutes();
            double progress = Math.min(1, Math.max(0,
                (double)(simulation.getSimulationMinutes() - start) / (end - start)));
            if(progress==0 || progress==1) continue;
            int x1 = mapX(from.getX(), width), y1 = mapY(from.getY(), height);
            int x2 = mapX(to.getX(), width), y2 = mapY(to.getY(), height);
            int planeX = (int)(x1 + progress * (x2 - x1));
            int planeY = (int)(y1 + progress * (y2 - y1));
            g.fillOval(planeX - 5, planeY - 5, 10, 10);
        }
    }
    public void resetSimulation() {
    	simulation.reset();
    	repaint();
    }
    private int mapX(double x, int width) {
        return (int)((x + 180) / 360 * width);
    }

    private int mapY(double y, int height) {
        return (int)((90 - y) / 180 * height);
    }

    @Override
    public void run() {
        try {
            while (running) {
                if (selectedAirport != null) {
                    blinkOn = !blinkOn;
                    repaint();
                }
                Thread.sleep(200);
            }
        } catch (InterruptedException e) {
        }
    }

    public void stopBlinking() {
        running = false;
        if (blinkThread != null) blinkThread.interrupt();
    }

    public void startSimulation()throws SimulationException {
        simulation.start(flights, this::repaint);
    }

    public boolean isSimulationRunning() {
        return simulation.isSimulationRunning();
    }

    public boolean isSimulationPaused() {
        return simulation.isSimulationPaused();
    }

    public void togglePause() {
        simulation.togglePause();
    }
}
