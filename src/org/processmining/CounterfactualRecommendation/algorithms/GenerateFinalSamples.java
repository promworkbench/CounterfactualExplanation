package org.processmining.CounterfactualRecommendation.algorithms;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.processmining.CounterfactualRecommendation.decisionTree.Classification;
import org.processmining.CounterfactualRecommendation.optimizer.Optimizer;
import org.processmining.CounterfactualRecommendation.parameters.ClassificationMethod;
import org.processmining.CounterfactualRecommendation.parameters.CounterfactualParameters;
import org.processmining.CounterfactualRecommendation.parameters.Metric;
import org.processmining.datadiscovery.estimators.Type;

import javafx.util.Pair;

/**
 * The goal is generating an ordered set of counterfactual samples with the desirable results.
 * @author qafari
 *
 */
public class GenerateFinalSamples {
	
	private DataExtraction de;
	private CounterfactualParameters params;
		
	/**
	 * for each desirable sample in the final list which attribute has been changed;
	 * the pattern is a string of 0 and 1s that 1 means that attribute value has been changed.
	 */
	private Map<Map<String, Double>, String> samplePattern;
	
	/**
	 * The list including the counterfactual samples with the desirable result (if exist).
	 */
	private LinkedList<Pair<Map<String, Double>, Double>> finalList;
	
	/**
	 * For the evaluation, to save the CSM value for each sample.
	 */
	private Map<Map<String, Double>, Double> sampleValueCSM;
	
	private RandomSampleGenerator rsg;
	
	private Classification classifier;
	
	private CSM sem;
	
	public GenerateFinalSamples(DataExtraction de, CounterfactualParameters params) {
		this.de = de;
		this.params = params;
		doTheSetUp();
	}

	private void doTheSetUp() {
		
		rsg = new RandomSampleGenerator(params);
        rsg.setDesirableValue(params.getGoodResultNmericalThreshold());
   //     rsg.setMaxAcceptableDistance(100000000); //TODO need to ne according to the number of atts and metric distance
        rsg.setAttributeType(de.getAttributeTypes());
        
        rsg.setIfModifiable(getModifiableAtts());
        rsg.setTheDistributions();
 //     finalList = rsg.generateSamples(de.classAttributeName(), params.getNumCounterfactuals(), 1000000);
        
        rsg.generateRandomSamplesDifferentAtts(de.classAttributeName(), params.getNumCounterfactuals(), 1000000);
        samplePattern = rsg.getSamplePattern();
        
//        params.setMethod(ClassificationMethod.RT);
        
        /**
         * This part is used for the evaluation results for the paper.
         */
//        evaluate();
//        evaluateCA();
        
        /**
         * End of paper evaluation part ;).
         */
     // If the method is SEM set the sem
        if (params.getMethod().equals( ClassificationMethod.SEM)) {
        	finalList = getCounterfactualSamples(getDesirableListSamplesSEM(), params.getMetric());
        } else 
        	finalList = getCounterfactualSamples(getDesirableListSamplesClassifier(), params.getMetric());
        
        if (finalList.isEmpty())
    		System.out.println("empty List");
    	else
    		for (int i = 0; i < finalList.size(); i++) {
    			System.out.println("final List CF " + i + " --> " +finalList.get(i).getKey() + " " + de.classAttributeName() + " : " + finalList.get(i).getValue());
    	}
        
        System.out.println("======================================== ");
        // Optimize counterfactual instances
        if (params.doOptimization()) {
        	optimizeFinalList();
        }  
        
        if (finalList.isEmpty())
    		System.out.println("empty List");
    	else
    		for (int i = 0; i < finalList.size(); i++) {
    			System.out.println("final List CF " + i + " --> " +finalList.get(i).getKey() + " " + de.classAttributeName() + " : " + finalList.get(i).getValue());
    	}
	}

	/**
	 * Optimize the current and the instances in the finalList and assign the new generated counterfactual instances to the finalList
	 */
	private void optimizeFinalList() {
		// final list of desirable counterfactual instances
		LinkedList<Pair<Map<String, Double>, Double>> optimizedList = new LinkedList<>();
		
		//setup the optimizer
		Optimizer opt = new Optimizer(params);
		if (params.getMethod().equals(ClassificationMethod.SEM))
			opt.setSEM(sem);
		else
			opt.setClassifier(classifier);
		
		//optimize current instance
		Set<Map<String, Double>> newInstances = new HashSet<>();
		newInstances = opt.optimizeInstance(null);
		addInstancesToList(optimizedList, newInstances);
		System.out.println(" current instance : "+ params.getCurrentInstance());
		//optimize instances in the final list
		for (Pair<Map<String, Double>, Double> item : finalList) {
			Map<String, Double> instance = turnToFullInstance(item.getKey());
			newInstances = new HashSet<>();
			newInstances = opt.optimizeInstance(instance);
			if(newInstances != null)
				addInstancesToList(optimizedList, newInstances);
			else
				System.out.println("null"+"null");
		}

		finalList = optimizedList;
	}
	
	/**
	 * add the attributes that already does not exists in the instance to it.
	 * The value of these attributes would be as the value of their value in the current instance.
	 * @param instance
	 * @return
	 */
	private Map<String, Double> turnToFullInstance(Map<String, Double> instance) {
		Map<String, Double> currentInstance = params.getCurrentInstance();
		for(String attName : currentInstance.keySet())
			if (!instance.containsKey(attName))
				instance.put(attName, currentInstance.get(attName));
		
		return instance;
	}

	/**
	 * add a set of instances to a list of instances
	 * @param list
	 * @param set
	 */
	private void addInstancesToList(LinkedList<Pair<Map<String, Double>, Double>> list,
			Set<Map<String, Double>> set) {

		for (Map<String, Double> item : set) {
			list.add(new Pair<Map<String, Double>, Double>(item, item.get(de.classAttributeName())));
		}
		
	}

private Map<String, LinkedList<Pair<Map<String, Double>, Double>>> getDesirableListSamplesClassifier() {
	String targetVarName = de.classAttributeName();
	int numSample = params.getNumCounterfactuals();
	int maxIteration = params.getMaxIteration();
	
	classifier = new Classification(params, de);
	
	Map<String, LinkedList<Map<String, Double>>> patternSamples = rsg.getRandomSamples();
	Map<String, LinkedList<Pair<Map<String, Double>, Double>>> desirableSamples = new HashMap<String, LinkedList<Pair<Map<String, Double>, Double>>>();
	
	int i = 0;
	for (String pattern : patternSamples.keySet()) {
		if (i < patternSamples.get(pattern).size()) {
			Map<String, Double> sample = patternSamples.get(pattern).get(i);
			if (rsg.isAValidCFW(sample)) {
				double value = classifier.evaluateInstance(sample);
				double threshold = params.getGoodResultNmericalThreshold ();
				if ((params.ifLowerIsDesirable() && value <= threshold) || (!params.ifLowerIsDesirable() && value > threshold)) {
					Pair<Map<String, Double>, Double> p = new Pair(sample, value);
					if (desirableSamples.containsKey(pattern))
						desirableSamples.get(pattern).add(p);
					else {
						LinkedList<Pair<Map<String, Double>, Double>> list = new LinkedList<Pair<Map<String, Double>, Double>>();
						list.add(p);
						desirableSamples.put(pattern, list);
					}
				}
			}
		}
		
		i = 0;			
	}
	

	return desirableSamples;
	}

//000--------------------------
	
	public Map<String, LinkedList<Pair<Map<String, Double>, Double>>> getDesirableListSamplesSEM() {
		String targetVarName = de.classAttributeName();
		int numSample = params.getNumCounterfactuals();
		int maxIteration = params.getMaxIteration();
	    sampleValueCSM = new HashMap<Map<String, Double>, Double>();
		sem = new CSM(params.getSEM());
		// set current instance
		sem.setCurrentWorld(getCurrentInstance());
		
		// set minMax
		sem.setMinMax(de.getMinMax());
		
		Map<String, LinkedList<Map<String, Double>>> patternSamples = rsg.getRandomSamples();
		Map<String, LinkedList<Pair<Map<String, Double>, Double>>> desirableSamples = new HashMap<String, LinkedList<Pair<Map<String, Double>, Double>>>();
		
		Set<String> ancestors = sem.getAttAncestors(de.classAttributeName());
		int i = 0;
		for (String pattern : patternSamples.keySet()) {
//			if (match(pattern, ancestors)) {
				if (i < patternSamples.get(pattern).size()) {
					Map<String, Double> sample = patternSamples.get(pattern).get(i);
					if (rsg.isAValidCFW(sample)) {
						sem.setCounterfactualWorld(sample);
						double value = sem.computeCounterfactualValue(targetVarName, sample);
						double threshold = params.getGoodResultNmericalThreshold ();
						if ((params.ifLowerIsDesirable() && value <= threshold) || (!params.ifLowerIsDesirable() && value > threshold)) {
							sampleValueCSM.put(sample, value);
							Pair<Map<String, Double>, Double> p = new Pair(sample, value);
							if (desirableSamples.containsKey(pattern))
								desirableSamples.get(pattern).add(p);
							else {
								LinkedList<Pair<Map<String, Double>, Double>> list = new LinkedList<Pair<Map<String, Double>, Double>>();
								list.add(p);
								desirableSamples.put(pattern, list);
							}
						}
					}
	//			}
			}
			
			i = 0;			
		}
		
	
		return desirableSamples;
	} 
	
	/**
	 * It matches if the attribute in the pattern is a subset of the effective atts on the class att according to the SEM.
	 * @param pattern
	 * @param ancestors
	 * @return true if there are 0 for all the attnames which are not causally effective on the class attribute according to the SEM, o.w. false.
	 */
	private boolean match(String pattern, Set<String> ancestors) {
		
		int j = 0;
		for (String attName : de.getAttributeTypes().keySet()) {
			if (pattern.charAt(j) == '0' && ancestors.contains(attName))
				return false;
			j++;
		}
		
		return true;
	}

	public LinkedList<Pair<Map<String, Double>, Double>> getCounterfactualSamples(Map<String, LinkedList<Pair<Map<String, Double>, Double>>> samples, Metric m) {
		LinkedList<Pair<Map<String, Double>, Double>> finalList = new LinkedList<>();
		if (m.equals(Metric.NUMATT))
			sortByNumAtts(samples);
		else if (m.equals(Metric.L1))
			sortBySumAtts(samples);
		
		String[] patterns = setPatterns(samples);
		
		Map<String, Integer> idxs = new HashMap<String, Integer>();
		for (String pattern : patterns)
			idxs.put(pattern, 0);
		
		int num = 0;
		for (String pattern : patterns) {
			if (!samples.get(pattern).isEmpty()) {
				finalList.add(samples.get(pattern).get(idxs.get(pattern)));
				int n = idxs.get(pattern);
				idxs.remove(pattern);
				idxs.put(pattern, n + 1);
				num++;
			}
			
			if (num == params.getNumCounterfactuals())
				break;
		}
		
		return finalList;
	}
	
	/**
	 * Generates final list of acceptable samples.
	 * @param targetVarName
	 * @param numSample
	 * @param maxIteration
	 * @return 
	 
	public LinkedList<Pair<Map<String, Double>, Double>> generateCounterfactualSampleList(String targetVarName, int numSample, int maxIteration) {
		LinkedList<Pair<Map<String, Double>, Double>> desirableSamples = new LinkedList<Pair<Map<String, Double>, Double>>();
		samplePattern = new HashMap<Map<String, Double>, String>();
		targetVariable = targetVarName;
		
		// Pattern 0..0 is useless.
		double numConfigs =  Math.pow(2.0, attributeTypes.size() ) - 1;
		int numIteration = (int) Math.round(maxIteration / numConfigs + numConfigs);
		
		String[] patterns = setPatterns(numConfigs);
		
		// finding the index of class variable
		int classVarIndex = -1;
		int j = 0;
		for (String attName : attributeTypes.keySet()) {
			if (attName.equals(targetVarName))
				classVarIndex = j;
			j++;
		}
		
		Map<String, LinkedList<Pair<Map<String, Double>, Double>>> patternSamoleListMap = new HashMap<>();
		
		for (int itr = 0; itr < numConfigs; itr++) {
			// Class att should not change.
			if (patterns[itr].charAt(classVarIndex) == '0') {
				setAttsToChangeValue(patterns[itr]);
				setTheDistributions();
				System.out.println(patterns[itr]);
				LinkedList<Pair<Map<String, Double>, Double>> sampleList = new LinkedList<>();
				for (int i = 0; i < numIteration; i++) {
					Map<String, Double> sample = generateOneCFW();
					if (isAValidCFW(sample)) {
						sem.setCounterfactualWorld(sample);
						double value = sem.computeCounterfactualValue(targetVarName, sample);
						sample.put(targetVariable, value); // add the class att to the sample.
//						System.out.println("CF sample " + sample);
						System.out.println("CF value " + value);
						if ((ifLowerIsDesirable && value <= desirableValue) || (!ifLowerIsDesirable && value > desirableValue)) {
							samplePattern.put(sample, patterns[itr]);
							sampleList.add(new Pair<Map<String, Double>, Double>(sample, value));
							desirableSamples.add(new Pair<Map<String, Double>, Double>(sample, value));
						}
					}
				} 
				patternSamoleListMap.put(patterns[itr], sampleList);
			}
		}
		
		// Sort the good counterfactual samples in each pattern list according to the difference of their dependent values
		// and the original one // TODO return the one with the bigger sum of atts. 
		// check what is needed and implement that one.
		for (int itr = 0; itr < numConfigs; itr++) {
			if (patternSamoleListMap.containsKey(patterns[itr])) {
				LinkedList<Pair<Map<String, Double>, Double>> list = patternSamoleListMap.get(patterns[itr]);
				Collections.sort(list, new Comparator<Pair<Map<String, Double>, Double>>() {
				     @Override
				     public int compare(Pair<Map<String, Double>, Double> o1, Pair<Map<String, Double>, Double> o2) {
				         double d1 = Math.abs(o1.getValue() - sem.getCurrentSample().get(targetVarName)[0]);
				         double d2 = Math.abs(o2.getValue() - sem.getCurrentSample().get(targetVarName)[0]);
				         
				         if (d1 == d2)
				        	 return 0;
				         if (d1 < d2)
				        	 return -1;
				         return 1;
				     }
				 });
			}
				
		}
		
		LinkedList<Pair<Map<String, Double>, Double>> finalDesirableSampleList = new LinkedList<>();
		
		
		int num = 1;
		while (num < numSample && !allListsAreEmpty(patternSamoleListMap)) {
			for (int itr = 0; itr < numConfigs; itr++) { 
				if ((patternSamoleListMap.containsKey(patterns[itr]))) {
					LinkedList<Pair<Map<String, Double>, Double>> list = patternSamoleListMap.get(patterns[itr]);
					if (!list.isEmpty()) {
						finalDesirableSampleList.add(list.get(0));
						num++;
						list.remove(0);
						if (num > numSample)
							break;
					}
				}
			}
		}
		return finalDesirableSampleList;
	}	*/
	

	/**
	 * Sort the good counterfactual samples in each pattern list according to the difference of their dependent values
	 * and the original one.
	 * 
	 * \\TODO it is already just sum sort without normalization
	 */
	public void sortByNumAtts(Map<String, LinkedList<Pair<Map<String, Double>, Double>>> patternSamplesMap) {
		double numConfigs =  Math.pow(2.0, de.getAttributeTypes().size() ) - 1;
		
		String[] patterns = setPatterns(patternSamplesMap);
		
		for (int itr = 0; itr < numConfigs; itr++) {
			if (patternSamplesMap.containsKey(patterns[itr])) {
				LinkedList<Pair<Map<String, Double>, Double>> list = patternSamplesMap.get(patterns[itr]);
				Collections.sort(list, new Comparator<Pair<Map<String, Double>, Double>>() {
				     @Override
				     public int compare(Pair<Map<String, Double>, Double> o1, Pair<Map<String, Double>, Double> o2) {
				    	 
				    	 
				         double d1 = Math.abs(o1.getValue() - params.getCurrentInstance().get(de.classAttributeName()));
				         double d2 = Math.abs(o2.getValue() - params.getCurrentInstance().get(de.classAttributeName()));
				         
				         if (d1 == d2)
				        	 return 0;
				         if (d1 < d2)
				        	 return -1;
				         return 1;
				     }
				 });
			}
				
		}		
	}
	
	/**
	 * Sort the good counterfactual samples in each pattern list according to the the summation of the difference
	 * between their attribute values and the original attribute values.  L1 distance on normalized values.
	 * Already it does not consider the values that would change as the result of changing current values.
	 */
	public void sortBySumAtts(Map<String, LinkedList<Pair<Map<String, Double>, Double>>> patternSamplesMap) {
		double numConfigs =  Math.pow(2.0, de.getAttributeTypes().size() ) - 1;
		String[] patterns = setPatterns(patternSamplesMap);
		
		for (String pattern : patterns) {
			LinkedList<Pair<Map<String, Double>, Double>> list = patternSamplesMap.get(pattern);
			Collections.sort(list, new Comparator<Pair<Map<String, Double>, Double>>() {
			     @Override
			     public int compare(Pair<Map<String, Double>, Double> o1, Pair<Map<String, Double>, Double> o2) {
			    	 double d1 = 0.0;
			    	 for (String attName : o1.getKey().keySet())
			    		 if (params.getCurrentInstance().containsKey(attName))
			    			 d1 = d1 + Math.abs((params.getCurrentInstance().get(attName) - o1.getKey().get(attName))/ (getDouble(de.getMinMax(attName)[1]) - getDouble((de.getMinMax(attName)[0]))));
			    	 
			    	 double d2 = 0.0;
			    	 for (String attName : o2.getKey().keySet())
			    		 if (params.getCurrentInstance().containsKey(attName))
			    			 d2 = d2 + Math.abs((params.getCurrentInstance().get(attName) - o2.getKey().get(attName))/ (getDouble(de.getMinMax(attName)[1]) - getDouble((de.getMinMax(attName)[0]))));

			         
			         if (d1 == d2)
			        	 return 0;
			         if (d1 < d2)
			        	 return -1;
			         return 1;
			     }
			 });
				
		}		
	}
	
	public String[] setPatterns(Map<String, LinkedList<Pair<Map<String, Double>, Double>>> patternSamplesMap) {
		String[] patterns = new String[patternSamplesMap.size()];
		int idx = 0;
		for (String pattern : patternSamplesMap.keySet()) {
			patterns[idx] = pattern;
			idx++;
		}
		rsg.bubbleSort(patterns);
		
		return patterns;
	}
	
	private void evaluateCA() {
		
		Map<Map<String, Double>, String> samplePatternCA = samplePatternCA();
		// open the file to save the results
				PrintWriter writerCA = null;
				PrintWriter writerSampleCA = null;
				
				try {
					writerCA = new PrintWriter("resultCECA.txt", "UTF-8");
					writerSampleCA = new PrintWriter("resultWithSampleCECA.txt", "UTF-8");
				} catch (FileNotFoundException | UnsupportedEncodingException e) {
					System.out.println("fail to create the file");
					e.printStackTrace();
				}
				
				// SEM
				writerCA.println("SEM");
				writerSampleCA.println("SEM");
				finalList = new LinkedList<Pair<Map<String, Double>, Double>>();
				params.setMethod(ClassificationMethod.SEM);
				finalList = getCounterfactualSamples(getDesirableListSamplesSEM(true), params.getMetric());
				writeFinalList(writerSampleCA);
				saveResults(writerSampleCA, null);
				saveResults(writerCA, null);
				// REGTree
				finalList = new LinkedList<Pair<Map<String, Double>, Double>>();
				params.setMethod(ClassificationMethod.RT);
				int[] minNum = {2, 4, 8, 16};
				int[] maxDepth = {-1, 5, 10, 20, 40, 80};
				boolean[] noPrune = {true, false};
				for (int n : minNum)
					for (int d : maxDepth)
						for (boolean b : noPrune) {
							String setting = "RT, " + n + ", " + d + ", " + b;
							saveResults(writerCA, setting);
							saveResults(writerSampleCA, setting);
							System.out.println(setting);
							
							finalList = new LinkedList<Pair<Map<String, Double>, Double>>();
							params.setMethod(ClassificationMethod.RT);
							params.setMaxDepth(d);
							params.setMinNum(n);
							params.setNoPruning(b);
							finalList = getCounterfactualSamples(getDesirableListSamplesClassifier(true), params.getMetric());
							saveResults(writerCA, null);
							writeFinalList(writerSampleCA);
							saveResults(writerSampleCA, null);
							
						}
				
				// LWL
				int[] kernelType = {0, 1, 2, 3, 4}; 
				int[] knn = {-1, 2, 4, 8, 16, 32};
				for (int kt : kernelType)
					for (int k : knn) {
						String setting = "LWL, " + kt + ", " + k;
						saveResults(writerCA, setting);
						saveResults(writerSampleCA, setting);
						System.out.println(setting);
						
						finalList = new LinkedList<Pair<Map<String, Double>, Double>>();
						params.setMethod(ClassificationMethod.LWL);
						params.setKernel(kt);
						params.setKNN(k);
						finalList = getCounterfactualSamples(getDesirableListSamplesClassifier(true), params.getMetric());
						saveResults(writerCA, null);
						writeFinalList(writerSampleCA);
						saveResults(writerSampleCA, null);
					}
				 
				// NN
				
				double[] learningRate = {0.1, 0.2, 0.3, 0.4};
				double[] momentum = {0.1, 0.2, 0.3, 0.4};
				int[] numEpoches = {10, 20, 40, 100}; //, 200, 500, 1000, 2000};
				String[] hiddenLayers = {"1" , "2", "4"};
				
				for (double lr : learningRate)
					for (double m : momentum) 
						for (int ne : numEpoches) {
							for (String h : hiddenLayers) {
								String setting = "NN, " + lr + ", " + m + ", " + ne + ", " + h;
								saveResults(writerCA, setting);
								saveResults(writerSampleCA, setting);
								System.out.println(setting);
								
								finalList = new LinkedList<Pair<Map<String, Double>, Double>>();
								params.setMethod(ClassificationMethod.NN);				
								params.setLearningRate(lr);
								params.setMomentum(m);
								params.setNumEpoches(ne);
								params.setHiddenLayers(h);
								finalList = getCounterfactualSamples(getDesirableListSamplesClassifier(true), params.getMetric());
							//	saveResults(writer, null);
							}
							saveResults(writerCA, null);
							writeFinalList(writerSampleCA);
							saveResults(writerSampleCA, null);
						}
				
				writerCA.close();
				writerSampleCA.close();
	}
	
	/**
	 * The set of ancestors of the class attribute in SEM.
	 */
	private Set<String> ancestors;
	
	
	/**
	 * remove the atts that are not ancestor of the calss at in SEM. Has no causal effect on class att.
	 * @return
	 */
	private Map<Map<String, Double>, String> samplePatternCA() {
		Map<Map<String, Double>, String> newSamples = new HashMap<Map<String, Double>, String>();
		
		for (Map<String, Double> sample : samplePattern.keySet()) {
			String pattern = samplePattern.get(sample);
			Map<String, Double> sm = new HashMap<String, Double>();
			int idx = 0;
			for (String attName : de.getAttributeTypes().keySet()) {
				if (sample.containsKey(attName) && ancestors.contains(attName))
					sm.put(attName, sample.get(attName));
				else
					pattern = replaceCharAt(pattern, idx);
				if (!sm.isEmpty())
					newSamples.put(sm, pattern);
				idx++;
			}
		}
		
		
		return newSamples;
	}

private String replaceCharAt(String pattern, int idx) {
		return  pattern.substring(0,idx)+'0'+pattern.substring(idx+1);
	}

public Map<String, LinkedList<Pair<Map<String, Double>, Double>>> getDesirableListSamplesSEM(boolean b) {
	String targetVarName = de.classAttributeName();
	int numSample = params.getNumCounterfactuals();
	int maxIteration = params.getMaxIteration();
    sampleValueCSM = new HashMap<Map<String, Double>, Double>();
	sem = new CSM(params.getSEM());
	// set current instance
	sem.setCurrentWorld(getCurrentInstance());
	
	// set minMax
	sem.setMinMax(de.getMinMax());
	
	ancestors = sem.getAttAncestors(de.classAttributeName());
	
	Map<String, LinkedList<Map<String, Double>>> patternSamples = rsg.getRandomSamples();
	patternSamples = cleanPatternSample(patternSamples);
	
	
	Map<String, LinkedList<Pair<Map<String, Double>, Double>>> desirableSamples = new HashMap<String, LinkedList<Pair<Map<String, Double>, Double>>>();
	
	
	int i = 0;
	for (String pattern : patternSamples.keySet()) {
//		if (match(pattern, ancestors)) {
			if (i < patternSamples.get(pattern).size()) {
				Map<String, Double> sample = patternSamples.get(pattern).get(i);
				if (rsg.isAValidCFW(sample)) {
					sem.setCounterfactualWorld(sample);
					double value = sem.computeCounterfactualValue(targetVarName, sample);
					double threshold = params.getGoodResultNmericalThreshold ();
					if ((params.ifLowerIsDesirable() && value <= threshold) || (!params.ifLowerIsDesirable() && value > threshold)) {
						sampleValueCSM.put(sample, value);
						Pair<Map<String, Double>, Double> p = new Pair(sample, value);
						if (desirableSamples.containsKey(pattern))
							desirableSamples.get(pattern).add(p);
						else {
							LinkedList<Pair<Map<String, Double>, Double>> list = new LinkedList<Pair<Map<String, Double>, Double>>();
							list.add(p);
							desirableSamples.put(pattern, list);
						}
					}
				}
//			}
		}
		
		i = 0;			
	}
	

	return desirableSamples;
} 


private Map<String, LinkedList<Map<String, Double>>> cleanPatternSample(
		Map<String, LinkedList<Map<String, Double>>> patternSamples) {
	Map<String, LinkedList<Map<String, Double>>> newPatternSamples = new HashMap<String, LinkedList<Map<String, Double>>>();
	int i = 0;
	for (String pattern : patternSamples.keySet()) {
//		if (match(pattern, ancestors)) {
			if (i < patternSamples.get(pattern).size()) {
				Map<String, Double> sample = patternSamples.get(pattern).get(i);
				Map<String, Double> sm = new HashMap<String, Double>();
				int idx = 0;
				String newPattern = pattern;
				for (String attName : de.getAttributeTypes().keySet()) {
					if (sample.containsKey(attName) && ancestors.contains(attName))
						sm.put(attName, sample.get(attName));
					else
						newPattern = replaceCharAt(pattern, idx);
					if (!sm.isEmpty())
						if (newPatternSamples.containsKey(newPattern))
							newPatternSamples.get(newPattern).add(sm);
						else {
							LinkedList<Map<String, Double>> list = new LinkedList<Map<String, Double>>();
							list.add(sm);
							newPatternSamples.put(newPattern, list);
						}
					idx++;
				}
			}
	}
				
	return newPatternSamples;
}

private Map<String, LinkedList<Pair<Map<String, Double>, Double>>> getDesirableListSamplesClassifier(boolean b) {
	String targetVarName = de.classAttributeName();
	int numSample = params.getNumCounterfactuals();
	int maxIteration = params.getMaxIteration();
	
	classifier = new Classification(params, de);
	
	Map<String, LinkedList<Map<String, Double>>> patternSamples = rsg.getRandomSamples();
	patternSamples = cleanPatternSample(patternSamples);
	Map<String, LinkedList<Pair<Map<String, Double>, Double>>> desirableSamples = new HashMap<String, LinkedList<Pair<Map<String, Double>, Double>>>();
	
	int i = 0;
	for (String pattern : patternSamples.keySet()) {
		if (i < patternSamples.get(pattern).size()) {
			Map<String, Double> sample = patternSamples.get(pattern).get(i);
			if (rsg.isAValidCFW(sample)) {
				double value = classifier.evaluateInstance(sample);
				double threshold = params.getGoodResultNmericalThreshold ();
				if ((params.ifLowerIsDesirable() && value <= threshold) || (!params.ifLowerIsDesirable() && value > threshold)) {
					Pair<Map<String, Double>, Double> p = new Pair(sample, value);
					if (desirableSamples.containsKey(pattern))
						desirableSamples.get(pattern).add(p);
					else {
						LinkedList<Pair<Map<String, Double>, Double>> list = new LinkedList<Pair<Map<String, Double>, Double>>();
						list.add(p);
						desirableSamples.put(pattern, list);
					}
				}
			}
		}
		
		i = 0;			
	}
	

	return desirableSamples;
	}

//000------------------------------
	
	/**
	 * return the ordered list of samples using the metric chosen by the user.
	 * default L1 metric
	 * @param sampleList
	 * @return 
	 */
	public LinkedList<Pair<Map<String, Double>, Double>> sortByMetricDistance() {
		Map<String, Double> coefficients = new HashMap<String, Double>();
		if (params.getMetric().equals(Metric.MAD_L1)) {
			MADdistance mad = new MADdistance(de);
			coefficients = mad.getMADcoefficients();
		} else if (params.getMetric().equals(Metric.L1)) {
			for (String attName : de.getAttributeTypes().keySet())
				coefficients.put(attName, 1.1);
		} else {
			//TODO if user specified distance
		}
		
		// Compute the distances
		LinkedList<Pair<Map<String, Double>, Double>> newSampleList = new LinkedList<Pair<Map<String, Double>, Double>>();
		for (Pair<Map<String, Double>, Double> sample : finalList) 
			newSampleList.add(new Pair<Map<String, Double>, Double> (sample.getKey(), distance(sample.getKey(), coefficients)));
		
		// Sort the good counterfactual samples according to the difference of their distance to the current world
		Collections.sort(newSampleList, new SampleComparator());		
				
		return newSampleList;
	}
	
	/**
	 * return the ordered list of samples using the number of different attribute values.
	 * default L1 metric
	 * @param sampleList
	 * @return 
	 */
	public LinkedList<Pair<Map<String, Double>, Double>> sortByNumDifferentAtts() {
		
		// Compute the distances
		LinkedList<Pair<Map<String, Double>, Double>> newSampleList = new LinkedList<Pair<Map<String, Double>, Double>>();
		for (Pair<Map<String, Double>, Double> sample : finalList) 
			newSampleList.add(new Pair<Map<String, Double>, Double> (sample.getKey(), numAttDistance(sample.getKey())));
		
		// Sort the good counterfactual samples according to the difference of their distance to the current world
		Collections.sort(newSampleList, new SampleComparator());		
				
		return newSampleList;
	}
	
	/**
	 * return the list of pair<samples, distance>.
	 * distance can be:
	 * 		numAtt : number of different attribute values
	 * 		output : difference in the value of target variable
	 * 		
	 * default L1 metric
	 * @param sampleList
	 * @return 
	 */
	public LinkedList<Pair<Map<String, Double>, Double>> listDustance(String distance) {
		
		// Compute the distances
		LinkedList<Pair<Map<String, Double>, Double>> newSampleList = new LinkedList<Pair<Map<String, Double>, Double>>();
		for (Pair<Map<String, Double>, Double> sample : finalList) 
			if (distance == "numAtt")
				newSampleList.add(new Pair<Map<String, Double>, Double> (sample.getKey(), numAttDistance(sample.getKey())));		
			else if (distance == "output")	
				newSampleList.add(new Pair<Map<String, Double>, Double> (sample.getKey(), outputDistance(sample.getValue())));
			else if (distance == "L1")
				newSampleList.add(new Pair<Map<String, Double>, Double> (sample.getKey(), l1Distance(sample.getKey())));
			else if (distance == "MAD")
				newSampleList.add(new Pair<Map<String, Double>, Double> (sample.getKey(), l1Distance(sample.getKey())));
		return newSampleList;
	}
	
	/**
	 * 
	 * @param sample
	 * @return the absolute difference between the dependent attribute value of sample and current world sample.
	 */
	public Double outputDistance (Double value) {
		Double d = getDouble(params.getCurrentInstance().get(de.classAttributeName()));
		return Math.abs(value - d);
	}
	
	/**
	 * 
	 * @param sample
	 * @return the L1 distance between sample and current world sample.
	 */
	public Double l1Distance (Map<String, Double> sample) {
		Map<String, Double> coeff = new HashMap<>();
		for (String attName : de.getAttributeTypes().keySet())
			coeff.put(attName, 1.0);
		
		return distance(sample, coeff);
	}
	
	/**
	 * 
	 * @param sample
	 * @return the MAD distance between sample and current world sample.
	 */
	public Double MADDistance (Map<String, Double> sample) {
		MADdistance mad = new MADdistance(de);
	
		return distance(sample, mad.getMADcoefficients());
	}
	
	/**
	 * For a given sample, computes the number of attributes that have a different value than the current world instance.
	 * @param sample
	 * @return
	 */
	private Double numAttDistance(Map<String, Double> sample) {
		double distance = 0.0;
		Map<String, Double> currentSample = params.getCurrentInstance();
		
		Set<String> attNames = getAllAttNames(sample);
		for (String attName : sample.keySet()) {
			if (!attName.equals(de.classAttributeName())) {
				if (!currentSample.containsKey(attName))
					distance = distance + 1;
				else if (sample.get(attName) != currentSample.get(attName))
					distance = distance + 1;
				
				attNames.remove(attName);
			}
			
		}
		
		return distance;
	}

	static class SampleComparator implements Comparator<Pair<Map<String, Double>, Double>> {
	     @Override
	     public int compare(Pair<Map<String, Double>, Double> o1, Pair<Map<String, Double>, Double> o2) {
	         double n1 = o1.getValue();
	         double n2 = o2.getValue();
	         
	         if (n1 == n2)
	        	 return 0;
	         if (n1 < n2)
	        	 return -1;
	         return 1;
	     }
	 }
	
	/**
	 * computes the distance of sample to the current sample using given metric.
	 * @param sample
	 * @param coefficients
	 * @return dist (sample, current world)
	 */
	private Double distance(Map<String, Double> sample, Map<String, Double> coefficients) {
		Map<String, Double> currentSample = getCurrentInstance();
		double distance = 0.0;
		
		// We may have some attributes that have value in just one of samples
		Set<String> attNames = getAllAttNames(sample);
		
		for (String attName : sample.keySet()) {
			if (currentSample.containsKey(attNames))
				distance = distance + net.sf.saxon.exslt.Math.abs(
						coefficients.get(attName)*(sample.get(attName) - currentSample.get(attName)));
			else
				distance = distance + net.sf.saxon.exslt.Math.abs(coefficients.get(attName) * sample.get(attName));
			attNames.remove(attName);
		}
		
		if (!attNames.isEmpty())
		for (String attName : attNames)
			if (sample.containsKey(attName)) 
				distance = distance + net.sf.saxon.exslt.Math.abs(
						coefficients.get(attName)*(sample.get(attName) - currentSample.get(attName)));
			else
				distance = distance + net.sf.saxon.exslt.Math.abs(coefficients.get(attName) * currentSample.get(attName));
		
		return distance;
	}
	
	/**
	 * Returns a set including all the attribute names in both current world sample and the given counterfactual sample.
	 * Note that we may have some attributes that have value in just one of samples
	 * @param sample
	 * @return A set including all the attribute names in both current world sample and the given counterfactual sample.
	 */
	private Set<String> getAllAttNames(Map<String, Double> sample) {
		Map<String, Double> currentSample = params.getCurrentInstance();
		Set<String> attNames = new HashSet<>();
				
		for (String attName : sample.keySet())
			 attNames.add(attName);
		
		for (String attName : currentSample.keySet())
			if (!attNames.contains(attName))
				attNames.add(attName);
		
		return attNames;
	}

	private Map<String, Boolean> getModifiableAtts() {
		Map<String, Boolean> isModifiable = new HashMap<String, Boolean>();
		
//		for (String attName : de.getAttributeTypes().keySet())
//			if (de.getAttributeTypes().get(attName).equals(Type.LITERAL))
//				for (String val : de.getLiteralValues(attName)) 
//					isModifiable.put(attName + " --> " + val, true); 
//			else
//				isModifiable.put(attName, true);
		
		for (String attName : params.getActionableAttNames())
			isModifiable.put(attName, true);
		
		for (String attName : de.getAttributeTypes().keySet())
			if (!isModifiable.containsKey(attName))
				isModifiable.put(attName, false);
		return isModifiable;
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

	/**
	 * 
	 * @return an instance in which literal values are one hot encoded.
	 */
	public Map<String, Double> getCurrentInstance() {
		return params.getCurrentInstance();
	}
	
	public boolean ifAttIsLiteral(String attName) {
		if (de.getAttributeTypes().get(attName).equals(Type.LITERAL))
			return true;
		
		return false;
	}
	
	public LinkedList<Pair<Map<String, Double>, Double>> getFinalList() {
		return finalList;
	}
	
	public DataExtraction getDataExtracrion()  {
		return de;
	}
	public CounterfactualParameters getParameters() {
		return params;
	}
	
	public int getNumCounterfactuals() {
		return params.getNumCounterfactuals();
	}
	
	public Map<Map<String, Double>, String> getSamplePattern() {
		return samplePattern;
	}
	
	//------------------- Evaluation Code ----------------------------------
	
	public void evaluate() {
		
		// open the file to save the results
		PrintWriter writer = null;
		PrintWriter writerSample = null;
		
		try {
			writer = new PrintWriter("resultCE.txt", "UTF-8");
			writerSample = new PrintWriter("resultWithSampleCE.txt", "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			System.out.println("fail to create the file");
			e.printStackTrace();
		}
		
		// SEM
		writer.println("SEM");
		writerSample.println("SEM");
		finalList = new LinkedList<Pair<Map<String, Double>, Double>>();
		params.setMethod(ClassificationMethod.SEM);
		finalList = getCounterfactualSamples(getDesirableListSamplesSEM(), params.getMetric());
		writeFinalList(writerSample);
		saveResults(writerSample, null);
		saveResults(writer, null);
		
/**		// REGTree
		finalList = new LinkedList<Pair<Map<String, Double>, Double>>();
		params.setMethod(ClassificationMethod.RT);
		int[] minNum = {2, 4, 8, 16};
		int[] maxDepth = {-1, 5, 10, 20, 40, 80};
		boolean[] noPrune = {true, false};
		for (int n : minNum)
			for (int d : maxDepth)
				for (boolean b : noPrune) {
					String setting = "RT, " + n + ", " + d + ", " + b;
					saveResults(writer, setting);
					saveResults(writerSample, setting);
					System.out.println(setting);
					
					finalList = new LinkedList<Pair<Map<String, Double>, Double>>();
					params.setMethod(ClassificationMethod.RT);
					params.setMaxDepth(d);
					params.setMinNum(n);
					params.setNoPruning(b);
					finalList = getCounterfactualSamples(getDesirableListSamplesClassifier(), params.getMetric());
					saveResults(writer, null);
					writeFinalList(writerSample);
					saveResults(writerSample, null);
					
				}
				
		// LWL
		int[] kernelType = {0, 1, 2}; 
		int[] knn = {-1, 2, 4, 8, 16, 32};
		for (int kt : kernelType)
			for (int k : knn) {
				String setting = "LWL, " + kt + ", " + k;
				saveResults(writer, setting);
				saveResults(writerSample, setting);
				System.out.println(setting);
				
				finalList = new LinkedList<Pair<Map<String, Double>, Double>>();
				params.setMethod(ClassificationMethod.LWL);
				params.setKernel(kt);
				params.setKNN(k);
				finalList = getCounterfactualSamples(getDesirableListSamplesClassifier(), params.getMetric());
				saveResults(writer, null);
				writeFinalList(writerSample);
				saveResults(writerSample, null);
			}
		 
		// NN
		
		double[] learningRate = {0.1, 0.2, 0.3, 0.4};
		double[] momentum = {0.1, 0.2, 0.3, 0.4};
		int[] numEpoches = {10, 20, 40, 100}; //, 200, 500, 1000, 2000};
//		String[] hiddenLayers = {"2", "4", "5", "8"};
		String[] hiddenLayers = {"i", "o", "t", "a"};
		
		for (double lr : learningRate)
			for (double m : momentum) 
				for (int ne : numEpoches) {
					for (String h : hiddenLayers) {
						String setting = "NN, " + lr + ", " + m + ", " + ne + ", " + h;
						saveResults(writer, setting);
						saveResults(writerSample, setting);
						System.out.println(setting);
						
						finalList = new LinkedList<Pair<Map<String, Double>, Double>>();
						params.setMethod(ClassificationMethod.NN);				
						params.setLearningRate(lr);
						params.setMomentum(m);
						params.setNumEpoches(ne);
						params.setHiddenLayers(h);
						finalList = getCounterfactualSamples(getDesirableListSamplesClassifier(), params.getMetric());
					//	saveResults(writer, null);
					}
					saveResults(writer, null);
					writeFinalList(writerSample);
					saveResults(writerSample, null);
				} */
		
		
		// this part is for the non linear case!
/**		String setting = "NN ";
		saveResults(writer, setting);
		saveResults(writerSample, setting);
		System.out.println(setting);
		
		finalList = new LinkedList<Pair<Map<String, Double>, Double>>();
		params.setMethod(ClassificationMethod.NN);				
		params.setLearningRate(0.3);
		params.setMomentum(0.1);
		params.setNumEpoches(1500);
		params.setHiddenLayers("16, 8");
		finalList = getCounterfactualSamples(getDesirableListSamplesClassifier(), params.getMetric());
		
		saveResults(writer, null);
		writeFinalList(writerSample);
		saveResults(writerSample, null); */
		
		//This part is for the linear case!
/**		params.setMethod(ClassificationMethod.RT);
		String setting = "RT " ;
		saveResults(writer, setting);
		saveResults(writerSample, setting);
		System.out.println(setting);
		
		finalList = new LinkedList<Pair<Map<String, Double>, Double>>();
		params.setMethod(ClassificationMethod.RT);
		params.setMaxDepth(-1);
		params.setMinNum(2);
		params.setNoPruning(true);
		finalList = getCounterfactualSamples(getDesirableListSamplesClassifier(), params.getMetric());
		saveResults(writer, null);
		writeFinalList(writerSample);
		saveResults(writerSample, null);   */
		
		writer.close();
		writerSample.close();
	}

	private void writeFinalList(PrintWriter writer) {
		for (Pair<Map<String, Double>, Double> sampleValue : finalList) {
			Map<String, Double> sample = sampleValue.getKey();
			String result = new String(); 
			for (String attName : sample.keySet())
				result = result + " " + attName + " " + sample.get(attName);
			writer.println(result);
		}
		
	}

	private void saveResults(PrintWriter writer, String setting) {
		if (setting != null) {
			writer.println(setting);
			return;
		}
		DecimalFormat df = new DecimalFormat("###.###");
		String result = new String();
		for (Pair<Map<String, Double>, Double> sampleValue : finalList) {
			result = result + " " + df.format(sampleValue.getValue()) + "," + sampleValueCSM.get(sampleValue.getKey()) + "," + numDiffAtts(sampleValue.getKey());
		}
		writer.println(result);
	}

	private int numDiffAtts(Map<String, Double> sample) {
		Map<String, Double> currentSample = params.getCurrentInstance();
		
		int num = 0;
		for (String attName : sample.keySet())
			if (sample.get(attName) != currentSample.get(attName))
				num++;
		
		return num;
	}
	
	public CSM getSEM() {
		return sem;
	}
	
	//---------------- testing several samples in batch --------------
	
	/**
	 * convert each line to an instance
	 * @param header
	 * @param line
	 * @return
	 */
	private Map<String, Object> getOneInstance(String[] header, String line) {
		Map<String, Object> instance = new HashMap<>();
		String[] parts = line.split(",");
		for (int i = 0; i < header.length; i++) {
			if (header[i].equals("CreditScore"))
				instance.put("O_Create_Offer_CreditScore", Double.valueOf(parts[i]));
			else if (header[i].equals("NumberOfTerms"))
				instance.put("O_Create_Offer_NumberOfTerms", Double.valueOf(parts[i]));
			else if (header[i].equals("MonthlyCost"))
				instance.put("O_Create_Offer_MonthlyCost", Double.valueOf(parts[i]));
			else if (header[i].equals("FirstWithdrawalAmount"))
				instance.put("O_Create_Offer_FirstWithdrawalAmount", Double.valueOf(parts[i]));
			else 
				instance.put(header[i], Double.valueOf(parts[i]));
		}
		instance.put("Selected", 0.0);
		return instance;
	}

	private void oneInstance() {
		rsg = new RandomSampleGenerator(params);
        rsg.setDesirableValue(params.getGoodResultNmericalThreshold());
   //     rsg.setMaxAcceptableDistance(100000000); //TODO need to ne according to the number of atts and metric distance
        rsg.setAttributeType(de.getAttributeTypes());
        
        rsg.setIfModifiable(getModifiableAtts());
        rsg.setTheDistributions();
 //     finalList = rsg.generateSamples(de.classAttributeName(), params.getNumCounterfactuals(), 1000000);
        
        rsg.generateRandomSamplesDifferentAtts(de.classAttributeName(), params.getNumCounterfactuals(), 10000);
        samplePattern = rsg.getSamplePattern();
        
//        params.setMethod(ClassificationMethod.RT);
        
        /**
         * This part is used for the evaluation results for the paper.
         */
//        evaluate();
//        evaluateCA();
        
        /**
         * End of paper evaluation part ;).
         */
     // If the method is SEM set the sem
        if (params.getMethod().equals( ClassificationMethod.SEM)) {
        	finalList = getCounterfactualSamples(getDesirableListSamplesSEM(), params.getMetric());
        } else 
        	finalList = getCounterfactualSamples(getDesirableListSamplesClassifier(), params.getMetric());
        	
        if (finalList.isEmpty())
    		System.out.println("empty List");
    	else
    		for (int i = 0; i < finalList.size(); i++) {
    			System.out.println("instance " + i + " --> " +finalList.get(i).getKey() + " " + de.classAttributeName() + " : " + finalList.get(i).getValue());
    	}
        
        
        if (params.doOptimization()) {
        	optimizeFinalList();
        }
	}
	
	public  CounterfactualParameters getParams() {
		return params;
	}
}
