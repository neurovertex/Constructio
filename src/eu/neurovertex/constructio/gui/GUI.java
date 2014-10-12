package eu.neurovertex.constructio.gui;

import eu.neurovertex.constructio.Constructio;
import eu.neurovertex.constructio.Scheduler;
import eu.neurovertex.constructio.Tickable;

import javax.swing.*;

/**
 * @author Neurovertex
 *         Date: 30/09/2014, 14:40
 */
public class GUI implements Tickable {
	private static GUI instance;
	private JFrame window;
	private Constructio constructio = Constructio.getInstance();

	public static GUI getInstance() {
		if (instance == null)
			instance = new GUI();
		return instance;
	}

	private GUI() {
		window = new JFrame("Constructio");
		window.add(new GridPanel(constructio.getGrid()));
		window.pack();
		window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		window.setVisible(true);
		Scheduler.addTickable(this);
	}

	@Override
	public boolean tick() {
		window.repaint();
		return false;
	}
}
