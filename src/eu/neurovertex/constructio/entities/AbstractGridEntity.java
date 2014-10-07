package eu.neurovertex.constructio.entities;

import eu.neurovertex.constructio.Constructio;
import eu.neurovertex.constructio.Grid;

import java.awt.*;

/**
 * @author Neurovertex
 *         Date: 30/09/2014, 15:37
 */
public abstract class AbstractGridEntity implements Grid.GridEntity {
	private final boolean solid;
	private Rectangle region;
	private boolean removed = false;

	public AbstractGridEntity(int x, int y, int width, int height, boolean solid) {
		region = new Rectangle(x, y, width, height);
		this.solid = solid;
	}

	@Override
	public boolean isSolid(Grid.Square pos) {
		return solid;
	}

	public int getWidth() {
		return region.width;
	}

	public int getHeight() {
		return region.height;
	}

	@Override
	public Grid.Region getRegion() {
		return sq -> region.contains(sq.getX()+0.5, sq.getY()+0.5);
	}

	@Override
	public int getX() {
		return region.x;
	}

	@Override
	public int getY() {
		return region.y;
	}

	@Override
	public boolean canAdd() {
		return Constructio.getInstance().getGrid().getSquares(getRegion()).filter(g -> g.getEntity().isPresent()).count() == 0;
	}

	@Override
	public void remove() {
		removed = true;
	}

	@Override
	public boolean isRemoved() {
		return removed;
	}
}
