package clustering;

import linearAlgebra.ITermsVector;

/**
 * This interface introduces a concept of cluster
 * @author Pdz
 *
 */
public interface ICluster extends Iterable<ITermsVector> {
	
	/**
	 * Adds new terms-vector to the cluster
	 * @param input New terms-vector
	 */
	public void addVector(ITermsVector input);
	
	/**
	 * Calculates cluster's center (central vector)
	 * @throws Exception
	 */
	public void calculateCentralVector() throws Exception;
	
	/**
	 * Gets cluster's central vector
	 * @return Central vector or null
	 */
	public ITermsVector getCentralVector();
	
	/**
	 * Calculates distance between the given vector and the central vector of the cluster
	 * @param vector Vector to calculate distance from
	 * @return Resulting distance
	 * @throws Exception
	 */
	public double calculateDistance(ITermsVector vector) throws Exception;
	
	/**
	 * Gets weight of a specified vector
	 * @param vector Vector to weight
	 * @return Resulting weight
	 */
	public double getVectorWeight(ITermsVector vector);
	
	/**
	 * "Manually" sets the central vector
	 * @param input New central vector
	 */
	void setCentralVector(ITermsVector input);
}
