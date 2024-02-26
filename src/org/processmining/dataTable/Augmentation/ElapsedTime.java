package org.processmining.dataTable.Augmentation;

import java.util.Date;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class ElapsedTime extends Augmentation {

	private Date firstEventTimeStamp;

	public ElapsedTime() {
		super("Elapsed_Time");
	}

	public void reset(XTrace trace) {
		firstEventTimeStamp=XTimeExtension.instance().extractTimestamp(trace.get(0));
	}

	public Object returnAttribute(XEvent event, XTrace trace) {
		firstEventTimeStamp=XTimeExtension.instance().extractTimestamp(trace.get(0));
		String activityName=XConceptExtension.instance().extractName(event);
		Date timestamp=XTimeExtension.instance().extractTimestamp(event);
		if (firstEventTimeStamp!=null && timestamp!=null)
		{
			return (double) (timestamp.getTime()-firstEventTimeStamp.getTime());
		}
		return null;
	}

	public void setLog(XLog log) {
		
	}

	@Override
	public boolean isTimeInterval() {
		return true;
	}

	public Object returnAttribute(XTrace trace, String augName) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
