package eu.neurovertex.constructio;

import java.util.function.Function;

/**
 * @author Neurovertex
 *         Date: 01/10/2014, 19:57
 */
public final class Distances {
	private static final DistanceFunction euclidianDistance = GridUtils::distance,
			taxicabDistance = (a, b) -> Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY()),
			groundDistance = composeFunction(sq -> (sq.getEntity().isPresent() && sq.getEntity().get().isSolid(sq) ? Double.POSITIVE_INFINITY : 1.0), defaultDistance());

	private Distances() {}

	public static DistanceFunction defaultDistance() {
		return taxicabDistance;
	}

	public static DistanceFunction euclidianDistance() {
		return euclidianDistance;
	}

	public static DistanceFunction taxicabDistance() {
		return taxicabDistance;
	}

	public static DistanceFunction groundDistance() {
		return groundDistance;
	}

	public static DistanceFunction composeFunction(Function<Grid.Square, Double> modifier, DistanceFunction distanceFunction) {
		return (Grid.Square a, Grid.Square b) -> modifier.apply(b) * distanceFunction.distance(a, b);
	}
}
