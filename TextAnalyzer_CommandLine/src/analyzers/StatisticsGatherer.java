package analyzers;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import analysis.IAnalysisResult;
import analysis.MetadataModification;
import dataUnits.IDataUnitCorpus;
import dataUnits.IDataUnitDoc;
import io.ExcelStyles;
import utils.ListMap;
import utils.PairAnalysisResults;
import utils.WeightedObject;

/**
 * This {@link IAnalyzer} gathers statistics about the input data and the resulting analysis 
 * @author Pdz
 *
 */
public class StatisticsGatherer implements IDocAnalyzer, ICorpusAnalyzer {

	/**
	 * "Rigid" data to gather (data types with single values)
	 */
	private static final List<AnalysisTypes> dataToGatherRigid = Arrays.asList(AnalysisTypes.documentID);
	
	/**
	 * "Flexible" data to gather (data types with multiple values, especially where the number of values is unknown at compile-time)
	 */
	private static final List<AnalysisTypes> dataToGatherFlex = Arrays.asList(AnalysisTypes.language, AnalysisTypes.category);
	
	/**
	 * List with additional types of "rigid" data
	 * <p> Cannot say for sure anymore, but probably list with "rigid" data that were discovered during the statistics gathering?
	 * I mean, that were not originally planned for. Maybe
	 */
	private List<AnalysisTypes> additionalRigidData;
	
	/**
	 * Internal data storage
	 */
	private Map<String, StatisticsBlock> data;
	
	/**
	 * Main list with "rigid" data types 
	 */
	private List<AnalysisTypes> rigidEntries;
	
	/**
	 * Storage with "flexible" data
	 */
	private ListMap<AnalysisTypes, String> flexibleEntries;
	
	/**
	 * Whether the {@link IAnalyzer} has been initialized successfully
	 */
	private boolean isInitialized;
	
	/**
	 * Constructor without parameters
	 */
	public StatisticsGatherer() {
		this.isInitialized = false;
		this.data = new HashMap<>();
		this.additionalRigidData = new ArrayList<>();
	}
	
	@Override
	public void initialize(IDataUnitCorpus data) {
		this.rigidEntries = new ArrayList<>();
		this.flexibleEntries = new ListMap<>();
		this.rigidEntries.addAll(dataToGatherRigid);
		this.isInitialized = true;
	}

	@Override
	public boolean isInitialized() {
		return this.isInitialized;
	}

	@Override
	public PairAnalysisResults feed(IDataUnitDoc input) {
		PairAnalysisResults result = new PairAnalysisResults();
		StatisticsBlock block = new StatisticsBlock();
		for (AnalysisTypes type : dataToGatherRigid) {
			List<IAnalysisResult> anRes = input.getAnalysisResults(type);
			if (anRes != null && !anRes.isEmpty()) {
				String data = ((MetadataModification)anRes.get(0)).getData().last().getData();
				block.setParams(type, data);
			}
		}
		for (AnalysisTypes type : dataToGatherFlex) {
			block.setParams(type, checkDataType(input, type));
		}
		if (block.hasData()) {
			this.data.put(block.getRigid(AnalysisTypes.documentID), block);
		}
		return result;
	}

	@Override
	public PairAnalysisResults feed(IDataUnitCorpus input) {
		for (AnalysisTypes type : dataToGatherFlex) {
			boolean multipleValsFound = false;
			for (StatisticsBlock block : data.values()) {
				if (block.getFlexible(type).size() > 1) {
					multipleValsFound = true;
					break;
				}
			}
			if (!multipleValsFound) {
				additionalRigidData.add(type);
			}
		}
		return new PairAnalysisResults();
	}

	/**
	 * Checks whether the provided document has data of specified type and adds these data to the storage with "flexible" data
	 * @param input Document to check
	 * @param type Data type to look for
	 * @return Resulting sorted set of weighted objects
	 */
	private TreeSet<WeightedObject> checkDataType(IDataUnitDoc input, AnalysisTypes type) {
		List<IAnalysisResult> anRes = input.getAnalysisResults(type);
		TreeSet<WeightedObject> data = new TreeSet<>();
		if (anRes != null && !anRes.isEmpty()) {
			data = ((MetadataModification)anRes.get(0)).getData();
			for (WeightedObject wo : data) {
				if (!this.flexibleEntries.get(type).contains(wo.getData())) {
					this.flexibleEntries.put(type, wo.getData());
				}
			}
		}
		return data;
	}

	/**
	 * Saves gathered statistics to the provided Excel-file
	 * @param path Excel file to save statistics to
	 * @return True, if data were saved successfully, otherwise false
	 */
	public boolean saveToFile(String path) {
		boolean Ok = true;
		try(FileOutputStream fileOut = new FileOutputStream(path);) {
			Workbook workbook = new HSSFWorkbook();
			Sheet sheet = workbook.createSheet();
			int indexRow = prepareHeaders(workbook, sheet);
			for (String documentID : this.data.keySet()) {
				Row row = sheet.createRow(indexRow);
				int count = 0;
				for (AnalysisTypes type : dataToGatherRigid) {
					Cell cell = row.createCell(count);
					cell.setCellValue(this.data.get(documentID).getRigid(type));
					count++;
				}
				for (AnalysisTypes type : additionalRigidData) {
					Optional<String> val = this.data.get(documentID).getFlexible(type).keySet().stream().findFirst();
					if (val.isPresent()) {
						Cell cell = row.createCell(count);
						cell.setCellValue(val.get());
					}
					count++;
				}
				for (AnalysisTypes type : dataToGatherFlex) {
					if (additionalRigidData.contains(type)) {
						continue;
					}
					Map<String, Double> flexible = this.data.get(documentID).getFlexible(type);
					for (int i = 0; i < flexibleEntries.get(type).size(); i++) {
						Double val = flexible.get(flexibleEntries.get(type).get(i));
						if (val != null && val != 0) {
							Cell cell = row.createCell(count + i);
							cell.setCellValue(val);
						}
					}
					count += flexibleEntries.get(type).size();
				}
				indexRow++;
			}
			workbook.write(fileOut);
			workbook.close();
			fileOut.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Ok = false;
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Ok = false;
			e.printStackTrace();
		}
		return Ok;
	}

	/**
	 * Helper-method, responsible for preparing a formatting for the Excel file
	 * @param wb Excel workbook
	 * @param sheet Excel sheet
	 * @return Next row to write data to
	 */
	private int prepareHeaders(Workbook wb, Sheet sheet) {
		Row header1 = sheet.createRow(0);
		Row header2 = null;
		int nextFreeRow = 1;
		if (!dataToGatherFlex.isEmpty()) {
			header2 = sheet.createRow(1);
			nextFreeRow = 2;
		}
		int count = 0;
		for (AnalysisTypes type : dataToGatherRigid) {
			Cell cell = header1.createCell(count);
			cell.setCellStyle(ExcelStyles.secondaryTitle.createStyle(wb));
			cell.setCellValue(type.getTagText());
			count++;
		}
		for (AnalysisTypes type : additionalRigidData) {
			Cell cell = header1.createCell(count);
			cell.setCellStyle(ExcelStyles.secondaryTitle.createStyle(wb));
			cell.setCellValue(type.getTagText());
			count++;
		}
		for (AnalysisTypes type : dataToGatherFlex) {
			if (additionalRigidData.contains(type)) {
				continue;
			}
			Cell cell1 = header1.createCell(dataToGatherRigid.size() + count);
			cell1.setCellStyle(ExcelStyles.secondaryTitle.createStyle(wb));
			cell1.setCellValue(type.getTagText());
			for (int i = 0; i < flexibleEntries.get(type).size(); i++) {
				Cell cell2 = header2.createCell(dataToGatherRigid.size() + count + i);
				cell2.setCellStyle(ExcelStyles.secondaryTitle.createStyle(wb));
				cell2.setCellValue(flexibleEntries.get(type).get(i));
			}
			count += flexibleEntries.get(type).size();
		}
		count = 0;
		for (int i = 0; i < dataToGatherRigid.size(); i++, count++) {
			sheet.addMergedRegion(new CellRangeAddress(header1.getRowNum(), nextFreeRow - 1, count, count));
		}
		for (int i = 0; i < additionalRigidData.size(); i++, count++) {
			sheet.addMergedRegion(new CellRangeAddress(header1.getRowNum(), nextFreeRow - 1, count, count));
		}
		for (int i = 0; i < dataToGatherFlex.size(); i++) {
			if (additionalRigidData.contains(dataToGatherFlex.get(i))) {
				continue;
			}			
			int joinSize = flexibleEntries.get(dataToGatherFlex.get(i)).size();
			if (joinSize > 1) {
				sheet.addMergedRegion(new CellRangeAddress(header1.getRowNum(), header1.getRowNum(), count, count + joinSize - 1));
			}
			count += joinSize;
		}
		return nextFreeRow;
	}
}
