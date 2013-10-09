package restaurant;

import agent.Agent;
import restaurant.gui.HostGui;

import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * Restaurant Host Agent
 */
//We only have 2 types of agents in this prototype. A customer and an agent that
//does all the rest. Rather than calling the other agent a waiter, we called him
//the HostAgent. A Host is the manager of a restaurant who sees that all
//is proceeded as he wishes.
public class CashierAgent extends Agent {
	
	public List<MyCheck> checks = new ArrayList<MyCheck>();
	
	private List<MyCustomer> customersWhoOweMoney = new ArrayList<MyCustomer>();
	
	private Menu menu = new Menu();

	private enum checkState { requested, givenToWaiter, beingPaid, fullyPaid, partiallyPaid, finished };

	private String name;
	
	Timer timer = new Timer();
	
	//public MarketGui marketGui = null;

	public CashierAgent(String name) {
		super();
		
		this.name = name;
	}

	public String getName() {
		return name;
	}

	// Messages
	public void msgProduceCheck(WaiterAgent w, CustomerAgent c, String choice) {
		Check check = new Check(this, c, choice);
		check.amount = menu.getPrice(choice);
		
		for(MyCustomer mc : customersWhoOweMoney) {
			if(mc.c == c) {
				check.amount += mc.amountOwed;
			}
		}
		
		checks.add(new MyCheck(w, check));
		stateChanged();
	}
	
	public void msgPayBill(Check check, double money) {
		for(MyCheck c : checks) {
			if(c.c == check) {
				if(money > c.c.amount) {
					c.state = checkState.fullyPaid;
				}
				else if(money < c.c.amount) {
					c.c.amount -= money;
					c.state = checkState.partiallyPaid;
				}
			}
		}
		stateChanged();
	}

	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		for(MyCheck c : checks) {
			if(c.state == checkState.fullyPaid) {
				giveChange(c);
			}
		}
		for(MyCheck c : checks) {
			if(c.state == checkState.partiallyPaid) {
				addCustomerToOweList(c);
			}
		}
		for(MyCheck c : checks) {
			if(c.state == checkState.requested) {
				giveCheckToWaiter(c);
			}
		}
		
		for(MyCheck c : checks) {
			
		}

		return false;
		//we have tried all our rules and found
		//nothing to do. So return false to main loop of abstract agent
		//and wait.
	}

	// Actions

	private void giveCheckToWaiter(MyCheck c) {
		c.w.msgHereIsCheck(c.c);
		c.state = checkState.givenToWaiter;
	}
	
	private void giveChange(MyCheck c) {
		print("Here is your change! Please come again!");
		c.state = checkState.finished;
	}
	
	private void addCustomerToOweList(MyCheck c) {
		customersWhoOweMoney.add(new MyCustomer(c.c.cust, c.c.amount));
		c.state = checkState.finished;
	}

	// The animation DoXYZ() routines
	

	//utilities

	//Stuff for cook GUi
	/*public void setGui(HostGui gui) {
		hostGui = gui;
	}

	public HostGui getGui() {
		return hostGui;
	}*/
	
	private class MyCheck {
		WaiterAgent w;
		Check c;
		checkState state = checkState.requested;
		
		MyCheck(WaiterAgent w, Check c) {
			this.w = w;
			this.c = c;
		}
	}
	
	private class MyCustomer {
		CustomerAgent c;
		double amountOwed;
	
		MyCustomer(CustomerAgent c, double amount) {
			this.c = c;
			this.amountOwed = amount;
		}
	}
}

