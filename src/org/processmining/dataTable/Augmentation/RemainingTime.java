package org.processmining.dataTable.Augmentation;

import java.util.Date;

import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class RemainingTime extends Augmentation implements ActivityLevelAugmentation {

	private Date lastEventTimeStamp;

	public RemainingTime() {
		super("Remaining_Time");
	}

	public void reset(XTrace trace) {
		
	}

	public Object returnAttribute(XEvent event, XTrace trace) {
		lastEventTimeStamp=XTimeExtension.instance().extractTimestamp(trace.get(trace.size()-1));
		Date timestamp=XTimeExtension.instance().extractTimestamp(event);
		if (lastEventTimeStamp!=null && timestamp!=null)
		{
			long remainingTime=lastEventTimeStamp.getTime()-timestamp.getTime();
			return (double) remainingTime;
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
