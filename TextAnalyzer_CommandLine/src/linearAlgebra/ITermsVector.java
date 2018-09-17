package linearAlgebra;

/**
 * Base interface for the terms vector
 * @author Pdz
 *
 */
public interface ITermsVector {
	
	/**
	 * Default ID value
	 */
	public static int DEFAULT_ID = -1;
	
	/**
	 * Gets ID of the corresponding document
	 * @return ID (or {@link ITermsVector#DEFAULT_ID} for temporary vectors)
	 */
	public int getDocID();
	
	/**
	 * Divides all vector elements by a provided coefficient
	 * @param coeff Divisor
	 * @return New vector
	 */
	public ITermsVector divide(double coeff);
	
	/**
	 * Adds two vectors
	 * @param vector Vector to add
	 * @return Resulting vector
	 * @throws Exception
	 */
	public ITermsVector add(ITermsVector vector) throws Exception;
	
	/**
	 * Gets vector size (dimensions)
	 * @return Vector dimension
	 */
	public int size();
}