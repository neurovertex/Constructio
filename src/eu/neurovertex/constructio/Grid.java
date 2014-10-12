package eu.neurovertex.constructio;

import eu.neurovertex.constructio.entities.FreeEntity;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Neurovertex
 *         Date: 30/09/2014, 15:28
 */
public class Grid extends Observable implements Tickable {
	private final int width, height;
	private final Square[][] grid;
	private final List<Square> squares;
	private static final List<int[]> ADJ = Arrays.asList(new int[]{1,0},new int[]{0,1},new int[]{-1,0},new int[]{0,-1});
	private Set<GridEntity> gridEntities = new HashSet<>();
	private Set<FreeEntity> freeEntities = new HashSet<>();

	public Grid(int width, int height) {
		this.width = width;
		this.height = height;
		grid = new Square[width][height];
		List<Square> sq = new ArrayList<>();
		for (int i = 0; i < width; i ++)
			for (int j = 0; j < height; j ++)
				sq.add(grid[i][j] = new Square(i, j));
		squares = Collections.unmodifiableList(sq);
		Scheduler.addTickable(this);
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	// Round coordinates are at the center of the squares
	// To avoid ambiguity, all conversion should be done with this method
	public Square get(double x, double y) {
		return get((int)Math.round(x), (int)Math.round(y));
	}

	public Square get(int x, int y) {
		return grid[x][y];
	}

	public Optional<GridEntity> getEntity(int x, int y) {
		return get(x, y).entity;
	}

	public boolean addEntity(GridEntity entity) {
		if (entity.canAdd()) {
			getSquares(entity.getRegion()).forEach(sq->sq.setEntity(entity));
			gridEntities.add(entity);
			return true;
		} else
			return false;
	}

	public boolean removeEntity(GridEntity entity) {
		if (!entity.isRemoved()) {
			entity.remove();
			getSquares(entity.getRegion()).forEach(Square::unsetEntity);
		}
		return gridEntities.remove(entity);
	}

	public Region getRegion(final int x, final int y, final int w, final int h) {
		if (w < 0 || h < 0)
			return getRegion((w<0) ? x - w : x, (h<0) ? y - h : y, Math.abs(w), Math.abs(h));
		if (x < 0 || y < 0 || x+w > width || y+h > height)
			throw new IndexOutOfBoundsException(String.format("Region %d:%d, %dx%d out of bound", x, y, w, h));
		return sq -> sq.getX() >= x && sq.getY() >= y && sq.getX() < x+w && sq.getY() < y+h;
	}

	public Stream<Square> getSquares() {
		return squares.stream();
	}

	public Stream<Square> getSquares(Region r) {
		return getSquares().filter(r);
	}

	public Set<GridEntity> getGridEntities() {
		return Collections.unmodifiableSet(gridEntities);
	}

	public <E extends GridEntity> Stream<E> getGridEntities(Class<E> c) {
		return getGridEntities().stream().filter(c::isInstance).map(c::cast);
	}

	public Set<FreeEntity> getFreeEntities() {
		return Collections.unmodifiableSet(freeEntities);
	}

	public <E extends FreeEntity> Stream<E> getFreeEntities(Class<E> c) {
		return getFreeEntities().stream().filter(c::isInstance).map(c::cast);
	}

	public void addFreeEntity(FreeEntity entity) {
		freeEntities.add(entity);
	}

	@Override
	public boolean tick() {
		freeEntities.removeAll(freeEntities.stream().filter(FreeEntity::isDestroyed).collect(Collectors.toList()));
		return false;
	}

	@Override
	public Priority getPriority() {
		return Priority.LOW; // Execute after others
	}

	public class Square {
		private final int x, y;
		private Optional<GridEntity> entity = Optional.empty();
		private Terrain terrain = Terrain.GROUND;
		private Set<Square> adjacents;

		public Square(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public void setEntity(GridEntity entity) {
			if (this.entity.isPresent())
				throw new IllegalStateException("Entity already set");
			this.entity = Optional.of(entity);
			setChanged();
			notifyObservers(entity);
		}

		public void unsetEntity() {
			entity = Optional.empty();
			setChanged();
			notifyObservers();
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

		public Grid getGrid() {
			return Grid.this;
		}

		public Optional<GridEntity> getEntity() {
			return entity;
		}

		public boolean isSolid() {
			return terrain.isSolid() || entity.map(e -> e.isSolid(this)).orElse(false);
		}

		public Set<Square> getAdjacent() {
			if (adjacents == null) {
				Stream<Square> squares = ADJ.stream().map((a) -> new int[]{a[0] + x, a[1] + y}) // Make absolute coordinates
						.filter((a) -> a[0] >= 0 && a[1] >= 0 && a[0] < width && a[1] < height) // Filter out-of-grid
						.map((int[] a) -> get(a[0], a[1]));// Get squares
				adjacents = squares.collect(Collectors.toSet()); // Unnecessary splitting of the method chain. Workaround for an IDE bug
			}
			return adjacents;
		}

		@Override
		public String toString() {
			return String.format("<%d:%d,e:%s>", x, y, entity.isPresent());
		}

		public void setTerrain(Terrain terrain) {
			this.terrain = terrain;
		}

		public Terrain getTerrain() {
			return terrain;
		}
	}

	@FunctionalInterface
	public interface Region extends Predicate<Square> {
		default Optional<Square> getCenter() {
			return Optional.empty();
		}
		default Region increment() {
			final Region reg = this;
			return new Region() {
				@Override
				public boolean test(Square square) {
					return square.getAdjacent().stream().filter(reg::test).count() > 0;
				}
				@Override
				public Optional<Square> getCenter() {
					return reg.getCenter();
				}
			};
		}
	}

	public interface GridEntity {
		public boolean isSolid(Square pos);

		public Region getRegion();
		default Region getInteractibleRection() {
			return getRegion().increment();
		}
		public int getX();
		public int getY();

		public boolean canAdd();
		public void remove();
		public boolean isRemoved();
	}
}
