package org.processmining.CounterfactualRecommendation.algorithms;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.processmining.CounterfactualRecommendation.parameters.CounterfactualParameters;
import org.processmining.datadiscovery.estimators.Type;

import javafx.util.Pair;

/**
 * Here we are going to generate n samples that are close to a given sample x.
 * We prefer to generate random samples that are diverse and close to x.
 * for each variable v of x, we drive for each sample from a 
 * 	- if the maximum acceptable difference of the instances is active and lower than 
 * 	  the maximum distance between the current value and the maximum possible value of
 * 	  variable v then
 * 		normal distribution with mean the current value and std-deviation 
 * 		of half of the maximum acceptable distance.
 * 	    for example if v = 10 and 0 <= v <= 100 and maximum acceptable distance between
 * 		samples is 22, then we use normal distribution with mean = 10 and std-deviation = 11. 
 * 	- else
 *		uniform distribution with mean the current value of v.
 *		for example, if v = 10 and  0 <= v <= 100 and maximum acceptable distance between
 * 		samples is 5, then we use uniform distribution over [5, 15].
 * @author qafari
 *
 */
public class RandomSampleGenerator {
	
	/**
	 * The maximum acceptable distance to the current instance.
	 * 0 value means that no distance limit has been set.
	 */
	private int maxAcceptableDistance = 0; 
	
	/**
	 * the number of samples
	 */
	private int numSamples;
	
	/**
	 * the variable that we want to see how its value change in the counterfactual situations
	 */	
	private String targetVariable;
	
	/**
	 * the desirable value of the targetVariable.
	 */
	private double desirableValue;
	
	/**
	 * the distribution that each modifiable variable is taken from
	 * <varName, DistributionInfo>
	 * distributionInfo is of form double[2] in which
	 * 		distributionInfo[0] is the mean (the value of variable in the 
	 * 			given sample, currentWorld) if the distribution is normal and 0 o.w.
	 * 		distributionInfo[1] is the std-deviation if the distribution is normal and 0 o.w.
	 * 		distributionInfo[2] = a if the distribution is uniform(a, b)
	 * 		distributionInfo[3] = b if the distribution is uniform(a, b) 
	 */
	private Map<String, Double[]> distributionsInfo;
	
	/**
	 * Indicate the type of the attributes. 
	 */
	private Map<String, Type> attributeTypes;
	
	/**
	 * for each desirable sample in the final list which attribute has been changed;
	 * the pattern is a string of 0 and 1s that 1 means that attribute value has been changed.
	 */
	private Map<Map<String, Double>, String> samplePattern;
	
	/**
	 * true if the value of the variable can be intervened and false o.w.
	 */
	private Map<String, Boolean> ifModifiable;
	
	/**
	 * a list of random samples.
	 */
	private Map<String, LinkedList<Map<String, Double>>> patternSamoleListMap;
	
	/**
	 * The parameters for generating samples
	 */
	CounterfactualParameters params;
	
	public RandomSampleGenerator(CounterfactualParameters pms) {
		params = pms;
	}
	
	public void setNumberOfSamples(int n) {
		this.numSamples = n;
	}
	
	public void setMaxAcceptableDistance(int d) {
		maxAcceptableDistance = d;
	}
	
	public void setDesirableValue(double v) {
		desirableValue = v;
	}
	
	public void setIfModifiable(Map<String, Boolean> map) {
		ifModifiable = map;

	}
	
	public Map<Map<String, Double>, String> getSamplePattern() {
		return samplePattern;
	}
	
	/**
	 * 
	 * set the distribution for each attribute. 
	 * If the sample does not have a value for that attribute, just set the distribution to Uniform [min, max]
	 * If no maximum acceptable distance is set, then set all the distributions to Uniform [min, max]
	 * If the maximum acceptable distance, d, is set, then
	 * 			- if d < max {value(att) - min , value(att) - max} then set the distribution to normal with mean = value(att) and 
	 * 				SD = maximum acceptable distance/2.
	 * 			- if d >= max {value(att) - min , value(att) - max} then set the distribution to Uniform [min, max]
	 */
	public void setTheDistributions() {
		distributionsInfo = new HashMap<String, Double[]>();
		Set<String> varNames = params.getActionableAttNames();
		System.out.println(params.getDataExtraction().getMinMax());
		//if there are no distance restriction
		if (maxAcceptableDistance == 0) {
			for (String varName : varNames)
				if (ifModifiable.get(varName) == true) {
					Double[] uniformDist = new Double[4];
					uniformDist[0] = 0.0;
					uniformDist[1] = 0.0;
					System.out.println(varName);
					uniformDist[2] = getDouble(params.getDataExtraction().getMinMax().get(varName)[0]);
					double m = getDouble(params.getDataExtraction().getMinMax().get(varName)[1]);
					if (m > 999999999d)/////////////
						m = 999999999d;/////////////////////
					uniformDist[3] = m;
					uniformDist[3] = getDouble(params.getDataExtraction().getMinMax().get(varName)[1]);
		//			distributionsInfo.put(varName, uniformDist);
				}
		}
		
		// else
		for (String varName : varNames) {
			if (ifModifiable.get(varName) == true) {
				Double[] distInfo = new Double[4];
				if (!params.getCurrentInstance().containsKey(varName)) {
					distInfo[0] = 0.0;
					distInfo[1] = 0.0;
					distInfo[2] = getDouble(params.getDataExtraction().getMinMax().get(varName)[0]);
					distInfo[3] = getDouble(params.getDataExtraction().getMinMax().get(varName)[1]);
				}
				else {
					double m = getDouble(params.getDataExtraction().getMinMax().get(varName)[1]);
					if (m > 999999999d)/////////////
						m = 999999999d;/////////////////////
					double distanceMax = Math.abs(m-params.getCurrentInstance().get(varName));
					double distanceMin = Math.abs(getDouble(params.getDataExtraction().getMinMax().get(varName)[0])-params.getCurrentInstance().get(varName));
					double maxDist = Math.max(distanceMax, distanceMin);
					if (maxDist <= maxAcceptableDistance) {
						distInfo[0] = 0.0;
						distInfo[1] = 0.0;
						distInfo[2] = getDouble(params.getDataExtraction().getMinMax().get(varName)[0]);
						distInfo[3] = getDouble(params.getDataExtraction().getMinMax().get(varName)[1]);
					} else {
						distInfo[0] = params.getCurrentInstance().get(varName);
						distInfo[1] = Math.ceil(maxAcceptableDistance/2.0);
						distInfo[2] = getDouble(params.getDataExtraction().getMinMax().get(varName)[0]);
				//		distInfo[3] = getDouble(params.getDataExtraction().getMinMax().get(varName)[1]);
						distInfo[3] = m;
					}
				}
				distributionsInfo.put(varName, distInfo);
			}
		}
	}
	
	
	public Map<String, Double> generateOneCFW(int i) {
		Map<String, Double> sample = new HashMap<String, Double>();
		Random rnd = new Random();
		if (i == 0) {
			for (String varName : distributionsInfo.keySet()) {
				double mean = distributionsInfo.get(varName)[0];
				double stdDev = distributionsInfo.get(varName)[1];
				double min = distributionsInfo.get(varName)[2];
				double max = distributionsInfo.get(varName)[3];
				if (stdDev == 0)
					sample.put(varName, min + (max - min) * rnd.nextDouble()); 
				else 
					sample.put(varName, (rnd.nextGaussian() * stdDev + mean));
			}
		} else {
			int size = params.getDataExtraction().getInstancesOfNDC().size();
			for (String varName : distributionsInfo.keySet()) {
				int idx = rnd.nextInt(size);
				if (params.getDataExtraction().getInstancesOfNDC().get(idx).containsKey(varName)) {
					sample.put(varName, getDouble(params.getDataExtraction().getInstancesOfNDC().get(idx).get(varName)));
				} else {
					double mean = distributionsInfo.get(varName)[0];
					double stdDev = distributionsInfo.get(varName)[1];
					double min = distributionsInfo.get(varName)[2];
					double max = distributionsInfo.get(varName)[3];
					if (stdDev == 0)
						sample.put(varName, min + (max - min) * rnd.nextDouble()); 
					else 
						sample.put(varName, (rnd.nextGaussian() * stdDev + mean));
				}
				
			}
		}
		
		
		sample = trimAttValues(sample);
		return sample;
	}
	
	/**
	 * Removing the extra precision for the double values assigned to the discrete attributes in a sample.
	 * @param sample
	 * @return
	 */
	public Map<String, Double> trimAttValues(Map<String, Double> sample) {
		Map<String, Double> newSample = new HashMap<String, Double>();
		for (String attName : sample.keySet()) {
			if (!attributeTypes.get(attName).equals(Type.CONTINUOS)) {
				Double d = round(sample.get(attName), 0);
				newSample.put(attName, d);
			} else 
				newSample.put(attName, sample.get(attName));
		}
		
		return newSample;
	}
	/**
	 * Check if all attributes satisfy min <= value(att) <= max
	 * @param sample
	 * @return
	 */
	public boolean isAValidCFW(Map<String, Double> sample) {
		// if it tries to change an unmodifiable variable return false.
		for (String str : sample.keySet())
			if (ifModifiable.get(str) == false)
				return false;
		
		for (String varName : sample.keySet())
			if (!isInRange(varName, sample.get(varName)))
				return false;
		
		return true;
	}
	
	public boolean isInRange(String varName, double value) {
		if (value <= distributionsInfo.get(varName)[3] && 
				value >= distributionsInfo.get(varName)[2])
			return true;
		
		return false;
	}
	
	/**
	 * round a double to the precision of the number of decimal digits that is mentioned (parameter place).
	 * @param value
	 * @param places
	 * @return
	 */
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = BigDecimal.valueOf(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	/**
	/**
	 * Generates a large number of random samples.
	 * @param targetVarName
	 * @param numSample
	 * @param maxIteration
	 * @return 
	 
	public LinkedList<Pair<Map<String, Double>, Double>> generateSamples(String targetVarName, int numSample, int maxIteration) {
		LinkedList<Pair<Map<String, Double>, Double>> desirableSamples = new LinkedList<Pair<Map<String, Double>, Double>>();
		int num = 0;
		for (int i = 0; i < maxIteration; i++) {
			Map<String, Double> sample = generateOneCFW();
			if (isAValidCFW(sample)) {
				sem.setCounterfactualWorld(sample);
				double value = sem.computeCounterfactualValue(targetVarName, sample);
				if ((ifLowerIsDesirable && value <= desirableValue) || (!ifLowerIsDesirable && value > desirableValue)) {
					Pair<Map<String, Double>, Double> p = new Pair(sample, value);
					desirableSamples.add(p);
					num++;
				}
				if (num == numSample)
					break;
			}
		} 
		
		// Sort the good counterfactual samples according to the difference of their dependent values
		// and the original one
		Collections.sort(desirableSamples, new Comparator<Pair<Map<String, Double>, Double>>() {
		     @Override
		     public int compare(Pair<Map<String, Double>, Double> o1, Pair<Map<String, Double>, Double> o2) {
		         double n1 = 0;
		         Map<String, Double> m1 = o1.getKey();
		         for (String s : m1.keySet())
		        	 n1 = n1 + Math.abs(m1.get(s));
		         double n2 = 0;
		         Map<String, Double> m2 = o2.getKey();
		         for (String s : m2.keySet())
		        	 n2 = n2 + Math.abs(m2.get(s));
		         
		         if (n1 == n2)
		        	 return 0;
		         if (n1 < n2)
		        	 return -1;
		         return 1;
		     }
		 });
		
		return desirableSamples;
	} */
	
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
	} */
	
	/**
	 * Generates a large number of random samples.
	 * @param targetVarName
	 * @param numSample
	 * @param maxIteration
	 * @return 
	 */
	public void generateRandomSamplesDifferentAtts(String targetVarName, int numSample, int maxIteration) {
		samplePattern = new HashMap<Map<String, Double>, String>();
		targetVariable = targetVarName;
		
		// Pattern 0..0 is useless.
		double numConfigs =  Math.pow(2.0, params.getActionableAttNames().size() ) - 1;
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
		
		patternSamoleListMap = new HashMap<>();
		
		for (int itr = 0; itr < numConfigs; itr++) {
			// Class att should not change.
	//		if (patterns[itr].charAt(classVarIndex) == '0') {
				setAttsToChangeValue(patterns[itr]);
				setTheDistributions();
				System.out.println(patterns[itr]);
				LinkedList<Map<String, Double>> sampleList = new LinkedList<>();
				for (int i = 0; i < numIteration; i++) {
					Map<String, Double> sample = generateOneCFW(i/2);
					if (isAValidCFW(sample)) {
						sampleList.add(sample);
						samplePattern.put(sample, patterns[itr]);///////////////////////////////
					}
				} 
				if(!sampleList.isEmpty())
					patternSamoleListMap.put(patterns[itr], sampleList);
	//		}
		}
	}
	
	private boolean allListsAreEmpty(Map<String, LinkedList<Pair<Map<String, Double>, Double>>> patternSamoleListMap) {
		if (patternSamoleListMap.isEmpty())
			return false;
		
		for (String pattern : patternSamoleListMap.keySet())
			if (!patternSamoleListMap.get(pattern).isEmpty())
				return false;
		
		return true;
	}

	private String[] setPatterns(double numConfigs) {
		String[] patterns = new String[(int) numConfigs];
		for (int i = 1; i <= numConfigs; i++) {
			patterns[i-1] = (Integer.toBinaryString(i)).toString();
			if (patterns[i-1].length() < params.getActionableAttNames().size()) {
				int l = params.getActionableAttNames().size() - patterns[i-1].length();
				for (int j = 0; j < l; j++)
					patterns[i-1] = "0" + patterns[i-1];
			}
		}
		
		bubbleSort(patterns);
		
		return patterns;
	}
	
	public void bubbleSort(String[] array) {
	    boolean swapped = true;
	    int j = 0;
	    String tmp;
	    while (swapped) {
	        swapped = false;
	        j++;
	        for (int i = 0; i < array.length - j; i++) {
	            if (!isSmallerThan(array[i] , array[i + 1])) {
	                tmp = array[i];
	                array[i] = array[i + 1];
	                array[i + 1] = tmp;
	                swapped = true;
	            }
	        }
	    }
	}
	
	public boolean isSmallerThan(String s1, String s2) {
		int n1 = 0;
		int n2 = 0;
		for (int i = 0; i < s1.length(); i++) {
			if (s1.charAt(i) == '1')
				n1++;
			if (s2.charAt(i) == '1')
				n2++;
		}
		
		if (n1 < n2)
			return true;
		
		return false;
	}

	/**
	 * given a pattern of 0 and 1s, it sets the ifModifiable such that
	 *       isModifialble att == false if its position is 0 and true if it is 1.
	 *       
	 *       example:
	 *       
	 *       00110   --> pattern for actionable attributes
	 *       att0 = false
	 *       att1 = true
	 *       att2 = true 
	 *       att3 = false
	 *       att4 = false
	 * @param itr
	 */
	private void setAttsToChangeValue(String pattern) {
		String[] attNames = new String[params.getActionableAttNames().size()];
		int i = 0;
		for (String attName : params.getActionableAttNames()) {
			attNames[i] = attName;
			i++;
		}
		
		Map<String, Boolean> map = new HashMap<>();
		for (int j = 0; j < pattern.length(); j++)
			if (pattern.charAt(j) == '1')
				map.put(attNames[j], true);
			else
				map.put(attNames[j], false);
		
		for (String attName : attributeTypes.keySet())
			if (!map.keySet().contains(attName))
				map.put(attName, false);
		
		setIfModifiable(map);
		
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
	
	public void setAttributeType(Map<String, Type> attributeTypes) {
		this.attributeTypes = attributeTypes;
		
	}
	
	public  Map<String, LinkedList<Map<String, Double>>> getRandomSamples() {
		return patternSamoleListMap;
	}
	//************************** Testing ***************************//
	
	public void setAttTypeForTest() {
		Map<String, Type> types = new HashMap();
		types.put("C", Type.DISCRETE);
		types.put("NT", Type.DISCRETE);
		types.put("P", Type.DISCRETE);
		types.put("PBD", Type.CONTINUOS);
		types.put("IPD", Type.CONTINUOS);
		
		attributeTypes =types;
	}
	
	/**	public static void main(String[] args) {
	        LinkedList<String> s = new LinkedList<String>();
	        s.add("#C# = noise");
	        s.add("#P# = noise");
	        s.add("#PBD# = #C# * 10 + noise ");
	        s.add("#NT# = 5 * #C# + 3 * #P# + noise");
	        s.add("#IPD# = 50 * #C# + 5 * #NT# + noise");
	        
	        CSM csm = new CSM(s);
	        csm.setUpCSM(csm);
	        
	        RandomSampleGenerator rsg = new RandomSampleGenerator(csm);
	        rsg.setDesirableValue(580);
	        rsg.setMaxAcceptableDistance(100000000);
	        rsg.setIfLowerIsdesirable(false);
	        rsg.setAttTypeForTest();
	        Map<String, Boolean> map = new HashMap<String, Boolean>();
	        map.put("C", false);
	        map.put("P", true);
	        map.put("PBD", true);
	        map.put("NT", true);
	        map.put("IPD", true);
	        rsg.setIfModifiable(map);
	        rsg.setTheDistributions();
	        LinkedList<Pair<Map<String, Double>, Double>> cfws = rsg.generateSamples("IPD", 10, 1000);
	        for (int i = 0; i < cfws.size(); i++) {
	        	System.out.println("CF" + i + " --> " +cfws.get(i).getKey() + " IPD : " + cfws.get(i).getValue());
	        }
		} */

}
