package org.processmining.CounterfactualRecommendation.plugins;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class RemoveComma {
	
	public static void main(String[] args) {
		try (BufferedReader br = new BufferedReader(new FileReader("bin\\train.txt"))) {
			PrintWriter writer = new PrintWriter("bin\\train1.txt", "UTF-8");
			
		    String line;
		    int i = 0;
		    while ((line = br.readLine()) != null) {
		       if (i != 0) {
		    	   line = line.substring(0, line.length()-1);
		       }
		       writer.println(line);
		       i++;
		    }
		    writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
