package linearAlgebra;

public interface ITermsVector {
	public int getDocID();
	public ITermsVector divide(double coeff);
	public ITermsVector add(ITermsVector vector);
	public double cosine(ITermsVector vector);
}
