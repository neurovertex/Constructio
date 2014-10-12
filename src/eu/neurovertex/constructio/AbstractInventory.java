package eu.neurovertex.constructio;

import java.util.Collections;
import java.util.Set;

/**
 * @author Neurovertex
 *         Date: 11/10/2014, 15:52
 */
public abstract class AbstractInventory implements Inventory {
	private final Set<Resource> resources;
	private final boolean deposit, withdraw;

	public AbstractInventory(Set<Resource> resources, boolean deposit, boolean withdraw) {
		this.resources = resources;
		this.deposit = deposit;
		this.withdraw = withdraw;
	}

	@Override
	public Set<Resource> getResources() {
		return Collections.unmodifiableSet(resources);
	}

	@Override
	public boolean canDeposit() {
		return deposit;
	}

	@Override
	public boolean canWithdraw() {
		return withdraw;
	}
}
