package org.processmining.CounterfactualRecommendation.algorithms;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.processmining.datadiscovery.estimators.Type;

	/**
	 * data type : 	numerical (long, integer, real)
	 * 				categorical
	 * 				mixed (columns with some numerical and some categorical values)
	 * 
	 * If R is the set of all rows and for r \in R r(k) be the value of attribute k then
	 * for attribute k with numerical values, we set 
	 * 			MAD = median_{r \in R} (|r(k) - median_{r \in R}(r(k))|}
	 * and using that we compute the distance between r and r' as:
	 * 			d(r,r') = sum_k [|r(k) - r'(k)| / MAD_k]
	 * 
	 * For computing the value of MAD_k all the values of the attribute k in the event log
	 * is considered (not just the last one in each trace).
	 * 
	 * Now, given a sample r, if we need n samples, we create a set R' of n^2 samples s.t.
	 *		sum d(r,r') - sum d(r',r") 
	 * where r \in R and r',r" \in R' is minimized.
	 * 
	 * 
	 * @author qafari
	 *
	 */

	public class MADdistance {
		
		private DataExtraction de;
		
		/**
		 * A map in which the key is the attName and the value is the coefficient of the value 
		 * of that attName when we use MAD_L1 distance.
		 */
		private Map<String, Double> MADs;
		
		public MADdistance(DataExtraction de) {
			this.de = de;
			computeMAD();
		}

		private void computeMAD() {
			MADs = new HashMap<String, Double>();
			for (String attName : de.getTypesNDC().keySet()) 
				if (de.getAttributeTypes().keySet().contains(attName)) {
					Type type = de.getAttributeTypes().get(attName);
					if (type.equals(Type.CONTINUOS) || type.equals(Type.DISCRETE))
						MADs.put(attName, 1 / mad(attName));
				}
		}
		
		/**
		 * computing MAD for the attribute name using the following formula
		 * 			MAD = median_{r \in R} (|r(k) - median_{r \in R}(r(k))|}
		 * @param attName
		 * @return MAD(attName)
		 */
		public Double mad(String attName) {
			if (!de.getTypesNDC().containsKey(attName))
				return null;
			
			LinkedList<Object> column = null; // de.getColumns().get(attName);
			// TODO
			LinkedList<Double> doubleList = new LinkedList<Double>();
			for (Object o : column) 
				doubleList.add((double) o);
			
			Double columnMedian = median(doubleList);
			
			LinkedList<Double> newList = new LinkedList<Double>();
			for (double d : doubleList)
				newList.add(Math.abs(d - columnMedian));
			
			return median(newList);
		}
		
		/**
		 * 
		 * @param list of doubles
		 * @return median of the list
		 */
		public double median(LinkedList<Double> list) {
			int x = list.size();
			if (x == 0)
				throw new IllegalArgumentException("No median for empty list");
			
			Collections.sort(list);
			if (x % 2 == 0) 
				return list.get(x/2);
			else 
				return (list.get(x/2) + list.get(x/2 + 1)) / 2;
		}
		
		public Map<String, Double> getMADcoefficients() {
			return MADs;
		}
	} 