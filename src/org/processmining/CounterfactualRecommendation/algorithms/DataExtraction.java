package org.processmining.CounterfactualRecommendation.algorithms;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeContinuousImpl;
import org.processmining.dataTable.Augmentation.ActivityDuration;
import org.processmining.dataTable.Augmentation.AttributeValue;
import org.processmining.dataTable.Augmentation.Augmentation;
import org.processmining.dataTable.Augmentation.DurationOfActivity;
import org.processmining.dataTable.Augmentation.ElapsedTime;
import org.processmining.dataTable.Augmentation.Executor;
import org.processmining.dataTable.Augmentation.Group;
import org.processmining.dataTable.Augmentation.NextActivity;
import org.processmining.dataTable.Augmentation.PreviousActivity;
import org.processmining.dataTable.Augmentation.RemainingTime;
import org.processmining.dataTable.Augmentation.Resource;
import org.processmining.dataTable.Augmentation.ResourceWorkload;
import org.processmining.dataTable.Augmentation.Role;
import org.processmining.dataTable.Augmentation.Timestamp;
import org.processmining.dataTable.Augmentation.TotalResourceWorkload;
import org.processmining.dataTable.Augmentation.sub_model_duration;
import org.processmining.datadiscovery.estimators.Type;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.DataConformance.ResultReplay;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

import csplugins.id.mapping.ui.CheckComboBox;

public class  DataExtraction
{
	public final static String notAllowedChars=".()&!|=<>-+*/% \t";
	private Map<String, Type> types;
	private Map<String, Type> typesNDC;
	private Petrinet model;
	private Set<Object[]> instanceSet;
	private Augmentation[] augementationArray;
	private XLog originalLog;
	private Map<String, Set<String>> literalValues;
	private Map<String, Set<String>> literalValuesNDC;
	private Map<XTrace, Object[]> instanceOfATrace=new HashMap<XTrace, Object[]>();
	private Map<XTrace, LinkedList<Map<String, Object>>> traceInstanceMap = new HashMap<XTrace, LinkedList<Map<String, Object>>>();
	private LinkedList<Map<String, Object>> instancesOfNDC = new LinkedList<Map<String, Object>>();
	private Collection<String> activitiesToConsider = new ArrayList<String>();
	private Augmentation outputAttribute = null;
	private final ArrayList<String> activityCollection=new ArrayList<String>();
	private ResultReplay resReplay;
	private PNRepResult replayREsultForChoiceAndTime;
	private HashSet<String> timeIntervalAugmentations;
	private boolean regressionTree=false;
	private ArrayList<String> originalLogAttributes=new ArrayList<String>();
	private XLog log;
	private Map<String, Set<String>> traceAttributesValues = new HashMap<String, Set<String>>();
	private Set<String> traceAttributeNames;
	private long traceDelayThreshold;
	private long minTraceDuration = 0;
	private long maxTraceDuration = 0;
	private Set<Place> selectedORplaces = new HashSet<Place>();
	private Set<Transition> selectetSub_model;
	private ArrayList<Map<String, Object>> orderedInstancesNDC;
	public Object[] outputValuesAsObjects;
	public String targetActivityName;  //if the dependent attribute is one of the event attributes, it belongs to this activity
	public Set<String> desirableOutcomes = new HashSet<String>(Arrays.asList("below"));  // the set of desirable outcomes if the dependent attribute is literal or boolean or "below"or "above"if discrete date or continues
	private String dependentAttName;  // the complete dependent att name.  attName if trace attribute; actName_attName if event attribute
	private Set<String> indepAttributes;
	private Object[] selectedTraceAttributes;
	private Object[] selectedActivityNames;
	private Object[] selectedEventAttributes;
	private boolean tableIsCreated = false;
	private LinkedList<String> attributeNames = null;
	private JScrollPane tableJScrollPane;
	private String[] allOrPlaces;
	private boolean dataHasMissingValues = false;
	boolean depAttIsEventAttandNotIndepAtt = false;
	boolean ifClassAttIsEventIndepAtt = false;
	boolean ifClassActIsInActToConsider = false;
	private Map<String, Object[]> minMax;
	
	/**
	 * When the dependent situation feature is of event type with "Resource", "Timestamp"
	 * or "Duration" activity name (grouper),
	 * then if the selected attribute is a time stamp, then the activities with that occurs
	 * in the given days (depValues) between the given times (min and max) are considered. 
	 * 
	 * if it is a duration attribute then activities with the duration between min and max are considered.
	 * 
	 * if it is a literal attribute, the activities whose values are in depValues are considered.
	 */	
	
	/**
	 * possible values for depActGrouperAttName = {Resource, Timestamp, Duration}
	 */
	private String depActGrouperAttName; // The name of the att when event situation type is selected with a set of activities 
	// which have a specific att value. 
	// specific values in this attribute are selected.
	private String minThreshold;
	private String maxThreshold;
	private Set<String> depValues;
	private long minActDuration;
	private long maxActDuration;
	
	
	private boolean isActGrouperAnEventAtt = false;
	
	public void setIfClassAttIsEventAtt(boolean b) {
		ifClassAttIsEventIndepAtt = b;
	}

	public void setDataHasMissingValues(boolean b) {
		dataHasMissingValues = b;
	}
	
	public boolean dataHasMissingValues() {
		return dataHasMissingValues;
	}
	
	public String[] getAllOrPlaces() {
		return allOrPlaces;
	}
	
	public void setTableIsCreated(boolean b) {
		this.tableIsCreated = b;
	}
	
	public boolean getTableIsCreated() {
		return tableIsCreated;
	}
	
	public void setDependentAttName(String name) {
		if (name.length() > 6 && name.substring(0, 7).equals("Choice_"))
			this.dependentAttName = replaceNotAllowedStrings(name);
		
		this.dependentAttName = name;
		Augmentation aug = new AttributeValue(name);
		this.outputAttribute = setTheAug(aug);
		// if dependent attName is a trace att name but it is not in selected trace attributes,
		// add it to selected trace attributes
		boolean flag = false;
		if (name.equals("Trace_Delay") || name.equals("Trace_Duration") || name.equals("Sub_Model_Attribute")
				|| traceAttributeNames.contains(name)) {
			if (selectedTraceAttributes != null) {
				for (Object o : selectedTraceAttributes) {
					if (replaceNotAllowedStrings((String)o).equals(name)) {
						flag = true;
						break;
					}
				}
			}
			if (flag == false) {
				int l = ((selectedTraceAttributes != null) ? selectedTraceAttributes.length + 1 : 1 );
				Object[] array = new Object[l];
				array[0] = name;
				for (int i = 1; i < l; i++) {
					array[i] = selectedTraceAttributes[i-1];
				}
				selectedTraceAttributes = array;
			}
		} else {
			boolean flagEvent = false;
			if (selectedEventAttributes != null) {
				for (Object o : selectedEventAttributes) {
					if (replaceNotAllowedStrings((String)o).equals(name)) {
						flagEvent = true;
						break;
					}
				}
			}
			if (flagEvent == false) {
				int l = ((selectedEventAttributes != null) ? selectedEventAttributes.length + 1 : 1 );
				Object[] array = new Object[l];
				array[0] = name;
				for (int i = 1; i < l; i++) {
					array[i] = selectedEventAttributes[i-1];
				}
				selectedEventAttributes = array;
				
				// indicating that the dep att is an event att but not a choice att and also not a indep att
				if (name.length() < 6 || !name.substring(0, 7).equals("Choice_"))
					depAttIsEventAttandNotIndepAtt = true;
			}
		}
			
	}
	
	public void setOutputAttribute(Augmentation atts) {
		this.outputAttribute = atts;
	}
	
	public void setSelectedTraceAttributes(Object[] attributes) {
		this.selectedTraceAttributes = attributes;
	}
	
	public Object[] getMinMax(String attName) {
		return minMax.get(attName);
	}

	public Map<String, Object[]> getMinMax() {
		Map<String, Object[]> newInstance = new HashMap<String, Object[]>();
		for (String attName : typesNDC.keySet()) {
			if (typesNDC.get(attName).equals(Type.LITERAL)) {
				for (String val : getLiteralValues(attName)) {
					Object[] mm = {0, 1};
					newInstance.put(attName + " --> " + val, mm);
				}
			} else {
				Double[] mm = new Double[2];
				mm[0] = getDouble(minMax.get(attName)[0]);
				mm[1] = getDouble(minMax.get(attName)[1]);
				newInstance.put(attName, mm);
			}
		}
		
		return minMax;
	}
	
	public double getDouble(Object v) {
		double d = 0;
		if (v instanceof Integer)
			d = ((Integer) v).doubleValue();
		else if (v instanceof Long)
			d = ((Long) v).doubleValue();
		else
			d = (double) v;
		
		return d;
	}
	private void updateMinMax(String augName, Map<String, Object> newInstanceNDC) {
		Type type = typesNDC.get(augName);
		if (type.equals(Type.CONTINUOS) || type.equals(Type.DISCRETE))
			if (!minMax.containsKey(augName)) {
				Object[] o = new Object[2];
				o[0] = newInstanceNDC.get(augName);
				o[1] = newInstanceNDC.get(augName);
				minMax.put(augName, o);
			} else {
				
				if (type.equals(Type.CONTINUOS))
					if ((double)newInstanceNDC.get(augName) < (double)minMax.get(augName)[0])
						minMax.get(augName)[0] = newInstanceNDC.get(augName);
					else
						if ((double)newInstanceNDC.get(augName) > (double)minMax.get(augName)[1])
							minMax.get(augName)[1] = newInstanceNDC.get(augName);
				
				if (type.equals(Type.DISCRETE))
					if ((long)newInstanceNDC.get(augName) < (long)minMax.get(augName)[0])
						minMax.get(augName)[0] = newInstanceNDC.get(augName);
					else
						if ((long)newInstanceNDC.get(augName) > (long)minMax.get(augName)[1])
							minMax.get(augName)[1] = newInstanceNDC.get(augName);

					
			}
		
		
		
	}

	
	public Map<String, Type> getAttributeTypes() {
		return typesNDC;
	}
	
	public void setSelectedEventAttributes(Object[] atts) {
		this.selectedEventAttributes = atts;
	}
	
	public void setSelectedActivityNames(Object[] acts) {
		this.selectedActivityNames = acts;
		this.activitiesToConsider = new ArrayList<String>();
		for (Object o : acts) 
			activitiesToConsider.add(replaceNotAllowedStrings((String)o));
	}
	
	public void setSelectedActivityNames(List acts) {
		this.selectedActivityNames = new Object[acts.size()];
		this.activitiesToConsider = new ArrayList<String>();
		int idx = 0;
		
		for (Object o : acts) {
			activitiesToConsider.add(replaceNotAllowedStrings((String)o));
			selectedActivityNames[idx] = o;
			idx++;
		}
	}

	
	public LinkedList<Map<String, Object>> getInstancesOfNDC() {
		return instancesOfNDC;
	}
		
	public Map<String, Type> getTypesNDC() {
		return typesNDC;
	}
	
	public ArrayList<String> getActivityCollection() {
		return activityCollection;
	}
	
	public void setDesirableOutcome(Collection<String> desirableOutcomes) {
		this.desirableOutcomes = new HashSet<String>();
		this.desirableOutcomes.addAll(desirableOutcomes);
	} 
	
	public Set<String> getDesirableOutcome() {
		return desirableOutcomes;
	}
	
	public Map<String, Set<String>> getLiteralValuesNDC () {
		return literalValuesNDC;
	}
	
	public void setTargetActivityName(String targetActivityName) {
		this.targetActivityName = targetActivityName;
	}
	
	public void setIndependentAttributes(Set<String> indepAttributes) {
		this.indepAttributes = indepAttributes;
	}
	public long getMinTraceDuration() {
		return minTraceDuration;
	}
	
	public long getMaxTraceDuration() {
		return maxTraceDuration;
	}
	
	public void setSelectedORplaces (Set<Place> selectedORPlaces) {
		this.selectedORplaces = new HashSet<Place>();
		this.selectedORplaces.addAll(selectedORPlaces);
		
	}
	
	public Set<Place> getSelectedORplace () {
		return selectedORplaces;
	}

	public void setSelectetSub_model (Set<Transition> selectedTransitions) {
		this.selectetSub_model = new HashSet<Transition>();
		this.selectetSub_model.addAll(selectedTransitions);
	}
	
	public Set<Transition> getSelectetSub_model() {
		return selectetSub_model;
	}
	
	public void setDependentActName(String activity) {
		this.targetActivityName = activity;
	}
	
	public void setTraceDelayThreshold (double traceDelayThreshold) {
		XTrace trace = log.get(0);
		long min = wholeTraceDuration(trace);
		long max = wholeTraceDuration(trace);
		long avg = 0;
		for (XTrace t : log) {
			long temp = wholeTraceDuration(t);
			avg = avg + temp;
			if (temp < min) {
				min = temp;
			}
			if (temp > max) {
				max = temp;
			}
		}
		System.out.println(" min : " + min);
		System.out.println(" max : " + max);
		System.out.println(" avg : " + avg/log.size());
		this.traceDelayThreshold = Math.round(((max - min) * traceDelayThreshold));
		System.out.println(" Delay Threshold : " + this.traceDelayThreshold);
	}
	
	public Collection<String> getActivitiesToConsider () {
		if (activitiesToConsider.isEmpty()) {
			return null;
		}
		return activitiesToConsider;
	}
	
	public JComponent getFairAndNormalTreeVisualization() throws Exception
	{
		return null;
	}
	
	public DataExtraction(XLog log, ResultReplay resReplay) 
	{
		this.log=log;
		this.resReplay = resReplay;
	}
	
	public void setDepActGrouperAttName(String attName) {
		this.depActGrouperAttName = attName;
	}
	
	public String getDepActGrouperAttName() {
		return depActGrouperAttName;
	}
	
	public DataExtraction(XLog log, Petrinet model, PNRepResult res) 
	{
		this.log=log;
		this.model = model;
		this.replayREsultForChoiceAndTime = res;
	}
	
	public String[] getArrayTraceAttributeNames() {
		if (traceAttributeNames == null) {
			return null;
		} 
		String[] result = new String[traceAttributeNames.size()];
		int i = 0;
		for (String str : traceAttributeNames) {
			result[i] = str;
			i++;
		}
		return result;
	}
	
	public Petrinet getModel() {
		return model;
	}
	// set up "originalLog" 
	//      add "startCaseEvent" and "endCaseEvent" to every trace, with the first and last time stamp in the trace respectively
	// set up "originalLogAttributes"
	// 		gather all the event attributes in the log
	// set up "activityCollection"
	//		gather all the activity names in the log
	public void init()
	{	
		originalLog = log;
		// Adding the first trace attributes to the traceAttributeNames.
		// Here we assume that all the traces has the same set of attributes.
		gatherTraceAttributeNames();
		
		if (replayREsultForChoiceAndTime != null)   // adding the choice information and the duration of each action as attributes to the events
			enrichTheLog();
		
		minTraceDuration = wholeTraceDuration(log.get(0));
		//<--
		HashSet<String> tempAttributeSet = new HashSet<String>();
		HashSet<String> tempActivitySet = new HashSet<String>();
		traceAttributesValues= new HashMap<String, Set<String>>(getTraceLiteralValuesMap(log));
		for (XTrace trace : log)
		{	
			for(XEvent event : trace)
			{
				tempActivitySet.add(XConceptExtension.instance().extractName(event));
				for(String attr : event.getAttributes().keySet()) // gathers all the attributes in the event except those that are mentioned
					if (!attr.startsWith("concept:") && !attr.startsWith("time:") && !attr.startsWith("resource:") && !attr.startsWith("org:") && !attr.startsWith("role:"))
						tempAttributeSet.add(attr);	
			}
			long duration = wholeTraceDuration(trace);
			if (duration < minTraceDuration) 
				minTraceDuration = duration;
			if (duration > maxTraceDuration) 
				maxTraceDuration = duration;
		}
		originalLogAttributes.addAll(tempAttributeSet); // collect all the event attribute names in the log except those
		// that start with "concept:", "time:", "resource:" and "org:"
		Collections.sort(originalLogAttributes);
		activityCollection.addAll(tempActivitySet); // collect all the event names in the log
		Collections.sort(this.activityCollection);
		
		for (String s : activityCollection) {
			activitiesToConsider.add(s);
		}
		
		cleanOriginalLogAttributes();
		literalValues = getLiteralValuesMap(log);
		System.out.println(" end of init();");
		
	}
	
	/**
	 * Gathering all the trace attribute names.
	 * 
	 * traceAttributeNames = first trace attribute names + drieven attribute names
	 */
	public void gatherTraceAttributeNames() {
		traceAttributeNames = new HashSet<String>();
		
		XTrace firstTrace = log.get(0);
		Set<String> str = firstTrace.getAttributes().keySet();
		for(String attr : firstTrace.getAttributes().keySet()) // gathers all the attributes in the event except those that are mentioned
		{
			if (!attr.startsWith("concept:") && !attr.startsWith("lifecycle:") && !attr.startsWith("time:"))
			{
				traceAttributeNames.add(attr);
			}					
		}
		traceAttributeNames.add("Choice Attribute");
		traceAttributeNames.add("Sub Model Attribute");
		traceAttributeNames.add("Trace Duration");
		traceAttributeNames.add("Trace Delay");
		traceAttributeNames.add("deviation");
		traceAttributeNames.add("number modelMove");
		traceAttributeNames.add("number logMove");
	}
	
	/**
	 * Adding driven attributes to traces in the log and events, including
	 * choice attribute, deviation, numLogMove, numModelMove, eventDuration
	 */
	public void enrichTheLog() {
		ORplaces orp = new ORplaces( model);
		DurationOfActivity ad = new DurationOfActivity( model);
		for (SyncReplayResult singleVariantReplay : replayREsultForChoiceAndTime) {
			Set<Integer> allTraceIdxOfThisVariant = singleVariantReplay.getTraceIndex();
			for (Integer traceIdx : allTraceIdxOfThisVariant) {
//				System.out.println("trace idx line 544 "+ traceIdx);
				orp.enrichTraceWithORChoices(log.get(traceIdx), singleVariantReplay, traceIdx);
				ad.setActivityDuration(log.get(traceIdx), singleVariantReplay);
			}
		}
		
		minActDuration = ad.getMinAllDurations();
		maxActDuration = ad.getMaxAllDurations();
		System.out.println("oout line 522");	
		allOrPlaces = orp.getArrayOfORPlaceNames();
	}
	
	public DataExtraction(XLog log, Petrinet model, PNRepResult res, ResultReplay resReplay) {
		this.log = log;
		this.resReplay=resReplay;
		this.replayREsultForChoiceAndTime = res;
		this.model = model;
	}
	

	public static String getName(XAttributable element) {
		XAttributeLiteral name = (XAttributeLiteral) element.getAttributes().get("concept:name");
		return name.getValue();
	}
	
	public boolean configureAugmentation(Augmentation[] augmentationCollection)
	{
		for(Augmentation aug : augmentationCollection)
		{
			aug.setLog(originalLog);
			String paramNames[]=aug.getParameterNames();
			for(int i=0;i<paramNames.length;i++)
			{
				String[] value;
				do
				{
					if (aug.getPossibleValuesForParameter(i)==null)
					{
						value=new String[1];
						value[0]=(String) JOptionPane.showInputDialog(null, "Please set the value for "+paramNames[i],
							"Attribute "+aug.getAttributeName(),JOptionPane.PLAIN_MESSAGE,null,null,aug.getDefaultValueForParameter(i)[0]);
					}
					else
						if (!aug.multipleValuesForParameter(i))
						{
							value=new String[1];
							value[0]=(String) JOptionPane.showInputDialog(null, "Please set the value for "+paramNames[i],
								"Attribute "+aug.getAttributeName(),JOptionPane.PLAIN_MESSAGE,null,aug.getPossibleValuesForParameter(i),aug.getDefaultValueForParameter(i)[0]);
						}
						else
						{
							JPanel p=new JPanel(new BorderLayout());
							CheckComboBox cbb=new CheckComboBox(aug.getPossibleValuesForParameter(i));
							Dimension dim=cbb.getPreferredSize();
							cbb.addSelectedItems(aug.getDefaultValueForParameter(i));
							dim.width*=2;
							cbb.setPreferredSize(dim);
							p.add(cbb,BorderLayout.CENTER);
							p.add(new JLabel("Please set the value for "+paramNames[i]),BorderLayout.NORTH);
							int yn=JOptionPane.showConfirmDialog(null, 
									p,"Attribute "+aug.getAttributeName(),JOptionPane.YES_NO_OPTION);
							if (yn==JOptionPane.NO_OPTION)
								value=null;
							else
								value=(String[]) cbb.getSelectedItems().toArray(new String[0]);
							
						}
					if (value==null || value.length==0 || value[0]==null)
						return false;
				} while(!aug.setParameter(i, value));
			}
			if (aug.isTimeInterval())
			{
				timeIntervalAugmentations.add(aug.getAttributeName());
			}
		}
		return true;
	}
	
	/**
	 * This method creates a table by extracting the data related to the
	 * dependent and independent attributes from the enriched event log.
	 * Also this function save the table in a file (bin\dataTable.txt) in a format that is suitable
	 * for Tetrad. 
	 * It is important thet in the resulting table no attribute name includes space or tab.
	 * @return
	 */
	public ArrayList<Map<String, Object>> augmentLogNDC() 
	{
		typesNDC=new HashMap<String, Type>();
		literalValuesNDC=new HashMap<String, Set<String>>();
		traceInstanceMap = new HashMap<XTrace,LinkedList<Map<String,Object>>>();
		instancesOfNDC = new LinkedList<Map<String, Object>>();
		orderedInstancesNDC = new ArrayList<Map<String, Object>>();
		minMax = new HashMap<String, Object[]>();
		
		indepAttributes = new HashSet<String>();
		if (selectedTraceAttributes != null) {
			for (Object o : selectedTraceAttributes)
				indepAttributes.add((String)o);
		}
		
		boolean depAttIsInEventIndepAttributes = false;
				
		if (selectedEventAttributes != null) {
			for (Object o : selectedEventAttributes) {
				indepAttributes.add((String)o);
				if (outputAttribute.getAttributeName().equals(o))
					depAttIsInEventIndepAttributes = true;
			}
		}


		Augmentation[] augmentationCollection = new Augmentation[indepAttributes.size()];
		int idx = 0;
		for (String att : indepAttributes) {
			Augmentation aug;
			aug = new  AttributeValue(att);
			if (outputAttribute.getAttributeName().equals(att))
				outputAttribute = aug;
			augmentationCollection[idx] = aug;
			idx++;
		}
		int total = 0;
	
		this.augementationArray=augmentationCollection;
		
		// if "Choice_Attribute" Augmentation is chosen, it adds an augmentation for each selected OR place
		boolean haveRawChoiceAug = false;
		Set<Augmentation> augSet = new HashSet<Augmentation>();
		for (Augmentation aug : augmentationCollection) {
			if (aug.getAttributeName().length() > 6 && aug.getAttributeName().substring(0, 7).equals("Choice_")) {
				haveRawChoiceAug = true;		//include any augmentation but choice ones	
			} else {
				augSet.add(aug);
			}
		}
		
		if (augmentationCollection.length > 0) {
			Augmentation[] newAugmentationCollection = new Augmentation[augSet.size() + selectedORplaces.size()];
			if (haveRawChoiceAug) {
				if (selectedORplaces != null) {
					int i = 0;
					for (Augmentation aug : augSet) {
						newAugmentationCollection[i] = aug;
						i++;
					}
					for (Place place : selectedORplaces) {
						Augmentation aug;
						aug = new AttributeValue(replaceNotAllowedStrings("Choice_"+place.getLabel()));
						newAugmentationCollection[i] = aug;
						i++;
					}
					augmentationCollection = new Augmentation[newAugmentationCollection.length];
					for (i=0; i< newAugmentationCollection.length; i++) {
						augmentationCollection[i] = newAugmentationCollection[i];
					}
				}
			}
		}
		
		this.augementationArray=augmentationCollection;
		
		LinkedList<Augmentation> traceAugs = new LinkedList<Augmentation>();  // the collection of attributes that belongs to the whole trace
		LinkedList<Augmentation> eventAugs = new LinkedList<Augmentation>();  // the collection of attributes that belongs to an event + choice Attributes
		
		separateTraceAndEventAugmentation(traceAugs, eventAugs);
		// compute the sub_model duration time and add it to the trace attributes
		boolean flag = false;
		if (!traceAugs.isEmpty())
			for(Augmentation aug : traceAugs) {
				if (aug.getAttributeName().equals("Sub_Model_Attribute")) {
					flag = true;
				}
			}
	//	System.out.println("non 582");
		// if the "Sub_Model_Attribute" Augmentation is chosen, this part computes and adds the relevant attribute to each trace
		if (flag && selectetSub_model != null) {
			sub_model_duration smd = new sub_model_duration(selectetSub_model, log, model, replayREsultForChoiceAndTime);
			Map<Integer, Long> smdValues = smd.sub_modelDurations();
			for (Integer traceIdx : smdValues.keySet()) {
				XTrace trace = originalLog.get(traceIdx);
				XAttributeMap amap = trace.getAttributes();
				XAttributeContinuousImpl nvalue = new XAttributeContinuousImpl("sub_model_duration", smdValues.get(traceIdx));
				if (amap.containsKey("sub_model_duration")) {
					amap.remove("sub_model_duration");
				}
				amap.put("sub_model_duration", nvalue);
				XEvent event = trace.get(0);
				XAttributeMap amapEvent = event.getAttributes();
				if (amapEvent.containsKey("sub_model_duration")) {
					amapEvent.remove("sub_model_duration");
				}
				amapEvent.put("sub_model_duration", nvalue);
			}
		}
		
		int numTraces = originalLog.size();
//		System.out.println("originalLog size : "+ originalLog.size());
		boolean isOutputAugATraceAug = false;
		boolean isOutputAugIsChoice = false;
		if (outputAttribute != null) {
			if (outputAttribute.getAttributeName().length() > 6 && outputAttribute.getAttributeName().substring(0, 7).equals("Choice_")) {
				String[] s = outputAttribute.getAttributeName().split("_to_", 2);
				Augmentation aug;
				aug = new AttributeValue(replaceNotAllowedStrings(s[0]));
				outputAttribute = aug;
				isOutputAugIsChoice = true;
			}
			
			for (Augmentation aug : traceAugs) {
				if (aug.getAttributeName().equals(outputAttribute.getAttributeName())) {
					isOutputAugATraceAug = true;
				}
			}
		}
		
		ifClassAttIsEventIndepAtt = !isOutputAugATraceAug && !isOutputAugATraceAug;
		
		System.out.println("ifClassAttIsEventIndepAtt : " + ifClassAttIsEventIndepAtt);
		ifClassActIsInActToConsider = activitiesToConsider.contains(targetActivityName);
		System.out.println("ifClassActIsInActToConsider : " + ifClassActIsInActToConsider);
//		boolean IndActIsInActsToConsider = 
	//	boolean IndAttIsInDepAttributes = (traceAugs.contains(outputAttribute) || (selectedEventAttributes != null && selectedEventAttributes.contains(outputAttribute)));
		
		if (outputAttribute == null || isOutputAugATraceAug) {
			for(XTrace trace : originalLog) {
				Map<String, Object> newInstanceNDC = new HashMap<String, Object>();
				doTraceAugmentations(trace, traceAugs, newInstanceNDC);
				if (!eventAugs.isEmpty()) 
					applyEventAugmentations(trace, eventAugs, newInstanceNDC);
				LinkedList<Map<String, Object>> list = new LinkedList<Map<String, Object>>();
				list.add(newInstanceNDC);
				traceInstanceMap.put(trace, list);
				instancesOfNDC.add( newInstanceNDC);
	//			doUpdate(newInstanceNDC);
				total++;
			}  // end for trace
		}  // end of if traceAugs.contains(outputAttribute)
		// If situation is a situation grouper
		else if (targetActivityName != null && (targetActivityName.equals("Resource") || targetActivityName.equals("Timestamp") 
				|| targetActivityName.equals("Duration"))) {
			Augmentation grouper = getGrouperAug();
			boolean grouperIsAddedToEventAugs = false;
			if (!contains(eventAugs, outputAttribute.getAttributeName())) {
				eventAugs.add(setTheAug(outputAttribute));
				grouperIsAddedToEventAugs = true;
			}
			for(XTrace trace : originalLog) {
				System.out.println("trace length : " + trace.size());
				Map<String, Object> newInstanceNDC = new HashMap<String, Object>();
				doTraceAugmentations(trace, traceAugs, newInstanceNDC);
				for (int eventIdx = 0; eventIdx < trace.size(); eventIdx++) {
					XEvent event = trace.get(eventIdx);
					applyActGrouperEventAugmentations(eventAugs, grouper, event, trace, newInstanceNDC, isOutputAugIsChoice, depAttIsInEventIndepAttributes, grouperIsAddedToEventAugs); 
				}  // end of for each event in the trace
			}  // end of for each trace in the log
		} // end of if situation is a situation grouper
		else {
			for(XTrace trace : originalLog) {
	//			System.out.println("trace no : "+ (elaboratedTrace++));
				Map<String, Object> newInstanceNDC = new HashMap<String, Object>();
				doTraceAugmentations(trace, traceAugs, newInstanceNDC);
				if (eventAugs != null && !eventAugs.isEmpty()) {
					for (int eventIdx = 0; eventIdx < trace.size(); eventIdx++) {
	//					System.out.println("event no : "+ eventIdx);
						XEvent event = trace.get(eventIdx);
						applyEventAugmentations(eventAugs, event, trace, newInstanceNDC, isOutputAugIsChoice, depAttIsInEventIndepAttributes); 
					}  // end of for each event in the trace
				}
			}  // end of for each trace in the log
		}
		System.out.println("total : "+total);
		orderedInstancesNDC = new ArrayList< Map<String, Object>>();
		if (!instancesOfNDC.isEmpty()) 
			for (Map<String, Object> instance : instancesOfNDC) 
				doUpdate(instance);
		
	return orderedInstancesNDC;
	}
	
	private void applyActGrouperEventAugmentations(LinkedList<Augmentation> eventAugs, Augmentation grouper, XEvent event, XTrace trace,
		Map<String, Object> newInstanceNDC, boolean isOutputAugIsChoice, boolean depAttIsInEventIndepAttributes, boolean grouperIsAddedToEventAugs) {
		
		for (Augmentation eventAug : eventAugs) {
			String augName = eventAug.getAttributeName();
			System.out.println("--> " + augName);
			String eventName = (String) getAttributeValues(event.getAttributes().get("concept:name"));
			System.out.println("--> " + eventName);
			String eventAugName = replaceNotAllowedStrings(eventName)+"_"+eventAug.getAttributeName();
			// if aug is the class att
			if (eventAug.getAttributeName().equals(outputAttribute.getAttributeName())) {
				// if activity is one of the class activities
				System.out.println("T/F : " + eventIsAnAllDepEvent(grouper.returnAttribute(event, trace), grouper.getAttributeName()) + " value " + grouper.returnAttribute(event, trace));
				if (eventIsAnAllDepEvent(grouper.returnAttribute(event, trace), grouper.getAttributeName())) {
					if (eventAug.returnAttribute(event, trace) != null && newInstanceNDC.size() >= 1)	{
						String eventAugNameClass = targetActivityName+"_"+eventAug.getAttributeName();
						Map<String, Object> instance = copy(newInstanceNDC);
						instance.put(eventAugNameClass, eventAug.returnAttribute(event, trace));
						System.out.println("--> instance added" );
						instancesOfNDC.add( instance);
						if (traceInstanceMap.containsKey(trace))
							traceInstanceMap.get(trace).add(instance);
						else {
							LinkedList<Map<String, Object>> list = new LinkedList<Map<String, Object>>();
							list.add(instance);
							traceInstanceMap.put(trace, list);
						}
					}
				} 
				// if activity is NOT one of the class activities and class att is an indep att too.
				else if (!grouperIsAddedToEventAugs && activitiesToConsider.contains(eventName)) {
					if (newInstanceNDC.containsKey(eventAugName)) {
						newInstanceNDC.remove(eventAugName);
					}
					newInstanceNDC.put(eventAugName, eventAug.returnAttribute(event, trace));
					System.out.println("--> add to instance");
				}
			} // if aug is not the class att
			 else if (activitiesToConsider.contains(eventName)){
				 if (!grouperIsAddedToEventAugs || (grouperIsAddedToEventAugs && !eventAugName.equals(outputAttribute.getAttributeName()))) {
					 if (newInstanceNDC.containsKey(eventAugName)) {
							newInstanceNDC.remove(eventAugName);
						}
						newInstanceNDC.put(eventAugName, eventAug.returnAttribute(event, trace));
				 }
			}
			
		} // end of grouper 
	}
	
	/**
	 * 
	 * @param oldInstance
	 * @return a deep copy of the given instance.
	 */
	private Map<String, Object> copy(Map<String, Object> oldInstance) {

		Map<String, Object> instance = new HashMap<String, Object>();
		for (String attName : oldInstance.keySet())
			instance.put(attName, oldInstance.get(attName));
		
		return instance;
	}

	private Augmentation getGrouperAug() {		
		if (targetActivityName != null) {
			if (targetActivityName.equals("Resource")) {
				return new Resource();
			}  
				
			if (targetActivityName.equals("Timestamp")) {
				return new Timestamp();
			} 
			
			if (targetActivityName.equals("Duration")) {
				return new ActivityDuration();
			}
		}
				
		return null;
	}

	public boolean setIfClassAttIsEventIndepAtt(LinkedList<Augmentation> eventAugs) {
		if (eventAugs.isEmpty())
			return false;
		
		for (Augmentation aug : eventAugs) 
			if (aug.getAttributeName().equals(outputAttribute.getAttributeName()))
				return true;
		
		return false;
	}
	
	public String classAttributeName() {
		String name = new String();
		if (outputAttribute != null) {
			if (targetActivityName != null && !targetActivityName.equals("Trace")) {
	    		name = targetActivityName+"_"+(outputAttribute.getAttributeName());
	    	} else 
	    		name = outputAttribute.getAttributeName();
		}
		
		return replaceNotAllowedStrings(name);
	}
	
	/**
	 * This method extract or compute the value of each event attribute when the class attribute is a trace attribute.
	 * The values are stored in the newInstance.
	 */
	public void applyEventAugmentations(XTrace trace, LinkedList<Augmentation> eventAugs, Map<String, Object> newInstanceNDC) {
		for (int eventIdx = trace.size()-1; eventIdx >= 0; eventIdx--) {
			XEvent event = trace.get(eventIdx);
			for (Augmentation eventAug : eventAugs) {
				String augName = eventAug.getAttributeName();
				if (augName.length()>6 && augName.substring(0, 7).equals("Choice_")) {
					if (!newInstanceNDC.containsKey(augName)) {
						XAttributeMap amap = event.getAttributes();
						boolean hasThisChoiceAtt = false;
						for (String key: amap.keySet()) {
							if (replaceNotAllowedStrings(key).equals(augName)) {
								hasThisChoiceAtt = true;
							}
						}
						if (hasThisChoiceAtt) {
							XAttribute att = amap.get(augName);
							String eventChoice = new String();
							eventChoice = (String) getAttributeValues(event.getAttributes().get(augName));
							if (eventChoice != null && !eventChoice.equals("NOT SET")) 
								newInstanceNDC.put(augName, eventChoice);
						}
					}
				}  // end of choice augmentation
				else if (activitiesToConsider != null) {
					String eventName = (String) getAttributeValues(event.getAttributes().get("concept:name"));
					if (activitiesToConsider.contains(eventName)) {
						String name = eventAug.getAttributeName();
						String eventAugName = replaceNotAllowedStrings(eventName)+"_"+eventAug.getAttributeName();
						if (!newInstanceNDC.containsKey(eventAugName)) 
							newInstanceNDC.put(eventAugName, eventAug.returnAttribute(event, trace));

					}
				}
			} //end of aug \in eventAug
		}  // end of events in the trace
	}
	
	/**
	 * This method separate trace augmentation and event augmentation from each other.
	 * If the dependent att is a choice att, then it is added to the eventaugs.
	 * @param traceAugs
	 * @param eventAugs
	 */
	public void separateTraceAndEventAugmentation(LinkedList<Augmentation> traceAugs, LinkedList<Augmentation> eventAugs) {
		// traceAugs are the augmentations related to trace which are presented in the trace attributes
		// eventAugs are the augmentations related to events which are presented in the event attributes
		// here the Choice attribute is an event augmentation
		for (Augmentation aug : augementationArray) {
			String augName = aug.getAttributeName();
			if (augName.equals("Trace_Delay") || augName.equals("Trace_Duration") || augName.equals("Sub_Model_Attribute")
					|| traceAttributeNames.contains(augName)) {
				traceAugs.add(setTheAug(aug));
			} else {
				eventAugs.add(setTheAug(aug));
			}
		}	
		
		if (dependentAttName.length() > 6 && dependentAttName.substring(0, 7).equals("Choice_")) {
			Augmentation aug = new AttributeValue(dependentAttName);
			eventAugs.add(aug);
		} 
	}
	
	private boolean contains(LinkedList<Augmentation> eventAugs, String attName) {
		if (eventAugs == null || eventAugs.isEmpty())
			return false;
		
		for (Augmentation aug : eventAugs)
			if (aug.getAttributeName().equalsIgnoreCase(attName))
				return true;
		
		return false;
	}
	
	public Augmentation setTheAug(String augName) {
		if (augName.equals("Executor"))
			return new Executor();
		if (augName.equals("Executor_Group"))
			return new Group();
		if (augName.equals("Resource"))
			return new Resource();
		if (augName.equals("Role"))
			return new Role();
		if (augName.equals("Total_Resource_Workload")) {
			TotalResourceWorkload trw = new TotalResourceWorkload();
			setAugmentationParameter(trw);
			trw.setLog(log);
			return trw;	
		}
		if (augName.equals("Resource_Workload")) {
			TotalResourceWorkload trw=new TotalResourceWorkload();
			setAugmentationParameter(trw);
			ResourceWorkload rw = new ResourceWorkload(trw);
			rw.setLog(log);
			return rw;
		}
		
		if (augName.equals("Activity_Duration"))
			return new ActivityDuration();
		if (augName.equals("Elapsed_Time"))
			return new ElapsedTime();
		if (augName.equals("Remaining_Time"))
			return new RemainingTime();
		if (augName.equals("Timestamp"))
			return new Timestamp();
		

		if (augName.equals("Next_Activity"))
			return new NextActivity();
		if (augName.equals("Previous_Activity"))
			return new PreviousActivity();
		
		Augmentation aug = new AttributeValue(augName);
		return aug;
	}
	
	public Augmentation setTheAug(Augmentation aug) {
		return setTheAug(aug.getAttributeName());
	}
	
	public void setAugmentationParameter(Augmentation aug) {
		String paramNames[]=aug.getParameterNames();
		for(int i=0;i<paramNames.length;i++)
		{
			String[] value;
			do
			{
				if (aug.getPossibleValuesForParameter(i)==null)
				{
					value=new String[1];
					value[0]=(String) JOptionPane.showInputDialog(null, "Please set the value for "+paramNames[i],
						"Attribute "+aug.getAttributeName(),JOptionPane.PLAIN_MESSAGE,null,null,aug.getDefaultValueForParameter(i)[0]);
				}
				else
					if (!aug.multipleValuesForParameter(i))
					{
						value=new String[1];
					value[0]=(String) JOptionPane.showInputDialog(null, "Please set the value for "+paramNames[i],
							"Attribute "+aug.getAttributeName(),JOptionPane.PLAIN_MESSAGE,null,aug.getPossibleValuesForParameter(i),aug.getDefaultValueForParameter(i)[0]);
					}
					else
					{
						JPanel p=new JPanel(new BorderLayout());
						CheckComboBox cbb=new CheckComboBox(aug.getPossibleValuesForParameter(i));
						Dimension dim=cbb.getPreferredSize();
						cbb.addSelectedItems(aug.getDefaultValueForParameter(i));
						dim.width*=2;
						cbb.setPreferredSize(dim);
						p.add(cbb,BorderLayout.CENTER);
						p.add(new JLabel("Please set the value for "+paramNames[i]),BorderLayout.NORTH);
						int yn=JOptionPane.showConfirmDialog(null, 
								p,"Attribute "+aug.getAttributeName(),JOptionPane.YES_NO_OPTION);
						if (yn==JOptionPane.NO_OPTION)
							value=null;
						else
							value=(String[]) cbb.getSelectedItems().toArray(new String[0]);
						
					}
			} while(!aug.setParameter(i, value));
		}
		if (aug.isTimeInterval())
		{
			timeIntervalAugmentations.add(aug.getAttributeName());
		}
	}
	/**
	 * This method extract or compute the value of each event attribute when the class attribute is an event attribute.
	 * The values are stored in the newInstance.
	 * @param eventAugs
	 * @param event
	 * @param trace
	 * @param newInstanceNDC
	 * @param isOutputAugIsChoice
	 * @param IndAttIsInEventDepAttributes
	 */
	public void applyEventAugmentations(LinkedList<Augmentation> eventAugs, XEvent event, XTrace trace, Map<String, Object> newInstanceNDC, boolean isOutputAugIsChoice, boolean IndAttIsInEventDepAttributes) {
		for (Augmentation eventAug : eventAugs) {
			String augName = eventAug.getAttributeName();
			String eventName = (String) getAttributeValues(event.getAttributes().get("concept:name"));
			System.out.println("aug name : " + augName);
			System.out.println("event name : " + eventName);
			if (augName.length()>6 && augName.substring(0, 7).equals("Choice_")) {
				XAttributeMap amap = event.getAttributes();
				boolean hasThisChoiceAtt = false;
				for (String key: amap.keySet()) {
					if (replaceNotAllowedStrings(key).equals(augName)) {
						hasThisChoiceAtt = true;
					}
				}
				if (hasThisChoiceAtt) {
					XAttribute att = amap.get(augName);
					String eventChoice = new String();
					eventChoice = (String) getAttributeValues(event.getAttributes().get(augName));
					if (eventChoice != null && !eventChoice.equals("NOT SET")) {
						if (newInstanceNDC.containsKey(augName)) {
							newInstanceNDC.remove(augName);
						}
						newInstanceNDC.put(augName, eventChoice);
						if (isOutputAugIsChoice) {
							if (outputAttribute.getAttributeName().equals(augName)) {
								instancesOfNDC.add( newInstanceNDC);
								if (traceInstanceMap.containsKey(trace))
									traceInstanceMap.get(trace).add(newInstanceNDC);
								else {
									LinkedList<Map<String, Object>> list = new LinkedList<Map<String, Object>>();
									list.add(newInstanceNDC);
									traceInstanceMap.put(trace, list);
								}
			//					doUpdate(newInstanceNDC);
							}
						}
					}
				}
			} // end of choice augmentation  
			else if (ifClassAttIsEventIndepAtt && ifClassActIsInActToConsider) {
				if (eventAugs.contains(eventAug) && (activitiesToConsider.contains(eventName) || eventName.equals(targetActivityName))) {
					String eventAugName = replaceNotAllowedStrings(eventName)+"_"+eventAug.getAttributeName();
					if (newInstanceNDC.containsKey(eventAugName)) {
						newInstanceNDC.remove(eventAugName);
					}
					newInstanceNDC.put(eventAugName, eventAug.returnAttribute(event, trace));
					if (eventAug.getAttributeName().equals(outputAttribute.getAttributeName()) && eventName.equals(targetActivityName) && newInstanceNDC.get(eventAugName) != null) {
						instancesOfNDC.add( newInstanceNDC);
						if (traceInstanceMap.containsKey(trace))
							traceInstanceMap.get(trace).add(newInstanceNDC);
						else {
							LinkedList<Map<String, Object>> list = new LinkedList<Map<String, Object>>();
							list.add(newInstanceNDC);
							traceInstanceMap.put(trace, list);
						}
			//			doUpdate(newInstanceNDC);
					}
				}
			} else if (!ifClassAttIsEventIndepAtt && !ifClassActIsInActToConsider) {
				String eventAugName = replaceNotAllowedStrings(eventName)+"_"+eventAug.getAttributeName();
				if (eventAugs.contains(eventAug) && activitiesToConsider.contains(eventName) && !eventAug.getAttributeName().equals(outputAttribute.getAttributeName())) {
					if (newInstanceNDC.containsKey(eventAugName)) {
						newInstanceNDC.remove(eventAugName);
					}
					newInstanceNDC.put(eventAugName, eventAug.returnAttribute(event, trace));
				}
				if (eventAug.getAttributeName().equals(outputAttribute.getAttributeName()) && eventName.equals(targetActivityName)) {
					if (newInstanceNDC.containsKey(eventAugName)) {
						newInstanceNDC.remove(eventAugName);
					}
					newInstanceNDC.put(eventAugName, eventAug.returnAttribute(event, trace));
					if (newInstanceNDC.get(eventAugName) != null) {
						instancesOfNDC.add( newInstanceNDC);
						if (traceInstanceMap.containsKey(trace))
							traceInstanceMap.get(trace).add(newInstanceNDC);
						else {
							LinkedList<Map<String, Object>> list = new LinkedList<Map<String, Object>>();
							list.add(newInstanceNDC);
							traceInstanceMap.put(trace, list);
						}
					}
	//				doUpdate(newInstanceNDC);
				}
			} else if (ifClassAttIsEventIndepAtt && !ifClassActIsInActToConsider) {
				String eventAugName = replaceNotAllowedStrings(eventName)+"_"+eventAug.getAttributeName();
//				System.out.println("1 : "+ eventAugs.contains(eventAug));
//				System.out.println("2 : "+ activitiesToConsider.contains(eventName));
				if (eventAugs.contains(eventAug) && (activitiesToConsider.contains(eventName))) {
					if (newInstanceNDC.containsKey(eventAugName)) {
						newInstanceNDC.remove(eventAugName);
					}
					newInstanceNDC.put(eventAugName, eventAug.returnAttribute(event, trace));
				}
				System.out.println(eventAug.getAttributeName().equals(outputAttribute.getAttributeName()));
				System.out.println(eventName.equals(targetActivityName));
				System.out.println(eventAug.returnAttribute(event, trace) != null);
				if (eventAug.getAttributeName().equals(outputAttribute.getAttributeName()) && eventName.equals(targetActivityName) && eventAug.returnAttribute(event, trace) != null) {
					if (newInstanceNDC.containsKey(eventAugName)) {
						newInstanceNDC.remove(eventAugName);
					}
					newInstanceNDC.put(eventAugName, eventAug.returnAttribute(event, trace));
					instancesOfNDC.add( newInstanceNDC);
					if (traceInstanceMap.containsKey(trace))
						traceInstanceMap.get(trace).add(newInstanceNDC);
					else {
						LinkedList<Map<String, Object>> list = new LinkedList<Map<String, Object>>();
						list.add(newInstanceNDC);
						traceInstanceMap.put(trace, list);
					}
		//			doUpdate(newInstanceNDC);
				}
			} else if (!ifClassAttIsEventIndepAtt && ifClassActIsInActToConsider) {
				String eventAugName = replaceNotAllowedStrings(eventName)+"_"+eventAug.getAttributeName();
				if (eventAugs.contains(eventAug) && activitiesToConsider.contains(eventName) && !eventAug.getAttributeName().equals(outputAttribute.getAttributeName())) {
					if (newInstanceNDC.containsKey(eventAugName)) {
						newInstanceNDC.remove(eventAugName);
					}
					newInstanceNDC.put(eventAugName, eventAug.returnAttribute(event, trace));
				}
				if (eventAug.getAttributeName().equals(outputAttribute.getAttributeName()) && eventName.equals(targetActivityName)) {
					if (newInstanceNDC.containsKey(eventAugName)) {
						newInstanceNDC.remove(eventAugName);
					}
					newInstanceNDC.put(eventAugName, eventAug.returnAttribute(event, trace));
					instancesOfNDC.add( newInstanceNDC);
					if (traceInstanceMap.containsKey(trace))
						traceInstanceMap.get(trace).add(newInstanceNDC);
					else {
						LinkedList<Map<String, Object>> list = new LinkedList<Map<String, Object>>();
						list.add(newInstanceNDC);
						traceInstanceMap.put(trace, list);
					}
	//				doUpdate(newInstanceNDC);
				}
			}
		} //end of aug \in eventAug
	}
	
	private boolean eventIsAnAllDepEvent(Object value, String attName) {
		if (value == null)
			return false;
		
		if (targetActivityName.equals(attName) || (targetActivityName.equals("Duration") && (attName.equalsIgnoreCase("activityDuration"))))
				if (attName.equals("Resource")) {
					for (String v : depValues)
						if (v.equals(value.toString()))
							return true;
				} else if (Pattern.compile(Pattern.quote("duration"), Pattern.CASE_INSENSITIVE).matcher(attName).find()) {
					double v = (double) value;
					if (v >= Long.valueOf(minThreshold) && v <= Long.valueOf(maxThreshold))
						return true;
				} else if (attName.equalsIgnoreCase("timeStamp")) {
					int dayOfWeek = 0;
					String time = value.toString();
					if (time.subSequence(0, 3).equals("Sun"))
						dayOfWeek = 1;
					if (time.subSequence(0, 3).equals("Mon"))
						dayOfWeek = 2;
					if (time.subSequence(0, 3).equals("Tue"))
						dayOfWeek = 3;
					if (time.subSequence(0, 3).equals("Wed"))
						dayOfWeek = 4;
					if (time.subSequence(0, 3).equals("The"))
						dayOfWeek = 5;
					if (time.subSequence(0, 3).equals("Fri"))
						dayOfWeek = 6;
					if (time.subSequence(0, 3).equals("Sat"))
						dayOfWeek = 7;
					
					if (isWantedDay(dayOfWeek)) {
						String[] part = time.split(" ");
						String[] hmValue = part[3].split(":");
						String[] hmThresholdMin = minThreshold.split(":");
						String[] hmThresholdMax = maxThreshold.split(":");
						if ((Integer.valueOf(hmValue[0]) >= Integer.valueOf(hmThresholdMin[0])) ||
								((Integer.valueOf(hmValue[0]) == Integer.valueOf(hmThresholdMin[0])) &&
										(Integer.valueOf(hmValue[1]) >= Integer.valueOf(hmThresholdMin[1])) &&
										(Integer.valueOf(hmValue[0]) <= Integer.valueOf(hmThresholdMax[0])) ||
										((Integer.valueOf(hmValue[0]) == Integer.valueOf(hmThresholdMax[0])) &&
												(Integer.valueOf(hmValue[1]) <= Integer.valueOf(hmThresholdMax[1]))) ))
										return true;
					}
				}
		return false;
	}

	private boolean isWantedDay(int day) {
		if (day == 1 && depValues.contains("Sunday"))
			return true;
		if (day == 2 && depValues.contains("Monday"))
			return true;
		if (day == 3 && depValues.contains("Tuesday"))
			return true;
		if (day == 4 && depValues.contains("Wednesday"))
			return true;
		if (day == 5 && depValues.contains("Thursday"))
			return true;
		if (day == 6 && depValues.contains("Friday"))
			return true;
		if (day == 7 && depValues.contains("Saturday"))
			return true;
		return false;
	}

	/**
	 * 
	 * @param log
	 * @return
	 */
	
	// it creates a map of the form <String, Set<Strings>>
	// the key is the name of literal attributes in the log
	// the value is the set of all possible values for the key attribute in the log
	private static Map<String, Set<String>> getLiteralValuesMap(XLog log) {
		
		Map<String, Set<String>> retValue=new HashMap<String, Set<String>>();
		
		for(XTrace trace : log) {
			
			
			for(XEvent event : trace) {
				
				for(XAttribute attributeEntry : event.getAttributes().values()) {
					
					if (attributeEntry instanceof XAttributeLiteral) {
						
						String value = ((XAttributeLiteral)attributeEntry).getValue();
						String varName=attributeEntry.getKey();
						Set<String> literalValues = retValue.get(varName);

						if (literalValues == null) {
							literalValues = new HashSet<String>();
							retValue.put(varName, literalValues);
						}
						
						literalValues.add(value);
					}
				}
			}
		}
		return retValue;
	}
	
	// -->
	// it creates a map of the form <String, Set<Strings>>
	// the key is the name of literal attributes in the log
	// the value is the set of all possible values for the key attribute in the log
	private static Map<String, Set<String>> getTraceLiteralValuesMap(XLog log) {
		
		Map<String, Set<String>> retValue=new HashMap<String, Set<String>>();
		
		for(XTrace trace : log) {
				
				for(XAttribute attributeEntry : trace.getAttributes().values()) {
					
					if (attributeEntry instanceof XAttributeLiteral) {
						
						String value = ((XAttributeLiteral)attributeEntry).getValue();
						String varName=attributeEntry.getKey();
						Set<String> literalValues = retValue.get(varName);

						if (literalValues == null) {
							literalValues = new HashSet<String>();
							retValue.put(varName, literalValues);
						}
						
						literalValues.add(value);
					}
				}
		}
		return retValue;
	}
	//<--

	public Map<String, Type> getTypes() {
		return Collections.unmodifiableMap(types);
	}
	
	// change the "activities" if it is changed
	public void setActivitiesToConsider(Collection<String> activitiesToConsider)
	{
		this.activitiesToConsider=new HashSet<String>(activitiesToConsider);
	}

	//determine the number of occurrence of each possible value of outputAttribute in the instanceSet and its total number of occurrence
	private Pair<TreeMap<Double, Integer>, Integer> determineFrequency(Augmentation outputAttribute) {
		int index=0;
		for(;!augementationArray[index].equals(outputAttribute);index++); // find the index of outputAttribute in the augmentationArray
		
		int totalOccurrence=0;
		TreeMap<Double,Integer> retValue=new TreeMap<Double, Integer>();
		Object value;
		

		for(Object[] instance : instanceSet)
		{
			value=instance[index];
			if (value!=null && value instanceof Number)
			{
				Integer numOccurrences=retValue.get(((Number)value).doubleValue());
				if (numOccurrences==null) numOccurrences=0;
				retValue.put(((Number) value).doubleValue(), numOccurrences+1);
				totalOccurrence++;
			}


		}
		return new Pair<TreeMap<Double,Integer>, Integer>(retValue, totalOccurrence);
	}

	private Pair<Double,Double> determineSmallestGreatest(String outputAttribute) {
		if (outputAttribute.endsWith("'"))
			outputAttribute=outputAttribute.substring(0, outputAttribute.length()-1);
		double smallest=Double.POSITIVE_INFINITY;
		double greatest=Double.NEGATIVE_INFINITY;
		int index=0;
		while(!augementationArray[index].getAttributeName().equals(outputAttribute))
			index++;
		Object value;


		for(Object[] instance : instanceSet)
		{
			value=instance[index];
			{
				if (value!=null && value instanceof Number)
				{
					smallest=Math.min(((Number) value).doubleValue(), smallest);
					greatest=Math.max(((Number) value).doubleValue(), greatest);
				}
				if (value!=null && value instanceof Date)
				{
					smallest=Math.min(((Date)value).getTime(), smallest);
					greatest=Math.max(((Date)value).getTime(), greatest);
				}
			}
		}
		return new Pair<Double, Double>(smallest, greatest);
	}

	private Object getAttributeValues(XAttribute xAttrib) 
	{
		if (xAttrib instanceof XAttributeBoolean)
			return((XAttributeBoolean)xAttrib).getValue();
		else if (xAttrib instanceof XAttributeContinuous)
			return((XAttributeContinuous)xAttrib).getValue();
		else if (xAttrib instanceof XAttributeDiscrete)
			return((XAttributeDiscrete)xAttrib).getValue();
		else if (xAttrib instanceof XAttributeTimestamp)
			return((XAttributeTimestamp)xAttrib).getValue();
		else if (xAttrib instanceof XAttributeLiteral)
			return((XAttributeLiteral)xAttrib).getValue();

		return null;
	}
	
	public ArrayList<String> getOriginalLogAttributes() {
		return originalLogAttributes;
	}

	public Set<String> getLiteralValues(String attribute) {
		return Collections.unmodifiableSet(literalValues.get(attribute));
	}
	
	public Map<String, Set<String>> getAllLiteralValues() {
		return literalValues;
	}

	public Collection<String> getActivities() {
		return Collections.unmodifiableCollection(activityCollection);
	}

	public XLog getLog() {
		return log;
	}

	public ResultReplay getResReplay() {
		return resReplay;
	}
	
	private static Type generateDataElement(Object value) {

		if (value instanceof Boolean) {
			return Type.BOOLEAN;
		} else if (value instanceof Long || value instanceof Integer) {
			return Type.DISCRETE;
		} else if (value instanceof Double || value instanceof Float) {
			return Type.CONTINUOS;
		} else if (value instanceof Date) {
			return Type.TIMESTAMP;
		} else if (value instanceof String) {
			return Type.LITERAL;
		}
		
		return null;	
	}

	public boolean isRegressionTree() {
		return regressionTree;
	}
	
	// return a Map of the form <attribute name, attribute type> for all the attributes in the log. (well, except those mentioned below) duplication is possible
	public static Map<String, Type> extractAttributeInformation(XLog log) {
		HashMap<String, Type> retValue = new HashMap<String, Type>();
		for (XTrace trace : log) {
			
			for (XEvent event : trace)
			{
				for(XAttribute attr : event.getAttributes().values())
				{
					if (!attr.getKey().startsWith("concept:") && !attr.getKey().startsWith("time:") && !attr.getKey().startsWith("resource:"))
					{
						Type classType = generateDataElement(attr);
						if (classType != null)
							retValue.put(attr.getKey(), classType);
					}
				}
			}
			
		}
		/*
		 * return: Mapping of Attribute name to the Attribute Data Type in a HashMap<String, Type>
		 */
		return retValue;
	}
	
	/**
	 * 
	 * returns the event attributes.
	 * @return
	 */
	public List<String> getAttributes() {
		return(originalLogAttributes);
	}

	public int getInstanceSetSize() {
		return instanceSet.size();
	}
	
	public Map<String, Set<String>> getTraceAttributeValuesMap() {
		return this.traceAttributesValues;
	}
	
	public Collection<String> getTraceAttributeNames() {
		for (String s : traceAttributeNames) {
			if (s.length() >= 7 && s.subSequence(0, 7).equals("Choice_")) {
				traceAttributeNames.remove(s);
			}
		}
		return traceAttributeNames;
	}
	
	//-->
   	
   	// input : all the places of a petrinet
   	// output : the set of place labels that are corresponding to 'OR' operation
   	public Set<String> getORPlaces (Collection<Place> places) {
   		Set<String> retValue = new HashSet<String>();
   		for(Place place : places) {
   			String s = new String();
   			String s1 = new String();
   			s = place.getLabel();
   			s1 = place.getLabel();
   			//System.out.println("predictor 1430 "+s);
   			if (s.charAt(0) == '(') {
   				//System.out.println("predictor 1432 %%%%% "+s);
   				s  = s.substring(2, s.length() - 2);
   				//System.out.println("([]) "+s);
   				String[] sides = s.split("]");
   				if (sides[1].substring(2, sides[1].length()).split(",").length > 1) {
   					//System.out.println("$$Choice## --> "+s1);
   					retValue.add(s1);
   				}
   			}
   		}
   		return retValue;
   	}
  	
  	public long wholeTraceDuration(XTrace trace) {
  		if (trace.size() == 1)
  			return 0;
  		
  		XEvent firstEvent = trace.get(0);
		XEvent lastEvent = trace.get(trace.size()-1);
		Date timestampE1=XTimeExtension.instance().extractTimestamp(firstEvent);
		Date timestampE2=XTimeExtension.instance().extractTimestamp(lastEvent);
		return timestampE2.getTime()-timestampE1.getTime();
  	}

   //<-- 
   	
   	public Map<String, Set<String>> getTraceAttributesValues() {
   		return traceAttributesValues;
   	}
   	
   	/**
   	 * 
   	 * Applies the trace augmentations on the given trace
   	 * @param trace
   	 * @param traceAugs
   	 * @param newInstanceNDC
   	 */
   	public void doTraceAugmentations(XTrace trace, LinkedList<Augmentation> traceAugs, Map<String, Object> newInstanceNDC) {
		for(Augmentation traceAug : traceAugs) {
			String traceAugName = traceAug.getAttributeName();
			if (traceAugName.equals("Trace_Delay")) {
				long d = wholeTraceDuration(trace);
//				System.out.println("duration : " + d);
				if (d > traceDelayThreshold) {
					newInstanceNDC.put(traceAugName, "delayed");
				} else {
					newInstanceNDC.put(traceAugName, "on_time");
				}
			} else if (traceAugName.equals("Trace_Duration")) {
				newInstanceNDC.put(traceAugName, wholeTraceDuration(trace));
			//} else if (traceAugName.equals("Sub_Model_Attribute")) {
    		//newInstanceNDC.put(traceAugName, subModelDurationInTrace(trace));
    			//doUpdate(traceAugName, newInstanceNDC);
			} else if (traceAugName.equals("Sub_Model_Attribute")) {
				XAttributeMap amap = trace.get(0).getAttributes();
				if (amap.containsKey("sub_model_duration")) {
					Object duration = getAttributeValues(amap.get("sub_model_duration"));
					newInstanceNDC.put("Sub_Model_Attribute", duration);
				}
			} else{
				
				try
				{
					XEvent firstEvent = trace.get(0);
					newInstanceNDC.put(traceAugName, getAttributeValues(trace.getAttributes().get(traceAugName)));
				}
				catch(Exception err)
				{
					err.printStackTrace();
				}
			}	
		} // end for aug \in traceAugs
   	}


	/**
   	 * This function updates the literalValueNDc and typesNDC according to the new instance.
   	 * @param augName
   	 * @param newInstanceNDC
   	 */
   	public void doUpdate(Map<String, Object> newInstanceNDC) {
   		for (String augName : newInstanceNDC.keySet()) {
   			if (newInstanceNDC.get(augName)!=null) {
   				augName = replaceNotAllowedStrings(augName);
   	   			
   				updateTypes(augName, newInstanceNDC);
   				updateMinMax(augName, newInstanceNDC);
   			}		
   		}
   	}
   	
   	/**
   	 * Updating the Types of attributes if it does not contain the current attribute type.
   	 * @param augName
   	 * @param newInstanceNDC
   	 */
   	private void updateTypes(String augName, Map<String, Object> newInstanceNDC) {
   		if (typesNDC.get(augName)==null ) {
  			typesNDC.put(augName, generateDataElement(newInstanceNDC.get(augName)));
  		}

   		if ((newInstanceNDC.get(augName) instanceof String) || typesNDC.get(augName).equals(Type.DISCRETE) || typesNDC.get(augName).equals(Type.BOOLEAN)){
   			Set<String> valueSet=literalValuesNDC.get(augName);
   			if (valueSet==null)	{
   				valueSet=new HashSet<String>();
   				literalValuesNDC.put(augName,valueSet);
   			}
   			valueSet.add(newInstanceNDC.get(augName).toString());
  		}
	}
   	
   	// removes the not allowed char for the consistency
   	public String replaceNotAllowedStrings(String str) {
   		char[] array=str.toCharArray();
		for(int i=0;i<array.length;i++)
		{
			if (notAllowedChars.indexOf(array[i])!=-1)
				array[i]='_';
		}
		return (new String(array));
   	}
   	
   	// this function used to remove choice attributes and trace_duration and sub_model_duration from
   	// originalLogAttributes.
   	public void cleanOriginalLogAttributes() {
   		if (originalLogAttributes.size() > 0) {
   			Set<String> names = new HashSet<String>();
   			for (String attName : originalLogAttributes) {
   				names.add(attName);
   			}
   			for (String attName : names) {
   				if (attName.length() >= 7 && attName.substring(0, 7).equals("Choice_")) {
   					originalLogAttributes.remove(attName);
   				} else if (attName.equals("Sub_Model_Attribute") || attName.equals("Trace_Duration") 
   						|| attName.equals("Trace_Delay") || attName.equals("Activity_Duration")) {
   					originalLogAttributes.remove(attName);
   				}
   			}
   		}
   	}
   	
   	public String[] getActivitiesArray() {
		String[] array = new String[activityCollection.size()];
		int i = 0;
		for (String str : activityCollection) {
			array[i] = str;
			i++;
		}
			
		return array;
	}
   	public String[] getTraceAttributeNamesArray() {
		for (String s : traceAttributeNames) {
			if (s.length() >= 7 && s.subSequence(0, 7).equals("Choice_")) {
				traceAttributeNames.remove(s);
			}
		}
		
		String[] array = new String[traceAttributeNames.size()];
		int i = 0;
		for (String str : traceAttributeNames) {
			array[i] = str;
			i++;
		}
			
		return array;
	}
   	
   	public Map<XTrace, LinkedList<Map<String, Object>>> getTraceInstanceMap() {
   		return traceInstanceMap;
   	}

	public void setMin(String text) {
		minThreshold = text;
	}

	public void setMax(String text) {
		maxThreshold = text;
		
	}

	public long getMinAllActDuration() {
		return minActDuration;
	}
	
	public long getMaxAllActDuration() {
		return maxActDuration;
	}

   	//*********************** Other Class Test Helpers ********************
   	 
   	public void setTypes(Map<String, Type> types) {
   		this.typesNDC = types;
   	}
   	
   	public void setInstances(LinkedList<Map<String, Object>> instances) {
   		this.instancesOfNDC = instances;
   	}
   	
   	public void setClassAttName(String name) {
   		this.outputAttribute = new AttributeValue(name);
   	}
   	
   	public DataExtraction() {
   		
   	}

	public DataExtraction(XLog log) {
		this.log = log;
	}

	public void setAttNames(ArrayList<String> attList) {
		originalLogAttributes = attList;
		
	}

	public void setAllActivityAttValues(Collection selectedItems) {
		depValues = new HashSet<String>();
		for (Object a : selectedItems)
			depValues.add((String)a);
		
	}
	
}