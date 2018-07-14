package linearAlgebra;

import java.util.Collection;
import java.util.List;

import utils.Tuple;

public interface IDocTermMatrix {
	public int getNumberTerms();
	public int getNumberDocs();
	public int getNumberRows();
	public int getNumberColumns();
	public double calcDistanceTerms(int term1, int term2);
	public double calcDistanceDocs(int doc1, int doc2);
//	public void setColumnVector(int id, SparseRealVector data);
	public void setColumnVector(int columnNum, ITermsVector data);
	public Tuple<IDocTermMatrix, IDocTermMatrix, IDocTermMatrix> calculateSVD();
	public void nullifyRows(List<Integer> input);
	public IDocTermMatrix multiply(IDocTermMatrix input) throws Exception;
	public List<Integer> removeEmptyRows();
	public Collection<ITermsVector> getColumnVectors();
	public ITermsVector getColumnData(int index);
	public void updateMatrix(IDocTermMatrix input) throws Exception;
}
