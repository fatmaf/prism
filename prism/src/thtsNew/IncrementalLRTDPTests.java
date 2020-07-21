package thtsNew;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import parser.State;
import parser.ast.ModulesFile;
import prism.DefaultModelGenerator;
import prism.Prism;
import prism.PrismDevNullLog;
import prism.PrismException;
import prism.PrismFileLog;
import prism.PrismLog;
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

	public static void main(String[] args) {
		try {
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

			tests = tests + goalStates.length * examples2.length;

			String[] examples3 = { "tro_example_new_small_allfailpaths_nowait", "tro_example_new_small_allfailpaths" };
			tests = tests + goalStates.length * examples3.length;

			int[] goalStates2 = { 0, 4, 6 };
			tests = tests + goalStates2.length;

			int currentTest = 1;

			IncrementalLRTDPTests tester = new IncrementalLRTDPTests();
			System.out.println("Test " + currentTest++ + "/" + tests);
			boolean[] goalFoundAndSolved = tester.gssp(debug);

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
					goalFoundAndSolved = tester.simpleLRTDPNoDeadends(example, g, debug);
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
					goalFoundAndSolved = tester.simpleLRTDP(example, g, debug);
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

			for (String example : examples3) {
				for (int g : goalStates) {
//with deadends
					System.out.println("Test " + currentTest++ + "/" + tests);
					goalFoundAndSolved = tester.nestedLRTDP(example, g, debug);
					if (goalFoundAndSolved[0])
						goalFound++;
					else
						goalsNotFound += example + " " + g + " nested lrtdp with deadends\n";
					if (goalFoundAndSolved[1])
						solved++;
					else
						notSolved += example + " " + g + " nested lrtdp with deadends\n";
					if (goalFoundAndSolved[0] && goalFoundAndSolved[1])
						passed++;

				}
			}

			for (int g : goalStates2) {
				// with deadends
				System.out.println("Test " + currentTest++ + "/" + tests);
				goalFoundAndSolved = tester.nestedLRTDPDoors(g, debug);
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
		String combString = "_cost_noh_nod_noprob";
		String algoIden = "rtdp" + combString;
		PrismLog fileLog = new PrismFileLog(resultsLocation + "log_" + example + "_" + algoIden + "_justmdp" + ".txt");//

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
		thts.setName("rtdp" + combString);
		thts.setResultsLocation(resultsLocation);
		thts.run(false);

		mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
		fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");

		boolean[] goalack = thts.runThrough(new ActionSelectorGreedyLowerBound(tieBreakingOrder, true),
				resultsLocation);
		return goalack;
	}

	boolean[] nestedLRTDP(String example, int stateVal, boolean debug) throws Exception {

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
		String combString = "_cost_noh_nod_noprob";
		String algoIden = "rtdp" + combString;
		PrismLog fileLog = new PrismFileLog(resultsLocation + "log_" + example + "_" + algoIden + "_justmdp" + ".txt");//

		prism.initialise();
		prism.setEngine(Prism.EXPLICIT);

		mainLog.println("Initialised Prism");

		String modelFileName = testsLocation + example + "0.prism";
		ModulesFile modulesFile = prism.parseModelFile(new File(modelFileName)); // because the models are uniform

		ModulesFileModelGenerator modGen = new ModulesFileModelGenerator(modulesFile, prism);
		MDPModelGenerator mdpModGen = new MDPModelGenerator(modGen);

		int maxRollouts = 2000;
		int trialLen = 50;
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
		thts.setName("rtdp" + combString);
		thts.setResultsLocation(resultsLocation);
		thts.run(false);

		mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
		fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");

		boolean[] goalack = thts.runThrough(new ActionSelectorGreedyLowerBound(tieBreakingOrder, true),
				resultsLocation);
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
		String combString = "_cost_noh_nod_noprob";
		String algoIden = "rtdp" + combString;
		PrismLog fileLog = new PrismFileLog(resultsLocation + "log_" + example + "_" + algoIden + "_justmdp" + ".txt");//

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
		thts.setName("rtdp" + combString);
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
		String combString = "_cost_noh_nod_noprob";
		String algoIden = "rtdp" + combString;
		PrismLog fileLog = new PrismFileLog(resultsLocation + "log_" + example + "_" + algoIden + "_justmdp" + ".txt");//

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
		thts.setName("rtdp" + combString);
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

		PrismLog mainLog;
		if (debug)
			mainLog = new PrismFileLog("stdout");
		else
			mainLog = new PrismDevNullLog();
		Prism prism = new Prism(mainLog);
		String combString = "_cost_noh_nod_noprob";
		String algoIden = "rtdp" + combString;
		PrismLog fileLog = new PrismFileLog(resultsLocation + "log_" + example + "_" + algoIden + "_justmdp" + ".txt");//

		prism.initialise();
		prism.setEngine(Prism.EXPLICIT);

		mainLog.println("Initialised Prism");

		String modelFileName = testsLocation + example + ".prism";
		ModulesFile modulesFile = prism.parseModelFile(new File(modelFileName)); // because the models are uniform

		ModulesFileModelGenerator modGen = new ModulesFileModelGenerator(modulesFile, prism);
		MDPModelGenerator mdpModGen = new MDPModelGenerator(modGen);

		int maxRollouts = 1000;
		int trialLen = 100;
		float epsilon = 0.0001f;
		int stateVal = 5;
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
		thts.setName("rtdp" + combString);
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
