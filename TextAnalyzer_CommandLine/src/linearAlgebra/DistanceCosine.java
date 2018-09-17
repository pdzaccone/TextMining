package linearAlgebra;

/**
 * This class calculates distance between vectors as a cosine
 * @author Pdz
 *
 */
public class DistanceCosine implements IDistanceMetrics {

	@Override
	public double calculateDistance(ITermsVector vector1, ITermsVector vector2) throws Exception {
		if (vector1 instanceof TermsVectorApacheCommons && vector2 instanceof TermsVectorApacheCommons) {
			return 1 - ((TermsVectorApacheCommons) vector1).getVectorData().cosine(((TermsVectorApacheCommons) vector2).getVectorData());
		}
		throw new Exception("Unsupported data types");
	}
}
