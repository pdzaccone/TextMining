package functions;

import java.util.List;

import linearAlgebra.IDocTermMatrix;

/**
 * This interface decides whether the matrix dimensions should be reduced and carries out the reduction
 * @author Pdz
 *
 */
public interface IMatrixFilter {
	
	/**
	 * Whether the reduction is required
	 * @param matrix Matrix to analyze
	 * @return True - reduction is required, false - not
	 */
	public boolean shouldReduceDimensions(IDocTermMatrix matrix);
	
	/**
	 * Reduces dimensions of the provided matrix according to the internal criteria
	 * @param matrix Matrix to analyze (and to reduce dimensions, if needed)
	 * @return List of rows that should be removed from the matrix
	 */
	public List<Integer> reduceDimensions(IDocTermMatrix matrix);
}
