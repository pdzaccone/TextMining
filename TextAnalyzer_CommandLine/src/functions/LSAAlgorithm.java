package functions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import analysis.IAnalysisResult;
import analysis.WordsMatrix;
import analyzers.AnalysisTypes;
import dataUnits.IDataUnitCorpus;
import dataUnits.IDataUnitDoc;
import linearAlgebra.IDocTermMatrix;
import utils.Languages;
import utils.Tuple;

/**
 * LSA-algorithm
 * @author Pdz
 *
 */
public class LSAAlgorithm implements IWeightsToDistancesConverter {

	/**
	 * Matrix filter, used for reducing matrix dimensions
	 */
	private IMatrixFilter filter;
	
	/**
	 * Internal data storage
	 */
	private WordsMatrix data;
	
	/**
	 * Constructor without parameters
	 */
	public LSAAlgorithm() {
		this.filter = new MatrixFilterNone();
		this.data = null;
	}
	
	@Override
	public void initializeData(IDataUnitCorpus input) {
		Objects.requireNonNull(input);
		List<IAnalysisResult> analysisResults = input.getAnalysisResults(AnalysisTypes.weightMatrix);
		if (analysisResults != null && analysisResults.size() == 1) {
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
