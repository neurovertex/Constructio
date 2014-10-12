package eu.neurovertex.constructio;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Neurovertex
 *         Date: 12/10/2014, 10:32
 */
public class InventoryRegistry {
	private Set<Container> inventories = new HashSet<>();

	public InventoryRegistry() {}

	public void register(Container inv) {
		if (inv.hasInventory())
			inventories.add(inv);
	}

	public Collection<Container> getInventories() {
		return Collections.unmodifiableCollection(inventories);
	}

	public Stream<Container> findWithdrawable(Resource res) {
		return inventories.stream().filter(i -> i.getInventory().getAmount(res) > 0);
	}

	public Stream<Container> findDepositable(Resource res) {
		return inventories.stream().filter(i -> i.getInventory().getFreeSpace(res) > 0);
	}
}
