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
				print("Well, look who's back! This customer will have to repay their previous bill of " + mc.amountOwed + " as well.");
				check.amount += mc.amountOwed;
			}
		}
		checks.add(new MyCheck(w, check));
		
		stateChanged();
	}
	
	public void msgPayBill(Check check, double money) {
		for(MyCheck c : checks) {
			if(c.c == check) {
				if(money == c.c.amount) {
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
				thankCustomer(c);
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
	
	private void thankCustomer(MyCheck c) {
		print("Thank you! Please come again!");
		c.state = checkState.finished;
	}
	
	private void addCustomerToOweList(MyCheck c) {
		print("Your bill is for $" + c.c.amount + "! You'll have to pay it back next time!");
		for(MyCustomer mc : customersWhoOweMoney) { //If customer is already on the "owe money" list, add the money to the amount they owe
			if(mc.c == c.c.cust) {
				c.state = checkState.finished;
				return;
			}
		}
		
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

