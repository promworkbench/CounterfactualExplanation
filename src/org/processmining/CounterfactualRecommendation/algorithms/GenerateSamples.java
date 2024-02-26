package org.processmining.CounterfactualRecommendation.algorithms;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.processmining.datadiscovery.estimators.Type;

import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

/**
 * We are going to generate a sample set which is as close as possible 
 * to given sample r and at the current sample.
 * Optimization method ...
 * @author qafari
 *
 */
public class GenerateSamples {
	
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
	private Map<String, Double> MADcoefficients;
	
	public GenerateSamples(DataExtraction de, int n) {
		this.attributeTypes = de.getAttributeTypes();
		this.minMax = de.getMinMax();
		this.numSamples = n * n;
		generateSamples();
		
	}
	
	 static {
		    System.loadLibrary("jniortools");
		  }

	
	public LinkedList<Map<String, Object>> generateSamples() {

		generatRandomVariableNames();
		// Create the linear solver with the CBC back-end.
	    MPSolver solver = new MPSolver(
	        "sampleGenerator", MPSolver.OptimizationProblemType.CBC_MIXED_INTEGER_PROGRAMMING);
	    
	    double plusInfinity = java.lang.Double.POSITIVE_INFINITY;
	    double minusInfinity = java.lang.Double.NEGATIVE_INFINITY;
	    
	    // Defining integer and continuous variables. 
	    
	    // variables for new examples x_1,...,x_n and the their distance to a (d(a,x_i)=|a-x_i|) z_1,...z_n.
	    Map<String, MPVariable[]> variables = new HashMap<>();
	    Map<String, MPVariable[]> zVariables = new HashMap<>();
	    for (String attName : attNameClassRandVar.keySet()) {
	    	if (attributeTypes.get(attName).equals(Type.CONTINUOS)) {
	    		MPVariable[] numVars = new MPVariable[numSamples];
	    		MPVariable[] zNumVars = new MPVariable[numSamples];
	    		for (int i = 0; i < numSamples; i++) {
	    			double min = Double.valueOf(minMax.get(attName)[0].toString());
	    			double max = Double.valueOf(minMax.get(attName)[1].toString());
	    			numVars[i] = solver.makeNumVar(min, max, attName+i);
	    			zNumVars[i] = solver.makeNumVar(0, max-min, "z"+attName+i);
	    		}
	    		variables.put(attName, numVars);
	    		zVariables.put(attName, zNumVars);
	    	} else if (attributeTypes.get(attName).equals(Type.DISCRETE)) {
	    		MPVariable[] intVars = new MPVariable[numSamples];
	    		MPVariable[] zIntVars = new MPVariable[numSamples];
	    		for (int i = 0; i < numSamples; i++) {
	    			int min = (int)minMax.get(attName)[0];
	    			int max = (int)minMax.get(attName)[1];
	    			intVars[i] = solver.makeIntVar(min, max, attName+i);
	    			zIntVars[i] = solver.makeIntVar(0, max-min, "z"+attName+i);
	    		} 
	    		variables.put(attName, intVars);
	    		zVariables.put(attName, zIntVars);
	    	}	
	    }
	    
	 // variables for the distance between new examples x_1,...,x_n (d(x_i,x_j)=|x_i-x_j|) z_1_2,...z_(n-1)_n.
	    LinkedList<String> attNames = new LinkedList<>();
	    for (String attName : attNameClassRandVar.keySet()) 
	    	attNames.add(attName);
	    
	    Map<String, Map<Integer, Map<Integer, MPVariable>>> newSampleZVariables = new HashMap<>();
	    for (String attName : attNames) {
	    	Map<Integer, Map<Integer, MPVariable>> zVars = new HashMap<>();
	    	for (int i = 0; i < numSamples-1; i++) {
	    		Map<Integer, MPVariable> vars = new HashMap<>();
	    		for (int j = i+1; j < numSamples; j++) {
	    			if (attributeTypes.get(attName).equals(Type.CONTINUOS)) {
	    				double min = Double.valueOf(minMax.get(attName)[0].toString());
		    			double max = Double.valueOf(minMax.get(attName)[1].toString());
	    				vars.put(j, solver.makeNumVar(0, max-min, "z_"+attName+"_"+i+"_"+j));
	    			} else if (attributeTypes.get(attName).equals(Type.DISCRETE)) {
	    				int min = (int)minMax.get(attName)[0];
		    			int max = (int)minMax.get(attName)[1];
	    				vars.put(j, solver.makeIntVar(0, max-min, "z_"+attName+"_"+i+"_"+j));
	    			}
	    		}
	    		zVars.put(i, vars);
	    	}
	    	newSampleZVariables.put(attName, zVars);
	    }
	    
	    // set Constraints d(x,x_i)   f: a_j - x_j \leq z_j    s: -a_j _ x_j \leq z_j
	    Map<String, MPConstraint[]> firstConstraints = new HashMap<>();
	    Map<String, MPConstraint[]> secondConstraints = new HashMap<>();
	    for (String attName : attNameClassRandVar.keySet()) {
	    	MPConstraint[] firstTerm = new MPConstraint[numSamples];
	    	MPConstraint[] secondTerm = new MPConstraint[numSamples];
	    	for (int i = 0; i < numSamples; i++) {
	    		firstTerm[i] = solver.makeConstraint(Double.valueOf(mainSample.get(attName).toString()), plusInfinity, "C_f_"+attName+"_"+i);
	    		firstTerm[i].setCoefficient(variables.get(attName)[i], 1);
	    		firstTerm[i].setCoefficient(zVariables.get(attName)[i], 1);
	    		System.out.println(firstTerm[i].name()+" : "+ Double.valueOf(mainSample.get(attName).toString())+" <= "+variables.get(attName)[i].name()+" + "+ zVariables.get(attName)[i].name() + " <= +inf");
	    		secondTerm[i] = solver.makeConstraint(minusInfinity, Double.valueOf(mainSample.get(attName).toString()), "C_s_"+attName+"_"+i);
	    		secondTerm[i].setCoefficient(variables.get(attName)[i], 1);
	    		secondTerm[i].setCoefficient(zVariables.get(attName)[i], -1);
	    		System.out.println(secondTerm[i].name()+" : -inf <= "+variables.get(attName)[i].name()+" - "+ zVariables.get(attName)[i].name() + " <= "+ Double.valueOf(mainSample.get(attName).toString()));
	    		
	    	}
	    	firstConstraints.put(attName, firstTerm);
	    	secondConstraints.put(attName, secondTerm);
	    }
	   
	    // set Constraints d(x_i,x_j)   f: z_i - z_j - z_ij \leq 0   s: -z_i + z_j - z_ij \leq 0
	    Map<String, Map<Integer, Map<Integer, MPConstraint>>> newSampleConsFirst = new HashMap<>();
	    Map<String, Map<Integer, Map<Integer, MPConstraint>>> newSampleConsSecond = new HashMap<>();
	    for (String attName : attNames) {
	    	Map<Integer, Map<Integer, MPConstraint>> zCons1 = new HashMap<>();
	    	Map<Integer, Map<Integer, MPConstraint>> zCons2 = new HashMap<>();
	    	for (int i = 0; i < numSamples-1; i++) {
	    		Map<Integer, MPConstraint> cons1 = new HashMap<>();
	    		Map<Integer, MPConstraint> cons2 = new HashMap<>();
	    		for (int j = i+1; j < numSamples; j++) {
	    			MPConstraint c1 = solver.makeConstraint(minusInfinity, 0, "C_f_i_j_"+attName+"_"+i+"_"+j);
	    			c1.setCoefficient(variables.get(attName)[i], 1);
		    		c1.setCoefficient(variables.get(attName)[j], -1);
		    		c1.setCoefficient(newSampleZVariables.get(attName).get(i).get(j),-1);
		    		cons1.put(j, c1);
		    		System.out.println(c1.name()+" : -inf <= "+variables.get(attName)[i].name()+" - "+ variables.get(attName)[j].name() +" - "+newSampleZVariables.get(attName).get(i).get(j).name()+ " <= 0");
		    		MPConstraint c2 = solver.makeConstraint(minusInfinity, 0, "C_s_i_j_"+attName+"_"+i+"_"+j);
	    			c2.setCoefficient(variables.get(attName)[i], -1);
		    		c2.setCoefficient(variables.get(attName)[j], 1);
		    		c2.setCoefficient(newSampleZVariables.get(attName).get(i).get(j),-1);
		    		cons1.put(j, c2);
		    		System.out.println(c2.name()+" : -inf <= -"+variables.get(attName)[i].name()+" + "+ variables.get(attName)[j].name() +" - "+newSampleZVariables.get(attName).get(i).get(j).name()+ " <= 0");
	    		}
	    		zCons1.put(i, cons1);
	    		zCons2.put(i, cons2);
	    	}
	    	newSampleConsFirst.put(attName, zCons1);
	    	newSampleConsSecond.put(attName, zCons2);
	    }
	    
	    // set objective function 
	    // coefficients of z variables are -1 and the coefficients of z_ij variables are 1
	    MPObjective objective = solver.objective();
	    for (String attName : attNames) {
	    	for (int i = 0; i < numSamples; i++) {
	    		if (MADcoefficients == null) {
	    			objective.setCoefficient(zVariables.get(attName)[i], -1);
	    		} else {
	    			objective.setCoefficient(zVariables.get(attName)[i], -1 * MADcoefficients.get(attName));
	    		}
	    	}
	    	for (int i = 0; i < numSamples-1; i++) {
	    		for (int j = i+1; j < numSamples; j++) {
	    			if (MADcoefficients == null) {
	    				objective.setCoefficient(newSampleZVariables.get(attName).get(i).get(j), 1);
	    			} else {
	    				objective.setCoefficient(newSampleZVariables.get(attName).get(i).get(j), MADcoefficients.get(attName));
	    			}
	    		}
	    	}
	    }
	    objective.setMaximization();
	    
	    System.out.println("Coefficients: ");
	    for (String attName : attNames) {
	    	for (int i = 0; i < numSamples; i++) 
	    		System.out.println(objective.getCoefficient(zVariables.get(attName)[i])+ " "+zVariables.get(attName)[i].name());
	    	for (int i = 0; i < numSamples-1; i++) {
	    		for (int j = i+1; j < numSamples; j++) {
	    			System.out.println(objective.getCoefficient(newSampleZVariables.get(attName).get(i).get(j))+ " "+newSampleZVariables.get(attName).get(i).get(j).name());
	    		}
	    	}
	    }
//	    for (String attName : attNames) 
//	    	System.out.println("--> : "+zVariables.get(attName)[0].name());

	    final MPSolver.ResultStatus resultStatus = solver.solve();
	    
	    MPVariable[] vs = solver.variables();
	    System.out.println("Variables:");
	    for (MPVariable v : vs)
	    	System.out.println("var : "+ v.name()+ " lb : "+ v.lb()+ " ub : "+ v.ub());
	    
	    MPConstraint[] cs = solver.constraints();
	    for (MPConstraint c : cs)
	    	System.out.println("cons : "+ c.name()+ " lb : "+ c.lb()+ " ub : "+ c.ub());
	    
	    System.out.println("************************\n\n" );
	    //generate the samples
	    LinkedList<Map<String, Object>> samples = new LinkedList<>();
	    if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
		      System.out.println("Solution:");
		      System.out.println("Objective value = " + objective.value());
		      
		      for (int i = 0; i < numSamples; i++) {
		    	  Map<String, Object> sample = new HashMap<>();
		    	  for (String attName : attNames) {
			    		System.out.println(variables.get(attName)[i].name() + " : " + variables.get(attName)[i].solutionValue());
			    		sample.put(attName, variables.get(attName)[i].solutionValue());
			    	}
		    	  samples.add(sample);
		      }
		      
		      System.out.println("\n###############6666");
		      for (int i = 0; i < numSamples; i++) {
		    	  for (String attName : attNames) {
			    		System.out.println(zVariables.get(attName)[i].name() + " : " + zVariables.get(attName)[i].solutionValue());
			    	}
		      }
		      
		      System.out.println("\n###############");
		      for (String attName : attNames) 
			    	for (int i = 0; i < numSamples-1; i++) 
			    		for (int j = i+1; j < numSamples; j++) 
			    			System.out.println(newSampleZVariables.get(attName).get(i).get(j).name() + " : " + newSampleZVariables.get(attName).get(i).get(j).solutionValue());
		      
		      System.out.println("\nAdvanced usage:");
		      System.out.println("Problem solved in " + solver.wallTime() + " milliseconds");
		      System.out.println("Problem solved in " + solver.iterations() + " iterations");
		      System.out.println("Problem solved in " + solver.nodes() + " branch-and-bound nodes");
		    } else {
		      System.err.println("The problem does not have an optimal solution!");
		    }
	    
	    return samples;
	}

	private void generatRandomVariableNames() {
		attNameClassRandVar = new HashMap<String, String>();
		attNameRandVars = new HashMap<String, String[]>();
		
		for (String attName : attributeTypes.keySet()) {
			int i = 0;
			Type type = attributeTypes.get(attName);
			if (type.equals(Type.CONTINUOS) || type.equals(Type.DISCRETE)) {
				attNameClassRandVar.put(attName, "X"+i);
				i++;
			}
				
		}
	}
	
	public void setMainSample(Map<String, Object> sample) {
		mainSample = sample;
	}
	
	public void setMADcoefficients(Map<String, Double> c) {
		MADcoefficients = c;
	}
	
	//***************************** TEST ********************************
	
	public void setTypes(Map<String, Type> types) {
		attributeTypes = types;
	}
	
	public void setMinMax(Map<String, Object[]> m) {
		minMax = m;
	}
	
	public GenerateSamples(int n) {
		this.numSamples = n;
	}
	
	public void set5variables() {
		Map<String, Type> types = new HashMap<>();
		types.put("H", Type.DISCRETE);
		types.put("P", Type.DISCRETE);
		types.put("NP", Type.DISCRETE);
		types.put("PBD", Type.CONTINUOS);
		types.put("IPD", Type.CONTINUOS);
		setTypes(types);
		
		Map<String, Object> sample = new HashMap<>();
		sample.put("H", 5);
		sample.put("P", 2);
		sample.put("NP", 42);
		sample.put("PBD", 71.0);
		sample.put("IPD", 577.0);
		setMainSample(sample);
		
		Map<String, Double> mad = new HashMap<>();
		mad.put("H", 0.3);
		mad.put("P", 0.5);
		mad.put("NP", 0.1);
		mad.put("PBD", 0.05);
		mad.put("IPD", 0.01);
		setMADcoefficients(mad);
		
		Map<String, Object[]> mm = new HashMap<>();
		Object[] o1 = {1, 10};
		mm.put("H", o1);
		Object[] o2 = {1, 3};
		mm.put("P", o2);
		Object[] o3 = {1, 100};
		mm.put("NP", o3);
		Object[] o4 = {1, 500};
		mm.put("PBD", o4);
		Object[] o5 = {1, 1000};
		mm.put("IPD", o5);
		setMinMax(mm);
	}
	
	public void set3variables() {
		Map<String, Type> types = new HashMap<>();
		types.put("Q", Type.DISCRETE);
		types.put("P", Type.DISCRETE);
		types.put("S", Type.CONTINUOS);
		setTypes(types);
		
		Map<String, Object> sample = new HashMap<>();
		sample.put("Q", 5);
		sample.put("P", 2);
		sample.put("S", 3.0);
		setMainSample(sample);
		
		Map<String, Double> mad = new HashMap<>();
		mad.put("Q", 1.0);
		mad.put("P", 1.0);
		mad.put("S", 1.0);
		setMADcoefficients(mad);
		
		Map<String, Object[]> mm = new HashMap<>();
		Object[] o1 = {1, 10};
		mm.put("Q", o1);
		Object[] o2 = {1, 3};
		mm.put("P", o2);
		Object[] o5 = {1, 5};
		mm.put("S", o5);
		setMinMax(mm);
	}
	
	public static void main(String[] args) {
		GenerateSamples g = new GenerateSamples(4);
		g.set3variables();
		
		g.generateSamples();
        System.out.println("This will be printed");
    }
	
}
