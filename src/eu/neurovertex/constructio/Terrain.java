package eu.neurovertex.constructio;

import java.awt.*;

/**
 * @author Neurovertex
 *         Date: 08/10/2014, 22:20
 */
public enum Terrain {
	GROUND(false, Color.LIGHT_GRAY), WATER(true, Color.BLUE.darker()), ROCK(true, Color.DARK_GRAY);

	private final boolean solid;
	private final Color color;

	Terrain(boolean solid, Color color) {
		this.solid = solid;
		this.color = color;
	}


	public boolean isSolid() {
		return solid;
	}

	public Color getColor() {
		return color;
	}
}
