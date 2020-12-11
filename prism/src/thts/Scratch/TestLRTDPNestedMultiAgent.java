package thts.Scratch;

import java.io.File;
import java.io.IOException;
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
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import prism.DefaultModelGenerator;
import prism.Prism;
import prism.PrismDevNullLog;
import prism.PrismException;
import prism.PrismFileLog;
import prism.PrismLog;
import simulator.ModulesFileModelGenerator;
import thts.Old.Objectives;
import thts.ActionSelector.ActionSelector;
import thts.ActionSelector.ActionSelectorGreedySimpleLowerBound;
import thts.Backup.BackupLabelledFullBelmanCap;
import thts.Backup.BackupNVI;
import thts.Heuristic.Heuristic;
import thts.ModelGens.MultiAgentNestedProductModelGenerator;
import thts.OutcomeSelector.OutcomeSelector;
import thts.OutcomeSelector.OutcomeSelectorProbSkipSolved;
import thts.OutcomeSelector.OutcomeSelectorRandom;
import thts.OutcomeSelector.OutcomeSelectorRandomSkipSolved;
import thts.RewardHelper.RewardCalculation;
import thts.RewardHelper.RewardHelper;
import thts.RewardHelper.RewardHelperMultiAgent;
import thts.TreeSearch.THTSRunInfo;
import thts.TreeSearch.TrialBasedTreeSearch;

public class TestLRTDPNestedMultiAgent {

	// running this from the commandline
	// PRISM_MAINCLASS=thtsNew.Scratch.TestLRTDPNestedMultiAgent prism/bin/prism
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			TestLRTDPNestedMultiAgent tester = new TestLRTDPNestedMultiAgent();
			String[] options = { "all", "noprobnoavoid", "noprobavoid", "avoidable", "unavoidable", "currentWIP",
					"unavoidable_sas_h", "noprobavoid_sas_h", "unavoidable_warehouse", "multruns" };

			String option = options[4];// "currentWIP";
			int maxRuns = 5;// 10;
			if (args.length > 0) {
				System.out.println(Arrays.deepToString(args));
				option = args[0];
				System.out.println("Running with argument: " + option);
				System.in.read();
			}

			if (option.contentEquals(options[0])) // all
			{

				THTSRunInfo rinfo = null;
				int numGoalFound = 0;
				int numSolved = 0;
				int numTests = 0;
				String labelString = "";
				ArrayList<String> noGoals = new ArrayList<>();
				ArrayList<String> noSolved = new ArrayList<>();

				rinfo = tester.noprobabilities_noavoid(false);
				numTests++;
				labelString = "noprobsnoavoid";
				if (rinfo.goalFound) {
					numGoalFound++;

				} else {
					noGoals.add(labelString);
				}
				if (rinfo.initialStateSolved) {
					numSolved++;
				} else {
					noSolved.add(labelString);
				}

				rinfo = tester.noprobabilities(false);
				numTests++;
				labelString = "noprobs";
				if (rinfo.goalFound) {
					numGoalFound++;

				} else {
					noGoals.add(labelString);
				}
				if (rinfo.initialStateSolved) {
					numSolved++;
				} else {
					noSolved.add(labelString);
				}

				rinfo = tester.avoidable(false);
				numTests++;
				labelString = "avoidable";
				if (rinfo.goalFound) {
					numGoalFound++;

				} else {
					noGoals.add(labelString);
				}
				if (rinfo.initialStateSolved) {
					numSolved++;
				} else {
					noSolved.add(labelString);
				}

				rinfo = tester.unavoidable(false, 0);
				numTests++;
				labelString = "unavoidable";
				if (rinfo.goalFound) {
					numGoalFound++;

				} else {
					noGoals.add(labelString);
				}
				if (rinfo.initialStateSolved) {
					numSolved++;
				} else {
					noSolved.add(labelString);
				}

				System.out.println(
						"NumTests: " + numTests + "\nNumSolved:" + numSolved + "\nNumGoalFound:" + numGoalFound);
				System.out.println("No Goals:");
				System.out.println(noGoals.toString());
				System.out.println("Not Solved:");
				System.out.println(noSolved.toString());

			} else if (option.contentEquals(options[1])) // noprob
			{

				tester.noprobabilities_noavoid(false);

			} else if (option.contentEquals(options[2])) // avoidable
			{

				tester.noprobabilities(false);

			} else if (option.contentEquals(options[3])) // unavoidable
			{

				tester.avoidable(false);

			} else if (option.contentEquals(options[4])) // unavoidable
			{

//				tester.runMultipleRunsUnavoidable(10);
//				tester.unavoidable(false);
//				tester.runMultipleRunsWarehous(maxRuns, 0);
//				tester.runMultipleRunsWarehous(maxRuns, 10);
//				tester.runMultipleRunsWarehous(maxRuns, 20);
//				tester.runMultipleRunsWarehous(maxRuns, 90);
				tester.runMultipleRunsWarehous(maxRuns);
//				tester.runMultipleRunsWarehouseSAS(maxRuns);

			} else {
				System.out
						.println("Unimplemented option " + option + "\nAvailable options " + Arrays.toString(options));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

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

	public void runMultipleRunsWarehous(int maxRuns) throws Exception {

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
		String fn = "warehouse_fs_.csv";
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
		int testNum = 0;
		long durestimate = 0;

		String name = "Warehouse";
		int[] fsNums = { 0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100 };
		int maxTests = fsNums.length*maxRuns; 
		for (int fsNum : fsNums/* = 0; fsNum<=100; fsNum+=30 */) {
			for (int run = 0; run < maxRuns; run++) {
				String result = "\n";
				result += name + sep + fsNum + sep + run;
				testString(testNum,maxTests,durestimate);
				startTime = System.currentTimeMillis();
				// run things
				THTSRunInfo rinfo = null;
				rinfo = unavoidableWarehousefs(debug, fsNum, 0);
				endTime = System.currentTimeMillis();
				duration = endTime - startTime;
				durestimate+=duration;
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

	public void runMultipleRunsUnavoidable(int maxRuns) throws Exception {

		String[] options = { /* "deterministic", "deterministic_avoid", "avoidable", */ "unavoidable" };
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

		long duration, startTime, endTime;

		String sep = ",";
		String results = "\nName" + sep + "Goal" + sep + "Run" + sep + "Duration" + sep + "GoalFound" + sep
				+ "InitialStateSolved" + sep + "numRollouts";

		for (String name : options) {
			String option = name;
			for (int run = 0; run < maxRuns; run++) {
				String result = "\n";
				result += name + sep + "ltlspec" + sep + run;
				startTime = System.currentTimeMillis();
				// run things
				THTSRunInfo rinfo = null;

				rinfo = unavoidable(debug, run);

				endTime = System.currentTimeMillis();
				duration = endTime - startTime;
				if (rinfo != null)
					result += sep + duration + sep + rinfo.goalFound + sep + rinfo.initialStateSolved + sep
							+ rinfo.numRolloutsTillSolved;
				results += result;
//System.in.read();

			}
		}
		System.out.println(results);
		PrismLog csvRes = new PrismFileLog(resultsLocation + "MultiAgentLRTDP_unavoidable_ltlspecs.csv");
		csvRes.println(results);
		csvRes.close();
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
			mainLog.println("Writing to " + resultsLocation + "da_" + i + ".dot");
			PrismLog out = new PrismFileLog(resultsLocation + "da_" + i + ".dot");
			// printing the da
			da.print(out, "dot");
			out.close();
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

//	public void currentWIP() throws Exception {
//		boolean debug = true;
//
//		unavoidableWarehouse90ProbBaseActions(debug);
//
//	}

	THTSRunInfo unavoidableWarehousefs(boolean debug, int fsnum, int runNum) throws Exception {
		int numModels = 4;
		double[] hvals = { 1000 };
		int[] rollouts = { 5000 };
		int[] trialLens = { 500 };
		double hval = 20;
		boolean[] goalack = new boolean[2];
		THTSRunInfo rinfo = null;
		int exampleNum = fsnum / 10;
		String[] examples = { "depotShelf_r10_g10_fs0_fsp_0.0_0_", "depotShelf_r10_g10_fs13_fsp_11.0_0_",
				"depotShelf_r10_g10_fs25_fsp_20.0_0_", "shelfDepot_r10_g10_fs37_fsp_30.0_6_",
				"shelfDepot_r10_g10_fs50_fsp_41.0_8_", "depotShelf_r10_g10_fs62_fsp_50.0_0_",
				"shelfDepot_r10_g10_fs74_fsp_60.0_7_", "shelfDepot_r10_g10_fs87_fsp_71.0_0_",
				"shelfDepot_r10_g10_fs100_fsp_81.0_7_", "shelfDepot_r10_g10_fs111_fsp_90.0_8_",
				"depotShelf_r10_g10_fs123_fsp_100_9_" };

		int hvalnum = 0;
		hval = hvals[hvalnum];
		int maxRollouts = rollouts[hvalnum];
		boolean hasSharedState = false;
		int trialLen = trialLens[hvalnum];

		float epsilon = 0.0001f;

		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String testsLocationBase = currentDir + "/tests/wkspace/warehouse_samples/";
		String testsLocation = testsLocationBase + fsnum + "/";
		String resultsLocation = testsLocationBase + "results/manp/" + fsnum + "/";
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
		String combString = "_multi_" + tieBreakingOrderStr + "_costcutoff_" + hval + "_trialLen_" + trialLen
				+ "_rollouts_" + maxRollouts;
		String algoIden = "_avoid_lrtdp" + combString + "_r" + numModels + "_run" + runNum;
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

		MultiAgentNestedProductModelGenerator maModelGen = createNestedMultiAgentModelGen(prism, mainLog, filenames,
				propertiesFileName, resultsLocation, hasSharedState);
		HashMap<Objectives, Entry<Double, Double>> minMaxVals = new HashMap<>();
		minMaxVals.put(Objectives.Cost, new AbstractMap.SimpleEntry<Double, Double>(0., hval));
		minMaxVals.put(Objectives.TaskCompletion,
				new AbstractMap.SimpleEntry<Double, Double>(0., (double) maModelGen.numDAs));

		Heuristic heuristicFunction = new EmptyNestedMultiAgentHeuristic(maModelGen, null, null, hval);

		mainLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
		fileLog.println("Tie Breaking Order " + tieBreakingOrder.toString());

		mainLog.println("Initialising Greedy Bounds Difference Action Selector Function");
		fileLog.println("Initialising Greedy Bounds Difference Action Selector Function");

		ActionSelector actionSelection = new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder,true);// new
																									// ActionSelectorGreedyBoundsDiff(tieBreakingOrder);

		mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
		fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

		OutcomeSelector outcomeSelection = new OutcomeSelectorProbSkipSolved();
		// new OutcomeSelectorRandomSkipSolved();

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
		rinfo = new THTSRunInfo();
		int numRolloutsTillSolved = thts.run(false, 0, debug);

		mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
		fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
		//goalack = thtsNew.thts.runThroughMostProb(new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder,false), resultsLocation);
		rinfo.goalOnProbablePath = goalack[0];
		//goalack = thtsNew.thts.runThrough(new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder,false), resultsLocation);

		mainLog.close();
		fileLog.close();

		rinfo.initialStateValues = thts.getInitialStateBounds();

		rinfo.numRolloutsTillSolved = numRolloutsTillSolved;
		rinfo.goalFound = goalack[0];
		rinfo.initialStateSolved = goalack[1];
		return rinfo;

	}

//	THTSRunInfo unavoidableWarehouse90ProbBaseActions(boolean debug) throws Exception {
//		boolean goalFound = false;
//		int numModels = 4;
//		double[] hvals = { 50, 100, 1000, 5000 };
//		int[] rollouts = { 200, 1000, 10000 };
//
//		double hval = 20;
//		THTSRunInfo rinfo = null;
//		for (int hvalnum = 0; hvalnum < hvals.length; hvalnum++) {
//			hval = hvals[hvalnum];
//			int trialLen = (int) hvals[hvalnum];
//
//			for (int rolloutnum = 0; rolloutnum < rollouts.length; rolloutnum++) {
//				int maxRollouts = rollouts[rolloutnum];
//
//				boolean hasSharedState = false;
//
//				float epsilon = 0.0001f;
//
//				System.out.println(System.getProperty("user.dir"));
//				String currentDir = System.getProperty("user.dir");
//				String testsLocation = currentDir + "/tests/wkspace/warehouse_samples/";
//				String resultsLocation = testsLocation + "results/manp/prob/";
//				// making sure resultsloc exits
//				createDirIfNotExist(resultsLocation);
//				System.out.println("Results Location " + resultsLocation);
//				String[] examples = { "whfree_r10_g10_a1_fs111_fsp_90_8_" };
////					String example = "tro_example_new_small";
//				for (String example : examples) {
//					ArrayList<Objectives> tieBreakingOrder = new ArrayList<Objectives>();
//					tieBreakingOrder.add(Objectives.TaskCompletion);
//					tieBreakingOrder.add(Objectives.Cost);
//					String tieBreakingOrderStr = "_obj_";
//					for (Objectives obj : tieBreakingOrder) {
//						tieBreakingOrderStr += obj.toString() + "_";
//					}
//
//					PrismLog mainLog;
//					if (debug)
//						mainLog = new PrismFileLog("stdout");
//					else
//						mainLog = new PrismDevNullLog();
//
//					Prism prism = new Prism(mainLog);
//					String combString = "_multi_" + tieBreakingOrderStr + "_costcutoff_" + hval + "_trialLen_"
//							+ trialLen + "_rollouts_" + maxRollouts;
//					String algoIden = "_avoid_lrtdp" + combString + "_r" + numModels;
//					algoIden += "_baseActSel_" +
//					// "_tieBreakingSoftmax"+
//							"_skipSolved_";
//
//					PrismLog fileLog = new PrismFileLog(
//							resultsLocation + "log_" + example + algoIden + "_justmdp" + ".txt");//
//
//					prism.initialise();
//					prism.setEngine(Prism.EXPLICIT);
//
//					mainLog.println("Initialised Prism");
//
//					ArrayList<String> filenames = new ArrayList<>();
//
//					for (int numModel = 0; numModel < numModels; numModel++) {
//						String modelFileName = testsLocation + example + numModel + ".prism";
//						filenames.add(modelFileName);
//					}
//
//					String propertiesFileName = testsLocation + example + "mult.prop";
//
//					MultiAgentNestedProductModelGenerator maModelGen = createNestedMultiAgentModelGen(prism, mainLog,
//							filenames, propertiesFileName, resultsLocation, hasSharedState);
//
//					Heuristic heuristicFunction = new MultiAgentHeuristic(maModelGen, singleAgentSolutions, hval);
//					// new MultiAgentHeuristicBaseActions(maModelGen,
//					// singleAgentSolutions, hval);
////								new EmptyNestedMultiAgentHeuristic(maModelGen, null, null, hval);
//
//					mainLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
//					fileLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
//
//					mainLog.println("Initialising Greedy Bounds Difference Action Selector Function");
//					fileLog.println("Initialising Greedy Bounds Difference Action Selector Function");
//
//					ActionSelector actionSelection = new ActionSelectorGreedySoftmaxLBMultiAgentBaseActions(
//							tieBreakingOrder, maModelGen);
//					// new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder);
////								new ActionSelectorGreedyTieBreakRandomSoftmaxLowerBound(tieBreakingOrder);
//					// new ActionSelectorGreedyTieBreakRandomLowerBound(tieBreakingOrder);
//
//					mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
//					fileLog.println("Initialising Greedy Bounds Outcome Selector Function");
//
//					OutcomeSelector outcomeSelection = new OutcomeSelectorRandomSkipSolved();// new
//																								// OutcomeSelectorRandom();
//
//					mainLog.println("Initialising Full Bellman Backup Function");
//					fileLog.println("Initialising Full Bellman Backup Function");
//					HashMap<Objectives, Entry<Double, Double>> minMaxVals = new HashMap<>();
//					minMaxVals.put(Objectives.Cost, new AbstractMap.SimpleEntry<Double, Double>(0., hval));
//					minMaxVals.put(Objectives.TaskCompletion,
//							new AbstractMap.SimpleEntry<Double, Double>(0., (double) maModelGen.numDAs));
//
//					BackupNVI backupFunction = new BackupLabelledFullBelmanCap(tieBreakingOrder, actionSelection,
//							epsilon, minMaxVals);
//
//					mainLog.println("Initialising Reward Helper Function");
//					fileLog.println("Initialising Reward Helper Function");
//
//					RewardHelper rewardH = new RewardHelperMultiAgent(maModelGen, RewardCalculation.SUM);
////							new RewardHelperNestedSingleAgent(saModelGen);
//
//					mainLog.println("Max Rollouts: " + maxRollouts);
//					mainLog.println("Max TrialLen: " + trialLen);
//					fileLog.println("Max Rollouts: " + maxRollouts);
//					fileLog.println("Max TrialLen: " + trialLen);
//
//					mainLog.println("\nInitialising THTS");
//					fileLog.println("\nInitialising THTS");
//					boolean doForwardBackup = true;
//					TrialBasedTreeSearch thtsNew.thts = new TrialBasedTreeSearch((DefaultModelGenerator) maModelGen,
//							maxRollouts, trialLen, heuristicFunction, actionSelection, outcomeSelection, rewardH,
//							backupFunction, doForwardBackup, tieBreakingOrder, mainLog, fileLog);
//
//					mainLog.println("\nBeginning THTS");
//					fileLog.println("\nBeginning THTS");
//					thtsNew.thts.setName(example + algoIden);
//					thtsNew.thts.setResultsLocation(resultsLocation);
//
//					int numRolloutsTillSolved = thtsNew.thts.run(false);
//
//					mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
//					fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
//
//					thtsNew.thts.runThroughMostProb(new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder),
//							resultsLocation);
//
//					boolean[] goalack = thtsNew.thts.runThrough(new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder),
//							resultsLocation);
//
//					mainLog.close();
//					fileLog.close();
//
//					rinfo = new THTSRunInfo();
//					rinfo.numRolloutsTillSolved = numRolloutsTillSolved;
//					rinfo.goalFound = goalack[0];
//					rinfo.initialStateSolved = goalack[1];
//
//					goalFound = rinfo.goalFoundAndSolved();
//
//
//				}
//				if (goalFound)
//					break;
//			}
//			if (goalFound)
//				break;
//		}
//
//		return rinfo;
//	}

	THTSRunInfo unavoidable(boolean debug, int numRun) throws Exception {
		boolean goalFound = false;
		double[] hvals = { 100 };
		int[] rollouts = { 1000 };
		int[] trialLens = { 100 };
		double hval = 20;
		boolean[] goalack = new boolean[2];

		int hvalnum = 0;

		hval = hvals[hvalnum];
		int maxRollouts = rollouts[hvalnum];
		boolean hasSharedState = false;
		int trialLen = trialLens[hvalnum];

		float epsilon = 0.0001f;

		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/manp/";
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
		String combString = "_multi_" + tieBreakingOrderStr + "_costcutoff_" + hval + "_trialLen_" + trialLen
				+ "_rollouts_" + maxRollouts;
		String algoIden = "_avoid_lrtdp" + combString + "_run" + numRun + "_";
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

		MultiAgentNestedProductModelGenerator maModelGen = createNestedMultiAgentModelGen(prism, mainLog, filenames,
				propertiesFileName, resultsLocation, hasSharedState);

		List<State> gs = new ArrayList<State>();
		List<State> deadend = new ArrayList<State>();

		int[] deadendvals = { -1 };

		for (int a1 : deadendvals) {
			for (int a2 : deadendvals) {
				for (int i1 = 0; i1 < 2; i1++) {
					for (int i2 = 0; i2 < 2; i2++) {
						for (int i3 = 0; i3 < 2; i3++) {
							State de1 = new State(5);
							de1.setValue(0, a1);
							de1.setValue(1, a2);
							de1.setValue(2, i1);
							de1.setValue(3, i2);
							de1.setValue(4, i3);
							deadend.add(de1);
						}
					}
				}
			}
		}

		Heuristic heuristicFunction = new EmptyNestedMultiAgentHeuristic(maModelGen, gs, null, hval);

		mainLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
		fileLog.println("Tie Breaking Order " + tieBreakingOrder.toString());

		mainLog.println("Initialising Greedy Bounds Difference Action Selector Function");
		fileLog.println("Initialising Greedy Bounds Difference Action Selector Function");

		ActionSelector actionSelection = new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder,false);// new
																									// ActionSelectorGreedyBoundsDiff(tieBreakingOrder);

		mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
		fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

		OutcomeSelector outcomeSelection = new OutcomeSelectorRandomSkipSolved();

		mainLog.println("Initialising Full Bellman Backup Function");
		fileLog.println("Initialising Full Bellman Backup Function");
		HashMap<Objectives, Entry<Double, Double>> minMaxVals = new HashMap<>();
		minMaxVals.put(Objectives.Cost, new AbstractMap.SimpleEntry<Double, Double>(0., hval));
		minMaxVals.put(Objectives.TaskCompletion,
				new AbstractMap.SimpleEntry<Double, Double>(0., (double) maModelGen.numDAs));

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

	//	goalack = thtsNew.thts.runThrough(new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder,false), resultsLocation);

		mainLog.close();
		fileLog.close();

		THTSRunInfo rinfo = new THTSRunInfo();
		rinfo.numRolloutsTillSolved = numRolloutsTillSolved;
		rinfo.goalFound = goalack[0];
		rinfo.initialStateSolved = goalack[1];
		return rinfo;

	}

	THTSRunInfo avoidable(boolean debug) throws Exception {
		boolean goalFound = false;
		double[] hvals = { 100 };
		int[] rollouts = { 1000 };
		int[] trialLens = { 100 };
		double hval = 20;
		boolean[] goalack = new boolean[2];

		int hvalnum = 0;

		hval = hvals[hvalnum];
		int maxRollouts = rollouts[hvalnum];
		boolean hasSharedState = false;
		int trialLen = trialLens[hvalnum];

		float epsilon = 0.0001f;

		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/manp/";
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
		String combString = "_multi_" + tieBreakingOrderStr + "_costcutoff_" + hval + "_trialLen_" + trialLen
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

		MultiAgentNestedProductModelGenerator maModelGen = createNestedMultiAgentModelGen(prism, mainLog, filenames,
				propertiesFileName, resultsLocation, hasSharedState);

		List<State> gs = new ArrayList<State>();
		List<State> deadend = new ArrayList<State>();

		int[] deadendvals = { -1 };

		for (int a1 : deadendvals) {
			for (int a2 : deadendvals) {
				for (int i1 = 0; i1 < 2; i1++) {
					for (int i2 = 0; i2 < 2; i2++) {
						for (int i3 = 0; i3 < 2; i3++) {
							State de1 = new State(5);
							de1.setValue(0, a1);
							de1.setValue(1, a2);
							de1.setValue(2, i1);
							de1.setValue(3, i2);
							de1.setValue(4, i3);
							deadend.add(de1);
						}
					}
				}
			}
		}

		Heuristic heuristicFunction = new EmptyNestedMultiAgentHeuristic(maModelGen, gs, deadend, hval);

		mainLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
		fileLog.println("Tie Breaking Order " + tieBreakingOrder.toString());

		mainLog.println("Initialising Greedy Bounds Difference Action Selector Function");
		fileLog.println("Initialising Greedy Bounds Difference Action Selector Function");

		ActionSelector actionSelection = new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder,false);// new
																									// ActionSelectorGreedyBoundsDiff(tieBreakingOrder);

		mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
		fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

		OutcomeSelector outcomeSelection = new OutcomeSelectorRandom();

		mainLog.println("Initialising Full Bellman Backup Function");
		fileLog.println("Initialising Full Bellman Backup Function");
		HashMap<Objectives, Entry<Double, Double>> minMaxVals = new HashMap<>();
		minMaxVals.put(Objectives.Cost, new AbstractMap.SimpleEntry<Double, Double>(0., hval));
		minMaxVals.put(Objectives.TaskCompletion,
				new AbstractMap.SimpleEntry<Double, Double>(0., (double) maModelGen.numDAs));

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

		//goalack = thtsNew.thts.runThrough(new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder,false), resultsLocation);
		// we can just trim things where task completion is 0 ?

		mainLog.close();
		fileLog.close();

		THTSRunInfo rinfo = new THTSRunInfo();
		rinfo.numRolloutsTillSolved = numRolloutsTillSolved;
		rinfo.goalFound = goalack[0];
		rinfo.initialStateSolved = goalack[1];
		return rinfo;

	}

	THTSRunInfo noprobabilities(boolean debug) throws Exception {
		boolean goalFound = false;
		double[] hvals = { 100 };
		int[] rollouts = { 1000 };
		int[] trialLens = { 100 };
		double hval = 20;
		boolean[] goalack = new boolean[2];

		int hvalnum = 0;

		hval = hvals[hvalnum];
		int maxRollouts = rollouts[hvalnum];
		boolean hasSharedState = false;
		int trialLen = trialLens[hvalnum];

		float epsilon = 0.0001f;

		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/manp/";
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
		String combString = "_multi_" + tieBreakingOrderStr + "_costcutoff_" + hval + "_trialLen_" + trialLen
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

		MultiAgentNestedProductModelGenerator maModelGen = createNestedMultiAgentModelGen(prism, mainLog, filenames,
				propertiesFileName, resultsLocation, hasSharedState);

		List<State> gs = new ArrayList<State>();
		List<State> deadend = new ArrayList<State>();

		int[] deadendvals = { -1 };

		for (int a1 : deadendvals) {
			for (int a2 : deadendvals) {
				for (int i1 = 0; i1 < 2; i1++) {
					for (int i2 = 0; i2 < 2; i2++) {
						for (int i3 = 0; i3 < 2; i3++) {
							State de1 = new State(5);
							de1.setValue(0, a1);
							de1.setValue(1, a2);
							de1.setValue(2, i1);
							de1.setValue(3, i2);
							de1.setValue(4, i3);
							deadend.add(de1);
						}
					}
				}
			}
		}

		Heuristic heuristicFunction = new EmptyNestedMultiAgentHeuristic(maModelGen, gs, deadend, hval);

		mainLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
		fileLog.println("Tie Breaking Order " + tieBreakingOrder.toString());

		mainLog.println("Initialising Greedy Bounds Difference Action Selector Function");
		fileLog.println("Initialising Greedy Bounds Difference Action Selector Function");

		ActionSelector actionSelection = new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder,false);// new
																									// ActionSelectorGreedyBoundsDiff(tieBreakingOrder);

		mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
		fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

		OutcomeSelector outcomeSelection = new OutcomeSelectorRandom();

		mainLog.println("Initialising Full Bellman Backup Function");
		fileLog.println("Initialising Full Bellman Backup Function");
		HashMap<Objectives, Entry<Double, Double>> minMaxVals = new HashMap<>();
		minMaxVals.put(Objectives.Cost, new AbstractMap.SimpleEntry<Double, Double>(0., hval));
		minMaxVals.put(Objectives.TaskCompletion,
				new AbstractMap.SimpleEntry<Double, Double>(0., (double) maModelGen.numDAs));

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

		//goalack = thtsNew.thts.runThrough(new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder,false), resultsLocation);

		mainLog.close();
		fileLog.close();

		THTSRunInfo rinfo = new THTSRunInfo();
		rinfo.numRolloutsTillSolved = numRolloutsTillSolved;
		rinfo.goalFound = goalack[0];
		rinfo.initialStateSolved = goalack[1];
		return rinfo;

	}

	THTSRunInfo noprobabilities_noavoid(boolean debug) throws Exception {
		boolean goalFound = false;
		double[] hvals = { 100 };
		int[] rollouts = { 1000 };
		int[] trialLens = { 100 };
		double hval = 20;
		boolean[] goalack = new boolean[2];

		int hvalnum = 0;

		hval = hvals[hvalnum];

		int maxRollouts = rollouts[hvalnum];

		boolean hasSharedState = false;
		int trialLen = trialLens[hvalnum];

		float epsilon = 0.0001f;

		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/manp/";
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
		String combString = "_multi_" + tieBreakingOrderStr + "_costcutoff_" + hval + "_trialLen_" + trialLen
				+ "_rollouts_" + maxRollouts;
		String algoIden = "_noavoid_lrtdp" + combString;
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

		String propertiesFileName = testsLocation + example + "_mult_noavoid.prop";

		MultiAgentNestedProductModelGenerator maModelGen = createNestedMultiAgentModelGen(prism, mainLog, filenames,
				propertiesFileName, resultsLocation, hasSharedState);

		List<State> gs = new ArrayList<State>();
		List<State> deadend = new ArrayList<State>();

		int[] deadendvals = { -1 };

		for (int a1 : deadendvals) {
			for (int a2 : deadendvals) {
				for (int i1 = 0; i1 < 2; i1++) {
					for (int i2 = 0; i2 < 2; i2++) {

						State de1 = new State(4);
						de1.setValue(0, a1);
						de1.setValue(1, a2);
						de1.setValue(2, i1);
						de1.setValue(3, i2);

						deadend.add(de1);

					}
				}
			}
		}

		Heuristic heuristicFunction = new EmptyNestedMultiAgentHeuristic(maModelGen, gs, deadend, hval);

		mainLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
		fileLog.println("Tie Breaking Order " + tieBreakingOrder.toString());

		mainLog.println("Initialising Greedy Bounds Difference Action Selector Function");
		fileLog.println("Initialising Greedy Bounds Difference Action Selector Function");

		ActionSelector actionSelection = new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder,false);// new
																									// ActionSelectorGreedyBoundsDiff(tieBreakingOrder);

		mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
		fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

		OutcomeSelector outcomeSelection = new OutcomeSelectorRandom();

		mainLog.println("Initialising Full Bellman Backup Function");
		fileLog.println("Initialising Full Bellman Backup Function");
		HashMap<Objectives, Entry<Double, Double>> minMaxVals = new HashMap<>();
		minMaxVals.put(Objectives.Cost, new AbstractMap.SimpleEntry<Double, Double>(0., hval));
		minMaxVals.put(Objectives.TaskCompletion,
				new AbstractMap.SimpleEntry<Double, Double>(0., (double) maModelGen.numDAs));

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

		//goalack = thtsNew.thts.runThrough(new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder,false), resultsLocation);

		mainLog.close();
		fileLog.close();

		THTSRunInfo rinfo = new THTSRunInfo();
		rinfo.numRolloutsTillSolved = numRolloutsTillSolved;
		rinfo.goalFound = goalack[0];
		rinfo.initialStateSolved = goalack[1];
		return rinfo;

	}

}
