package linearAlgebra;

/**
 * This interface provides base functionality for measuring distance between two vectors
 * @author Pdz
 *
 */
public interface IDistanceMetrics {
	
	/**
	 * Calculates distance between two provided vectors
	 * @param vector1 1st vector
	 * @param vector2 2nd vector
	 * @return Resulting distance
	 * @throws Exception
	 */
	public double calculateDistance(ITermsVector vector1, ITermsVector vector2) throws Exception;
}
