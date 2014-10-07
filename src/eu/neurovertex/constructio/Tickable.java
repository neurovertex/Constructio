package eu.neurovertex.constructio;

/**
 * @author Neurovertex
 *         Date: 30/09/2014, 15:52
 */
public interface Tickable {
	public boolean tick();

	default Priority getPriority() {
		return Priority.NORMAL;
	}

	public enum Priority {
		VERY_HIGH, HIGH, NORMAL, LOW, VERY_LOW
	}
}
