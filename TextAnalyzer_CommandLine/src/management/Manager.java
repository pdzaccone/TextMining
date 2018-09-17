package management;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import analysis.IAnalysisResult;
import analysis.ICategory;
import analyzers.AnalysisTypes;
import analyzers.CategoriesCalculator;
import analyzers.LanguageAnalyzer;
import analyzers.LanguageAnalyzerDefault;
import analyzers.MetadataAnalyzer;
import analyzers.StatisticsGatherer;
import analyzers.TDMatrixPreparator;
import analyzers.TfIdfCalculator;
import analyzers.TfIdfPreparator;
import clustering.KMeansCosineBased;
import crawlers.CrawlerBase;
import crawlers.ICrawler;
import dataUnits.CorpusImpl;
import dataUnits.IDataUnitCorpus;
import functions.IDF_LogSmooth;
import functions.IWeightsToDistancesConverter;
import functions.LSAAlgorithm;
import functions.MatrixFilterLSAFixedSize;
import functions.TF_CountAdjusted;
import io.FileExtensions;
import io.IReadable;
import io.IReader;
import io.ISaveableXML;
import io.ReaderXML;
import io.WriterXML;
import utils.ConfigurationData;

/**
 * This class is responsible for overall operation control - it manages IO operations and data analysis 
 * @author Pdz
 *
 */
public class Manager {

	/**
	 * String constant for separating file extension
	 */
	private static final String separatorExt = ".";
	private static final int NUMBER_KEYWORDS_CATEGORY = 10;
	
	/**
	 * Document corpus
	 */
	private IDataUnitCorpus data;
	/**
	 * List of crawlers in use
	 */
	private List<ICrawler> crawlers;
	/**
	 * Configuration data
	 */
	private ConfigurationData config;
	/**
	 * Known (loaded) categories
	 */
	private Map<String, ICategory> categories;
	/**
	 * Statistics manager
	 */
	private StatisticsGatherer statisticsGatherer;
	
	public static void main(String[] args) {
		Manager manager = new Manager();
		manager.init();
		if (args != null && args.length != 0) {
			manager.loadCorpusData(args[0]);
		}
		manager.setupDataPreparation(args);
		manager.prepareData();
		manager.saveData();
		manager.processStatistics();
		manager.updateCategories();
		manager.saveCategories();
	}

	/**
	 * Default constructor
	 */
	public Manager() {
		this.crawlers = new ArrayList<>();
		this.categories = new HashMap<>();
	}
	
	/**
	 * Initialization method.
	 * Responsible for loading configuration data, categories and also previously analyzed document corpus, if need be 
	 */
	private void init() {
		data = new CorpusImpl();
		crawlers = new ArrayList<>();
		loadConfigData();
		loadCategories();
//		loadCorpusData(this.config.getPathData());
	}

	/**
	 * This method is responsible for loading configuration data
	 */
	private void loadConfigData() {
		ReaderXML reader = new ReaderXML();
		if (reader.readFromFile(ConfigurationData.getConfigPath())) {
			if (reader.getData() == null || reader.getData().size() != 1) {
				this.config = new ConfigurationData();
			} else {
				this.config = (ConfigurationData) reader.getData().get(0);
			}
		} else {
			this.config = new ConfigurationData();
		}
		try {
			this.config.checkAndCreateFolders();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This method is responsible for loading categories data from an XML file
	 */
	private void loadCategories() {
		ReaderXML reader = new ReaderXML();
		Path pathData = Paths.get(config.getPathCategories());
		if (Files.exists(pathData) && Files.isReadable(pathData)
								   && !Files.isDirectory(pathData)
								   && reader.readFromFile(config.getPathCategories())) {
			for (IReadable obj : reader.getData()) {
				if (obj instanceof ICategory) {
					this.categories.put(((ICategory) obj).getName(), (ICategory)obj);
				}
			}
		}
	}

	/**
	 * This method manages loading document corpus from the file / files. Checks if the provided path is valid, etc.
	 * @param path Path to individual file or to a folder with several files
	 */
	private void loadCorpusData(String path) {
		final IDataUnitCorpus result = new CorpusImpl();
		boolean Ok = true;
		Path pathData = Paths.get(path);
		if (Files.exists(pathData) && Files.isReadable(pathData)) {
			if (!Files.isDirectory(pathData)) {
				result.addCorpus(readCorpusFromFile(pathData));
			} else {
				try (Stream<Path> paths = Files.walk(pathData)) {
				    paths.filter(f -> !Files.isDirectory(f))
				         .forEach(f -> {
							result.addCorpus(readCorpusFromFile(f));
				         });
				} catch (IOException e) {
					Ok = false;
				} 
			}
		}
		if (Ok) {
			this.data.addCorpus(result);
		}
	}
	
	/**
	 * This method reads document corpus from the file, assuming that the provided path is valid
	 * @param path Valid path to a single file with document corpus data
	 * @return Resulting IDataUnitCorpus object
	 */
	private IDataUnitCorpus readCorpusFromFile(Path path) {
		IReader reader = getFileReader(path);
		IDataUnitCorpus results = new CorpusImpl();
		if (reader != null && reader.readFromFile(path.toString())) {
			for (IReadable obj : reader.getData()) {
				if (obj instanceof IDataUnitCorpus) {
					results.addCorpus((IDataUnitCorpus) obj);
				}
			}
		}
		return results;
	}
	
	/**
	 * This method creates an IReader-object, capable of reading provided file
	 * @param path Valid file to read data from
	 * @return Resulting IReader or null if the provided file has unsupported format
	 */
	private IReader getFileReader(Path path) {
		File file = new File(path.toString());
		String extString = file.getName().substring(file.getName().lastIndexOf(separatorExt) + 1);
		return FileExtensions.fromString(extString).createReader();
	}

	/**
	 * This method initializes Crawler with various Analyzer-objects. Also responsible for creation and setup of these objects
	 * @param args These arguments are not being used at the moment
	 */
	private void setupDataPreparation(String[] args) {
		CrawlerBase crawler = new CrawlerBase();
		crawler.addAnalyzer(new MetadataAnalyzer(false), 0);
//		crawler.addAnalyzer(new LanguageAnalyzerDefault(false), 0);
		crawler.addAnalyzer(new LanguageAnalyzer(false), 0);
		crawler.addAnalyzer(new TfIdfPreparator(true, new TF_CountAdjusted(), new IDF_LogSmooth(), false), 0);
//		crawler.addAnalyzer(new TfIdfPreparator(true, new TF_Count(), new IDF_LogSmooth(), false), 0);
//		crawler.addAnalyzer(new TfIdfCalculator(true, new WeightsFilterPercentage(0.3)), 1);
		crawler.addAnalyzer(new TfIdfCalculator(true, null), 1);
		crawler.addAnalyzer(new TDMatrixPreparator(true), 2);
		IWeightsToDistancesConverter converter = new LSAAlgorithm();
		converter.setMatrixFilter(new MatrixFilterLSAFixedSize(50));
//		crawler.addAnalyzer(new CategoriesCalculator(false, converter, new KMeansBase(false)), 3);
		crawler.addAnalyzer(new CategoriesCalculator(this.categories, false, converter, new KMeansCosineBased(false), NUMBER_KEYWORDS_CATEGORY), 3);
//		CategoriesSetter catSetter = new CategoriesSetter(false);
//		catSetter.readPredefinedCategories(fileCategories);
//		crawler.addAnalyzer(catSetter, 3);
		this.statisticsGatherer = new StatisticsGatherer();
		crawler.addAnalyzer(statisticsGatherer, 4);
		this.crawlers.add(crawler);
	}

	/**
	 * This method updates the original categories with the analysis results (these data are taken from the document corpus)
	 */
	private void updateCategories() {
		List<IAnalysisResult> analysis = this.data.getAnalysisResults(AnalysisTypes.categoryWords);
		if (analysis != null && !analysis.isEmpty()) {
			for (IAnalysisResult anRes : analysis) {
				if (anRes instanceof ICategory) {
					ICategory category = (ICategory) anRes;
					if (this.categories.containsKey(category.getName())) {
						this.categories.get(category.getName()).update(category);
					} else {
						this.categories.put(category.getName(), category);
					}
				}
			}
		}
	}

	/**
	 * This method saves produced categories to file
	 * @return True if categories were saved successfully, otherwise false
	 */
	private boolean saveCategories() {
		boolean Ok = true;
		if (this.categories != null && !this.categories.isEmpty()) {
			WriterXML writer = new WriterXML();
			List<ISaveableXML> coll = new ArrayList<>();
			for (ICategory cat : this.categories.values()) {
				coll.add(cat);
			}
			Ok = writer.writeToFile(this.config.generateNameCategories(), coll, ICategory.XMLTags.categories.getTagText(), false);
		}
		return Ok;
	}

	/**
	 * This method generates statistics and saves resulting data to the corresponding file
	 */
	private void processStatistics() {
		if (statisticsGatherer != null) {
			statisticsGatherer.saveToFile(this.config.generateNameStatistics());
		}
	}

	/**
	 * This method starts crawler with all its data analyzers
	 */
	private void prepareData() {
		for (ICrawler crawler : crawlers) {
			data = crawler.crawl(data);
		}
	}

	/**
	 * This method saves document corpus to file
	 * @return True, if data were saved successfully, otherwise - false
	 */
	private boolean saveData() {
		WriterXML writer = new WriterXML();
		boolean Ok = writer.writeToFile(ConfigurationData.getConfigPath(), this.config, true);
		if (Ok) {
			Ok = writer.writeToFile(this.config.generateNameCorpus(), data, false);
		}
		return Ok;
	}
}
