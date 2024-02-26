package org.processmining.CounterfactualRecommendation.algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class AncestorFinder {
	/**
	 * Map on variable names and their indexes
	 */
	 Map<String, Set<String>> adjacencyMatrix;
	 
	 public void setAdjecencyMatrix(LinkedList<String> sem, Set<String> varNames) {
		 adjacencyMatrix = new HashMap<>();
		 
		 // create one node for each variable
		 Map<String, String> attEq = new HashMap<>();
		 for (String eq : sem) {
			 String[] parts = eq.split("=");
			 
			// removing extra characters that are before or after the variable name
			while(parts[0].charAt(0) != '@')
				parts[0] = parts[0].substring(1, parts[0].length());
			while(parts[0].charAt(parts[0].length()-1) != '@')
				parts[0] = parts[0].substring(0, parts[0].length()-1);
			Set<String> set = new HashSet<String>();
			adjacencyMatrix.put(parts[0].substring(1,parts[0].length() - 1), set);
			attEq.put(parts[0].substring(1,parts[0].length() - 1), parts[1]);
		 }
		 
		 // add direct ancestors of each node
		 for (String attName : attEq.keySet()) {
			 String[] chanks = attEq.get(attName).split("@");
				if (chanks.length > 0) 
					for (int i=0; i < chanks.length; i++)
						if (varNames.contains(chanks[i]))
							adjacencyMatrix.get(attName).add(chanks[i]);
		 }
	
	 }
	 
	 public Set<String> getAncestors(String attName, LinkedList<String> sem, Set<String> varNames) {
		 setAdjecencyMatrix(sem, varNames);
		 
		 // if the node is a source node return null
		 if (adjacencyMatrix.get(attName).isEmpty())
			 return null;
		 
		 Set<String> ancestors = new HashSet<String>();
		 Set<String> newAncs = new HashSet<String>(); 
		 for (String name : adjacencyMatrix.get(attName)) {
			 ancestors.add(name);
			 if (!adjacencyMatrix.get(name).isEmpty())
				 newAncs.addAll(adjacencyMatrix.get(name));
		 }
		 
		 
		 while (!newAncs.isEmpty()) {
			 Set<String> set = new HashSet<>();
			 for  (String str : newAncs)
				 set.add(str);
			 
			for (String name : set) {
				if (!ancestors.contains(name)) {
					if (!adjacencyMatrix.get(name).isEmpty())
						 newAncs.addAll(adjacencyMatrix.get(name));
					ancestors.add(name);
					newAncs.remove(name);
				} else
					newAncs.remove(name);
			}
		 }
		 
		 return ancestors;
	 }
	 
	 // ----------------------- Test -----------------------------
	 public static void main(String[] args) {
	        LinkedList<String> sem = new LinkedList<String>();
	        sem.add("@C@ = noise");
	        sem.add("@P@ = noise");
	        sem.add("@PBD@ = @C@ * 10 + noise ");
	        sem.add("@NT@ = 5 * @C@ + 3 * @P@ + noise");
	        sem.add("@IPD@ = 50 * @C@ + 5 * @NT@ + noise");
	       
	        Set<String> varNames = new HashSet<String>();
	        varNames.add("C");
	        varNames.add("P");
	        varNames.add("NT");
	        varNames.add("IPD");
	        varNames.add("PBD");
	        
	        String classAttName = "IPD";
	        
	        AncestorFinder af = new AncestorFinder();
	        System.out.println(af.getAncestors(classAttName, sem, varNames));
	    }
	 
}
