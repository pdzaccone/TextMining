package functions;

import java.util.List;

import linearAlgebra.IDocTermMatrix;

public interface IMatrixFilter {
	public boolean shouldReduceDimensions(IDocTermMatrix matrix);
	public List<Integer> reduceDimensions(IDocTermMatrix matrix);
}
