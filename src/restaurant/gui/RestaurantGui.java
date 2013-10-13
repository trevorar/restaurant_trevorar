package restaurant.gui;

import restaurant.CustomerAgent;
import restaurant.WaiterAgent;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;
import java.awt.event.*;
/**
 * Main GUI class.
 * Contains the main frame and subsequent panels
 */
public class RestaurantGui extends JFrame implements ActionListener, ChangeListener {
    /* The GUI has two frames, the control frame (in variable gui) 
     * and the animation frame, (in variable animationFrame within gui)
     */
	//JFrame animationFrame = new JFrame("Restaurant Animation");
	//AnimationPanel animationPanel = new AnimationPanel();
	
	
	
    /* restPanel holds 3 panels
     * 1) the staff listing, menu, and lists of current customers all constructed
     *    in RestaurantPanel()
     * 2) the Animation Panel
     * 3) the infoPanel about the clicked Customer (created just below)
     */    
    AnimationPanel animationPanel = new AnimationPanel();
    private RestaurantPanel restPanel = new RestaurantPanel(this);
    
    /* infoPanel holds information about the clicked customer, if there is one*/
    private JPanel infoPanel;
    private JLabel infoLabel; //part of infoPanel
    private JCheckBox stateCB;//part of infoLabel
    
    private JButton addMoney;
    private JButton removeMoney;
    
    //Components of an option panel that allows the user to make customers hungry, pause/resume, and change the animation speed
    private JLabel pauseLabel;
    private JLabel hungryLabel;
    private JButton makeHungry;
    private JPanel optionPanel;
    private JButton pauseButton;
    private JButton resumeButton;
    private JLabel speedLabel;
    private JSlider speedSlider;
    private JButton emptyMarkets;
    private JButton noMoreSteak;
    
    private Object currentPerson;/* Holds the agent that the info is about.
    								Seems like a hack */
    private final int WINDOWX = 1200;
    private final int WINDOWY = 800;
    private final int WINDOW_X_COORD = 50;
    private final int WINDOW_Y_COORD = 50;
    /**
     * Constructor for RestaurantGui class.
     * Sets up all the gui components.
     */
    public RestaurantGui() {
        

        /*animationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        animationFrame.setBounds(100+WINDOWX, 50 , WINDOWX+100, WINDOWY+100);
        animationFrame.setVisible(true);
    	animationFrame.add(animationPanel); */
    	
    	setBounds(WINDOW_X_COORD, WINDOW_Y_COORD, WINDOWX, WINDOWY);

        //setLayout(new BoxLayout((Container) getContentPane(), 
        //		BoxLayout.Y_AXIS));

    	setLayout(new BorderLayout());
    	
        Dimension restDim = new Dimension(WINDOWX/3, (int) (WINDOWY * .6));
        restPanel.setPreferredSize(restDim);
        restPanel.setMinimumSize(restDim);
        restPanel.setMaximumSize(restDim);
        add(restPanel, BorderLayout.WEST);
        
        Dimension animationDim = new Dimension(WINDOWX/2, (int) (WINDOWY * .75));
        animationPanel.setPreferredSize(animationDim);
        add(animationPanel, BorderLayout.CENTER);
        
        // Now, setup the info panel
        Dimension infoDim = new Dimension(WINDOWX/3, (int) (WINDOWY * .2));
        infoPanel = new JPanel();
        infoPanel.setPreferredSize(infoDim);
        infoPanel.setMinimumSize(infoDim);
        infoPanel.setMaximumSize(infoDim);
        infoPanel.setBorder(BorderFactory.createTitledBorder("Information"));
        
        stateCB = new JCheckBox();
        stateCB.setVisible(false);
        stateCB.addActionListener(this);
        
        addMoney = new JButton("+ $10");
        addMoney.setVisible(false);
        addMoney.addActionListener(this);
        
        removeMoney = new JButton("- $10");
        removeMoney.setVisible(false);
        removeMoney.addActionListener(this);

        //infoPanel.setLayout(new GridLayout(1, 2, 30, 0));
        
        infoPanel.setLayout(new FlowLayout());
        
        infoLabel = new JLabel(); 
        infoLabel.setText("<html><pre><i>Click Add to make customers</i></pre></html>");
        infoPanel.add(infoLabel);
        infoPanel.add(stateCB);
        infoPanel.add(addMoney);
        infoPanel.add(removeMoney);
        
        add(infoPanel, BorderLayout.SOUTH);
        
        //The pause/resume buttons;
        pauseButton = new JButton("Pause");
        pauseButton.addActionListener(this);
        resumeButton = new JButton("Resume");
        resumeButton.addActionListener(this);
        resumeButton.setEnabled(false);
        speedSlider = new JSlider(JSlider.HORIZONTAL, 1, 30, animationPanel.getTimerInterval()); // A slider for animation speed
        speedSlider.addChangeListener(this);
        
        pauseLabel = new JLabel("                Pause/Resume: ");
        speedLabel = new JLabel("                Animation Speed: ");
                
        emptyMarkets = new JButton("Empty all markets");
        emptyMarkets.addActionListener(this);
        noMoreSteak = new JButton("No more steak");
        noMoreSteak.addActionListener(this);
        
        optionPanel = new JPanel();
        
        optionPanel.add(pauseLabel);
        optionPanel.add(pauseButton);
        optionPanel.add(resumeButton);
        optionPanel.add(speedLabel);
        optionPanel.add(speedSlider);
        optionPanel.add(emptyMarkets);
        optionPanel.add(noMoreSteak);
        
        add(optionPanel, BorderLayout.NORTH);
    }
    /**
     * updateInfoPanel() takes the given customer (or, for v3, Host) object and
     * changes the information panel to hold that person's info.
     *
     * @param person customer (or waiter) object
     */
    public void updateInfoPanel(Object person) {
        stateCB.setVisible(true);
        currentPerson = person;

        if (person instanceof CustomerAgent) {
            CustomerAgent customer = (CustomerAgent) person;
            stateCB.setText("Hungry?");

            stateCB.setSelected(customer.getGui().isHungry());

            stateCB.setEnabled(!customer.getGui().isHungry());
            
            addMoney.setVisible(true);
            addMoney.setEnabled(true);
           	removeMoney.setVisible(true);
            removeMoney.setEnabled(true);

            infoLabel.setText(
               "<html><pre>     Name: " + customer.getName() + "    Money: " + customer.getMoney() + " </pre></html>");
        }
        if(person instanceof WaiterAgent) {
        	WaiterAgent waiter = (WaiterAgent) person;
        	
        	addMoney.setVisible(false);
        	removeMoney.setVisible(false);
        	
        	if(!waiter.isOnBreak()) {
        		stateCB.setText("Want a break?");

        		stateCB.setSelected(waiter.wantsToTakeBreak());

        		stateCB.setEnabled(!waiter.wantsToTakeBreak());
        	}
        	else {
        		stateCB.setText("Done with break?");
        		
        		stateCB.setSelected(!waiter.isOnBreak());
        		
        		stateCB.setEnabled(waiter.isOnBreak()); 
        	}
        	infoLabel.setText(
        			"<html><pre>     Name: " + waiter.getName() + " </pre></html>");
        }
        infoPanel.validate();
    }
    /**
     * Action listener method that reacts to the checkbox being clicked;
     * If it's the customer's checkbox, it will make him hungry
     * For v3, it will propose a break for the waiter.
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == stateCB) {
            if (currentPerson instanceof CustomerAgent) {
                CustomerAgent c = (CustomerAgent) currentPerson;
                c.getGui().setHungry();
                stateCB.setEnabled(false);
            }
            if(currentPerson instanceof WaiterAgent) {
            	WaiterAgent w = (WaiterAgent) currentPerson;
            	if(!w.wantsToTakeBreak()) {
            		w.msgIWantABreak();
            		stateCB.setEnabled(false);
            	}
            	else if(w.isOnBreak()) {
            		w.msgBreakIsFinished();
            		stateCB.setEnabled(false);
            	}
            	else {
            		stateCB.setEnabled(false);
            	}
            }
        }
        if(e.getSource() == addMoney) {
        	if(currentPerson instanceof CustomerAgent) {
        		CustomerAgent c = (CustomerAgent) currentPerson;
        		c.addTenDollars();
        		updateInfoPanel(c);
        	}
        }
        if(e.getSource() == removeMoney) {
        	if(currentPerson instanceof CustomerAgent) {
        		CustomerAgent c = (CustomerAgent) currentPerson;
        		c.removeTenDollars();
        		updateInfoPanel(c);
        	}
        }
        if(e.getSource() == pauseButton) {
        	restPanel.pause();
        	pauseButton.setEnabled(false);
        	resumeButton.setEnabled(true);
        }
        if(e.getSource() == resumeButton) {
        	restPanel.resume();
        	resumeButton.setEnabled(false);
        	pauseButton.setEnabled(true);
        }
        if(e.getSource() == emptyMarkets) {
        	restPanel.emptyMarkets();
        }
        if(e.getSource() == noMoreSteak) {
        	restPanel.noMoreSteak();
        }
    }
    
    public void stateChanged(ChangeEvent e) {
    	JSlider slider = (JSlider) e.getSource();
    	if(!slider.getValueIsAdjusting()) {
    		animationPanel.setSpeed((int) (31 - slider.getValue())); // Sets the animation panel timer interval between 1 and 30
    	}
    }
    /**
     * Message sent from a customer gui to enable that customer's
     * "I'm hungry" checkbox.
     *
     * @param c reference to the customer
     */
    public void setCustomerEnabled(CustomerAgent c) {
        if (currentPerson instanceof CustomerAgent) {
            CustomerAgent cust = (CustomerAgent) currentPerson;
            if (c.equals(cust)) {
                stateCB.setEnabled(true);
                stateCB.setSelected(false);
            }
        }
    }
    /**
     * Main routine to get gui started
     */
    public static void main(String[] args) {
        RestaurantGui gui = new RestaurantGui();
        gui.setTitle("Restaurant V2");
        gui.setVisible(true);
        gui.setResizable(false);
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
