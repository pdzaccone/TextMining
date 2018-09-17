package dataUnits;

import io.IWriterXML;

/**
 * Empty elemental data block
 * @author Pdz
 *
 */
public class DataUnitElementalEmpty extends DataUnitElementalBase {

	/**
	 * Constructor without parameters
	 */
	public DataUnitElementalEmpty() {
		super("", "");
	}
	
	@Override
	public boolean writeToXML(IWriterXML writer) {
		return true;
	}
}
