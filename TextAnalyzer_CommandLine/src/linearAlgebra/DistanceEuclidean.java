package linearAlgebra;

/**
 * This class calculates Euclidean distance between vectors
 * @author Pdz
 *
 */
public class DistanceEuclidean implements IDistanceMetrics {

	@Override
	public double calculateDistance(ITermsVector vector1, ITermsVector vector2) throws Exception {
		if (vector1 instanceof TermsVectorApacheCommons && vector2 instanceof TermsVectorApacheCommons) {
			return ((TermsVectorApacheCommons) vector1).getVectorData().getDistance(((TermsVectorApacheCommons) vector2).getVectorData());
		}
		throw new Exception("Unsupported types");
	}
}
