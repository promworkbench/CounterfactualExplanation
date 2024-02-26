package org.processmining.CounterfactualRecommendation.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;

public class FindTraces {
	@Plugin(

			name = "Find traces in changed BPM 2017 event log with specific property", parameterLabels = {"log BPI 2017 changed"}, returnLabels = 
		{"non"}, returnTypes = { XLog.class}, userAccessible = true, help = "help")
	
	@UITopiaVariant(affiliation = "University of PADS RWTH Aachen", author = "Mahnaz S. Qafari", email = "m.s.qafari@pads.rwth-aachen.de")
	// Test IT company
	public static XLog apply(UIPluginContext context, XLog log) {
		
	//	return applay(context, log); 
		return log;
	}
}
