package eu.neurovertex.constructio.entities;

import eu.neurovertex.constructio.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

/**
 * @author Neurovertex
 *         Date: 11/10/2014, 15:49
 */
public class Stockpile extends Building implements Container {
	private final CompoundInventory inventory;
	private final SingleInventory[][] inventoryGrid;

	public Stockpile(int x, int y, int width, int height, int capacity, Collection<Resource> whitelist) {
		super(x, y, width, height, true);
		inventoryGrid = new SingleInventory[width][height];
		List<Inventory> invs = new ArrayList<>();
		for (int i = 0; i < width; i ++)
			for (int j = 0; j < height; j ++)
				invs.add(inventoryGrid[i][j] = new SingleInventory(EnumSet.copyOf(whitelist), true, true, capacity));
		inventory = new CompoundInventory(true, true, invs);
	}

	public Stockpile(int x, int y, int width, int height, int capacity) {
		this(x, y, width, height, capacity, Resource.valueSet());
	}

	public Inventory getInventory(int x, int y) {
		return inventoryGrid[x][y];
	}

	@Override
	public Inventory getInventory() {
		return inventory;
	}

	@Override
	public boolean hasInventory() {
		return true;
	}
}
