package org.processmining.dataTable.Augmentation;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;


public class traceAttributeValue extends Augmentation implements ActivityLevelAugmentation {
	private Object value;
	private String attribute=null;

	public traceAttributeValue(String attribute)
	{
		super("Trace_"+attribute);
		this.attribute=attribute;
	}
	
	public void reset(XTrace trace) {
		XAttribute attributeObj = trace.getAttributes().get(attribute);
		if (attributeObj==null)
			value=null;
		else
			value=getAttributeValues(attributeObj);
	}
	
	public Object returnAttribute(XEvent event, XTrace trace) {
		return null;
	}

	public Object returnAttribute(XTrace trace, String traceAugName) {
		String attname = traceAugName.substring(6,traceAugName.length());
		Object newValue = getAttributeValues(trace.getAttributes().get(attname));
		value = newValue;
		return newValue;
	}

	public void setLog(XLog log) {
		
	}

}