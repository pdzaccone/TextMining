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

/**
 * Base implementation of K-Means clustering algorithm
 * @author Pdz
 *
 */
public class KMeansBase implements IClusterer, IMultilingual {

	/**
	 * String constant for identification of a temporary "unknown" cluster
	 */
	protected static final String CATEGORY_UNKNOWN = "CU";

	/**
	 * String constant used for naming a new cluster
	 */
	private static final String PREFIX_CATEGORY = "categoryNew";
	
	/**
	 * This empirical coefficient more or less defines size of clusters
	 */
	private static final double THRESHOLD = 0.01;
	
	/**
	 * Whether to keep the "unknown" category
	 */
	protected boolean keepUnknown;
	
	/**
	 * This function defines how the distance between two vectors is calculated
	 */
	protected IDistanceMetrics distMetrics;
	
	/**
	 * Internal storage with new categories
	 */
	protected ListMap<Languages, String> categoriesNew;
	
	/**
	 * All clusters 
	 */
	private Map<Languages, Map<String, ICluster>> clusters;
	
	/**
	 * Clustering results (weighted terms for each document)
	 */
	private Map<Integer, WeightedMap> clusteringResults;
	
	/**
	 * Constructor with parameter
	 * @param keepUnknownCategory Whether to keep the "unknown" category
	 */
	public KMeansBase(boolean keepUnknownCategory) {
		this.keepUnknown = keepUnknownCategory;
		this.categoriesNew = new ListMap<>();
		this.clusters = new HashMap<>();
		this.clusteringResults = new HashMap<>();
		this.initSpecifics();
	}
	
	/**
	 * This method defines how algorithm calculates distances
	 */
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

	/**
	 * Imports already existing categories by creating empty clusters, defining their centers, etc.
	 * @param lang Language
	 * @param inCategories Categories to import
	 * @return Resulting cluster map
	 */
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

	/**
	 * Removes the "unknown" category and its cluster from internal data storages
	 */
	private void updateCategories() {
		if (!keepUnknown) {
			for (Languages lang : this.categoriesNew.keySet()) {
				this.categoriesNew.remove(lang, CATEGORY_UNKNOWN);
			}
		}
	}

	/**
	 * Calculates new centroids (clusters)
	 * @param input Existing clusters
	 * @return Set of central vectors, defining new clusters
	 * @throws Exception
	 */
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

	/**
	 * Distributes vectors between clusters
	 * @param input Vectors
	 * @param lang Language
	 * @return Updated cluster set
	 * @throws Exception
	 */
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

	/**
	 * Finds new / existing cluster for the given terms vector
	 * @param input Set with all clusters
	 * @param vector Vector to "sort"
	 * @return Resulting pair of cluster / distance, cluster name = "" -> new cluster should be created
	 * @throws Exception
	 */
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

	/**
	 * Generates new category (cluster)
	 * @param lang Language
	 * @return Category name
	 */
	protected String generateNewCategory(Languages lang) {
		int maxExistingSuffix = searchForMaxCategory(PREFIX_CATEGORY);
		return PREFIX_CATEGORY + (maxExistingSuffix + 1);
	}

	/**
	 * Helper method for generation of new category name
	 * @param prefix Prefix to use
	 * @return Resulting category index number
	 */
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
	
	/**
	 * Calculates error as squared difference between cluster centers
	 * @param oldData Old cluster centers
	 * @param newData New cluster centers
	 * @return Resulting pair new clusters have been added / removed - error. 1st value is true - no changes in clusters themselves
	 * @throws Exception
	 */
	private Pair<Boolean, Double> calculateDifference(Map<String, ITermsVector> oldData, Map<String, ITermsVector> newData) throws Exception {
		Pair<Boolean, Double> error = new Pair<>(true, (double) 0);
		if (!oldData.keySet().containsAll(newData.keySet()) || !newData.keySet().containsAll(oldData.keySet())) {
			error.update(false, Double.MAX_VALUE);
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
			// TODO: handle exception
		}
		return null;
	}

	@Override
	public Set<Languages> getLanguages() {
		return categoriesNew.isEmpty() ? new HashSet<>() : categoriesNew.keySet();
	}
}
