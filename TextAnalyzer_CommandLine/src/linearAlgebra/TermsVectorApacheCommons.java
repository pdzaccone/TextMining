package linearAlgebra;

import java.util.List;
import java.util.Map;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

public class TermsVectorApacheCommons implements ITermsVector {

	private final int ID;
	private RealVector data;
	
	private TermsVectorApacheCommons(int id, RealVector vector) {
		this.ID = id;
		this.data = vector;
	}
	
	public TermsVectorApacheCommons(int size) {
		this.ID = DEFAULT_ID;
		this.data = new ArrayRealVector(size);
	}

	public TermsVectorApacheCommons(int id, List<String> wordList, Map<String, ? extends Number> wordVals) {
		this.ID = id;
		this.data = new ArrayRealVector(wordList.size());
		for (int i = 0; i < wordList.size(); i++) {
			if (wordVals.containsKey(wordList.get(i))) {
				this.data.setEntry(i, (double) wordVals.get(wordList.get(i)));
			}
		}
	}

	public TermsVectorApacheCommons(int id, double[] input) {
		this.ID = id;
		this.data = new ArrayRealVector(input);
	}

	public RealVector getVectorData() {
		return data;
	}

	@Override
	public int getDocID() {
		return this.ID;
	}

	@Override
	public ITermsVector divide(double coeff) {
		return new TermsVectorApacheCommons(ID, this.data.mapDivide(coeff));
	}

	@Override
	public ITermsVector add(ITermsVector vector) throws Exception {
		if (vector instanceof TermsVectorApacheCommons) {
			return new TermsVectorApacheCommons(ID, this.data.add(((TermsVectorApacheCommons) vector).getVectorData()));
		}
		throw new Exception("Unsupported data type");
	}

	@Override
	public int size() {
		return this.data.getDimension();
	}
}
