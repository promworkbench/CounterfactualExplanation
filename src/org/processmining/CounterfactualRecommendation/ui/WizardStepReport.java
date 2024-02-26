package org.processmining.CounterfactualRecommendation.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.io.FileUtils;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import org.processmining.CounterfactualRecommendation.algorithms.GenerateFinalSamples;
import org.processmining.CounterfactualRecommendation.parameters.ClassificationMethod;
import org.processmining.datadiscovery.estimators.Type;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.framework.util.ui.widgets.WidgetImages;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;

import com.fluxicon.slickerbox.components.IconVerticalTabbedPane;

import javafx.util.Pair;

public class WizardStepReport extends ProMPropertiesPanel implements ProMWizardStep<GenerateFinalSamples> {

	private static final String TITLE = "Report";

	private GenerateFinalSamples finalSampleGenerator;
	
	/**
	 * The set of final counterfactual suggestions
	 */
	private LinkedList<Pair<Map<String, Double>, Double>> finalList;
	
	/**
	 * Panes for Distance and Window configuration
	 */
	private final IconVerticalTabbedPane tabbedpane;


	public WizardStepReport(GenerateFinalSamples fgs) {
		super(TITLE);
		finalSampleGenerator = fgs;
		// Basic layout is a BorderLayout
		setBackground(new Color(40, 40, 40));
		setBorder(BorderFactory.createEmptyBorder());
		setLayout(new BorderLayout());
	
		// Pane for selecting between Distance and Window Configurations
		tabbedpane = new IconVerticalTabbedPane(new Color(230, 230, 230, 210),
				new Color(20, 20, 20, 160));
		add(tabbedpane, BorderLayout.CENTER);
		
		// Add tabs for Window and Distance configuration panes
		tabbedpane.addTab("Suggestions", WidgetImages.inspectorIcon, createReportTab());
		tabbedpane.addTab("Num Att", WidgetImages.inspectorIcon, createDistTab("numAtt"));
	//	tabbedpane.addTab("MAD-L1", WidgetImages.inspectorIcon, createDistTab("MAD"));
	//	tabbedpane.addTab("L1-Distance", WidgetImages.inspectorIcon, createDistTab("L1"));
		tabbedpane.addTab("dependent Att", WidgetImages.inspectorIcon, createDistTab("output"));
		tabbedpane.addTab("All", WidgetImages.inspectorIcon, createDistTab("All"));
		tabbedpane.addTab("Tabular", WidgetImages.inspectorIcon, createTabularView(fgs));
				
	}
	
	private JComponent createTabularView(GenerateFinalSamples fgs) {
		TablePanel table = new TablePanel("Tabular", fgs);
		return table.createTable();
	}

	private JComponent createDistTab(String dist) {
		LinkedList<Pair<Map<String, Double>, Double>> distances = finalSampleGenerator.listDustance("numAtt");
		
		// The panel that will finally be returned
		final ProMPropertiesPanel numAttDistChart = new ProMPropertiesPanel("Counterfactual report");

		
		JPanel numAttDistPanel = new JPanel();
		numAttDistPanel.setOpaque(false);
		numAttDistPanel.setLayout(new BoxLayout(numAttDistPanel, BoxLayout.PAGE_AXIS));
		
		String title = null;
		if (dist.equals("numAtt"))
        	title = "Number of Different attributes";
        else if (dist.equals("MAD"))
        	title = "MAD-L1 Distance";
        else if (dist.equals("L1"))
        	title = "L1 Distance";
        else if (dist.equals("output"))
        	title = "Output Distance";
        else
        	title = "All Metrics";
		
        BarChartPanel demo = new BarChartPanel(title, finalSampleGenerator);
        CategoryDataset data = null;
        if (dist.equals("numAtt"))
        	data = demo.createDataset("numAtt");
        else if (dist.equals("MAD"))
        	data = demo.createDataset("MAD");
        else if (dist.equals("L1"))
        	data = demo.createDataset("L1");
        else if (dist.equals("output"))
        	data = demo.createDataset("output");
        else
        	data = demo.createDataset();
        
        JFreeChart c = demo.createChart(data);
        ChartPanel CP = new ChartPanel(c);
        
		numAttDistPanel.add(CP,BorderLayout.CENTER);
		numAttDistPanel.validate();
		
  //      demo.setVisible(true);
		
		numAttDistChart.addProperty(" Target:", numAttDistPanel);
		
		return numAttDistChart;

	}
	

	/**
	 * Creates the sliding window configuration panel
	 * @param paramBuilder Parameter builder that will contain the final configuration
	 * @return The created sliding window configuration panel
	 */
	public ProMPropertiesPanel createReportTab() {
		// The panel that will finally be returned
		final ProMPropertiesPanel rextReport = new ProMPropertiesPanel("Counterfactual Explanation(s)");
		
		// Generate the report
		finalList = finalSampleGenerator.getFinalList();
//		String report = generateReport();
		String report = generateFirstLineReport();	
		
		// Panel containing controls for adding windows and displaying the currently added windows 
		JPanel reportPanel = new JPanel();
		reportPanel.setOpaque(false);
		reportPanel.setLayout(new BoxLayout(reportPanel, BoxLayout.PAGE_AXIS));

		reportPanel.add(new JLabel(report));
		System.out.println(report);
		rextReport.addProperty(" Target:", reportPanel);
		
		// Generate the counterfactual samples
		int i = 1;
		for (Pair<Map<String, Double>, Double> sample : finalList) {
			report = new String();
			report = oneSuggention(sample);
			if (report == null)
				break;
			JPanel rPanel = new JPanel();
			rPanel.setToolTipText(report);
			System.out.println(i + " --> " + report);
			rPanel.setOpaque(false);
			rPanel.setLayout(new BoxLayout(rPanel, BoxLayout.PAGE_AXIS));
			rPanel.add(new JLabel(report));
			rextReport.addProperty(i + ".  " , rPanel);
			i++;
		}
		return rextReport;
	}
	
	private String generateFirstLineReport() {		
		String report = new String();

		if (finalList == null && finalList.isEmpty())
			return " No counterfactual sample with the desirable result has been found";
		else {
			String classAttName = finalSampleGenerator.getDataExtracrion().classAttributeName();
			String lowOrHeigh = finalSampleGenerator.getParameters().getLowerOrHeigher().toString();
			double threshold = finalSampleGenerator.getParameters().getGoodResultNmericalThreshold();
			double cValue = finalSampleGenerator.getCurrentInstance().get(classAttName);
					
			report = "For " + classAttName + " to be " + lowOrHeigh + " than " + threshold + " instead of " + cValue + " : \n\n";
		}
		
		writetoFile(report);
		try {
			FileUtils.writeStringToFile(new File("bin\\report.txt"), report);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return report;
	}
	
	
	
	private String generateReport() {
		LinkedList<Pair<Map<String, Double>, Double>> finalList = finalSampleGenerator.getFinalList();
		
		String report = new String();
		
		String[] suggestions = null;

		if (finalList == null || finalList.isEmpty())
			return " No counterfactual sample with the desirable result has been found";
		else {
			suggestions = new String[finalList.size() + 1];
			String classAttName = finalSampleGenerator.getDataExtracrion().classAttributeName();
			String lowOrHeigh = finalSampleGenerator.getParameters().getLowerOrHeigher().toString();
			double threshold = finalSampleGenerator.getParameters().getGoodResultNmericalThreshold();
			double cValue = finalSampleGenerator.getCurrentInstance().get(classAttName);
			
			System.out.println("For " + classAttName + " to be " + lowOrHeigh + " than " + threshold + " insted of " + cValue + " : \n\n");
			report = "For " + classAttName + " to be " + lowOrHeigh + " than " + threshold + " insted of " + cValue + " : \n\n";
			suggestions[0] = report;
			int i = 1;
			
			for (Pair<Map<String, Double>, Double> sample : finalList) {
				if (oneSuggention(sample) == null)
					break;
				report = report + i + ".  " + oneSuggention(sample) + "\n";
				System.out.println(i + ".  " + oneSuggention(sample) + "\n");
				suggestions[i] = oneSuggention(sample);
				i++;
			}
		}
		System.out.println(report);
		writeToFile(suggestions);
		writetoFile(report);
		try {
			FileUtils.writeStringToFile(new File("report.txt"), report);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return report;
	}

	private String oneSuggention(Pair<Map<String, Double>, Double> sample) {
		String classAttName = finalSampleGenerator.getDataExtracrion().classAttributeName();
		Map<String, Double> instance = sample.getKey();
		Map<String, Double> currentSample = finalSampleGenerator.getCurrentInstance();
//		Set<String> attNames = getAllAttNames(instance);
		
		String oneReport = new String();
		Set<String> attNames = new HashSet<>();
		if (finalSampleGenerator.getParams().getMethod().equals(ClassificationMethod.SEM)) {
			attNames = finalSampleGenerator.getSEM().getAttAncestors(classAttName);
		} else
			attNames = finalSampleGenerator.getParams().getActionableAttNames();
		
		for (String attName : attNames) {
			if (!attName.equals(classAttName) && currentSample.containsKey(attName) && instance.containsKey(attName) && !currentSample.get(attName).equals(instance.get(attName)))
				oneReport = oneReport + "Set " + attName + " to " + getRightValue(instance, attName).toString()
				+ " instead of " + getRightValue(currentSample, attName).toString() + " and \n";
		}
		
		if (oneReport.length() > 5)
			oneReport = oneReport.substring(0, oneReport.length() - 5);
		else 
			return null;
		
		oneReport = oneReport + " would result in " + sample.getValue();
		
		System.out.println(oneReport);
		
		return oneReport;
	}
	
	/**
	 * returns the double, integer, long, or timestamp based of the attribute type
	 * @param instance
	 * @param attName
	 * @return
	 */
	private Object getRightValue(Map<String, Double> instance, String attName) {
		Double value = instance.get(attName);
		Type type = finalSampleGenerator.getDataExtracrion().getAttributeTypes().get(attName);
		
		if (type.equals(Type.CONTINUOS))
			return value;
		
		if (type.equals(Type.DISCRETE))
			return value.longValue();
		
		if (type.equals(Type.TIMESTAMP))
			return new Timestamp(value.longValue());
		return null;
	}

	/**
	 * Returns a set including all the attribute names in both current world sample and the given counterfactual sample.
	 * Note that we may have some attributes that have value in just one of samples
	 * @param sample
	 * @return A set including all the attribute names in both current world sample and the given counterfactual sample.
	 */
	private Set<String> getAllAttNames(Map<String, Double> sample) {
		Map<String, Double> currentSample = finalSampleGenerator.getCurrentInstance();
		Set<String> attNames = new HashSet<String>();
		for (String attName : currentSample.keySet())
			attNames.add(attName);
		
		for (String attName : currentSample.keySet())
			if (!attNames.contains(attName))
				attNames.add(attName);
		
		return attNames;
	}

	public String getTitle() {
		return TITLE;
	}

	public GenerateFinalSamples apply(GenerateFinalSamples model, JComponent component) {
		return model;
	}

	public boolean canApply(GenerateFinalSamples model, JComponent component) {
		return component instanceof WizardStepReport;
	}

	public JComponent getComponent(GenerateFinalSamples model) {
		return this;
	}
	
	
	/**
	 * This method writes counterfactual explanations to a text file.
	 */
	public void writeToFile(String[] suggestions) { 
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("bin\\Explanations.txt", "ASCII") {
				@Override
			    public void println() {
			        write('\n');
			    }
			};
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		for (int i = 0; i < suggestions.length; i++)
			writer.println(suggestions[i]);	
			
		writer.close();
	}
	
	public void writetoFile(String str) { ///////////
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("C:\\Users\\qafari\\Dropbox\\GitHub\\CounterfactualExplanation\\bin\\ExpExp.txt", "ASCII") {
				@Override
			    public void println() {
			        write('\n');
			    }
			};
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		writer.println(str);

		writer.close();
		
	}
	
}
