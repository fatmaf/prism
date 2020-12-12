package thts.scratch;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

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
import thts.treesearch.utils.Objectives;
import thts.treesearch.actionselector.ActionSelector;
import thts.treesearch.actionselector.ActionSelectorGreedySimpleLowerBound;
import thts.treesearch.backup.BackupLabelledFullBelmanCap;
import thts.treesearch.backup.BackupNVI;
import thts.treesearch.heuristic.Heuristic;
import thts.treesearch.outcomeselector.OutcomeSelector;
import thts.treesearch.outcomeselector.OutcomeSelectorRandom;
import thts.treesearch.rewardhelper.RewardCalculation;
import thts.treesearch.rewardhelper.RewardHelper;
import thts.treesearch.rewardhelper.RewardHelperMASubGoals;
import thts.treesearch.utils.THTSRunInfo;
import thts.treesearch.TrialBasedTreeSearch;
import thts.vi.SingleAgentSolverMaxExpTask;

public class TestLRTDPNestedMASubGoals {

	// running this from the commandline
	// PRISM_MAINCLASS=thtsNew.Scratch.TestLRTDPNestedMultiAgent prism/bin/prism
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			TestLRTDPNestedMASubGoals tester = new TestLRTDPNestedMASubGoals();
			String[] options = { "all", "noprobnoavoid", "noprobavoid", "avoidable", "unavoidable", "currentWIP",
					"unavoidable_sas_h", "noprobavoid_sas_h", "unavoidable_warehouse", "multruns" };

			String option = "currentWIP";

			if (args.length > 1) {
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
				tester.runMultipleRunsUnavoidable(10);
//				tester.unavoidable(false);

			} else if (option.contentEquals(options[5])) // currentWIP
			{
				tester.currentWIP();
			} else {
				System.out
						.println("Unimplemented option " + option + "\nAvailable options " + Arrays.toString(options));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

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
//				if (option.contentEquals(options[0])) // noprob
//				{
//					rinfo = noprobabilities_noavoid(debug);
//
//				} else if (option.contentEquals(options[1])) // avoidable
//				{
//					rinfo = noprobabilities(debug);
//
//				} else if (option.contentEquals(options[2])) // unavoidable
//				{
//					rinfo = avoidable(debug);
//
//				} else if (option.contentEquals(options[3])) // unavoidable
//				{
				rinfo = unavoidable(debug, run);

//				} else {
//					System.out.println(
//							"Unimplemented option " + option + "\nAvailable options " + Arrays.toString(options));
//				}

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
		PrismLog csvRes = new PrismFileLog(resultsLocation + "MultiAgentLRTDP_ltlspecs.csv");
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

	public MANestedProductModelGeneratorSubGoals createNestedMultiAgentModelGen(Prism prism, PrismLog mainLog,
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
		MANestedProductModelGeneratorSubGoals mapmg = new MANestedProductModelGeneratorSubGoals(mfmodgens, das,
				labelExprsList, safetydaind, sharedStateVars);

		return mapmg;
	}

	public void currentWIP() throws Exception {
		boolean debug = true;
		unavoidable(debug, 0);
//		unavoidableSingleAgentSolH(debug);
//		unavoidableWarehouseNoProb(debug);
//		unavoidableWarehouse90Prob(debug);
//		unavoidableWarehouse90ProbBaseActions(debug);
//		unavoidableWarehouseVariableProb(debug);
//		unavoidableWarehouse90ProbSingleAgentSolH(debug);
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

//		for (int hvalnum = 0; hvalnum < hvals.length; hvalnum++) {
//			hval = hvals[hvalnum];
//			for (int rolloutnum = 0; rolloutnum < rollouts.length; rolloutnum++) {
//				int maxRollouts = rollouts[rolloutnum];
//				for (int trialLennum = 0; trialLennum < trialLens.length; trialLennum++) {
//					boolean hasSharedState = true;
//					int trialLen = trialLens[trialLennum];

		float epsilon = 0.0001f;

		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/manp/subgoals/";
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

		MANestedProductModelGeneratorSubGoals maModelGen = createNestedMultiAgentModelGen(prism, mainLog, filenames,
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

		EmptyNestedMAHeuristicSubGoals heuristicFunction = new EmptyNestedMAHeuristicSubGoals(maModelGen, gs, null,
				hval);

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

		RewardHelper rewardH = new RewardHelperMASubGoals(maModelGen, RewardCalculation.SUM);
//							new RewardHelperNestedSingleAgent(saModelGen);

		mainLog.println("Max Rollouts: " + maxRollouts);
		mainLog.println("Max TrialLen: " + trialLen);
		fileLog.println("Max Rollouts: " + maxRollouts);
		fileLog.println("Max TrialLen: " + trialLen);

		mainLog.println("\nInitialising THTS");
		fileLog.println("\nInitialising THTS");
		boolean doForwardBackup = true;
		TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) maModelGen, maxRollouts, trialLen,
				(Heuristic) heuristicFunction, actionSelection, outcomeSelection, rewardH, backupFunction,
				doForwardBackup, tieBreakingOrder, mainLog, fileLog);

		mainLog.println("\nBeginning THTS");
		fileLog.println("\nBeginning THTS");
		thts.setName(example + algoIden);
		thts.setResultsLocation(resultsLocation);

		// simple idea, you run thtsNew.thts to the first subgoal,
		// then for all the subgoals you found, you run it using those as the initial
		// state
		// to the next subgoal
		// and keep doing this till you reach the acc number
		int numRolloutsTillSolved = 0;
		while (!heuristicFunction.reachedAcc()) {
			heuristicFunction.incSubGoal();
			if (maModelGen.initialStateList == null)
				numRolloutsTillSolved += thts.run(false, 0,debug);
			else {
				for (int rnNum = 0; rnNum < maModelGen.initialStateList.size(); rnNum++) {
					numRolloutsTillSolved += thts.run(false, rnNum,debug);
				}
			}

			mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
			fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
			ArrayList<State> res;
			if (maModelGen.initialStateList == null) {
				res = thts.runThroughRetFinalStatesList(new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder,false),
						resultsLocation, 0, heuristicFunction.subGoalNum);
				if (res.size() > 0)
					maModelGen.initialStateList = res;
			} else {
				res = new ArrayList<>();
				for (int rnNum = 0; rnNum < maModelGen.initialStateList.size(); rnNum++) {
					ArrayList<State> tres = thts.runThroughRetFinalStatesList(
							new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder,false), resultsLocation, rnNum,
							heuristicFunction.subGoalNum);
					res.addAll(tres);

				}
				if (res.size() > 0) {
					maModelGen.initialStateList = res;
				}
			}
			if(res == null)
				break;

		}
		// one last time
		maModelGen.initialStateList = null;
		ArrayList<State> tres = thts.runThroughRetFinalStatesList(
				new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder,false), resultsLocation, 0,
				heuristicFunction.subGoalNum + 1);
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

//		for (int hvalnum = 0; hvalnum < hvals.length; hvalnum++) {
//			hval = hvals[hvalnum];
//			for (int rolloutnum = 0; rolloutnum < rollouts.length; rolloutnum++) {
//				int maxRollouts = rollouts[rolloutnum];
//				for (int trialLennum = 0; trialLennum < trialLens.length; trialLennum++) {
//					boolean hasSharedState = false;
//					int trialLen = trialLens[trialLennum];

		float epsilon = 0.0001f;

		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/manp/subgoals/";
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

		MANestedProductModelGeneratorSubGoals maModelGen = createNestedMultiAgentModelGen(prism, mainLog, filenames,
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

		EmptyNestedMAHeuristicSubGoals heuristicFunction = new EmptyNestedMAHeuristicSubGoals(maModelGen, gs, deadend,
				hval);

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

		RewardHelper rewardH = new RewardHelperMASubGoals(maModelGen, RewardCalculation.SUM);
//							new RewardHelperNestedSingleAgent(saModelGen);

		mainLog.println("Max Rollouts: " + maxRollouts);
		mainLog.println("Max TrialLen: " + trialLen);
		fileLog.println("Max Rollouts: " + maxRollouts);
		fileLog.println("Max TrialLen: " + trialLen);

		mainLog.println("\nInitialising THTS");
		fileLog.println("\nInitialising THTS");
		boolean doForwardBackup = true;

		TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) maModelGen, maxRollouts, trialLen,
				(Heuristic) heuristicFunction, actionSelection, outcomeSelection, rewardH, backupFunction,
				doForwardBackup, tieBreakingOrder, mainLog, fileLog);

		mainLog.println("\nBeginning THTS");
		fileLog.println("\nBeginning THTS");
		thts.setName(example + algoIden);
		thts.setResultsLocation(resultsLocation);

		// simple idea, you run thtsNew.thts to the first subgoal,
		// then for all the subgoals you found, you run it using those as the initial
		// state
		// to the next subgoal
		// and keep doing this till you reach the acc number
		int numRolloutsTillSolved = 0;
		while (!heuristicFunction.reachedAcc()) {
			heuristicFunction.incSubGoal();
			numRolloutsTillSolved += thts.run(false);

			mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
			fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");

			ArrayList<State> res = thts.runThroughRetFinalStatesList(
					new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder,false), resultsLocation, 0,
					heuristicFunction.subGoalNum);
			maModelGen.initialStateList = res;

		}
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

//		for (int hvalnum = 0; hvalnum < hvals.length; hvalnum++) {
//			hval = hvals[hvalnum];
//			for (int rolloutnum = 0; rolloutnum < rollouts.length; rolloutnum++) {
//				int maxRollouts = rollouts[rolloutnum];
//				for (int trialLennum = 0; trialLennum < trialLens.length; trialLennum++) {
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

		MANestedProductModelGeneratorSubGoals maModelGen = createNestedMultiAgentModelGen(prism, mainLog, filenames,
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

		Heuristic heuristicFunction = new EmptyNestedMAHeuristicSubGoals(maModelGen, gs, deadend, hval);

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

		RewardHelper rewardH = new RewardHelperMASubGoals(maModelGen, RewardCalculation.SUM);
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

//		goalack = thtsNew.thts.runThrough(new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder,false), resultsLocation);

		mainLog.close();
		fileLog.close();

		THTSRunInfo rinfo = new THTSRunInfo();
		rinfo.numRolloutsTillSolved = numRolloutsTillSolved;
		rinfo.goalFound = goalack[0];
		rinfo.initialStateSolved = goalack[1];
		return rinfo;

//					thtsNew.thts.run(false);
//
//					mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
//					fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
//
//					goalack = thtsNew.thts.runThrough(new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder,false),
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

	THTSRunInfo noprobabilities_noavoid(boolean debug) throws Exception {
		boolean goalFound = false;
		double[] hvals = { 100 };
		int[] rollouts = { 1000 };
		int[] trialLens = { 100 };
		double hval = 20;
		boolean[] goalack = new boolean[2];

//		for (
		int hvalnum = 0;
//		hvalnum < hvals.length; hvalnum++) {
		hval = hvals[hvalnum];
//			for (int rolloutnum = 0; rolloutnum < rollouts.length; rolloutnum++) {
		int maxRollouts = rollouts[hvalnum];
//				for (int trialLennum = 0; trialLennum < trialLens.length; trialLennum++) {
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

		MANestedProductModelGeneratorSubGoals maModelGen = createNestedMultiAgentModelGen(prism, mainLog, filenames,
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

		Heuristic heuristicFunction = new EmptyNestedMAHeuristicSubGoals(maModelGen, gs, deadend, hval);

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

		RewardHelper rewardH = new RewardHelperMASubGoals(maModelGen, RewardCalculation.SUM);
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

//		goalack = thtsNew.thts.runThrough(new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder,false), resultsLocation);

		mainLog.close();
		fileLog.close();

		THTSRunInfo rinfo = new THTSRunInfo();
		rinfo.numRolloutsTillSolved = numRolloutsTillSolved;
		rinfo.goalFound = goalack[0];
		rinfo.initialStateSolved = goalack[1];
		return rinfo;

//					
//					
//					thtsNew.thts.run(false);
//
//					mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
//					fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
//
//					goalack = thtsNew.thts.runThrough(new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder,false),
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

//				}
//				if (goalFound)
//					break;
//			}
//			if (goalFound)
//				break;
//		}

//		return goalack;
	}

}
