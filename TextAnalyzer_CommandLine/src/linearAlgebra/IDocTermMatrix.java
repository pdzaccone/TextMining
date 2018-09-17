package linearAlgebra;

import java.util.Collection;
import java.util.List;

import utils.Tuple;

/**
 * This interface defines basic functionality that a term-document matrix should have
 * @author Pdz
 *
 */
public interface IDocTermMatrix {
	
	/**
	 * Gets overall number of terms
	 * @return Number of terms in matrix
	 */
	public int getNumberTerms();
	
	/**
	 * Gets overall number of documents
	 * @return Number of documents in matrix
	 */
	public int getNumberDocs();
	
	/**
	 * Gets number of rows
	 * @return Number of rows
	 */
	public int getNumberRows();
	
	/**
	 * Gets number of columns
	 * @return Number of columns
	 */
	public int getNumberColumns();
	
	/**
	 * Sets vector for a specific column
	 * @param columnNum Column number
	 * @param data Vector
	 */
	public void setColumnVector(int columnNum, ITermsVector data);
	
	/**
	 * Calculates SVD (singular-value decomposition) for the matrix
	 * @return Tuple of 3 resulting matrices
	 */
	public Tuple<IDocTermMatrix, IDocTermMatrix, IDocTermMatrix> calculateSVD();
	
	/**
	 * Sets all values in specified rows to 0
	 * @param input List of index numbers of rows to nullify
	 */
	public void nullifyRows(List<Integer> input);
	
	/**
	 * Multiples two matrices
	 * @param input 2nd matrix
	 * @return Resulting matrix
	 * @throws Exception
	 */
	public IDocTermMatrix multiply(IDocTermMatrix input) throws Exception;
	
	/**
	 * Removes empty rows from the matrix (those with no values greater than some specified minimum)
	 * @return List with indices of removed rows
	 */
	public List<Integer> removeEmptyRows();
	
	/**
	 * Retrieves collection of term vectors
	 * @return All term vectors
	 */
	public Collection<ITermsVector> getColumnVectors();
	
	/**
	 * Gets terms-vector with a specific index 
	 * @param index Index
	 * @return Terms-vector or null, if the index is outside of bounds
	 */
	public ITermsVector getColumnData(int index);
	
	/**
	 * Updates internal matrix with new data
	 * @param input New data
	 * @throws Exception
	 */
	public void updateMatrix(IDocTermMatrix input) throws Exception;
}