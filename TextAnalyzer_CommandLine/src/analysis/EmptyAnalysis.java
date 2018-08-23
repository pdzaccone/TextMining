package analysis;

import analyzers.AnalysisTypes;
import dataUnits.IDataUnit;
import io.IWriterXML;

public class EmptyAnalysis implements IAnalysisResult {

	@Override
	public AnalysisTypes getType() {
		return AnalysisTypes.none;
	}

	@Override
	public boolean writeToXML(IWriterXML writer) {
		return true;
	}

	@Override
	public void markAsFinal() {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isFinal() {
		return true;
	}

	@Override
	public void update(IDataUnit obj, boolean shouldOverwrite) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isEmpty() {
		return true;
	}
	
//	@Override
//	public boolean equals(Object obj) {
//		if (obj instanceof EmptyAnalysis) {
//			return true;
//		}
//		return false;
//	}
//	
//	@Override
//	public int hashCode() {
//	    return 31;
//	}
}
