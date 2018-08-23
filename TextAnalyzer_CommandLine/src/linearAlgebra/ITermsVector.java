package linearAlgebra;

public interface ITermsVector {
	
	public static int DEFAULT_ID = -1;
	
	public int getDocID();
	public ITermsVector divide(double coeff);
	public ITermsVector add(ITermsVector vector) throws Exception;
	public int size();
}
