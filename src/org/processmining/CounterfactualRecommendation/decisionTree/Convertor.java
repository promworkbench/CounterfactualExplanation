package org.processmining.CounterfactualRecommendation.decisionTree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class Convertor {
	
	private static boolean isConvertToDoubleSucc = true;
	
	private static double convertObjectToDouble(Object o) {
		isConvertToDoubleSucc = true;
		
		//TODO: should d be 0? or should it be the mean of the column?
		double d = 0;
		try {
			if(o != null) {
//				if(o.toString().equals("true")) {
//					d=1;
//				} else if(o.toString().equals("false")){
//					d=0;
//				} else {
//					d = Double.valueOf(o.toString());
//				}
				d = Double.valueOf(o.toString());
			}
		} catch(NumberFormatException e) {
			isConvertToDoubleSucc = false;
		}
		return d;
	}
	
	
	public static Instances convertDataToInstances(List<Map<String, Object>> data,
			String targetLabel, List<String> attributeNames) {
		
		//Attributes are "headlines" for the Instances
		ArrayList<Attribute> attributeList = new ArrayList<>();
		
		
		for(String attributeName : attributeNames) {
			ConversionResult result = checkConformance(data, attributeName);
			 
			//select the constructor for the Attributes("headlines") to choose if the column is nominal or ordinal
			if(result.isDataConform) {
				if(result.isDataNumeric) {
					
					//use constructor for ordinal attribute
					attributeList.add(new Attribute(attributeName));
					
				} else {
					//construct a list of unique Strings from the column
					List<String> columnList = getUniqueColumnList(data, attributeName);
					
					//use constructor for nominal attribute
					//use List columnList to select the desired constructor
					attributeList.add(new Attribute(attributeName, columnList));					
				}
			} else {
				//TODO
				//Exception?
				System.out.println("The column is not conform: " + attributeName);
			}
			
		}
		
//		if(isTargetNumeric) {
			//choose constructor for numeric attribute
			attributeList.add(new Attribute(targetLabel));
//		} else {
//			//choose constructor for nominal attribute
//			attributeList.add(new Attribute(targetLabel, getUniqueColumnList(data, targetLabel)));
//		}
		
		//make Instances instances with
		//1. arg: instance name
		//2. arg: column headlines
		//3. arg: number of rows
		Instances instances = new Instances("just some string", attributeList, data.size());
		
		//fill Instances instances with empty instance
		List<Instance> instanceList = new ArrayList<>();
		for(int i = 0; i < data.size(); i++) {
			Instance inst = new DenseInstance(2 * attributeNames.size()+1);
			instanceList.add(inst);
		}
		instances.addAll(instanceList);
		
		//fill all instance with data 
		for(int i = 0; i < data.size(); i++) {
			fillInstance(instances.get(i), data.get(i), attributeNames, targetLabel);
			
		}
		
		//sets the target for the classifier
		instances.setClass(attributeList.get(attributeList.size() - 1));
		
		return instances;
	}
	
	public static List<String> getUniqueColumnList(List<Map<String, Object>> data, String attributeName) {
		HashSet<String> set = new HashSet<>();
		for(Map<String,Object> map : data) {
			if(map.get(attributeName) != null) {
				set.add(map.get(attributeName).toString());
			} else {
				set.add("NULL");
			}
		}
//		if(set.size() <= 2) {
//			set.add("dummy");
//		}
		return new ArrayList<>(set);
	}
	
	
	public static void fillInstance(Instance instance, Map<String, Object> map,
			List<String> attributeNames, String targetLabel) 
	{
		for(int i=0; i<attributeNames.size() ; i++) {
			
			boolean isNumeric = instance.attribute(i).isNumeric();
			Object value = map.get(attributeNames.get(i));			
			
			if(isNumeric) {
				instance.setValue(i, convertObjectToDouble(value));
			} else {
				if(value != null) {
					instance.setValue(i, value.toString());
				} else {
					instance.setValue(i, "NULL");
				}
			}
		}
		
		//fill target in last place
		Object target = map.get(targetLabel);
		if(target != null) {
			instance.setValue(attributeNames.size(), (Double) target);	
		} else {
			instance.setValue( attributeNames.size(), 0);
		}
	}
	
	//checks if the specific column is conform (only strings or only doubles...)
	public static ConversionResult checkConformance (List<Map<String, Object>> data, String columnName) {
		ConversionResult result = new ConversionResult();
		
		boolean firstItem = true;
		boolean allElementsAreConform = true;
		boolean isDouble = false;
		for(Map<String, Object> map : data) {
			Object o = map.get(columnName);
			//TODO:
			//handle NULL for Strings and Double
			if(o != null) {
				convertObjectToDouble(o);

				if(firstItem) {
					isDouble = isConvertToDoubleSucc;
					firstItem = false;
				} else {
					if(isConvertToDoubleSucc != isDouble) {
						allElementsAreConform = false;
						break;
					}
				}
			} 
			else {
				//...
			}
		}
		
		result.isDataConform = allElementsAreConform;
		result.isDataNumeric = isDouble;
		return result;
	}
	
	private static class ConversionResult {
		public boolean isDataConform;
		public boolean isDataNumeric;
	}
}