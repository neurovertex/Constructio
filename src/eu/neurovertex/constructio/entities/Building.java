package eu.neurovertex.constructio.entities;

/**
 * @author Neurovertex
 *         Date: 30/09/2014, 17:00
 */
public class Building extends AbstractGridEntity {

	public Building(int x, int y, int width, int height, boolean solid) {
		super(x, y, width, height, solid);
	}

	public Building(int x, int y, int width, int height) {
		this(x, y, width, height, true);
	}
}
