package org.processmining.CounterfactualRecommendation.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.deckfour.xes.model.XLog;
import org.processmining.CounterfactualRecommendation.algorithms.DataExtraction;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;
import org.processmining.models.graphbased.directed.DirectedGraphNode;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

import com.fluxicon.slickerbox.components.NiceDoubleSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

import csplugins.id.mapping.ui.CheckComboBox;

public class WizardStepTable extends ProMPropertiesPanel implements ProMWizardStep<DataExtraction> {

	private static final String TITLE = " "; //Configure Situation Feature Table";
	
	private JComboBox<String> depActComboBox;
	
	private JComboBox<String> depAttComboBox;
	
	JList traceAttributeList;
	
	JList activityNamesList;
	
	JList timePerspectiveList;
	
	JList resourcePerspectiveList;
	
	JList controlFlowPerspectiveList;
	
	JList otherAttributesList;
	
	JRadioButton traceSituation;
	
	JRadioButton choiceSituation;
	
	JRadioButton eventSituation;
	
	String minThreshold;
	String maxThreshold;
	Collection<String> selectedValues;
	
	private DataExtraction de;
	
	String depPlace;
	
	Set<Place> indepChoicePlaces = null;
	
	double delayThreshold = 0;
	
	Set<Transition> subModel = null;
	
	XLog log;
	
	Petrinet model; 
	
	PNRepResult res;
	
	PluginContext context;
	
	public WizardStepTable(PluginContext context, XLog log, Petrinet model, PNRepResult res) {
		super(TITLE);
		this.de = new DataExtraction(log, model, res);
		this.log = log;
		this.model = model;
		this.res = res;
		this.context = context;
		de.init();
		Set<Place> dependentChoicePlaces;
		
		SlickerFactory factory = SlickerFactory.instance();
		Listener listener = new Listener();
		int numVisibleItmes = 2;
		int row = 0;
		
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints(); 
//		SpringLayout layout = new SpringLayout();
//		setLayout(layout);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;  
	    gbc.gridx = 0;  
	    gbc.gridy = row;  
	    traceSituation = new JRadioButton("Trace situation"); 
	    traceSituation.setSelected(true);
		add(traceSituation, gbc); 
	    gbc.gridx = 1;  
	    gbc.gridy = row;     
		choiceSituation =new JRadioButton("Choice situation");
		add(choiceSituation, gbc); 
	    gbc.gridx = 2;  
	    gbc.gridy = row; 
		eventSituation =new JRadioButton("Event situation");
		add(eventSituation, gbc);
		
		ButtonGroup group = new ButtonGroup();    
		group.add(choiceSituation);
		group.add(eventSituation);
		group.add(traceSituation);
		
		//Dependent Activity
		gbc.fill = GridBagConstraints.HORIZONTAL;  
	    gbc.gridx = 0;  
	    gbc.gridy = row;  
	    JLabel depActLabel;
	    depActLabel = factory.createLabel("Dependent activity");
		add(depActLabel, gbc);
		row++;
		
		gbc.fill = GridBagConstraints.HORIZONTAL; 
	    gbc.gridx = 1;  
	    gbc.gridy = row;  
	    String[] actNames = de.getActivitiesArray();
		Arrays.sort(actNames);
		depActComboBox = factory.createComboBox(actNames);
		depActComboBox.setSelectedIndex(0);
		depActComboBox.setEnabled(false);
		depActComboBox.setPreferredSize(depActComboBox.getMaximumSize());
		add(depActComboBox, gbc);  
		row++;
		
	    
		gbc.fill = GridBagConstraints.HORIZONTAL;  
	    gbc.gridx = 0;  
	    gbc.gridy = row;  
	    JLabel depAttLabel = factory.createLabel("Dependent attribute");
		add(depAttLabel, gbc); 
	    gbc.gridx = 1;  
	    gbc.gridy = row;  
	    String[] attNames = de.getTraceAttributeNamesArray();
		Arrays.sort(de.getActivitiesArray());
		depAttComboBox = factory.createComboBox(attNames);
		depAttComboBox.setPreferredSize(depAttComboBox.getMaximumSize());
		depAttComboBox.setSelectedIndex(0);
		add(depAttComboBox, gbc);  
		row++;
		
		//Independent Trace Attributes
		String[] traceAttributeNames = de.getArrayTraceAttributeNames();
		if (traceAttributeNames != null) {
			gbc.gridx = 0;  
			gbc.gridy = row; 
			JLabel traceAttLabel = new JLabel("Select relevant trace or choice attributes:");
			this.add(traceAttLabel, gbc);
			
			gbc.gridx = 1;  
			gbc.gridy = row;
			Arrays.sort(traceAttributeNames);
			traceAttributeList = new JList(traceAttributeNames);
			traceAttributeList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			traceAttributeList.setVisibleRowCount(numVisibleItmes);
			traceAttributeList.addListSelectionListener(listener);
	        JScrollPane listScroller1 = new JScrollPane(traceAttributeList);
			add(listScroller1, gbc);
			row++;
		}
		
		// Relevant Independent Activities
		gbc.fill = GridBagConstraints.HORIZONTAL;  
	//	gbc.ipady = 20;  
		gbc.gridx = 0;  
		gbc.gridy = row; 
		JLabel inDepActLabel = new JLabel("Select relevant activities:");
		add(inDepActLabel, gbc);
		gbc.gridx = 1;  
		gbc.gridy = row; 
		String[] activityNames = de.getActivitiesArray();
		Arrays.sort(activityNames);			
		activityNamesList = new JList(activityNames);
		activityNamesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		activityNamesList.setVisibleRowCount(2);				
//		activityNamesList.addListSelectionListener(listener);
		JScrollPane listScrollerAct = new JScrollPane(activityNamesList);
		add(listScrollerAct, gbc);
		row++;
	  	
	    //Time Perspective Attributes
	  	gbc.fill = GridBagConstraints.HORIZONTAL;  
//	    gbc.ipady = 20;  
	    gbc.gridx = 0;  
	    gbc.gridy = row; 
	  	JLabel timePerspectiveLabel = new JLabel("Time perspective attributes:");
	  	add(timePerspectiveLabel, gbc);
	  	gbc.gridx = 1;  
	    gbc.gridy = row;
	  	String[] timePerspectiveAtts = { "Activity Duration", "Elapsed Time", "Remaining Time", "Timestamp"};
		timePerspectiveList = new JList(timePerspectiveAtts);
		timePerspectiveList.setVisibleRowCount(numVisibleItmes);
		timePerspectiveList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//		timePerspectiveList.addListSelectionListener(listener);
		JScrollPane listScrollerTime = new JScrollPane(timePerspectiveList);
		add(listScrollerTime, gbc);	
		row++;
		
		//Resource Perspective Attributes
	  	gbc.fill = GridBagConstraints.HORIZONTAL;   
	    gbc.gridx = 0;  
	    gbc.gridy = row; 
	  	JLabel resourcePerspectiveLabel = new JLabel("Resource perspective attributes:");
	  	add(resourcePerspectiveLabel, gbc);
	  	gbc.gridx = 1;  
	    gbc.gridy = row;
	    String[] resourcePerspectiveAtts = { "Executor_Group", "Resource", "Total Resource Workload"};
		resourcePerspectiveList = new JList(resourcePerspectiveAtts);
		resourcePerspectiveList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		resourcePerspectiveList.setVisibleRowCount(numVisibleItmes);
//		resourcePerspectiveList.addListSelectionListener(listener);
		JScrollPane listScrollerResource = new JScrollPane(resourcePerspectiveList);
		add(listScrollerResource, gbc);
		row++;
		
		//Control-flow Perspective Attributes
	  	gbc.fill = GridBagConstraints.HORIZONTAL;   
	    gbc.gridx = 0;  
	    gbc.gridy = row; 
	  	JLabel controlFlowPerspectiveLabel = new JLabel("Control-flow perspective attributes:");
	  	add(controlFlowPerspectiveLabel, gbc);
	  	gbc.gridx = 1;  
	    gbc.gridy = row;
	    String[] controlFlowPerspectiveAtts = {"Next Activity", "Previous Activity"};
		controlFlowPerspectiveList = new JList(controlFlowPerspectiveAtts);
		controlFlowPerspectiveList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		controlFlowPerspectiveList.setVisibleRowCount(numVisibleItmes);
//		controlFlowPerspectiveList.addListSelectionListener(listener);
		JScrollPane listScrollercontrolFlow = new JScrollPane(controlFlowPerspectiveList);
		add(listScrollercontrolFlow, gbc);
		row++;
		
		//Other Attributes
	  	gbc.fill = GridBagConstraints.HORIZONTAL;   
	    gbc.gridx = 0;  
	    gbc.gridy = row; 
	  	JLabel otherAttributesLabel = new JLabel("Other attributes:");
	  	add(otherAttributesLabel, gbc);
	  	gbc.gridx = 1;  
	    gbc.gridy = row;
		otherAttributesList = new JList(arrayListToArray(de.getOriginalLogAttributes()));
		otherAttributesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		otherAttributesList.setVisibleRowCount(numVisibleItmes);
//		controlFlowPerspectiveList.addListSelectionListener(listener);
		JScrollPane listSotherAttributes = new JScrollPane(otherAttributesList);
		add(otherAttributesList, gbc);
		row++;
		
		traceSituation.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				
				if (traceSituation.isSelected()) {
					depActComboBox.setEnabled(false);
					depAttComboBox.setEnabled(true);
					depAttComboBox.setModel( new DefaultComboBoxModel<String>( de.getArrayTraceAttributeNames()) );
				}
			}
        });
		
		eventSituation.addActionListener(new ActionListener() { // TODO
			
			public void actionPerformed(ActionEvent arg0) {
				
				if (eventSituation.isSelected()) {

					String[] groupingAtts = {"Resource", "Timestamp", "Duration"};
				    String[] actNames = de.getActivitiesArray();
					Arrays.sort(actNames);
					actNames = concatenate(groupingAtts, actNames);
					depActComboBox.setModel( new DefaultComboBoxModel<String>(actNames));
					depActComboBox.setEnabled(true);
					
					depAttComboBox.setEnabled(true);
					String[] allAttributes = concatenate(timePerspectiveAtts, resourcePerspectiveAtts);
			 		allAttributes = concatenate(allAttributes, concatenate(controlFlowPerspectiveAtts,arrayListToArray(de.getOriginalLogAttributes())));
			 		Arrays.sort(allAttributes);
			 		depAttComboBox.setModel( new DefaultComboBoxModel<String>(allAttributes));
				}	
			}
        });
		
		choiceSituation.addActionListener(new ActionListener() { // TODO
			
			public void actionPerformed(ActionEvent arg0) {
				
				if (choiceSituation.isSelected()) {
					de.setTargetActivityName("Choice");
					try {
						SelectionUtil selUtil = new SelectionUtil(context, model);
						Set<DirectedGraphNode> selection = selUtil.getChoice("Select the dependent choice place", true);
						Place p = null;
						for (DirectedGraphNode node : selection) {
							p = (Place)node;
						}
						depPlace = p.getLabel();
						depActComboBox.setEnabled(false);
						depAttComboBox.setEnabled(false);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}	
			}
        });

		depActComboBox.addActionListener(new ActionListener() { // TODO
			
			public void actionPerformed(ActionEvent arg0) {
				
				if (eventSituation.isSelected() && (((String) depActComboBox.getSelectedItem()).equals("Resource") ||
						((String) depActComboBox.getSelectedItem()).equals("Timestamp") ||
						((String) depActComboBox.getSelectedItem()).equals("Duration"))) {
					System.out.println((String) depActComboBox.getSelectedItem());
					showWhichActsPupup();
				}	
			}

			private void showWhichActsPupup() {
				// TODO Auto-generated method stub
				JPanel p=new JPanel( new GridLayout(5,1));
				String message = "Please select the attribute values.";
				JLabel label = new JLabel(message);
				p.add(label);
				String actName = (String) depActComboBox.getSelectedItem();
				JTextField min = null;
				JTextField max = null;
				CheckComboBox cbb = null;
				JLabel l = null;
				if (actName.equals("Resource")) {
					if (de.getAllLiteralValues() != null && de.getAllLiteralValues().containsKey("org:resource")) {
						cbb = new CheckComboBox(de.getAllLiteralValues().get("org:resource"));
				    	Dimension dim=cbb.getPreferredSize();
				    	dim.width*=2;
				    	cbb.setPreferredSize(dim);
				    	p.add(cbb);
					}
				} else if (actName.equals("Timestamp")) {
					Set<String> days = new HashSet<String>();
					days.add("Sunday");
					days.add("Monday");
					days.add("Tuesday");
					days.add("Wednesday");
					days.add("Thursday");
					days.add("Friday");
					days.add("Saturday");
					
					cbb = new CheckComboBox(days);
					p.add(cbb);
					min = new JTextField("00:00");
					max = new JTextField("00:00");
					p.add(min);
					p.add(max);

				} else if (actName.contains("Duration") || actName.contains("duration")) {
					l = new JLabel("Min : " + de.getMinAllActDuration() + "Max : " + de.getMaxAllActDuration());
					min = new JTextField(de.getMinAllActDuration() + " ");
					max = new JTextField(de.getMaxAllActDuration() + " ");
					p.add(min);
					p.add(max);
				}
				
		    	int yn=JOptionPane.showConfirmDialog(null, 
						p,message,JOptionPane.YES_NO_OPTION);
				if (yn==JOptionPane.NO_OPTION)
					return;
				
				if (de.getAllLiteralValues() != null && actName.equals("Resource") && de.getAllLiteralValues().containsKey("org:resource"))
					selectedValues  = cbb.getSelectedItems();
				
				if (actName.equals("Timestamp")) {
					selectedValues  = cbb.getSelectedItems();
					minThreshold = min.getText();
					maxThreshold = max.getText();
				}
				if (actName.contains("Duration") || actName.contains("duration")) {
					minThreshold = min.getText();
					maxThreshold = max.getText();
				}
			}
        });
	}
	
	public String getDependentActiviry() {
		if (traceSituation.isSelected())
			return "Trace";
		if (choiceSituation.isSelected())
			return "Choise";
					
		return (String) depActComboBox.getSelectedItem();
	}
	
	public String getDependentAttribute() {
		return (String) depAttComboBox.getSelectedItem();
	}

	private String[] addToArray(String str, String[] array) {
		String[] newArray = new String[array.length+1];
		newArray[0] = str;
		for (int i = 1; i <= array.length; i++)
			newArray[i] = array[i-1];
		return newArray;
	}
	
	public String[] arrayListToArray(ArrayList<String> list) {
		String[] array = new String[list.size()];
		int i = 0;
		for (String o : list) {
			array[i] = o;
			i++;
		}
		return array;
	}
	
	public String[] listToArray(List list) {
		String[] array = new String[list.size()];
		int i = 0;
		for (Object o : list) {
			array[i] = (String) o;
			i++;
		}
		return array;
	}
	
	private String[] concatenate(String[] array1, String[] array2) {
		
		if (array1 == null && array2 == null)
			return null;
		if (array1 == null)
			return array2;
		if (array2 == null)
			return array1;
		
		String[] array = new String[array1.length + array2.length];
		int i = 0;
		for (String str : array1) {
			array[i] = str;
			i++;
		}
		for (String str : array2) {
			array[i] = str;
			i++;
		}
		
		return array;
	}

	public DataExtraction apply(DataExtraction de, JComponent component) {
		if (canApply(de, component)) {
			// set dep act and att
			WizardStepTable step = (WizardStepTable) component;
			de.setDependentActName(step.getDependentActiviry());
			if (step.getDependentActiviry().equals("Choice"))
				de.setDependentAttName(depPlace);
			else
				de.setDependentAttName(step.getDependentAttribute());
			
			// set indep acts and atts
			if (!traceAttributeList.isSelectionEmpty()) 
				handelTraceAttributes(de);
			String[] allAttributes = null;
			if (!activityNamesList.isSelectionEmpty()) 
				de.setActivitiesToConsider(activityNamesList.getSelectedValuesList());	
			if (!timePerspectiveList.isSelectionEmpty()) 
				allAttributes = concatenate(allAttributes, listToArray(timePerspectiveList.getSelectedValuesList()));
			if (!resourcePerspectiveList.isSelectionEmpty()) 
				allAttributes = concatenate(allAttributes, listToArray(resourcePerspectiveList.getSelectedValuesList()));
			if (!controlFlowPerspectiveList.isSelectionEmpty()) 
				allAttributes = concatenate(allAttributes, listToArray(controlFlowPerspectiveList.getSelectedValuesList()));
			if (!otherAttributesList.isSelectionEmpty()) 
				allAttributes = concatenate(allAttributes, listToArray(otherAttributesList.getSelectedValuesList()));
			if(allAttributes != null)
				de.setSelectedEventAttributes(allAttributes);
			if (selectedValues != null && !selectedValues.isEmpty())
				de.setAllActivityAttValues(selectedValues);
		}
		return de;
	}
	
	private void handelTraceAttributes(DataExtraction de) {
		de.setSelectedTraceAttributes(listToArray(traceAttributeList.getSelectedValuesList()));
		if (doesInclude(traceAttributeList.getSelectedValuesList(), "Choice Attribute"))
			de.setSelectedORplaces(indepChoicePlaces);
		if (doesInclude(traceAttributeList.getSelectedValuesList(), "Sub Model Attribute"))
			de.setSelectetSub_model(subModel);
		if (doesInclude(traceAttributeList.getSelectedValuesList(), "Trace Delay"))
			de.setTraceDelayThreshold(delayThreshold);	
	}
	
	private boolean doesInclude(List l, String value) {
		for (Object element : l) {
		    if (((String)element).equals(value)) {
		        return true;
		    }
		}
		
		return false;
	}

	public String getTitle() {
		return TITLE;
	}
	
	public static <T> Object[] toArray(final Iterable<T> values) {
		final ArrayList<T> valueList = new ArrayList<T>();
		for (final T value : values) {
			valueList.add(value);
		}
		return valueList.toArray();
	}

	public boolean canApply(DataExtraction model, JComponent component) {
		return component instanceof WizardStepTable;
	}

	public JComponent getComponent(DataExtraction model) {
		return this;
	}
	
	
	private class Listener implements ListSelectionListener {  
		/**
		 * This class save the names of the dependent and independent attributes and activity 
		 * names (and relevant parameters)
		 * selected by the used in the "TableConfigurationWizard". These values will
		 * get set to the relevant attribute in the DataExtraction class.
		 * 
		 */
		NiceDoubleSlider traceDelayThresholdSlider;
		boolean isChoiceHandeled = false;
		boolean isSubModelHandeled = false;
		boolean isTraceDelayHandeled = false;
		
		public void valueChanged(ListSelectionEvent e) {
			
			if (e.getSource().equals(traceAttributeList)) 
				handelTraceAttributes();				
		}
		
		private void handelTraceAttributes() {
		//	de.addTraceIndepAtts(traceAttributeList.getSelectedValuesList());
			if (doesInclude(traceAttributeList.getSelectedValuesList(), "Choice Attribute") && !isChoiceHandeled)
				handelChoice();
			else 
				releaseChoice();
			if (doesInclude(traceAttributeList.getSelectedValuesList(), "Sub Model Attribute") && !isSubModelHandeled)
				handelSubModel();
			else
				releaseSubModel();
			if (doesInclude(traceAttributeList.getSelectedValuesList(), "Trace Delay") && !isTraceDelayHandeled)
				handelTraceDelay();
			else
				releaseTraceDelay();
		}
		
		private void releaseTraceDelay() {
			isTraceDelayHandeled = false;
			delayThreshold = 0; 
		}

		private void releaseSubModel() {
			isSubModelHandeled = false; 
			subModel = null;
		}

		private void releaseChoice() {
			isChoiceHandeled = false;
			indepChoicePlaces = null;			
		}

		private boolean doesInclude(List l, String value) {
			for (Object element : l) {
			    if (((String)element).equals(value)) {
			        return true;
			    }
			}
			
			return false;
		}
		
		private void handelChoice() {
			try {
				SelectionUtil selUtil = new SelectionUtil(context, model);
				Set<DirectedGraphNode> selection = selUtil.getChoice("Select a Set of Choice Places", true);
				Set<Place> selectedChoicePlaces = new HashSet<Place>();
				for (DirectedGraphNode node : selection) {
					selectedChoicePlaces.add((Place)node);
				}
//				de.setSelectedORplaces(selectedChoicePlaces);
				indepChoicePlaces = selectedChoicePlaces;
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			this.isChoiceHandeled = true;
		}
		
		private void handelSubModel() {
			try {
				SelectionUtil selUtil = new SelectionUtil(context, model);
				Set<DirectedGraphNode> selection = selUtil.getChoice("Select a Set of Transitions", false);
				Set<Transition> selectedTransitions = new HashSet<Transition>();
				for (DirectedGraphNode transition : selection) {
					selectedTransitions.add((Transition)transition);
				}
//				de.setSelectetSub_model(selectedTransitions);
				subModel = selectedTransitions;
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			this.isSubModelHandeled = true;
		}
		
		private void handelTraceDelay() {
			JPanel p=new JPanel(new BorderLayout());
			SlickerFactory instance = SlickerFactory.instance();	
			traceDelayThresholdSlider = instance.createNiceDoubleSlider("Set the threshold for trace delay", 0, 1, 0.5, Orientation.HORIZONTAL);
	    	p.add(traceDelayThresholdSlider,BorderLayout.CENTER);
	    	p.add(new JLabel("Trace delay thereshold"),BorderLayout.NORTH);
	    	int yn=JOptionPane.showConfirmDialog(null, 
	    			p,"TRACE DELAY THRESHOLD",JOptionPane.YES_NO_OPTION);
	    	if (yn==JOptionPane.NO_OPTION) {
	    		this.isTraceDelayHandeled = false;
	    		return;
	    	}
	    
//	    	de.setTraceDelayThreshold(traceDelayThresholdSlider.getValue());
	    	delayThreshold = traceDelayThresholdSlider.getValue();
	    	this.isTraceDelayHandeled = true;
		}
		
	} 

}