package org.processmining.dataTable.Augmentation;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;


public class Whole_Trace_Attribute extends Augmentation implements ActivityLevelAugmentation {
	private Object value;
	private String attribute=null;

	public Whole_Trace_Attribute(String attribute)
	{
		super("Whole_Trace_Attribute"+attribute);
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
		Object newValue=getAttributeValues(event.getAttributes().get(attribute));
		if (value!=null)
			return value;
		value=newValue;
		return null;
	}

	public void setLog(XLog log) {
		
	}

	public Object returnAttribute(XTrace trace, String augName) {
		// TODO Auto-generated method stub
		return null;
	}

}
