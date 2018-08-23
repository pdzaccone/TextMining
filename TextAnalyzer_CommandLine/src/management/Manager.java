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

public class Manager {

	private static final String separatorExt = ".";
	private static final int NUMBER_KEYWORDS_CATEGORY = 10;
	
	private IDataUnitCorpus data;
	private List<ICrawler> crawlers;
	private ConfigurationData config;
	private Map<String, ICategory> categories;
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

	public Manager() {
		this.crawlers = new ArrayList<>();
		this.categories = new HashMap<>();
	}
	
	private void init() {
		data = new CorpusImpl();
		crawlers = new ArrayList<>();
		loadConfigData();
		loadCategories();
//		loadCorpusData(this.config.getPathData());
	}

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
	
	private IReader getFileReader(Path path) {
		File file = new File(path.toString());
		String extString = file.getName().substring(file.getName().lastIndexOf(separatorExt) + 1);
		return FileExtensions.fromString(extString).createReader();
	}

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

	private void processStatistics() {
		if (statisticsGatherer != null) {
			statisticsGatherer.saveToFile(this.config.generateNameStatistics());
		}
	}

	private void prepareData() {
		for (ICrawler crawler : crawlers) {
			data = crawler.crawl(data);
		}
	}

	private boolean saveData() {
		WriterXML writer = new WriterXML();
		boolean Ok = writer.writeToFile(ConfigurationData.getConfigPath(), this.config, true);
		if (Ok) {
			Ok = writer.writeToFile(this.config.generateNameCorpus(), data, false);
		}
		return Ok;
	}
}
