package org.processmining.CounterfactualRecommendation.optimizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.processmining.CounterfactualRecommendation.algorithms.CSM;
import org.processmining.CounterfactualRecommendation.decisionTree.Classification;
import org.processmining.CounterfactualRecommendation.parameters.ClassificationMethod;
import org.processmining.CounterfactualRecommendation.parameters.CounterfactualParameters;
import org.processmining.datadiscovery.estimators.Type;

import com.espertech.esper.collection.Pair;

public class Optimizer {
	private CounterfactualParameters params;
//	Set<String> attNamesToChange;
	Map<String, Double> step;
	Classification classifier;
	CSM sem;
	
	public Optimizer(CounterfactualParameters params) {
		this.params = params;
		params.getActionableAttNames();
		setSteps();
	}
	
	
	/** 
	 * setting the step size for all the atts
	 */
	private void setSteps() {
		step = new HashMap<>();
		
		double timeStep = params.getTimeStep();
		if (timeStep != 0) {
			Double ts = Math.floor(timeStep * 3600000); //TODO cast needs a check
			
			for (String attName : params.getActionableAttNames()) {
				String name = attName.toLowerCase();
				if (name.contains("duration") || name.contains("timestamp") || name.contains("elapsed_time") || name.contains("remaining_time"))
					step.put(attName, ts);
				
			}
		}
		
		double nonTimeStep = params.getNonTimeStep();
		if (params.getDataExtraction().getMinMax() != null)
			for (String name : params.getActionableAttNames()) 
				if (params.getDataExtraction().getMinMax().containsKey(name))
					if (!(name.contains("duration") || name.contains("timestamp") || name.contains("elapsed_time") || name.contains("remaining_time"))) {
						Object[] minMax = params.getDataExtraction().getMinMax().get(name);
						double min = Double.parseDouble(minMax[0].toString());
						double max = Double.parseDouble(minMax[1].toString());
						if (params.getDataExtraction().getAttributeTypes().get(name).equals(Type.DISCRETE)) {
							step.put(name, Math.ceil(nonTimeStep * (max - min)));
						//	System.out.println(nonTimeStep * (max - min));
						} else if (params.getDataExtraction().getAttributeTypes().get(name).equals(Type.CONTINUOS)) {
							step.put(name, nonTimeStep * (max - min));
						} 
					}
	}

	/**
	 * For a given instance, find the attribute names that have a different value than the current world instance.
	 * @param instance
	 * @return
	 */
	public Set<String> attNamesToChange(Map<String, Double> instance) {
		Set<String> attNamesToChange = new HashSet<>();
		Map<String, Double> currentInstance = params.getCurrentInstance();
		for (String attName : params.getActionableAttNames()) {
			if (instance.containsKey(attName))
				if (!attName.equals(params.getDataExtraction().classAttributeName())) {
					if (!params.getCurrentInstance().containsKey(attName))
						attNamesToChange.add(attName);
					else if (instance.get(attName) != params.getCurrentInstance().get(attName))
						attNamesToChange.add(attName);
			}
		}
		
		return attNamesToChange;
	}
	
	/**
	 * Returns a set including all the attribute names in both current world instance and the given counterfactual instance.
	 * Note that we may have some attributes that have value in just one of samples
	 * @param instance
	 * @return A set including all the attribute names in both current world sample and the given counterfactual instance.
	 */
	private Set<String> getAllAttNames(Map<String, Double> instance) {
		Map<String, Double> currentInstance = params.getCurrentInstance();
		Set<String> attNames = new HashSet<>();
				
		for (String attName : instance.keySet())
			 attNames.add(attName);
		
		for (String attName : currentInstance.keySet())
			if (!attNames.contains(attName))
				attNames.add(attName);
		
		return attNames;
	}
	
	/**
	 * if instance == null
	 * 		first creates all the possible subsets of the actionable attNames and then optimize
	 * else
	 * 		create all the possible subsets of those atts in the instance that have different values with currentinstance, and then optimize them.
	 * the current instance over each subset. 
	 * @return
	 */
	public Set<Map<String, Double>> optimizeInstance(Map<String, Double> instance) {
		
		// a set of generated counterfactual instances
		Set<Map<String, Double>> newInstances = new HashSet<>();
		Subsets sets = new Subsets();
		Set<Set<String>> attNameSet = new HashSet<>();
		
		if (instance == null) {
			attNameSet = sets.getSubsets(params.getActionableAttNames());
			
//			System.out.println(attNameSet.size());
			
			for (Set<String> attNames : attNameSet) {
				Map<String, Double> inst = oneOptimization(attNames, null);
				if (inst != null && isDesirableInstance(inst))
					newInstances.add(inst);
			}
			
			return newInstances;
		}
		
//		System.out.println(instance);
		attNameSet = sets.getSubsets(attNamesToChange(instance));
			
		if (!attNameSet.isEmpty() && attNameSet != null) {
			int i = 0;
			for (Set<String> attNames : attNameSet) {
//				System.out.println("i : " + i);
				instance = oneOptimization(attNames, instance);
				if (instance != null) {
					Map<String, Double> inst = new HashMap<>();
					for (String name : instance.keySet())
						inst.put(name, instance.get(name));
					newInstances.add(inst);
				}
			}
			if (newInstances != null)
				finalCheck(newInstances);
				
			return newInstances;	
		}
		
		return null;
	}
	
	/**
	 * for each new instance, checks if replacing one of its features values
	 * by the current instance value for that feature does not change still
	 * the class value is desirable, then replace it.
	 * @param newInstances
	 */
	private void finalCheck(Set<Map<String, Double>> newInstances) {
		Map<String, Double> currentInstnace = params.getCurrentInstance();
		String classAttName = params.getDataExtraction().classAttributeName();
		Set<Map<String, Double>> newSet = new HashSet<>();
		for (Map<String, Double> item : newInstances) {
			for (String attName : params.getActionableAttNames()) {
				if (item.containsKey(attName))
					if (currentInstnace.containsKey(attName))
						if (!item.get(attName).equals(currentInstnace.get(attName))) 
							if (isReplacementDesirable(item, attName))
								newSet.add(replace(item, attName));
							else
								newSet.add(item);
			}
		}
		
		newInstances = newSet;		
	}

	/**
	 * Checks if the instance after the replacement of the value of attName with the one in
	 * the current instance is still desirable or not.
	 * @param item
	 * @param attName
	 * @return true if desirable instance, false o.w.
	 */
	private boolean isReplacementDesirable(Map<String, Double> instance, String attName) {
		Map<String, Double> newInstance = replace(instance, attName);
		return isDesirableInstance(newInstance);
	}


	/**
	 * replace the value of the attName in the instance with the one in the current instance
	 * @param item
	 * @param attName
	 * @return
	 */
	private Map<String, Double> replace(Map<String, Double> instance, String attName) {
		instance.put(attName, params.getCurrentInstance().get(attName));
		Double classVal = classifier.evaluateInstance(instance);
		instance.put(params.getDataExtraction().classAttributeName(), classVal);
		return instance;
	}


	/** 
	 * Optimize the current instance using the given set of attNames
	 * @param attNames
	 * @param instance : if null currentInstance is considered, o.w. the given instance
	 * @return
	 */
	private Map<String, Double> oneOptimization(Set<String> attNames, Map<String, Double> instance) {
			boolean flag = true;
			boolean isCurrentInstance = false;
			String classAttName = params.getDataExtraction().classAttributeName();
			// in the history we save the class attribute value. If we saw a repetitive pattern, then we can 
			// stop and break the loop
			LinkedList<Double> history = new LinkedList<>();
			
//			System.out.println("nothing love ;)");
			if (instance == null) {
				instance = params.getCurrentInstance();
				isCurrentInstance = true;
			}
			
			Pair<String, String> attNameToChange = selectTheBestAttToChange(instance, attNames, isCurrentInstance);
			LinkedList<Double> historyClassValue = new LinkedList<>();
			LinkedList<Double> historyDistance = new LinkedList<>();
			LinkedList<Map<String, Double>> historyInstance = new LinkedList<>();
			LinkedList<Pair<String, String>> historyChange = new LinkedList<>();
			LinkedList<Set<String>> historyAttNameSet = new LinkedList<>();
			
			while (flag) {
//				if (attNameToChange != null)
//					System.out.println(" line 118 " + attNameToChange.toString());
//				System.out.println(instance);
				
				// When optimizing the current instance, if the desirable value is reached, remove the attName
//				System.out.println(params.ifLowerIsDesirable() );
//				System.out.println(params.getGoodResultNmericalThreshold() );
				if (isCurrentInstance && params.ifLowerIsDesirable() && getClassAttValue(instance) <= params.getGoodResultNmericalThreshold()) {
					break;
				//	attNames.remove(attNameToChange.getFirst());
				//	if (attNames.isEmpty())
				//		break;
				//	else
				//		attNameToChange = selectTheBestAttToChange(instance, attNames, isCurrentInstance);
				}
					
				if (isCurrentInstance && !params.ifLowerIsDesirable() && getClassAttValue(instance) >= params.getGoodResultNmericalThreshold()) {
					break;
				//	attNames.remove(attNameToChange.getFirst());
				//	if (attNames.isEmpty())
				//		break;
				//	else
				//		attNameToChange = selectTheBestAttToChange(instance, attNames, isCurrentInstance);
				}
				
				// if no more optimization is possible, leave the while loop
				if (attNameToChange == null) {
					flag = false;
					break;
				}
				
				// if the suggested change by attNameToChange result in an invalid instance
				// then do not continue changing that attName
				if (!isValid(attNameToChange, instance)) {
					attNames.remove(attNameToChange.getFirst());
					if (attNames.isEmpty())
						flag = false;
					else
						attNameToChange = selectTheBestAttToChange(instance, attNames, isCurrentInstance);
				} else {
					instance = doOneOptimizationStep(instance, attNameToChange);
					if (instance == null)
						break;
					
					boolean b = true;
					if (!isCurrentInstance) {  // if it is not the optimization of the current instance, then the instance should allways have a good result
						b = isDesirableInstance(instance);
						if (b) {
							historyClassValue.add(instance.get(params.getDataExtraction().classAttributeName()));
							historyDistance.add(distance(instance, instance.get(classAttName)));
							add(historyInstance, instance);
							add(historyAttNameSet, attNames);
							add(historyChange,attNameToChange); 
							attNameToChange = selectTheBestAttToChange(instance, attNames, isCurrentInstance);
						} else {
							attNames.remove(attNameToChange.getFirst());
							//TODO revert the instance too
							if (attNames.isEmpty()) {
								flag = false;
								break;
							} else
								attNameToChange = selectTheBestAttToChange(instance, attNames, isCurrentInstance);
						}
					} else {
						historyClassValue.add(instance.get(params.getDataExtraction().classAttributeName()));
						historyDistance.add(distance(instance, instance.get(classAttName)));
						add(historyInstance, instance);
						add(historyAttNameSet, attNames);
						add(historyChange,attNameToChange); //TODO make sure that deep copy is happening
						attNameToChange = selectTheBestAttToChange(instance, attNames, isCurrentInstance);
					}
				}
				
				// Check if we are in loop
				boolean loop = false;
				if (isCurrentInstance) {
					if (inLoop(historyClassValue))
						loop = true;
				} else { 
					if (inLoop(historyDistance))
						loop = true;
				}
							
				if (loop) {
					Pair<Map<String, Double>, String> instanceAttName = 
							handelLoop(historyClassValue, historyDistance, historyInstance, historyChange, isCurrentInstance);
					attNames.remove(attNameToChange.getFirst()); 
					if (attNames.isEmpty())
						flag = false;
					else
						attNameToChange = selectTheBestAttToChange(instance, attNames, isCurrentInstance);
				}
			}
			
			return instance;
	}
	
	private boolean isDesirableInstance(Map<String, Double> instance) {
		double value = instance.get(params.getDataExtraction().classAttributeName());
		if (params.ifLowerIsDesirable() && value > params.getGoodResultNmericalThreshold())
			return false;
		if (!params.ifLowerIsDesirable() && value < params.getGoodResultNmericalThreshold())
			return false;
		
		return true;
	}


	/**
	 * Deep copy a pair of strings to a Given LinkedList.
	 * @param historyChange
	 * @param attNameToChange
	 */
	private void add(LinkedList<Pair<String, String>> historyChange, Pair<String, String> attNameToChange) {
		 historyChange.add(new Pair<String, String>(attNameToChange.getFirst(), attNameToChange.getSecond()));
	}


	/**
	 * Deep copy a set of attNames and add it to the given linkedList.
	 * @param historyAttNameSet
	 * @param attNames
	 */
	private void add(LinkedList<Set<String>> historyAttNameSet, Set<String> attNames) {
		Set<String> set = new HashSet<>();
		
		for (String str : attNames) {
			set.add(str);
		}
		
		historyAttNameSet.add(set);
	}

	/**
	 * Deep copy an instance and add it to the given linkedList
	 * @param historyInstance
	 * @param instance
	 */
	private void add(LinkedList<Map<String, Double>> historyInstance, Map<String, Double> instance) {
		Map<String, Double> newInstance = new HashMap<>();
		
		for (String attName : instance.keySet())
			newInstance.put(attName, instance.get(attName));
		
		historyInstance.add(newInstance);		
	}


	private Pair<Map<String, Double>, String> handelLoop(LinkedList<Double> historyClassValue,
			LinkedList<Double> historyDistance, LinkedList<Map<String, Double>> historyInstance,
			 LinkedList<Pair<String, String>> historyChange, boolean isCurrentInstance) {
		//find the best index to revert to that index
		int idx;
		if (isCurrentInstance)
			idx = handelLoopCurrentInstance(historyClassValue, historyInstance, isCurrentInstance);
		else
			idx = handelLoopInstance(historyDistance, historyInstance, isCurrentInstance);
		
		//revert history
		int num = historyInstance.size() - 1;
		for (int i = num; i > idx; i--) {
			historyClassValue.remove(i);
			historyInstance.remove(i);
			historyDistance.remove(i);
			historyChange.remove(i);
		}
		
		//return the good instance and the attName to remove from attNames set
		return new Pair<Map<String, Double>, String>(historyInstance.getLast(), historyChange.getLast().getSecond());
	}

	/**
	 * When optimizing a generated instance, if we are in a loop, 
	 * it returns the best index of the history to role bake to it.
	 * @param historyDistance
	 * @param historyInstance
	 * @param isCurrentInstance
	 * @return best index of history to role back.
	 */
	private int handelLoopInstance(LinkedList<Double> historyDistance, LinkedList<Map<String, Double>> historyInstance,
			boolean isCurrentInstance) {
		// find the first time that the last index is repeated
		int index = 0;
		for (int i = historyDistance.size() - 2; i >= 0 ; i--)
			if (historyDistance.get(i) == historyDistance.getLast()) {
				index = i;
				break;
			}
		
		int bestInstanceIdx = index;
		double dist = distance(historyInstance.get(index));
		for (int i = 1; i + index < historyInstance.size(); i++) {
			double newDist = distance(historyInstance.get(index + i));
			if (newDist < dist) {
				dist = newDist;
				bestInstanceIdx = index + i;
			}
		}
			
		return bestInstanceIdx;
	}

	/**
	 * When optimizing the current instance, if we are in a loop, 
	 * it returns the best index of the history to role bake to it.
	 * @param historyClassValue
	 * @param historyInstance
	 * @param isCurrentInstance
	 * @return best index of history to role back.
	 */
	private int handelLoopCurrentInstance(LinkedList<Double> historyClassValue,
			LinkedList<Map<String, Double>> historyInstance, boolean isCurrentInstance) {
		// find the first time that the last index is repeated
		int index = 0;
		for (int i = historyClassValue.size() - 2; i >= 0 ; i--)
			if (historyClassValue.get(i) == historyClassValue.getLast()) {
				index = i;
				break;
			}
		
		// if we had some instances with the good result, among them find the one that its
		// class value is closest to the threshold and return its index
		
		// find the index of instances with good result
		LinkedList<Integer> desirableInstances = new LinkedList<>();
		double thereshold = params.getGoodResultNmericalThreshold();
		if (params.ifLowerIsDesirable()) {
			for (int i = index; i < historyClassValue.size() - 1; i++) {
				if (historyClassValue.get(i) <= thereshold)
					desirableInstances.add(i);
			}
		} else {
			for (int i = index; i < historyClassValue.size() - 1; i++) {
				if (historyClassValue.get(i) >= thereshold)
					desirableInstances.add(i);
			}
		}
		
		// choose the best index and return it
		int bestInstanceIdx = 0;
		double dist;
		if(!desirableInstances.isEmpty()) {
			bestInstanceIdx = desirableInstances.get(0);
			dist = Math.abs(thereshold - historyClassValue.get(desirableInstances.get(0)));
			for (int i = 1; i < desirableInstances.size(); i++) {
				double newDist = Math.abs(thereshold - historyClassValue.get(desirableInstances.get(i)));
				if (newDist < dist) {
					dist = newDist;
					bestInstanceIdx = desirableInstances.get(i);
				}
			}
			
			return bestInstanceIdx;
		}
		
		// if we do not have any instance with the good result, just find the instance with
		// closest class value to the threshold and return its index.
		bestInstanceIdx = index;
		dist = Math.abs(thereshold - historyClassValue.get(index));
		for (int i = 1; i + index < historyClassValue.size(); i++) {
			double newDist = Math.abs(thereshold - historyClassValue.get(index + i));
			if (newDist < dist) {
				dist = newDist;
				bestInstanceIdx = index + i;
			}
		}
			
		return bestInstanceIdx;
	}


	private boolean isValid(Pair<String, String> attNameToChange, Map<String, Double> instance) {
		String attName = attNameToChange.getFirst();
		double min = getDouble(params.getDataExtraction().getMinMax().get(attNameToChange.getFirst())[0]);
		double max = getDouble(params.getDataExtraction().getMinMax().get(attNameToChange.getFirst())[1]);
		
		if (attNameToChange.getSecond().equals("Plus"))
			if (instance.get(attName) + step.get(attName) > max ) {
				return false;
			}
		if (attNameToChange.getSecond().equals("Minus"))
			if (instance.get(attName) - step.get(attName) < min ) {
				return false;
			}
		
		return true;
	}


	public Double getClassAttValue(Map<String, Double> instance) {
		return instance.get(params.getDataExtraction().classAttributeName());
	}
	
	private void handelLoop(LinkedList<Double> history, Map<String, Double> instance) {
		if (history.size() <= 1)
			return;
		
		// find the first repetition of the last item.  
		int index = 0;
		for (int i = history.size() - 2; i >= 0 ; i--)
			if (history.get(i) == history.getLast()) {
				index = i;
				break;
			}
		index = index +1; // the index of the first item in the repetition.
		
		// find the desirable values, if any!
		double threshold = params.getGoodResultNmericalThreshold();
		LinkedList<Double> desirableValues = new LinkedList<>();
		for (int i = index; i < history.size(); i++) {
			if (params.ifLowerIsDesirable() && history.get(i) <= threshold)
				desirableValues.add(history.get(i));
			if (!params.ifLowerIsDesirable() && history.get(i) >= threshold)
				desirableValues.add(history.get(i));
		}
		//TODO find the closest desirable value to the threshold if any
		// if no desirable instance exists, then find the closest thereshold value to the 
		// current instance
		int bestIndex = index;
		
		
	}


	private boolean inLoop(LinkedList<Double> history) {
		if (history.size() <= 1)
			return false;
		
		// find the first repetition of the last item
		// items between the last one and the one in index might be duplicated. 
		int index = 0;
		for (int i = history.size() - 2; i >= 0 ; i--) {
//			System.out.println("history " + i + "  " + history.get(i));
//			System.out.println("history last " + history.getLast());
			if (history.get(i).equals(history.getLast())) {
				index = i;
				break;
			}
		}
		
		// last item is not repeated
		if (index == 0)
			return false;
		
		// if the length of the history is shorter than twice the number of items 
		// between the last item and the index, then there is no repetition.
		if (history.size() < (history.size()-1)-index)
			return false;
		
		//check if all the values between the last two occurrence of last value are repeated
		int dist = (history.size()-1)-index;
		for (int i = history.size()-1; i > index ; i--)
			if (!history.get(i).equals(history.get(i - dist)))
				return false;
		
		return true;
	}


	private Map<String, Double> doOneOptimizationStep(Map<String, Double> instance, Pair<String, String> attNameToChange) {
		
		if (attNameToChange == null)
			return null;
		
		String attName = attNameToChange.getFirst();
		String direction = attNameToChange.getSecond();
		
		Map<String, Double> newInstance = new HashMap<>();
		
		for (String name : instance.keySet()) {
			if (name.equals(attName)) {
				if (direction.equals("Plus"))
					newInstance.put(name, instance.get(name) + step.get(name));
				else
					newInstance.put(name, instance.get(name) - step.get(name));
			} else {
				newInstance.put(name, instance.get(name));
			}
		}
		
		if (!isAValidCFW(newInstance))
			return null;
		
		Double value = evaluateOneInstance(newInstance);
		
		newInstance.put(params.getDataExtraction().classAttributeName(), value);
		
		return newInstance;
		
	}

	private Double evaluateOneInstance(Map<String, Double> instance) {
		
		Map<String, Double> newInstance = trimInstance(instance);
		
		if (params.getMethod().equals(ClassificationMethod.SEM)) {
			sem.setCounterfactualWorld(newInstance);
			return sem.computeCounterfactualValue(params.getDataExtraction().classAttributeName(), newInstance);
		} else {
			return classifier.evaluateInstance(instance);
		}
	}
	
	/**
	 * Given a counterfactual instance, remove the attributes that have the same value as
	 * the current instance.
	 * @param instance
	 * @return trimmed instance
	 */
	private Map<String, Double> trimInstance(Map<String, Double> instance) {
		Map<String, Double> inst = new HashMap<>();
		for (String attName : instance.keySet())
			if (!attName.equals(params.getDataExtraction().classAttributeName()))
				if (instance.get(attName) != params.getCurrentInstance().get(attName))
					inst.put(attName, instance.get(attName));
		
		return inst;
	}


	private Pair<String, String> selectTheBestAttToChange(Map<String, Double> instance, Set<String> attNames, boolean isCurrentInstance) {

		/**
		 * attNameProgress for each attName how much progress (toward desirable threshold) is done if we do one step + or - 
		 * 0 : no progress in any direction
		 */
		Map<String, Double> attNameProgress = new HashMap<>();
		/**
		 * direction for each attName which direction result in more progress.
		 *  values "Plus", "Minus", "Non"
		 */
		Map<String, String> direction = new HashMap<>();
		
		
		for (String attName : attNames) {
			Map<String, Double> instancePlus = getInstance(instance, attName, "Plus");
			Map<String, Double> instanceMinus = getInstance(instance, attName, "Minus");
			Double valuePlus;
			Double valueMinus;
			
			double vp = evaluateOneInstance(instancePlus);
			double vm = evaluateOneInstance(instanceMinus);
//			System.out.println(" l 615 : vp "+ vp + " vm " + vm);
			if (isCurrentInstance) {
				valuePlus = vp;
				valueMinus = vm;
			} else {
				valuePlus = distance(instancePlus, vp);
				valueMinus = distance(instanceMinus, vm);
			}
			
			String direc = whichDirection(valuePlus, valueMinus, vp, vm, isCurrentInstance);
			if (direc != null) {
				
				if (direc.equals("Plus")) {
					attNameProgress.put(attName, valuePlus);
					direction.put(attName, "Plus");
				}
				else if (direc.equals("Minus")) {
					attNameProgress.put(attName, valueMinus);
					direction.put(attName, "Minus");
				}
			}
		}
		
		String name = selectAttName(attNameProgress);
		
		if (name == null)
			return null;
		
		Pair<String, String> result = new Pair(name, direction.get(name));
				
		return result;
	}
	
	/**
	 * return the distance(instance, current instance) if the class value of the instance is in the desirable area
	 *  -1 o.w.
	 * @param instance
	 * @param value
	 * @return
	 */
	private Double distance(Map<String, Double> instance , double value) {
		// if the class attribute value of the new instance is not desirable return -1
		if (params.ifLowerIsDesirable() && value > params.getGoodResultNmericalThreshold())
			return (double) -1;
		if (!params.ifLowerIsDesirable() && value < params.getGoodResultNmericalThreshold())
			return (double) -1;
		
		double distance = 0.0;
		for (String attName : instance.keySet())
			if (params.getCurrentInstance().containsKey(attName) && !attName.equals(params.getDataExtraction().classAttributeName()))
				distance = distance + Math.abs((params.getCurrentInstance().get(attName) - instance.get(attName))/ (getDouble(params.getDataExtraction().getMinMax(attName)[1]) - getDouble((params.getDataExtraction().getMinMax(attName)[0]))));
		
		return distance;
	}
	
	private Double distance(Map<String, Double> instance) {
		// if the class attribute value of the new instance is not desirable return -1
		if (!isDesirableInstance(instance))
			return (double) -1;
		
		double distance = 0.0;
		for (String attName : instance.keySet())
			if (params.getCurrentInstance().containsKey(attName) && !attName.equals(params.getDataExtraction().classAttributeName()))
				distance = distance + Math.abs((params.getCurrentInstance().get(attName) - instance.get(attName))/ (getDouble(params.getDataExtraction().getMinMax(attName)[1]) - getDouble((params.getDataExtraction().getMinMax(attName)[0]))));
		
		return distance;
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


	private String selectAttName(Map<String, Double> attNameProgress) {
		if (attNameProgress.isEmpty())
			return null;
		
		boolean flag = true;
		
		for (String name : attNameProgress.keySet())
			if (attNameProgress.get(name) != -1)
				flag = false;
		
		if (flag)
			return null;
		
		Double threshold = params.getGoodResultNmericalThreshold();
		
		String attName = new String();
		double minDist = Double.MAX_VALUE;
		for (String name : attNameProgress.keySet()) {
			double dist = Math.abs(threshold - attNameProgress.get(name));
			if (dist < minDist && dist != -1) {
				minDist = dist;
				attName = name;
			}
		}
		
		return attName;
	}

	private String whichDirection(Double valuePlus, Double valueMinus, Double vp, double vm, boolean isCurrentInstance) {
		if (valuePlus.equals(valueMinus))
			return null;
		
		Double threshold = params.getGoodResultNmericalThreshold();
		if (isCurrentInstance) {
			
			double valueCurrent = params.getCurrentInstance().get(params.getDataExtraction().classAttributeName());
			
			double distPlus = Math.abs(threshold - valuePlus);
			double distMinus = Math.abs(threshold - valueMinus);
			double distCurrent = Math.abs(threshold - valueCurrent);
			

			if (valueCurrent <= threshold && params.ifLowerIsDesirable() && valueMinus <= threshold && valuePlus > threshold )
				return null;
			if (valueCurrent <= threshold && params.ifLowerIsDesirable() && valueMinus > threshold && valuePlus <= threshold )
				return null;
			if (valueCurrent >= threshold && !params.ifLowerIsDesirable() && valueMinus < threshold && valuePlus >= threshold )
				return null;
			if (valueCurrent >= threshold && !params.ifLowerIsDesirable() && valueMinus >= threshold && valuePlus < threshold )
				return null;
			
			if ((distPlus <= distMinus && distMinus <= distCurrent) || (distPlus <= distCurrent && distCurrent <= distMinus))
				return "Plus";
			if ((distMinus <= distPlus && distPlus <= distCurrent) || (distMinus <= distCurrent && distCurrent <= distPlus))
				return "Minus";
			
			boolean flagPlus = false;
			if (params.ifLowerIsDesirable() && valuePlus <= threshold)
				flagPlus = true;
			if (!params.ifLowerIsDesirable() && valuePlus >= threshold)
				flagPlus = true;
			
			boolean flagMinus = false;
			if (params.ifLowerIsDesirable() && valueMinus <= threshold)
				flagMinus = true;
			if (!params.ifLowerIsDesirable() && valueMinus >= threshold)
				flagMinus = true;
			
			if (flagPlus && flagMinus)
				if (distPlus < distMinus)
					return "Plus";
				else if (distPlus > distMinus)
					return "Minus";
			
			if (flagPlus && !flagMinus)
				return "Plus";
			
			if (!flagPlus && flagMinus)
				return "Minus";
			
			return null;
		}
		
		if (vm <= threshold && vm > threshold)
			return null;
		if (vp <= threshold && vm > threshold)
			return null;
		
		if (valuePlus < valueMinus)
			return "Plus";
		else 
			return "Minus";
		
	}

	private Map<String, Double> getInstance(Map<String, Double> instance, String attName, String direction) {
		Map<String, Double> newInstance = new HashMap<>();
		for (String name : instance.keySet()) {
			if (attName.equals(name))
				if (direction.equals("Plus"))
					newInstance.put(name, instance.get(name) + step.get(name));
				else 
					newInstance.put(name, instance.get(name) - step.get(name));
			else 
				newInstance.put(name, instance.get(name));
		}
		
		return newInstance;
	}
	
	
	
	private class Subsets { //Generate all subsets by generating all binary numbers

	    public  Set<Set<String>> getSubsets(Set<String> set) {

	        Set<Set<String>> allSubsets = new HashSet<Set<String>>();
	        int max = 1 << set.size();             //there are 2 power n different subsets
	        
	        ArrayList<String> array = new ArrayList<String>();
	        for (String name : set)
	        	array.add(name);

	        int n = array.size(); 
	        
	        // Run a loop for printing all 2^n 
	        // subsets one by one 
	        for (int i = 0; i < (1<<n); i++) 
	        { 
	            Set<String> subset = new HashSet<String>(); 
	  
	            // Print current subset 
	            for (int j = 0; j < n; j++) 
	                if ((i & (1 << j)) > 0) 
	                    subset.add(array.get(j)); 
	            
	            if (!subset.isEmpty())
	            	allSubsets.add(subset); 
	        } 
	        
	        return allSubsets;
	    } 
	}
	
	public void setClassifier(Classification c) {
		classifier = c;
	}
	
	public void setSEM(CSM sem)	   {
		this.sem = sem;
	}
	
	/**
	 * Check if all attributes satisfy min <= value(att) <= max
	 * @param sample
	 * @return
	 */
	public boolean isAValidCFW(Map<String, Double> instance) {

		for (String varName : instance.keySet())
			if (!isInRange(varName, instance.get(varName)))
				return false;
		
		return true;
	}
	
	public boolean isInRange(String varName, double value) {
		
		if (value <= getDouble(params.getDataExtraction().getMinMax().get(varName)[1]) && 
				value >= getDouble(params.getDataExtraction().getMinMax().get(varName)[0]))
			return true;
		
		return false;
	}
}
