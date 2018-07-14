package linearAlgebra;

import org.apache.commons.math3.linear.OpenMapRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SparseRealVector;

public class TermsVectorApacheCommons implements ITermsVector {

	private final int ID;
	private RealVector data;
	
	public TermsVectorApacheCommons(int id, SparseRealVector vector) {
		this.ID = id;
		this.data = vector;
	}

	public TermsVectorApacheCommons(int id, RealVector vector) {
		this.ID = id;
		this.data = vector;
	}

	public TermsVectorApacheCommons() {
		this.ID = -1;
		this.data = new SparseRealVector();
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
	public ITermsVector add(ITermsVector vector) {
		if (vector instanceof TermsVectorApacheCommons) {
			double minValue = this.data.getMinValue();
			double maxValue = this.data.getMaxValue();
			double minValue2 = ((TermsVectorApacheCommons)vector).data.getMinValue();
			double maxValue2 = ((TermsVectorApacheCommons)vector).data.getMaxValue();
			return new TermsVectorApacheCommons(ID, this.data.add(((TermsVectorApacheCommons)vector).data));
		}
		//TODO
		return null;
	}

	@Override
	public double cosine(ITermsVector vector) {
		if (vector instanceof TermsVectorApacheCommons) {
			return this.data.cosine(((TermsVectorApacheCommons)vector).data);
		}
		//TODO
		return Double.MAX_VALUE;
	}
}
