package org.processmining.CounterfactualRecommendation.decisionTree;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.processmining.CounterfactualRecommendation.algorithms.DataExtraction;
import org.processmining.CounterfactualRecommendation.parameters.ClassificationMethod;
import org.processmining.CounterfactualRecommendation.parameters.CounterfactualParameters;
import org.processmining.datadiscovery.estimators.Type;

import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.lazy.LWL;
import weka.classifiers.trees.REPTree;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

public class Classification {
		
	/**
	 *  Includes the classification setting.
	 */
	private CounterfactualParameters params;
	
	/**
	 *  The WEKA classifier.
	 */
	private Classifier classifier;
	
	/**
	 *  This object has the situation set and attNames.
	 */
	private DataExtraction de;
	
	/**
	 *  A Mapping between the position of the attName in the data table and the attName.
	 */
	private Map<String, Integer> attNameIndex;
	
	/**
	 * WEKA dataset.
	 */
	private Instances instances;
	
	/**
	 * the set of attributes in the WEKA dataset.
	 */
	private ArrayList<Attribute> attributes;
	
	public Classification(CounterfactualParameters params, DataExtraction de) {
		this.params = params;
		this.de = de;
		createInstances();
		createClassifier();
	}
	
	public void createClassifier() {
		
		try {
			if (params.getMethod().equals(ClassificationMethod.RT)) {
//				Classifier c = new REPTree();
//				Evaluation eval = new Evaluation(instances);
//			    eval.crossValidateModel(c, instances, 5, new Random(1));
//			    System.out.println("RT " + params.getMinNum() + " " +params.getMaxDepth() + " " +params.getNoPruning() + " acc : "+ (1 - eval.errorRate()));
//			    Classifier c = new REPTree();
//			    Evaluation eval = new Evaluation(instances);
//			    eval.evaluateModel(c, instances);
//			    System.out.println("RT " + params.getMinNum() + " " +params.getMaxDepth() + " " +params.getNoPruning() + "err : "+ eval.meanAbsoluteError());
			    
				classifier = new REPTree();
				((REPTree) classifier).setMaxDepth(params.getMaxDepth());
				((REPTree) classifier).setNoPruning(params.getNoPruning());
				((REPTree) classifier).setMinNum(params.getMinNum());
				classifier.buildClassifier(instances);
				System.out.println(classifier);
			} else if (params.getMethod().equals(ClassificationMethod.LWL)) {
//				Classifier c = new LWL();
//				Evaluation eval = new Evaluation(instances);
//			    eval.crossValidateModel(c, instances, 5, new Random(1));
//			    System.out.println("LWL " + params.getKernel() + " " +params.getKNN() + " acc : "+ (1 - eval.errorRate()));
				Classifier c = new LWL();
			    Evaluation eval = new Evaluation(instances);
			    eval.evaluateModel(c, instances);
			    System.out.println("LWL " + params.getKernel() + " " +params.getKNN() + "err : "+ eval.meanAbsoluteError());
				
				classifier = new LWL();   
				((LWL) classifier).setKNN(params.getKNN());
				((LWL) classifier).setWeightingKernel(params.getKernel());
				classifier.buildClassifier(instances);
			} else if (params.getMethod().equals(ClassificationMethod.NN)) {
//				Classifier c = new MultilayerPerceptron();
//				Evaluation eval = new Evaluation(instances);
//			    eval.crossValidateModel(c, instances, 5, new Random(1));
//			    System.out.println("NN " + params.getLearningRate() + " " +params.getMomentum() + " " +params.getNumEpoches() + " " + params.getHiddenLayers() + " acc : "+ (1 - eval.errorRate()));
//			    
				classifier = new MultilayerPerceptron();
//				((MultilayerPerceptron) classifier).setOptions(Utils.splitOptions(params.getNNparameters()));
				((MultilayerPerceptron) classifier).setLearningRate(params.getLearningRate());
				((MultilayerPerceptron) classifier).setMomentum(params.getMomentum());
				((MultilayerPerceptron) classifier).setTrainingTime(params.getNumEpoches());
				((MultilayerPerceptron) classifier).setHiddenLayers(params.getHiddenLayers());
				classifier.buildClassifier(instances);
			} else 
				System.out.println(" -- Not a valid classifier! -- ");
		} catch (Exception e) {
			System.out.println("Fail to create the decision tree!");
			e.printStackTrace();
		} 
		
	}
	
	/**
	 * Turns the situations gathered by DataExtraction class to the instances format of WEKA
	 * @return the instances created by the raw data.
	 */
	public void createInstances() {
		// build attributes
		attributes = new ArrayList<Attribute>();
		Set<String> attNames = de.getAttributeTypes().keySet();
		attNameIndex = new HashMap<>();
		String classAttName = de.classAttributeName();
		int classAttIndex = 0; //The class attribute index in the WEKA instances dataset.
		  
		int i = 0;
		for (String attName : attNames) {
			  // attribute name is the polynomial power
			 Attribute xattr = new Attribute(attName); 
			 attributes.add(xattr);
			 attNameIndex.put(attName, i);
			 if (attName.equals(classAttName))
				  classAttIndex = i;
			 i++;
		 }    
		  
		  
		 LinkedList<Map<String, Object>> situations = de.getInstancesOfNDC();
		 int numInstances = situations.size();

		 instances = new Instances("Situation Table", attributes, numInstances);

		 // fill instances 
		 for (Map<String, Object> situation : situations) {
			 instances.add(getAnInstance(situation));
		 }
		 
		 instances.setClassIndex(classAttIndex);
	}
	
	/**
	 * Turning a situation to a WEKA instance.
	 */
	public Instance getAnInstance(Map<String, Object> situation) {
		Instance inst = new DenseInstance(attNameIndex.size());

		for (String attName : attNameIndex.keySet()) {
			// If situation has a value for the attribute
			if (situation.containsKey(attName)) {
				// If it is a literal attribute
	    		if (de.getAttributeTypes().get(attName).equals(Type.LITERAL)) {
	    			inst.setValue(instances.attribute(attNameIndex.get(attName)), situation.get(attName).toString());;
	    		}
	    		else { // If it is not a literal attribute
	    			inst.setValue(instances.attribute(attNameIndex.get(attName)), getDouble(situation.get(attName)));
	    		}
	    	} else { // Set to missing value if instance does not have a value for this attribute
	    		inst.setMissing(attNameIndex.get(attName)); 
	    	}
		}
		
		// Set instance's dataset to be the "instances"
		inst.setDataset(instances);
				
		return inst;
	}
	
	/**
	 * Turning a situation to a WEKA instance.
	 */
	public Instance createAnInstance(Map<String, Double> situation) {
		Instance inst = new DenseInstance(attNameIndex.size());

		for (String attName : attNameIndex.keySet()) {
			// If situation has a value for the attribute
			if (situation.containsKey(attName)) {
				// If it is a literal attribute
	    		if (de.getAttributeTypes().get(attName).equals(Type.LITERAL)) {
	    			inst.setValue(instances.attribute(attNameIndex.get(attName)), situation.get(attName).toString());;
	    		}
	    		else { // If it is not a literal attribute
	    			inst.setValue(instances.attribute(attNameIndex.get(attName)), situation.get(attName));
	    		}
	    	} else { // Set to missing value if instance does not have a value for this attribute
	    		if (params.getCurrentInstance().containsKey(attName))
	    			inst.setValue(instances.attribute(attNameIndex.get(attName)), params.getCurrentInstance().get(attName));
	    		else
	    			inst.setMissing(attNameIndex.get(attName)); 
	    			
	    	}
		}
		
		// Set instance's dataset to be the "instances"
		inst.setDataset(instances);
				
		return inst;
	}
	
	/**
	 * Predict the value of one situation.
	 * @param situation
	 * @return predicted class value
	 */
	public Double evaluateInstance(Map<String, Double> situation) {
		// Creating one WEKA dataset with just one instance.
		Instances unlabeled = new Instances("Test", attributes, 1);
        unlabeled.setClassIndex(instances.classIndex());
        unlabeled.add(createAnInstance(situation));
        
        //Make prediction for the only instance in the test dataset.
        double clsLabel = 0.0;
		try {
			clsLabel = classifier.classifyInstance(unlabeled.instance(0));
		} catch (Exception e) {
			System.out.println(" Evaluation faild for instance " + situation.toString());
			e.printStackTrace();
		}

		return	clsLabel;

	}
	
	/**
	 * Returnd the double value of v.
	 * @param v
	 * @return
	 */
	public double getDouble(Object v) {
		double d = 0;
		if (v instanceof Integer)
			d = ((Integer) v).doubleValue();
		else if (v instanceof Long)
			d = ((Long) v).doubleValue();
		else 
			d = Double.parseDouble(v.toString());
		
		return d;
	}
	
	//--------------------------- Test ---------------------------
	
	public Classification(ClassificationMethod method) {
//		this.method = method;
	}
	
	public void setInstancs(Instances insts) {
		instances = insts;
	}
	
	public void test(Instances testData) {
		double clsLabel = 0.0;
		int i = 0;
			try {
				for (; i < testData.size(); i++) {
					clsLabel = classifier.classifyInstance(testData.instance(i));
					System.out.println("The predicted value : " + clsLabel); 
				}
				
			} catch (Exception e) {
				System.out.println(" Evaluation faild for instance " + i );
				e.printStackTrace();
			}
	}
	
	
	public static void main(String[] args) {
		CSVLoader loader = new CSVLoader();
		Instances testData = null;
		Instances trainData = null;
		try {
		    loader.setSource(new File("bin\\testData.csv"));
			testData = loader.getDataSet();
			loader.setSource(new File("bin\\trainData.csv"));
			trainData = loader.getDataSet();
		} catch (IOException e) {
			System.out.println("fail to load data files!");
			e.printStackTrace();
		}
		
		trainData.setClassIndex(4);
		testData.setClassIndex(4);
		
		System.out.println("----------------RT--------------------!");
		Classification cls1 = new Classification(ClassificationMethod.RT);
		
		cls1.setInstancs(trainData);
		cls1.createClassifier();
		cls1.test(testData);
		
		System.out.println("----------------LWL--------------------!");
		Classification cls2 = new Classification(ClassificationMethod.LWL);
		
		cls2.setInstancs(trainData);
		cls2.createClassifier();
		cls2.test(testData);
		
		System.out.println("---------------NN--------------------!");
		Classification cls3 = new Classification(ClassificationMethod.NN);
		
		cls3.setInstancs(trainData);
		cls3.createClassifier();
		cls3.test(testData);
	}
	
	
	//--------------------- Evaluation -------------------------
	public Classification(CounterfactualParameters params, DataExtraction de, boolean remove) {
		this.params = params;
		this.de = de;
		createInstances();
		createClassifier();
	}
	
	/**
	 * Turns the situations gathered by DataExtraction class to the instances format of WEKA
	 * @return the instances created by the raw data.
	 */
	public void createInstances(Set<String> attNamesSEM) {
		// build attributes
		attributes = new ArrayList<Attribute>();
		Set<String> attNames = de.getAttributeTypes().keySet();
		attNameIndex = new HashMap<>();
		String classAttName = de.classAttributeName();
		  
		int i = 0;
		for (String attName : attNames) {
			  // attribute name is the polynomial power
			 Attribute xattr = new Attribute(attName); 
			 attributes.add(xattr);
			 attNameIndex.put(attName, i);
		 }    
		  
		  
		 LinkedList<Map<String, Object>> situations = de.getInstancesOfNDC();
		 int numInstances = situations.size();

		 instances = new Instances("Situation Table", attributes, numInstances);

		 // fill instances 
		 for (Map<String, Object> situation : situations) {
			 Map<String, Object> s = new HashMap<String, Object>();
			 for (String attName : situation.keySet())
				 if (attNamesSEM.contains(attName))
					 s.put(attName, situation.get(attName));
			 instances.add(getAnInstance(s));
		 }
		 
		 for (int j = 0; j < instances.numAttributes(); j++) {
			 String name = instances.attribute(j).name();
			 if (name.equals(classAttName)) {
				 instances.setClassIndex(j);
				 break;
			 }
		 }
	}
}
