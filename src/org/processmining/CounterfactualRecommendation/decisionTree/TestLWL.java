package org.processmining.CounterfactualRecommendation.decisionTree;

import java.io.File;
import java.io.IOException;

import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.lazy.LWL;
import weka.classifiers.trees.REPTree;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

public class TestLWL {
	public static void main(String[] args) {
		CSVLoader loader = new CSVLoader();
		Instances testData = null;
		Instances trainData = null;
		Instances allData = null;
		Instances finalData = null;
		try {
		    loader.setSource(new File("bin\\testDLCA.csv"));
			testData = loader.getDataSet();
			loader.setSource(new File("bin\\trainDLCA.csv"));
			trainData = loader.getDataSet();
			loader.setSource(new File("bin\\ADLCA.csv"));
			allData = loader.getDataSet();
			loader.setSource(new File("bin\\FSLCA.csv"));
			finalData = loader.getDataSet();
		} catch (IOException e) {
			System.out.println("fail to load data files!");
			e.printStackTrace();
		}
		
		trainData.setClassIndex(3);
		testData.setClassIndex(3);
		allData.setClassIndex(3);
		finalData.setClassIndex(3);
		
//		System.out.println("---------------LWL--------------------!");

/**		int[] kernelType = {0, 1, 2}; 
		int[] knn = {8, 9, 10, 11, 12 , 13, 14, 15, 16, 32};//{-1, 2, 4, 8, 16, 32};
		for (int kt : kernelType)
			for (int k : knn) {
				try {
					double sum = 0.0;
					double meanErr = 0.0;
					LWL classifier = new LWL();   
					classifier.setKNN(k);
					classifier.setWeightingKernel(kt);
					classifier.buildClassifier(trainData);
					
					Evaluation evalTrainData = new Evaluation(trainData);
					evalTrainData.evaluateModel(classifier, trainData);
					double accTrainD = evalTrainData.meanAbsoluteError();
					Evaluation evalTestData = new Evaluation(trainData);
					evalTestData.evaluateModel(classifier, testData);
					double accTestD = evalTestData.meanAbsoluteError();
					System.out.println("\\n\\n kernel: " + kt + " KNN: " + k + " --> " + accTrainD + "  "+ accTestD);
					
					int i = 0;
				
					for (; i < testData.size(); i++) {
						double clsLabel = classifier.classifyInstance(testData.instance(i));
						double realValue = testData.instance(i).classValue();
						sum = sum + Math.abs(realValue - clsLabel);
						meanErr = meanErr + Math.abs(realValue - clsLabel) / realValue;
					}
					System.out.println("all error : " + sum);
					System.out.println("accuracy : " +  (1 - (meanErr/testData.numInstances())) + " \n\n");
					
				} catch (Exception e) {
					System.out.println(" Evaluation faild for instance ");
					e.printStackTrace();
				}
			} */
		
		System.out.println(" ========================================= ");
		
		try {
			double sum = 0.0;
			double meanErr = 0.0;
			LWL classifier = new LWL();   
			classifier.setKNN(18);
			classifier.setWeightingKernel(0);
			classifier.buildClassifier(trainData);
			
			Evaluation evalTrainData = new Evaluation(trainData);
			evalTrainData.evaluateModel(classifier, trainData);
			double accTrainD = evalTrainData.meanAbsoluteError();
			Evaluation evalTestData = new Evaluation(trainData);
			evalTestData.evaluateModel(classifier, testData);
			double accTestD = evalTestData.meanAbsoluteError();
			System.out.println("\\n\\n LWL  --> " + accTrainD + "  "+ accTestD);
			
			int i = 0;
		
			for (; i < testData.size(); i++) {
				double clsLabel = classifier.classifyInstance(testData.instance(i));
				double realValue = testData.instance(i).classValue();
				sum = sum + Math.abs(realValue - clsLabel);
				meanErr = meanErr + Math.abs(realValue - clsLabel) / realValue;
			}
			System.out.println("all error : " + sum);
			System.out.println("accuracy : " +  (1 - (meanErr/testData.numInstances())) + " \n\n");
			
		} catch (Exception e) {
			System.out.println(" Evaluation faild for instance ");
			e.printStackTrace();
		}
		
		System.out.println(" ========================================= ");
		try {
			double sum = 0.0;
			double meanErr = 0.0;
			REPTree classifier = new REPTree();
			classifier.setMaxDepth(-1);
			classifier.setNoPruning(true);
			classifier.setMinNum(2);
			classifier.buildClassifier(trainData);
			
			Evaluation evalTrainData = new Evaluation(trainData);
			evalTrainData.evaluateModel(classifier, trainData);
			double accTrainD = evalTrainData.meanAbsoluteError();
			Evaluation evalTestData = new Evaluation(trainData);
			evalTestData.evaluateModel(classifier, testData);
			double accTestD = evalTestData.meanAbsoluteError();
			System.out.println("\\n\\n RT  --> " + accTrainD + "  "+ accTestD);
			
			int i = 0;
		
			for (; i < testData.size(); i++) {
				double clsLabel = classifier.classifyInstance(testData.instance(i));
				double realValue = testData.instance(i).classValue();
				sum = sum + Math.abs(realValue - clsLabel);
				meanErr = meanErr + Math.abs(realValue - clsLabel) / realValue;
			}
			System.out.println("all error : " + sum);
			System.out.println("accuracy : " +  (1 - (meanErr/testData.numInstances())) + " \n\n");
			
		} catch (Exception e) {
			System.out.println(" Evaluation faild for instance ");
			e.printStackTrace();
		}
		
		//=============================================
		try {
			double sum = 0.0;
			double meanErr = 0.0;
			MultilayerPerceptron classifier = new MultilayerPerceptron();   
			classifier.setLearningRate(0.1);
			classifier.setMomentum(0.1);
			classifier.setTrainingTime(111);
			classifier.setHiddenLayers("16, 8");
			classifier.buildClassifier(trainData);
			
			Evaluation evalTrainData = new Evaluation(trainData);
			evalTrainData.evaluateModel(classifier, trainData);
			double accTrainD = evalTrainData.meanAbsoluteError();
			Evaluation evalTestData = new Evaluation(trainData);
			evalTestData.evaluateModel(classifier, testData);
			double accTestD = evalTestData.meanAbsoluteError();
			System.out.println("\\n\\n NN  --> " + accTrainD + "  "+ accTestD);
			
			int i = 0;
		
			for (; i < testData.size(); i++) {
				double clsLabel = classifier.classifyInstance(testData.instance(i));
				double realValue = testData.instance(i).classValue();
				sum = sum + Math.abs(realValue - clsLabel);
				meanErr = meanErr + Math.abs(realValue - clsLabel) / realValue;
			}
			System.out.println("all error : " + sum);
			System.out.println("accuracy : " +  (1 - (meanErr/testData.numInstances())) + " \n\n");
			
		} catch (Exception e) {
			System.out.println(" Evaluation faild for instance ");
			e.printStackTrace();
		} 
		
		////////////////////////////////////////////
		System.out.println(" ========================================= ");
		try {
			double sum1 = 0.0;
			double meanErr1 = 0.0;
			REPTree classifier1 = new REPTree();
			classifier1.setMaxDepth(-1);
			classifier1.setNoPruning(true);
			classifier1.setMinNum(2);
			classifier1.buildClassifier(allData);
			
			double sum2 = 0.0;
			double meanErr2 = 0.0;
			LWL classifier2 = new LWL();   
			classifier2.setKNN(18);
			classifier2.setWeightingKernel(0);
			classifier2.buildClassifier(allData);
			
			double sum3 = 0.0;
			double meanErr3 = 0.0;
			MultilayerPerceptron classifier3 = new MultilayerPerceptron();   
			classifier3.setLearningRate(0.1);
			classifier3.setMomentum(0.1);
			classifier3.setTrainingTime(111);
			classifier3.setHiddenLayers("16, 8");
			classifier3.buildClassifier(allData);
			
			
			int i = 0;
		
			for (; i < finalData.size(); i++) {
				double clsLabel1 = classifier1.classifyInstance(finalData.instance(i));
				double clsLabel2 = classifier2.classifyInstance(finalData.instance(i));
				double clsLabel3 = classifier3.classifyInstance(finalData.instance(i));
				double realValue = finalData.instance(i).classValue();
				System.out.println("pv : " +  String.format("%.2f", clsLabel1) + "," +  String.format("%.2f", clsLabel2) + "," + String.format("%.2f", clsLabel3) + "," + realValue);
				sum1 = sum1 + Math.abs(realValue - clsLabel1);
				meanErr1 = meanErr1 + Math.abs(realValue - clsLabel1) / realValue;
				sum2 = sum2 + Math.abs(realValue - clsLabel2);
				meanErr2 = meanErr2 + Math.abs(realValue - clsLabel2) / realValue;
				sum1 = sum3 + Math.abs(realValue - clsLabel3);
				meanErr3 = meanErr3 + Math.abs(realValue - clsLabel3) / realValue;
			}
			System.out.println("all error RT: " + sum1);
			System.out.println("accuracy : " +  (1 - (meanErr1/finalData.numInstances())) + " \n\n");
			System.out.println("all error LWL: " + sum1);
			System.out.println("accuracy : " +  (1 - (meanErr2/finalData.numInstances())) + " \n\n");
			System.out.println("all error NN: " + sum1);
			System.out.println("accuracy : " +  (1 - (meanErr3/finalData.numInstances())) + " \n\n");
			
		} catch (Exception e) {
			System.out.println(" Evaluation faild for instance ");
			e.printStackTrace();
		} 

	}
	
	
	
}
