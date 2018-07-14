package clustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import analysis.IAnalysisResult;
import analysis.IMultilingual;
import analysis.WordsMatrix;
import dataUnits.IDataUnitDoc;
import linearAlgebra.ITermsVector;
import utils.Languages;

public class KMeans implements IClusterer, IMultilingual {

	private static final String CATEGORY_UNKNOWN = "CU";
	private static final String BAD_DATA = "BD";
	private static final double THRESHOLD = 0.01;
	
	private Map<Languages, List<String>> categories;
	private Map<Languages, Map<String, ICluster>> clusters;
//	private Map<Languages, List<ITermsVector>> data;
	
	public KMeans() {
//		this.data = new HashMap<>();
		this.categories = new HashMap<>();
		this.clusters = new HashMap<>();
	}
	
	/**
	 * This method initializes clusters + a cluster with unsorted data. 
	 * The following iteration should first sort the uncategorized elements into clusters 
	 */
	@Override
	public void initialize(WordsMatrix data) {
		for (Languages lang : data.getLanguages()) {
			Map<String, ICluster> clustersMap = new HashMap<>();
			data.getDocuments(lang).stream().forEach(doc -> {
				String cat = CATEGORY_UNKNOWN;
				List<String> categoriesDoc = doc.getCategoriesMap();
				if (!categoriesDoc.isEmpty()) {
					cat = categoriesDoc.get(0);
				}
				ICluster currCluster = clustersMap.get(cat);
				if (currCluster == null) {
					currCluster = new ClusterBase(cat);
				}
				ITermsVector vector = data.getDataMatrix(lang).getColumnData(data.getIndex(lang, doc));
				currCluster.addVector(vector);
				clustersMap.put(cat, currCluster);
			});
			this.clusters.put(lang, clustersMap);
		}
	}

	@Override
	public void doClustering() {
		for (Languages lang : this.clusters.keySet()) {
			List<ITermsVector> oldCentralVectors = calculateNewCentroids(this.clusters.get(lang));
			boolean repeat = true, firstTime = true;
			while (repeat) {
				reassignVectorsAmongClusters(this.clusters.get(lang), firstTime);
				if (firstTime) {
					firstTime = false;
				}
				List<ITermsVector> newCentralVectors = calculateNewCentroids(this.clusters.get(lang));
				repeat = checkConvergence(oldCentralVectors, newCentralVectors);
			}
		}
	}

	private List<ITermsVector> calculateNewCentroids(Map<String, ICluster> input) {
		List<ITermsVector> vectors = new ArrayList<>();
		for (String cat : input.keySet()) {
			if (!CATEGORY_UNKNOWN.equalsIgnoreCase(cat)) {
				input.get(cat).calculateCentralVector();
				vectors.add(input.get(cat).getCentralVector());
			}
		}
		return vectors;
	}

	private void reassignVectorsAmongClusters(Map<String, ICluster> input, boolean firstTime) {
		Map<String, ICluster> results = new HashMap<>();
		for (String cat : input.keySet()) {
			if (firstTime && !CATEGORY_UNKNOWN.equalsIgnoreCase(cat)) {
				continue;
			}
			for (ITermsVector vector : input.get(cat)) {
				String minCat = BAD_DATA;
				double minVal = Double.MAX_VALUE;
				for (String catInner : input.keySet()) {
					double dist = input.get(catInner).cosine(vector);
					if (dist < minVal) {
						minVal = dist;
						minCat = catInner;
					}
				}
				ICluster iCluster = results.get(minCat);
				if (iCluster == null) {
					iCluster = new ClusterBase(minCat);
				}
				iCluster.addVector(vector);
				results.put(minCat, iCluster);
			}
		}
	}

	private boolean checkConvergence(List<ITermsVector> oldCentrals, List<ITermsVector> newCentrals) {
		double error = 0;
		for (int i = 0; i < oldCentrals.size(); i++) {
			error += Math.pow(oldCentrals.get(i).cosine(newCentrals.get(i)), 2);
		}
		return error > THRESHOLD;
	}

	@Override
	public IAnalysisResult getCategories(IDataUnitDoc input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IAnalysisResult> getCategories() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Languages> getLanguages() {
		return this.categories != null ? this.categories.keySet() : new HashSet<>();
	}
}
