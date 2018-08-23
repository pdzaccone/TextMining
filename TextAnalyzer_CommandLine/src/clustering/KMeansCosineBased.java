package clustering;

import java.util.Map;

import linearAlgebra.DistanceCosine;
import linearAlgebra.ITermsVector;
import utils.Pair;

public class KMeansCosineBased extends KMeansBase {

	private static final double COSINE_THRESHOLD = 1;
		
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
