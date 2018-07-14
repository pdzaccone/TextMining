package analyzers;

import dataUnits.IDataUnitCorpus;

public interface IAnalyzer {
	public static final boolean LOCAL = true;
	public static final boolean SEND_UP = false;
	
	public void initialize(IDataUnitCorpus data);
	public boolean isInitialized();
}
