package org.processmining.CounterfactualRecommendation.ui;

import java.awt.BorderLayout;

/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc. 
 * in the United States and other countries.]
 *
 * ------------------
 * BarChartDemo2.java
 * ------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: BarChartDemo2.java,v 1.10 2004/04/26 19:11:53 taqua Exp $
 *
 * Changes
 * -------
 * 14-Nov-2002 : Version 1 (DG);
 * 11-Nov-2003 : Renamed BarChartDemo2 (DG);
 *
 */

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.ui.ApplicationFrame;
import org.processmining.CounterfactualRecommendation.algorithms.GenerateFinalSamples;

import javafx.util.Pair;

/**
 * A simple demonstration application showing how to create a horizontal bar chart.
 *
 */
public class BarChartPanel extends ApplicationFrame {
	
    // The header of the table
    private String[] names = new String[4];
    
    // The body
    private double[][] data;
    
    // For easier access to the att names
    Map<String, Integer> attIndex;
    
    GenerateFinalSamples gfs;
    
    CategoryDataset dataset;
    
    JFreeChart chart; 
    
    String title;
    
	/**
	 * min value on the X-axes of the chart
	 */
	double min = 0;
	
	/**
	 * max value on the X-axes of the chart
	 */
	double max = 100;

    /**
     * Creates a new demo instance.
     *
     * @param title  the frame title.
     */
    public BarChartPanel(final String title, GenerateFinalSamples gfs) {
    	super(title);
    	this.gfs = gfs;
    	this.title = title;
    }
    
    /**
     * Creates a dataset from the .
     * 
     * @return A dataset.
     */
    public CategoryDataset createDataset() { 
    	
    	LinkedList<Pair<Map<String, Double>, Double>> finalList = gfs.getFinalList();
		LinkedList<Pair<Map<String, Double>, Double>> listNumatt = gfs.listDustance("numAtt");
		LinkedList<Pair<Map<String, Double>, Double>> listOutput = gfs.listDustance("MAD");
		LinkedList<Pair<Map<String, Double>, Double>> listAMD = gfs.listDustance("output");
		LinkedList<Pair<Map<String, Double>, Double>> listL1 = gfs.listDustance("L1");
		
		// Creating headers
		String[] metrics = { "Number of different attribute values", "MAD_L1 distance",
    			"Difference in dependet attribute value", "L1 Distance"};
		
		String[] samples = null;
		if (!finalList.isEmpty()) {
    		 samples = new String[finalList.size()];
    		 for (int i = 0; i < finalList.size();i++)
    			 samples[i] = "sample " + i;
		}
		
		attIndex = new HashMap<String, Integer>();
		attIndex.put("numAtt", 0);
		attIndex.put("MAD", 1);
		attIndex.put("outPut", 2);
		attIndex.put("L1", 3);
		
		if (!finalList.isEmpty()) {
			data = new double[4][finalList.size()];
			for (int i = 0; i < finalList.size(); i++) {
				data[0][i] = listNumatt.get(i).getValue();
				data[1][i] = listOutput.get(i).getValue();
				data[2][i] = listAMD.get(i).getValue();
				data[3][i] = listL1.get(i).getValue();
			}
				
		}
		
		setMinMax();
		
        return DatasetUtilities.createCategoryDataset("Metric", "Sample", data);
    }
   
    
    /**
     * Set the min and max value for the X-axes
     */
	public void setMinMax() {
		min = data[0][0];
		max = data[0][0];
		for (int i = 0; i < data.length; i++)
			for (int j = 0; j < data[0].length; j++)
				if (data[i][j] < min)
					min = data[i][j];
				else if (data[i][j] > max)
					max = data[i][j];	
		
		if (min == max)
			min = 0;
		
		max = max + 1;
		min = min - 1;
	}
	
    /**
     * Creates a dataset with just one distance.
     * 
     * @return A dataset.
     */
    public CategoryDataset createDataset(String distance) { 
    	
    	LinkedList<Pair<Map<String, Double>, Double>> finalList = gfs.getFinalList();
    	LinkedList<Pair<Map<String, Double>, Double>> listSampleDistance = null;
    	String[] metrics = new String[1];
    	if (distance.equals("numAtt")) {
    		listSampleDistance = gfs.listDustance("numAtt");
    		metrics[0] = "Number of different attribute values";
    	}
    	else if (distance.equals("MAD")) {
    		listSampleDistance = gfs.listDustance("MAD");
    		metrics[0] = "MAD_L1 distance";
    	}
    	else if (distance.equals("output")) {
    		listSampleDistance =  gfs.listDustance("output");
    		metrics[0] = "Difference in dependet attribute value";
    	}
    	else if (distance.equals("L1")) {
    		listSampleDistance =  gfs.listDustance("L1");
    		metrics[0] = "L1 Distance";
    	}
		
		// Creating headers
		String[] samples = null;
		if (!finalList.isEmpty()) 
    		 samples = new String[finalList.size()];
		
		attIndex = new HashMap<String, Integer>();
		attIndex.put("numAtt", 0);
		attIndex.put("MAD", 1);
		attIndex.put("outPut", 2);
		attIndex.put("L1", 3);
		
		if (!finalList.isEmpty()) {
			data = new double[1][finalList.size()];
			for (int i = 0; i < finalList.size(); i++) {
				data[0][i] = listSampleDistance.get(i).getValue();
			}
				
		}
		
		setMinMax();

        return DatasetUtilities.createCategoryDataset("Metric", "Sample", data);
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
    
    /**
     * Creates a chart.
     * 
     * @param dataset  the dataset.
     * 
     * @return A chart.
     */
    public JFreeChart createChart(CategoryDataset dataset) {
        
        final JFreeChart chart = ChartFactory.createBarChart(
            title,         // chart title
            "Sample",                 // domain axis label
            "Distance",                // range axis label
            dataset,                    // data
            PlotOrientation.HORIZONTAL, // orientation
            true,                       // include legend
            true,
            false
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...

        // set the background color for the chart...
        chart.setBackgroundPaint(Color.lightGray);

        // get a reference to the plot for further customisation...
        final CategoryPlot plot = chart.getCategoryPlot();
        plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        
        // change the auto tick unit selection to integer units only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setRange(min, max);
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
        // OPTIONAL CUSTOMISATION COMPLETED.
        
        // add the chart to a panel...
        
        return chart;
        
    }
  //************************* TEST ***********************************
    
    public BarChartPanel(final String title) {
    	super(title);
    	this.title = title;
    }
    
    /**
     * Creates a dataset from the .
     * 
     * @return A dataset.
     */
    public CategoryDataset createDatasetTest() { 
    		
		// Creating headers
		String[] metrics = { "Number of different attribute values", "MAD_L1 distance",
    			"Difference in dependet attribute value", "L1 Distance"};
//	
//	data = new double[4][10];
//	for (int i = 0; i < 10; i++) {
//		data[0][i] = i;
//		data[1][i] = i;
//		data[2][i] = i;
//		data[3][i] = i;
//	}
//	
		data = new double[][] {
            {1.0, 43.0, 35.0, 58.0, 54.0, 77.0, 71.0, 89.0},
            {54.0, 75.0, 63.0, 83.0, 43.0, 46.0, 27.0, 13.0},
            {41.0, 33.0, 22.0, 34.0, 62.0, 32.0, 42.0, 34.0}
        };

        return DatasetUtilities.createCategoryDataset("Metric", "Sample", data);
    }
    
  /**
   * Starting point for the demonstration application.
   *
   * @param args  ignored.
   */
  public static void main(final String[] args) {

//      final BarChartPanel demo = new BarChartPanel("Bar Chart Demo 2");
//  demo.createDatasetTest();
//  
//  final ChartPanel chartPanel = new ChartPanel(demo.createChart());
//  chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
//  setContentPane(chartPanel);
//  
//  demo.pack();
//  RefineryUtilities.centerFrameOnScreen(demo);
//  demo.setVisible(true);
//  
//  JFrame frame = new JFrame("Swing JTable");
//  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//  
//  JPanel jPanel1 = new JPanel();
//  jPanel1.setLayout(new java.awt.BorderLayout());
	  JFrame frame = new JFrame("Swing JTable");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      
      JPanel jPanel1 = new JPanel();
      jPanel1.setLayout(new java.awt.BorderLayout());
      
      BarChartPanel demo = new BarChartPanel("Bar Chart Demo");
      JFreeChart c = demo.createChart(demo.createDatasetTest());
      ChartPanel CP = new ChartPanel(c); 
      jPanel1.add(CP,BorderLayout.CENTER);
      jPanel1.validate();
      frame.add(jPanel1);
      frame.setLocationRelativeTo(null);
      frame.setVisible(true);
      demo.setVisible(true);

  }

}