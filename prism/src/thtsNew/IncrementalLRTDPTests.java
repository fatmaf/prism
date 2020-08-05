package thtsNew;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import acceptance.AcceptanceOmega;
import acceptance.AcceptanceType;
import automata.DA;
import explicit.LTLModelChecker;
import parser.State;
import parser.ast.Expression;
import parser.ast.ExpressionProb;
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
import prism.RewardGenerator;
import simulator.ModulesFileModelGenerator;
import thts.Objectives;

//a class that tests LRTDP and THTS
public class IncrementalLRTDPTests {

	// testing trialbasedtree search with just an mdp
	// have the default product model generator
	public void createDirIfNotExist(String directoryName) {
		File directory = new File(directoryName);
		if (!directory.exists()) {
			directory.mkdir();
			// If you require it to make the entire directory path including parents,
			// use directory.mkdirs(); here instead.
		}

	}

	public void runTests() throws Exception {
		String goalsNotFound = "";
		String notSolved = "";
		int tests = 1;
		int passed = 0;
		int goalFound = 0;
		int solved = 0;
		boolean debug = false;

		int[] goalStates = { 3, 6 };

		String[] examples = { "tro_example_new_small_noprob", "tro_example_new_small_onefailaction" };

		tests = tests + goalStates.length * examples.length;

		String[] examples2 = { "tro_example_new_small_noprob", "tro_example_new_small_onefailaction",
				"tro_example_new_small_allfailpaths_nowait", "tro_example_new_small_allfailpaths" };

		tests = tests + (goalStates.length * examples2.length) * 2;

		String[] examples3 = examples2;// { "tro_example_new_small_allfailpaths_nowait",
										// "tro_example_new_small_allfailpaths" };
		tests = tests + (goalStates.length * examples3.length) * 2;

		int[] goalStates2 = { 0, 4, 6 };
		tests = tests + goalStates2.length * 2;

		int currentTest = 1;

		System.out.println("Test " + currentTest++ + "/" + tests);
		boolean[] goalFoundAndSolved = gssp(debug);

		if (goalFoundAndSolved[0])
			goalFound++;
		else
			goalsNotFound += "GSSP\n";
		if (goalFoundAndSolved[1])
			solved++;
		else
			notSolved += "GSSP\n";
		if (goalFoundAndSolved[0] && goalFoundAndSolved[1])
			passed++;

		for (String example : examples) {
			for (int g : goalStates) {
				System.out.println("Test " + currentTest++ + "/" + tests);
				goalFoundAndSolved = simpleLRTDPNoDeadends(example, g, debug);
				if (goalFoundAndSolved[0])
					goalFound++;
				else
					goalsNotFound += example + " lrtdp no deadends\n";
				if (goalFoundAndSolved[1])
					solved++;
				else
					notSolved += example + " lrtdp no deadends\n";
				if (goalFoundAndSolved[0] && goalFoundAndSolved[1])
					passed++;

			}
		}

		for (String example : examples2) {
			for (int g : goalStates) {
//with deadends	
				System.out.println("Test " + currentTest++ + "/" + tests);
				goalFoundAndSolved = simpleLRTDP(example, g, debug);
				if (goalFoundAndSolved[0])
					goalFound++;
				else
					goalsNotFound += example + " lrtdp with deadends\n";
				if (goalFoundAndSolved[1])
					solved++;
				else
					notSolved += example + " lrtdp with deadends\n";
				if (goalFoundAndSolved[0] && goalFoundAndSolved[1])
					passed++;

			}
		}

		for (String example : examples2) {
			for (int g : goalStates) {
//with deadends	
				System.out.println("Test " + currentTest++ + "/" + tests);
				goalFoundAndSolved = simpleLRTDPAvoid(example, g, debug);
				if (goalFoundAndSolved[0])
					goalFound++;
				else
					goalsNotFound += example + " lrtdp with deadends and avoid\n";
				if (goalFoundAndSolved[1])
					solved++;
				else
					notSolved += example + " lrtdp with deadends and avoid\n";
				if (goalFoundAndSolved[0] && goalFoundAndSolved[1])
					passed++;

			}
		}

		for (String example : examples3) {
			for (int g : goalStates) {
//with deadends
				System.out.println("Test " + currentTest++ + "/" + tests);
				goalFoundAndSolved = nestedLRTDP(example, g, debug);
				if (goalFoundAndSolved[0])
					goalFound++;
				else {
					goalsNotFound += example + " " + g + " nested lrtdp with deadends\n";
					System.out.println(example + " " + g + " nested lrtdp with deadends - goal not found");
				}
				if (goalFoundAndSolved[1])
					solved++;
				else {
					notSolved += example + " " + g + " nested lrtdp with deadends\n";
					System.out.println(example + " " + g + " nested lrtdp with deadends - not solved");
				}
				if (goalFoundAndSolved[0] && goalFoundAndSolved[1])
					passed++;

			}
		}

		for (String example : examples3) {
			for (int g : goalStates) {
//with deadends
				System.out.println("Test " + currentTest++ + "/" + tests);
				goalFoundAndSolved = nestedLRTDPAvoid(example, g, debug);
				if (goalFoundAndSolved[0])
					goalFound++;
				else {
					goalsNotFound += example + " " + g + " nested lrtdp with deadends and avoid\n";
					System.out.println(example + " " + g + " nested lrtdp with deadends and avoid - goal not found");
				}
				if (goalFoundAndSolved[1])
					solved++;
				else {
					notSolved += example + " " + g + " nested lrtdp with deadends and avoid\n";
					System.out.println(example + " " + g + " nested lrtdp with deadends and avoid - not solved");
				}
				if (goalFoundAndSolved[0] && goalFoundAndSolved[1])
					passed++;

			}
		}

		for (int g : goalStates2) {
			// with deadends
			System.out.println("Test " + currentTest++ + "/" + tests);
			goalFoundAndSolved = nestedLRTDPDoors(g, debug);
			if (goalFoundAndSolved[0])
				goalFound++;
			else
				goalsNotFound += " " + g + " doors nested lrtdp with deadends\n";

			if (goalFoundAndSolved[1])
				solved++;
			else
				notSolved += " " + g + " doors nested lrtdp with deadends\n";

			if (goalFoundAndSolved[0] && goalFoundAndSolved[1])
				passed++;

		}
		for (int g : goalStates2) {
			// with deadends
			System.out.println("Test " + currentTest++ + "/" + tests);
			goalFoundAndSolved = nestedLRTDPDoorsAvoid(g, debug);
			if (goalFoundAndSolved[0])
				goalFound++;
			else
				goalsNotFound += " " + g + " doors nested lrtdp with deadends and avoid\n";

			if (goalFoundAndSolved[1])
				solved++;
			else
				notSolved += " " + g + " doors nested lrtdp with deadends and avoid\n";

			if (goalFoundAndSolved[0] && goalFoundAndSolved[1])
				passed++;

		}

		System.out.println("Passed " + passed + "/" + tests);
		System.out.println("Goals Found " + goalFound + "/" + tests
				+ ((goalFound == tests) ? "" : "\nGoals Not Found:\n" + goalsNotFound));
		System.out.println("Initial State Solved " + solved + "/" + tests
				+ ((solved == tests) ? "" : "\nInitial State Not Solved:\n" + notSolved));

//		subTest();
	}

	public void testLRTDPWithDeadendsAvoid() throws Exception {
		int[] goalStates = { 3, 6 };
		String[] examples2 = { "tro_example_new_small_noprob", "tro_example_new_small_onefailaction",
				"tro_example_new_small_allfailpaths_nowait", "tro_example_new_small_allfailpaths" };

		boolean[] goalFoundAndSolved;
		String goalsNotFound = "";
		String notSolved = "";
		int tests = goalStates.length * examples2.length;
		int passed = 0;
		int goalFound = 0;
		int solved = 0;
		boolean debug = false;
		int currentTest = 1;
		for (String example : examples2) {
			for (int g : goalStates) {
//with deadends	
				System.out.println("Test " + currentTest++ + "/" + tests);
				goalFoundAndSolved = simpleLRTDPAvoid(example, g, debug);
				if (goalFoundAndSolved[0])
					goalFound++;
				else
					goalsNotFound += example + " lrtdp with deadends\n";
				if (goalFoundAndSolved[1])
					solved++;
				else
					notSolved += example + " lrtdp with deadends\n";
				if (goalFoundAndSolved[0] && goalFoundAndSolved[1])
					passed++;

			}
		}
		System.out.println("Passed " + passed + "/" + tests);
		System.out.println("Goals Found " + goalFound + "/" + tests
				+ ((goalFound == tests) ? "" : "\nGoals Not Found:\n" + goalsNotFound));
		System.out.println("Initial State Solved " + solved + "/" + tests
				+ ((solved == tests) ? "" : "\nInitial State Not Solved:\n" + notSolved));

	}

	public void testLRTDPWithDeadends() throws Exception {
		int[] goalStates = { 3, 6 };
		String[] examples2 = { "tro_example_new_small_noprob", "tro_example_new_small_onefailaction",
				"tro_example_new_small_allfailpaths_nowait", "tro_example_new_small_allfailpaths" };

		boolean[] goalFoundAndSolved;
		String goalsNotFound = "";
		String notSolved = "";
		int tests = goalStates.length * examples2.length;
		int passed = 0;
		int goalFound = 0;
		int solved = 0;
		boolean debug = false;
		int currentTest = 1;
		for (String example : examples2) {
			for (int g : goalStates) {
//with deadends	
				System.out.println("Test " + currentTest++ + "/" + tests);
				goalFoundAndSolved = simpleLRTDP(example, g, debug);
				if (goalFoundAndSolved[0])
					goalFound++;
				else
					goalsNotFound += example + " lrtdp with deadends\n";
				if (goalFoundAndSolved[1])
					solved++;
				else
					notSolved += example + " lrtdp with deadends\n";
				if (goalFoundAndSolved[0] && goalFoundAndSolved[1])
					passed++;

			}
		}
		System.out.println("Passed " + passed + "/" + tests);
		System.out.println("Goals Found " + goalFound + "/" + tests
				+ ((goalFound == tests) ? "" : "\nGoals Not Found:\n" + goalsNotFound));
		System.out.println("Initial State Solved " + solved + "/" + tests
				+ ((solved == tests) ? "" : "\nInitial State Not Solved:\n" + notSolved));

	}

	public void testNestedLRTDPDoors() throws Exception {

		boolean[] goalFoundAndSolved;
		String goalsNotFound = "";
		String notSolved = "";
		int[] goalStates2 = { 0, 4, 6 };
		int tests = goalStates2.length;
		int passed = 0;
		int goalFound = 0;
		int solved = 0;
		boolean debug = false;
		int currentTest = 1;

		for (int g : goalStates2) {
			// with deadends
			System.out.println("Test " + currentTest++ + "/" + tests);
			goalFoundAndSolved = nestedLRTDPDoors(g, debug);
			if (goalFoundAndSolved[0])
				goalFound++;
			else
				goalsNotFound += " " + g + " doors nested lrtdp with deadends\n";

			if (goalFoundAndSolved[1])
				solved++;
			else
				notSolved += " " + g + " doors nested lrtdp with deadends\n";

			if (goalFoundAndSolved[0] && goalFoundAndSolved[1])
				passed++;

		}
		System.out.println("Passed " + passed + "/" + tests);
		System.out.println("Goals Found " + goalFound + "/" + tests
				+ ((goalFound == tests) ? "" : "\nGoals Not Found:\n" + goalsNotFound));
		System.out.println("Initial State Solved " + solved + "/" + tests
				+ ((solved == tests) ? "" : "\nInitial State Not Solved:\n" + notSolved));

	}

	public void testNestedLRTDPDoorsAvoid() throws Exception {

		boolean[] goalFoundAndSolved;
		String goalsNotFound = "";
		String notSolved = "";
		int[] goalStates2 = { 0, 4, 6 };
		int tests = goalStates2.length;
		int passed = 0;
		int goalFound = 0;
		int solved = 0;
		boolean debug = false;
		int currentTest = 1;

		for (int g : goalStates2) {
			// with deadends
			System.out.println("Test " + currentTest++ + "/" + tests);
			goalFoundAndSolved = nestedLRTDPDoorsAvoid(g, debug);
			if (goalFoundAndSolved[0])
				goalFound++;
			else
				goalsNotFound += " " + g + " doors nested lrtdp with deadends\n";

			if (goalFoundAndSolved[1])
				solved++;
			else
				notSolved += " " + g + " doors nested lrtdp with deadends\n";

			if (goalFoundAndSolved[0] && goalFoundAndSolved[1])
				passed++;

		}
		System.out.println("Passed " + passed + "/" + tests);
		System.out.println("Goals Found " + goalFound + "/" + tests
				+ ((goalFound == tests) ? "" : "\nGoals Not Found:\n" + goalsNotFound));
		System.out.println("Initial State Solved " + solved + "/" + tests
				+ ((solved == tests) ? "" : "\nInitial State Not Solved:\n" + notSolved));

	}

	public void testNestedLRTDPWithDeadends() throws Exception {
		int[] goalStates = { 3, 6 };

		String[] examples3 = { "tro_example_new_small_noprob", "tro_example_new_small_onefailaction",
				"tro_example_new_small_allfailpaths_nowait", "tro_example_new_small_allfailpaths" };
		boolean[] goalFoundAndSolved;
		String goalsNotFound = "";
		String notSolved = "";
		int tests = goalStates.length * examples3.length;
		int passed = 0;
		int goalFound = 0;
		int solved = 0;
		boolean debug = false;
		int currentTest = 1;

		for (String example : examples3) {
			for (int g : goalStates) {
//with deadends
				System.out.println("Test " + currentTest++ + "/" + tests);
				goalFoundAndSolved = nestedLRTDP(example, g, debug);
				if (goalFoundAndSolved[0])
					goalFound++;
				else {
					goalsNotFound += example + " " + g + " nested lrtdp with deadends\n";
					System.out.println(example + " " + g + " nested lrtdp with deadends - goal not found");
				}
				if (goalFoundAndSolved[1])
					solved++;
				else {
					notSolved += example + " " + g + " nested lrtdp with deadends\n";
					System.out.println(example + " " + g + " nested lrtdp with deadends - not solved");
				}
				if (goalFoundAndSolved[0] && goalFoundAndSolved[1])
					passed++;

			}
		}
		System.out.println("Passed " + passed + "/" + tests);
		System.out.println("Goals Found " + goalFound + "/" + tests
				+ ((goalFound == tests) ? "" : "\nGoals Not Found:\n" + goalsNotFound));
		System.out.println("Initial State Solved " + solved + "/" + tests
				+ ((solved == tests) ? "" : "\nInitial State Not Solved:\n" + notSolved));

	}

	public void testNestedLRTDPWithDeadendsAvoidLTLSpecs() throws Exception {

		int g = 4;

		String goalsNotFound = "";
		String notSolved = "";
		int tests = 1;
		int passed = 0;
		int goalFound = 0;
		int solved = 0;
		boolean debug = false;

		boolean[] goalFoundAndSolved = nestedLRTDPDoorsAvoidProductSingleAgent(g, debug);
		if (goalFoundAndSolved[0])
			goalFound++;
		else {
			goalsNotFound += "nested lrtdp doors with deadends and avoid with ltl specs\n";
			System.out.println(
					"nested lrtdp doors with deadends and avoid with ltl specs" + " " + g + " - goal not found");
		}
		if (goalFoundAndSolved[1])
			solved++;
		else {
			notSolved += "nested lrtdp doors with deadends and avoid with ltl specs\n";
			System.out.println("nested lrtdp doors with deadends and avoid with ltl specs" + " " + g + " - not solved");

		}
		if (goalFoundAndSolved[0] && goalFoundAndSolved[1])
			passed++;

		System.out.println("Passed " + passed + "/" + tests);
		System.out.println("Goals Found " + goalFound + "/" + tests
				+ ((goalFound == tests) ? "" : "\nGoals Not Found:\n" + goalsNotFound));
		System.out.println("Initial State Solved " + solved + "/" + tests
				+ ((solved == tests) ? "" : "\nInitial State Not Solved:\n" + notSolved));

	}

	public void debugInstance() throws Exception {

		String example = "tro_example_new_small_noprob";// "tro_example_new_small_allfailpaths";
		int g = 4;// 3;//6;
		boolean debug = true;
		boolean[] goalFoundAndSolved = nestedLRTDPDoorsAvoid(g, debug);// nestedLRTDPAvoid(example, g, debug);
		if (goalFoundAndSolved[1] == false)
			System.out.println("Not Solved");
	}

	public void currentWIP() throws Exception {

		boolean debug = true;
		boolean[] goalFoundAndSolved = nestedLRTDPDoorsAvoidProductNestedSingleAgentTaskCompletion(debug);
	}

	public static void main(String[] args) {
		try {
			IncrementalLRTDPTests tester = new IncrementalLRTDPTests();
			String options[] = { "all", "lrtdp_deadends", "nested_lrtdp_deadends_doors", "nested_lrtdp_deadends",
					"help", "-h", "nested_lrtdp_deadends_doors_avoid", "debugInstance", "currentWIP",
					"nested_lrtdp_deadends_doors_avoid_ltl" };
			String option = "currentWIP";// "debugInstance";//"all";
			if (args.length > 1) {
				System.out.println(Arrays.deepToString(args));
				option = args[0];
				System.out.println("Running with argument: " + option);
				System.in.read();
			}

			if (option.contentEquals(options[0]))
				tester.runTests();
			else if (option.contentEquals(options[1]))
				tester.testLRTDPWithDeadends();
			else if (option.contentEquals(options[2]))
				tester.testNestedLRTDPDoors();
			else if (option.contentEquals(options[3]))
				tester.testNestedLRTDPWithDeadends();
			else if (option.contentEquals(options[4]) | option.contentEquals(options[5])) {
				System.out.println("Available options " + Arrays.toString(options));
			} else if (option.contentEquals(options[6])) {
				tester.testNestedLRTDPDoorsAvoid();
			} else if (option.contentEquals(options[7])) {
				tester.debugInstance();
			} else if (option.contentEquals(options[8])) {
				tester.currentWIP();
			} else if (option.contentEquals(options[9])) {
				tester.testNestedLRTDPWithDeadendsAvoidLTLSpecs();
			} else {
				System.out
						.println("Unimplemented option " + option + "\nAvailable options " + Arrays.toString(options));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	boolean[] nestedLRTDPDoors(int stateVal, boolean debug) throws Exception {
		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/";
		// making sure resultsloc exits
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);

		String example = "tro_example_new_small";

		PrismLog mainLog;
		if (debug)
			mainLog = new PrismFileLog("stdout");
		else
			mainLog = new PrismDevNullLog();

		Prism prism = new Prism(mainLog);
		String combString = "_prob_cost_lrtdp";
		String algoIden = "rtdp" + combString;
		PrismLog fileLog = new PrismFileLog(
				resultsLocation + "log_" + example + "_g_" + stateVal + "_" + algoIden + "_justmdp" + ".txt");//

		prism.initialise();
		prism.setEngine(Prism.EXPLICIT);

		mainLog.println("Initialised Prism");

		String modelFileName = testsLocation + example + "0.prism";
		ModulesFile modulesFile = prism.parseModelFile(new File(modelFileName)); // because the models are uniform

		ModulesFileModelGenerator modGen = new ModulesFileModelGenerator(modulesFile, prism);
		MDPModelGenerator mdpModGen = new MDPModelGenerator(modGen);

		int maxRollouts = 1000;
		int trialLen = 100;
		float epsilon = 0.0001f;

		List<State> gs = new ArrayList<State>();
		List<State> deadend = new ArrayList<State>();
		for (int i = -1; i < 2; i++) {

			State goalState1 = new State(2);
			goalState1.setValue(0, stateVal);
			goalState1.setValue(1, i);
			gs.add(goalState1);
		}

		int deadendval = -1;
		for (int i = -1; i < 2; i++) {
			State de1 = new State(2);
			de1.setValue(0, deadendval);
			de1.setValue(1, i);
			deadend.add(de1);
		}

		Heuristic heuristicFunction = new EmptyHeuristic(gs, deadend);
		ArrayList<Objectives> tieBreakingOrder = new ArrayList<Objectives>();
		tieBreakingOrder.add(Objectives.Probability);
		tieBreakingOrder.add(Objectives.Cost);

		mainLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
		fileLog.println("Tie Breaking Order " + tieBreakingOrder.toString());

		mainLog.println("Initialising Greedy Bounds Difference Action Selector Function");
		fileLog.println("Initialising Greedy Bounds Difference Action Selector Function");

		ActionSelector actionSelection = new ActionSelectorGreedyLowerBound(tieBreakingOrder, true);// new
																									// ActionSelectorGreedyBoundsDiff(tieBreakingOrder);

		mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
		fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

		OutcomeSelector outcomeSelection = new OutcomeSelectorRandom();

		mainLog.println("Initialising Full Bellman Backup Function");
		fileLog.println("Initialising Full Bellman Backup Function");

		BackupNVI backupFunction = new BackupLabelledFullBelman(tieBreakingOrder, actionSelection, epsilon);

		mainLog.println("Initialising Reward Helper Function");
		fileLog.println("Initialising Reward Helper Function");

		RewardHelper rewardH = new RewardHelperMDP(mdpModGen);

		mainLog.println("Max Rollouts: " + maxRollouts);
		mainLog.println("Max TrialLen: " + trialLen);
		fileLog.println("Max Rollouts: " + maxRollouts);
		fileLog.println("Max TrialLen: " + trialLen);

		mainLog.println("\nInitialising THTS");
		fileLog.println("\nInitialising THTS");
		boolean doForwardBackup = true;
		TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) mdpModGen, maxRollouts, trialLen,
				heuristicFunction, actionSelection, outcomeSelection, rewardH, backupFunction, doForwardBackup,
				tieBreakingOrder, mainLog, fileLog);

		mainLog.println("\nBeginning THTS");
		fileLog.println("\nBeginning THTS");
		thts.setName(example + "g_" + stateVal + "_rtdp" + combString);
		thts.setResultsLocation(resultsLocation);
		thts.run(false);

		mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
		fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");

		boolean[] goalack = thts.runThrough(new ActionSelectorGreedyLowerBound(tieBreakingOrder, true),
				resultsLocation);
		return goalack;
	}

	public MultiAgentNestedProductModelGenerator createMAMG(Prism prism, PrismLog mainLog, ArrayList<String> filenames,
			String propertiesFileName, String resultsLocation, boolean hasSharedState)
			throws PrismException, FileNotFoundException {

		AcceptanceType[] allowedAcceptance = { AcceptanceType.RABIN, AcceptanceType.REACH };

		// step 1
		// create the modulesfilemodelgenerators
		ArrayList<ModulesFileModelGenerator> mfmodgens = new ArrayList<>();
		ModulesFile modulesFile = null; // just here so we can use the last modules file for our properties

		for (String modelFileName : filenames) {
			mainLog.println("Loading model gen for " + modelFileName);
			modulesFile = prism.parseModelFile(new File(modelFileName)); // because the models are uniform
			// we might have to find a way to change this later
			ModulesFileModelGenerator modGen = new ModulesFileModelGenerator(modulesFile, prism);
			mfmodgens.add(modGen);
		}
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
		Expression notsafetyexpr = Expression.Not(safetyexpr);
		safetydaind = processedExprs.size();
		processedExprs.add(safetyexpr);

		mainLog.println("Properties " + processedExprs.toString());
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

	boolean[] nestedLRTDPDoorsAvoidProductNestedSingleAgentTaskCompletion(boolean debug) throws Exception {
		boolean goalFound = false;
		double[] hvals = { 20, 50, 1000, 200, 500, 1000, 2000, 10000};
		double hval = 20;// trialLen;//trialLen*maxRollouts;
		boolean[] goalack = new boolean[2];

		for (int hvalnum = 0; hvalnum < hvals.length; hvalnum++) {
			hval = hvals[hvalnum];
			if (goalFound)
				break;
			System.out.println(System.getProperty("user.dir"));
			String currentDir = System.getProperty("user.dir");
			String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
			String resultsLocation = testsLocation + "results/";
			// making sure resultsloc exits
			createDirIfNotExist(resultsLocation);
			System.out.println("Results Location " + resultsLocation);

			String example = "tro_example_new_small";

//		example = "tro_example_new_small_allfailpaths";//"tro_example_new_small_onefailaction";//"tro_example_new_small_noprob";
			boolean hasSharedState = false;
			PrismLog mainLog;
			if (debug)
				mainLog = new PrismFileLog("stdout");
			else
				mainLog = new PrismDevNullLog();

			Prism prism = new Prism(mainLog);
			String combString = "_prob_cost_lrtdp_avoid_prod_ltl_specs_taskcomp_costcutoff_"+hval;
			String algoIden = "rtdp" + combString;
			PrismLog fileLog = new PrismFileLog(
					resultsLocation + "log_" + example + "_ltl_specs_taskcomp_costcutoff_" +hval+ "_" + algoIden + "_justmdp" + ".txt");//

			prism.initialise();
			prism.setEngine(Prism.EXPLICIT);

			mainLog.println("Initialised Prism");

			// create a single agent model generator first

			AcceptanceType[] allowedAcceptance = { AcceptanceType.RABIN, AcceptanceType.REACH };
//
			List<Expression> labelExprs = new ArrayList<Expression>();

			String modelFileName = testsLocation + example + "0.prism";
			ModulesFile modulesFile = prism.parseModelFile(new File(modelFileName)); // because the models are uniform
			String propertiesFileName = testsLocation + example + "_mult.prop";

			ArrayList<String> filenames = new ArrayList<>();
			filenames.add(modelFileName);
			// so we want to create a single agent model generator
			// good test
			PropertiesFile propertiesFile = prism.parsePropertiesFile(modulesFile, new File(propertiesFileName));

			LTLModelChecker ltlMC = new LTLModelChecker(prism);

			ExpressionReward rewExpr = null;
			Expression safetyExpr = null;
			ArrayList<Expression> otherExpressions = new ArrayList<Expression>();
			// assumption a safety expression can not be a reward expression

			List<Expression> processedExprs = new ArrayList<Expression>();
			for (int i = 0; i < propertiesFile.getNumProperties(); i++) {

				boolean isSafeExpr = false;
				Expression exprHere = propertiesFile.getProperty(i);
				if (exprHere instanceof ExpressionReward)
					rewExpr = (ExpressionReward) exprHere;
				else {
					Expression daExpr = ((ExpressionQuant) exprHere).getExpression();
					isSafeExpr = !Expression.isCoSafeLTLSyntactic(daExpr, true);
					if (isSafeExpr)
						safetyExpr = daExpr;
					else
						otherExpressions.add(exprHere);
				}
				if (!isSafeExpr)
					processedExprs.add(((ExpressionQuant) exprHere).getExpression());
			}

			otherExpressions.add(((ExpressionQuant) rewExpr).getExpression());
			otherExpressions.add(safetyExpr);

			ArrayList<List<Expression>> labelExprsList = new ArrayList<List<Expression>>();
			ArrayList<DA<BitSet, ? extends AcceptanceOmega>> das = new ArrayList<DA<BitSet, ? extends AcceptanceOmega>>();

			DA<BitSet, ? extends AcceptanceOmega> da;
			for (int i = 0; i < processedExprs.size(); i++) {
				labelExprs = new ArrayList<Expression>();

				Expression expr = (Expression) processedExprs.get(i);
				expr = (Expression) expr.expandPropRefsAndLabels(propertiesFile, modulesFile.getLabelList());
				da = ltlMC.constructExpressionDAForLTLFormula(expr, labelExprs, allowedAcceptance);
				da.setDistancesToAcc();
				PrismLog out = new PrismFileLog(resultsLocation + "da_" + i + ".dot");
				// printing the da
				da.print(out, "dot");
				out.close();
				labelExprsList.add(labelExprs);
				das.add(da);
			}
			// lastly the safety expr
			Expression expr = Expression.Not(safetyExpr);
			expr = (Expression) expr.expandPropRefsAndLabels(propertiesFile, modulesFile.getLabelList());
			labelExprs = new ArrayList<Expression>();
			da = ltlMC.constructExpressionDAForLTLFormula(expr, labelExprs, allowedAcceptance);
			da.setDistancesToAcc();
			PrismLog out = new PrismFileLog(resultsLocation + "da_safety.dot");
			// printing the da
			da.print(out, "dot");
			out.close();
			labelExprsList.add(labelExprs);
			das.add(da);

			ModulesFileModelGenerator modGen = new ModulesFileModelGenerator(modulesFile, prism);

			NestedProductModelGenerator saModelGen = new NestedProductModelGenerator(modGen, das, labelExprsList,
					das.size() - 1);

			int maxRollouts = 1000;
			int trialLen = 50;
			float epsilon = 0.0001f;

			List<State> gs = new ArrayList<State>();
			List<State> deadend = new ArrayList<State>();

			int[] deadendvals = { -1, 5 };

			for (int j = -1; j < 2; j++) {
				for (int deadendval : deadendvals) {
					for (int i1 = 0; i1 < 2; i1++) {
						for (int i2 = 0; i2 < 2; i2++) {
							for (int i3 = 0; i3 < 2; i3++) {

								State de1 = new State(5);
								// State de1 = new State(2);
								de1.setValue(0, deadendval);
								de1.setValue(1, j);
								de1.setValue(2, i1);
								de1.setValue(3, i2);
								de1.setValue(4, i3);
								deadend.add(de1);
							}
						}
					}
				}
			}

			Heuristic heuristicFunction = new EmptyNestedSingleAgentHeuristic(saModelGen, gs, deadend, hval);
			ArrayList<Objectives> tieBreakingOrder = new ArrayList<Objectives>();
//		tieBreakingOrder.add(Objectives.Probability);
			tieBreakingOrder.add(Objectives.TaskCompletion);
			tieBreakingOrder.add(Objectives.Cost);

			mainLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
			fileLog.println("Tie Breaking Order " + tieBreakingOrder.toString());

			mainLog.println("Initialising Greedy Bounds Difference Action Selector Function");
			fileLog.println("Initialising Greedy Bounds Difference Action Selector Function");

			ActionSelector actionSelection = new ActionSelectorGreedyLowerBound(tieBreakingOrder, true);// new
																										// ActionSelectorGreedyBoundsDiff(tieBreakingOrder);

			mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
			fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

			OutcomeSelector outcomeSelection = new OutcomeSelectorRandom();

			mainLog.println("Initialising Full Bellman Backup Function");
			fileLog.println("Initialising Full Bellman Backup Function");

			BackupNVI backupFunction = new BackupLabelledFullBelmanCap(tieBreakingOrder, actionSelection, epsilon,
					hval);

			mainLog.println("Initialising Reward Helper Function");
			fileLog.println("Initialising Reward Helper Function");

			RewardHelper rewardH = new RewardHelperNestedSingleAgent(saModelGen);

			mainLog.println("Max Rollouts: " + maxRollouts);
			mainLog.println("Max TrialLen: " + trialLen);
			fileLog.println("Max Rollouts: " + maxRollouts);
			fileLog.println("Max TrialLen: " + trialLen);

			mainLog.println("\nInitialising THTS");
			fileLog.println("\nInitialising THTS");
			boolean doForwardBackup = true;
			TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) saModelGen, maxRollouts,
					trialLen, heuristicFunction, actionSelection, outcomeSelection, rewardH, backupFunction,
					doForwardBackup, tieBreakingOrder, mainLog, fileLog);

			mainLog.println("\nBeginning THTS");
			fileLog.println("\nBeginning THTS");
			thts.setName(example + "_rtdp" + combString);
			thts.setResultsLocation(resultsLocation);
//			thts.run(true);
		thts.run(false);

			mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
			fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");

			goalack = thts.runThrough(new ActionSelectorGreedyLowerBound(tieBreakingOrder, true), resultsLocation);
			goalFound = goalack[0];
			mainLog.println("Goal Found: " + goalack[0]);
			mainLog.println("Initial State Solved: " + goalack[1]);

		}
		return goalack;
	}

	boolean[] nestedLRTDPDoorsAvoidProductSingleAgent(int stateVal, boolean debug) throws Exception {
		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/";
		// making sure resultsloc exits
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);

		String example = "tro_example_new_small";

//		example = "tro_example_new_small_allfailpaths";//"tro_example_new_small_onefailaction";//"tro_example_new_small_noprob";
		boolean hasSharedState = false;
		PrismLog mainLog;
		if (debug)
			mainLog = new PrismFileLog("stdout");
		else
			mainLog = new PrismDevNullLog();

		Prism prism = new Prism(mainLog);
		String combString = "_prob_cost_lrtdp_avoid_prod";
		String algoIden = "rtdp" + combString;
		PrismLog fileLog = new PrismFileLog(
				resultsLocation + "log_" + example + "_g_" + stateVal + "_" + algoIden + "_justmdp" + ".txt");//

		prism.initialise();
		prism.setEngine(Prism.EXPLICIT);

		mainLog.println("Initialised Prism");

		// create a single agent model generator first

		AcceptanceType[] allowedAcceptance = { AcceptanceType.RABIN, AcceptanceType.REACH };
//
		List<Expression> labelExprs = new ArrayList<Expression>();

		String modelFileName = testsLocation + example + "0.prism";
		ModulesFile modulesFile = prism.parseModelFile(new File(modelFileName)); // because the models are uniform
		String propertiesFileName = testsLocation + example + ".prop";

		ArrayList<String> filenames = new ArrayList<>();
		filenames.add(modelFileName);
		// so we want to create a single agent model generator
		// good test
		PropertiesFile propertiesFile = prism.parsePropertiesFile(modulesFile, new File(propertiesFileName));

		// the assumption is that its a reward expr for now
		Expression exprHere = propertiesFile.getProperty(0);
		ExpressionReward exprRew = (ExpressionReward) exprHere;
		Expression expr = exprRew.getExpression();
		ModulesFileModelGenerator modGen = new ModulesFileModelGenerator(modulesFile, prism);
		LTLModelChecker ltlMC = new LTLModelChecker(prism);

		DA<BitSet, ? extends AcceptanceOmega> da = ltlMC.constructExpressionDAForLTLFormula(expr, // .getExpression(),
				labelExprs, allowedAcceptance);

		da.printDot(System.out);
		SingleAgentProductModelGenerator saModelGen = new SingleAgentProductModelGenerator(modGen, da, labelExprs);
//		MDPModelGenerator mdpModGen = new MDPModelGenerator(modGen);

		int maxRollouts = 1000;
		int trialLen = 50;
		float epsilon = 0.0001f;

		double hval = 20;

		List<State> gs = new ArrayList<State>();
		List<State> deadend = new ArrayList<State>();
		for (int j = -1; j < 2; j++) {
			for (int i = 1; i < 2; i++) {

				State goalState1 = new State(3);
				// State goalState1 = new State(2);
				goalState1.setValue(0, stateVal);
				goalState1.setValue(1, j);
				goalState1.setValue(2, i);
				gs.add(goalState1);
			}
		}
		int[] deadendvals = { -1, 5 };
		for (int j = -1; j < 2; j++) {
			for (int deadendval : deadendvals) {
				for (int i = 1; i < 4; i++) {
					State de1 = new State(3);
					// State de1 = new State(2);
					de1.setValue(0, deadendval);
					de1.setValue(1, j);
					de1.setValue(2, i);
					deadend.add(de1);
				}
			}
		}

		Heuristic heuristicFunction = new EmptySingleAgentHeuristic(saModelGen, gs, deadend, hval);
		// new EmptyHeuristic(gs, deadend, hval);
		ArrayList<Objectives> tieBreakingOrder = new ArrayList<Objectives>();
		tieBreakingOrder.add(Objectives.Probability);
		tieBreakingOrder.add(Objectives.Cost);

		mainLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
		fileLog.println("Tie Breaking Order " + tieBreakingOrder.toString());

		mainLog.println("Initialising Greedy Bounds Difference Action Selector Function");
		fileLog.println("Initialising Greedy Bounds Difference Action Selector Function");

		ActionSelector actionSelection = new ActionSelectorGreedyLowerBound(tieBreakingOrder, true);// new
																									// ActionSelectorGreedyBoundsDiff(tieBreakingOrder);

		mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
		fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

		OutcomeSelector outcomeSelection = new OutcomeSelectorRandom();

		mainLog.println("Initialising Full Bellman Backup Function");
		fileLog.println("Initialising Full Bellman Backup Function");

		BackupNVI backupFunction = new BackupLabelledFullBelman(tieBreakingOrder, actionSelection, epsilon);

		mainLog.println("Initialising Reward Helper Function");
		fileLog.println("Initialising Reward Helper Function");

		RewardHelper rewardH = new RewardHelperMDP(saModelGen);

		mainLog.println("Max Rollouts: " + maxRollouts);
		mainLog.println("Max TrialLen: " + trialLen);
		fileLog.println("Max Rollouts: " + maxRollouts);
		fileLog.println("Max TrialLen: " + trialLen);

		mainLog.println("\nInitialising THTS");
		fileLog.println("\nInitialising THTS");
		boolean doForwardBackup = true;
		TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) saModelGen, maxRollouts, trialLen,
				heuristicFunction, actionSelection, outcomeSelection, rewardH, backupFunction, doForwardBackup,
				tieBreakingOrder, mainLog, fileLog);

		mainLog.println("\nBeginning THTS");
		fileLog.println("\nBeginning THTS");
		thts.setName(example + "g_" + stateVal + "_rtdp" + combString);
		thts.setResultsLocation(resultsLocation);
		thts.run(false);

		mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
		fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");

		boolean[] goalack = thts.runThrough(new ActionSelectorGreedyLowerBound(tieBreakingOrder, true),
				resultsLocation);
		mainLog.println("Goal Found: " + goalack[0]);
		mainLog.println("Initial State Solved: " + goalack[1]);
		return goalack;
	}

	boolean[] nestedLRTDPDoorsAvoid(int stateVal, boolean debug) throws Exception {
		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/";
		// making sure resultsloc exits
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);

		String example = "tro_example_new_small";

		PrismLog mainLog;
		if (debug)
			mainLog = new PrismFileLog("stdout");
		else
			mainLog = new PrismDevNullLog();

		Prism prism = new Prism(mainLog);
		String combString = "_prob_cost_lrtdp_avoid";
		String algoIden = "rtdp" + combString;
		PrismLog fileLog = new PrismFileLog(
				resultsLocation + "log_" + example + "_g_" + stateVal + "_" + algoIden + "_justmdp" + ".txt");//

		prism.initialise();
		prism.setEngine(Prism.EXPLICIT);

		mainLog.println("Initialised Prism");

		String modelFileName = testsLocation + example + "0.prism";
		ModulesFile modulesFile = prism.parseModelFile(new File(modelFileName)); // because the models are uniform

		ModulesFileModelGenerator modGen = new ModulesFileModelGenerator(modulesFile, prism);
		MDPModelGenerator mdpModGen = new MDPModelGenerator(modGen);

		int maxRollouts = 1000;
		int trialLen = 50;
		float epsilon = 0.0001f;

		List<State> gs = new ArrayList<State>();
		List<State> deadend = new ArrayList<State>();
		for (int i = -1; i < 2; i++) {

			State goalState1 = new State(2);
			goalState1.setValue(0, stateVal);
			goalState1.setValue(1, i);
			gs.add(goalState1);
		}

		int[] deadendvals = { -1, 5 };
		for (int deadendval : deadendvals) {
			for (int i = -1; i < 2; i++) {
				State de1 = new State(2);
				de1.setValue(0, deadendval);
				de1.setValue(1, i);
				deadend.add(de1);
			}
		}
		double hval = 20;
		Heuristic heuristicFunction = new EmptyHeuristic(gs, deadend, hval);
		ArrayList<Objectives> tieBreakingOrder = new ArrayList<Objectives>();
		tieBreakingOrder.add(Objectives.Probability);
		tieBreakingOrder.add(Objectives.Cost);

		mainLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
		fileLog.println("Tie Breaking Order " + tieBreakingOrder.toString());

		mainLog.println("Initialising Greedy Bounds Difference Action Selector Function");
		fileLog.println("Initialising Greedy Bounds Difference Action Selector Function");

		ActionSelector actionSelection = new ActionSelectorGreedyLowerBound(tieBreakingOrder, true);// new
																									// ActionSelectorGreedyBoundsDiff(tieBreakingOrder);

		mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
		fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

		OutcomeSelector outcomeSelection = new OutcomeSelectorRandom();

		mainLog.println("Initialising Full Bellman Backup Function");
		fileLog.println("Initialising Full Bellman Backup Function");

		BackupNVI backupFunction = new BackupLabelledFullBelman(tieBreakingOrder, actionSelection, epsilon);

		mainLog.println("Initialising Reward Helper Function");
		fileLog.println("Initialising Reward Helper Function");

		RewardHelper rewardH = new RewardHelperMDP(mdpModGen);

		mainLog.println("Max Rollouts: " + maxRollouts);
		mainLog.println("Max TrialLen: " + trialLen);
		fileLog.println("Max Rollouts: " + maxRollouts);
		fileLog.println("Max TrialLen: " + trialLen);

		mainLog.println("\nInitialising THTS");
		fileLog.println("\nInitialising THTS");
		boolean doForwardBackup = true;
		TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) mdpModGen, maxRollouts, trialLen,
				heuristicFunction, actionSelection, outcomeSelection, rewardH, backupFunction, doForwardBackup,
				tieBreakingOrder, mainLog, fileLog);

		mainLog.println("\nBeginning THTS");
		fileLog.println("\nBeginning THTS");
		thts.setName(example + "g_" + stateVal + "_rtdp" + combString);
		thts.setResultsLocation(resultsLocation);
		thts.run(false);

		mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
		fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");

		boolean[] goalack = thts.runThrough(new ActionSelectorGreedyLowerBound(tieBreakingOrder, true),
				resultsLocation);
		return goalack;
	}

	boolean[] nestedLRTDP(String example, int stateVal, boolean debug) throws Exception {

//		if (example.contains("tro_example_new_small_allfailpaths") & stateVal == 6) {
//			System.out.println("Debug");
//			debug = true;
//		}
		boolean isSolved = false;
		int maxRollouts = 5000;
		int minRollouts = 1000;
		boolean[] goalack = null;
		int numRollouts = minRollouts;
//		for (int numRollouts = minRollouts; numRollouts < maxRollouts; numRollouts += 1000) {
		int trialLen = 50;
		float epsilon = 0.0001f;

		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/";
		// making sure resultsloc exits
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);

//		String example = "tro_example_new_small_noprob";

		PrismLog mainLog;
		if (debug)
			mainLog = new PrismFileLog("stdout");
		else
			mainLog = new PrismDevNullLog();

		Prism prism = new Prism(mainLog);
		String combString = "_prob_cost";
		String algoIden = "rtdp" + combString;
		PrismLog fileLog = new PrismFileLog(
				resultsLocation + "log_" + example + "_g_" + stateVal + "_" + algoIden + "_justmdp" + ".txt");//

		prism.initialise();
		prism.setEngine(Prism.EXPLICIT);

		mainLog.println("Initialised Prism");

		String modelFileName = testsLocation + example + "0.prism";
		ModulesFile modulesFile = prism.parseModelFile(new File(modelFileName)); // because the models are uniform

		ModulesFileModelGenerator modGen = new ModulesFileModelGenerator(modulesFile, prism);
		MDPModelGenerator mdpModGen = new MDPModelGenerator(modGen);

		List<State> gs = new ArrayList<State>();
		List<State> deadend = new ArrayList<State>();
		State goalState1 = new State(1);
		goalState1.setValue(0, stateVal);
		gs.add(goalState1);
		State de1 = new State(1);
		de1.setValue(0, -1);
		deadend.add(de1);

		Heuristic heuristicFunction = new EmptyHeuristic(gs, deadend);

		ArrayList<Objectives> tieBreakingOrder = new ArrayList<Objectives>();
		tieBreakingOrder.add(Objectives.Probability);
		tieBreakingOrder.add(Objectives.Cost);

		mainLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
		fileLog.println("Tie Breaking Order " + tieBreakingOrder.toString());

		mainLog.println("Initialising Greedy Bounds Difference Action Selector Function");
		fileLog.println("Initialising Greedy Bounds Difference Action Selector Function");

		ActionSelector actionSelection = new ActionSelectorGreedyLowerBound(tieBreakingOrder, true);// new
																									// ActionSelectorGreedyBoundsDiff(tieBreakingOrder);

		mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
		fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

		OutcomeSelector outcomeSelection = new OutcomeSelectorRandom();

		mainLog.println("Initialising Full Bellman Backup Function");
		fileLog.println("Initialising Full Bellman Backup Function");

		BackupNVI backupFunction = new BackupLabelledFullBelman(tieBreakingOrder, actionSelection, epsilon);

		mainLog.println("Initialising Reward Helper Function");
		fileLog.println("Initialising Reward Helper Function");

		RewardHelper rewardH = new RewardHelperMDP(mdpModGen);

		mainLog.println("Max Rollouts: " + numRollouts);
		mainLog.println("Max TrialLen: " + trialLen);
		fileLog.println("Max Rollouts: " + numRollouts);
		fileLog.println("Max TrialLen: " + trialLen);

		mainLog.println("\nInitialising THTS");
		fileLog.println("\nInitialising THTS");
		boolean doForwardBackup = true;
		TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) mdpModGen, numRollouts, trialLen,
				heuristicFunction, actionSelection, outcomeSelection, rewardH, backupFunction, doForwardBackup,
				tieBreakingOrder, mainLog, fileLog);

		mainLog.println("\nBeginning THTS");
		fileLog.println("\nBeginning THTS");
		thts.setName(example + "g_" + stateVal + "_rtdp" + combString);
		thts.setResultsLocation(resultsLocation);
		thts.run(false);

		mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
		fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");

		goalack = thts.runThrough(new ActionSelectorGreedyLowerBound(tieBreakingOrder, true), resultsLocation);
		isSolved = goalack[1];
//			if (isSolved)
//				break;
//		}
		return goalack;

	}

	boolean[] nestedLRTDPAvoid(String example, int stateVal, boolean debug) throws Exception {

//		if (example.contains("tro_example_new_small_allfailpaths") & stateVal == 6) {
//			System.out.println("Debug");
//			debug = true;
//		}
		debug = true;
		boolean isSolved = false;
		int maxRollouts = 5000;
		int minRollouts = 1000;
		boolean[] goalack = null;
		int numRollouts = minRollouts;
//		for (int numRollouts = minRollouts; numRollouts < maxRollouts; numRollouts += 1000) {
		int trialLen = 50;
		float epsilon = 0.0001f;

		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/";
		// making sure resultsloc exits
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);

//		String example = "tro_example_new_small_noprob";

		PrismLog mainLog;
		if (debug)
			mainLog = new PrismFileLog("stdout");
		else
			mainLog = new PrismDevNullLog();

		Prism prism = new Prism(mainLog);
		String combString = "_prob_cost_avoid";
		String algoIden = "rtdp" + combString;
		PrismLog fileLog = new PrismFileLog(
				resultsLocation + "log_" + example + "_g_" + stateVal + "_" + algoIden + "_justmdp" + ".txt");//

		prism.initialise();
		prism.setEngine(Prism.EXPLICIT);

		mainLog.println("Initialised Prism");

		String modelFileName = testsLocation + example + "0.prism";
		ModulesFile modulesFile = prism.parseModelFile(new File(modelFileName)); // because the models are uniform

		ModulesFileModelGenerator modGen = new ModulesFileModelGenerator(modulesFile, prism);
		MDPModelGenerator mdpModGen = new MDPModelGenerator(modGen);

		List<State> gs = new ArrayList<State>();
		List<State> deadend = new ArrayList<State>();
		State goalState1 = new State(1);
		goalState1.setValue(0, stateVal);
		gs.add(goalState1);
		State de1 = new State(1);
		de1.setValue(0, -1);
		deadend.add(de1);
		de1 = new State(1);
		de1.setValue(0, 5);
		deadend.add(de1);

		double hval = 20;
		Heuristic heuristicFunction = new EmptyHeuristic(gs, deadend, hval);

		ArrayList<Objectives> tieBreakingOrder = new ArrayList<Objectives>();
		tieBreakingOrder.add(Objectives.Probability);
		tieBreakingOrder.add(Objectives.Cost);

		mainLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
		fileLog.println("Tie Breaking Order " + tieBreakingOrder.toString());

		mainLog.println("Initialising Greedy Bounds Difference Action Selector Function");
		fileLog.println("Initialising Greedy Bounds Difference Action Selector Function");

		ActionSelector actionSelection = new ActionSelectorGreedyLowerBound(tieBreakingOrder, true);// new
																									// ActionSelectorGreedyBoundsDiff(tieBreakingOrder);

		mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
		fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

		OutcomeSelector outcomeSelection = new OutcomeSelectorRandom();

		mainLog.println("Initialising Full Bellman Backup Function");
		fileLog.println("Initialising Full Bellman Backup Function");

		BackupNVI backupFunction = new BackupLabelledFullBelman(tieBreakingOrder, actionSelection, epsilon);

		mainLog.println("Initialising Reward Helper Function");
		fileLog.println("Initialising Reward Helper Function");

		RewardHelper rewardH = new RewardHelperMDP(mdpModGen);

		mainLog.println("Max Rollouts: " + numRollouts);
		mainLog.println("Max TrialLen: " + trialLen);
		fileLog.println("Max Rollouts: " + numRollouts);
		fileLog.println("Max TrialLen: " + trialLen);

		mainLog.println("\nInitialising THTS");
		fileLog.println("\nInitialising THTS");
		boolean doForwardBackup = true;
		TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) mdpModGen, numRollouts, trialLen,
				heuristicFunction, actionSelection, outcomeSelection, rewardH, backupFunction, doForwardBackup,
				tieBreakingOrder, mainLog, fileLog);

		mainLog.println("\nBeginning THTS");
		fileLog.println("\nBeginning THTS");
		thts.setName(example + "g_" + stateVal + "_rtdp" + combString);
		thts.setResultsLocation(resultsLocation);
		thts.run(false);

		mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
		fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");

		goalack = thts.runThrough(new ActionSelectorGreedyLowerBound(tieBreakingOrder, true), resultsLocation);
		isSolved = goalack[1];
//			if (isSolved)
//				break;
//		}
		return goalack;

	}

	boolean[] simpleLRTDPNoDeadends(String example, int stateVal, boolean debug) throws Exception {

		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/";
		// making sure resultsloc exits
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);

		PrismLog mainLog;
		if (debug)
			mainLog = new PrismFileLog("stdout");
		else
			mainLog = new PrismDevNullLog();

		Prism prism = new Prism(mainLog);
		String combString = "_cost_nodeadends";
		String algoIden = "rtdp" + combString;
		PrismLog fileLog = new PrismFileLog(
				resultsLocation + "log_" + example + "_g_" + stateVal + "_" + algoIden + "_justmdp" + ".txt");//

		prism.initialise();
		prism.setEngine(Prism.EXPLICIT);

		mainLog.println("Initialised Prism");

		String modelFileName = testsLocation + example + "0.prism";
		ModulesFile modulesFile = prism.parseModelFile(new File(modelFileName)); // because the models are uniform

		ModulesFileModelGenerator modGen = new ModulesFileModelGenerator(modulesFile, prism);
		MDPModelGenerator mdpModGen = new MDPModelGenerator(modGen);

		int maxRollouts = 1000;
		int trialLen = 100;
		float epsilon = 0.0001f;

		List<State> gs = new ArrayList<State>();
		State goalState1 = new State(1);
		goalState1.setValue(0, stateVal);
		gs.add(goalState1);

		Heuristic heuristicFunction = new EmptyHeuristic(gs, null);

		ArrayList<Objectives> tieBreakingOrder = new ArrayList<Objectives>();
		tieBreakingOrder.add(Objectives.Cost);

		mainLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
		fileLog.println("Tie Breaking Order " + tieBreakingOrder.toString());

		mainLog.println("Initialising Greedy Bounds Difference Action Selector Function");
		fileLog.println("Initialising Greedy Bounds Difference Action Selector Function");

		ActionSelector actionSelection = new ActionSelectorGreedyLowerBound(tieBreakingOrder, true);// new
																									// ActionSelectorGreedyBoundsDiff(tieBreakingOrder);

		mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
		fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

		OutcomeSelector outcomeSelection = new OutcomeSelectorRandom();

		mainLog.println("Initialising Full Bellman Backup Function");
		fileLog.println("Initialising Full Bellman Backup Function");

		BackupNVI backupFunction = new BackupLabelledFullBelman(tieBreakingOrder, actionSelection, epsilon);

		mainLog.println("Initialising Reward Helper Function");
		fileLog.println("Initialising Reward Helper Function");

		RewardHelper rewardH = new RewardHelperMDP(mdpModGen);

		mainLog.println("Max Rollouts: " + maxRollouts);
		mainLog.println("Max TrialLen: " + trialLen);
		fileLog.println("Max Rollouts: " + maxRollouts);
		fileLog.println("Max TrialLen: " + trialLen);

		mainLog.println("\nInitialising THTS");
		fileLog.println("\nInitialising THTS");
		boolean doForwardBackup = true;
		TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) mdpModGen, maxRollouts, trialLen,
				heuristicFunction, actionSelection, outcomeSelection, rewardH, backupFunction, doForwardBackup,
				tieBreakingOrder, mainLog, fileLog);

		mainLog.println("\nBeginning THTS");
		fileLog.println("\nBeginning THTS");
		thts.setName(example + "g_" + stateVal + "_rtdp" + combString);
		thts.setResultsLocation(resultsLocation);
		thts.run(false);

		mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
		fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");

		boolean[] goalack = thts.runThrough(new ActionSelectorGreedyLowerBound(tieBreakingOrder, true),
				resultsLocation);
		return goalack;

	}

	boolean[] simpleLRTDP(String example, int stateVal, boolean debug) throws Exception {

		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/";
		// making sure resultsloc exits
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);

//		String example = "tro_example_new_small_noprob";

		PrismLog mainLog;
		if (debug)
			mainLog = new PrismFileLog("stdout");
		else
			mainLog = new PrismDevNullLog();

		Prism prism = new Prism(mainLog);
		String combString = "_cost_deadends";
		String algoIden = "rtdp" + combString;
		PrismLog fileLog = new PrismFileLog(
				resultsLocation + "log_" + example + "_g_" + stateVal + "_" + algoIden + "_justmdp" + ".txt");//

		prism.initialise();
		prism.setEngine(Prism.EXPLICIT);

		mainLog.println("Initialised Prism");

		String modelFileName = testsLocation + example + "0.prism";
		ModulesFile modulesFile = prism.parseModelFile(new File(modelFileName)); // because the models are uniform

		ModulesFileModelGenerator modGen = new ModulesFileModelGenerator(modulesFile, prism);
		MDPModelGenerator mdpModGen = new MDPModelGenerator(modGen);

		int maxRollouts = 1000;
		int trialLen = 100;
		float epsilon = 0.0001f;

		List<State> gs = new ArrayList<State>();
		List<State> deadend = new ArrayList<State>();
		State goalState1 = new State(1);
		goalState1.setValue(0, stateVal);
		gs.add(goalState1);
		State de1 = new State(1);
		de1.setValue(0, -1);
		deadend.add(de1);

		Heuristic heuristicFunction = new EmptyHeuristic(gs, deadend);

		ArrayList<Objectives> tieBreakingOrder = new ArrayList<Objectives>();
		tieBreakingOrder.add(Objectives.Cost);

		mainLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
		fileLog.println("Tie Breaking Order " + tieBreakingOrder.toString());

		mainLog.println("Initialising Greedy Bounds Difference Action Selector Function");
		fileLog.println("Initialising Greedy Bounds Difference Action Selector Function");

		ActionSelector actionSelection = new ActionSelectorGreedyLowerBound(tieBreakingOrder, true);// new
																									// ActionSelectorGreedyBoundsDiff(tieBreakingOrder);

		mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
		fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

		OutcomeSelector outcomeSelection = new OutcomeSelectorRandom();

		mainLog.println("Initialising Full Bellman Backup Function");
		fileLog.println("Initialising Full Bellman Backup Function");

		BackupNVI backupFunction = new BackupLabelledFullBelman(tieBreakingOrder, actionSelection, epsilon);

		mainLog.println("Initialising Reward Helper Function");
		fileLog.println("Initialising Reward Helper Function");

		RewardHelper rewardH = new RewardHelperMDP(mdpModGen);

		mainLog.println("Max Rollouts: " + maxRollouts);
		mainLog.println("Max TrialLen: " + trialLen);
		fileLog.println("Max Rollouts: " + maxRollouts);
		fileLog.println("Max TrialLen: " + trialLen);

		mainLog.println("\nInitialising THTS");
		fileLog.println("\nInitialising THTS");
		boolean doForwardBackup = true;
		TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) mdpModGen, maxRollouts, trialLen,
				heuristicFunction, actionSelection, outcomeSelection, rewardH, backupFunction, doForwardBackup,
				tieBreakingOrder, mainLog, fileLog);

		mainLog.println("\nBeginning THTS");
		fileLog.println("\nBeginning THTS");
		thts.setName(example + "g_" + stateVal + "_rtdp" + combString);
		thts.setResultsLocation(resultsLocation);
		thts.run(false);

		mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
		fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");

		boolean[] goalack = thts.runThrough(new ActionSelectorGreedyLowerBound(tieBreakingOrder, true),
				resultsLocation);
		return goalack;

	}

	boolean[] simpleLRTDPAvoid(String example, int stateVal, boolean debug) throws Exception {

		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/";
		// making sure resultsloc exits
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);

//		String example = "tro_example_new_small_noprob";

		PrismLog mainLog;
		if (debug)
			mainLog = new PrismFileLog("stdout");
		else
			mainLog = new PrismDevNullLog();

		Prism prism = new Prism(mainLog);
		String combString = "_cost_deadends_avoid";
		String algoIden = "rtdp" + combString;
		PrismLog fileLog = new PrismFileLog(
				resultsLocation + "log_" + example + "_g_" + stateVal + "_" + algoIden + "_justmdp" + ".txt");//

		prism.initialise();
		prism.setEngine(Prism.EXPLICIT);

		mainLog.println("Initialised Prism");

		String modelFileName = testsLocation + example + "0.prism";
		ModulesFile modulesFile = prism.parseModelFile(new File(modelFileName)); // because the models are uniform

		ModulesFileModelGenerator modGen = new ModulesFileModelGenerator(modulesFile, prism);
		MDPModelGenerator mdpModGen = new MDPModelGenerator(modGen);

		int maxRollouts = 1000;
		int trialLen = 100;
		float epsilon = 0.0001f;

		List<State> gs = new ArrayList<State>();
		List<State> deadend = new ArrayList<State>();
		State goalState1 = new State(1);
		goalState1.setValue(0, stateVal);
		gs.add(goalState1);
		State de1 = new State(1);
		de1.setValue(0, -1);

		deadend.add(de1);
		de1 = new State(1);
		de1.setValue(0, 5);

		deadend.add(de1);

		double hval = 20;
		Heuristic heuristicFunction = new EmptyHeuristic(gs, deadend, hval);

		ArrayList<Objectives> tieBreakingOrder = new ArrayList<Objectives>();
		tieBreakingOrder.add(Objectives.Cost);

		mainLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
		fileLog.println("Tie Breaking Order " + tieBreakingOrder.toString());

		mainLog.println("Initialising Greedy Bounds Difference Action Selector Function");
		fileLog.println("Initialising Greedy Bounds Difference Action Selector Function");

		ActionSelector actionSelection = new ActionSelectorGreedyLowerBound(tieBreakingOrder, true);// new
																									// ActionSelectorGreedyBoundsDiff(tieBreakingOrder);

		mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
		fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

		OutcomeSelector outcomeSelection = new OutcomeSelectorRandom();

		mainLog.println("Initialising Full Bellman Backup Function");
		fileLog.println("Initialising Full Bellman Backup Function");

		BackupNVI backupFunction = new BackupLabelledFullBelman(tieBreakingOrder, actionSelection, epsilon);

		mainLog.println("Initialising Reward Helper Function");
		fileLog.println("Initialising Reward Helper Function");

		RewardHelper rewardH = new RewardHelperMDP(mdpModGen);

		mainLog.println("Max Rollouts: " + maxRollouts);
		mainLog.println("Max TrialLen: " + trialLen);
		fileLog.println("Max Rollouts: " + maxRollouts);
		fileLog.println("Max TrialLen: " + trialLen);

		mainLog.println("\nInitialising THTS");
		fileLog.println("\nInitialising THTS");
		boolean doForwardBackup = true;
		TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) mdpModGen, maxRollouts, trialLen,
				heuristicFunction, actionSelection, outcomeSelection, rewardH, backupFunction, doForwardBackup,
				tieBreakingOrder, mainLog, fileLog);

		mainLog.println("\nBeginning THTS");
		fileLog.println("\nBeginning THTS");
		thts.setName(example + "g_" + stateVal + "_rtdp" + combString);
		thts.setResultsLocation(resultsLocation);
		thts.run(false);

		mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
		fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");

		boolean[] goalack = thts.runThrough(new ActionSelectorGreedyLowerBound(tieBreakingOrder, true),
				resultsLocation);
		return goalack;

	}

	boolean[] gssp(boolean debug) throws Exception {

		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/";
		// making sure resultsloc exits
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);

		String example = "gssp_paper_example";
		int maxRollouts = 1000;
		int trialLen = 100;
		float epsilon = 0.0001f;
		int stateVal = 5;
		PrismLog mainLog;
		if (debug)
			mainLog = new PrismFileLog("stdout");
		else
			mainLog = new PrismDevNullLog();
		Prism prism = new Prism(mainLog);
		String combString = "_cost_fret_nodeadends";
		String algoIden = "rtdp" + combString;
		PrismLog fileLog = new PrismFileLog(
				resultsLocation + "log_" + example + "_g_" + stateVal + "_" + algoIden + "_justmdp" + ".txt");//

		prism.initialise();
		prism.setEngine(Prism.EXPLICIT);

		mainLog.println("Initialised Prism");

		String modelFileName = testsLocation + example + ".prism";
		ModulesFile modulesFile = prism.parseModelFile(new File(modelFileName)); // because the models are uniform

		ModulesFileModelGenerator modGen = new ModulesFileModelGenerator(modulesFile, prism);
		MDPModelGenerator mdpModGen = new MDPModelGenerator(modGen);

		List<State> gs = new ArrayList<State>();
		State goalState1 = new State(1);
		goalState1.setValue(0, stateVal);
		gs.add(goalState1);

		Heuristic heuristicFunction = new EmptyHeuristic(gs, null);

		ArrayList<Objectives> tieBreakingOrder = new ArrayList<Objectives>();
		tieBreakingOrder.add(Objectives.Cost);

		mainLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
		fileLog.println("Tie Breaking Order " + tieBreakingOrder.toString());

		mainLog.println("Initialising Greedy Bounds Difference Action Selector Function");
		fileLog.println("Initialising Greedy Bounds Difference Action Selector Function");

		ActionSelector actionSelection = new ActionSelectorGreedyLowerBound(tieBreakingOrder, true);// new
																									// ActionSelectorGreedyBoundsDiff(tieBreakingOrder);

		mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
		fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

		OutcomeSelector outcomeSelection = new OutcomeSelectorRandom();

		mainLog.println("Initialising Full Bellman Backup Function");
		fileLog.println("Initialising Full Bellman Backup Function");

		BackupNVI backupFunction = new BackupLabelledFullBelman(tieBreakingOrder, actionSelection, epsilon);

		mainLog.println("Initialising Reward Helper Function");
		fileLog.println("Initialising Reward Helper Function");

		RewardHelper rewardH = new RewardHelperGSSPPaper(mdpModGen);

		mainLog.println("Max Rollouts: " + maxRollouts);
		mainLog.println("Max TrialLen: " + trialLen);
		fileLog.println("Max Rollouts: " + maxRollouts);
		fileLog.println("Max TrialLen: " + trialLen);

		mainLog.println("\nInitialising THTS");
		fileLog.println("\nInitialising THTS");
		boolean doForwardBackup = true;
		TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) mdpModGen, maxRollouts, trialLen,
				heuristicFunction, actionSelection, outcomeSelection, rewardH, backupFunction, doForwardBackup,
				tieBreakingOrder, mainLog, fileLog);

		mainLog.println("\nBeginning THTS");
		fileLog.println("\nBeginning THTS");
		thts.setName(example + "g_" + stateVal + "_rtdp" + combString);
		thts.setResultsLocation(resultsLocation);
		boolean fixSCCs = true;
		thts.run(fixSCCs);

		mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
		fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");

		boolean[] goalack = thts.runThrough(new ActionSelectorGreedyLowerBound(tieBreakingOrder, true),
				resultsLocation);
		return goalack;

	}

}
