package restaurant;

import restaurant.gui.CustomerGui;
import restaurant.gui.RestaurantGui;
import agent.Agent;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Restaurant customer agent.
 */
public class CustomerAgent extends Agent {
	private String name;
	private int hungerLevel = 5;        // determines length of meal
	private int tableNumber; // Variable to hold number of table to eat at
	Timer timer = new Timer();
	private CustomerGui customerGui;

	// agent correspondents
	private WaiterAgent waiter;
	private HostAgent host;

	//    private boolean isHungry = false; //hack for gui
	public enum AgentState
	{doingNothing, waitingInRestaurant, beingSeated, seated, ordering, eating, doneEating, leaving};
	private AgentState state = AgentState.doingNothing;//The start state

	public enum AgentEvent 
	{none, gotHungry, followWaiter, seated, ordered, doneEating, doneLeaving};
	AgentEvent event = AgentEvent.none;

	/**
	 * Constructor for CustomerAgent class
	 *
	 * @param name name of the customer
	 * @param gui  reference to the customergui so the customer can send it messages
	 */
	public CustomerAgent(String name){
		super();
		this.name = name;
	}

	/**
	 * hack to establish connection to Host agent.
	 */
	public void setWaiter(WaiterAgent waiter) {
		this.waiter = waiter;
	}
	
	public void setHost(HostAgent host) {
		this.host = host;
	}

	public String getCustomerName() {
		return name;
	}
	// Messages

	public void gotHungry() {//from animation
		print("I'm hungry");
		event = AgentEvent.gotHungry;
		stateChanged();
	}

	public void msgSitAtTable(int tableNumber) {
		this.tableNumber = tableNumber;
		print("Received msgSitAtTable");
		event = AgentEvent.followWaiter;
		stateChanged();
	}

	public void msgAnimationFinishedGoToSeat() {
		//from animation
		event = AgentEvent.seated;
		stateChanged();
	}
	public void msgAnimationFinishedLeaveRestaurant() {
		//from animation
		event = AgentEvent.doneLeaving;
		stateChanged();
	}

	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		//	CustomerAgent is a finite state machine

		if (state == AgentState.doingNothing && event == AgentEvent.gotHungry ){
			state = AgentState.waitingInRestaurant;
			goToRestaurant();
			return true;
		}
		if (state == AgentState.waitingInRestaurant && event == AgentEvent.followWaiter ){
			state = AgentState.beingSeated;
			SitDown(tableNumber);
			return true;
		}
		if (state == AgentState.beingSeated && event == AgentEvent.seated){
			state = AgentState.eating;
			EatFood();
			return true;
		}

		if (state == AgentState.eating && event == AgentEvent.doneEating){
			state = AgentState.leaving;
			leaveTable();
			return true;
		}
		if (state == AgentState.leaving && event == AgentEvent.doneLeaving){
			state = AgentState.doingNothing;
			//no action
			return true;
		}
		return false;
	}

	// Actions

	private void goToRestaurant() {
		Do("Going to restaurant");
		host.msgImHungry(this);//send our instance, so he can respond to us
	}

	private void SitDown(int tableNumber) {
		Do("Being seated. Going to table");
		customerGui.DoGoToSeat(tableNumber);
	}

	private void EatFood() {
		Do("Eating Food");
		//This next complicated line creates and starts a timer thread.
		//We schedule a deadline of getHungerLevel()*1000 milliseconds.
		//When that time elapses, it will call back to the run routine
		//located in the anonymous class created right there inline:
		//TimerTask is an interface that we implement right there inline.
		//Since Java does not all us to pass functions, only objects.
		//So, we use Java syntactic mechanism to create an
		//anonymous inner class that has the public method run() in it.
		timer.schedule(new TimerTask() {
			Object cookie = 1;
			public void run() {
				print("Done eating, cookie=" + cookie);
				event = AgentEvent.doneEating;
				//isHungry = false;
				stateChanged();
			}
		},
		50000);//getHungerLevel() * 1000);//how long to wait before running task
	}

	private void leaveTable() {
		Do("Leaving.");
		waiter.msgImDoneEating(this);
		customerGui.DoExitRestaurant();
	}

	// Accessors, etc.

	public String getName() {
		return name;
	}
	
	public int getHungerLevel() {
		return hungerLevel;
	}

	public void setHungerLevel(int hungerLevel) {
		this.hungerLevel = hungerLevel;
		//could be a state change. Maybe you don't
		//need to eat until hunger lever is > 5?
	}

	public String toString() {
		return "customer " + getName();
	}

	public void setGui(CustomerGui g) {
		customerGui = g;
	}

	public CustomerGui getGui() {
		return customerGui;
	}
}

