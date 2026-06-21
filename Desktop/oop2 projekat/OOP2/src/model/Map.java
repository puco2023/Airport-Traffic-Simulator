package model;
import java.awt.Canvas;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
public class Map extends Canvas implements Runnable{
	private ArrayList<Flight> flights;
	private ArrayList<Airport> airports;
	private Airport selectedAirport = null;
	private boolean blinkOn = true;
	private Thread blinkThread;
	private boolean running = true;
	private static final int AIRPORT_SIZE=10;
	private static final int CLICK_RADIUS = 8;
	
	private int simulationMinutes;
	private boolean simulationRunning = false;
	private boolean simulationPaused = false;
	private Thread simulationThread;
	private ArrayList<SimulationFlight> simFlights = new ArrayList<>();
	public Map(ArrayList<Airport> a, ArrayList<Flight> f){
		flights = f;
		airports = a;
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				handleMouseClick(e.getPoint());
			}
		});
		blinkThread = new Thread(this);
		blinkThread.start();

	}
	private int timeToMinutes(String i)
	{
		String[] parts = i.split(":");
		int hour = Integer.parseInt(parts[0]);
		int minutes = Integer.parseInt(parts[1]);
		return hour*60+minutes;
	}
	private void prepareSimulation()
	{
		simFlights.clear();
		ArrayList<Flight> sortedFlights = new ArrayList<>();
		sortedFlights.sort((f1,f2)->
				{
					int t1 = timeToMinutes(f1.getDepartureTime());
					int t2 = timeToMinutes(f2.getDepartureTime());
					return t1-t2;
				});
		java.util.HashMap<Airport,Integer> nextFreeDeparture = new java.util.HashMap<Airport,Integer>();
		for(Flight f: sortedFlights)
		{
			Airport from = f.getFrom();
			int plannedDeparture = timeToMinutes(f.getDepartureTime());
			int airportFreeAt = nextFreeDeparture.getOrDefault(from,0);
			int realDeparture = Math.max(plannedDeparture, airportFreeAt);
	        nextFreeDeparture.put(from, realDeparture + 10);
	        
	        simFlights.add(new SimulationFlight(f, plannedDeparture, realDeparture));
		}
		
	}
	public void clearSelection() {
	    selectedAirport = null;
	    blinkOn = true;
	    repaint();
	}
	private Airport findAirportAtPoint(Point p)
	{
		int width = getWidth();
		int height = getHeight();
		for(Airport a: airports)
		{
			int x = mapX(a.getX(),width);
			int y = mapY(a.getY(),height);
			int dx = p.x-x;
			int dy = p.y-y;
	        if (dx * dx + dy * dy <= CLICK_RADIUS * CLICK_RADIUS) {
	            return a;
	        }
		}
		return null;
	}
	private void handleMouseClick(Point p)
	{
		Airport clickedAirport = findAirportAtPoint(p);
		if(clickedAirport==null)
			return;
		if(selectedAirport == clickedAirport)
		{
			selectedAirport = null;
		}
		else {
			selectedAirport = clickedAirport;
		}
		blinkOn = true;
		repaint();
	}
	ArrayList<Flight> getFlights()
	{
		return flights;
	}
	ArrayList<Airport> getAirports()
	{
		return airports;
	}
	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		int width = getWidth();
		int height = getHeight();

		g.setColor(new Color(220,235,250));
		g.fillRect(0, 0, width, height);

		g.setColor(Color.BLACK);
		g.drawRect(0,0,width-1,height-1);

		drawFlights(g,width,height);
		drawAirports(g,width,height);
	}
	private void drawAirports(Graphics g,int width,int height)
	{
		int size=AIRPORT_SIZE;
		int half = size/2;
		int gap=3;
		FontMetrics fm = g.getFontMetrics();
		for( Airport a:airports)
		{
			if(!a.isVisible())
				continue;
            int x = mapX(a.getX(), width);
            int y = mapY(a.getY(), height);
           
            if (a == selectedAirport && blinkOn) {
                g.setColor(Color.RED);
            } else {
                g.setColor(Color.GRAY);
            }

            g.fillRect(x - half, y - half, size, size);
            
            String code = a.getCode();
            
            int textWidth = fm.stringWidth(code);
            int textHeight = fm.getHeight();

            int textX = x + half + gap;
            int textY = y - half - gap;
            if(textX+textWidth>width)
            {
            	textX = x-half-gap-textWidth;
            }
            if(textX<0)
            	textX=0;
            if (textY - textHeight < 0) {
                textY = y + half + gap + textHeight;
            }

           
            if (textY > height) {
                textY = y - half - gap;
            }

            g.setColor(Color.BLACK);
            g.drawString(code, textX, textY);
		}
	}
	private void drawFlights(Graphics g,int width,int height)
	{
	}

	private int mapX(double x, int width) {
	    return (int) ((x + 180) / 360 * width);
	}

	private int mapY(double y, int height) {
	    return (int) ((90 - y) / 180 * height);
	}
	@Override
	public void run() {
	    try {
	        while (running) {
	            if (selectedAirport != null) {
	                blinkOn = !blinkOn;
	                repaint();
	            }

	            Thread.sleep(400);
	        }
	    } catch (InterruptedException e) {
	    }
	}
	public void stopBlinking() {
	    running = false;

	    if (blinkThread != null) {
	        blinkThread.interrupt();
	    }
	}
	public void StartSimulation() {
		if(simulationRunning)
			return;
		prepareSimulation();
	    simulationMinutes = 0;
	    simulationRunning = true;
	    simulationPaused = false;
	    
	    simulationThread = new Thread(()->
	    {
	    	while(simulationRunning)
	    	{
	    		if(simulationPaused==false)
	    		{
	    			updateSimulation();
	    			repaint();
	    			
	    		}
	    	}
	    	try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    });
	}
	private void updateSimulation() {
		simulationMinutes+=2;
		for(SimulationFlight sf:simFlights)
		{
			if(sf.isFinished()) continue;
			if(sf.isActive()==false && sf.getRealDepartureMinutes()<=simulationMinutes)
				sf.setActive(true);
			if(sf.isActive() && sf.getArrivalMinutes()>=simulationMinutes)
			{
				sf.setActive(false);
				sf.setFinished(true);
			}
		}
		
	}



}

