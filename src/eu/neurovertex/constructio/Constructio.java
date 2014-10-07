package eu.neurovertex.constructio;

import java.util.logging.Logger;

/**
 * @author Neurovertex
 *         Date: 27/09/2014, 18:57
 */
public class Constructio {
	public static final Logger log = Logger.getLogger(Constructio.class.getName());
	private static Constructio instance = new Constructio();
	private Grid grid;

	public static Constructio getInstance() {
		return instance;
	}

	public static void main(String[] args) {
		instance.init();
		instance.run();
	}

	public void init() {
		Scheduler.initialize(50);
		grid = new Grid(20, 20);
	}

	public void run() {

	}

	public Grid getGrid() {
		return grid;
	}
}
