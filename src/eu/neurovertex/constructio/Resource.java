package eu.neurovertex.constructio;

import java.awt.*;
import java.util.EnumSet;
import java.util.Set;

/**
 * @author Neurovertex
 *         Date: 08/10/2014, 16:57
 */
public enum Resource {
	WOOD(new Color(128, 91, 32)),
	METAL(Color.DARK_GRAY),
	STONE(Color.LIGHT_GRAY);
	private final Color color;

	Resource(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	public static Set<Resource> valueSet() {
		return EnumSet.allOf(Resource.class);
	}
}
