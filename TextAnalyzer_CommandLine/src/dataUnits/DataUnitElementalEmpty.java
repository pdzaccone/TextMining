package dataUnits;

import io.IWriterXML;

public class DataUnitElementalEmpty extends DataUnitElementalBase {

	public DataUnitElementalEmpty() {
		super("", "");
	}
	
//	@Override
//	public boolean equals(Object obj) {
//		if (obj instanceof DataUnitElementalEmpty) {
//			return true;
//		}
//		return false;
//	}
//	
//	@Override
//    public int hashCode() {
//		return 1;
//    }

	@Override
	public boolean writeToXML(IWriterXML writer) {
		return true;
	}
}
