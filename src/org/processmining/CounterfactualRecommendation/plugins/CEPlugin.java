package org.processmining.CounterfactualRecommendation.plugins;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.model.XLog;
import org.processmining.CounterfactualRecommendation.algorithms.DataExtraction;
import org.processmining.CounterfactualRecommendation.algorithms.GenerateFinalSamples;
import org.processmining.CounterfactualRecommendation.parameters.ClassificationMethod;
import org.processmining.CounterfactualRecommendation.parameters.CounterfactualParameters;
import org.processmining.CounterfactualRecommendation.parameters.LowOrHeigh;
import org.processmining.CounterfactualRecommendation.ui.WisardStepParameters;
import org.processmining.CounterfactualRecommendation.ui.WizardStepReport;
import org.processmining.CounterfactualRecommendation.ui.WizardStepTable;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.util.ui.wizard.ListWizard;
import org.processmining.framework.util.ui.wizard.ProMWizardDisplay;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
	

public class CEPlugin {

	@Plugin(

			name = "Counterfactual Explanations", parameterLabels = {"log", "model", "replay result"}, returnLabels = 
		{"Counterfactual Explanations"}, returnTypes = { GenerateFinalSamples.class}, userAccessible = true, help = "help")
	//parameterLabels = {"log", "model", "replay result"}
	@UITopiaVariant(affiliation = "University of PADS RWTH Aachen", author = "Mahnaz S. Qafari", email = "m.s.qafari@pads.rwth-aachen.de")
	// Test IT company
	public static GenerateFinalSamples apply(UIPluginContext context, XLog log, Petrinet model, PNRepResult res) {
		
//		return testLRT(context, log, model , res);  // linear Company IT
		
		return applay(context, log, model , res);
		
//		return BPI2017(context, log);
		
		
/**		DataExtraction de = new DataExtraction(log, model, res);
		de.init();
		
		// this part is added for the sake of easy testing.
		de.setDependentAttName("Trace Duration"); 
		de.setDependentActName("Trace");
		Collection<String> actNames = new HashSet<String>();
		actNames.add("Confirmation of receipt");
		actNames.add("T02 Check confirmation of receipt");
		actNames.add("T04 Determine confirmation of receipt");
		actNames.add("T06 Determine necessity of stop advice");
		actNames.add("T10 Determine necessity to stop indication");
		de.setActivitiesToConsider(actNames);	
		Object[] atts = {"activityduration"};
		de.setSelectedEventAttributes(atts);
		de.augmentLogNDC();  */
//		CounterfactualParameters params = new CounterfactualParameters();
//		params.setDataExtraction(de);
//		params.setGoodResultNmericalThreshold(520);
//	params.setGoodResultNmericalThreshold(2800);
//		
//	String sem = "initiate_hardness = noise\r\n" +
//				 "initiate_priority = noise\r\n" +
//				 "initiate_num_people = round(initiate_hardness  * sqrt(initiate_hardness) + initiate_priority * initiate_priority,0) + noise\r\n" +
//				 "initiate_man_day = round(initiate_hardness * initiate_hardness + floor(initiate_hardness/2),0) + noise\r\n" +
//			 	 "maintain_man_day = round(initiate_hardness * initiate_hardness * initiate_hardness + initiate_hardness * initiate_num_people * 5 * sqrt(initiate_num_people) - (initiate_num_people # 5 +1) * sqrt(initiate_num_people # 5 +1),0) + noise\r\n";
//	params.setSEM(sem);
//	Map<String, Object> instance = new HashMap<>();
/**		String sem = "Trace_Duration = +1.2315 * T06_Determine_necessity_of_stop_advice_activityduration+1.4703 * T04_Determine_confirmation_of_receipt_activityduration+0.4138 * T10_Determine_necessity_to_stop_indication_activityduration + noise\r\n" + 
				"Confirmation_of_receipt_activityduration =  noise\r\n" + 
				"T06_Determine_necessity_of_stop_advice_activityduration = +0.1687 * Confirmation_of_receipt_activityduration + noise\r\n" + 
				"T04_Determine_confirmation_of_receipt_activityduration = +0.068 * T02_Check_confirmation_of_receipt_activityduration + noise\r\n" + 
				"T02_Check_confirmation_of_receipt_activityduration =  noise\r\n" + 
				"T10_Determine_necessity_to_stop_indication_activityduration =  noise\r\n";
		CounterfactualParameters params = new CounterfactualParameters(); */
		
		
		
/**		params.setSEM(sem);
		params.setDataExtraction(de);
		params.setGoodResultNmericalThreshold(500000000);
		Map<String, Object> instance = new HashMap<>();
		instance.put("Trace_Duration", 577940448);
		instance.put("Confirmation_of_receipt_activityduration", 61897);
		instance.put("T06_Determine_necessity_of_stop_advice_activityduration", 19193);
		instance.put("T04_Determine_confirmation_of_receipt_activityduration", 41719551);
		instance.put("T02_Check_confirmation_of_receipt_activityduration", 81630);
		instance.put("T10_Determine_necessity_to_stop_indication_activityduration", 41719551);
		params.setCurrentInstance(instance);  */
//		WisardStepParameters wizStepParam = new WisardStepParameters(de.getTraceInstanceMap(), de.getAttributeTypes(), de.classAttributeName());
//		List<ProMWizardStep<CounterfactualParameters>> wizStepListR = new ArrayList<>();
//		wizStepListR.add(wizStepParam);
//		ListWizard<CounterfactualParameters> listWizardR = new ListWizard<>(wizStepListR);
//		
		
//	instance.put("initiate_hardness", 7);
//	instance.put("initiate_man_day", 53);
//	instance.put("initiate_num_people", 19);
//	instance.put("initiate_priority", 1);
//	instance.put("maintain_man_day", 3244);
//	params.setCurrentInstance(instance);
//	
	
	}

	private static GenerateFinalSamples BPI2017(UIPluginContext context, XLog log) {
		DataExtraction de = new DataExtraction(log);
		de.init();
		de.setDependentAttName("Selected"); 
		de.setDependentActName("Trace");
		Collection<String> actNames = new HashSet<String>();
		actNames.add("O_Create Offer");
		de.setActivitiesToConsider(actNames);	
		Object[] atts = {"numberOffers", "RequestedAmount", "ApplicationType", 
			//	"Motorcycle", "Remaining_debt_home", "Debt_restructuring", 
			//	"Home_improvement", "Extra_spending_limit",
			//	"Caravan___Camper", "Tax_payments", "Car", "Other__see_explanation",
			//	"Existing_loan_takeover", "Business_goal", "Not_speficied", "Boat",
				"CreditScore", "NumberOfTerms", // "MonthlyCost", 
				"FirstWithdrawalAmount"};//, "OfferedAmount"};
		de.setSelectedEventAttributes(atts);

		de.augmentLogNDC();
		LinkedList<Map<String, Object>> instances = de.getInstancesOfNDC();
		System.out.println(" Table is creates!");
		
		// set parameters
		CounterfactualParameters params = new CounterfactualParameters();
		params.setDataExtraction(de);
		Map<String, Object> instance = new HashMap<>();
		instance.put("RequestedAmount", 20000);
		instance.put("numberOffers", 2);
		instance.put("ApplicationType", 1);
//		instance.put("Motorcycle", 0);
//		instance.put("Remaining_debt_home", 0);
//		instance.put("Debt_restructuring", 0);
//		instance.put("Home_improvement", 0);
//		instance.put("Extra_spending_limit", 0);
//		instance.put("Caravan___Camper", 0);
//		instance.put("Tax_payments", 0);
//		instance.put("Car", 0);
		instance.put("Selected", 0);
//		instance.put("Other__see_explanation", 1);
//		instance.put("Existing_loan_takeover", 0);
//		instance.put("Business_goal", 0);
//		instance.put("Not_speficied", 0);
//		instance.put("Boat", 0);
		instance.put("O_Create_Offer_CreditScore", 0);
		instance.put("O_Create_Offer_NumberOfTerms", 42);	// --
//		instance.put("O_Create_Offer_MonthlyCost", 200);  // --
		instance.put("O_Create_Offer_FirstWithdrawalAmount", 5000); // --
//		instance.put("O_Create_Offerr_OfferedAmount", 30000.0);
		params.setCurrentInstance(instance);
		params.setGoodResultNmericalThreshold(0.5);
		params.setLowerOrHeigher(LowOrHeigh.HEIGHER);
		params.setDoOptimization(true);
		params.setNonTimeStep(0.01);
		params.setDoRandomSampling(true);
		Collection<String> actionableAttNames = new HashSet<String>();
		actionableAttNames.add("O_Create_Offer_FirstWithdrawalAmount");
//		actionableAttNames.add("O_Create_Offer_MonthlyCost");
		actionableAttNames.add("O_Create_Offer_NumberOfTerms");
		actionableAttNames.add("numberOffers");
		params.setActionableAttNames(actionableAttNames);
		params.setMethod(ClassificationMethod.NN);
		params.setHiddenLayers("16, 16, 8");
		params.setLearningRate(0.05);
		params.setMomentum(0.1);
		params.setNumEpoches(200);
		
		GenerateFinalSamples gfs = new GenerateFinalSamples(de, params);
//		test(de, params);
		return gfs;
	}
	
	private static void test(DataExtraction de, CounterfactualParameters params) {
		//read instances
		try (BufferedReader br = new BufferedReader(new FileReader("bin\\testPluginData.txt"))) {
		    String line = br.readLine();
		    //create the header
		    String[] header = line.split(",");
		    int i = 1;
		    while ((line = br.readLine()) != null) {
		       System.out.println("\n-----------------------instance " + i);
		       i++;
		       params.setCurrentInstance(getOneInstance(header, line));
		       GenerateFinalSamples gfs = new GenerateFinalSamples(de, params);
		    }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * convert each line to an instance
	 * @param header
	 * @param line
	 * @return
	 */
	private static Map<String, Object> getOneInstance(String[] header, String line) {
		Map<String, Object> instance = new HashMap<>();
		String[] parts = line.split(",");
		for (int i = 0; i < header.length; i++) {
			if (header[i].equals("CreditScore"))
				instance.put("O_Create_Offer_CreditScore", Double.valueOf(parts[i]));
			else if (header[i].equals("NumberOfTerms"))
				instance.put("O_Create_Offer_NumberOfTerms", Double.valueOf(parts[i]));
			else if (header[i].equals("MonthlyCost"))
				instance.put("O_Create_Offer_MonthlyCost", Double.valueOf(parts[i]));
			else if (header[i].equals("FirstWithdrawalAmount"))
				instance.put("O_Create_Offer_FirstWithdrawalAmount", Double.valueOf(parts[i]));
			else 
				instance.put(header[i], Double.valueOf(parts[i]));
		}
		instance.put("Selected", 0.0);
		return instance;
	}

	private static GenerateFinalSamples applay(UIPluginContext context, XLog log, Petrinet model, PNRepResult res) {
		DataExtraction de = new DataExtraction(log, model, res);
		de.init();
		WizardStepTable stepTable = new WizardStepTable(context, log, model , res);
		List<ProMWizardStep<DataExtraction>> steplist = new ArrayList<ProMWizardStep<DataExtraction>>();
		steplist.add(stepTable);
		ListWizard<DataExtraction> listWizard = new ListWizard<DataExtraction>(steplist);
		DataExtraction de1 = ProMWizardDisplay.show(context, listWizard, de);
		
		de.augmentLogNDC();
		LinkedList<Map<String, Object>> instances = de.getInstancesOfNDC();
		System.out.println(" Table is creates!");
		
		WisardStepParameters wizStepParam = new WisardStepParameters(de.getTraceInstanceMap(), de.getAttributeTypes(), de.classAttributeName());
		List<ProMWizardStep<CounterfactualParameters>> wizStepListR = new ArrayList<>();
		wizStepListR.add(wizStepParam);
		ListWizard<CounterfactualParameters> listWizardR = new ListWizard<>(wizStepListR);
		CounterfactualParameters pms = new CounterfactualParameters();
		pms.setDataExtraction(de);
		CounterfactualParameters params2 = ProMWizardDisplay.show(context, listWizardR, pms);
		
		GenerateFinalSamples gfs = new GenerateFinalSamples(de, params2);
		WizardStepReport wizStepReport = new WizardStepReport(gfs);
		List<ProMWizardStep<GenerateFinalSamples>> wizardStepListReport = new ArrayList<>();
		wizardStepListReport.add(wizStepReport);
		ListWizard<GenerateFinalSamples> listWizardReport = new ListWizard<>(wizardStepListReport);
		GenerateFinalSamples g = ProMWizardDisplay.show(context, listWizardReport, gfs);
		return gfs;	
	}
	
	/**
	 * Test the plugin for IT company, linear, SEM
	 * @param context
	 * @param log
	 * @param model
	 * @param res
	 * @return
	 */
	private static GenerateFinalSamples test(UIPluginContext context, XLog log, Petrinet model, PNRepResult res) {
		DataExtraction de = new DataExtraction(log, model, res);
		de.init();
		de.setDependentAttName("man day"); 
		de.setDependentActName("maintain");
		Collection<String> actNames = new HashSet<String>();
		actNames.add("initiate");
		de.setActivitiesToConsider(actNames);	
		Object[] atts = {"hardness", "man day", "priority", "num people"};
		de.setSelectedEventAttributes(atts);

		de.augmentLogNDC();
		LinkedList<Map<String, Object>> instances = de.getInstancesOfNDC();
		System.out.println(" Table is creates!");
		
		// set parameters
		CounterfactualParameters params = new CounterfactualParameters();
		params.setDataExtraction(de);
		Map<String, Object> instance = new HashMap<>();
		instance.put("initiate_hardness", 7);
		instance.put("initiate_man_day", 71);
		instance.put("initiate_num_people", 42);
		instance.put("initiate_priority", 2);
		instance.put("maintain_man_day", 577);
		params.setCurrentInstance(instance);
		params.setGoodResultNmericalThreshold(500);
		params.setLowerOrHeigher(LowOrHeigh.LOWER);
		params.setDoOptimization(true);
		params.setNonTimeStep(0.01);
		params.setDoRandomSampling(true);
		Collection<String> actionableAttNames = new HashSet<String>();
		actionableAttNames.add("initiate_hardness");
		actionableAttNames.add("initiate_priority");
		actionableAttNames.add("initiate_num_people");
		params.setActionableAttNames(actionableAttNames);
		params.setMethod(ClassificationMethod.SEM);
		String sem = "initiate_hardness =  noise\r\n" + 
				"initiate_man_day = +10 * initiate_hardness + noise\r\n" + 
				"initiate_num_people = +5 * initiate_hardness+3 * initiate_priority + noise\r\n" + 
				"initiate_priority =  noise\r\n" + 
				"maintain_man_day = +50 * initiate_hardness + 5 * initiate_num_people + noise";
		params.setSEM(sem);
		
		GenerateFinalSamples gfs = new GenerateFinalSamples(de, params);
		return gfs;
	}
	/**
	 * Test the plugin for IT company, linear, RT
	 * @param context
	 * @param log
	 * @param model
	 * @param res
	 * @return
	 */
	private static GenerateFinalSamples testLRT(UIPluginContext context, XLog log, Petrinet model, PNRepResult res) {
		DataExtraction de = new DataExtraction(log, model, res);
		de.init();
		de.setDependentAttName("man day"); 
		de.setDependentActName("maintain");
		Collection<String> actNames = new HashSet<String>();
		actNames.add("initiate");
		de.setActivitiesToConsider(actNames);	
		Object[] atts = {"hardness", "man day", "priority", "num people"};
		de.setSelectedEventAttributes(atts);

		de.augmentLogNDC();
		LinkedList<Map<String, Object>> instances = de.getInstancesOfNDC();
		System.out.println(" Table is creates!");
		
		// set parameters
		CounterfactualParameters params = new CounterfactualParameters();
		params.setDataExtraction(de);
		Map<String, Object> instance = new HashMap<>();
		instance.put("initiate_hardness", 7);
		instance.put("initiate_man_day", 71);
		instance.put("initiate_num_people", 42);
		instance.put("initiate_priority", 2);
		instance.put("maintain_man_day", 577);
		params.setCurrentInstance(instance);
		params.setGoodResultNmericalThreshold(500);
		params.setLowerOrHeigher(LowOrHeigh.LOWER);
		params.setDoOptimization(false);
		params.setNonTimeStep(0.01);
		params.setDoRandomSampling(true);
		Collection<String> actionableAttNames = new HashSet<String>();
		actionableAttNames.add("initiate_hardness");
		actionableAttNames.add("initiate_priority");
		actionableAttNames.add("initiate_num_people");
		params.setActionableAttNames(actionableAttNames);
		params.setMethod(ClassificationMethod.RT);
		params.setNoPruning(false);
		params.setMinNum(2);
		params.setMaxDepth(-1);
		
		GenerateFinalSamples gfs = new GenerateFinalSamples(de, params);
		return gfs;
	}
	
	/**
	 * public static void apply(UIPluginContext context, XLog log, Petrinet model, PNRepResult res) {
		DataExtraction de = new DataExtraction(log, model, res);
		de.init();
		
		// this part is added for the sake of easy testing.
		de.setDependentAttName("Trace Duration"); 
		de.setDependentActName("Trace");
		Collection<String> actNames = new HashSet<String>();
		actNames.add("Confirmation of receipt");
		actNames.add("T02 Check confirmation of receipt");
		actNames.add("T04 Determine confirmation of receipt");
		actNames.add("T06 Determine necessity of stop advice");
		actNames.add("T10 Determine necessity to stop indication");
		de.setActivitiesToConsider(actNames);	
		Object[] atts = {"activityduration"};
		de.setSelectedEventAttributes(atts);
//		WizardStepTable stepTable = new WizardStepTable(context, log, model , res);
//		List<ProMWizardStep<DataExtraction>> steplist = new ArrayList<ProMWizardStep<DataExtraction>>();
//		steplist.add(stepTable);
//		ListWizard<DataExtraction> listWizard = new ListWizard<DataExtraction>(steplist);
//		DataExtraction de1 = ProMWizardDisplay.show(context, listWizard, de);
		de.augmentLogNDC();
		LinkedList<Map<String, Object>> instances = de.getInstancesOfNDC();
		System.out.println(" Table is creates!");
		
		// test code
		CounterfactualParameters params = new CounterfactualParameters();
		params.setDataExtraction(de);
		params.setGoodResultNmericalThreshold(1.9232861009868E9);
		String sem = "Trace_Duration = +1.2315 * T06_Determine_necessity_of_stop_advice_activityduration+1.4703 * T04_Determine_confirmation_of_receipt_activityduration+0.4138 * T10_Determine_necessity_to_stop_indication_activityduration + noise\r\n" + 
				"Confirmation_of_receipt_activityduration =  noise\r\n" + 
				"T06_Determine_necessity_of_stop_advice_activityduration = +0.1687 * Confirmation_of_receipt_activityduration + noise\r\n" + 
				"T04_Determine_confirmation_of_receipt_activityduration = +0.068 * T02_Check_confirmation_of_receipt_activityduration + noise\r\n" + 
				"T02_Check_confirmation_of_receipt_activityduration =  noise\r\n" + 
				"T10_Determine_necessity_to_stop_indication_activityduration =  noise\r\n";
		params.setSEM(sem);
		Map<String, Object> instance = new HashMap<>();
		instance.put("Trace_Duration", 2412485627l);
		instance.put("Confirmation_of_receipt_activityduration", 614411017l);
		instance.put("T06_Determine_necessity_of_stop_advice_activityduration", 61640l);
		instance.put("T04_Determine_confirmation_of_receipt_activityduration", 5067090l);
		instance.put("T02_Check_confirmation_of_receipt_activityduration", 22107l);
		instance.put("T10_Determine_necessity_to_stop_indication_activityduration",31755l);
		params.setCurrentInstance(instance);
		
//		WisardStepParameters wizStepParam = new WisardStepParameters(de.getTraceInstanceMap(), de.getAttributeTypes(), de.classAttributeName());
//		List<ProMWizardStep<CounterfactualParameters>> wizStepListR = new ArrayList<>();
//		wizStepListR.add(wizStepParam);
//		ListWizard<CounterfactualParameters> listWizardR = new ListWizard<>(wizStepListR);
//		CounterfactualParameters pms = new CounterfactualParameters();
//		pms.setDataExtraction(de);
//		CounterfactualParameters params = ProMWizardDisplay.show(context, listWizardR, pms);
		
		GenerateFinalSamples gfs = new GenerateFinalSamples(de, params);
		WizardStepReport wizStepReport = new WizardStepReport(gfs);
		List<ProMWizardStep<GenerateFinalSamples>> wizardStepListReport = new ArrayList<>();
		wizardStepListReport.add(wizStepReport);
		ListWizard<GenerateFinalSamples> listWizardReport = new ListWizard<>(wizardStepListReport);
		GenerateFinalSamples g = ProMWizardDisplay.show(context, listWizardReport, gfs);
	} */
	 
	/**  // Test trace duration
	 * 	public static void apply(UIPluginContext context, XLog log, Petrinet model, PNRepResult res) {
		DataExtraction de = new DataExtraction(log, model, res);
		de.init();
		
		de.setDependentAttName("Trace Duration"); 
		de.setDependentActName("Trace");
		Collection<String> actNames = new HashSet<String>();
		actNames.add("Confirmation of receipt");
		actNames.add("T02 Check confirmation of receipt");
		actNames.add("T04 Determine confirmation of receipt");
		actNames.add("T06 Determine necessity of stop advice");
		actNames.add("T10 Determine necessity to stop indication");
		de.setActivitiesToConsider(actNames);	
		Object[] atts = {"activityduration"};
		de.setSelectedEventAttributes(atts);

		de.augmentLogNDC();
		LinkedList<Map<String, Object>> instances = de.getInstancesOfNDC();
		System.out.println(" Table is creates!");
		
		CounterfactualParameters params = new CounterfactualParameters();
		params.setDataExtraction(de);
		params.setGoodResultNmericalThreshold(1.9232861009868E9);
		String sem = "Trace_Duration = +1.2315 * T06_Determine_necessity_of_stop_advice_activityduration+1.4703 * T04_Determine_confirmation_of_receipt_activityduration+0.4138 * T10_Determine_necessity_to_stop_indication_activityduration + noise\r\n" + 
				"Confirmation_of_receipt_activityduration =  noise\r\n" + 
				"T06_Determine_necessity_of_stop_advice_activityduration = +0.1687 * Confirmation_of_receipt_activityduration + noise\r\n" + 
				"T04_Determine_confirmation_of_receipt_activityduration = +0.068 * T02_Check_confirmation_of_receipt_activityduration + noise\r\n" + 
				"T02_Check_confirmation_of_receipt_activityduration =  noise\r\n" + 
				"T10_Determine_necessity_to_stop_indication_activityduration =  noise\r\n";
		params.setSEM(sem);
		Map<String, Object> instance = new HashMap<>();
		instance.put("Trace_Duration", 2412485627l);
		instance.put("Confirmation_of_receipt_activityduration", 614411017l);
		instance.put("T06_Determine_necessity_of_stop_advice_activityduration", 61640l);
		instance.put("T04_Determine_confirmation_of_receipt_activityduration", 5067090l);
		instance.put("T02_Check_confirmation_of_receipt_activityduration", 22107l);
		instance.put("T10_Determine_necessity_to_stop_indication_activityduration",31755l);
		params.setCurrentInstance(instance);
		
		GenerateFinalSamples gfs = new GenerateFinalSamples(de, params);
		WizardStepReport wizStepReport = new WizardStepReport(gfs);
		List<ProMWizardStep<GenerateFinalSamples>> wizardStepListReport = new ArrayList<>();
		wizardStepListReport.add(wizStepReport);
		ListWizard<GenerateFinalSamples> listWizardReport = new ListWizard<>(wizardStepListReport);
		GenerateFinalSamples g = ProMWizardDisplay.show(context, listWizardReport, gfs);
	}
	 */
}
