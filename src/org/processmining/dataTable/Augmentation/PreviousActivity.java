package org.processmining.dataTable.Augmentation;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class PreviousActivity extends Augmentation implements ActivityLevelAugmentation {

	private XTrace trace=null;
	private int currPos=0;
	
	public PreviousActivity() {
		super("PreviousActivity");
	}

	public void reset(XTrace trace) {
		this.trace=trace;
		currPos=0;
	}

	public Object returnAttribute(XEvent event, XTrace trace ) {
		int index = 0;
		for (XEvent e : trace)
			if (!e.equals(event))
				index++;
			else 
				break;
		
		if (index == 0)
			return "NOT SET";
//		System.out.println("index : "+ index + " trace length : "+ trace.size());
		return XConceptExtension.instance().extractName(trace.get(index-1));
	}

	public void setLog(XLog log) {
		
	}

	public Object returnAttribute(XTrace trace, String augName) {
		// TODO Auto-generated method stub
		return null;
	}

}
