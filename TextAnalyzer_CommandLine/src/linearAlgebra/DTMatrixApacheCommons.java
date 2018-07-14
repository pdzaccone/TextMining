package linearAlgebra;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.DoubleStream;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.OpenMapRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import utils.Tuple;

public class DTMatrixApacheCommons implements IDocTermMatrix {

	private static final double EPSILON = 0.00001;
	
	private Map<Integer, Integer> docIndices;
	private RealMatrix data;
	
	public DTMatrixApacheCommons() {
		this.docIndices = new HashMap<>();
		this.data = null;
	}
	
	public DTMatrixApacheCommons(int numberTerms, int numberDocs) {
		this.docIndices = new HashMap<>();
		data = new OpenMapRealMatrix(numberTerms, numberDocs);
	}

	public DTMatrixApacheCommons(RealMatrix matrix, Map<Integer, Integer> indices) {
		this.data = matrix;
		this.docIndices = indices;
	}

	@Override
	public int getNumberTerms() {
		return data != null ? data.getRowDimension() : 0;
	}

	@Override
	public int getNumberDocs() {
		return data != null ? data.getColumnDimension() : 0;
	}

	@Override
	public int getNumberRows() {
		return getNumberTerms();
	}

	@Override
	public int getNumberColumns() {
		return getNumberDocs();
	}

	@Override
	public void setColumnVector(int columnNum, ITermsVector input) {
		if (data != null && input != null && input instanceof TermsVectorApacheCommons) {
			data.setColumnVector(columnNum, ((TermsVectorApacheCommons)input).getVectorData());
			this.docIndices.put(columnNum, input.getDocID());
		}
	}

	@Override
	public double calcDistanceTerms(int term1, int term2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double calcDistanceDocs(int doc1, int doc2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Tuple<IDocTermMatrix, IDocTermMatrix, IDocTermMatrix> calculateSVD() {
//		int numberCols = this.data.getColumnDimension();
//		int numberRows = this.data.getRowDimension();
		SingularValueDecomposition svd = new SingularValueDecomposition(data);
//		int columnDimension = svd.getU().getColumnDimension();
//		int rowDimension = svd.getU().getRowDimension();
//		int columnDimension2 = svd.getS().getColumnDimension();
//		int rowDimension2 = svd.getS().getRowDimension();
//		int columnDimension3 = svd.getVT().getColumnDimension();
//		int rowDimension3 = svd.getVT().getRowDimension();
		return new Tuple<>(new DTMatrixApacheCommons(svd.getU(), null),
						   new DTMatrixApacheCommons(svd.getS(), null), 
						   new DTMatrixApacheCommons(svd.getVT(), null));
	}

	@Override
	public void nullifyRows(List<Integer> input) {
//		double[] arr = new double[this.data.getColumnDimension()];
//		Arrays.fill(arr, 0);
		for (int index : input) {
			this.data.setRowVector(index, new OpenMapRealVector(this.data.getColumnDimension()));
		}
	}

	@Override
	public IDocTermMatrix multiply(IDocTermMatrix input) throws Exception {
		Objects.requireNonNull(input);
		Objects.requireNonNull(data);
		if (input instanceof DTMatrixApacheCommons) {
			return new DTMatrixApacheCommons(data.multiply(((DTMatrixApacheCommons)input).data), new HashMap<>());
		} else {
			throw new Exception("Unsupported type");
		}
	}

	@Override
	public List<Integer> removeEmptyRows() {
		List<Integer> emptyRows = new ArrayList<>();
		for (int i = 0; i < this.data.getRowDimension(); i++) {
			boolean notEmpty = DoubleStream.of(this.data.getRow(i)).anyMatch(x -> Math.abs(x) > EPSILON);
			if (!notEmpty) {
				emptyRows.add(i);
			}
		}
		if (!emptyRows.isEmpty()) {
			int[] finalRows = new int[this.data.getRowDimension() - emptyRows.size()];
			int[] finalColumns = new int[this.data.getColumnDimension()];
			for (int i = 0, j = 0; i < this.data.getRowDimension(); i++) {
				if (!emptyRows.contains(i)) {
					finalRows[j] = i;
					j++;
				}
			}
			for (int i = 0; i < this.data.getColumnDimension(); i++) {
				finalColumns[i] = i;
			}
			this.data = this.data.getSubMatrix(finalRows, finalColumns);
		}
		return emptyRows;
	}

	@Override
	public Collection<ITermsVector> getColumnVectors() {
		Collection<ITermsVector> res = new ArrayList<>();
		for (int i = 0; i < this.data.getColumnDimension(); i++) {
			res.add(getColumnData(i));
		}
		return res;
	}

	@Override
	public ITermsVector getColumnData(int index) {
		//TODO
		if (this.docIndices != null) {
			return new TermsVectorApacheCommons(this.docIndices.get(index), this.data.getColumnVector(index));
		}
		return null;
	}

	@Override
	public void updateMatrix(IDocTermMatrix input) throws Exception {
		if (input.getNumberColumns() == this.getNumberColumns() && input instanceof DTMatrixApacheCommons) {
			this.data = ((DTMatrixApacheCommons)input).data;
		} else {
			throw new Exception();
		}
	}
}