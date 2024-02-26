package org.processmining.CounterfactualRecommendation.decisionTree;

import java.io.File;
import java.io.IOException;

import weka.classifiers.IterativeClassifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.trees.REPTree;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.core.converters.CSVSaver;

public class TestNeuralNetwork {
	public static void main(String[] args) {
		CSVLoader loader = new CSVLoader();
		Instances testData = null;
		Instances trainData = null;
		Instances wrongClassifiedData = null;
		Instances trueNegativeClassifiedData = null;
//		Instances allData = null;
		try {
		    loader.setSource(new File("bin\\testC.csv"));
			testData = loader.getDataSet();
			System.out.println("rwst data : "+ testData.numInstances());
			
			loader.setSource(new File("bin\\trainC.csv"));
			trainData = loader.getDataSet();
			System.out.println("trainData :" + trainData.numInstances());
			
//			loader.setSource(new File("bin\\testBPI2017.csv"));
//			allData = loader.getDataSet();
		} catch (IOException e) {
			System.out.println("fail to load data files!");
			e.printStackTrace();
		}
		
		for (int i = 0; i < trainData.numAttributes(); i++)
			System.out.println(i + trainData.attribute(i).name());
		trainData.setClassIndex(7);
		testData.setClassIndex(7);
		System.out.println(trainData.classAttribute().name());
		
		//set up wrongClassifiesData
		System.out.println(trainData.classAttribute().name());
		wrongClassifiedData = new Instances(trainData);
		wrongClassifiedData.setClassIndex(7);
		trueNegativeClassifiedData = new Instances(trainData);
		trueNegativeClassifiedData.setClassIndex(7);
		for (int i = trainData.numInstances() - 1; i >= 0; i-- )
			wrongClassifiedData.remove(i);
		System.out.println(wrongClassifiedData.numInstances());
		for (int i = trainData.numInstances() - 1; i >= 0; i-- )
			trueNegativeClassifiedData.remove(i);
		
//		System.out.println("---------------NN--------------------!");
		
		int numEpoch = 200;
		IterativeClassifier classifier = new MultilayerPerceptron();
		((MultilayerPerceptron) classifier).setLearningRate(0.05);
		((MultilayerPerceptron) classifier).setMomentum(0.1);
		((MultilayerPerceptron) classifier).setTrainingTime(numEpoch);
		((MultilayerPerceptron) classifier).setHiddenLayers("16, 16, 8");

//		System.out.println("---------------Evaluate--------------------!");		
		double sumTest = 0.0;
		double sumTrain = 0.0;
		double meanErrTest = 0.0;
		double meanErrTrain = 0.0;
		double[] accTrainD = new double[numEpoch];
		double[] accTestD = new double[numEpoch];
		
		try {
			classifier.initializeClassifier(trainData);
			for (int j = 0; j < numEpoch; j++) {
				classifier.next();
				Evaluation evalTrainData = new Evaluation(trainData);
				evalTrainData.evaluateModel(classifier, trainData);
				accTrainD[j] = evalTrainData.meanAbsoluteError();
				Evaluation evalTestData = new Evaluation(trainData);
				evalTestData.evaluateModel(classifier, testData);
				accTestD[j] = evalTestData.meanAbsoluteError();
//				System.out.println(j + " --> " + accTrainD[j] + "  "+ accTestD[j]);
			}
			double clsLabel = 0.0;
			double predictedBalue = 0.0;
			int i = 0;
				try {
					for (; i < testData.size(); i++) {
						predictedBalue = classifier.classifyInstance(testData.instance(i));
						if (predictedBalue <= 0.5)
							clsLabel = 0;
						else
							clsLabel = 1;
						double realValue = testData.instance(i).classValue();
						sumTest = sumTest + Math.abs(realValue - clsLabel);
						meanErrTest = meanErrTest + Math.abs(realValue - clsLabel); // / realValue;
						if (realValue != clsLabel) {
							wrongClassifiedData.add(testData.instance(i));
//							System.out.println(" real : " + realValue + "    predicted : " + predictedBalue);
						} else if (clsLabel == 0.0 && realValue == 0.0)
							trueNegativeClassifiedData.add(testData.instance(i));
					}
					
					System.out.println(" ------------- Train data ------------------ ");
						
					for (i = 0; i < trainData.size(); i++) {
						predictedBalue = classifier.classifyInstance(trainData.instance(i));
						if (predictedBalue <= 0.5)
							clsLabel = 0;
						else
							clsLabel = 1;
						double realValue = trainData.instance(i).classValue();
						sumTrain = sumTrain + Math.abs(realValue - clsLabel);
						meanErrTrain = meanErrTrain + Math.abs(realValue - clsLabel); // / realValue;
						if (realValue != clsLabel) {
							System.out.println(" real : " + realValue + "    predicted : " + predictedBalue);
							wrongClassifiedData.add(trainData.instance(i));
						} else if (clsLabel == 0.0 && realValue == 0.0)
							trueNegativeClassifiedData.add(trainData.instance(i));
					}
					
				} catch (Exception e) {
					System.out.println(" Evaluation faild for instance " + i );
					e.printStackTrace();
				}
		} catch (Exception e) {
			System.out.println(" NO classifier ");
			e.printStackTrace();
		}
		
		// save wrongly classified in a csv file
		try {
		    CSVSaver saver = new CSVSaver();
		    saver.setInstances(wrongClassifiedData);
		    saver.setFile(new File("wrongClassifiedDataC.csv"));
		    saver.writeBatch();
		  } catch (Exception e) {
		    throw new RuntimeException(
		        "ARFF file could not be read (" +  ")", e);
		  }
		
		// save wrongly classified in a csv file
		try {
			CSVSaver saver = new CSVSaver();
		    saver.setInstances(trueNegativeClassifiedData);
		    saver.setFile(new File("trueNegativeClassifiedDataC.csv"));
		    saver.writeBatch();
		  } catch (Exception e) {
		    throw new RuntimeException(
		        "ARFF file could not be read (" +  ")", e);
		  }
		
		System.out.println("\n\n all error test : " + sumTest );
		System.out.println(" accuracy test: " +  (1 - (meanErrTest/testData.numInstances())) + " \n\n");
		
		System.out.println(" all error train : " + sumTrain );
		System.out.println(" accuracy train: " +  (1 - (meanErrTrain/trainData.numInstances())) + " \n\n");
		
		// create a regrassion tree on the wrongly classified instances
		REPTree tree = new REPTree();
		tree.setMinNum(100);
		 try {
			tree.buildClassifier(wrongClassifiedData);
		} catch (Exception e1) {
			System.out.println(" Faild to create REPTree! ");
			e1.printStackTrace();
		}
		 
		double clsLabel = 0.0;
		double predictedBalue = 0.0;
		int num = 0;
		int i = 0;
		try {
			for (; i < wrongClassifiedData.size(); i++) {
				clsLabel = tree.classifyInstance(wrongClassifiedData.instance(i));
				double realValue = wrongClassifiedData.instance(i).classValue();
				if (clsLabel <= 0.5)
					clsLabel = 0;
				else
					clsLabel = 1;
				if (realValue == clsLabel) {
					num++;
				}
			}
			
		} catch (Exception e) {
			System.out.println(" Evaluation faild for instance " + i );
			e.printStackTrace();
		}
		
		System.out.println(" num " + num);
		System.out.println(" size " + wrongClassifiedData.numInstances());
		System.out.println(tree.toString());
		
		testRT(trainData, testData);
	}
	
	public static void testRT(Instances trainData, Instances testData) {
		double sumTest = 0.0;
		double sumTrain = 0.0;
		double meanErrTest = 0.0;
		double meanErrTrain = 0.0;
		double predictedBalue;
		double clsLabel;
		
		REPTree classifier = new REPTree();
		classifier.setMaxDepth(-1);
		classifier.setNoPruning(true);
		classifier.setMinNum(2);
		try {
			classifier.buildClassifier(trainData);
			for (int i = 0; i < testData.size(); i++) {
				clsLabel = classifier.classifyInstance(testData.instance(i));
				double realValue = testData.instance(i).classValue();
				sumTest = sumTest + Math.abs(realValue - clsLabel);
				meanErrTest = meanErrTest + Math.abs(realValue - clsLabel); // / realValue;
//					System.out.println(" real : " + realValue + "    predicted : " + predictedBalue);
				}

			for (int i = 0; i < trainData.size(); i++) {
				clsLabel = classifier.classifyInstance(trainData.instance(i));
				double realValue = trainData.instance(i).classValue();
				sumTrain = sumTrain + Math.abs(realValue - clsLabel);
				meanErrTrain = meanErrTrain + Math.abs(realValue - clsLabel); // / realValue;
				}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("------------------ RT -------------------");
		
		System.out.println("\n\n all error test : " + sumTest );
		System.out.println(" accuracy test: " +  (1 - (meanErrTest/testData.numInstances())) + " \n\n");
		
		System.out.println(" all error train : " + sumTrain );
		System.out.println(" accuracy train: " +  (1 - (meanErrTrain/trainData.numInstances())) + " \n\n");
		
	}
}
