package eu.neurovertex.constructio;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Neurovertex
 *         Date: 30/09/2014, 16:27
 */
public class Scheduler {
	private static List<Tickable> tickables = new ArrayList<>();
	private static int period;
	private static boolean enabled = false;
	private final static Timer timer = new Timer("Scheduler Timer", true);
	private final static TimerTask task = new TimerTask() {
		@Override
		public void run() {
			Scheduler.tick();
		}
	};

	public synchronized static void initialize(int period) {
		Scheduler.period = period;
	}

	public synchronized static void start() {
		timer.scheduleAtFixedRate(task, period, period);
	}

	public synchronized static void stop() {
		timer.cancel();
	}

	private Scheduler() {}

	public synchronized static boolean addTickable(Tickable tickable) {
		boolean b = tickables.add(tickable);
		Collections.sort(tickables, (Tickable t1, Tickable t2) -> t1.getPriority().compareTo(t2.getPriority()));
		if (b && tickables.size() == 1)
			start();
		System.out.printf("Added tickable %s (%s)%n", tickable, tickable.getPriority());
		return b;
	}

	public synchronized static boolean removeTickable(Tickable tickable) {
		return tickables.remove(tickable);
	}

	private synchronized static void tick() {
		if (!enabled)
			return;
		List<Tickable> remove = tickables.stream().filter(Tickable::tick).collect(Collectors.toList());
		tickables.removeAll(remove);
		if (tickables.size() == 0) {
			System.out.println("No tickables left, stopping update thread");
		}
	}

	public static void setEnabled(boolean enabled) {
		Scheduler.enabled = enabled;
	}
}
