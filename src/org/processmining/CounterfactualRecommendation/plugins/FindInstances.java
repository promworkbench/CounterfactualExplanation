package org.processmining.CounterfactualRecommendation.plugins;

import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
	

public class FindInstances {

	@Plugin(

			name = "find BPM 2017 instances with specific attributes", parameterLabels = {"log BPI 2017"}, returnLabels = 
		{"BPI 2017"}, returnTypes = { XLog.class}, userAccessible = true, help = "help")
	
	@UITopiaVariant(affiliation = "University of PADS RWTH Aachen", author = "Mahnaz S. Qafari", email = "m.s.qafari@pads.rwth-aachen.de")
	// Test IT company
	public static XLog apply(UIPluginContext context, XLog log) {
		
		return applay(context, log); 
	}
	public final static String notAllowedChars=".()&!|=<>-+*/% \t";
	
	public static XLog applay(UIPluginContext context, XLog log) {
		
		for (XTrace trace : log) {
			//check the trace attribute(s)
			if (validateTraceAtts(trace))
				if (validateEventAtts(trace)) {
					XAttributeMap amapTrace = trace.getAttributes();
					for (String attName : amapTrace.keySet())
						System.out.println(attName + "  :  " + amapTrace.get(attName).toString());
					XAttributeMap amapEvent = trace.get(0).getAttributes();
					for (String attName : amapEvent.keySet())
						System.out.println(attName + "  :  " + amapEvent.get(attName).toString());
				}
					
		}
		
		return log;
	}

	private static boolean validateEventAtts(XTrace trace) {
		XAttributeMap amapEvent = trace.get(0).getAttributes();
		if (Double.valueOf(amapEvent.get("MonthlyCost").toString()) >= 120.0)
			return false;
		if (Integer.valueOf(amapEvent.get("CreditScore").toString()) <= 885)
			return false;
		if (Double.valueOf(amapEvent.get("FirstWithdrawalAmount").toString()) < 5700.0)
			return false;
		if (Double.valueOf(amapEvent.get("FirstWithdrawalAmount").toString()) > 9895.0)
			return false;
		return true;
	}

	private static boolean validateTraceAtts(XTrace trace) {
		XAttributeMap amapTrace = trace.getAttributes();
		if (!Double.valueOf(amapTrace.get("ApplicationType").toString()).equals(0.0))
			return false;
		return true;
	}
}