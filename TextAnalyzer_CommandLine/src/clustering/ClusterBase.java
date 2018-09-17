package clustering;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import linearAlgebra.IDistanceMetrics;
import linearAlgebra.ITermsVector;
import linearAlgebra.TermsVectorApacheCommons;

/**
 * Base implementation of a {@link ICluster} 
 * @author Pdz
 *
 */
public class ClusterBase implements ICluster {
	
	/**
	 * Cluster category
	 */
	private final String category;
	
	/**
	 * Vectors inside cluster 
	 */
	private List<ITermsVector> vectors;
	
	/**
	 * Central cluster vector
	 */
	private ITermsVector center;
	
	/**
	 * Function for calculating distance between cluster and other vectors
	 */
	private IDistanceMetrics distanceCalculator;
	
	/**
	 * Constructor with parameters
	 * @param distMetric Function for calculating distance between cluster and other vectors
	 * @param category Cluster category
	 */
	public ClusterBase(IDistanceMetrics distMetric, String category) {
		this.vectors = new ArrayList<>();
		this.center = null;
		this.category = category;
		this.distanceCalculator = distMetric;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ClusterBase) {
			return this.category.equals(((ClusterBase)obj).category);
		}
		return false;
	}
	
	@Override
    public int hashCode() {
		return 31 + this.category.hashCode();
    }

	@Override
	public void addVector(ITermsVector input) {
		int size = -1;
		if (!vectors.isEmpty()) {
			size = vectors.get(vectors.size() - 1).size();
		}
		if (size == -1 || input.size() == size) {
			this.vectors.add(input);
		}
	}

	@Override
	public void calculateCentralVector() throws Exception {
		if (!vectors.isEmpty()) {
			this.center = new TermsVectorApacheCommons(vectors.get(0).size());
			for (ITermsVector vector : this.vectors) {
				center = center.add(vector);
			}
			center = center.divide(this.vectors.size());
		}
	}

	@Override
	public ITermsVector getCentralVector() {
		return this.center;
	}

	@Override
	public void setCentralVector(ITermsVector input) {
		this.center = input;
	}

	@Override
	public Iterator<ITermsVector> iterator() {
		return this.vectors.iterator();
	}

	@Override
	public double calculateDistance(ITermsVector vector) throws Exception {
		return this.distanceCalculator.calculateDistance(this.center, vector);
	}

	/**
	 * Currently simply returns 1 if vector belongs to cluster and zero otherwise 
	 */
	@Override
	public double getVectorWeight(ITermsVector vector) {
		if (this.vectors.contains(vector)) {
			return 1;
		}
		return 0;
	}
}