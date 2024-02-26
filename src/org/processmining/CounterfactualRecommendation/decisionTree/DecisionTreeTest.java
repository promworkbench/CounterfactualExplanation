package org.processmining.CounterfactualRecommendation.decisionTree;

import java.io.File;
import java.io.IOException;

import weka.classifiers.trees.REPTree;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

public class DecisionTreeTest {

	public void readData() {
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
		
		REPTree tree = new REPTree();
		 try {
			tree.buildClassifier(trainData);
		} catch (Exception e1) {
			System.out.println(" Faild to create REPTree! ");
			e1.printStackTrace();
		}
		 
		double clsLabel = 0.0;
		int i = 0;
			try {
				for (; i < testData.size(); i++) {
					clsLabel = tree.classifyInstance(testData.instance(i));
					System.out.println("The predicted value : " + clsLabel); 
				}
				
			} catch (Exception e) {
				System.out.println(" Evaluation faild for instance " + i );
				e.printStackTrace();
			}
	        
			
	}
	
	public static void main(String[] args)throws Exception 
	  { 
	  DecisionTreeTest dt = new DecisionTreeTest();
	  dt.readData();
	  } 
}
