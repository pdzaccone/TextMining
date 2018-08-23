package dataUnits;

import java.util.List;

import analysis.IAnalysisResult;
import analyzers.AnalysisTypes;
import io.IReadableXML;
import io.ISaveableXML;
import utils.ListMap;

public interface IDataUnit extends ISaveableXML, IReadableXML {
	
	public static enum XMLTags {
		corpus("corpus"),
		singleDoc("doc"),
		elementaryDoc("elem"),
		analysisData("analysis"),
		documents("documents"),
		data("data"),
		corpusType("corpusType"),
		docType("docType"),
		elementaryType("elemType"),
		elementaryTag("tag");
		
		private final String tagText;
		
		private XMLTags(String text) {
			tagText = text;
		}

		public String getTagText() {
			return this.tagText;
		}
	}
	
	public void addAnalysis(AnalysisTypes type, IAnalysisResult input);
	public void addAllAnalysis(ListMap<AnalysisTypes, IAnalysisResult> additionalData);
	public void addAllAnalysis(List<IAnalysisResult> input);
	public List<IAnalysisResult> getAnalysisResults(AnalysisTypes type);
	public List<IAnalysisResult> getAllAnalysisResults();
	public void resetAnalysis(AnalysisTypes type);
	public boolean analysisIsFinalized(AnalysisTypes type);
	public boolean isEmpty();
}
