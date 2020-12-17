package thts.testing.old;

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
import thts.treesearch.actionselector.*;
import thts.treesearch.backup.BackupLabelledFullBelman;
import thts.treesearch.backup.BackupLabelledFullBelmanCap;
import thts.treesearch.backup.BackupNVI;
import thts.treesearch.heuristic.Heuristic;
import thts.treesearch.heuristic.MultiAgentHeuristicTC;
import thts.modelgens.MultiAgentNestedProductModelGenerator;
import thts.treesearch.outcomeselector.OutcomeSelector;
import thts.treesearch.outcomeselector.OutcomeSelectorProb;
import thts.treesearch.rewardhelper.RewardCalculation;
import thts.treesearch.rewardhelper.RewardHelper;
import thts.treesearch.rewardhelper.RewardHelperMultiAgent;
import thts.treesearch.utils.THTSRunInfo;
import thts.treesearch.TrialBasedTreeSearch;
import thts.vi.SingleAgentSolverMaxExpTask;

//PRISM_MAINCLASS=thtsNew.Testing.TestLRTDPVariousConfigs prism/bin/prism
public class TestLRTDPVariousConfigs {
	private static boolean dofincost;
	// each configuration has a bunch of things
	// so there are a bunch of configurations
	ActionSelector actSel = null;
	Heuristic heuristic = null;
	BackupNVI backup = null;
	ActionSelector polActSel = null;
	OutcomeSelector outSel = null;
	boolean useSASH = false;

	int trialLength = -1;
	int maxRollouts = 10000;
	double maxCost = 50;
	float epsilon = 0.0001f;

	String currentDir;
	String testsLocation;
	String resultsLocation;
	String logFilesLocation;

	FileWriter fw;
	BufferedWriter bw;
	PrintWriter out;

	int fsp;

	boolean timeBound = false;

	MultiAgentNestedProductModelGenerator maModelGen;
	ArrayList<HashMap<State, Object>> stateActions;
	ArrayList<HashMap<Objectives, HashMap<State, Double>>> singleAgentStateValues;

	ArrayList<Objectives> tieBreakingOrder;

	String[] confignames = { "0a", "0b", "0c", "0d", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12",
			"13", "14", "15", "16", "17", "18", "19", "20" };

	private boolean dovichecksonintervals;
	private boolean dovipolcheckonintervals;

	public class TestFileInfo {
		String filename;
		String filelocation;
		String propertiesfile;
		ArrayList<String> filenames;
		boolean hasSharedState;

		public TestFileInfo(String fn, String propsuffix, String loc, int numModels, boolean hasSharedState) {
			filename = fn;
			filelocation = loc;
			filenames = new ArrayList<>();

			for (int numModel = 0; numModel < numModels; numModel++) {
				String modelFileName = testsLocation + fn + numModel + ".prism";
				filenames.add(modelFileName);
			}
			propertiesfile = testsLocation + fn + propsuffix + ".prop";
			this.hasSharedState = hasSharedState;

		}
	}

	public static void main(String[] args) {

		// first we set the test file
		// then we set the configuration
		// then we set the number of runs
		// then we run tests
		TestLRTDPVariousConfigs t1 = new TestLRTDPVariousConfigs();
		int maxRuns = 10;// 20;
		dofincost = true;
		try {
			t1.runAllConfigsSmallExample(maxRuns);
//			t1.runSelConfigsGrid5(maxRuns);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void runAllConfigsSmallExample(int maxRuns) throws Exception {
		if (!dofincost)
			maxCost = 0;
		for (int i = 0; i < confignames.length; i++) {

			String config = confignames[i];

			if(!config.contentEquals("0a"))
				continue;
			runSmallExample(config, maxRuns);
		}
	}

	public void runSelConfigsGrid5(int maxRuns) throws Exception {
		String[] selConfigs = { "0a", "0b", "0c", "0d", "6", "8", "10", "13" };
		this.maxCost = 1000;
		for (int i = 0; i < selConfigs.length; i++) {
			for (fsp = 0; fsp < 100; fsp += 30) {
				String config = selConfigs[i];
				runGrid5(config, maxRuns);
			}
		}
	}

	public void runGrid5(String configname, int maxRuns) throws Exception {

		String[] examples = { "r10_g10_a1_grid_5_fsp_0_0_", "r10_g10_a1_grid_5_fsp_10_1_",
				"r10_g10_a1_grid_5_fsp_20_2_", "r10_g10_a1_grid_5_fsp_30_3_", "r10_g10_a1_grid_5_fsp_40_4_",
				"r10_g10_a1_grid_5_fsp_50_5_", "r10_g10_a1_grid_5_fsp_60_6_", "r10_g10_a1_grid_5_fsp_70_7_",
				"r10_g10_a1_grid_5_fsp_80_8_", "r10_g10_a1_grid_5_fsp_90_9_", "r10_g10_a1_grid_5_fsp_100_0_" };
		String filename = examples[fsp / 10];

		String propsuffix = "mult";
		String resFolderExt = "grid5/" + fsp + "/";
		String logFilesExt = "results/configs/" + configname + "/";
		initialiseResultsLocations(resFolderExt, logFilesExt);
		int numRobots = 3;
		boolean hasSharedState = false;
		double egreedyProb = 0.8;
		boolean debug = false;
		timeBound = true;
		setTieBreakingOrder();
		TestFileInfo tfi = new TestFileInfo(filename, propsuffix, testsLocation, numRobots, hasSharedState);
		openResultsFile(filename + "_" + configname);
		printResultsHeader();
		closeResultsFile();
		for (int i = 0; i < maxRuns; i++) {

			long startTime = System.currentTimeMillis();
			THTSRunInfo rinfo = runConfiguration(configname, tfi, debug, i, egreedyProb);
			long endTime = System.currentTimeMillis();
			openResultsFile(filename + "_" + configname);
			printResult(configname, i, egreedyProb, rinfo,3,3,endTime-startTime);
			closeResultsFile();

		}

	}

	public void runSmallExample(String configname, int maxRuns) throws Exception {

		String resFolderExt = "tro_examples/";
		String logFilesExt = "results/configs/" + configname + "/";
		initialiseResultsLocations(resFolderExt, logFilesExt);
		int numRobots = 2;
		String filename = "tro_example_new_small";
		String propsuffix = "_mult";
		boolean hasSharedState = true;
		double egreedyProb = 0.8;
		this.maxCost = 50;
		boolean debug = false;
		String fnsuffix = "_understanding";
		if (!dofincost) {
			maxCost = 0;
			fnsuffix = "_nofinpen";
		}
		setTieBreakingOrder();
		TestFileInfo tfi = new TestFileInfo(filename, propsuffix, testsLocation, numRobots, hasSharedState);
		openResultsFile(filename + "_" + configname + fnsuffix);
		printResultsHeader();
		for (int i = 0; i < maxRuns; i++) {
			long startTime = System.currentTimeMillis();

			THTSRunInfo rinfo = runConfiguration(configname, tfi, debug, i, egreedyProb);
			long endTime = System.currentTimeMillis();
	
			printResult(configname, i, egreedyProb, rinfo,2,2,endTime-startTime);
		}
		closeResultsFile();

	}

	void initialiseConfiguration(String configname, HashMap<Objectives, Entry<Double, Double>> minMaxVals,
			PrismLog fileLog, double actSelSoftmaxProb) {
		if (configname.contentEquals(confignames[0]))
			initialiseConfig0(minMaxVals, fileLog, -1, false);
		else if (configname.contentEquals(confignames[1]))
			initialiseConfig0(minMaxVals, fileLog, -1, true);
		else if (configname.contentEquals(confignames[2]))
			initialiseConfig0(minMaxVals, fileLog, actSelSoftmaxProb, false);
		else if (configname.contentEquals(confignames[3]))
			initialiseConfig0(minMaxVals, fileLog, actSelSoftmaxProb, true);
		else if (configname.contentEquals(confignames[4]))
			initialiseConfig1(minMaxVals, fileLog, actSelSoftmaxProb, false, false);
		else if (configname.contentEquals(confignames[5]))
			initialiseConfig1(minMaxVals, fileLog, actSelSoftmaxProb, false, true);
		else if (configname.contentEquals(confignames[6]))
			initialiseConfig1(minMaxVals, fileLog, actSelSoftmaxProb, true, false);
		else if (configname.contentEquals(confignames[7]))
			initialiseConfig1(minMaxVals, fileLog, actSelSoftmaxProb, true, true);
		else if (configname.contentEquals(confignames[8]))
			initialiseConfig5(minMaxVals, fileLog, actSelSoftmaxProb, false, false);
		else if (configname.contentEquals(confignames[9]))
			initialiseConfig5(minMaxVals, fileLog, actSelSoftmaxProb, false, true);
		else if (configname.contentEquals(confignames[10]))
			initialiseConfig5(minMaxVals, fileLog, actSelSoftmaxProb, true, false);
		else if (configname.contentEquals(confignames[11]))
			initialiseConfig5(minMaxVals, fileLog, actSelSoftmaxProb, true, true);
		else if (configname.contentEquals(confignames[12])) {
			ArrayList<Boolean> useUpperBound = new ArrayList<>();
			useUpperBound.add(true);
			useUpperBound.add(false);
			initialiseConfig9(minMaxVals, fileLog, actSelSoftmaxProb, true, 0, useUpperBound);

		} else if (configname.contentEquals(confignames[13])) {
			ArrayList<Boolean> useUpperBound = new ArrayList<>();
			useUpperBound.add(true);
			useUpperBound.add(false);
			initialiseConfig9(minMaxVals, fileLog, actSelSoftmaxProb, true, 1, useUpperBound);

		} else if (configname.contentEquals(confignames[14])) {
			ArrayList<Boolean> useUpperBound = new ArrayList<>();
			useUpperBound.add(true);
			useUpperBound.add(false);
			initialiseConfig9(minMaxVals, fileLog, actSelSoftmaxProb, true, 2, useUpperBound);

		} else if (configname.contentEquals(confignames[15])) {
			ArrayList<Boolean> useUpperBound = new ArrayList<>();
			useUpperBound.add(true);
			useUpperBound.add(false);
			initialiseConfig9(minMaxVals, fileLog, actSelSoftmaxProb, false, 0, useUpperBound);

		} else if (configname.contentEquals(confignames[16])) {
			ArrayList<Boolean> useUpperBound = new ArrayList<>();
			useUpperBound.add(true);
			useUpperBound.add(false);
			initialiseConfig9(minMaxVals, fileLog, actSelSoftmaxProb, false, 1, useUpperBound);

		} else if (configname.contentEquals(confignames[17])) {
			ArrayList<Boolean> useUpperBound = new ArrayList<>();
			useUpperBound.add(true);
			useUpperBound.add(false);
			initialiseConfig9(minMaxVals, fileLog, actSelSoftmaxProb, false, 2, useUpperBound);

		} else if (configname.contentEquals(confignames[18])) {
			ArrayList<Boolean> useUpperBound = new ArrayList<>();
			useUpperBound.add(false);
			useUpperBound.add(false);
			initialiseConfig9(minMaxVals, fileLog, actSelSoftmaxProb, true, 0, useUpperBound);

		} else if (configname.contentEquals(confignames[19])) {
			ArrayList<Boolean> useUpperBound = new ArrayList<>();
			useUpperBound.add(false);
			useUpperBound.add(false);
			initialiseConfig9(minMaxVals, fileLog, actSelSoftmaxProb, true, 1, useUpperBound);

		} else if (configname.contentEquals(confignames[20])) {
			ArrayList<Boolean> useUpperBound = new ArrayList<>();
			useUpperBound.add(false);
			useUpperBound.add(false);
			initialiseConfig9(minMaxVals, fileLog, actSelSoftmaxProb, true, 2, useUpperBound);

		} else if (configname.contentEquals(confignames[21])) {
			ArrayList<Boolean> useUpperBound = new ArrayList<>();
			useUpperBound.add(false);
			useUpperBound.add(false);
			initialiseConfig9(minMaxVals, fileLog, actSelSoftmaxProb, false, 0, useUpperBound);

		} else if (configname.contentEquals(confignames[22])) {
			ArrayList<Boolean> useUpperBound = new ArrayList<>();
			useUpperBound.add(false);
			useUpperBound.add(false);
			initialiseConfig9(minMaxVals, fileLog, actSelSoftmaxProb, false, 1, useUpperBound);

		} else if (configname.contentEquals(confignames[23])) {
			ArrayList<Boolean> useUpperBound = new ArrayList<>();
			useUpperBound.add(false);
			useUpperBound.add(false);
			initialiseConfig9(minMaxVals, fileLog, actSelSoftmaxProb, false, 2, useUpperBound);

		} else
			System.out.println("Invalid Option: " + configname);

	}

	void initialiseConfig0(HashMap<Objectives, Entry<Double, Double>> minMaxVals, PrismLog fileLog,
			double actSelSoftmaxProb, boolean initialiseWithSAS) {

		this.trialLength = -1;// stateActions.get(0).size()*maModelGen.numDAs*maModelGen.numModels*2;
		useSASH = initialiseWithSAS;
		heuristic = new MultiAgentHeuristicTC(maModelGen, singleAgentStateValues, minMaxVals, useSASH);

		ActionSelector greedyActSel = new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder, false);

		ActionSelector rolloutPol = new ActionSelectorSASRolloutPol(maModelGen, stateActions);
		if (actSelSoftmaxProb > 0 && actSelSoftmaxProb < 1) {
			ActionSelector egreedyActSel = new ActionSelectorSoftmax(greedyActSel, actSelSoftmaxProb);

			actSel = new ActionSelectorMCTS(egreedyActSel, rolloutPol);
		} else
			actSel = new ActionSelectorMCTS(greedyActSel, rolloutPol);
		outSel = new OutcomeSelectorProb();

		if (dofincost)
			backup = new BackupLabelledFullBelmanCap(tieBreakingOrder, greedyActSel, epsilon, minMaxVals, fileLog,false);
		else
			backup = new BackupLabelledFullBelman(tieBreakingOrder,
					new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder, false), epsilon);
		polActSel = greedyActSel;

	}

	void initialiseConfig1(HashMap<Objectives, Entry<Double, Double>> minMaxVals, PrismLog fileLog,
			double actSelSoftmaxProb, boolean initialiseWithSAS, boolean polActGreedy) {

		this.trialLength = 50;// stateActions.get(0).size()*maModelGen.numDAs*maModelGen.numModels*2;
		useSASH = initialiseWithSAS;
		heuristic = new MultiAgentHeuristicTC(maModelGen, singleAgentStateValues, minMaxVals, useSASH);

		ActionSelector greedyActSel = new ActionSelectorGreedySimpleUpperLowerBound(tieBreakingOrder,true);
		ActionSelector rolloutPol = new ActionSelectorSASRolloutPol(maModelGen, stateActions);
		actSel = new ActionSelectorMCTS(greedyActSel, rolloutPol);

		outSel = new OutcomeSelectorProb();

		if (dofincost)
			backup = new BackupLabelledFullBelmanCap(tieBreakingOrder, greedyActSel, epsilon, minMaxVals, fileLog,false);
		else
			backup = new BackupLabelledFullBelman(tieBreakingOrder,
					new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder, false), epsilon);

		polActSel = greedyActSel;
		if (polActGreedy)

			polActSel = new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder, false);

	}

	void initialiseConfig5(HashMap<Objectives, Entry<Double, Double>> minMaxVals, PrismLog fileLog,
			double actSelSoftmaxProb, boolean initialiseWithSAS, boolean polActGreedy) {
		this.trialLength = -1;
		useSASH = initialiseWithSAS;
		heuristic = new MultiAgentHeuristicTC(maModelGen, singleAgentStateValues, minMaxVals, useSASH);

		ActionSelector greedyActSel = new ActionSelectorGreedySimpleUpperLowerBound(tieBreakingOrder,true);
		ActionSelector egreedyActSel = new ActionSelectorSoftmax(greedyActSel, actSelSoftmaxProb);
		ActionSelector rolloutPol = new ActionSelectorSASRolloutPol(maModelGen, stateActions);
		actSel = new ActionSelectorMCTS(egreedyActSel, rolloutPol);

		outSel = new OutcomeSelectorProb();

		if (dofincost)
			backup = new BackupLabelledFullBelmanCap(tieBreakingOrder, greedyActSel, epsilon, minMaxVals, fileLog,false);
		else
			backup = new BackupLabelledFullBelman(tieBreakingOrder,
					new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder, false), epsilon);

		polActSel = greedyActSel;
		if (polActGreedy)

			polActSel = new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder, false);

	}

	void initialiseConfig9(HashMap<Objectives, Entry<Double, Double>> minMaxVals, PrismLog fileLog,
			double actSelSoftmaxProb, boolean initialiseWithSAS, int polActChoice, ArrayList<Boolean> useUpperBound) {
		this.trialLength = -1;
		useSASH = initialiseWithSAS;
		heuristic = new MultiAgentHeuristicTC(maModelGen, singleAgentStateValues, minMaxVals, useSASH);

		ActionSelector actSelUCT = new ActionSelectorUCT(tieBreakingOrder, /* uctC, */useUpperBound);

		ActionSelector greedyActSel = new ActionSelectorGreedySimpleUpperLowerBound(tieBreakingOrder,true);
		ActionSelector rolloutPol = new ActionSelectorSASRolloutPol(maModelGen, stateActions);
		actSel = new ActionSelectorMCTS(actSelUCT, rolloutPol);

		outSel = new OutcomeSelectorProb();

		if (dofincost)
			backup = new BackupLabelledFullBelmanCap(tieBreakingOrder, greedyActSel, epsilon, minMaxVals, fileLog,false);
		else
			backup = new BackupLabelledFullBelman(tieBreakingOrder,
					new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder, false), epsilon);

		polActSel = greedyActSel;
		if (polActChoice == 1)

			polActSel = new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder, false);
		else if (polActChoice == 2)
			polActSel = new ActionSelectorMostVisited();

	}

	THTSRunInfo runConfiguration(String configname, TestFileInfo tfi, boolean debug, int run, double egreedyProb)
			throws Exception {
		THTSRunInfo runInfo = new THTSRunInfo();

		PrismLog mainLog;
		if (debug)
			mainLog = new PrismFileLog("stdout");
		else
			mainLog = new PrismDevNullLog();

		String runName = configname + "_" + tfi.filename + "_r" + run;
		Prism prism = new Prism(mainLog);
		PrismLog fileLog = new PrismFileLog(logFilesLocation + "log_" + runName + "_justmdp" + ".txt");

		prism.initialise();
		prism.setEngine(Prism.EXPLICIT);

		mainLog.println("Initialised Prism");

		stateActions = new ArrayList<>();
		singleAgentStateValues = solveMaxTaskForAllSingleAgents(prism, mainLog, logFilesLocation, tfi.filenames,
				tfi.propertiesfile, stateActions);

		maModelGen = createNestedMultiAgentModelGen(prism, mainLog, tfi.filenames, tfi.propertiesfile, logFilesLocation,
				tfi.hasSharedState);

		HashMap<Objectives, Entry<Double, Double>> minMaxVals = new HashMap<>();
		minMaxVals.put(Objectives.Cost, new AbstractMap.SimpleEntry<Double, Double>(0., maxCost));
		minMaxVals.put(Objectives.TaskCompletion,
				new AbstractMap.SimpleEntry<Double, Double>(0., (double) maModelGen.numDAs));

		mainLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
		fileLog.println("Tie Breaking Order " + tieBreakingOrder.toString());

		initialiseConfiguration(configname, minMaxVals, fileLog, egreedyProb);
		RewardHelper rewardH = new RewardHelperMultiAgent(maModelGen, RewardCalculation.SUM);

		mainLog.println("Max Rollouts: " + maxRollouts);
		mainLog.println("Max TrialLen: " + trialLength);
		fileLog.println("Max Rollouts: " + maxRollouts);
		fileLog.println("Max TrialLen: " + trialLength);

		mainLog.println("\nInitialising THTS");
		fileLog.println("\nInitialising THTS");
		boolean doForwardBackup = true;

		TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) maModelGen, maxRollouts,
				trialLength, heuristic, actSel, outSel, rewardH, backup, doForwardBackup, tieBreakingOrder, mainLog,
				fileLog);
		if (dovipolcheckonintervals) {
			thts.enablePolCheckAtIntervals(0, prism);
		}
		mainLog.println("\nBeginning THTS");
		fileLog.println("\nBeginning THTS");
		thts.setName(runName);
		thts.setResultsLocation(logFilesLocation);
		if (this.timeBound) {
			thts.setTimeBound(true);
			runInfo.timeLimited = true;
			runInfo.maxTimeLimit = thts.timeLimitInMS;
		}
		int numRolloutsTillSolved = -1;
		try {
			numRolloutsTillSolved = thts.run(false, 0, true);
		} catch (StackOverflowError e) {
			runInfo.stackoverflowerror = true;
		}
		mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
		fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");

		HashMap<Objectives, Double> tempres = thts.doVIOnPolicy(polActSel, logFilesLocation, run, prism);
		System.out.println(tempres);

		mainLog.close();
		fileLog.close();
		prism.closeDown();
		runInfo.initialStateSolved = thts.getRootNode(0).isSolved();
		runInfo.vipol = tempres;
		runInfo.numRolloutsTillSolved = numRolloutsTillSolved;
		runInfo.initialStateValues = thts.getInitialStateBounds();
		runInfo.duration = thts.getDuration();
		runInfo.setTrialLenStuff(thts.trialLenArray);
		runInfo.averageTrialLen = thts.avgTrialLen;
		runInfo.chanceNodesExp = thts.chanceNodesExplored;
		runInfo.decisionNodesExp = thts.decisionNodesExplored;
		runInfo.vipolAtIntervals = thts.timeValues;

		return runInfo;
	}

	public void createDirIfNotExist(String directoryName) {
		File directory = new File(directoryName);
		if (!directory.exists()) {

			directory.mkdirs();
		}

	}

	void setTieBreakingOrder() {
		tieBreakingOrder = new ArrayList<Objectives>();
		tieBreakingOrder.add(Objectives.TaskCompletion);
		tieBreakingOrder.add(Objectives.Cost);
	}

	void printResultsHeader() {
		String header = "\nConfiguration\tFSP\tRobots\tGoals\tFN\tEpsilon\tTC_U\tTC_L\tC_U\tC_L"
				+ "\tSolved\tGoal\tProbGoal\tNumRollouts\tSOError\tVI_TC\tVI_C\tVI_P"
				+ "\tTimeBound\tTimeLimit\tTHTSTimeTaken\tMaxTLen\tMinTLen\tAvgTLen\tDNExp\tCNExp\tTotalTime\tVIPolAtIntervals";
	if (out != null)
			out.println(header);

	}

	void printResult(String configname, int run, double epsilon, THTSRunInfo rinfo, int numRobots, int numGoals,
			long totalTime) {
		String resLine = configname + "\t" + fsp + "\t" + numRobots + "\t" + numGoals + "\t" + run + "\t" + epsilon
				+ "\t" + rinfo.getBoundsString(Objectives.TaskCompletion, "\t") + "\t"
				+ rinfo.getBoundsString(Objectives.Cost, "\t") + "\t" + rinfo.initialStateSolved + "\t"
				+ rinfo.goalFound + "\t" + rinfo.goalOnProbablePath + "\t" + rinfo.numRolloutsTillSolved + "\t"
				+ rinfo.stackoverflowerror + "\t" + rinfo.getviInfo(Objectives.TaskCompletion) + "\t"
				+ rinfo.getviInfo(Objectives.Cost) + "\t" + rinfo.getviInfo(Objectives.Probability) + "\t"
				+ rinfo.timeLimited + "\t" + rinfo.maxTimeLimit + "\t" + rinfo.duration + "\t" + rinfo.maxTrialLen
				+ "\t" + rinfo.minTrialLen + "\t" + rinfo.averageTrialLen + "\t" + rinfo.decisionNodesExp + "\t"
				+ rinfo.chanceNodesExp + "\t" + totalTime + "\t" + rinfo.getVIPolIntervalString();
		if (out != null)
			out.println(resLine);
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

	public ArrayList<HashMap<Objectives, HashMap<State, Double>>> solveMaxTaskForAllSingleAgents(Prism prism,
			PrismLog mainLog, String resultsLocation, ArrayList<String> fns, String propFilename,
			ArrayList<HashMap<State, Object>> stateActions) throws Exception {
		SingleAgentSolverMaxExpTask sas = new SingleAgentSolverMaxExpTask(prism, mainLog, resultsLocation);
		// so now we can read in the model
		ArrayList<HashMap<Objectives, HashMap<State, Double>>> allStateValues = new ArrayList<>();
		for (String filename : fns) {
			String[] nameval = filename.split("/");
			sas.setName(nameval[nameval.length - 1].replaceAll(".prism", ""));
			sas.loadModel(filename);
			sas.loadProperties(propFilename);
			// so we need to edit this bit
			// so we need a new function with the strategy

			HashMap<Objectives, HashMap<State, Double>> stateValues = new HashMap<>();
			HashMap<State, Object> stateAction = new HashMap<>();
			sas.getStateValuesAndStrategies(stateValues, stateAction);
			stateActions.add(stateAction);
			allStateValues.add(stateValues);
		}
		return allStateValues;
	}

	void initialiseResultsLocations(String resFolderext, String logFilesExt) {
		currentDir = System.getProperty("user.dir");
		testsLocation = currentDir + "/tests/wkspace/" + resFolderext;
		logFilesLocation = testsLocation + logFilesExt;
		resultsLocation = testsLocation + "results/configs/csvs/";
		createDirIfNotExist(logFilesLocation);
		createDirIfNotExist(resultsLocation);

	}

	void openResultsFile(String fn) throws IOException {
		fw = new FileWriter(resultsLocation + fn + ".csv", true);
		bw = new BufferedWriter(fw);
		out = new PrintWriter(bw);
	}

	void closeResultsFile() throws IOException {

		out.close();
		bw.close();
		fw.close();
	}

}
