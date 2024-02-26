package org.processmining.CounterfactualRecommendation.decisionTree;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.processmining.CounterfactualRecommendation.algorithms.DataExtraction;
import org.processmining.datadiscovery.estimators.Type;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.visualisation.DotPanel;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.REPTree;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class DecisionTree {
	DataExtraction de;
	
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
	
	/**
	 * The decision tree classifier.
	 */
	private Classifier tree;
	
	public DecisionTree(DataExtraction de) {
		this.de = de;
		
		
		Convertor c = new Convertor();
		Instances insts = c.convertDataToInstances(de.getInstancesOfNDC(), de.targetActivityName, de.getAttributes());
		// Create dataset for WEKA
		createInstances();
		
		//Create classifier
		createDecisionTree();
		
	}
	/**
	 * As the class attribute is continuous, we need regression tree not a J48 decision tree.
	 * @return
	 */
	private REPTree createREPTree() {
		 tree = new REPTree();
		 try {
			tree.buildClassifier(instances);
		} catch (Exception e1) {
			System.out.println(" Faild to create REPTree! ");
			e1.printStackTrace();
		}
		 
		return (REPTree) tree;
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
			  instances.add(createAnInstance(situation));
		  }
		  
		  instances.setClassIndex(classAttIndex);
	}
	
	/**
	 * Turning a situation to a WEKA instance.
	 */
	public Instance createAnInstance(Map<String, Object> situation) {
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

	/**	double[] instanceValues = new double[attNameIndex.size()];
		
		for (String attName : attNameIndex.keySet()) {
	    	if (situation.containsKey(attName)) {
	    		if (de.getAttributeTypes().get(attName).equals(Type.LITERAL)) {
	    			instanceValues[attNameIndex.get(attName)] = instances.attribute(attNameIndex.get(attName)).addStringValue((String) situation.get(attName));
	    		}
	    		else {
	    			instanceValues[attNameIndex.get(attName)] = getDouble(situation.get(attName));
	    		}
	    	} 
		}

        return new DenseInstance(1.0, instanceValues); */
	}
	
	/**
	 * Creates a J48 decision tree.
	 */
	private void createDecisionTree() {
		tree = new J48();
		try {
			tree.buildClassifier(instances);
		} catch (Exception e) {
			System.out.println("Fail to create the decision tree!");
			e.printStackTrace();
		}
		
		System.out.println(tree);
	}
	
	public Object evaluateInstance(Map<String, Object> situation) {
		// Creating one WEKA dataset with just one instance.
		Instances unlabeled = new Instances("Test", attributes, 1);
        unlabeled.setClassIndex(instances.classIndex());
        unlabeled.add(createAnInstance(situation));
        
        //Make prediction for the only instance in the test dataset.
        double clsLabel = 0.0;
		try {
			clsLabel = tree.classifyInstance(unlabeled.instance(0));
		} catch (Exception e) {
			System.out.println(" Evaluation faild for instance " + situation.toString());
			e.printStackTrace();
		}
        
		System.out.println("The predicted value : " + unlabeled.classAttribute().value(unlabeled.classIndex()));
		return	unlabeled.classAttribute().value((int) clsLabel);

	}
	
		
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
	
	
	//******************** TEST *****************************
	public void setAttType() {
		Map<String, Type> types = new HashMap<>();
		types.put("H", Type.DISCRETE);
		types.put("P", Type.DISCRETE);
		types.put("NP", Type.DISCRETE);
		types.put("PBD", Type.CONTINUOS);
		types.put("IPD", Type.CONTINUOS);
		de.setTypes(types);
		
		ArrayList<String> attList =new ArrayList<String>();
		attList.add("H");
		attList.add("P");
		attList.add("NP");
		attList.add("PBD");
		de.setAttNames(attList);
	}
	
	private void setUpDE() {
		de = new DataExtraction();
		setAttType();
		de.setClassAttName("IPD");
		setDataset();
	}
	
	public void setDataset() {
		LinkedList<Map<String, Object>> instancesDe =  new LinkedList<Map<String, Object>>();
		File file = new File("bin\\test.txt"); 
		  
		  BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		  
		  String st; 
		  try {
			while ((st = br.readLine()) != null) {
				String[] parts = st.split("\t");
				Map<String, Object> ins = new HashMap<String, Object>();
				ins.put("H", getDouble(parts[0]));
				ins.put("PBD", getDouble(parts[1]));
				ins.put("NP", getDouble(parts[2]));
				ins.put("P", getDouble(parts[3]));
				ins.put("IPD", getDouble(parts[4]));
				instancesDe.add(ins);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		  
		de.setInstances(instancesDe);
	}
	
	public void testDTEvaluation() {
		File file = new File("bin\\test2.txt"); 
		  
		  BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		  
		  String st; 
		  try {
			while ((st = br.readLine()) != null) {
				String[] parts = st.split("\t");
				Map<String, Object> ins = new HashMap<String, Object>();
				ins.put("H", parts[0]);
				ins.put("PBD", parts[1]);
				ins.put("NP", parts[2]);
				ins.put("P", parts[3]);
				ins.put("IPD", parts[4]);
				System.out.println(" class : " + evaluateInstance(ins).toString());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public Object evaluateInstance(REPTree rt, Map<String, Object> situation) {
		// Creating one WEKA dataset with just one instance.
		System.out.println("Situation : " + situation);
		Instances unlabeled = new Instances("Test", attributes, 0);
        unlabeled.setClassIndex(instances.classIndex());
        unlabeled.add(createAnInstance(situation));
        
        System.out.println(rt);
        System.out.println(unlabeled.instance(0));
        
        //Make prediction for the only instance in the test dataset.
        double clsLabel = 0.0;
		try {
			clsLabel = rt.classifyInstance(unlabeled.instance(0));
		} catch (Exception e) {
			System.out.println(" Evaluation faild for instance " + situation.toString());
			e.printStackTrace();
		}
        
		System.out.println("The predicted value : " + clsLabel);
		
		return	clsLabel;

	}
	
	public void testDTEvaluation(REPTree rt) {
		File file = new File("bin\\test2.txt"); 
		  
		  BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		  
		  String st; 
		  try {
			while ((st = br.readLine()) != null) {
				String[] parts = st.split("\t");
				Map<String, Object> ins = new HashMap<String, Object>();
				ins.put("H", parts[0]);
				ins.put("PBD", parts[1]);
				ins.put("NP", parts[2]);
				ins.put("P", parts[3]);
				ins.put("IPD", parts[4]);
				System.out.println(" class : " + evaluateInstance(rt, ins).toString());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public DecisionTree() {	
		
		setUpDE();
		Convertor c = new Convertor();
		instances = c.convertDataToInstances(de.getInstancesOfNDC(), de.classAttributeName(), de.getAttributes());
		// Create dataset for WEKA
//		createInstances();
		
		//Create classifier
//		createDecisionTree();
		
		REPTree rt = createREPTree();
		attributes = new ArrayList<Attribute>();
		attNameIndex = new HashMap<>();
		int i = 0;
		 for (String attName : de.getAttributeTypes().keySet()) {
			 Attribute xattr = new Attribute(attName); 
			 attributes.add(xattr);
			 attNameIndex.put(attName, i);
			 i++;
		 }
		 
		try {
			System.out.println(rt.graph());
		} catch (Exception e) {
			System.out.println(" No tree love!" );
			e.printStackTrace();
		}
		testDTEvaluation(rt);
		
		JFrame frame = new JFrame("REPTree");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setSize(600, 600);

        try {
			frame.getContentPane().add(getVisualization(rt));
		} catch (Exception e) {
			System.out.println("No tree ;) ");
			e.printStackTrace();
		}
        frame.pack();
        frame.setVisible(true);
		
	}
	public JPanel getVisualization(REPTree rt) throws Exception {
		JPanel panel = new JPanel();		
        Dot newDot = new Dot();
        panel.setLayout(new BorderLayout());
        String dotRepresentation = rt.graph();
        System.out.println("VISUALIZEFAIRTREE@@@@ "+dotRepresentation);
        newDot.setStringValue(dotRepresentation);
        DotPanel dotPanel = new DotPanel(newDot);
        panel.add(dotPanel);
		return panel;
	}
	
	 public static void main(String[] args)throws Exception 
	  { 
	  DecisionTree dt = new DecisionTree();
	  } 
}

