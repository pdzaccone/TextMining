package clustering;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;

import analysis.IAnalysisResult;
import analysis.ICategory;
import analysis.IMultilingual;
import analysis.MetadataModification;
import analysis.WordsMatrix;
import analyzers.AnalysisTypes;
import dataUnits.IDataUnitDoc;
import linearAlgebra.DistanceEuclidean;
import linearAlgebra.IDistanceMetrics;
import linearAlgebra.ITermsVector;
import utils.Languages;
import utils.ListMap;
import utils.Pair;
import utils.WeightedMap;

public class KMeansBase implements IClusterer, IMultilingual {

	protected static final String CATEGORY_UNKNOWN = "CU";
	private static final String PREFIX_CATEGORY = "categoryNew";
	private static final double THRESHOLD = 0.01;
	
	protected boolean keepUnknown;
	protected IDistanceMetrics distMetrics;
	
	protected ListMap<Languages, String> categoriesNew;
	private Map<Languages, Map<String, ICluster>> clusters;
	private Map<Integer, WeightedMap> clusteringResults;
	
	public KMeansBase(boolean keepUnknownCategory) {
		this.keepUnknown = keepUnknownCategory;
		this.categoriesNew = new ListMap<>();
		this.clusters = new HashMap<>();
		this.clusteringResults = new HashMap<>();
		this.initSpecifics();
	}
	
	protected void initSpecifics() {
		this.distMetrics = new DistanceEuclidean();
	}

	/**
	 * This method initializes clusters + a cluster with unsorted data. 
	 * The following iteration should first sort the uncategorized elements into clusters 
	 */
	@Override
	public void initialize(WordsMatrix data, Map<String, ICategory> inCategories) {
		for (Languages lang : data.getLanguages()) {
			Map<String, ICluster> clustersMap = importExistingCategories(lang, inCategories);
			data.getDocuments(lang).stream().forEach(doc -> {
				String cat = CATEGORY_UNKNOWN;
				List<String> categoriesDoc = doc.getCategoriesMap();
				if (!categoriesDoc.isEmpty()) {
					cat = categoriesDoc.get(0);
					categoriesNew.put(lang, categoriesDoc);
				} else {
					categoriesNew.put(lang, cat);
				}
				ICluster currCluster = clustersMap.get(cat);
				if (currCluster == null) {
					currCluster = new ClusterBase(this.distMetrics, cat);
				}
				ITermsVector vector = data.getDataMatrix(lang).getColumnData(data.getIndex(lang, doc));
				currCluster.addVector(vector);
				clustersMap.put(cat, currCluster);
			});
			this.clusters.put(lang, clustersMap);
		}
		categoriesNew.removeDuplicates();
	}

	private Map<String, ICluster> importExistingCategories(Languages lang, Map<String, ICategory> inCategories) {
		Map<String, ICluster> results = new HashMap<>();
		for (ICategory cat : inCategories.values()) {
			if (cat.getLanguages().contains(lang) && cat.getVector(lang) != null) {
				ICluster cluster = new ClusterBase(this.distMetrics, cat.getName());
				cluster.setCentralVector(cat.getVector(lang));
				results.put(cat.getName(), cluster);
				categoriesNew.put(lang, cat.getName());
			}
		}
		return results;
	}

	@Override
	public void doClustering() throws Exception {
		for (Languages lang : this.clusters.keySet()) {
			Map<String, ICluster> clustersL = this.clusters.get(lang);
			Map<String, ITermsVector> oldCentralVectors = calculateNewCentroids(clustersL);
			boolean repeat = true;
			double errorOld = Double.MAX_VALUE;
			while (repeat) {
				clustersL = reassignVectorsAmongClusters(clustersL, lang);
				Map<String, ITermsVector> newCentralVectors = calculateNewCentroids(clustersL);
				Pair<Boolean, Double> error = calculateDifference(oldCentralVectors, newCentralVectors);
				if (error.getFirst() && Math.abs(errorOld - error.getSecond()) <= THRESHOLD) {
					break;
				}
				errorOld = error.getSecond();
				oldCentralVectors = new HashMap<>(newCentralVectors);
			}
			this.clusters.put(lang, clustersL);
			for (String cat : clustersL.keySet()) {
				for (ITermsVector vector : clustersL.get(cat)) {
					WeightedMap wm = this.clusteringResults.get(vector.getDocID());
					if (wm == null) {
						wm = new WeightedMap();
					}
					wm.add(cat, clustersL.get(cat).getVectorWeight(vector));
					this.clusteringResults.put(vector.getDocID(), wm);
				}
			}
		}
		updateCategories();
	}

	private void updateCategories() {
		if (!keepUnknown) {
			for (Languages lang : this.categoriesNew.keySet()) {
				this.categoriesNew.remove(lang, CATEGORY_UNKNOWN);
			}
		}
	}

	private Map<String, ITermsVector> calculateNewCentroids(Map<String, ICluster> input) throws Exception {
		Map<String, ITermsVector> vectors = new HashMap<>();
		for (String cat : input.keySet()) {
			if (!keepUnknown && CATEGORY_UNKNOWN.equalsIgnoreCase(cat)) {
				continue;
			}
			input.get(cat).calculateCentralVector();
			vectors.put(cat, input.get(cat).getCentralVector());
		}
		return vectors;
	}

	protected Map<String, ICluster> reassignVectorsAmongClusters(Map<String, ICluster> input, Languages lang) throws Exception {
		Map<String, ICluster> results = new HashMap<>();
		Map<String, ICluster> newClusters = new HashMap<>();
		for (String cat : input.keySet()) {
			for (ITermsVector vector : input.get(cat)) {
				boolean addingNewCategory = false;
				Pair<String, Double> clusterData = findNewCluster(input, vector);
				if (clusterData.getFirst().isEmpty()) {
					clusterData = findNewCluster(newClusters, vector);
					if (clusterData.getFirst().isEmpty()) {
						clusterData.update(generateNewCategory(lang), clusterData.getSecond());
						addingNewCategory = true;
					}
				}
				ICluster iCluster = results.get(clusterData.getFirst());
				if (iCluster == null) {
					iCluster = newClusters.get(clusterData.getFirst());
					if (iCluster == null) {
						iCluster = new ClusterBase(this.distMetrics, clusterData.getFirst());
						if (!addingNewCategory) {
							iCluster.setCentralVector(input.get(clusterData.getFirst()).getCentralVector());
						}
					}
				}
				iCluster.addVector(vector);
				if (addingNewCategory) {
					this.categoriesNew.put(lang, clusterData.getFirst());
					iCluster.calculateCentralVector();
					newClusters.put(clusterData.getFirst(), iCluster);
				} else {
					results.put(clusterData.getFirst(), iCluster);
				}
			}
		}
		results.putAll(newClusters);
		return results;
	}

	protected Pair<String, Double> findNewCluster(Map<String, ICluster> input, ITermsVector vector) throws Exception {
		Pair<String, Double> result = new Pair<String, Double>("", Double.MAX_VALUE);
		for (String catInner : input.keySet()) {
			if (!keepUnknown && CATEGORY_UNKNOWN.equalsIgnoreCase(catInner)) {
				continue;
			}
			double dist = input.get(catInner).calculateDistance(vector);
			if (dist < result.getSecond()) {
				result.update(catInner, dist);
			}
		}
		return result;
	}

	protected String generateNewCategory(Languages lang) {
		int maxExistingSuffix = searchForMaxCategory(PREFIX_CATEGORY);
		return PREFIX_CATEGORY + (maxExistingSuffix + 1);
	}

	private int searchForMaxCategory(String prefix) {
		OptionalInt maxSuffix = this.categoriesNew.values().stream()
				.flatMap(val -> val.stream()).filter(val -> val.startsWith(prefix)).mapToInt(val -> {
					String s = val.substring(prefix.length());
					int index;
					try {
						index = Integer.parseInt(s);
					} catch (NumberFormatException e) {
						return -1;
					}
					return index;
				}).max();
		return maxSuffix.isPresent() ? maxSuffix.getAsInt() : -1;
	}
	
	private Pair<Boolean, Double> calculateDifference(Map<String, ITermsVector> oldData, Map<String, ITermsVector> newData) throws Exception {
		Pair<Boolean, Double> error = new Pair<>(true, (double) 0);
		if (!oldData.keySet().containsAll(newData.keySet()) || !newData.keySet().containsAll(oldData.keySet())) {
			error.update(false,  Double.MAX_VALUE);
		} else {
			for (String category : oldData.keySet()) {
				error.update(error.getFirst(), error.getSecond() + 
						Math.pow(distMetrics.calculateDistance(oldData.get(category), newData.get(category)), 2));
			}
		}
		return error;
	}

	@Override
	public IAnalysisResult getCategories(IDataUnitDoc input) {
		try {
			if (!this.clusteringResults.get(input.getID()).isEmpty()) {
				MetadataModification anRes = new MetadataModification(AnalysisTypes.category, this.clusteringResults.get(input.getID()).getWeights());
				return anRes;
			}
		} catch (Exception e) {
			int zzz = 0;
			zzz++;
		}
		return null;
	}

	@Override
	public Set<Languages> getLanguages() {
		return categoriesNew.isEmpty() ? new HashSet<>() : categoriesNew.keySet();
	}
}
