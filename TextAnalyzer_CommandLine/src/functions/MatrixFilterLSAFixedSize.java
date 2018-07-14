package functions;

import java.util.ArrayList;
import java.util.List;

import linearAlgebra.IDocTermMatrix;

public class MatrixFilterLSAFixedSize implements IMatrixFilter {

	private final int maxNum;
	
	public MatrixFilterLSAFixedSize(int maxNumberOfTerms) {
		this.maxNum = maxNumberOfTerms;
	}
	
	@Override
	public List<Integer> reduceDimensions(IDocTermMatrix matrix) {
		List<Integer> rowsToRemove = new ArrayList<>();
		for (int i = matrix.getNumberRows() - 1; i >= 0; i--) {
			if (i >= maxNum) {
				rowsToRemove.add(i);
			}
		}
		return rowsToRemove;
	}

	@Override
	public boolean shouldReduceDimensions(IDocTermMatrix input) {
		return input.getNumberTerms() > maxNum;
	}
}