package org.processmining.CounterfactualRecommendation.plugins;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeContinuousImpl;
import org.deckfour.xes.model.impl.XAttributeDiscreteImpl;
import org.deckfour.xes.model.impl.XEventImpl;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.xeslite.external.XFactoryExternalStore.MapDBDiskImpl;

import javafx.util.Pair;
	

public class BPI2017justAddNumOffers {

	@Plugin(

			name = "BPM 2017 event log add num offer same event in the trace", parameterLabels = {"log BPI 2017", "log BPI 2017 offers"}, returnLabels = 
		{"Add number of offers to BPM 2017 event log and making some attrubutes Numerical"}, returnTypes = { XLog.class}, userAccessible = true, help = "help")
	
	@UITopiaVariant(affiliation = "University of PADS RWTH Aachen", author = "Mahnaz S. Qafari", email = "m.s.qafari@pads.rwth-aachen.de")
	// Test IT company
	public static XLog apply(UIPluginContext context, XLog loan, XLog offer) {
		
		return applay(context, loan, offer); 
	}
	public final static String notAllowedChars=",.()&!|=<>-+*/% \t";
	
	public static XLog applay(UIPluginContext context, XLog l1, XLog l2) {
		XLog log = null;
		XLog offer = null;
		if (l1.size() > 35000) {  //the size of offer log is more than 42000 while the size of 
			log = l2;				// loan application log is slightly more than 31000
			offer = l1;
		} else {
			log = l1;
			offer = l2;
		}
		// count the number of offers per application ID
		// application ID ==> a list of offers
		Map<String, LinkedList<Pair<XEvent, XTrace>>>offers = new HashMap<>();
		for (XTrace trace : offer) {
			// for each offer find the application ID
			XAttributeMap amapTrace = trace.getAttributes();
			XAttribute applicationID = amapTrace.get("ApplicationID");
			String[] parts = applicationID.toString().split("_");
			//add offer to the proper list
			if (offers.containsKey(parts[1])) {
				LinkedList<Pair<XEvent, XTrace>> list = offers.get(parts[1]);
				list.add(new Pair<XEvent, XTrace>(trace.get(0), trace));
				offers.remove(parts[1]);
				offers.put(parts[1], list);
			} else {
				LinkedList<Pair<XEvent, XTrace>> list = new LinkedList<>();
				list.add(new Pair<XEvent, XTrace>(trace.get(0), trace));
				offers.put(parts[1], list);
			}
		}
		
		// sort the offers in each list chronologically
		for (String aid : offers.keySet())
			offers.get(aid).sort( new Comparator<Pair<XEvent, XTrace>>(){
			    @Override
			    public int compare(Pair<XEvent, XTrace> e1,Pair<XEvent, XTrace> e2){
		    		Date d1 = XTimeExtension.instance().extractTimestamp(e1.getKey());
		    		Date d2 = XTimeExtension.instance().extractTimestamp(e2.getKey());
		    		if(d1.after(d2))
		    			return 1;
		    		else
		    			return -1;
		        }
		    });   
		
		// print the list to see if it is correct
		for (String aid : offers.keySet())
			if (offers.get(aid).size() > 1)
				StringCheckList(aid, offers.get(aid));
		
		// gather the goals of loans
		Set<String> goals = new HashSet<>();
		for (XTrace trace : log) {
			XAttributeMap amapTrace = trace.getAttributes();
			XAttribute goal = amapTrace.get("LoanGoal");
			goals.add(goal.toString());
		}
		
		for (String str : goals)
			System.out.println("goal : " + str);
		
		// one hot encoding of loanGoal in application log
    	for (XTrace trace : log) {
			XAttributeMap amapTrace = trace.getAttributes();
			XAttribute goalAtt = amapTrace.get("LoanGoal");
			String goal = goalAtt.toString();
			for (String g : goals) {
				if (g.equals(goal)) {
					XAttributeDiscreteImpl nameValueEvent = new XAttributeDiscreteImpl(replaceNotAllowedStrings(goal), 1);
					amapTrace.put(replaceNotAllowedStrings(goal), nameValueEvent);
				} else {
					XAttributeDiscreteImpl nameValueEvent = new XAttributeDiscreteImpl(replaceNotAllowedStrings(g), 0);
					amapTrace.put(replaceNotAllowedStrings(g), nameValueEvent);
				}
			}
    	}
		
		//generate a new log in which each offer is a trace with the information of the requested loan
		MapDBDiskImpl factory = new MapDBDiskImpl();
		XLog newLog = factory.createLog(log.getAttributes());
    	for (XTrace trace : log)
		{	
			String traceName = XConceptExtension.instance().extractName(trace);
			String[] parts = traceName.toString().split("_");
			if (offers.containsKey(parts[1])) {
	//			int num = 1;
				double requestedAmount;				
				// create a new trace
				XTrace newTrace = factory.createTrace(trace.getAttributes());
				newTrace.setAttributes(trace.getAttributes());
				
				// add number of offers
				String labelTrace = "numberOffers";
				XAttributeDiscreteImpl numOffers = new XAttributeDiscreteImpl(labelTrace, offers.get(parts[1]).size());
				XAttributeMap amapTrace = newTrace.getAttributes();
				amapTrace.put(labelTrace, numOffers);
				
				// requested amount
				XAttribute attTrace = amapTrace.get("RequestedAmount");
				requestedAmount = Double.valueOf(attTrace.toString());
				
				// create a new event
				int idx = 0; // the index of O_Create Offer event 
				for (XEvent e : trace) {
					String activityName = XConceptExtension.instance().extractName(e);
					if (activityName.equals("O_Create Offer")) {
						break;
					}	
					idx++;
				}
				
				XEvent event = new XEventImpl();
				event.setAttributes(trace.get(idx).getAttributes());
				XAttributeMap amapEvent = event.getAttributes();
				XAttribute attEvent = amapEvent.get("OfferedAmount");
		//		System.out.println(XConceptExtension.instance().extractName(event));
				double offeredAmount = Double.valueOf(attEvent.toString());
				Double value = offeredAmount - requestedAmount;
				String labelEvent = "difference"; // difference between requested and offered amount
				XAttributeContinuousImpl nameValueEvent = new XAttributeContinuousImpl(labelEvent, value);
				amapEvent.put(labelEvent, nameValueEvent);
				
				//add selected to the trace attributes
				XAttribute selected = amapEvent.get("Selected");
				if (selected.toString().equals("true")) {
					XAttributeDiscreteImpl newSelected = new XAttributeDiscreteImpl("Selected", 1);
					amapTrace.put("Selected", newSelected);
				} else {
					XAttributeDiscreteImpl newSelected = new XAttributeDiscreteImpl("Selected", 0);
					amapTrace.put("Selected", newSelected);
				}
				
				//add selected to the trace attributes
				XAttribute at = amapTrace.get("ApplicationType");
				if (at.toString().equals("New credit")) {
					XAttributeDiscreteImpl ncAtt = new XAttributeDiscreteImpl("ApplicationType", 1);
					amapTrace.put("ApplicationType", ncAtt);
				} else {
					XAttributeDiscreteImpl ncAtt = new XAttributeDiscreteImpl("ApplicationType", 0);
					amapTrace.put("ApplicationType", ncAtt);
				}
				
				// add event to the trace
				newTrace.add(event);
				// add trace to the event log
				newLog.add(newTrace);
			} 
		} 
    	
    	//select the rows with "Selected" == 0
    	Set<Map<String, Object>> data = returnRejectedOffers(newLog);
    	
    	//Write them to a file
    	write(data);
		
		XTrace trace = newLog.get(0);
		XAttributeMap amapTrace = trace.getAttributes();
		for (String attName : amapTrace.keySet())
			System.out.println(attName);
		XAttributeMap amapEvent = trace.get(0).getAttributes();
		for (String attName : amapEvent.keySet())
			System.out.println(attName);
		
		return newLog;
	}
	
	/**
	 * write data to a file.
	 * @param data
	 */
	private static void write(Set<Map<String, Object>> data) {
		String head = new String();
		
		//find the header
		LinkedList<String> header = new LinkedList<>();
		for (Map<String, Object> instance : data) {
			for (String attName : instance.keySet())
				header.add(attName);
			if (true)
				break;
		}
		
		//write the header
		String h = new String();
		for (String attName : header)
			h = h + attName + ',';
		h = h.substring(0,h.length()-1);
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("bin\\dataBPI2017.txt", "UTF-8");
			writer.println(h);
			// write the data lines
			for (Map<String, Object> instance : data) {
				String row = new String();
				for (String attName : header)
					row = row + instance.get(attName) + ',';
				row = row.substring(0,row.length()-1);
				writer.println(row);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		writer.close();
		
	}

	/**
	 * returns the important attribute values of those traces that the value of 
	 * trace attribute Selected is 0 (false) for them.
	 * @param newLog
	 * @return a set of instances for which the offer has been rejected.
	 */
	private static Set<Map<String, Object>> returnRejectedOffers(XLog log) {
		Set<Map<String, Object>> data = new HashSet<>();
		LinkedList<String> traceAttNames = new LinkedList<>();
		traceAttNames.add("Caravan___Camper");
		traceAttNames.add("Motorcycle");
		traceAttNames.add("Home_improvement");
		traceAttNames.add("Existing_loan_takeover");
		traceAttNames.add("Other__see_explanation");
		traceAttNames.add("Car");
		traceAttNames.add("Remaining_debt_home");
		traceAttNames.add("Extra_spending_limit");
		traceAttNames.add("Tax_payments");
		traceAttNames.add("Business_goal");
		traceAttNames.add("Not_speficied");
		traceAttNames.add("Debt_restructuring");
		traceAttNames.add("Boat");
		traceAttNames.add("ApplicationType");
		traceAttNames.add("RequestedAmount");
		traceAttNames.add("numberOffers");
		LinkedList<String> eventAttNames = new LinkedList<>();
		eventAttNames.add("FirstWithdrawalAmount");
		eventAttNames.add("CreditScore");
		eventAttNames.add("MonthlyCost");
		eventAttNames.add("NumberOfTerms");
		int num = 0;
		for (XTrace trace : log) {
			XAttributeMap amapTrace = trace.getAttributes();
			XAttribute selected = amapTrace.get("Selected");
			if (Integer.valueOf(selected.toString()) == 0) {
				num++;
				Map<String, Object> instance = new HashMap<>();
				for (String attName : traceAttNames) {
//					System.out.println(attName);
					XAttribute att = amapTrace.get(attName);
					instance.put(attName, att.toString());
				}
				XAttributeMap amapEvent = trace.get(0).getAttributes();
				for (String attName : eventAttNames) {
					XAttribute att = amapEvent.get(attName);
					instance.put(attName, att.toString());
				}
				data.add(instance);
			}
		}
		
		System.out.println("num : " + num + " size : " + data.size());
		return data;
	}

	private static void StringCheckList(String aid, LinkedList<Pair<XEvent, XTrace>> list) {
		System.out.println(aid);
		String str = new String();
		for (Pair<XEvent, XTrace> item : list) {
			XTrace trace = item.getValue();
			XAttributeMap amapTrace = trace.getAttributes();
			XAttributeMap amapEvent = trace.get(0).getAttributes();
			str = str + amapTrace.get("Selected").toString() + " / " + amapTrace.get("ApplicationID").toString() + " / " + amapEvent.get("time:timestamp").toString() + " \n";
		}
		System.out.println(str);
	}

	public static String replaceNotAllowedStrings(String str) {
   		char[] array=str.toCharArray();
		for(int i=0;i<array.length;i++)
		{
			if (notAllowedChars.indexOf(array[i])!=-1)
				array[i]='_';
		}
		return (new String(array));
   	}
}