package analyzers;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;

import analysis.CategoryImpl;
import analysis.IAnalysisResult;
import analysis.ICategory;
import analysis.MetadataModification;
import analysis.WeightsTable;
import analysis.WordsMatrix;
import clustering.IClusterer;
import dataUnits.IDataUnitCorpus;
import dataUnits.IDataUnitDoc;
import functions.IWeightsToDistancesConverter;
import linearAlgebra.ITermsVector;
import utils.Languages;
import utils.Pair;
import utils.PairAnalysisResults;
import utils.WeightedMap;
import utils.WeightedObject;

/**
 * This is the main {@link IAnalyzer} that is designed to identify categories. It can work both with and without the existing categories data
 * @author Pdz
 *
 */
public class CategoriesCalculator implements ICategorizer {

	/**
	 * Default term weight value when integrating keywords from previously "calculated" categories
	 */
	private static final double defaultKeywordWeight = 5;
	
	/**
	 * Internal weights-to-distance converter, used for data preprocessing inside this analyzer
	 */
	private final IWeightsToDistancesConverter converter;
	
	/**
	 * Clustering algorithm, used during the clustering stage
	 */
	private final IClusterer clusterer;
	
	/**
	 * Whether the {@link IAnalyzer} has been initialized successfully
	 */
	private boolean isInitialized;
	
	/**
	 * Whether this {@link IAnalyzer} should overwrite already existing results from previous analysis if they exist
	 */
	private final boolean shouldOverwrite;
	
	/**
	 * This value is used during the generation of categories to decide the number of keywords to be kept and saved
	 */
	private int numberOfImportantKeywords;
	
	/**
	 * Internal categories data
	 */
	private Map<String, Map<Languages, WeightedMap>> categoriesData;
	
	/**
	 * Original (previously calculated or user-provided) categories
	 */
	private Map<String, ICategory> categoriesOriginal;
	
	/**
	 * Constructor with parameters
	 * @param categories Original categories
	 * @param overwrite Whether to overwrite old categories data
	 * @param inConverter Weights-to-distance converter to use
	 * @param inClusterer Clustering algorithm to use
	 * @param keywordsNum Number of keywords to keep when generating a new category
	 */
	public CategoriesCalculator(Map<String, ICategory> categories, boolean overwrite, IWeightsToDistancesConverter inConverter, 
			IClusterer inClusterer, int keywordsNum) {
		this.shouldOverwrite = overwrite;
		this.converter = inConverter;
		this.clusterer = inClusterer;
		this.isInitialized = false;
		this.numberOfImportantKeywords = keywordsNum;
		this.categoriesData = new HashMap<>();
		this.categoriesOriginal = new HashMap<>(categories);
	}
	
	@Override
	public void initialize(IDataUnitCorpus input) {
		Objects.requireNonNull(input);
		boolean Ok = true;
		converter.initializeData(input);
		try {
			converter.prepareData();
		} catch (Exception e) {
			Ok = false;
		}
		if (Ok) {
			List<IAnalysisResult> anRes = input.getAnalysisResults(AnalysisTypes.weightMatrix);
			if (anRes != null && anRes.size() == 1) {
				for (ICategory cat : this.categoriesOriginal.values()) {
					for (Languages lang : cat.getLanguages()) {
						ITermsVector vector = ((WordsMatrix)anRes.get(0)).createVector(lang, cat.getKeywords(lang), defaultKeywordWeight);
						if (vector != null) {
							cat.setVector(lang, vector);
						}
					}
				}
			}
			clusterer.initialize(converter.getData(), this.categoriesOriginal);
			try {
				clusterer.doClustering();
			} catch (Exception e) {
				Ok = false;
			}
		}
		this.isInitialized = Ok;
	}

	@Override
	public boolean isInitialized() {
		return this.isInitialized;
	}

	@Override
	public PairAnalysisResults feed(IDataUnitDoc input) {
		PairAnalysisResults results = new PairAnalysisResults();
		Languages lang = input.getMainLanguage();
		IAnalysisResult categories = clusterer.getCategories(input);
		List<IAnalysisResult> analysisWeights = input.getAnalysisResults(AnalysisTypes.weights);
		if (analysisWeights != null && analysisWeights.size() == 1) {
			Map<String, ? extends Number> weights = ((WeightsTable)analysisWeights.get(0)).getWeights(lang);
			processCategoriesData(weights, (MetadataModification) categories, lang);
		}
		results.addResult(new Pair<>(categories, shouldOverwrite), IAnalyzer.LOCAL);
		results.addResult(new Pair<>(categories, shouldOverwrite), IAnalyzer.SEND_UP);
		return results;
	}

	/**
	 * Processes gathered categories data in order to prepare a list of most important keywords for a given document (for a specific language) 
	 * @param weights Weighted terms of a document
	 * @param categories Categories of a document
	 * @param language Language
	 */
	private void processCategoriesData (Map<String, ? extends Number> weights, MetadataModification categories, Languages language) {
		Comparator<Pair<String, Double>> comparator = (Pair<String, Double> p1, Pair<String, Double> p2) -> (Double.compare(p2.getSecond(), p1.getSecond()));
		TreeSet<Pair<String, Double>> keywords = new TreeSet<>(comparator);
		for (String s : weights.keySet()) {
			keywords.add(new Pair<String, Double>(s, (Double) weights.get(s)));
		}
		for (WeightedObject wo : categories.getData()) {
			Map<Languages, WeightedMap> wml = categoriesData.get(wo.getData());
			if (wml == null) {
				wml = new HashMap<>();
			}
			WeightedMap wm = wml.get(language);
			if (wm == null) {
				wm = new WeightedMap();
			}
			int count = 0;
			for (Pair<String, Double> pair : keywords) {
				wm.add(pair.getFirst(), 1);
				count++;
				if (count > numberOfImportantKeywords) {
					break;
				}
			}
			wml.put(language, wm);
			categoriesData.put(wo.getData(), wml);
		}
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
		for (String category : this.categoriesData.keySet()) {
			CategoryImpl cat = new CategoryImpl(category);
			for (Languages lang : this.categoriesData.get(category).keySet()) {
				int count = 0;
				for (WeightedObject wo : this.categoriesData.get(category).get(lang).getWeights().descendingSet()) {
					cat.addKeyword(lang, wo.getData());
					count++;
					if (count > numberOfImportantKeywords) {
						break;
					}
				}
			}
			results.addResult(new Pair<>(cat, true), IAnalyzer.LOCAL);
		}
		return results;
	}
}