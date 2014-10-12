package eu.neurovertex.constructio;

import java.util.*;
import java.util.function.ToDoubleFunction;

import static eu.neurovertex.constructio.Constructio.log;

/**
 * @author Neurovertex
 *         Date: 30/09/2014, 15:53
 */
public final class GridUtils {
	private GridUtils() {}

	public static Optional<Path> findPath(Grid grid, Grid.Square src, Grid.Region dst,
										  DistanceFunction dist, ToDoubleFunction<Path> heuristic) {
		return new PathFinder().with(dist).with(heuristic).from(src).to(dst).find(grid);
	}

	public static Optional<Path> findPath(Grid grid, Grid.Square src, Grid.Square dst) {
		return new PathFinder().from(src).to(dst).find(grid);
	}

	public static PathFinder pathFinder() {
		return new PathFinder();
	}

	public static double distance(Grid.Square sq1, Grid.Square sq2) {
		return Math.hypot(sq1.getX() - sq2.getX(), sq1.getY() - sq2.getY());
	}

	public static Comparator<Path> aStarComparator(Grid.Square dst) {
		return (Path p1, Path p2) -> Double.compare(p1.getDistance() + distance(p1.getLast(), dst), p2.getDistance() + distance(p1.getLast(), dst));
	}

	public static class PathFinder {
		private Path src;
		private Grid.Region dst;
		private DistanceFunction distance = Distances.defaultDistance();
		private Optional<ToDoubleFunction<Path>> heuristic = Optional.empty();

		private PathFinder() {}

		public PathFinder from(Path src) {
			this.src = src;
			return this;
		}

		public PathFinder from(Grid.Square src) {
			this.src = new Path(src, distance);
			return this;
		}

		public PathFinder to(Grid.Region dst) {
			this.dst = dst;
			if (dst.getCenter().isPresent())
				heuristic = Optional.of(p -> p.getDistance() + distance(p.getLast(), dst.getCenter().get()));
			return this;
		}

		public PathFinder to(Grid.Square dst) {
			this.dst = sq -> sq == dst;
			if (!heuristic.isPresent())
				heuristic = Optional.of(p -> p.getDistance() + distance(p.getLast(), dst));
			return this;
		}

		public PathFinder with(DistanceFunction function) {
			this.distance = function;
			if (src != null)
				src.setDistanceFunction(function);
			return this;
		}

		public PathFinder with(ToDoubleFunction<Path> heur) {
			this.heuristic = Optional.of(heur);
			return this;
		}

		public Optional<Path> find(Grid grid) {
			if (src == null || dst == null || distance == null)
				throw new IllegalStateException("Null attribute");

			Comparator<Path> cmp = Comparator.comparingDouble(this.heuristic.orElse(Path::getDistance));
			LinkedList<Path> paths = new LinkedList<>();
			Path[][] pathGrid = new Path[grid.getWidth()][grid.getHeight()];
			paths.add(src);
			Optional<Path> srcPath = Optional.of(src);
			while (srcPath.isPresent()) {
				pathGrid[srcPath.get().getLast().getX()][srcPath.get().getLast().getY()] = srcPath.get();
				srcPath = srcPath.get().getParent();
			}

			while (paths.size() > 0 && !dst.test(paths.peek().getLast())) {
				Path p = paths.poll();
				Set<Grid.Square> adj = p.getLast().getAdjacent();
				for (Grid.Square sq : adj) {
					Path path = p.append(sq);
					Optional<Path> curPath = Optional.ofNullable(pathGrid[sq.getX()][sq.getY()]);
					if (!Double.isFinite(path.getDistance()) || curPath.map((c) -> cmp.compare(c, path) <= 0).orElse(false))
						continue;
					curPath.map(paths::remove);
					pathGrid[sq.getX()][sq.getY()] = path;
					paths.add(path);
				}
				Collections.sort(paths, cmp);
			}
			return Optional.ofNullable(paths.size() > 0 ? paths.peek() : null);
		}
	}

	public static class Path implements Comparable<Path>, Iterable<Grid.Square> {
		private Grid.Square square;
		private Optional<Path> parent;
		private double distance;
		private DistanceFunction distFunc;

		public Path(Grid.Square next, DistanceFunction distFunc) {
			if (next == null)
				throw new IllegalArgumentException("Next can't be null");
			this.distFunc = distFunc;
			this.square = next;
			this.parent = Optional.empty();
		}

		public Path(Grid.Square first) {
			this(first, Distances.defaultDistance());
		}

		public Path(Path base, Grid.Square next) {
			this(next, base.distFunc);
			this.parent = Optional.of(base);
			distance = parent.map(p->p.distance + distFunc.apply(p.square, next)).orElse(0.);
		}

		public Path append(Grid.Square square) {
			return new Path(this, square);
		}

		public Collection<Grid.Square> getPath() {
			Collection<Grid.Square> path = parent.map(Path::getPath).orElse(new ArrayList<>(Arrays.asList(square)));
			path.add(square);
			return path;
		}

		public Optional<Grid.Square> getNext(Grid.Square current) {
			if (current == square)
				return Optional.empty();
			else if (parent.isPresent()) {
				Optional<Grid.Square> sq = parent.get().getNext(current);
				return (sq.isPresent()) ? sq : Optional.of(square);
			}
			// If it reaches there it means current wasn't a get of the path chain
			log.severe(String.format("Called getNext with a square that wasn't in the chain : %s\n%s", square, printPath()));
			return Optional.of(square);
		}

		public double getDistance() {
			return distance;
		}

		public DistanceFunction getDistanceFunction() {
			return distFunc;
		}

		public void setDistanceFunction(DistanceFunction func) {
			this.distFunc = func;
		}

		public Grid.Square getLast() {
			return square;
		}

		public Grid.Square getFirst() {
			return parent.map(Path::getFirst).orElse(square);
		}

		public Optional<Path> getParent() {
			return parent;
		}

		public boolean has(Grid.Square sq) {
			return this.square == sq || parent.map((p) -> p.has(sq)).orElse(false);
		}

		@Override
		public int compareTo(Path o) {
			return Double.compare(distance, o.distance);
		}

		@Override
		public int hashCode() {
			return buildPath().toString().hashCode();
		}

		public String printPath() {
			return buildPath().toString();
		}

		private StringBuilder buildPath() {
			StringBuilder sb = parent.map(p -> p.buildPath().append('.')).orElse(new StringBuilder());
			return sb.append('<').append(square.getX()).append(':').append(square.getY()).append('>');
		}

		@Override
		public String toString() {
			return String.format("<Path,last=%s,dist=%f>", square.toString(), distance);
		}

		@Override
		public Iterator<Grid.Square> iterator() {
			return getPath().iterator();
		}
	}
}
