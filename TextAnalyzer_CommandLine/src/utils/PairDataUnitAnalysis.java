package utils;

import java.util.List;

import analysis.IAnalysisResult;
import analyzers.AnalysisTypes;
import dataUnits.IDataUnit;

/**
 * This class groups together a data unit and its analysis results
 * @author Pdz
 *
 */
public class PairDataUnitAnalysis {
	
	/**
	 * Data unit
	 */
	private IDataUnit data;
	
	/**
	 * Analysis results for the data unit
	 */
	private ListMap<AnalysisTypes, Pair<IAnalysisResult, Boolean>> analysis;
	
	/**
	 * Constructor with parameters
	 * @param doc Data unit
	 * @param analysis Analysis results
	 */
	public PairDataUnitAnalysis(IDataUnit doc, List<Pair<IAnalysisResult, Boolean>> analysis) {
		this.data = doc;
		this.analysis = new ListMap<>();
		for (Pair<IAnalysisResult, Boolean> val : analysis) {
			this.analysis.put(val.getFirst().getType(), val);
		}
	}
	
	/**
	 * Gets data unit
	 * @return
	 */
	public IDataUnit getDataUnit() {
		return data;
	}
	
	/**
	 * Gets analysis results
	 * @return
	 */
	public ListMap<AnalysisTypes, Pair<IAnalysisResult, Boolean>> getAnalysisData() {
		return analysis;
	}

	/**
	 * Updates internal data unit with provided data
	 * @param input
	 */
	public void updateDataUnit(IDataUnit input) {
		if (hasAnalysis()) {
			for (AnalysisTypes type : analysis.keySet()) {
				for (Pair<IAnalysisResult, Boolean> val : analysis.get(type)) {
					val.getFirst().update(input, val.getSecond());
				}
			}
		}
	}

	/**
	 * Checks whether the storage holds any analysis data
	 * @return
	 */
	private boolean hasAnalysis() {
		return analysis != null && !analysis.isEmpty();
	}
}
