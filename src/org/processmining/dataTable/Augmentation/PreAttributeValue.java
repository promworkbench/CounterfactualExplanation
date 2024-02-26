package org.processmining.dataTable.Augmentation;
import java.util.Date;

import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;


public class PreAttributeValue extends Augmentation implements ActivityLevelAugmentation {
	private Object value;
	private String attribute=null;

	public PreAttributeValue(String attribute)
	{
		super("PreValue_"+attribute);
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
		Object newValue = new Object();
		newValue=getAttributeValues(event.getAttributes().get(attribute));
		
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
	
	public long wholeTraceDuration(XTrace trace) {
  		XEvent firstEvent = trace.get(0);
		XEvent lastEvent = trace.get(trace.size()-1);
		Date timestampE1=XTimeExtension.instance().extractTimestamp(firstEvent);
		Date timestampE2=XTimeExtension.instance().extractTimestamp(lastEvent);
		return timestampE2.getTime()-timestampE1.getTime();
  	}
  	

}
