package thts.Scratch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.AbstractMap;
import java.util.ArrayList;
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
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import prism.DefaultModelGenerator;
import prism.Prism;
import prism.PrismDevNullLog;
import prism.PrismException;
import prism.PrismFileLog;
import prism.PrismLog;
import simulator.ModulesFileModelGenerator;
import thts.old.Objectives;
import thts.actionselector.ActionSelector;
import thts.actionselector.ActionSelectorGreedySimpleLowerBound;
import thts.actionselector.ActionSelectorGreedySimpleUpperLowerBound;
import thts.actionselector.ActionSelectorSoftmax;
import thts.backup.BackupLabelledFullBelmanCap;
import thts.backup.BackupNVI;
import thts.heuristic.Heuristic;
import thts.heuristic.MultiAgentHeuristicTC;
import thts.modelgens.MultiAgentNestedProductModelGenerator;
import thts.OutcomeSelector.OutcomeSelector;
import thts.OutcomeSelector.OutcomeSelectorProb;
import thts.OutcomeSelector.OutcomeSelectorProbSkipSolved;
import thts.RewardHelper.RewardCalculation;
import thts.RewardHelper.RewardHelper;
import thts.RewardHelper.RewardHelperMultiAgent;
import thts.TreeSearch.THTSRunInfo;
import thts.TreeSearch.TrialBasedTreeSearch;
import thts.VI.SingleAgentSolverMaxExpTask;

public class TestLRTDPNestedMaSAS_again {

	// running this from the commandline
	// PRISM_MAINCLASS=thtsNew.Scratch.TestLRTDPNestedMaSAS_again prism/bin/prism
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			TestLRTDPNestedMaSAS_again tester = new TestLRTDPNestedMaSAS_again();
			String[] options = { "sas", "sasgreedy" };
			String resString = "";
			String resLine;
			String currentDir = System.getProperty("user.dir");
			String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
			String resultsLocation = testsLocation + "results/csvs/";

			FileWriter fw = new FileWriter(resultsLocation + "unavoidable_allconfigs.txt", true);
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

	public void runMultipleRunsWarehouseSAS(int maxRuns) throws Exception {

//		String[] options = { /* "deterministic", "deterministic_avoid", "avoidable", */ "unavoidable" };
		// we time each test
		// we run it for atleast 5 times
		// we check how many times the goal was found
		// and how many times the intial state was solved
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/csvs/";
		// making sure resultsloc exits
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);
		boolean debug = false;

		String fn = "warehouse_fs_sas_.csv";
		if (debug)
			fn = "temp_" + fn;
		long duration, startTime, endTime;
		PrismLog csvRes = new PrismFileLog(resultsLocation + fn);
		String sep = ",";
		String results = "\nName" + sep + "Failstates" + sep + "Run" + sep + "Duration" + sep + "Goal" + sep
				+ "ProbableGoal" + sep + "InitialStateSolved" + sep + "numRollouts" + sep + "TaskCompletion Upper" + sep
				+ "TaskCompletion Lower" + sep + "Cost Upper" + sep + "Cost Lower";

		csvRes.println(results);
		csvRes.close();
		String name = "Warehouse";
		int[] fsNums = { 0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100 };
		int maxTests = fsNums.length * maxRuns;
		int testNum = 0;
		long durestimate = 0;

		for (int fsNum : fsNums/* = 10; fsNum<=100; fsNum+=30 */) {
			for (int run = 0; run < maxRuns; run++) {

				String result = "\n";
				result += name + sep + fsNum + sep + run;
				testString(testNum, maxTests, durestimate);
				startTime = System.currentTimeMillis();
				// run things
				THTSRunInfo rinfo = null;
				rinfo = unavoidableWarehousefsSingleAgentSolH(debug, fsNum, 0);
				endTime = System.currentTimeMillis();
				duration = endTime - startTime;
				durestimate += duration;
				if (rinfo != null) {
					result += sep + duration + sep + rinfo.goalFound + sep + rinfo.goalOnProbablePath + sep
							+ rinfo.initialStateSolved + sep + rinfo.numRolloutsTillSolved + sep
							+ rinfo.getBoundsString(Objectives.TaskCompletion, sep) + sep
							+ rinfo.getBoundsString(Objectives.Cost, sep);
					csvRes = new PrismFileLog(resultsLocation + fn, true);
					csvRes.print(result);
					csvRes.close();
				}
				results += result;
				testNum++;

			}

		}
		System.out.println(results);

//		csvRes.println(results);
//		csvRes.close();
	}

	public void runMultipleRunsWarehouseSASGreedy(int maxRuns) throws Exception {

//		String[] options = { /* "deterministic", "deterministic_avoid", "avoidable", */ "unavoidable" };
		// we time each test
		// we run it for atleast 5 times
		// we check how many times the goal was found
		// and how many times the intial state was solved
		boolean debug = false;

		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/csvs/";
		// making sure resultsloc exits
		if (debug)
			resultsLocation += "debug/";
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);

		long duration, startTime, endTime;
		String fn = "warehouse_fs_sas_greedy.csv";
		if (debug)
			fn = "debug_" + fn;
		PrismLog csvRes = new PrismFileLog(resultsLocation + fn);
		String sep = ",";
		String results = "\nName" + sep + "Failstates" + sep + "Run" + sep + "Duration" + sep + "Goal" + sep
				+ "ProbableGoal" + sep + "InitialStateSolved" + sep + "numRollouts" + sep + "TaskCompletion Upper" + sep
				+ "TaskCompletion Lower" + sep + "Cost Upper" + sep + "Cost Lower";

		csvRes.println(results);
		csvRes.close();
		String name = "Warehouse";
		int[] fsNums = { 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 0 };
		int maxTests = fsNums.length * maxRuns;
		int testNum = 0;
		long durestimate = 0;
		for (int fsNum : fsNums/* = 10; fsNum<=100; fsNum+=30 */) {
			for (int run = 0; run < maxRuns; run++) {

				String result = "\n";
				result += name + sep + fsNum + sep + run;
				testString(testNum, maxTests, durestimate);
				startTime = System.currentTimeMillis();
				// run things
				THTSRunInfo rinfo = null;
				rinfo = unavoidableWarehousefsSingleAgentSolHGreedyActSel(debug, fsNum, 0);
				endTime = System.currentTimeMillis();
				duration = endTime - startTime;
				durestimate += duration;
				if (rinfo != null) {
					result += sep + duration + sep + rinfo.goalFound + sep + rinfo.goalOnProbablePath + sep
							+ rinfo.initialStateSolved + sep + rinfo.numRolloutsTillSolved + sep
							+ rinfo.getBoundsString(Objectives.TaskCompletion, sep) + sep
							+ rinfo.getBoundsString(Objectives.Cost, sep);
					csvRes = new PrismFileLog(resultsLocation + fn, true);
					csvRes.print(result);
					csvRes.close();
				}
				results += result;
				testNum++;

			}

		}
		System.out.println(results);

//		csvRes.println(results);
//		csvRes.close();
	}

	private void testString(int testNum, int maxTests, long durMS) {
		System.out.println("Test " + testNum + "/" + maxTests);
		long times = TimeUnit.SECONDS.convert(durMS, TimeUnit.MILLISECONDS);
		long timeM = TimeUnit.MINUTES.convert(durMS, TimeUnit.MILLISECONDS);
		long timeH = TimeUnit.HOURS.convert(durMS, TimeUnit.MILLISECONDS);
		System.out.println("Time: " + times + "(s)/" + timeM + "(min)/" + timeH + "(h)");

		long avgTime = 0;
		if (testNum > 0)
			avgTime = durMS / (testNum);
		long estTime = avgTime * (maxTests - testNum);

		long etimes = TimeUnit.SECONDS.convert(estTime, TimeUnit.MILLISECONDS);
		long etimeM = TimeUnit.MINUTES.convert(estTime, TimeUnit.MILLISECONDS);
		long etimeH = TimeUnit.HOURS.convert(estTime, TimeUnit.MILLISECONDS);
		System.out.println("ET End: " + etimes + "(s)/" + etimeM + "(min)/" + etimeH + "(h)");
	}

	public void createDirIfNotExist(String directoryName) {
		File directory = new File(directoryName);
		if (!directory.exists()) {
//			directory.mkdir();
			// If you require it to make the entire directory path including parents,
			// use directory.mkdirs(); here instead.
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
//			mainLog.println("Writing to " + resultsLocation + "da_" + i + ".dot");
//			PrismLog out = new PrismFileLog(resultsLocation + "da_" + i + ".dot");
//			// printing the da
//			da.print(out, "dot");
//			out.close();
			labelExprsList.add(labelExprs);
			das.add(da);
			mainLog.println("Created DA for " + expr.toString());
		}

		ArrayList<String> sharedStateVars = new ArrayList<String>();
		if (hasSharedState)
			sharedStateVars.add("door0");
//		sharedStateVars = null;
		MultiAgentNestedProductModelGenerator mapmg = new MultiAgentNestedProductModelGenerator(mfmodgens, das,
				labelExprsList, safetydaind, sharedStateVars);

		return mapmg;
	}

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
			HashMap<Objectives, HashMap<State, Double>> solution = sas.getStateValues();
			results.add(solution);
		}
		return results;
	}

	THTSRunInfo unavoidableWarehousefsSingleAgentSolH(boolean debug, int fsNum, int rNum) throws Exception {
		boolean goalFound = false;
		double[] hvals = { 1000 };
		int[] rollouts = { 10000 };
		int[] trialLens = { 50 };
		double hval = 20;
		boolean[] goalack = new boolean[2];
		int numModels = 4;
		boolean hasSharedState = false;

		int hvalnum = 0;
		hval = hvals[hvalnum];
		int maxRollouts = rollouts[hvalnum];
		int trialLen = trialLens[hvalnum];

		int exampleNum = fsNum / 10;
		String[] examples = { "depotShelf_r10_g10_fs0_fsp_0.0_0_", "depotShelf_r10_g10_fs13_fsp_11.0_0_",
				"depotShelf_r10_g10_fs25_fsp_20.0_0_", "shelfDepot_r10_g10_fs37_fsp_30.0_6_",
				"shelfDepot_r10_g10_fs50_fsp_41.0_8_", "depotShelf_r10_g10_fs62_fsp_50.0_0_",
				"shelfDepot_r10_g10_fs74_fsp_60.0_7_", "shelfDepot_r10_g10_fs87_fsp_71.0_0_",
				"shelfDepot_r10_g10_fs100_fsp_81.0_7_", "shelfDepot_r10_g10_fs111_fsp_90.0_8_",
				"depotShelf_r10_g10_fs123_fsp_100_9_" };

		float epsilon = 0.0001f;

		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String basetestsLocation = currentDir + "/tests/wkspace/warehouse_samples/";
		String testsLocation = basetestsLocation + fsNum + "/";
		String resultsLocation = basetestsLocation + "results/sasmanp/sas/" + fsNum + "/";
		if (debug)
			resultsLocation += "debug/";
		// making sure resultsloc exits
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);

		String example = examples[exampleNum];
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
		String combString = "_multi_sash_" + tieBreakingOrderStr + "_costcutoff_" + hval + "_trialLen_" + trialLen
				+ "_rollouts_" + maxRollouts + "_out_skipsolved";
		String algoIden = "_avoid_lrtdp" + combString + "_r" + numModels + "_run" + rNum;
		PrismLog fileLog = new PrismFileLog(resultsLocation + "log_" + example + algoIden + "_justmdp" + ".txt");//

		prism.initialise();
		prism.setEngine(Prism.EXPLICIT);

		mainLog.println("Initialised Prism");

		ArrayList<String> filenames = new ArrayList<>();

		for (int numModel = 0; numModel < numModels; numModel++) {
			String modelFileName = testsLocation + example + numModel + ".prism";
			filenames.add(modelFileName);
		}

		String propertiesFileName = testsLocation + example + "mult.prop";

		mainLog.println("Generating Single Agent Solutions using Nested Products and NVI");
		fileLog.println("Generating Single Agent Solutions using Nested Products and NVI");

		ArrayList<HashMap<Objectives, HashMap<State, Double>>> singleAgentSolutions = solveMaxTaskForAllSingleAgents(
				prism, mainLog, resultsLocation, filenames, propertiesFileName);

		MultiAgentNestedProductModelGenerator maModelGen = createNestedMultiAgentModelGen(prism, mainLog, filenames,
				propertiesFileName, resultsLocation, hasSharedState);

		boolean tightBounds = true;
		HashMap<Objectives, Entry<Double, Double>> minMaxVals = new HashMap<>();
		minMaxVals.put(Objectives.Cost, new AbstractMap.SimpleEntry<Double, Double>(0., hval));
		minMaxVals.put(Objectives.TaskCompletion,
				new AbstractMap.SimpleEntry<Double, Double>(0., (double) maModelGen.numDAs));
		Heuristic heuristicFunction = new MultiAgentHeuristicTC(maModelGen, singleAgentSolutions, minMaxVals,
				tightBounds);
		// EmptyNestedMultiAgentHeuristicTCTC(maModelGen, gs, null, hval);

		mainLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
		fileLog.println("Tie Breaking Order " + tieBreakingOrder.toString());

		double epsilonActSel = 0.8;
		ActionSelector baseActSel = new ActionSelectorGreedySimpleUpperLowerBound(tieBreakingOrder,true);// new

		mainLog.println("Initialising ActionSelectorGreedyTieBreakRandomSoftmaxLowerBound epsilon=" + epsilonActSel);
		fileLog.println("Initialising ActionSelectorGreedyTieBreakRandomSoftmaxLowerBound epsilon=" + epsilonActSel);
		ActionSelector actionSelection = new ActionSelectorSoftmax(baseActSel, epsilonActSel);
//				new ActionSelectorGreedyTieBreakRandomSoftmaxLowerBound(tieBreakingOrder,epsilonActSel);
		// new ActionSelectorGreedySimpleUpperLowerBound(tieBreakingOrder,true);// new
		// ActionSelectorGreedyBoundsDiff(tieBreakingOrder);

		mainLog.println("Initialising OutcomeSelectorProbSkipSolved Function");
		fileLog.println("Initialising OutcomeSelectorProbSkipSolved Function");

		OutcomeSelector outcomeSelection = new OutcomeSelectorProbSkipSolved();// new
																				// OutcomeSelectorProb();

		mainLog.println("Initialising BackupLabelledFullBelmanCap Function");
		fileLog.println("Initialising BackupLabelledFullBelmanCap Function");

		mainLog.println("Caps: " + minMaxVals.toString());
		fileLog.println("Caps: " + minMaxVals.toString());
		BackupNVI backupFunction = new BackupLabelledFullBelmanCap(tieBreakingOrder,
//				new ActionSelectorGreedySimpleUpperLowerBound(tieBreakingOrder), 
				baseActSel, epsilon, minMaxVals,false);

		mainLog.println("Initialising Reward Helper Function");
		fileLog.println("Initialising Reward Helper Function");

		RewardHelper rewardH = new RewardHelperMultiAgent(maModelGen, RewardCalculation.SUM);
//							new RewardHelperNestedSingleAgent(saModelGen);

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
		THTSRunInfo rinfo = new THTSRunInfo();
		int numRolloutsTillSolved = thts.run(false, 0, debug);

		mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
		fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
//		goalack = thtsNew.thts.runThroughMostProb(
////				new ActionSelectorGreedySimpleUpperLowerBound(tieBreakingOrder),
//				baseActSel, resultsLocation);
		rinfo.goalOnProbablePath = goalack[0];
//		goalack = thtsNew.thts.runThrough(
////				new ActionSelectorGreedySimpleUpperLowerBound(tieBreakingOrder),
//				baseActSel, resultsLocation);

		mainLog.close();
		fileLog.close();

		rinfo.initialStateValues = thts.getInitialStateBounds();

		rinfo.numRolloutsTillSolved = numRolloutsTillSolved;
		rinfo.goalFound = goalack[0];
		rinfo.initialStateSolved = goalack[1];
		return rinfo;

	}

	THTSRunInfo unavoidableWarehousefsSingleAgentSolHGreedyActSel(boolean debug, int fsNum, int rNum) throws Exception {
		boolean goalFound = false;
		double[] hvals = { 1000 };
		int[] rollouts = { 10000 };
		int[] trialLens = { 50 };
		double hval = 20;
		boolean[] goalack = new boolean[2];
		int numModels = 4;
		boolean hasSharedState = false;

		int hvalnum = 0;
		hval = hvals[hvalnum];
		int maxRollouts = rollouts[hvalnum];
		int trialLen = trialLens[hvalnum];

		int exampleNum = fsNum / 10;
		String[] examples = { "depotShelf_r10_g10_fs0_fsp_0.0_0_", "depotShelf_r10_g10_fs13_fsp_11.0_0_",
				"depotShelf_r10_g10_fs25_fsp_20.0_0_", "shelfDepot_r10_g10_fs37_fsp_30.0_6_",
				"shelfDepot_r10_g10_fs50_fsp_41.0_8_", "depotShelf_r10_g10_fs62_fsp_50.0_0_",
				"shelfDepot_r10_g10_fs74_fsp_60.0_7_", "shelfDepot_r10_g10_fs87_fsp_71.0_0_",
				"shelfDepot_r10_g10_fs100_fsp_81.0_7_", "shelfDepot_r10_g10_fs111_fsp_90.0_8_",
				"depotShelf_r10_g10_fs123_fsp_100_9_" };

		float epsilon = 0.0001f;

		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String basetestsLocation = currentDir + "/tests/wkspace/warehouse_samples/";
		String testsLocation = basetestsLocation + fsNum + "/";
		String resultsLocation = basetestsLocation + "results/sasmanp/sas/" + fsNum + "/greedy/";
		// making sure resultsloc exits
		if (debug)
			resultsLocation += "debug/";
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);

		String example = examples[exampleNum];
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

		PrismLog backupFunctionLog = null;

		Prism prism = new Prism(mainLog);
		String combString = "_multi_sash_" + tieBreakingOrderStr + "_costcutoff_" + hval + "_trialLen_" + trialLen
				+ "_rollouts_" + maxRollouts + "_out_skipsolved";
		String algoIden = "_avoid_lrtdp" + combString + "_r" + numModels + "_run" + rNum;
		PrismLog fileLog = new PrismFileLog(resultsLocation + "log_" + example + algoIden + "_justmdp" + ".txt");//

		if (debug)
			backupFunctionLog = new PrismFileLog(resultsLocation + "log_" + example + algoIden + "_backups" + ".txt");

		prism.initialise();
		prism.setEngine(Prism.EXPLICIT);

		mainLog.println("Initialised Prism");

		ArrayList<String> filenames = new ArrayList<>();

		for (int numModel = 0; numModel < numModels; numModel++) {
			String modelFileName = testsLocation + example + numModel + ".prism";
			filenames.add(modelFileName);
		}

		String propertiesFileName = testsLocation + example + "mult.prop";

		mainLog.println("Generating Single Agent Solutions using Nested Products and NVI");
		fileLog.println("Generating Single Agent Solutions using Nested Products and NVI");

		ArrayList<HashMap<Objectives, HashMap<State, Double>>> singleAgentSolutions = solveMaxTaskForAllSingleAgents(
				prism, mainLog, resultsLocation, filenames, propertiesFileName);

		MultiAgentNestedProductModelGenerator maModelGen = createNestedMultiAgentModelGen(prism, mainLog, filenames,
				propertiesFileName, resultsLocation, hasSharedState);

		boolean tightBounds = true;
		HashMap<Objectives, Entry<Double, Double>> minMaxVals = new HashMap<>();
		minMaxVals.put(Objectives.Cost, new AbstractMap.SimpleEntry<Double, Double>(0., hval));
		minMaxVals.put(Objectives.TaskCompletion,
				new AbstractMap.SimpleEntry<Double, Double>(0., (double) maModelGen.numDAs));
		Heuristic heuristicFunction = new MultiAgentHeuristicTC(maModelGen, singleAgentSolutions, minMaxVals,
				tightBounds);
		// EmptyNestedMultiAgentHeuristicTC(maModelGen, gs, null, hval);

		mainLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
		fileLog.println("Tie Breaking Order " + tieBreakingOrder.toString());

		mainLog.println("Initialising ActionSelectorGreedyTieBreakRandomSoftmaxLowerBound Action Selector Function");
		fileLog.println("Initialising ActionSelectorGreedyTieBreakRandomSoftmaxLowerBound Action Selector Function");

		ActionSelector actionSelection = new ActionSelectorGreedySimpleUpperLowerBound(tieBreakingOrder,true);// new
		// ActionSelectorGreedyBoundsDiff(tieBreakingOrder);

		mainLog.println("Initialising OutcomeSelectorProbSkipSolved Function");
		fileLog.println("Initialising OutcomeSelectorProbSkipSolved Function");

		OutcomeSelector outcomeSelection = new OutcomeSelectorProbSkipSolved();// OutcomeSelectorProbSkipSolved();// new
																				// OutcomeSelectorProb();

		mainLog.println("Initialising BackupLabelledFullBelmanCap Function");
		fileLog.println("Initialising BackupLabelledFullBelmanCap Function");

		mainLog.println("Caps: " + minMaxVals.toString());
		fileLog.println("Caps: " + minMaxVals.toString());
		BackupNVI backupFunction = new BackupLabelledFullBelmanCap(tieBreakingOrder,
//				new ActionSelectorGreedySimpleUpperLowerBound(tieBreakingOrder), 
				actionSelection, epsilon, minMaxVals, backupFunctionLog,false);

		mainLog.println("Initialising Reward Helper Function");
		fileLog.println("Initialising Reward Helper Function");

		RewardHelper rewardH = new RewardHelperMultiAgent(maModelGen, RewardCalculation.SUM);
//							new RewardHelperNestedSingleAgent(saModelGen);

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
		THTSRunInfo rinfo = new THTSRunInfo();
		int numRolloutsTillSolved = thts.run(false, 0, debug);

		mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
		fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
//		goalack = thtsNew.thts.runThroughMostProb(
////				new ActionSelectorGreedySimpleUpperLowerBound(tieBreakingOrder),
//				actionSelection, resultsLocation);
		rinfo.goalOnProbablePath = goalack[0];
//		goalack = thtsNew.thts.runThrough(
////				new ActionSelectorGreedySimpleUpperLowerBound(tieBreakingOrder),
//				actionSelection, resultsLocation);

		mainLog.close();
		fileLog.close();

		rinfo.initialStateValues = thts.getInitialStateBounds();

		rinfo.numRolloutsTillSolved = numRolloutsTillSolved;
		rinfo.goalFound = goalack[0];
		rinfo.initialStateSolved = goalack[1];
		return rinfo;

	}

	THTSRunInfo avoidableSingleAgentSolH(boolean debug) throws Exception {
		boolean goalFound = false;
		double[] hvals = { 50 };
		int[] rollouts = { 100 };
		int[] trialLens = { 50 };

		int hvalnum = 0;
		THTSRunInfo rinfo = null;
		double hval = hvals[hvalnum];
		int maxRollouts = rollouts[hvalnum];
		boolean hasSharedState = false;
		int trialLen = trialLens[hvalnum];

		float epsilon = 0.0001f;

		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/sasmanp/";
		// making sure resultsloc exits
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);

		String example = "tro_example_new_small_onefailaction";
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
		String combString = "_multi_sash_" + tieBreakingOrderStr + "_costcutoff_" + hval + "_trialLen_" + trialLen
				+ "_rollouts_" + maxRollouts;
		String algoIden = "_avoid_lrtdp" + combString;
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

		Heuristic heuristicFunction = new MultiAgentHeuristicTC(maModelGen, singleAgentSolutions, minMaxVals, true);
		// EmptyNestedMultiAgentHeuristicTC(maModelGen, gs, null, hval);

		mainLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
		fileLog.println("Tie Breaking Order " + tieBreakingOrder.toString());

		mainLog.println("Initialising Greedy Bounds Difference Action Selector Function");
		fileLog.println("Initialising Greedy Bounds Difference Action Selector Function");

		ActionSelector actionSelection = new ActionSelectorGreedySimpleUpperLowerBound(tieBreakingOrder,true);// new
		// ActionSelectorGreedyBoundsDiff(tieBreakingOrder);

		mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
		fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

		OutcomeSelector outcomeSelection = new OutcomeSelectorProb();

		mainLog.println("Initialising Full Bellman Backup Function");
		fileLog.println("Initialising Full Bellman Backup Function");

		BackupNVI backupFunction = new BackupLabelledFullBelmanCap(tieBreakingOrder, actionSelection, epsilon,
				minMaxVals,false);

		mainLog.println("Initialising Reward Helper Function");
		fileLog.println("Initialising Reward Helper Function");

		RewardHelper rewardH = new RewardHelperMultiAgent(maModelGen, RewardCalculation.SUM);
//							new RewardHelperNestedSingleAgent(saModelGen);

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

		int numRolloutsTillSolved = thts.run(false);

		mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
		fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
//		thtsNew.thts.runThroughMostProb(new ActionSelectorGreedySimpleUpperLowerBound(tieBreakingOrder,true), resultsLocation);
//		boolean[] goalack = thtsNew.thts.runThrough(new ActionSelectorGreedySimpleUpperLowerBound(tieBreakingOrder,true),
//				resultsLocation);

		mainLog.close();
		fileLog.close();

		rinfo = new THTSRunInfo();
		rinfo.numRolloutsTillSolved = numRolloutsTillSolved;
//		rinfo.goalFound = goalack[0];
//		rinfo.initialStateSolved = goalack[1];
		return rinfo;

//					thtsNew.thts.run(false);
//
//					mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
//					fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
//
//					goalack = thtsNew.thts.runThrough(new ActionSelectorGreedySimpleUpperLowerBound(tieBreakingOrder),
//							resultsLocation);
//					goalFound = goalack[0];
//					fileLog.println("Goal Found: " + goalack[0]);
//					fileLog.println("Initial State Solved: " + goalack[1]);
//
//					mainLog.println("Goal Found: " + goalack[0]);
//					mainLog.println("Initial State Solved: " + goalack[1]);
////					System.in.read();
//
//					mainLog.close();
//					fileLog.close();
//
//				}
//				if (goalFound)
//					break;
//			}
//			if (goalFound)
//				break;
//		}
//		return goalack;
	}

	THTSRunInfo noprobabilitiesSingleAgentSolH(boolean debug) throws Exception {
		boolean goalFound = false;
		double[] hvals = { 50 };
		int[] rollouts = { 100 };
		int[] trialLens = { 50 };
		double hval = 20;
		boolean[] goalack = new boolean[2];
		int hvalnum = 0;
		THTSRunInfo rinfo = null;
		hval = hvals[hvalnum];
		int maxRollouts = rollouts[hvalnum];
		boolean hasSharedState = false;
		int trialLen = trialLens[hvalnum];

		float epsilon = 0.0001f;

		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/sasmanp/";
		// making sure resultsloc exits
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);

		String example = "tro_example_new_small_noprob";
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
		String combString = "_multi_sash_" + tieBreakingOrderStr + "_costcutoff_" + hval + "_trialLen_" + trialLen
				+ "_rollouts_" + maxRollouts;
		String algoIden = "_avoid_lrtdp" + combString;
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

		Heuristic heuristicFunction = new MultiAgentHeuristicTC(maModelGen, singleAgentSolutions, minMaxVals, true);
		// EmptyNestedMultiAgentHeuristicTC(maModelGen, gs, null, hval);

		mainLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
		fileLog.println("Tie Breaking Order " + tieBreakingOrder.toString());

		mainLog.println("Initialising Greedy Bounds Difference Action Selector Function");
		fileLog.println("Initialising Greedy Bounds Difference Action Selector Function");

		ActionSelector actionSelection = new ActionSelectorGreedySimpleUpperLowerBound(tieBreakingOrder,true);// new
		// ActionSelectorGreedyBoundsDiff(tieBreakingOrder);

		mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
		fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

		OutcomeSelector outcomeSelection = new OutcomeSelectorProb();

		mainLog.println("Initialising Full Bellman Backup Function");
		fileLog.println("Initialising Full Bellman Backup Function");

		BackupNVI backupFunction = new BackupLabelledFullBelmanCap(tieBreakingOrder, actionSelection, epsilon,
				minMaxVals,false);

		mainLog.println("Initialising Reward Helper Function");
		fileLog.println("Initialising Reward Helper Function");

		RewardHelper rewardH = new RewardHelperMultiAgent(maModelGen, RewardCalculation.SUM);
//							new RewardHelperNestedSingleAgent(saModelGen);

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

		int numRolloutsTillSolved = thts.run(false);

		mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
		fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
//		thtsNew.thts.runThroughMostProb(new ActionSelectorGreedySimpleUpperLowerBound(tieBreakingOrder,true), resultsLocation);
//		goalack = thtsNew.thts.runThrough(new ActionSelectorGreedySimpleUpperLowerBound(tieBreakingOrder,true), resultsLocation);

		mainLog.close();
		fileLog.close();

		rinfo = new THTSRunInfo();
		rinfo.numRolloutsTillSolved = numRolloutsTillSolved;
		rinfo.goalFound = goalack[0];
		rinfo.initialStateSolved = goalack[1];
		return rinfo;

	}

	THTSRunInfo unavoidableSingleAgentSolH(boolean debug,int run,int config) throws Exception {
		boolean goalFound = false;
		double[] hvals = { 50 };
		int[] rollouts = { 1000 };
		int[] trialLens = { 50 };
		double hval = 20;
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
		String resultsLocation = testsLocation + "results/sasmanp/";
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
		if(config >2)
			useSASH = true; 
		Heuristic heuristicFunction = new MultiAgentHeuristicTC(maModelGen,
				singleAgentSolutions, minMaxVals, useSASH);
		// EmptyNestedMultiAgentHeuristicTCTC(maModelGen, gs, null, hval);

		mainLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
		fileLog.println("Tie Breaking Order " + tieBreakingOrder.toString());

		mainLog.println("Initialising Greedy Bounds Difference Action Selector Function");
		fileLog.println("Initialising Greedy Bounds Difference Action Selector Function");

		ActionSelector actionSelection =
//				new ActionSelectorGreedySimpleUpperLowerBound(tieBreakingOrder,true);
				new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder,true);// new
		
		// ActionSelectorGreedyBoundsDiff(tieBreakingOrder);

		if(config == 2 || config == 4)
			actionSelection = new ActionSelectorGreedySimpleUpperLowerBound(tieBreakingOrder,true);
		
		mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
		fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

		OutcomeSelector outcomeSelection = new OutcomeSelectorProb();

		mainLog.println("Initialising Full Bellman Backup Function");
		fileLog.println("Initialising Full Bellman Backup Function");

		BackupNVI backupFunction = new BackupLabelledFullBelmanCap(tieBreakingOrder, actionSelection, epsilon,
				minMaxVals,false);

		mainLog.println("Initialising Reward Helper Function");
		fileLog.println("Initialising Reward Helper Function");

		RewardHelper rewardH = new RewardHelperMultiAgent(maModelGen, RewardCalculation.SUM);
//							new RewardHelperNestedSingleAgent(saModelGen);

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
		if (actionSelection instanceof ActionSelectorGreedySimpleLowerBound)
			actionSelection = new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder,false);

		rinfo = new THTSRunInfo();

//		goalack = thtsNew.thts.runThroughMostProb(actionSelection, resultsLocation);
//		rinfo.goalOnProbablePath = goalack[0];
//		goalack = thtsNew.thts.runThrough(actionSelection, resultsLocation);

		mainLog.close();
		fileLog.close();

		rinfo.numRolloutsTillSolved = numRolloutsTillSolved;
		rinfo.goalFound = goalack[0];
		rinfo.initialStateSolved = goalack[1];
		rinfo.initialStateValues = thts.getInitialStateBounds();
		return rinfo;

	}

	THTSRunInfo grid5SingleAgentSolH(boolean debug, int fsp) throws Exception {
		boolean goalFound = false;
		double[] hvals = { 1000 };
		int[] rollouts = { 1000 };
		int[] trialLens = { 50 };
		double hval = 20;
		THTSRunInfo rinfo = null;
		boolean[] goalack = new boolean[2];
		int hvalnum = 0;
		hval = hvals[hvalnum];
		int maxRollouts = rollouts[hvalnum];
		boolean hasSharedState = false;
		int trialLen = trialLens[hvalnum];

		float epsilon = 0.0001f;

		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/grid5/" + fsp + "/";
		String resultsLocation = testsLocation + "results/sasmanp/";
		// making sure resultsloc exits
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);

		String[] examples = { "r10_g10_a1_grid_5_fsp_0_0_", "r10_g10_a1_grid_5_fsp_10_1_",
				"r10_g10_a1_grid_5_fsp_20_2_", "r10_g10_a1_grid_5_fsp_30_3_", "r10_g10_a1_grid_5_fsp_40_4_",
				"r10_g10_a1_grid_5_fsp_50_5_", "r10_g10_a1_grid_5_fsp_60_6_", "r10_g10_a1_grid_5_fsp_70_7_",
				"r10_g10_a1_grid_5_fsp_80_8_", "r10_g10_a1_grid_5_fsp_90_9_", "r10_g10_a1_grid_5_fsp_100_0_" };
		String example = examples[fsp / 10];// r10_g10_a1_grid_5_fsp_0_0_9//"tro_example_new_small";
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
		String combString = "_multi_sash_" + tieBreakingOrderStr + "_costcutoff_" + hval + "_trialLen_" + trialLen
				+ "_rollouts_" + maxRollouts;
		String algoIden = "_avoid_lrtdp" + combString;
		PrismLog fileLog = new PrismFileLog(resultsLocation + "log_" + example + algoIden + "_justmdp" + ".txt");//

		prism.initialise();
		prism.setEngine(Prism.EXPLICIT);

		mainLog.println("Initialised Prism");

		int numModels = 3;
		ArrayList<String> filenames = new ArrayList<>();

		for (int numModel = 0; numModel < numModels; numModel++) {
			String modelFileName = testsLocation + example + numModel + ".prism";
			filenames.add(modelFileName);
		}

		String propertiesFileName = testsLocation + example + "mult.prop";

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

		Heuristic heuristicFunction = new MultiAgentHeuristicTC(maModelGen, singleAgentSolutions, minMaxVals, false);
		// EmptyNestedMultiAgentHeuristicTCTC(maModelGen, gs, null, hval);

		mainLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
		fileLog.println("Tie Breaking Order " + tieBreakingOrder.toString());

		mainLog.println("Initialising Greedy Bounds Difference Action Selector Function");
		fileLog.println("Initialising Greedy Bounds Difference Action Selector Function");

		ActionSelector actionSelection =
//				new ActionSelectorGreedySimpleUpperLowerBound(tieBreakingOrder,true);
				new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder,true);// new
																					// ActionSelectorGreedyBoundsDiff(tieBreakingOrder);

		mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
		fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

		OutcomeSelector outcomeSelection = new OutcomeSelectorProb();

		mainLog.println("Initialising Full Bellman Backup Function");
		fileLog.println("Initialising Full Bellman Backup Function");

		BackupNVI backupFunction = new BackupLabelledFullBelmanCap(tieBreakingOrder, actionSelection, epsilon,
				minMaxVals,false);

		mainLog.println("Initialising Reward Helper Function");
		fileLog.println("Initialising Reward Helper Function");

		RewardHelper rewardH = new RewardHelperMultiAgent(maModelGen, RewardCalculation.SUM);
//							new RewardHelperNestedSingleAgent(saModelGen);

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
		if (actionSelection instanceof ActionSelectorGreedySimpleLowerBound)
			actionSelection = new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder,false);
		rinfo = new THTSRunInfo();

//		goalack = thtsNew.thts.runThroughMostProb(actionSelection, resultsLocation);
//		rinfo.goalOnProbablePath = goalack[0];
//		goalack = thtsNew.thts.runThrough(actionSelection, resultsLocation);

		mainLog.close();
		fileLog.close();

		rinfo.numRolloutsTillSolved = numRolloutsTillSolved;
		rinfo.goalFound = goalack[0];
		rinfo.initialStateSolved = goalack[1];
		rinfo.initialStateValues = thts.getInitialStateBounds();
		return rinfo;

	}
}
