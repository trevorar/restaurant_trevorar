package restaurant.gui;


import restaurant.CustomerAgent;
import restaurant.HostAgent;
import restaurant.CookAgent;

import java.awt.*;

public class CookGui implements Gui {
	
	private static final int XPOS = -20, YPOS = -20;
	private static final int XTABLE = 200, YTABLE = 250;

    private CookAgent agent = null;

    private int xPos = XPOS, yPos = YPOS;//default waiter position
    private int xDestination = XPOS, yDestination = YPOS;//default start position

    public static final int xTable = XTABLE;
    public static final int yTable = YTABLE;

    public CookGui(CookAgent agent) {
        this.agent = agent;
        
        tableLocations.put(new Integer(1), new Dimension(200, 200));
        tableLocations.put(new Integer(2), new Dimension(450, 200));
        tableLocations.put(new Integer(3), new Dimension(200, 400));
        tableLocations.put(new Integer(4), new Dimension(450, 400));
    }

    public void updatePosition() {
        if (xPos < xDestination)
            xPos++;
        else if (xPos > xDestination)
            xPos--;

        if (yPos < yDestination)
            yPos++;
        else if (yPos > yDestination)
            yPos--;
    }

    public void draw(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.fillRect(xPos, yPos, 20, 20);
    }

    public boolean isPresent() {
        return true;
    }
}
