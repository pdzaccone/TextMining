package functions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import analysis.WordsMatrix;
import analyzers.AnalysisTypes;
import dataUnits.IDataUnitCorpus;
import dataUnits.IDataUnitDoc;
import linearAlgebra.IDocTermMatrix;
import utils.Languages;
import utils.Tuple;

public class LSAAlgorithm implements IWeightsToDistancesConverter {

	private IMatrixFilter filter;
	private WordsMatrix data;
	
	public LSAAlgorithm() {
		this.filter = new MatrixFilterNone();
		this.data = null;
	}
	
	@Override
	public void initializeData(IDataUnitCorpus input) {
		Objects.requireNonNull(input);
		if (input.getAnalysisResults(AnalysisTypes.weightMatrix).size() == 1) {
			this.data = (WordsMatrix) input.getAnalysisResults(AnalysisTypes.weightMatrix).get(0);
		}
	}
	
	@Override
	public void prepareData() throws Exception {
		for (Languages lang : this.data.getLanguages()) {
			Tuple<IDocTermMatrix, IDocTermMatrix, IDocTermMatrix> svd = this.data.getDataMatrix(lang).calculateSVD();
			if (filter.shouldReduceDimensions(svd.getSecond())) {
				List<Integer> rowsToDelete = new ArrayList<>();
				rowsToDelete = filter.reduceDimensions(svd.getSecond());
				svd.getSecond().nullifyRows(rowsToDelete);
			}
			this.data.updateDataMatrix(lang, svd.getFirst().multiply(svd.getSecond()).multiply(svd.getThird()));
		}
	}

	@Override
	public Set<Languages> getLanguages() {
		if (this.data != null) {
			return this.data.getLanguages();
		}
		return new HashSet<>();
	}

//	@Override
//	public IAnalysisResult getData() {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public WordsMatrix getData() {
		return data;
	}

	@Override
	public List<String> getTerms(Languages lang) {
		if (this.data != null) {
			return this.data.getTerms(lang);
		}
		return new ArrayList<>();
	}

	@Override
	public List<IDataUnitDoc> getDocuments(Languages lang) {
		if (this.data != null) {
			return this.data.getDocuments(lang);
		}
		return new ArrayList<>();
	}
	
	@Override
	public void setMatrixFilter(IMatrixFilter input) {
		this.filter = input;
	}
}
