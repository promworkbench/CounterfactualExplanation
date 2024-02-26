package org.processmining.CounterfactualRecommendation.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import org.processmining.CounterfactualRecommendation.algorithms.GenerateFinalSamples;
import org.processmining.datadiscovery.estimators.Type;

import javafx.util.Pair;

public class TablePanel extends JPanel {

    static JTable table;
    Object[] columnNames = new String[] {
        "Id", "Name", "Hourly Rate", "Part Time"
    };
    Object[][] data  = new Object[][] {
        {1, "John", 40.0, false },
        {2, "Rambo", 70.0, false },
        {3, "Zorro", 60.0, true },
    };
    
    /**
     * 1 --> red   v(new sample) > v(current sample)
     * 2 --> green   v(new sample) < v(current sample)
     * 3 --> cyan   v(new sample) == v(current sample)
     * 4 --> yellow v(current sample) = null
     * 5 --> gray v(new sample) == v(current sample) = null
     * 6 --> pink v(current sample)
     */
    Map<Integer, Color> colorMap;
    int[][] color = new int[][] {
        {1, 1, 5, 4 },
        {2, 2, 3, 3 },
        {3, 4, 5, 1 },
    };

    public TablePanel(){
    	initiateColorCodeMape();
    }
    
    public TablePanel(final String title, GenerateFinalSamples gfs) {
    	initiateColorCodeMape();
    	setHeader(gfs);
    	setBodyAndColor(gfs);
    }
    
    private void initiateColorCodeMape() {
    	  colorMap = new HashMap<Integer, Color>();
          colorMap.put(1, Color.red);
          colorMap.put(2, Color.green);
          colorMap.put(3, Color.cyan);
          colorMap.put(4, Color.yellow);
          colorMap.put(5, Color.lightGray);
          colorMap.put(6, Color.pink);
	}

	private void setHeader(GenerateFinalSamples gfs) {
    	Map<String, Type> types = gfs.getDataExtracrion().getTypesNDC();
		columnNames = new Object[types.size() + 1];
		columnNames[0] = " ";
		int i = 1;
		for (String attName : types.keySet()) {
			columnNames[i] = attName;
			i++;
		}
	}
    
    private void setBodyAndColor(GenerateFinalSamples gfs) {
    	LinkedList<Pair<Map<String, Double>, Double>> finalList = gfs.getFinalList();
		
    	if (!finalList.isEmpty()) {
    		data = new Object[finalList.size() + 1][columnNames.length];
    		color = new int[finalList.size() + 1][columnNames.length];
    		
    		int row = 0;
    		Map<String, Double> currentSample = gfs.getCurrentInstance();
    		data[row][0] = "Current world";
			color[row][0] = 0;
			for(int col = 1; col < columnNames.length; col++) {
				if (currentSample.containsKey(columnNames[col])) {
					data[row][col] = currentSample.get(columnNames[col]);
					color[row][col] = 6;
				}
    		}
			
    		row = 1;
    		for (Pair<Map<String, Double>, Double> sampleValue : finalList) {
    			Map<String, Double> sample = sampleValue.getKey();
    			data[row][0] = "Sample " + row;
    			color[row][0] = 0;
    			for(int col = 1; col < columnNames.length; col++) {
    				data[row][col] = sample.get(columnNames[col]);
    				color[row][col] = setColor(sample.get(columnNames[col]), currentSample.get(columnNames[col]));
    			}
    	//		data[row][ columnNames.length - 1] = sampleValue.getValue();
    			row++;
    		}
    	}
		
		
	}

	private int setColor(Double cfV, Double csV) {
		if (cfV == null && csV == null)
			return 5;
		if (csV == null)
			return 4;
		if (csV == cfV)
			return 3;
		if (cfV == null)
			return 0;
		if (csV > cfV)
			return 2;
		if (csV < cfV)
			return 1;
		return 0;
	}

	public JScrollPane createTable() {
    table = new JTable(data, columnNames){
        @Override
        public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
            Component comp = super.prepareRenderer(renderer, row, col);
            
            Object value = getModel().getValueAt(row, col);
            if (color[row][col] > 0) {
            	System.out.println("row : " + row + " col : " + col + colorMap.get(color[row][col]));
                comp.setBackground(colorMap.get(color[row][col]));
            } else {
               comp.setBackground(Color.white);
            }
            return comp;
        }
    };

    JTableHeader header = table.getTableHeader();
    header.setFont(new Font("Times New Roman", Font.BOLD, 13));
    header.setBackground(Color.black);
    header.setForeground(Color.white);

    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.setSize(988, 618);
    scrollPane.setFont(new Font("Times New Roman", Font.BOLD, 13));
    scrollPane.setLocation(10, 60);
    return scrollPane;
    }
    
    // ****************** TEST *************************
    public static void main(String[] args) {
   
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                	JFrame frame = new JFrame();
                	frame.getContentPane().setBackground(Color.WHITE);
                    frame.setTitle("Portable test file viewing");
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setBounds(50, 50, 1024, 768);
                    frame.getContentPane().setLayout(null);
                    TablePanel t = new TablePanel();
                    frame.getContentPane().add(t.createTable());
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }); 
    }
}