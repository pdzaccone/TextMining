package clustering;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import linearAlgebra.ITermsVector;
import linearAlgebra.TermsVectorApacheCommons;

public class ClusterBase implements ICluster {
		
	private final String category;
	private List<ITermsVector> vectors;
	private ITermsVector center;
	
	public ClusterBase(String category) {
		this.vectors = new ArrayList<>();
		this.center = null;
		this.category = category;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ClusterBase) {
			return this.category.equals(((ClusterBase)obj).category);
		}
		return false;
	}
	
	@Override
    public int hashCode() {
		return 31 + this.category.hashCode();
    }

	@Override
	public void addVector(ITermsVector input) {
		this.vectors.add(input);
	}

	@Override
	public void calculateCentralVector() {
		this.center = new TermsVectorApacheCommons();
		for (ITermsVector vector : this.vectors) {
			center = center.add(vector);
		}
		center = center.divide(this.vectors.size());
	}

	@Override
	public ITermsVector getCentralVector() {
		return this.center;
	}

	@Override
	public Iterator<ITermsVector> iterator() {
		return this.vectors.iterator();
	}

	@Override
	public double cosine(ITermsVector vector) {
		return this.center.cosine(vector);
	}
}