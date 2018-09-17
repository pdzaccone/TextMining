package dataUnits;

import java.util.List;

import analysis.IAnalysisResult;
import analyzers.AnalysisTypes;
import io.IReadableXML;
import io.ISaveableXML;
import utils.ListMap;

/**
 * Base interface for all data units
 * @author Pdz
 *
 */
public interface IDataUnit extends ISaveableXML, IReadableXML {
	
	/**
	 * This enumeration holds all data-unit-related XML-tags 
	 * @author Pdz
	 *
	 */
	public static enum XMLTags {
		
		/**
		 * Document corpus
		 */
		corpus("corpus"),
		
		/**
		 * Single document
		 */
		singleDoc("doc"),
		
		/**
		 * Elementary document
		 */
		elementaryDoc("elem"),
		
		/**
		 * Analysis data
		 */
		analysisData("analysis"),
		
		/**
		 * Documents block
		 */
		documents("documents"),
		
		/**
		 * Data block
		 */
		data("data"),
		
		/**
		 * Type of the document corpus (version)
		 */
		corpusType("corpusType"),
		
		/**
		 * Type of single document (version)
		 */
		docType("docType"),
		
		/**
		 * Type of elementary data unit (version)
		 */
		elementaryType("elemType"),
		
		/**
		 * Tag for elementary data unit
		 */
		elementaryTag("tag");
		
		/**
		 * Tag text
		 */
		private final String tagText;
		
		private XMLTags(String text) {
			tagText = text;
		}

		public String getTagText() {
			return this.tagText;
		}
	}
	
	/**
	 * Adds analysis results to the data unit
	 * @param type Type of analysis data
	 * @param input Analysis data itself
	 */
	public void addAnalysis(AnalysisTypes type, IAnalysisResult input);
	
	/**
	 * Adds all analysis data at once
	 * @param additionalData All analysis data
	 */
	public void addAllAnalysis(ListMap<AnalysisTypes, IAnalysisResult> additionalData);

	/**
	 * Adds all analysis data at once
	 * @param input All analysis data
	 */
	public void addAllAnalysis(List<IAnalysisResult> input);
	
	/**
	 * Gets analysis data of a specific type
	 * @param type Type of analysis data to search for
	 * @return List with resulting analysis data
	 */
	public List<IAnalysisResult> getAnalysisResults(AnalysisTypes type);
	
	/**
	 * Gets absolutely ALL analysis data
	 * @return ALL analysis data
	 */
	public List<IAnalysisResult> getAllAnalysisResults();
	
	/**
	 * Deletes analysis data of the specified type
	 * @param type Type of analysis data to delete
	 */
	public void resetAnalysis(AnalysisTypes type);
	
	/**
	 * Checks whether the analysis data of a specified type are final
	 * @param type Type of analysis data to check
	 * @return True, if data have been finalized, otherwise false
	 */
	public boolean analysisIsFinalized(AnalysisTypes type);
	
	/**
	 * Checks whether the data unit is empty of data or not
	 * @return True - empty, false otherwise
	 */
	public boolean isEmpty();
}
