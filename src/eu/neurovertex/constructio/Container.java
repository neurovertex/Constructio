package eu.neurovertex.constructio;

/**
 * @author Neurovertex
 *         Date: 08/10/2014, 23:10
 */
public interface Container extends Grid.GridEntity {
	public Inventory getInventory();
	public boolean hasInventory();
}
