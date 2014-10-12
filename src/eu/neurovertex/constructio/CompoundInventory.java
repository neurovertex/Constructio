package eu.neurovertex.constructio;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Neurovertex
 *         Date: 11/10/2014, 15:51
 */
public class CompoundInventory extends AbstractInventory {
	private final Collection<Inventory> inventories;

	public CompoundInventory(boolean deposit, boolean withdraw, Collection<Inventory> inventories) {
		super(null, deposit, withdraw);
		if (inventories.size() == 0)
			throw new IllegalArgumentException("Can't make a compound inventory without subinventories");
		this.inventories = Collections.unmodifiableList(new ArrayList<>(inventories));
	}

	@Override
	public Set<Resource> getResources() {
		Set<Resource> resources = EnumSet.noneOf(Resource.class);
		inventories.stream().forEach(i -> resources.addAll(i.getResources()));
		return resources;
	}

	@Override
	public int getAmount(Resource resource) {
		return inventories.stream().mapToInt(i -> i.getAmount(resource)).sum();
	}

	@Override
	public int getFreeSpace(Resource resource) {
		return inventories.stream().mapToInt(i -> i.getFreeSpace(resource)).sum();
	}

	@Override
	public int getCapacity() {
		return inventories.stream().mapToInt(Inventory::getCapacity).sum();
	}

	@Override
	public boolean deposit(Resource resource, int amount) {
		return deposit(inventories.stream().filter(i -> i.getFreeSpace(resource) > 0).collect(Collectors.toList()), resource, amount);
	}

	private boolean deposit(List<Inventory> invs, Resource res, int amount) {
		if (invs.size() == 0)
			return false;
		if (amount == 0)
			return true;
		Inventory inv = invs.get(0);
		int deposited = inv.depositMax(res, amount);
		if (!deposit(invs.subList(1, invs.size()), res, amount - deposited)) {
			inv.withdraw(res, deposited);
			return false;
		} else
			return true;
	}

	@Override
	public int depositMax(Resource resource, int amount) {
		int deposited = 0;
		for (Inventory inv : inventories)
			deposited += inv.depositMax(resource, amount-deposited);
		return deposited;
	}

	@Override
	public boolean withdraw(Resource resource, int amount) {
		return withdraw(inventories.stream().filter(i -> i.getAmount(resource) > 0).collect(Collectors.toList()), resource, amount);
	}

	private boolean withdraw(List<Inventory> invs, Resource res, int amount) {
		if (invs.size() == 0)
			return false;
		if (amount == 0)
			return true;
		Inventory inv = invs.get(0);
		int deposited = inv.withdrawMax(res, amount);
		if (!withdraw(invs.subList(1, invs.size()), res, amount - deposited)) {
			inv.deposit(res, deposited);
			return false;
		} else
			return true;
	}

	@Override
	public int withdrawMax(Resource resource, int amount) {
		int withdrawn = 0;
		for (Inventory inv : inventories) {
			withdrawn += inv.withdrawMax(resource, amount - withdrawn);
		}
		return withdrawn;
	}
}
