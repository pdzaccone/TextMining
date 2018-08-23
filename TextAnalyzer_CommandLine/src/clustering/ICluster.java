package clustering;

import linearAlgebra.ITermsVector;

public interface ICluster extends Iterable<ITermsVector> {
	public void addVector(ITermsVector input);
	public void calculateCentralVector() throws Exception;
	public ITermsVector getCentralVector();
	public double calculateDistance(ITermsVector vector) throws Exception;
	public double getVectorWeight(ITermsVector vector);
	void setCentralVector(ITermsVector input);
}
