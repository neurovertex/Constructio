package eu.neurovertex.constructio.entities;

import eu.neurovertex.constructio.Constructio;
import eu.neurovertex.constructio.Grid;

/**
 * @author Neurovertex
 *         Date: 30/09/2014, 15:47
 */
public interface FreeEntity {
	double getX();
	double getY();
	default Grid.Square getPosition() {
		return Constructio.getInstance().getGrid().get(getX(), getY());
	}
	double getSpeed();
	void destroyEntity();
	boolean isDestroyed();
}
