package thts.Scratch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import parser.State;
import parser.ast.ModulesFile;
import prism.DefaultModelGenerator;
import prism.Prism;
import prism.PrismFileLog;
import prism.PrismLog;
import simulator.ModulesFileModelGenerator;
import thts.old.Objectives;
import thts.actionselector.ActionSelector;
import thts.actionselector.ActionSelectorGreedySimpleLowerBound;
import thts.backup.BackupLabelledFullBelman;
import thts.backup.BackupNVI;
import thts.heuristic.Heuristic;
import thts.modelgens.MDPModelGenerator;
import thts.OutcomeSelector.OutcomeSelector;
import thts.OutcomeSelector.OutcomeSelectorRandom;
import thts.RewardHelper.RewardHelper;
import thts.RewardHelper.RewardHelperMDP;
import thts.TreeSearch.TrialBasedTreeSearch;

public class TestJustMDP {

	public static void main(String[] args) {
		try {
			new TestJustMDP().run();
//			new TestJustMDP().gssp();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

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

	public void gssp() throws Exception {
		int goalFound = 0;

		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/";
		// making sure resultsloc exits
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);

		String example = "gssp_paper_example";
		String propertiesFileName = testsLocation + example + ".prop";

		PrismLog mainLog = new PrismFileLog("stdout");// new PrismDevNullLog();//new PrismFileLog("stdout");
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
		int trialLen = 500;
		double deadendCost = 0;// maxRollouts*trialLen;//1000;
		boolean dodeadends = false;
		double costH = maxRollouts * trialLen * 100;
		float epsilon = 0.0001f;
		// rtdp is simple
		// heuristic the same
		// action selection greedy on lower bound really
		// outcome selction probabilistic
		// update full bellman backup
		int stateVal = 5;
		List<State> gs = new ArrayList<State>();
		State goalState1 = new State(1);
		goalState1.setValue(0, stateVal);
		gs.add(goalState1);

		Heuristic heuristicFunction = new EmptyHeuristic(gs, null);// new EmptyHeuristic();//new
																	// MultiAgentHeuristic(mapmg,singleAgentSolutions);

		// lets see if we can get a heuristic for each state in the model
		// from the initial state
		// TODO: check results from nvi
		// TODO: check state mapping for heuristic function
//	this.testMAPMG(mapmg);
//	testMultiAgentH(mapmg, heuristicFunction);
		ArrayList<Objectives> tieBreakingOrder = new ArrayList<Objectives>();
//	tieBreakingOrder.add(Objectives.TaskCompletion);
//	tieBreakingOrder.add(Objectives.Probability);
		tieBreakingOrder.add(Objectives.Cost);

//	tieBreakingOrder.add(Objectives.Probability); // really just here so I can get this too

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

		BackupNVI backupFunction = new BackupLabelledFullBelman(tieBreakingOrder, actionSelection, epsilon);

		mainLog.println("Initialising Reward Helper Function");
		fileLog.println("Initialising Reward Helper Function");

		RewardHelper rewardH = new RewardHelperGSSPPaper(mdpModGen);

		mainLog.println("Max Rollouts: " + maxRollouts);
		mainLog.println("Max TrialLen: " + trialLen);
		fileLog.println("Max Rollouts: " + maxRollouts);
		fileLog.println("Max TrialLen: " + trialLen);

		//
//	MultiAgentNestedProductModelGenerator mapmg = createMAMG(prism, mainLog, filenames, propertiesFileName,
//			resultsLocation);
//
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

		//boolean[] goalack = thtsNew.thts.runThrough(new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder,false), resultsLocation);
//		if (goalack[0])
//			goalFound++;


	}

	public void run() throws Exception {
		int goalFound = 0;
		int maxruns = 5;
		for (int run = 0; run < maxruns; run++) {
			System.out.println(System.getProperty("user.dir"));
			String currentDir = System.getProperty("user.dir");
			String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
			String resultsLocation = testsLocation + "results/";
			// making sure resultsloc exits
			createDirIfNotExist(resultsLocation);
			System.out.println("Results Location " + resultsLocation);

			String example = "tro_example_new_small_onefailaction";
//		example="tro_example_new_small_noprob";
			example = "tro_example_new_small_allfailpaths";
//			example = "tro_example_new_small_allfailpaths_nowait";
			example = "tro_example_new_small";

			String propertiesFileName = testsLocation + example + ".prop";

			PrismLog mainLog = new PrismFileLog("stdout"); // new PrismDevNullLog();// new PrismFileLog("stdout");
			Prism prism = new Prism(mainLog);
			String combString = "_cost_noh_nod_noprob";
			String algoIden = "rtdp" + combString;
			PrismLog fileLog = new PrismFileLog(
					resultsLocation + "log_" + example + "_" + algoIden + "_justmdp" + ".txt");//

			prism.initialise();
			prism.setEngine(Prism.EXPLICIT);

			mainLog.println("Initialised Prism");

			String modelFileName = testsLocation + example + "0.prism";
			ModulesFile modulesFile = prism.parseModelFile(new File(modelFileName)); // because the models are uniform

			ModulesFileModelGenerator modGen = new ModulesFileModelGenerator(modulesFile, prism);
			MDPModelGenerator mdpModGen = new MDPModelGenerator(modGen);

			int maxRollouts = 1000;
			int trialLen = 500;
			double deadendCost = 0;// maxRollouts*trialLen;//1000;
			boolean dodeadends = false;
			double costH = maxRollouts * trialLen * 100;
			float epsilon = 0.0001f;
			// rtdp is simple
			// heuristic the same
			// action selection greedy on lower bound really
			// outcome selction probabilistic
			// update full bellman backup
			int stateVal = 4;
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
			
//			State goalState1 = new State(1);
//			goalState1.setValue(0, stateVal);
//			gs.add(goalState1);
//			State de1 = new State(1); 
//			de1.setValue(0, -1); 
//			deadend.add(de1);


			Heuristic heuristicFunction = new EmptyHeuristic(gs, deadend);// new EmptyHeuristic();//new
			// MultiAgentHeuristic(mapmg,singleAgentSolutions);

			// lets see if we can get a heuristic for each state in the model
			// from the initial state
			// TODO: check results from nvi
			// TODO: check state mapping for heuristic function
//		this.testMAPMG(mapmg);
//		testMultiAgentH(mapmg, heuristicFunction);
			ArrayList<Objectives> tieBreakingOrder = new ArrayList<Objectives>();
//		tieBreakingOrder.add(Objectives.TaskCompletion);
			tieBreakingOrder.add(Objectives.Probability);
			tieBreakingOrder.add(Objectives.Cost);

//		tieBreakingOrder.add(Objectives.Probability); // really just here so I can get this too

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

			BackupNVI backupFunction = new BackupLabelledFullBelman(tieBreakingOrder, actionSelection, epsilon);

			mainLog.println("Initialising Reward Helper Function");
			fileLog.println("Initialising Reward Helper Function");

			RewardHelper rewardH = new RewardHelperMDP(mdpModGen);

			mainLog.println("Max Rollouts: " + maxRollouts);
			mainLog.println("Max TrialLen: " + trialLen);
			fileLog.println("Max Rollouts: " + maxRollouts);
			fileLog.println("Max TrialLen: " + trialLen);

			//
//		MultiAgentNestedProductModelGenerator mapmg = createMAMG(prism, mainLog, filenames, propertiesFileName,
//				resultsLocation);
//
			mainLog.println("\nInitialising THTS");
			fileLog.println("\nInitialising THTS");
			boolean doForwardBackup = true;
			TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) mdpModGen, maxRollouts,
					trialLen, heuristicFunction, actionSelection, outcomeSelection, rewardH, backupFunction,
					doForwardBackup, tieBreakingOrder, mainLog, fileLog);

			mainLog.println("\nBeginning THTS");
			fileLog.println("\nBeginning THTS");
			thts.setName("rtdp" + combString);
			thts.setResultsLocation(resultsLocation);
			thts.run(false);

			mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
			fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");

//			boolean[] goalackAndSolved = thtsNew.thts.runThrough(new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder,false),
//					resultsLocation);
//			if (goalackAndSolved[0])
//				goalFound++;
		}

		System.out.println("Goal found " + goalFound + "/" + maxruns);
	}
}
