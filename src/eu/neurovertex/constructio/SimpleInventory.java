package eu.neurovertex.constructio;

import java.util.*;

/**
 * @author Neurovertex
 *         Date: 08/10/2014, 17:51
 */
public class SimpleInventory extends AbstractInventory {
	private final int capacity;
	private final Map<Resource, Integer> amounts = new EnumMap<>(Resource.class);

	public SimpleInventory(boolean deposit, boolean withdraw, int capacity, Collection<Resource> resources) {
		super(EnumSet.copyOf(resources), deposit, withdraw);
		this.capacity = capacity;
		for (Resource r : Resource.values())
			amounts.put(r, 0);
	}

	public SimpleInventory(boolean deposit, boolean withdraw, int capacity, Resource... resources) {
		this(deposit, withdraw, capacity, Arrays.asList(resources));
	}

	@Override
	public synchronized int getAmount(Resource res) {
		return amounts.get(res);
	}

	@Override
	public synchronized int getFreeSpace(Resource res) {
		if (!getResources().contains(res) || !canDeposit())
			return 0;
		return capacity - amounts.get(res);
	}

	@Override
	public synchronized int getCapacity() {
		return capacity;
	}

	@Override
	public synchronized boolean deposit(Resource res, int amount) {
		if (res == null || amount < 0)
			throw new IllegalArgumentException();
		int newAmount = amounts.get(res)+amount;
		if (getResources().contains(res) && newAmount <= capacity && canDeposit()) {
			amounts.put(res, newAmount);
			return true;
		}
		return false;
	}

	/**
	 * Deposit as much of the given resource as possible
	 * @param res       Resource to deposit
	 * @param amount    Max amount to deposit
	 * @return The amount effectively deposited into the inventory, i.e max(amount, freeSpace(res))
	 */
	@Override
	public int depositMax(Resource res, int amount) {
		if (res == null || amount < 0)
			throw new IllegalArgumentException();
		int current = amounts.get(res), maxDeposit = (canDeposit()) ? Math.max(capacity - current, amount) : 0;
		if (maxDeposit > 0)
			amounts.put(res, current+maxDeposit);
		return maxDeposit;
	}

	@Override
	public boolean withdraw(Resource res, int amount) {
		if (res == null || amount < 0)
			throw new IllegalArgumentException();
		int newAmount = amounts.get(res)-amount;
		if (getResources().contains(res) && newAmount >= 0 && canWithdraw()) {
			amounts.put(res, newAmount);
			return true;
		}
		return false;
	}


	/**
	 * Withdraw as much of the given resource as possible
	 * @param res       Resource to withdraw
	 * @param amount    Max amount to withdraw
	 * @return The amount effectively withdrawn from the inventory, i.e max(amount, freeSpace(res))
	 */
	@Override
	public int withdrawMax(Resource res, int amount) {
		if (res == null || amount < 0)
			throw new IllegalArgumentException();
		int current = amounts.get(res), maxDeposit = (canDeposit()) ? Math.max(current, amount) : 0;
		if (maxDeposit > 0)
			amounts.put(res, current-maxDeposit);
		return maxDeposit;
	}
}
