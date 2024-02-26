package org.processmining.dataTable.Augmentation;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class NextActivity extends Augmentation implements ActivityLevelAugmentation {

	private XTrace trace=null;
	private int currPos=0;
	private String defaultValue="";
	private String activityNamesToConsider[];
	
	public NextActivity() {
		super("NextActivity");
	}

	public void reset(XTrace trace) {
		this.trace=trace;
		currPos=0;
	}

	public Object returnAttribute(XEvent event, XTrace trace) { // why event is fed as an input????		???
		int index = 0;
		for (XEvent e : trace)
			if (!e.equals(event))
				index++;
			else 
				break;
		
		if (index == trace.size() || index == trace.size()-1)
			return "NOT SET";
		
		return XConceptExtension.instance().extractName(trace.get(index+1));
	}
	
	private boolean isInIgnoringCase(String value, String[] array) {
		for(String aValue : array)
		{
			if (value.equalsIgnoreCase(aValue))
				return true;
		}
		return false;
	}

	@Override
	public String[] getParameterNames() {
		return new String[] {"the activities that you want to consider as potential subsequent activity in a trace"};
	}
	
	@Override
	public boolean multipleValuesForParameter(int i)
	{
		return true;
	}
	
	@Override
	public boolean setParameter(int i,String value[]) {
		if (value.length>0)
		{
			activityNamesToConsider=value.clone();
			return true;
		}
		else
			return false;
	}

	public void setLog(XLog log) {
		
	}

	public Object returnAttribute(XTrace trace, String augName) {
		// TODO Auto-generated method stub
		return null;
	}
}
