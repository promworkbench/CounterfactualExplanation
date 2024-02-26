package org.processmining.CounterfactualRecommendation.plugins;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.xeslite.external.XFactoryExternalStore.MapDBDiskImpl;

public class cleanBPI2017 {
	@Plugin(

			name = "Clean BPM 2017 remove firstWithdra more than 25% of the request", parameterLabels = {"log BPI 2017"}, returnLabels = 
		{"event log"}, returnTypes = { XLog.class}, userAccessible = true, help = "help")
	
	@UITopiaVariant(affiliation = "University of PADS RWTH Aachen", author = "Mahnaz S. Qafari", email = "m.s.qafari@pads.rwth-aachen.de")
	// Test IT company
	public static XLog apply(UIPluginContext context, XLog log) {
		
		return applay(context, log); 
	}

	private static XLog applay(UIPluginContext context, XLog log) {
		MapDBDiskImpl factory = new MapDBDiskImpl();
		XLog newLog = factory.createLog(log.getAttributes());
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("bin\\dataBPI2017cleaned.txt", "UTF-8");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LinkedList<String> header = new LinkedList<>();
		header.add("FirstWithdrawalAmount");
		header.add("numberOffers");
		header.add("RequestedAmount");
		header.add("ApplicationType");
		header.add("CreditScore");
		header.add("NumberOfTerms");
		header.add("CreditScore");
		header.add("Selected");
		String h = new String();
		for (String str : header)
			h = h + str + ',';
		h = h.substring(0, h.length()-1);
		writer.println(h);
    	for (XTrace trace : log) {
    		Map<String, String> data = new HashMap<>();
    		XAttributeMap amapTrace = trace.getAttributes();
			
			// requested amount
			XAttribute attTrace = amapTrace.get("RequestedAmount");
			double requestedAmount = Double.valueOf(attTrace.toString());
			data.put("RequestedAmount", amapTrace.get("RequestedAmount").toString());
			data.put("numberOffers", amapTrace.get("numberOffers").toString());
			data.put("ApplicationType", amapTrace.get("ApplicationType").toString());
			data.put("Selected", amapTrace.get("Selected").toString());
			
			XAttributeMap amapEvent = trace.get(0).getAttributes();
			XAttribute attEvent = amapEvent.get("FirstWithdrawalAmount");
			double firstW = Double.valueOf(attEvent.toString());
			data.put("FirstWithdrawalAmount", amapEvent.get("FirstWithdrawalAmount").toString());
			data.put("NumberOfTerms", amapEvent.get("NumberOfTerms").toString());
			data.put("CreditScore", amapEvent.get("CreditScore").toString());
			
			if (firstW <= 0.25 * requestedAmount) {
				newLog.add(trace);
				String d = new String();
				for (String str : header)
					d = d + data.get(str) + ',';
				d = d.substring(0, d.length()-1);
				writer.println(d);
			}
    	}
    	writer.close();
		return newLog;
	}
}
