package functions;

import java.util.ArrayList;
import java.util.List;

import linearAlgebra.IDocTermMatrix;

/**
 * None-filter
 * @author Pdz
 *
 */
public class MatrixFilterNone implements IMatrixFilter {

	@Override
	public boolean shouldReduceDimensions(IDocTermMatrix matrix) {
		return false;
	}

	@Override
	public List<Integer> reduceDimensions(IDocTermMatrix matrix) {
		return new ArrayList<>();
	}
}
