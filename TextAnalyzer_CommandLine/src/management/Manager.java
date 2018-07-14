package management;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import analyzers.CategoriesCalculator;
import analyzers.LanguageAnalyzer;
import analyzers.MetadataAnalyzer;
import analyzers.TDMatrixPreparator;
import analyzers.TfIdfCalculator;
import analyzers.TfIdfPreparator;
import categories.ICategory;
import clustering.KMeans;
import crawlers.CrawlerBase;
import crawlers.ICrawler;
import dataUnits.CorpusImpl;
import dataUnits.IDataUnitCorpus;
import filters.WeightsFilterPercentage;
import functions.IDF_LogSmooth;
import functions.IWeightsToDistancesConverter;
import functions.LSAAlgorithm;
import functions.MatrixFilterLSAFixedSize;
import functions.TF_Count;
import io.FileExtensions;
import io.IReadable;
import io.IReader;
import io.ReaderXML;
import io.WriterXML;
import utils.ConfigurationData;

public class Manager {

	private static final String separatorExt = ".";
	
	private IDataUnitCorpus data;
	private List<ICrawler> crawlers;
	private ConfigurationData config;
	private List<ICategory> categories;
	
	public static void main(String[] args) {
		Manager manager = new Manager();
		manager.init();
//		if (args != null && args.length != 0) {
//			manager.loadCorpusData(args[0]);
//		}
		manager.setupDataPreparation(args);
		manager.prepareData();
		manager.saveData();
	}

	private void init() {
		data = new CorpusImpl();
		crawlers = new ArrayList<>();
		loadConfigData();
		loadCategories();
		loadCorpusData(this.config.getPathData());
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
	}

	private void loadCategories() {
		ReaderXML reader = new ReaderXML();
		Path pathData = Paths.get(config.getPathCategories());
		if (Files.exists(pathData) && Files.isReadable(pathData)
								   && !Files.isDirectory(pathData)
								   && reader.readFromFile(config.getPathCategories())) {
			for (IReadable obj : reader.getData()) {
				if (obj instanceof ICategory) {
					this.categories.add((ICategory) obj);
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
		crawler.addAnalyzer(new LanguageAnalyzer(false), 0);
		crawler.addAnalyzer(new TfIdfPreparator(true, new TF_Count(), new IDF_LogSmooth()), 0);
		crawler.addAnalyzer(new TfIdfCalculator(true, new WeightsFilterPercentage(0.3)), 1);
		crawler.addAnalyzer(new TDMatrixPreparator(true), 2);
		IWeightsToDistancesConverter converter = new LSAAlgorithm();
//		converter.setMatrixFilter(new MatrixFilterLSAFixedSize(50));
		crawler.addAnalyzer(new CategoriesCalculator(false, converter, new KMeans()), 3);
		this.crawlers.add(crawler);
	}

	private void prepareData() {
		for (ICrawler crawler : crawlers) {
			data = crawler.crawl(data);
		}
//		IWeightsToDistancesConverter alg = new LSAAlgorithm(new FixedSizeMatrixCompressor(10));
//		alg.initializeData(data);
//		alg.reduceDimensionality();
//		int zzz = 0;
//		zzz++;
	}

	private void saveData() {
		WriterXML writer = new WriterXML();
		boolean Ok = writer.writeToFile(ConfigurationData.getConfigPath(), this.config, true);
		Ok = writer.writeToFile(this.config.generateCorpusName(), data, false);
	}
}
