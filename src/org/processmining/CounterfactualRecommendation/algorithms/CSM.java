package org.processmining.CounterfactualRecommendation.algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.mariuszgromada.math.mxparser.Expression;
/**
 * A CSM is the system of equation model (a set of equations) that demonstrate 
 * the structural causal model between different variables in the process.
 * Given a situation (the values of variables in a specific sample (world) and its result)
 * and a variable that we want to see its value in an counterfactual situation, 
 * this class evaluates the result of that situation in a conterfactual world (sample)
 * where the value of those variable were set to the new ones.
 * 
 * Note :
 * 		Here we just consider additive noises.
 * 		Noise term in each equation is the last term and is defined by the word "noise".
 * 		Each variable in each equation is surrounded by two @ symbol.
 * 
 * Note that all variables have to be continuous.
 * @author qafari
 *
 */
public class CSM {
	private Set<String> varNames;	
	
	/**
	 * the minimum [0] and maximum [1] value that a variable can take
	 */
	private Map<String, Double[]> minMax;
	
	/**
	 * The list of equations in the SEM
	 */
	private LinkedList<String> sem;
	
	/**
	 * each key is the name of a variable and its 
	 * value indicate how many steps are the difference
	 * between the variable values in the real and 
	 * counterfactual world.
	 */
	private Map<String, Double> counterfactualWorld;
	
	/**
	 * each key is the name of a variable and its 
	 * value indicate each key is the name of a variable and its 
	 * value indicate the value of the variable[0] and 
	 * the noise value[1] in its corresponding equation
	 * in the SEM.
	 */
	private Map<String, Double[]> currentWorld;
	
	public CSM(LinkedList<String> sem) {

		varNames = new HashSet<String>();
		for (String eq : sem) 
			varNames.add(getLeftVar(eq));
		
		this.sem = sort(sem);
	}
	
	/**
	 * This function set the currentWorld map which means that in the given sample
	 * what is the value of each variable and what is the amounth of noise in each
	 * equation. Each entry of the currentWorld map is of the form 
	 * 			<varName, [value, noise]>
	 * @param cw a map of the name of variables and their values (a sample of the data)
	 */
	public void setCurrentWorld(Map<String, Double> cw) {
		currentWorld = new HashMap<String, Double[]>();
		for (String eq : sem) {
			Double[] valueNoise = new Double[2];
			// valueNoise[0] = the value of the variable in the current world
			valueNoise[0] = cw.get(getLeftVar(eq)); 
			valueNoise[1] = getNoiseValue(cw, eq);
			currentWorld.put(getLeftVar(eq), valueNoise);
		}
	}
	
	public double getNoiseValue(Map<String, Double> cw, String eq) {
		Expression e = new Expression(getNoiseExperesion(cw, eq));
		return e.calculate();
	}
	
	/**
	 * It turns "V = f(parents of V) + noise" to "noise = V - f(parents of V)"
	 * In this expressions still variables are surrounded by @ symbol.
	 * @param cw
	 * @param eq
	 * @return
	 */
	public String getNoiseExperesion(Map<String, Double> cw, String eq) {
		String newEQ = new String();
		
		//if eq is a source variable equation, just return its value
		if (ifEqIsOfASourceVariable(eq))
			return String.valueOf(cw.get(getLeftVar(eq)));
		
		//if eq is not a source variable equation
		String[] parts = eq.split("noise");
		
		while (parts[0].charAt(parts[0].length()-1) == ' ' || parts[0].charAt(parts[0].length()-1) == '+')
			parts[0] = parts[0].substring(0, parts[0].length()-1);

		String[] p = parts[0].split("=");
		
		while (p[0].charAt(0) == ' ')
			p[0] = p[0].substring(1, p[0].length());
		
		if (p[1].charAt(0) == '-')
			newEQ = p[0] + " + " + p[1].substring(1, p[0].length());
		else 
			newEQ = p[0] + " -(" +  p[1]+ ")";
		
		newEQ = replaceVariablesWithValues(cw, newEQ);
		
		return newEQ;
		
	}
	
	/**
	 * This function replace the variable names with their values.
	 * @param cw
	 * @param eq
	 * @return
	 */
	public String replaceVariablesWithValues(Map<String, Double> cw, String eq) {
		
		String parts[] = eq.split("@");
		String newEQ = new String();
		
		for (String part : parts) 
			if (cw.containsKey(part))
				newEQ = newEQ + cw.get(part).toString();
			else
				newEQ = newEQ + part;
		
		return newEQ;
	}
	
	public void setCounterfactualWorld(Map<String, Double> counterfactualWorld) {
		this.counterfactualWorld = counterfactualWorld;
	}
	
//	public LinkedList<String> 
	/**
	 * It returns the left hand side variable in equation eq.
	 * @param eq
	 * @return the name of the variable on the left side of equation
	 */
	public String getLeftVar(String eq) {
		String[] parts = eq.split("=");
		
		// removing extra characters that are before or after the variable name
		while(parts[0].charAt(0) != '@')
			parts[0] = parts[0].substring(1, parts[0].length());
		while(parts[0].charAt(parts[0].length()-1) != '@')
			parts[0] = parts[0].substring(0, parts[0].length()-1);
		
		return parts[0].substring(1, parts[0].length()-1);
	}
	
	/**
	 * It returns the right hand side variable names in equation eq.
	 * @param eq
	 * @return the names of the variable on the right side of equation
	 */
	public Set<String> getRightVars(String eq) {
		String[] parts = eq.split("=");
		Set<String> rvNames = new HashSet<String>();
		String[] chanks = parts[1].split("@");
		if (chanks.length > 0) 
			for (int i=0; i < chanks.length; i++)
				if (varNames.contains(chanks[i]))
					rvNames.add(chanks[i]);
		
		return rvNames;
	}
	
	/**
	 * 
	 * @param sem
	 * @return sem in which equation are sorted in the topological order
	 * of their left variable in the corresponding causal graph.
	 */
	public LinkedList<String> sort(LinkedList<String> sem) {

		Map<String, Set<String>> dependencies = new HashMap<String, Set<String>>();
		Map<String, String> varNameEquation = getVarNameEquationMap(sem);
		
		for (String eq : sem) 
			dependencies.put(getLeftVar(eq), getRightVars(eq));
		
		Set<String> varNames = new HashSet<String>();
		for (String varName : dependencies.keySet())
			varNames.add(varName);
		
		// adding source variables (independent of other variables) to the sem
		LinkedList<String> newSem = new LinkedList<String>();	
		for (String varName : varNames)
			if (dependencies.get(varName).isEmpty()) {
				newSem.add(varNameEquation.get(varName));
				dependencies.remove(varName);
			}
		// the set of non-source variables
		varNames = new HashSet<String>();
		for (String varName : dependencies.keySet())
			varNames.add(varName);
		
		// add the rest of the variables according to a topological sort
		for (String varName : varNames) {
			int idx = -1;
			int i = -1;
			for (String eq : newSem) {
				i++;
				String leftVar = getLeftVar(eq);
				if (dependencies.get(varName).contains(leftVar))
					idx = i;
			}
			
			if (idx < dependencies.size()-1 && idx > -1)
				newSem.add(idx+1, varNameEquation.get(varName));
			else
				newSem.add(varNameEquation.get(varName));
			
			dependencies.remove(varName);
		}
		
		return newSem;
	}
	
	public Map<String, String> getVarNameEquationMap(LinkedList<String> sem) {
		Map<String, String> varNameEquation = new HashMap<String, String>();
		for (String eq : sem) 
			varNameEquation.put(getLeftVar(eq), eq);
		
		return varNameEquation;
	}
	
	/**
	 * Given the target varName and a counterfactual world, it computes the value of the
	 * target variable in that counterfactual world.
	 * @param varName
	 * @param cfValues
	 * @return the value of the desirable variable in the counterfactual world
	 */
	public double computeCounterfactualValue(String varName, Map<String, Double> cfValues) {
		Map<String, Double> cfv = new HashMap<String, Double>();
//		System.out.println(cfValues);
		for (String eq : sem) {
//			System.out.println(eq);
			String vName = getLeftVar(eq);
//			System.out.println(vName);
			cfv.put(vName, computeCFValue(eq, cfValues, cfv));
			if (vName.equals(varName))
				return cfv.get(vName);
		}
		
		return 0;
	}
	
	/**
	 * Given the target varName and a counterfactual world, it computes the value of the
	 * all the variable in that counterfactual world.
	 * @param varName
	 * @param cfValues
	 * @return the value of the desirable variable in the counterfactual world
	 */
	public Map<String, Double> completeCounterfactualInstance(String varName, Map<String, Double> cfValues) {
		Map<String, Double> cfv = new HashMap<String, Double>();
//		System.out.println(cfValues);
		for (String eq : sem) {
//			System.out.println(eq);
			String vName = getLeftVar(eq);
//			System.out.println(vName);
			cfv.put(vName, computeCFValue(eq, cfValues, cfv));
		}
		
		return cfv;
	}
	
	/**
	 * returns true if the eq is a source variable equations
	 * and false o.w..
	 * @param eq
	 * @return
	 */
	public boolean ifEqIsOfASourceVariable(String eq) {
		String parts[] = eq.split("=");
		if (!parts[1].contains("@"))
			return true;
		else {
			String p[] = parts[1].split("@");
			for (String str : p) 
				if (varNames.contains(str))
					return false;
		}
		return true;
	}
	
	/**
	 * For the given equation and counterfactual values, it computes the value
	 * of the left variable. Counterfactual values are replaced with the current
	 * values of the variable whenever they exists.
	 * @param eq
	 * @param cfvIser :counterfactual instance given by the user
	 *        cfv : counterfactual values computed by now
	 * @return
	 */

	public double computeCFValue(String eq, Map<String, Double> cfvUser, Map<String, Double> cfv) {
		String[] parts = eq.split("=");
		String newEq = new String();
		String varName = getLeftVar(eq);
		
		// if eq is of a variable which its value has been set by the given counterfactualworld.
		if (cfvUser.containsKey(varName)) 
			return valueOfVariablesInCFW(varName);
			
		// if eq is of a source variable, just return its value
		if (ifEqIsOfASourceVariable(eq)) 
			if (cfvUser.keySet().contains(varName)) // if the value of this att is set by the user
				return cfvUser.get(varName);
			else if (cfv.containsKey(varName)) // if the value of this att is computed by now
				return cfv.get(varName);
			else 
				return currentWorld.get(varName)[0];

		
		// if eq is a not computed equation which does not belong to a source node 
		String[] p = parts[1].split("@");
		for (String str : p) {
			if (cfvUser.keySet().contains(str))	// by user
				newEq = newEq + String.valueOf(cfvUser.get(str)); 
			else if (cfv.keySet().contains(str)) // computed by now
				newEq = newEq +  String.valueOf(cfv.get(str));
			else if (currentWorld.containsKey(str)) 
				newEq = newEq + String.valueOf(currentWorld.get(str)[1]);
			else
				newEq = newEq + str;
		}
		
		if (newEq.charAt(newEq.length() - 1) == '\r')
			newEq = newEq.substring(0, newEq.length() - 1);
		
		//replacing the value of noise
		String finalEq = new String(); 
		p = newEq.split("noise");  // newEq is in the form of .... + noise +... or .... + noise
		if (p.length == 2)
			finalEq = p[0] + currentWorld.get(getLeftVar(eq))[1] + p[1];
		else if (p.length == 1)
			if (currentWorld.get(getLeftVar(eq))[1] < 0)
				finalEq = removeLastPlus(p[0]) + currentWorld.get(getLeftVar(eq))[1]; // p[0].substring(0, p[0].length()-2) + currentWorld.get(getLeftVar(eq))[1];
			else
				finalEq = p[0] + currentWorld.get(getLeftVar(eq))[1];
		
		Expression e = new Expression(finalEq);
//		System.out.println(finalEq);
//		System.out.println(e.calculate());
		return e.calculate();
	}
	
	
	/**
	 * 
	 * @param eq : some equation with an extra + at the end
	 * @return eq : without that extra +
	 */
	public String removeLastPlus(String eq) {
		String newEq = eq;
		while (eq.charAt(eq.length()-1) == ' ')
				eq = eq.substring(0, eq.length()-1);
		
		if (eq.charAt(eq.length()-1) == '+')
			return eq.substring(0, eq.length()-1);
		else
			return eq;
	}
	
	/**
	 * If the value of variable varName has been set in the given counterfactualWorld
	 * this function computes its value and check if the computed value is in the range 
	 * min and max. If it is not in the right range, this function throw an exception.
	 * @param varName
	 * @param cfv
	 * @return
	 */
	public double valueOfVariablesInCFW(String varName) {
		double v = counterfactualWorld.get(varName);
		
		if (v <= minMax.get(varName)[1] && v >= minMax.get(varName)[0])
			return v;
		else
			throw new IllegalArgumentException(" The value of " + varName  + " is " + v + " which is out of range!");
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
	

	
	public Set<String> getVarNames() {
		return varNames;
	}


	public Map<String, Double[]> getCurrentSample() {
		return currentWorld;
	}
	
	public Map<String, Double[]> getMinMax() {
		return minMax;
	}
	
	public void setMinMax(Map<String, Object[]> map) {
		this.minMax = new HashMap();
		
		for (String attName : map.keySet()) {
			Double[] mm = new Double[2];
			mm[0] = getDouble(map.get(attName)[0]);
			mm[1] = getDouble(map.get(attName)[1]);
			minMax.put(attName, mm);
		}
	}
	
	public Set<String> getAttAncestors(String classAttName) {
		AncestorFinder af = new AncestorFinder();
		return af.getAncestors(classAttName, sem, varNames);
	}
	
	//************************** Testing ***************************//

	
	public void setUpCSM(CSM csm) {
		 Map<String, Double> cw = new HashMap<String, Double>();
	        cw.put("C", 7.0);
	        cw.put("P", 2.0);
	        cw.put("PBD", 71.0);
	        cw.put("NT", 42.0);
	        cw.put("IPD", 577.0);
	        csm.setCurrentWorld(cw);
	        
	        Map<String, Object[]> map = new HashMap<String, Object[]>();
	        Object[] arrC = {1.0, 10.0};
	        map.put("C", arrC);
	        Object[] arrP = {1.0, 3.0};
	        map.put("P", arrP);
	        Object[] arrPBD = {0.0, 200.0};
	        map.put("PBD", arrPBD);
	        Object[] arrNT = {0.0, 100.0};
	        map.put("NT", arrNT);
	        Object[] arrIPD = {0.0, 1000.0};
	        map.put("IPD", arrIPD);
	        csm.setMinMax(map);
	}
	public void setUpCSM2(CSM csm) {
		 Map<String, Double> cw = new HashMap<String, Double>();
	        cw.put("initiate_hardness", 6.0);
	        cw.put("initiate_priority", 1.0);
	        cw.put("initiate_man_day", 37.0);
	        cw.put("initiate_num_people", 18.0);
	        cw.put("maintain_man_day", 2519.0);
	        csm.setCurrentWorld(cw);
	        
	        Map<String, Object[]> map = new HashMap<String, Object[]>();
	        Object[] arrC = {1.0, 10.0};
	        map.put("initiate_hardness", arrC);
	        Object[] arrP = {1.0, 3.0};
	        map.put("initiate_priority", arrP);
	        Object[] arrPBD = {-1.0, 1000.0};
	        map.put("initiate_man_day", arrPBD);
	        Object[] arrNT = {0.0, 200.0};
	        map.put("initiate_num_peopleT", arrNT);
	        Object[] arrIPD = {0.0, 10000.0};
	        map.put("maintain_man_day", arrIPD);
	        csm.setMinMax(map);
	}
	
	public void check(CSM csm) {
		double[][] data = {{4.0, 1.0, 7.0, 6.0, 5.0}, {3.0, 1.0, 3.0, 1.0, 1.0}, {17.0, -1.0, 50.0, 37.0, 28.0}, {19.0,  1.0, 30.0, 18.0, 12.0}, {1721.0,   13.0, 6103.0, 2519.0, 1173.0}};
		
		Map<String, Double> cw = new HashMap<String, Double>();
		for (int i = 0; i < 5; i++) {
	        cw.put("initiate_hardness", data[0][i]);
	        cw.put("initiate_priority", data[1][i]);
	        cw.put("initiate_man_day", data[2][i]);
	        cw.put("initiate_num_people", data[3][i]);
	        cw.put("maintain_man_day", data[4][i]);
	        csm.setCurrentWorld(cw);
	        Map<String, Double> cfw = new HashMap<String, Double>();
	        cfw.put("initiate_priority", data[1][i]);
	        csm.setCounterfactualWorld(cfw);
	        System.out.println("CF : " + csm.computeCounterfactualValue("maintain_man_day", cfw));
		}
	}
	
	public static void main(String[] args) {
        LinkedList<String> s = new LinkedList<String>();
//        s.add("@C@ = noise");
//        s.add("@P@ = noise");
//        s.add("@PBD@ = @C@ * 10 + noise ");
//        s.add("@NT@ = 5 * @C@ + 3 * @P@ + noise");
//        s.add("@IPD@ = 10 * @C@^2 + 5 * @C@ + 20 * (floor((@NT@ / 6)) +(@NT@ # 2) + (@NT@ # 2)) + noise");
        
  //      50 * ((x//5) * 5 +4 %(x % 5) + 3 % (x % 2)) + 48 
        
        s.add("@C@ = noise");
        s.add("@P@ = noise");
        s.add("@PBD@ = @C@ * 10 + noise ");
        s.add("@NT@ = 5 * @C@ + 3 * @P@ + noise");
        s.add("@IPD@ = 50 * @C@ + 5 * @NT@ + noise");
        
        CSM csm = new CSM(s);
        csm.setUpCSM(csm);
       
        // first cfw
        Map<String, Double> cfw = new HashMap<String, Double>();
//        cfw.put("P", 2.0);
        
//        csm.setCounterfactualWorld(cfw);
//        System.out.println("CF1 --> IPD : " + csm.computeCounterfactualValue("IPD", cfw));
        Map<String, Double> cfw0 = new HashMap<String, Double>();
        cfw0.put("P", 3.0);
        csm.setCounterfactualWorld(cfw0);
        System.out.println("CF0 (592) --> IPD : " + csm.computeCounterfactualValue("IPD", cfw0));
        
        Map<String, Double> cfw1 = new HashMap<String, Double>();
        cfw1.put("C", 8.0);
        csm.setCounterfactualWorld(cfw1);
        System.out.println("CF1 (652) --> IPD : " + csm.computeCounterfactualValue("IPD", cfw1));
        
        Map<String, Double> cfw2 = new HashMap<String, Double>();
        cfw2.put("NT", 25.0);
        cfw2.put("P", 3.0);
        csm.setCounterfactualWorld(cfw2);
        System.out.println("CF2 (492) --> IPD : " + csm.computeCounterfactualValue("IPD", cfw2));
        
        LinkedList<String> s2 = new LinkedList<String>();
        s2.add("@initiate_hardness@ = noise\r\n");
        s2.add("@initiate_priority@ = noise\r\n");
        s2.add("@initiate_num_people@ = round(@initiate_hardness@  * sqrt(@initiate_hardness@) + @initiate_priority@ * @initiate_priority@,0) + noise\r\n");
        s2.add("@initiate_man_day@ = round(@initiate_hardness@ * @initiate_hardness@ + floor(@initiate_hardness@/2),0) + noise\r\n");
        s2.add("@maintain_man_day@ = round(@initiate_hardness@ * @initiate_hardness@ * @initiate_hardness@ + @initiate_hardness@ * @initiate_num_people@ * 5 * sqrt(@initiate_num_people@) - (@initiate_num_people@ # 5 +1) * sqrt(@initiate_num_people@ # 5 +1),0) + noise\r\n");
        CSM csm2 = new CSM(s2);
        csm2.setUpCSM2(csm2);
        Map<String, Double> cfw3 = new HashMap<String, Double>();
        cfw3.put("initiate_priority", 2.0);
        csm2.setCounterfactualWorld(cfw3);
        System.out.println("CF3 --> IPD : " + csm2.computeCounterfactualValue("maintain_man_day", cfw3));
        csm2.check(csm2);
    }
}
 