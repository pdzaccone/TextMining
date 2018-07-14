package utils;

import java.util.List;

import analysis.IAnalysisResult;
import analyzers.AnalysisTypes;
import analyzers.IAnalyzer;
import dataUnits.IDataUnit;

public class PairDataUnitAnalysis {
	
	private IDataUnit data;
	private ListMap<AnalysisTypes, Pair<IAnalysisResult, Boolean>> analysis;
	
	public PairDataUnitAnalysis(IDataUnit doc, List<Pair<IAnalysisResult, Boolean>> analysis) {
		this.data = doc;
		this.analysis = new ListMap<>();
		for (Pair<IAnalysisResult, Boolean> val : analysis) {
			this.analysis.put(val.getFirst().getType(), val);
		}
	}
	
	public IDataUnit getDataUnit() {
		return data;
	}
	
	public ListMap<AnalysisTypes, Pair<IAnalysisResult, Boolean>> getAnalysisData() {
		return analysis;
	}

	public void updateDataUnit(IDataUnit input) {
		if (hasAnalysis()) {
			for (AnalysisTypes type : analysis.keySet()) {
				for (Pair<IAnalysisResult, Boolean> val : analysis.get(type)) {
					val.getFirst().update(input, val.getSecond());
				}
			}
		}
	}

	private boolean hasAnalysis() {
		return analysis != null && !analysis.isEmpty();
	}
}
