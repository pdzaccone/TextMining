package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import analysis.EmptyAnalysis;
import analysis.IAnalysisResult;
import dataUnits.IDataUnit;

/**
 * This class holds {@link IAnalysisResult} objects that should be applied locally and those to be sent to higher level
 * @author Pdz
 *
 */
public class PairAnalysisResults {
	
	/**
	 * Analysis results to be used locally
	 */
	private List<Pair<IAnalysisResult, Boolean>> resultsLocal;
	
	/**
	 * Analysis results to send to a higher level
	 */
	private List<Pair<IAnalysisResult, Boolean>> resultsUp;

	/**
	 * Constructor without parameters
	 */
	public PairAnalysisResults() {
		this.resultsLocal = new ArrayList<>();
		this.resultsUp = new ArrayList<>();
	}

	/**
	 * Adds new analysis result
	 * @param input Analysis result data + whether it should overwrite existing one of the same type
	 * @param localUse Whether the analysis result should be used locally or should be sent to a higher level
	 */
	public void addResult(Pair<IAnalysisResult, Boolean> input, boolean localUse) {
		Objects.requireNonNull(input);
		if (!(input.getFirst() instanceof EmptyAnalysis)) {
			if (localUse) {
				this.resultsLocal.add(input);
			} else {
				this.resultsUp.add(input);
			}
		}
	}

	/**
	 * Gets all local analysis results
	 * @return All local analysis results + whether they should overwrite existing results of the same type
	 */
	public List<Pair<IAnalysisResult, Boolean>> getResultsLocal() {
		return resultsLocal;
	}

	/**
	 * Gets all to-send-up analysis results
	 * @return All to-send-up analysis results + whether they should overwrite existing results of the same type
	 */
	public List<Pair<IAnalysisResult, Boolean>> getResultsToSendUp() {
		return resultsUp;
	}

	/**
	 * Updates provided data unit with local analysis results
	 * @param input Data unit to update
	 */
	public void updateLocal(IDataUnit input) {
		Objects.requireNonNull(input);
		for (Pair<IAnalysisResult, Boolean> value : this.resultsLocal) {
			value.getFirst().update(input, value.getSecond());
		}
	}

	/**
	 * Checks whether there are any analysis results available
	 * @return True - storage is not empty, otherwise false
	 */
	public boolean isEmpty() {
		return resultsLocal.isEmpty() && resultsUp.isEmpty();
	}
}
