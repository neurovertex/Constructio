package eu.neurovertex.constructio;

import eu.neurovertex.constructio.Grid.Square;

import java.util.function.BiFunction;

/**
 * @author Neurovertex
 *         Date: 01/10/2014, 19:54
 */
@FunctionalInterface
public interface DistanceFunction extends BiFunction<Square, Square, Double> {
	double distance(Square a, Square b);

	@Override
	default Double apply(Square a, Square b) {
		return distance(a, b);
	}
}
