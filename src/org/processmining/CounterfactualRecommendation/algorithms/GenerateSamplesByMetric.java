package org.processmining.CounterfactualRecommendation.algorithms;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.processmining.datadiscovery.estimators.Type;

import com.google.ortools.linearsolver.MPSolver;

public class GenerateSamplesByMetric {
	
		private Map<String, String[]> attNameRandVars;
		private Map<String, String> attNameClassRandVar;
		private Map<String, Type> attributeTypes;
		private Map<String, Object[]> minMax;
		private int numSamples;
		private Map<String, Object> mainSample;
		private Map<String, Double> MADcoefficients;
		private Set<String> effectiveAttNames;
		private String sem;
		
	public GenerateSamplesByMetric(DataExtraction de, int n) {
		this.attributeTypes = de.getAttributeTypes();
		this.minMax = de.getMinMax();
		this.numSamples = n * n;
	}
	
	/**
	 * 
	 * @return the set of counterfactual examples with a desirable result
	 */
	public LinkedList<Map<String, Object>> generateSamples() {
		LinkedList<Map<String, Object>> samples = new LinkedList<Map<String, Object>>();
		
		for (int i = 1; i <= effectiveAttNames.size(); i++)
			addSamples(itoa(i, 2));
		
		return samples;
	}
	
	public void addSamples(String pattern) {
		
		 MPSolver solver = new MPSolver(
			        "sampleGenerator", MPSolver.OptimizationProblemType.CBC_MIXED_INTEGER_PROGRAMMING);
			    
		for (int i = 0; i < pattern.length(); i++) {
			if (pattern.charAt(i) == '1') {
				
			}
		}
	}
		
	/**
	 * turn an integer to the string of its binary representation.
	 * @param x
	 * @param base
	 * @return
	 */
	static String itoa(int x,  int base) 
	{ 
		boolean negative = false; 
		String s = ""; 
		if (x == 0) 
			return "0"; 
		negative = (x < 0); 
		if (negative) 
			x = -1 * x; 
		while (x != 0)  { 
			s = (x % base) + s;  
			x = x / base;  
		} 
		if (negative) 
			s = "-" + s; 
		return s; 
	} 
}
