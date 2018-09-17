package analyzers;

import dataUnits.IDataUnitCorpus;

/**
 * This is a base interface for data analyzers
 * @author Pdz
 *
 */
public interface IAnalyzer {
	
	/**
	 * This constant marks analysis results as belonging to the same data level
	 */
	public static final boolean LOCAL = true;

	/**
	 * This constant marks analysis results as belonging to the next data level (1 higher)
	 */
	public static final boolean SEND_UP = false;
	
	/**
	 * Initializes analyzer
	 * @param data Current corpus data
	 */
	public void initialize(IDataUnitCorpus data);
	
	/**
	 * Checks whether the analyzer has been initialized 
	 * @return True if analyzer has been initialized successfully, otherwise false
	 */
	public boolean isInitialized();
}
