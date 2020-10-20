package thtsNew;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import acceptance.AcceptanceOmega;
import acceptance.AcceptanceType;
import automata.DA;
import explicit.LTLModelChecker;
import parser.State;
import parser.ast.Expression;
import parser.ast.ExpressionQuant;
import parser.ast.ExpressionReward;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import prism.DefaultModelGenerator;
import prism.Prism;
import prism.PrismDevNullLog;
import prism.PrismException;
import prism.PrismFileLog;
import prism.PrismLog;
import simulator.ModulesFileModelGenerator;
import thts.Objectives;

public class TestLRTDPNestedMaSAS_rolloutpol {

	// running this from the commandline
	// PRISM_MAINCLASS=thtsNew.TestLRTDPNestedMaSAS_rolloutpol prism/bin/prism
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			TestLRTDPNestedMaSAS_rolloutpol tester = new TestLRTDPNestedMaSAS_rolloutpol();

			String resString = "";
			String resLine;
			String currentDir = System.getProperty("user.dir");
			String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
			String resultsLocation = testsLocation + "results/csvs/";

			FileWriter fw = new FileWriter(resultsLocation + "uanvoidable_banjo.csv", true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw);
			resLine="\nName\tFSP\tRun\tTC_U\tTC_L\tC_U\tC_L\tSolved\tGoal\tProbGoal"; 
			out.print(resLine);
			for(int c = 1; c<=4; c++) {
			for(int i = 0; i<10; i++) {
			THTSRunInfo rinfo = tester.unavoidableSingleAgentSolH(true,i,c);
		
			resLine="\nunavoidable"+"\tc"+c+"\t"+i+"\t"+rinfo.getBoundsString(Objectives.TaskCompletion, "\t")+"\t"
			+rinfo.getBoundsString(Objectives.Cost, "\t")+"\t"+rinfo.initialStateSolved+"\t"
			+rinfo.goalFound+"\t"+rinfo.goalOnProbablePath; 
//			resLine = "\nunavoidable\t" + rinfo.toString();
			out.print(resLine);
			resString += resLine;
			}
			}
//			for (int i = 0; i <= 100; i += 10) {
//				for (int j = 0; j < 10; j++) {
//					rinfo = tester.grid5SingleAgentSolH(true, i);
////					resLine = "\nGrid5-fsp" + i + "\t" + rinfo.toString();
//					resLine="\nG\t"+i+"\t"+j+"\t"+rinfo.getBoundsString(Objectives.TaskCompletion, "\t")+"\t"
//							+rinfo.getBoundsString(Objectives.Cost, "\t")+"\t"+rinfo.initialStateSolved+"\t"
//							+rinfo.goalFound+"\t"+rinfo.goalOnProbablePath; 
//					out.print(resLine);
//					resString += resLine;
//				}
//			}
			System.out.println(resString);
			out.close();
			bw.close();
			fw.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
// things to do 
	// find the sangle agent solution function 
	// get strategies for each agent and store them with states 
	// we want the mdp choices not the joint mdp ones 
	// so perhaps we'll need the actions 
	// create a new action selection function 
	//also have to remove the trial len thing 
	//you can basically set it to a very large number or just add a caveat 
	// like an if and set it to -1 so that its never true etc 
	//we need to look at the thts paper okay 
	//but for now lets just comment on all the places we need to edit things 
	

	public void createDirIfNotExist(String directoryName) {
		File directory = new File(directoryName);
		if (!directory.exists()) {

			directory.mkdirs();
		}

	}

	public MultiAgentNestedProductModelGenerator createNestedMultiAgentModelGen(Prism prism, PrismLog mainLog,
			ArrayList<String> filenames, String propertiesFileName, String resultsLocation, boolean hasSharedState)
			throws PrismException, IOException {

		AcceptanceType[] allowedAcceptance = { AcceptanceType.RABIN, AcceptanceType.REACH };

		String initString = "Initial States: [ ";
		// step 1
		// create the modulesfilemodelgenerators
		ArrayList<ModulesFileModelGenerator> mfmodgens = new ArrayList<>();
		ModulesFile modulesFile = null; // just here so we can use the last modules file for our properties

		for (String modelFileName : filenames) {
			mainLog.println("Loading model gen for " + modelFileName);
			modulesFile = prism.parseModelFile(new File(modelFileName)); // because the models are uniform
			// we might have to find a way to change this later
			ModulesFileModelGenerator modGen = new ModulesFileModelGenerator(modulesFile, prism);
			initString += modGen.getInitialState() + " ";
			mfmodgens.add(modGen);
		}
		initString += "]";
		// step 2
		// load all the exprs and remember to check them
		mainLog.println("Loading properties ");
		PropertiesFile propertiesFile = prism.parsePropertiesFile(modulesFile, new File(propertiesFileName));
		List<Expression> processedExprs = new ArrayList<Expression>();
		int safetydaind = -1;
		Expression safetyexpr = null;
		for (int i = 0; i < propertiesFile.getNumProperties(); i++) {
			mainLog.println(propertiesFile.getProperty(i));
			// so reward + safety
			boolean isSafeExpr = false;
			Expression exprHere = propertiesFile.getProperty(i);

			Expression daExpr = ((ExpressionQuant) exprHere).getExpression();
			isSafeExpr = !Expression.isCoSafeLTLSyntactic(daExpr, true);
			if (isSafeExpr) {
				if (safetyexpr != null) {
					// two safety exprs? lets and this stuff
					// TODO: could this cause problems ? //like if one was min and max since we're
					// ignoring those
					safetyexpr = Expression.And(safetyexpr, daExpr);
				} else
					safetyexpr = daExpr;

			}

			if (!isSafeExpr)
				processedExprs.add(daExpr);
		}

		// we've got the safety stuff left
		// we need to not it
		if (safetyexpr != null) {
			Expression notsafetyexpr = Expression.Not(safetyexpr);
			safetydaind = processedExprs.size();
			processedExprs.add(notsafetyexpr);
		}
		mainLog.println(initString);
		mainLog.println("Properties " + processedExprs.toString());
//		System.in.read();
		// hmmmm so this is important I guess
		// and we have a single safety da okay
		// oooo reward structures we don't have to care about
		// right o -lets do this
		// for the honor of greyskull

		LTLModelChecker ltlMC = new LTLModelChecker(prism);

		ArrayList<List<Expression>> labelExprsList = new ArrayList<List<Expression>>();
		ArrayList<DA<BitSet, ? extends AcceptanceOmega>> das = new ArrayList<DA<BitSet, ? extends AcceptanceOmega>>();
		for (int i = 0; i < processedExprs.size(); i++) {
			List<Expression> labelExprs = new ArrayList<Expression>();

			Expression expr = (Expression) processedExprs.get(i);
			// this will need to be changed if you've got different variables accross models
			// then its better to do the v=5 stuff in the prop files and just ignore labels

			expr = (Expression) expr.expandPropRefsAndLabels(propertiesFile, modulesFile.getLabelList());
			DA<BitSet, ? extends AcceptanceOmega> da = ltlMC.constructExpressionDAForLTLFormula(expr, labelExprs,
					allowedAcceptance);
			da.setDistancesToAcc();

			labelExprsList.add(labelExprs);
			das.add(da);
			mainLog.println("Created DA for " + expr.toString());
		}

		ArrayList<String> sharedStateVars = new ArrayList<String>();
		if (hasSharedState)
			sharedStateVars.add("door0");

		MultiAgentNestedProductModelGenerator mapmg = new MultiAgentNestedProductModelGenerator(mfmodgens, das,
				labelExprsList, safetydaind, sharedStateVars);

		return mapmg;
	}

	//TODO: edit here 
	public ArrayList<HashMap<Objectives, HashMap<State, Double>>> solveMaxTaskForAllSingleAgents(Prism prism,
			PrismLog mainLog, String resultsLocation, ArrayList<String> fns, String propFilename) throws Exception {
		SingleAgentSolverMaxExpTask sas = new SingleAgentSolverMaxExpTask(prism, mainLog, resultsLocation);
		// so now we can read in the model
		ArrayList<HashMap<Objectives, HashMap<State, Double>>> results = new ArrayList<>();
		for (String filename : fns) {
			String[] nameval = filename.split("/");
			sas.setName(nameval[nameval.length - 1].replaceAll(".prism", ""));
			sas.loadModel(filename);
			sas.loadProperties(propFilename);
			//so we need to edit this bit 
			//so we need a new function with the strategy 
			
			HashMap<Objectives, HashMap<State, Double>> solution = sas.getSolution();
			results.add(solution);
		}
		return results;
	}

	




	THTSRunInfo unavoidableSingleAgentSolH(boolean debug,int run,int config) throws Exception {

		double[] hvals = { 50 };
		int[] rollouts = { 1000 };
		int[] trialLens = { -1}; // -1 to say we don't want to restrict trials 
		double hval;
		THTSRunInfo rinfo = null;
		boolean[] goalack = new boolean[2];
		int hvalnum = 0;
		hval = hvals[hvalnum];
		int maxRollouts = rollouts[hvalnum];
		boolean hasSharedState = true;
		int trialLen = trialLens[hvalnum];

		float epsilon = 0.0001f;

		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/towardsmcts/";
		// making sure resultsloc exits
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);

		String example = "tro_example_new_small";
		ArrayList<Objectives> tieBreakingOrder = new ArrayList<Objectives>();
		tieBreakingOrder.add(Objectives.TaskCompletion);
		tieBreakingOrder.add(Objectives.Cost);
		String tieBreakingOrderStr = "_obj_";
		for (Objectives obj : tieBreakingOrder) {
			tieBreakingOrderStr += obj.toString() + "_";
		}

		PrismLog mainLog;
		if (debug)
			mainLog = new PrismFileLog("stdout");
		else
			mainLog = new PrismDevNullLog();

		Prism prism = new Prism(mainLog);
		String combString = "_r"+run+"_config"+config;
		String algoIden =  combString;
		PrismLog fileLog = new PrismFileLog(resultsLocation + "log_" + example + algoIden + "_justmdp" + ".txt");//

		prism.initialise();
		prism.setEngine(Prism.EXPLICIT);

		mainLog.println("Initialised Prism");

		int numModels = 2;
		ArrayList<String> filenames = new ArrayList<>();

		for (int numModel = 0; numModel < numModels; numModel++) {
			String modelFileName = testsLocation + example + numModel + ".prism";
			filenames.add(modelFileName);
		}

		String propertiesFileName = testsLocation + example + "_mult.prop";

		mainLog.println("Generating Single Agent Solutions using Nested Products and NVI");
		fileLog.println("Generating Single Agent Solutions using Nested Products and NVI");

		ArrayList<HashMap<Objectives, HashMap<State, Double>>> singleAgentSolutions = solveMaxTaskForAllSingleAgents(
				prism, mainLog, resultsLocation, filenames, propertiesFileName);

		MultiAgentNestedProductModelGenerator maModelGen = createNestedMultiAgentModelGen(prism, mainLog, filenames,
				propertiesFileName, resultsLocation, hasSharedState);

		HashMap<Objectives, Entry<Double, Double>> minMaxVals = new HashMap<>();
		minMaxVals.put(Objectives.Cost, new AbstractMap.SimpleEntry<Double, Double>(0., hval));
		minMaxVals.put(Objectives.TaskCompletion,
				new AbstractMap.SimpleEntry<Double, Double>(0., (double) maModelGen.numDAs));

		boolean useSASH = false; 
//		if(config >2)
//			useSASH = true; 
		Heuristic heuristicFunction = new MultiAgentHeuristicTC(maModelGen,
				singleAgentSolutions, minMaxVals, useSASH);


		mainLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
		fileLog.println("Tie Breaking Order " + tieBreakingOrder.toString());

		mainLog.println("Initialising Greedy Bounds Difference Action Selector Function");
		fileLog.println("Initialising Greedy Bounds Difference Action Selector Function");

		//TODO:change this // to a new action selection =D 
		ActionSelector actionSelection
		= new ActionSelectorGreedySimpleUpperLowerBound(tieBreakingOrder);
		
		mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
		fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

		OutcomeSelector outcomeSelection = new OutcomeSelectorProb();

		mainLog.println("Initialising Full Bellman Backup Function");
		fileLog.println("Initialising Full Bellman Backup Function");

		BackupNVI backupFunction = new BackupLabelledFullBelmanCap(tieBreakingOrder, actionSelection, epsilon,
				minMaxVals);

		mainLog.println("Initialising Reward Helper Function");
		fileLog.println("Initialising Reward Helper Function");

		RewardHelper rewardH = new RewardHelperMultiAgent(maModelGen, RewardCalculation.SUM);


		mainLog.println("Max Rollouts: " + maxRollouts);
		mainLog.println("Max TrialLen: " + trialLen);
		fileLog.println("Max Rollouts: " + maxRollouts);
		fileLog.println("Max TrialLen: " + trialLen);

		mainLog.println("\nInitialising THTS");
		fileLog.println("\nInitialising THTS");
		boolean doForwardBackup = true;
		
		TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) maModelGen, maxRollouts, trialLen,
				heuristicFunction, actionSelection, outcomeSelection, rewardH, backupFunction, doForwardBackup,
				tieBreakingOrder, mainLog, fileLog);

		mainLog.println("\nBeginning THTS");
		fileLog.println("\nBeginning THTS");
		thts.setName(example + algoIden);
		thts.setResultsLocation(resultsLocation);

		int numRolloutsTillSolved = thts.run(false, 0, true);

		mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
		fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
		if (actionSelection instanceof ActionSelectorGreedyTieBreakRandomLowerBound)
			actionSelection = new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder);

		rinfo = new THTSRunInfo();

		goalack = thts.runThroughMostProb(actionSelection, resultsLocation);
		rinfo.goalOnProbablePath = goalack[0];
		goalack = thts.runThrough(actionSelection, resultsLocation);

		mainLog.close();
		fileLog.close();

		rinfo.numRolloutsTillSolved = numRolloutsTillSolved;
		rinfo.goalFound = goalack[0];
		rinfo.initialStateSolved = goalack[1];
		rinfo.initialStateValues = thts.getInitialStateBounds();
		return rinfo;

	}

	
}
