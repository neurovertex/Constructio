package eu.neurovertex.constructio.entities;

import eu.neurovertex.constructio.Grid;
import eu.neurovertex.constructio.Scheduler;
import eu.neurovertex.constructio.Tickable;

import java.util.Iterator;
import java.util.Optional;

import static eu.neurovertex.constructio.GridUtils.Path;

/**
 * @author Neurovertex
 *         Date: 30/09/2014, 15:50
 */
public abstract class AbstractFreeEntity implements FreeEntity, Tickable {
	private Optional<Iterator<Grid.Square>> path = Optional.empty();
	private double x, y;
	private Optional<Grid.Square> next = Optional.empty();
	private double speed;
	private boolean destroyed = false;

	public AbstractFreeEntity(Grid.Square position, double speed) {
		this.x = position.getX();
		this.y = position.getY();
		this.speed = speed;
		Scheduler.addTickable(this);
		position.getGrid().addFreeEntity(this);
	}

	@Override
	public boolean tick() {
		update();
		return isDestroyed();
	}

	public void update() {
		if (path.isPresent() && !next.isPresent()) {
			if (path.get().hasNext())
				next = Optional.of(path.get().next());
			else
				path = Optional.empty();
		}
		if (next.isPresent()) {
			double dist = speed;
			while (dist > 0 && next.isPresent())
				dist = advance(dist);
		}
	}

	private double advance(double advance) {
		double dx = next.get().getX() - x, dy = next.get().getY() - y,
				dist = Math.hypot(dx, dy), ratio = advance/dist;
		if (advance < dist) {
			x += dx * ratio;
			y += dy * ratio;
			return 0;
		} else {
			x = next.get().getX();
			y = next.get().getY();
			if (path.isPresent() && path.get().hasNext())
				next = Optional.of(path.get().next());
			else {
				next = Optional.empty();
				path = Optional.empty();
			}
			return advance - dist;
		}
	}

	@Override
	public double getX() {
		return x;
	}

	@Override
	public double getY() {
		return y;
	}

	@Override
	public double getSpeed() {
		return speed;
	}

	@Override
	public void destroyEntity() {
		destroyed = true;
	}

	@Override
	public boolean isDestroyed() {
		return destroyed;
	}

	public void setPath(Path path) {
		this.path = Optional.of(path.iterator());
	}

	public Optional<Iterator<Grid.Square>> getPath() {
		return path;
	}
}
