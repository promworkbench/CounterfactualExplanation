package org.processmining.CounterfactualRecommendation.parameters;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.CounterfactualRecommendation.algorithms.DataExtraction;
import org.processmining.datadiscovery.estimators.Type;

public class CounterfactualParameters {
	
	private Set<String> goodResultNominal = null;  // null if independent variable is numerical
	private double goodResultNmericalThreshold; 
	private List goodValues;
	private LowOrHeigh lowerOrHeigher = LowOrHeigh.LOWER;  // 
	private int maxNumIteration;
	private String regex = "\n";
	
	private String SEM;
	private Map<String, Double> currentInstance;
	private boolean useSampling = true;
	private Metric metric = Metric.L1;
	private DataExtraction de;
	private Set<String> actionableAttNames; //TODO make the connection to the visualization
	
	//--------- Classifier Parameters---------------
	private ClassificationMethod  method = ClassificationMethod.SEM;
	
	// Nural network (multilayer perceptron)
	private double learningRate = 0.1;  // in [0, 1]
	private double momentum = 2.0; // in [0, 1]
	private int numEpoches = 500;
//	private int trainingTime = 2000;
	private java.lang.String hiddenLayers = "3?";
	
	// LWL
	private int kernelType = 0; // kernel type for LWL  0 = Linear, 1 = Inverse, 2 = Gaussian. (default 0)
	private int knn = -1;  // number of neighbors to consider for LWL (default 0 = -1)
	
	// RegTree
	private int maxDepth = -1; //-1 means the algorithm will automatically control the depth.
	private boolean noPruning = false; // cut back on a leaf node that does not contain much information?
	private int minNum = 2; // Minimum number of instances per leaf.
	
	// do random sampling?
	boolean doRandomSampling = true;
	
	// do Optimization? 
	boolean doOptimization = false;

	// optimization step parameters
	double timeStep = 0; // in houre
	double nonTimeStep; // other atts step size in percent
	
	
	
	/**
	 * the number of neighbours included inside the kernel bandwidth, or 0 to specify using all neighbors.
	 */
	private int numberOfNeighbours = 0;
	
	/**
	 * 0=Linear, 1=Epanechnikov, 2=Tricube, 3=Inverse, 4=Gaussian.
	 */
	private int kernel = 1; 
	
	/**
	 * the number of counterfactual worlds with the desirable result that we want to have.
	 */
	private int numCounterfactuals = 10; 
	
	
	public boolean ifAttIsLiteral(String attName) {
//		System.out.println(de.getAttributeTypes());
		System.out.println(attName);
		if (de.getAttributeTypes().get(attName).equals(Type.LITERAL))
			return true;
		
		return false;
	}
	
	public double getDouble(Object v) {
		double d = 0;
		if (v instanceof Integer)
			d = ((Integer) v).doubleValue();
		else if (v instanceof Long)
			d = ((Long) v).doubleValue();
		else
			d = (double) v;
		
		return d;
	}

	
	public void setGoodResultNominal (Set<String> result) {
		goodResultNominal = result;
	}
	
	public Set<String> getGoodResultNominal () {
		return goodResultNominal;
	}

	public void setGoodResultNmericalThreshold (double d) {
		goodResultNmericalThreshold = d;
	}
	
	public double getGoodResultNmericalThreshold () {
		return goodResultNmericalThreshold;
	}

	public void setLowerOrHeigher(LowOrHeigh l) {
		lowerOrHeigher = l; 
	}
	
	public LowOrHeigh getLowerOrHeigher() {
		return lowerOrHeigher; 
	}
	
	public void setMethod(ClassificationMethod m) {
		method = m;
	}
	
	public ClassificationMethod  getMethod() {
		return method;
	}
	
	public void setSEM(String sem) {
		SEM = sem;
	}
	
	public LinkedList<String> getSEM() {
		
		for (String attName : de.getAttributeTypes().keySet()) 
			SEM = SEM.replaceAll(attName, "@" + attName + "@");
		
		LinkedList<String> s = new LinkedList<String>();
		
		String[] terms = SEM.split(regex);
		for (String term : terms) 
			s.add(term);
		
		return s;
	}
	
	
	
	/**
	 * set the current instance in such a way that the literal attributes are one hot encoded.
	 * @param instance
	 */
	public void setCurrentInstance(Map<String, Object> instance) {
		Map<String, Double> newInstance = new HashMap<String, Double>();
		
		for (String attName : instance.keySet()) {
			if (ifAttIsLiteral(attName)) {
				for (String val : de.getLiteralValues(attName)) {
					if (instance.get(attName).equals(val))
						newInstance.put(attName + " --> " + val, 1.0);
					else 
						newInstance.put(attName + " --> " + val, 0.0);
				}
			} else {
				newInstance.put(attName, getDouble(instance.get(attName)));
			}
		}
			
		currentInstance = newInstance;
	}
	
	public Map<String, Double> getCurrentInstance() {
		return currentInstance;
	}

	public void setUseSampling(boolean b) {
		useSampling = b;
	}
	
	public boolean getUseSampling() {
		return useSampling;
	}
	
	public void setMetric(Metric m) {
		metric = m;
	}
	
	public Metric getMetric() {
		return metric;
	}
	
	public void setValues (List list) {
		goodValues = list;
	}
	
	public List getValues () {
		return goodValues;
	}

	public int getNumCounterfactuals() {
		return numCounterfactuals;
	}
	
	public void setLearningRate(double d) {
		learningRate = d;
	}
	
	public double getLearningRate() {
		return learningRate;
	}
	
	public void setMomentum(double d) {
		momentum = d;
	}
	
	public double getMomentum() {
		return momentum;
	}
/**	
	public void setTrainingTime(int t) {
		trainingTime = t;
	}
	
	public int getTrainingTime() {
		return trainingTime;
	} */
	
	public void setHiddenLayers(String str) {
		hiddenLayers = str;
	}
	
	public String getHiddenLayers() {
		return hiddenLayers;
	}
	
	public void setDataExtraction (DataExtraction d) {
		de = d;
	}
	
	public DataExtraction getDataExtraction() {
		return de;
	}
	
	/**
	 * is true if we are looking for lower values than the given desirableValue 
	 * for the targetVariable or the higher variable.
	 */
	
	public boolean ifLowerIsDesirable() {
		if (lowerOrHeigher.equals(LowOrHeigh.LOWER))
			return true;
		
		return false;
	}

	public int getMaxIteration() {
		return maxNumIteration;
	}
	
	public void setMaxIteration(int num) {
		maxNumIteration = num;
	}

	public int getKNN() {
		return knn;
	}
	
	public void setKNN(int n) {
		knn = n;
	}

	public int getKernel() {
		return kernelType;
	}
	
	public void setKernel(int n) {
		if (n <= 2 && n >= 0)
			kernelType = n;
		else 
			kernelType = 0;
	}

	public int getNumEpoches() {
		return numEpoches;
	}
	
	public void setNumEpoches(int n) {
		numEpoches = n;
	}
	
	public void setMaxDepth(int n) {
		if (n >= -1)
			maxDepth = n; 
	}
	
	public int getMaxDepth() {
		return maxDepth; 
	}
	
	public void setNoPruning(boolean b) {
		noPruning = b;
	}
	
	public boolean getNoPruning() {
		return noPruning;
	}
	
	public void setMinNum(int n) {
		if (n > 0)
			minNum = n;
	}
	
	public int getMinNum() {
		return minNum;
	}
	
	/**
	 * return parameters for the neural network.
	 * L = Learning Rate
	 * M = Momentum
	 * N = Training Time or Epochs
	 * H = Hidden Layers
	 */
	public String getNNparameters() {
		return "-L "+ learningRate +" -M " + momentum + " -N " + numEpoches + " -V 0 -S 0 -E 20 -H" + hiddenLayers;
	}

	public Set<String> getActionableAttNames() {
		return actionableAttNames;
	}
	
	public void setActionableAttNames(Collection collection) {
		actionableAttNames = new HashSet<>();
		for (Object str : collection)
			actionableAttNames.add((String) str);
	}
	
	public boolean doRandomSampling() {
		return doRandomSampling;
	}
	
	public void setDoRandomSampling(boolean b) {
		doRandomSampling = b;
	}
	
	public boolean doOptimization() {
		return doOptimization;
	}
	
	public void setDoOptimization(boolean b) {
		doOptimization = b;
	}

	public double getTimeStep() {
		return timeStep;
	}
	
	public void setTimeStep(double step) {
		timeStep = step;
	}

	public double getNonTimeStep() {
		return nonTimeStep;
	}
	public void setNonTimeStep(double step) {
		nonTimeStep = step;
	}
}
