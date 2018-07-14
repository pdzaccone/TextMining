package clustering;

import linearAlgebra.ITermsVector;

public interface ICluster extends Iterable<ITermsVector> {
	public void addVector(ITermsVector input);
	public void calculateCentralVector();
	public ITermsVector getCentralVector();
	public double cosine(ITermsVector vector);
}
