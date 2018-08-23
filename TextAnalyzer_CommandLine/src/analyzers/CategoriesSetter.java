package analyzers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import analysis.IAnalysisResult;
import analysis.MetadataModification;
import dataUnits.IDataUnitCorpus;
import dataUnits.IDataUnitDoc;
import utils.Pair;
import utils.PairAnalysisResults;
import utils.WeightedMap;
import utils.WeightedObject;

public class CategoriesSetter implements ICategorizer {

	private Map<String, String> categories;
	private boolean isInitialized;
	private final boolean shouldOverwrite;
	
	public CategoriesSetter(boolean overwrite) {
		this.categories = new HashMap<>();
		this.isInitialized = false;
		this.shouldOverwrite = overwrite;
	}
	
	@Override
	public PairAnalysisResults feed(IDataUnitDoc input) {
		PairAnalysisResults result = new PairAnalysisResults();
		List<IAnalysisResult> IDs = input.getAnalysisResults(AnalysisTypes.documentID);
		if (IDs == null || IDs.isEmpty()) {
			return result;
		}
		String ID = ((MetadataModification)IDs.get(0)).getData().last().getData();
		if (!categories.containsKey(ID)) {
			return result;
		}
		WeightedMap counter = new WeightedMap();
		counter.add(categories.get(ID), 1);
		MetadataModification anResLocal = new MetadataModification(AnalysisTypes.category, counter.getWeights());
		anResLocal.markAsFinal();
		MetadataModification anResUp = new MetadataModification(AnalysisTypes.category, counter.getWeights());
		result.addResult(new Pair<>(anResLocal, shouldOverwrite), IAnalyzer.LOCAL);
		result.addResult(new Pair<>(anResUp, shouldOverwrite), IAnalyzer.SEND_UP);
		return result;
	}

	@Override
	public void initialize(IDataUnitCorpus data) {
		// TODO Auto-generated method stub		
	}

	@Override
	public boolean isInitialized() {
		return this.isInitialized;
	}

	@Override
	public PairAnalysisResults feed(IDataUnitCorpus input) {
		PairAnalysisResults results = new PairAnalysisResults();
		List<IAnalysisResult> anResAll = input.getAnalysisResults(AnalysisTypes.category);
		WeightedMap counter = new WeightedMap();
		for (IAnalysisResult anRes : anResAll) {
			Iterator<WeightedObject> iterator = ((MetadataModification)anRes).getData().iterator();
			while (iterator.hasNext()) {
				WeightedObject wo = iterator.next();
				counter.add(wo.getData(), 1);
			}
		}
		MetadataModification anRes = new MetadataModification(AnalysisTypes.category, counter.getWeights());
		anRes.markAsFinal();
		results.addResult(new Pair<>(anRes, true), IAnalyzer.LOCAL);
		return results;
	}
	
	public void readPredefinedCategories(String path) {
		boolean Ok = true;
		Map<String, String> data = new HashMap<>();
		try(FileInputStream fileIn = new FileInputStream(path);) {
			Workbook workbook = new HSSFWorkbook(fileIn);
			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> iterRow = sheet.rowIterator();
			while (iterRow.hasNext()) {
				Row row = iterRow.next();
				if (row.getRowNum() == 0) {
					continue;
				}
				String id = "";
				if (row.getCell(0).getCellTypeEnum() == CellType.NUMERIC) {
					double val = row.getCell(0).getNumericCellValue();
					id += (int) val;
				} else {
					id = row.getCell(0).getStringCellValue().trim();
				}
				String category = row.getCell(1).getStringCellValue().trim();
				data.put(id, category);
			}
			workbook.close();
			fileIn.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Ok = false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Ok = false;
		}
		if (Ok) {
			this.categories = data;
			isInitialized = true;
		}
	}
}
