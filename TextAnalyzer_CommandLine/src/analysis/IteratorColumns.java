package analysis;

import java.util.Collection;
import java.util.Iterator;

import linearAlgebra.ITermsVector;

public class IteratorColumns implements Iterator<ITermsVector> {

	private final Collection<ITermsVector> data;
	
	public IteratorColumns(final Collection<ITermsVector> input) {
		this.data = input;
	}
	
	@Override
	public boolean hasNext() {
		return data.iterator().hasNext();
	}

	@Override
	public ITermsVector next() {
		return this.data.iterator().next();
	}
}
