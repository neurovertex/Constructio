package eu.neurovertex.constructio;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * @author Neurovertex
 *         Date: 11/10/2014, 17:14
 */
public class SingleInventory extends AbstractInventory {
	private Optional<Resource> current = Optional.empty();
	private int content;
	private final int capacity;

	public SingleInventory(Set<Resource> resources, boolean deposit, boolean withdraw, int capacity) {
		super(resources, deposit, withdraw);
		this.capacity = capacity;
	}

	@Override
	public Set<Resource> getResources() {
		return current.isPresent() ? EnumSet.of(current.get()) : super.getResources();
	}

	@Override
	public int getAmount(Resource resource) {
		if (!current.isPresent() || current.get() != resource)
			return 0;
		return content;
	}

	@Override
	public int getFreeSpace(Resource resource) {
		return (!current.isPresent() || current.get() == resource) ? capacity - content : 0;
	}

	@Override
	public int getCapacity() {
		return capacity;
	}

	@Override
	public boolean deposit(Resource resource, int amount) {
		if ((this.content + amount) > capacity || (current.isPresent() && current.get() != resource) || !super.getResources().contains(resource))
			return false;
		if (this.content == 0 && amount > 0)
			current = Optional.of(resource);
		this.content += amount;
		return true;
	}

	@Override
	public int depositMax(Resource resource, int amount) {
		if (current.isPresent() && current.get() != resource || !super.getResources().contains(resource))
			return 0;
		if (this.content == 0 && amount > 0)
			current = Optional.of(resource);
		int deposited = Math.min(amount, capacity - this.content);
		this.content += deposited;
		return deposited;
	}

	@Override
	public boolean withdraw(Resource resource, int amount) {
		if (content < amount || current.isPresent() && current.get() != resource)
			return false;
		content -= amount;
		if (content == 0)
			current = Optional.empty();
		return true;
	}

	@Override
	public int withdrawMax(Resource resource, int amount) {
		if (current.isPresent() && current.get() != resource)
			return 0;
		int withdrawn = Math.min(amount, content);
		this.content -= withdrawn;
		if (this.content == 0)
			current = Optional.empty();
		return withdrawn;
	}
}
