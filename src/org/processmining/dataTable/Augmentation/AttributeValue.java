package org.processmining.dataTable.Augmentation;

import java.util.Map;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class AttributeValue extends Augmentation{

	private Object value;
	private String originalName;

	public AttributeValue(String attributeName) {
		super(attributeName);
		originalName=attributeName;
	}

	public void reset(XTrace trace) {
		value=null;
	}

	public Object returnAttribute( XEvent event) {
		XAttribute attr=event.getAttributes().get(originalName);
		if (attr!=null)
			value=getAttributeValues(attr);
		return value;
	}

	public void setLog(XLog log) {
		// TODO Auto-generated method stub
		
	}

	public Object returnAttribute(XEvent event, XTrace trace) {
		XAttribute attr=event.getAttributes().get(originalName);
		if (attr!=null) {
			value=getAttributeValues(attr);
			return value;
		}
		
		Map<String, XAttribute> map = event.getAttributes();
		for (String attName : map.keySet()) {
			if (originalName.equals(replaceNotAllowedStrings(attName))) {
				attr=event.getAttributes().get(attName);
				if (attr!=null) {
					value=getAttributeValues(attr);
					return value;
				}
			}
		}
		
		return value;
	}
	
	public String replaceNotAllowedStrings(String str) {
   		char[] array=str.toCharArray();
		for(int i=0;i<array.length;i++)
		{
			if (notAllowedChars.indexOf(array[i])!=-1)
				array[i]='_';
		}
		return (new String(array));
   	}
		
}
