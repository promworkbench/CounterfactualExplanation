package org.processmining.CounterfactualRecommendation.algorithms;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.processmining.datadiscovery.estimators.Type;

/**
 * We are going to generate a sample set which is as close as possible 
 * to given sample r and at the current sample.
 * @author qafari
 *
 */
public class RandomSampling {
	
	/**
	 * for each attribute name we have an array of Strings of random
	 * Variables that correspond to that attribute name. If we need to
	 * generate n samples then the length of each array is equal n^2.
	 */
	private Map<String, String[]> attNameRandVars;
	private Map<String, String> attNameClassRandVar;
	private Map<String, Type> attributeTypes;
	private Map<String, Object[]> minMax;
	private int numSamples;
	private Map<String, Object> mainSample;
	private DataExtraction de;
	private Set<String> effectiveAttNames; // the set of attribute names that have a causal effect
	//on the dependent attribute
	
	public RandomSampling(DataExtraction de, int n) {
		this.attributeTypes = de.getAttributeTypes();
		this.minMax = de.getMinMax();
		this.numSamples = n * n;
		this.de = de;		
	}
	
	public LinkedList<Map<String, Object>> generateSamples() {
		
		LinkedList<Map<String, Object>> samples = new LinkedList();
		for (int i = 0; i <= numSamples; i++) {
			samples.add(generateOneSample());
		}
	    
	    return samples;
	}

	private Map<String, Object> generateOneSample() {
		Map<String, Object> sample = new HashMap();
		for (String attName : effectiveAttNames) {
			if (attributeTypes.get(attName).equals(Type.CONTINUOS)) {
				Random r = new Random();
				double randomValue = (double) minMax.get(attName)[0] + ((double)minMax.get(attName)[1] - (double)minMax.get(attName)[0]) * r.nextDouble();
				sample.put(attName, randomValue);
			} else if (attributeTypes.get(attName).equals(Type.DISCRETE)) {
				Random r = new Random();
				int randomValue = (int) minMax.get(attName)[0] + ((int)minMax.get(attName)[1] - (int)minMax.get(attName)[0]) * r.nextInt();
				sample.put(attName, randomValue);
			} else if (attributeTypes.get(attName).equals(Type.CONTINUOS)) { 
				Random r = new Random();
				double randomValue = (double) minMax.get(attName)[0] + ((double)minMax.get(attName)[1] - (double)minMax.get(attName)[0]) * r.nextDouble();
				sample.put(attName, Math.round(randomValue));
			} else if (attributeTypes.get(attName).equals(Type.BOOLEAN) || attributeTypes.get(attName).equals(Type.LITERAL)) {
				Random r = new Random();
				int randomValue = 0 +  de.getLiteralValuesNDC().get(attName).size() * r.nextInt();
				sample.put(attName, randomValue);
			}
				
		}
		return sample;
	}
	
	public void setEffectiveAttNames(Set<String> set) {
		effectiveAttNames = set;
	}
	
	//***************************** TEST ********************************

}
