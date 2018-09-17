package io;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * This enumeration holds various styles for Excel-files
 * @author Pdz
 *
 */
public enum ExcelStyles {
	
	/**
	 * This style is used for the main title
	 */
	mainTitle {
		@Override
		public CellStyle createStyle(Workbook wb) {
			if (styles.containsKey(this)) {
				return styles.get(this);
			}
			CellStyle style = wb.createCellStyle();
			style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
	        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	        style.setAlignment(HorizontalAlignment.CENTER);	        
	        styles.put(this, style);
	        return style;
		}
	},
	
	/**
	 * This style is used for the secondary title
	 */
	secondaryTitle {
		@Override
		public CellStyle createStyle(Workbook wb) {
			if (styles.containsKey(this)) {
				return styles.get(this);
			}
			CellStyle style = wb.createCellStyle();
			style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
	        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	        style.setAlignment(HorizontalAlignment.CENTER);
	        styles.put(this, style);
	        return style;
		}
	};
	
	/**
	 * Map with all styles currently in use. This map is cleared after the data have been saved
	 */
	private static Map<ExcelStyles, CellStyle> styles = new HashMap<>(); 
	
	/**
	 * Creates style based on a provided {@link Workbook}
	 * @param wb Workbook
	 * @return Resulting cell style object
	 */
	abstract public CellStyle createStyle(Workbook wb);
	
	/**
	 * Clears the internal storage with styles
	 */
	public static void resetStyles() {
		styles.clear();
	}
}
