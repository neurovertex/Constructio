package eu.neurovertex.constructio;

import java.util.Collection;
import java.util.Set;

import static eu.neurovertex.constructio.Constructio.log;

/**
 * @author Neurovertex
 *         Date: 08/10/2014, 17:32
 */
public interface Inventory {
	Set<Resource> getResources();
	boolean canDeposit();
	boolean canWithdraw();
	int getAmount(Resource resource);
	int getFreeSpace(Resource resource);
	int getCapacity();
	boolean deposit(Resource resource, int amount);
	int depositMax(Resource resource, int amount);
	boolean withdraw(Resource resource, int amount);
	int withdrawMax(Resource resource, int amount);

	default boolean accepts(Resource resource) {
		return getResources().contains(resource);
	}
	default int getAmount() {
		return getAmount(Resource.values());
	}
	default int getAmount(Resource... resources) {
		int total = 0;
		for (Resource r : resources)
			total += getAmount(r);
		return total;
	}

	public static int transfer(Inventory src, Inventory dst, Resource resource) {
		int max = dst.getFreeSpace(resource);
		int withdrawn = src.withdrawMax(resource, max);
		return (max > 0) ? dst.depositMax(resource, withdrawn) : 0;
	}

	public static int transfer(Inventory src, Inventory dst, Resource resource, int max) {
		log.info("Max : " + (max = Math.min(max, dst.getFreeSpace(resource))));
		return (max > 0) ? dst.depositMax(resource, src.withdrawMax(resource, max)) : 0;
	}

	public static int transfer(Inventory src, Inventory dst, Collection<Resource> resources) {
		int total = 0;
		for (Resource r : resources)
			transfer(src, dst, r);
		return total;
	}

	public static int transfer(Inventory src, Inventory dst) {
		return transfer(src, dst, Resource.valueSet());
	}
}
