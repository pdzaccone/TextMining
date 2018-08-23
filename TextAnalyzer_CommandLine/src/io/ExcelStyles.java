package io;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;

public enum ExcelStyles {
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
	
	private static Map<ExcelStyles, CellStyle> styles = new HashMap<>(); 
	
	abstract public CellStyle createStyle(Workbook wb);
	public static void resetStyles() {
		styles.clear();
	}
}
