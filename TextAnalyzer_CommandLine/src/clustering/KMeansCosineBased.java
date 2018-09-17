package clustering;

import java.util.Map;

import linearAlgebra.DistanceCosine;
import linearAlgebra.ITermsVector;
import utils.Pair;

/**
 * This version of K-Means clustering algorithm uses cosine to calculate distance between vectors
 * @author Pdz
 *
 */
public class KMeansCosineBased extends KMeansBase {

	private static final double COSINE_THRESHOLD = 1;
	
	/**
	 * Constructor with parameter
	 * @param keepUnknownCategory Whether to keep the "unknown" category
	 */
	public KMeansCosineBased(boolean keepUnknownCategory) {
		super(keepUnknownCategory);
	}

	@Override
	protected void initSpecifics() {
		this.distMetrics = new DistanceCosine();
	}

	@Override
	protected Pair<String, Double> findNewCluster(Map<String, ICluster> input, ITermsVector vector) throws Exception {
		Pair<String, Double> result = new Pair<String, Double>("", Double.MAX_VALUE);
		for (String catInner : input.keySet()) {
			if (!keepUnknown && CATEGORY_UNKNOWN.equalsIgnoreCase(catInner)) {
				continue;
			}
			double dist = input.get(catInner).calculateDistance(vector);
			if (dist < COSINE_THRESHOLD && dist < result.getSecond()) {
				result.update(catInner, dist);
			} else if (dist >= COSINE_THRESHOLD) {
				continue;
			}
		}
		return result;
	}
}
