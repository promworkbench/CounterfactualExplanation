package org.processmining.CounterfactualRecommendation.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XTrace;
import org.processmining.CounterfactualRecommendation.parameters.ClassificationMethod;
import org.processmining.CounterfactualRecommendation.parameters.CounterfactualParameters;
import org.processmining.CounterfactualRecommendation.parameters.LowOrHeigh;
import org.processmining.CounterfactualRecommendation.parameters.Metric;
import org.processmining.datadiscovery.estimators.Type;
import org.processmining.framework.util.ui.widgets.ProMList;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;

import com.fluxicon.slickerbox.components.NiceDoubleSlider;
import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.factory.SlickerFactory;

import csplugins.id.mapping.ui.CheckComboBox;

public class WisardStepParameters extends ProMPropertiesPanel implements ProMWizardStep<CounterfactualParameters> {
	private static final String TITLE = " ";
	
	private Map<String, Type> attTypes;
	private String indepAttName;
	private Map<XTrace, LinkedList<Map<String, Object>>> traceInstanceMap;
	private NiceDoubleSlider thresholdDouble;
	private NiceDoubleSlider thresholdInt;
	private JList goodValues;
	private String sem = null;
	private JComboBox<ClassificationMethod> metodComboBox;
	Map<String, Map<String, Object>> idInstance;
	CheckComboBox actionableAtts; 
	JCheckBox isLowerDesirable;
	JComboBox instanceID;
	JCheckBox ifRandSampling;
	JCheckBox doOptimize;
	
	//optimization parameters
	JTextField timeStep; // step size for time related atts, in houre
	JTextField nonTimeStep; // step size for other atts, in percent
	
	// NN parameters
	JTextField learningRate;
	JTextField momentum;
	JTextField trainingTime;
	JTextField hiddenLayers;
	
	// RT parameters
	JTextField MaxDepth;
	JTextField MinNumberPerLeaf;
	JCheckBox noPruning;
	
	// LWL parameters
	JTextField knn;
	JTextField kernel;
	
	public WisardStepParameters(Map<XTrace, LinkedList<Map<String, Object>>> traceInstanceMap, Map<String, Type> attTypes, String indepAttName) {
		super(TITLE);
		
		this.traceInstanceMap = traceInstanceMap;
		this.attTypes = attTypes;
		this.indepAttName = indepAttName;
		
		SlickerFactory factory = SlickerFactory.instance();
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		int row = 0;
		
		
		// select the instance
		gbc.fill = GridBagConstraints.HORIZONTAL;  
	    gbc.gridx = 0;  
	    gbc.gridy = row;  
	    JLabel label = factory.createLabel("Select the instance:");
		add(label, gbc); 
	    gbc.gridx = 1;  
	    gbc.gridy = row;  
	    instanceID = factory.createComboBox(caseIDs());
		instanceID.setSelectedIndex(0);
		add(instanceID, gbc);
		row++;
		// TODO add action linstener		
		
		// select the good values
		gbc.fill = GridBagConstraints.HORIZONTAL;  
		gbc.fill = GridBagConstraints.HORIZONTAL;  
	    gbc.gridx = 0;  
	    gbc.gridy = row;  
	    JLabel goodResultLabel = factory.createLabel("Select the good result:");
	    add(goodResultLabel, gbc);
	    gbc.gridx = 1;  
	    gbc.gridy = row;  
		if (attTypes.get(indepAttName).equals(Type.CONTINUOS)) {
			thresholdDouble = SlickerFactory.instance().createNiceDoubleSlider("Threshold", (double) getMinMax()[0], 
					(double) getMinMax()[1], (double) getMinMax()[0], NiceSlider.Orientation.HORIZONTAL);
			add(thresholdDouble, gbc);
		} else if (attTypes.get(indepAttName).equals(Type.DISCRETE)) {
			System.out.println(((Long) getMinMax()[0]).intValue());
			System.out.println(((Long) getMinMax()[1]).intValue());
			System.out.println(((Long) getMinMax()[1]));
			System.out.println(((Long) getMinMax()[1]));
			thresholdInt = SlickerFactory.instance().createNiceDoubleSlider("Threshold", ( (long) getMinMax()[0]), 
					(Long) getMinMax()[1], (Long) getMinMax()[0], NiceSlider.Orientation.HORIZONTAL);
			add(thresholdInt, gbc);
		} else if (attTypes.get(indepAttName).equals(Type.LITERAL)) {
			DefaultListModel<JCheckBox> model = new DefaultListModel<JCheckBox>();
		    String[] values = getLiteralValues();
		    for (int i = 0; i < values.length; i++)
		    	model.addElement(new JCheckBox(values[i]));
		    CheckBoxList list = new CheckBoxList(model);
		    ProMList l = new ProMList("no 1", model);
		    list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		    list.setVisibleRowCount(3);
		    JScrollPane scrollBar = new JScrollPane(list);
			add(l, gbc);
		} else if (attTypes.get(indepAttName).equals(Type.BOOLEAN)) {
			String[] values = {"true", "false"};
			goodValues = new JList(values);
			goodValues.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			goodValues.setVisibleRowCount(2);
			add(goodValues, gbc);
		}
		row++;
		
		
		//is the lower values that are lower than the given threshold desirable
		gbc.fill = GridBagConstraints.HORIZONTAL; 
	    gbc.gridx = 0;  
	    gbc.gridy = row;
	    add(new JLabel("Is lower desirable:"), gbc);
	    gbc.gridx = 1;  
	    gbc.gridy = row;
	    isLowerDesirable = new JCheckBox("Lower", true);
	    add(isLowerDesirable, gbc);
	    row++;
		
		// at least on of random selection or optimization should be selected
		gbc.fill = GridBagConstraints.HORIZONTAL;  
	 	gbc.gridx = 1;  
	 	gbc.gridy = row;  
	 	JLabel randOrOpt = new JLabel("At least one of the random selection or randomization should be selected?");  
	 	add(randOrOpt, gbc);
	 	row++;
		
		// Is random sampling used? 
		gbc.fill = GridBagConstraints.HORIZONTAL;  
	    gbc.gridx = 1;  
	    gbc.gridy = row;  
	    ifRandSampling = new JCheckBox("Random Sampling", true);  
	    add(ifRandSampling, gbc);
	    row++;
	    
	 // Do use optimization? 
	    gbc.fill = GridBagConstraints.HORIZONTAL;  
	 	gbc.gridx = 1;  
	 	gbc.gridy = row;  
	 	doOptimize = new JCheckBox("Use Optimization", false);  
	 	add(doOptimize, gbc);
	 	row++;
	 	
	 	doOptimize.addActionListener(new ActionListener(){  
		    public void actionPerformed(ActionEvent e){  
		    	optimizationParameters();  
		    }

			private void optimizationParameters() {
				String[] options = {"OK"};
				JPanel panel = new JPanel();
				panel.setLayout(new GridLayout(4,2));
				
				if (hasTimeRelatedAtt()) {
					JLabel timeStepLabel = new JLabel("Step size for time related situation features: ");
					timeStep = new JTextField("in houre");
					timeStep.addFocusListener(new FocusListener() {
						public void focusGained(FocusEvent e) {
							timeStep.setText("");
						}

						public void focusLost(FocusEvent e) {
							// TODO Auto-generated method stub
							
						}
					});
					panel.add(timeStepLabel);
					panel.add(timeStep);
			 	}
				
				JLabel nonTimeStepLabel = new JLabel("Learning rate: ");
				nonTimeStep = new JTextField("a number in (0,1)");
				nonTimeStep.addFocusListener(new FocusListener() {
					public void focusGained(FocusEvent e) {
						nonTimeStep.setText("");
					}

					public void focusLost(FocusEvent e) {
						// TODO Auto-generated method stub
						
					}
				});
				panel.add(nonTimeStepLabel);
				panel.add(nonTimeStep);
				
				
				int selectedOption = JOptionPane.showOptionDialog(null, panel, "Select Step Size for Optimization:", JOptionPane.NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options , options[0]);

				if(selectedOption == 0)
					return;
			}  
	    });
	 		    
	    // What metric is used for the final report?
	    gbc.fill = GridBagConstraints.HORIZONTAL; 
	    gbc.gridx = 0;  
	    gbc.gridy = row;
	    add(new JLabel("Select the method:"), gbc);
	    gbc.gridx = 1;  
	    gbc.gridy = row;
	    ClassificationMethod[] methods = {ClassificationMethod.SEM, ClassificationMethod.NN, ClassificationMethod.LWL, ClassificationMethod.RT};
	    metodComboBox = factory.createComboBox(methods);
	    metodComboBox.setPreferredSize(metodComboBox.getMaximumSize());
	    metodComboBox.setSelectedIndex(0);
	    add(metodComboBox, gbc);
	    metodComboBox.addActionListener(new MethodListener());  
	    row++;
	    
	 // What metric is used for the final report?
	    gbc.fill = GridBagConstraints.HORIZONTAL; 
	    gbc.gridx = 0;  
	    gbc.gridy = row;
	    add(new JLabel("Select the metric:"), gbc);
	    gbc.gridx = 1;  
	    gbc.gridy = row;
	    Metric[] metrics = {Metric.L1, Metric.L2, Metric.MAD_L1};
	    JComboBox<ClassificationMethod> metricComboBox = factory.createComboBox(metrics);
	    metricComboBox.setPreferredSize(metricComboBox.getMaximumSize());
	    metricComboBox.setSelectedIndex(0);
	    add(metricComboBox, gbc);
	    row++;
	    
	    gbc.fill = GridBagConstraints.HORIZONTAL; 
	    gbc.gridx = 0;  
	    gbc.gridy = row;
	    add(new JLabel("Select the actionable attribute:"), gbc);
	    gbc.gridx = 1;  
	    gbc.gridy = row;
	    actionableAtts = new CheckComboBox(attTypes.keySet());
	    add(actionableAtts, gbc);
	    row++;
		//selectedValues  = cbb.getSelectedItems();
	}
	
	/**
	 * return True if the table contains timestamp, remaining time, elapsed time, or duration
	 * o.w., false.
	 * @return
	 */
	private boolean hasTimeRelatedAtt() {
		for (String attName : attTypes.keySet()) {
			String name = attName.toLowerCase();
			if (name.contains("duration") || name.contains("timestamp") || name.contains("elapsed_time") || name.contains("remaining_time"))
				return true;
		}
		
		return false;
	}

	private String[] getLiteralValues() {
		if (!attTypes.get(indepAttName).equals(Type.LITERAL))
			return null;
		
		Set<String> values = new HashSet<String>();
		for (XTrace trace : traceInstanceMap.keySet()) 
			for (Map<String, Object> instance : traceInstanceMap.get(trace)) 
				values.add((String) instance.get(indepAttName));
		
		String[] array = new String[values.size()];
		int idx = 0;
		for (String str : values) {
			array[idx] = str;
			idx++;
		}
		return array;
	}
	
	private Object[] caseIDs() {
		
		LinkedList<Object> ids = new LinkedList<Object>();
		idInstance = new HashMap<String, Map<String, Object>>();
		
		Writer output = null;	// This part added for evaluation
	    File file = new File("TDData.txt");	// This part added for evaluation
	    try {
			output = new BufferedWriter(new FileWriter(file)); // This part added for evaluation
			String h = headerString();
	        output.write(h);
	        output.write(System.getProperty( "line.separator" ));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();  // This part added for evaluation
		}
		
		for (XTrace trace : traceInstanceMap.keySet()) {
			String name = XConceptExtension.instance().extractName(trace);
			LinkedList<Map<String, Object>> instanceList = traceInstanceMap.get(trace);
//			if (traceInstanceMap.get(trace).size() == 1) {
//				String label = name + " [" + instanceList.get(0).get(indepAttName)+ "]";
//				ids.add(label);
//				idInstance.put(label, instanceList.get(0));
//			}  
			

			if (traceInstanceMap.get(trace).size() == 1) {     // This part added for evaluation
				if (instanceList.get(0).size() == attTypes.size()) { 	 // This part added for evaluation
					String label = name + " [" + instanceList.get(0).get(indepAttName)+ "]";		 // This part added for evaluation
					ids.add(label);	 // This part added for evaluation
					idInstance.put(label, instanceList.get(0));	 // This part added for evaluation
					writeToFile(instanceList.get(0), output);	 // This part added for evaluation  //TODO remove this part
				}  // This part added for evaluation
			}
			else {
				for (int idx = 0; idx <= traceInstanceMap.get(trace).size(); idx++) {
					String label = name + " -- " + idx + " [" + instanceList.get(idx).get(indepAttName)+ "]"; 
					ids.add(label);
					idInstance.put(label, instanceList.get(idx));
				}
			}
		}
		
		try {
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    // This part added for evaluation
//        System.out.println("File has been written");     // This part added for evaluation
		
		return ids.toArray();
	}

	private void writeToFile(Map<String, Object> map, Writer output) {
		// TODO remove this function
		try{  
			boolean tooBig = false;
			long t = 999999999;
			for (String attName : attTypes.keySet()) {
				if (Long.parseLong(map.get(attName).toString()) > t)
					tooBig = true;
			}
			
			if (!tooBig) {
				String r = new String();
		        for (String attName : attTypes.keySet()) {
		        	r = r + map.get(attName).toString() + ","; //String.format("%.2f", clsLabel1)
		        }
		        output.write(r.substring(0, r.length()-1));
		        output.write(System.getProperty( "line.separator" ));
			}
	    }catch(Exception e){
	    	System.out.println(e);
	        System.out.println("Could not create file");
	    }
		
	}
	
	private String headerString() {
		String h = new String();
		for (String attName : attTypes.keySet())
			h = h + attName + ",";
		
		return h.substring(0, h.length()-1);
	}

	public CounterfactualParameters apply(CounterfactualParameters params, JComponent component) {
		System.out.println(" threshold : " + thresholdInt.getValue());
		if (canApply(params, component)) {
			if (attTypes.get(indepAttName).equals(Type.CONTINUOS))
				params.setGoodResultNmericalThreshold(thresholdDouble.getValue());
			else if (attTypes.get(indepAttName).equals(Type.DISCRETE))
				params.setGoodResultNmericalThreshold(thresholdInt.getValue());
			else if (attTypes.get(indepAttName).equals(Type.LITERAL))
				params.setValues(goodValues.getSelectedValuesList());
			else if (attTypes.get(indepAttName).equals(Type.BOOLEAN))
				params.setValues(goodValues.getSelectedValuesList());
			
			if (metodComboBox.getSelectedItem().equals(ClassificationMethod.SEM)) 
				params.setSEM(sem);
			
			System.out.println(instanceID.getSelectedItem());
			params.setCurrentInstance(idInstance.get(instanceID.getSelectedItem()));
			
			setParameters(params);
			
			if (actionableAtts == null)
				params.setActionableAttNames(attTypes.keySet());
			else
				if (!actionableAtts.getSelectedItems().isEmpty())
					params.setActionableAttNames(actionableAtts.getSelectedItems());
			
			if (isLowerDesirable.isSelected())
				params.setLowerOrHeigher(LowOrHeigh.LOWER);
			else
				params.setLowerOrHeigher(LowOrHeigh.HEIGHER);
			
			if (doOptimize.isSelected()) {
				params.setDoOptimization(true);
				if (timeStep != null)
					params.setTimeStep(Double.parseDouble(timeStep.getText()));
				if (nonTimeStep != null)
					params.setTimeStep(Double.parseDouble(nonTimeStep.getText()));
			} else
				params.setDoOptimization(false);
		}
		return params;
	}

	private void setParameters(CounterfactualParameters params) {
		if (metodComboBox.getSelectedItem().equals(ClassificationMethod.NN)) {
			params.setMethod(ClassificationMethod.NN);
			params.setLearningRate(Double.parseDouble(learningRate.getText()));  
			params.setMomentum(Double.parseDouble(momentum.getText()));
			params.setNumEpoches(Integer.parseInt(trainingTime.getText()));
			params.setHiddenLayers(hiddenLayers.getText());
		} else if (metodComboBox.getSelectedItem().equals(ClassificationMethod.RT)) {
			params.setMethod(ClassificationMethod.RT);
			params.setMaxDepth(Integer.parseInt(MaxDepth.getText()));
			params.setMinNum(Integer.parseInt(MinNumberPerLeaf.getText()));
			params.setNoPruning(noPruning.isSelected());
		} if (metodComboBox.getSelectedItem().equals(ClassificationMethod.LWL)) {
			params.setMethod(ClassificationMethod.LWL);
			params.setKNN(Integer.parseInt(knn.getText()));
			params.setKernel(Integer.parseInt(kernel.getText()));
		} if (doOptimize.isSelected()) {
			if (hasTimeRelatedAtt())
				params.setTimeStep(Double.parseDouble(timeStep.getText()));
			else
				params.setTimeStep(0);
			
			boolean numeric = true;

	        try { // If the text in nonTimeStep is a valid double
	            Double num = Double.parseDouble(nonTimeStep.getText());
	        } catch (NumberFormatException e) {
	            numeric = false;
	        }
			if (numeric)
			params.setNonTimeStep(Double.parseDouble(nonTimeStep.getText()));
		}
		
	}

	public boolean canApply(CounterfactualParameters params, JComponent component) {
		return component instanceof WisardStepParameters;
	}

	public JComponent getComponent(CounterfactualParameters params) {
		return this;
	}

	public String getTitle() {
		return TITLE;
	}
	
	public void setAttTypes(HashMap<String, Type> types) {
		 attTypes = types;
	}
	
	public Map<String, Type> getAttTypes() {
		return attTypes;
	}
	
	public void setIndepAttName (String name) {
		indepAttName = name;
	}
	
	public String getIndepAttName() {
		return indepAttName;
	}
	
	public Object[] getMinMax(String attName) {
		Object[] minMax = new Object[2];
		
		for (XTrace trace : traceInstanceMap.keySet()) {
			for (Map<String, Object> instance : traceInstanceMap.get(trace)) {
				if (attTypes.get(attName).equals(Type.CONTINUOS)) {
					if (minMax[0] == null || (double) instance.get(indepAttName) < (double) minMax[0])
						minMax[0] = instance.get(indepAttName);
					if (minMax[1] == null || (double) instance.get(indepAttName) > (double) minMax[1])
						minMax[1] = instance.get(indepAttName);
				}
				else if (attTypes.get(attName).equals(Type.DISCRETE) || attTypes.get(attName).equals(Type.TIMESTAMP)) {
					if (minMax[0] == null || (long) instance.get(indepAttName) < (long) minMax[0])
						minMax[0] = instance.get(indepAttName);
					if (minMax[1] == null || (long) instance.get(indepAttName) > (long) minMax[1])
						minMax[1] = instance.get(indepAttName);
				}
			}
		}
		return minMax;
	}
	
	public Object[] getMinMax() {
		return getMinMax(indepAttName);
	}
	
	public File semFile(){
        
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setCurrentDirectory(new File("/User/alvinreyes"));
         
        int result = jFileChooser.showOpenDialog(new JFrame());
     
     
        if (result == JFileChooser.APPROVE_OPTION) 
            return jFileChooser.getSelectedFile();
        
        return null;
    }
	
	public class MethodListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			File semFile = null;
			if (e.getSource().equals(metodComboBox))
				if (metodComboBox.getSelectedItem().equals(ClassificationMethod.SEM)) {
					semFile = semFile();
					
					BufferedReader br = null;
					try {
						br = new BufferedReader(new FileReader(semFile));
					} catch (FileNotFoundException e1) {
						System.out.println(" line 290 file not found");
						e1.printStackTrace();
					} 
					  
					  String st;
					  sem = new String();
					  try {
						while ((st = br.readLine()) != null) 
							    sem = sem + st + "\n";
					  } catch (IOException e1) {
						  System.out.println(" line 301 reading a line from a file");
						  e1.printStackTrace();
					  }
				} else if (metodComboBox.getSelectedItem().equals(ClassificationMethod.NN)) {
					getNNParameters();
				} else if (metodComboBox.getSelectedItem().equals(ClassificationMethod.LWL)) {
					getLWLParameters();
				} else if (metodComboBox.getSelectedItem().equals(ClassificationMethod.RT)) {
					getRTParameters();
				}
					
			
			
		}
		
		private void getLWLParameters() {
			String[] options = {"OK"};
			JPanel panel = new JPanel();
			panel.setLayout(new GridLayout(4,2));
			
			JLabel knnLabel = new JLabel("number of neighbours: ");
			knn = new JTextField(" defauld -1");
			knn.addFocusListener(new Focus());
			panel.add(knnLabel);
			panel.add(knn);
			
			JLabel kernelLabel = new JLabel("weighting kernel shape: ");
			kernel = new JTextField("0=Linear, 1=Epanechnikov," + 
					" 2=Tricube, 3=Inverse, 4=Gaussian");
			kernel.addFocusListener(new Focus());
			panel.add(kernelLabel);
			panel.add(kernel);
			
			
			int selectedOption = JOptionPane.showOptionDialog(null, panel, "Select Locally weighted learning Parameters:", JOptionPane.NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options , options[0]);

			if(selectedOption == 0)
				return;
		}
		
		private void getRTParameters() {
			String[] options = {"OK"};
			JPanel panel = new JPanel();
			panel.setLayout(new GridLayout(4,2));
			
			JLabel MaxDepthLabel = new JLabel("Maximum tree depth: ");
			MaxDepth = new JTextField(" defauld -1");
			MaxDepth.addFocusListener(new Focus());
			panel.add(MaxDepthLabel);
			panel.add(MaxDepth);
			
			JLabel MinNumberPerLeafLabel = new JLabel("minimum number of instances: ");
			MinNumberPerLeaf = new JTextField("default -1");
			MinNumberPerLeaf.addFocusListener(new Focus());
			panel.add(MinNumberPerLeafLabel);
			panel.add(MinNumberPerLeaf);
			
			noPruning = new JCheckBox(" no Pruning");
			
			
			int selectedOption = JOptionPane.showOptionDialog(null, panel, "Select Locally weighted learning Parameters:", JOptionPane.NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options , options[0]);

			if(selectedOption == 0)
				return;
		}

		private void getNNParameters() {
			String[] options = {"OK"};
			JPanel panel = new JPanel();
			panel.setLayout(new GridLayout(8,2));
			
			JLabel learnigRateLabel = new JLabel("Learning rate: ");
			learningRate = new JTextField("a number in (0,1)");
			learningRate.addFocusListener(new Focus());
			panel.add(learnigRateLabel);
			panel.add(learningRate);
			
			JLabel momentumLabel = new JLabel("Mumentum: ");
			momentum = new JTextField("a number in (0,1)");
			momentum.addFocusListener(new Focus());
			panel.add(momentumLabel);
			panel.add(momentum);
			
			JLabel trainingTimeLabel = new JLabel("Training time: ");
			trainingTime = new JTextField("number of Epoches");
			trainingTime.addFocusListener(new Focus());
			panel.add(trainingTimeLabel);
			panel.add(trainingTime);
			
			JLabel hiddenLayersLabel = new JLabel("Hidden layers: ");
			hiddenLayers = new JTextField("e.x. 16,8");
			hiddenLayers.addFocusListener(new Focus());
			panel.add(hiddenLayersLabel);
			panel.add(hiddenLayers);
			
			
			int selectedOption = JOptionPane.showOptionDialog(null, panel, "Select Nural Network Parameters:", JOptionPane.NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options , options[0]);

			if(selectedOption == 0)
				return;
		}
		
		
		public class Focus implements FocusListener {

			public void focusGained(FocusEvent e) {
				if (e.getSource().equals(learningRate)) {
					learningRate.setText("");
				} else if (e.getSource().equals(momentum)) {
					momentum.setText("");
				} else if (e.getSource().equals(trainingTime)) {
					trainingTime.setText("");
				} else if (e.getSource().equals(hiddenLayers)) {
					hiddenLayers.setText("");
				} else if (e.getSource().equals(MaxDepth)) {
					MaxDepth.setText("");
				} else if (e.getSource().equals(MinNumberPerLeaf)) {
					MinNumberPerLeaf.setText("");
				} else if (e.getSource().equals(knn)) {
					knn.setText("");
				} else if (e.getSource().equals(kernel)) {
					kernel.setText("");
				}
			}

			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				
			}
		}
	}
}
