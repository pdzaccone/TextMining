package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import analysis.EmptyAnalysis;
import analysis.IAnalysisResult;
import dataUnits.IDataUnit;

public class PairAnalysisResults {
	private List<Pair<IAnalysisResult, Boolean>> resultsLocal;
	private List<Pair<IAnalysisResult, Boolean>> resultsUp;
	
	public PairAnalysisResults() {
		this.resultsLocal = new ArrayList<>();
		this.resultsUp = new ArrayList<>();
	}

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

//	public void addResults(List<IAnalysisResult> input, boolean localUse) {
//		if (localUse) {
//			this.resultsLocal.addAll(input);
//		} else {
//			this.resultsUp.addAll(input);
//		}
//	}

	public List<Pair<IAnalysisResult, Boolean>> getResultsLocal() {
		return resultsLocal;
	}

	public List<Pair<IAnalysisResult, Boolean>> getResultsToSendUp() {
		return resultsUp;
	}

//	public ListMap<AnalysisTypes, IAnalysisResult> getResultsToSendUp() {
//		ListMap<AnalysisTypes, IAnalysisResult> result = new ListMap<>();
//		for (IAnalysisResult anRes : resultsUp) {
//			result.put(anRes.getType(), anRes);
//		}
//		return result;
//	}

	public void updateLocal(IDataUnit input) {
		Objects.requireNonNull(input);
		for (Pair<IAnalysisResult, Boolean> value : this.resultsLocal) {
			value.getFirst().update(input, value.getSecond());
		}
	}

	public boolean isEmpty() {
		return resultsLocal.isEmpty() && resultsUp.isEmpty();
	}
}
