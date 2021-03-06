package restaurant;

import agent.Agent;
import restaurant.interfaces.Customer;
import restaurant.interfaces.Market;
import restaurant.interfaces.Waiter;
import restaurant.test.mock.EventLog;

import java.util.*;

public class CashierAgent extends Agent {
	
	public EventLog log; //Log for keeping track of events while unit testing cashier
	
	public List<MyCheck> checks = Collections.synchronizedList(new ArrayList<MyCheck>());
	
	public List<MyCustomer> customersWhoOweMoney = Collections.synchronizedList(new ArrayList<MyCustomer>());
	
	public List<MarketBill> marketBills = Collections.synchronizedList(new ArrayList<MarketBill>());
	
	private Menu menu = new Menu();

	public enum checkState { requested, givenToWaiter, fullyPaid, partiallyPaid, finished };

	private String name;
	
	public double money = 1000.0;
	
	Timer timer = new Timer();

	public CashierAgent(String name) {
		super();
		
		this.name = name;
	}

	public String getName() {
		return name;
	}

	// Messages
	public void msgProduceCheck(Waiter w, Customer c, String choice) {
		Check check = new Check(this, c, choice);
		check.amount = menu.getPrice(choice);
		
		synchronized(customersWhoOweMoney) {
			for(MyCustomer mc : customersWhoOweMoney) {
				if(mc.c == c) {
					print("Well, look who's back! This customer will have to repay their previous bill of " + mc.amountOwed + " as well.");
					check.amount += mc.amountOwed;
				}
			}
		}
		checks.add(new MyCheck(w, check));
		
		stateChanged();
	}
	
	public void msgPayBill(Check check, double money) {
		synchronized(checks) {
			for(MyCheck c : checks) {
				if(c.c == check) {
					if(money >= c.c.amount) {
						c.state = checkState.fullyPaid;
						c.amountPaid = money;
					}
					else if(money < c.c.amount) {
						this.money += money;
						c.c.amount -= money;
						c.state = checkState.partiallyPaid;
						c.amountPaid = money;
					}
				}
			}
		}
		stateChanged();
	}
	
	public void msgYouOwe(Market m, double amount) {
		marketBills.add(new MarketBill(m, amount));
		stateChanged();
	}

	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	public boolean pickAndExecuteAnAction() {
		synchronized(checks) {
			for(MyCheck c : checks) {
				if(c.state == checkState.fullyPaid) {
					giveChange(c);
					return true;
				}
			}
		}
		
		synchronized(checks) {
			for(MyCheck c : checks) {
				if(c.state == checkState.partiallyPaid) {
					addCustomerToOweList(c);
					return true;
				}
			}
		}
		
		synchronized(checks) {
			for(MyCheck c : checks) {
				if(c.state == checkState.requested) {
					giveCheckToWaiter(c);
					return true;
				}
			}
		}

		if(!marketBills.isEmpty()) {
			payBill(marketBills.get(0));
			return true;
		}

		return false;
	}

	// Actions

	private void giveCheckToWaiter(MyCheck c) {
		print("The check for " + c.c.cust + " is ready!");
		c.w.msgHereIsCheck(c.c);
		c.state = checkState.givenToWaiter;
	}
	
	private void giveChange(MyCheck c) {
		this.money += c.c.amount;
		double change = c.amountPaid - c.c.amount;
		print("Here is your change of $" + change);
		c.c.cust.msgHereIsChange(change);
		c.state = checkState.finished;
	}
	
	private void addCustomerToOweList(MyCheck c) {
		print("You still owe $" + c.c.amount + "! You'll have to pay it back next time!");
		synchronized(customersWhoOweMoney) {
			for(MyCustomer mc : customersWhoOweMoney) { //If customer is already on the "owe money" list, add the money to the amount they owe
				if(mc.c == c.c.cust) {
					c.state = checkState.finished;
					return;
				}
			}
		}
		
		customersWhoOweMoney.add(new MyCustomer(c.c.cust, c.c.amount));
		c.state = checkState.finished;
	}
	
	private void payBill(MarketBill mb) {
		if(money > mb.amountOwed) {
			print("Here is my payment of $" + mb.amountOwed + " for the recent shipment!");
			mb.m.msgHereIsPayment(this, mb.amountOwed);
			money -= mb.amountOwed;
		} else {
			print("Thanks for the food, but I can't pay for it!");
			mb.m.msgCannotPayBill(this, mb.amountOwed);
		}
		
		marketBills.remove(mb);
	}

	// The animation DoXYZ() routines
	

	//utilities
	
	public class MyCheck {
		Waiter w;
		public Check c;
		public checkState state = checkState.requested;
		public double amountPaid;
		
		MyCheck(Waiter w, Check c) {
			this.w = w;
			this.c = c;
		}
	}
	
	public class MyCustomer {
		public Customer c;
		double amountOwed;
	
		MyCustomer(Customer c, double amount) {
			this.c = c;
			this.amountOwed = amount;
		}
	}
	
	public class MarketBill {
		public Market m;
		public double amountOwed;
		
		MarketBill(Market m, double amount) {
			this.m = m;
			this.amountOwed = amount;
		}
	}
}