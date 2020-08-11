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

public class TestLRTDPNestedMultiAgent {

	// running this from the commandline
	// PRISM_MAINCLASS=thtsNew.TestLRTDPNestedMultiAgent prism/bin/prism
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			TestLRTDPNestedMultiAgent tester = new TestLRTDPNestedMultiAgent();
			String[] options = { "all", "noprobnoavoid", "noprobyesavoid", "avoidable", "unavoidable", "currentWIP" };

			String option = "currentWIP";

			if (args.length > 1) {
				System.out.println(Arrays.deepToString(args));
				option = args[0];
				System.out.println("Running with argument: " + option);
				System.in.read();
			}

			if (option.contentEquals(options[0])) // all
			{

				tester.noprobabilities_noavoid(false);
				tester.noprobabilities(false);
//				tester.avoidable(false);
//				tester.unavoidable(false);

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
				System.out
						.println("Unimplemented option " + option + "\nAvailable options " + Arrays.toString(options));
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

	public void createDirIfNotExist(String directoryName) {
		File directory = new File(directoryName);
		if (!directory.exists()) {
			directory.mkdir();
			// If you require it to make the entire directory path including parents,
			// use directory.mkdirs(); here instead.
		}

	}

	public MultiAgentNestedProductModelGenerator createNestedMultiAgentModelGen(Prism prism, PrismLog mainLog,
			ArrayList<String> filenames, String propertiesFileName, String resultsLocation, boolean hasSharedState)
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
		if (safetyexpr != null) {
			Expression notsafetyexpr = Expression.Not(safetyexpr);
			safetydaind = processedExprs.size();
			processedExprs.add(notsafetyexpr);
		}
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

	public void currentWIP() throws Exception {
		boolean debug = true;
		this.unavoidable(debug);
	}
	
	boolean[] unavoidable(boolean debug) throws Exception {
		boolean goalFound = false;
		double[] hvals = { 50 };
		int[] rollouts = { 100 };
		int[] trialLens = { 50 };
		double hval = 20;
		boolean[] goalack = new boolean[2];

		for (int hvalnum = 0; hvalnum < hvals.length; hvalnum++) {
			hval = hvals[hvalnum];
			for (int rolloutnum = 0; rolloutnum < rollouts.length; rolloutnum++) {
				int maxRollouts = rollouts[rolloutnum];
				for (int trialLennum = 0; trialLennum < trialLens.length; trialLennum++) {
					boolean hasSharedState = true;
					int trialLen = trialLens[trialLennum];

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
					String combString = "_multi_" + tieBreakingOrderStr + "_costcutoff_" + hval + "_trialLen_"
							+ trialLen + "_rollouts_" + maxRollouts;
					String algoIden = "_avoid_lrtdp" + combString;
					PrismLog fileLog = new PrismFileLog(
							resultsLocation + "log_" + example + algoIden + "_justmdp" + ".txt");//

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

					MultiAgentNestedProductModelGenerator maModelGen = createNestedMultiAgentModelGen(prism, mainLog,
							filenames, propertiesFileName, resultsLocation, hasSharedState);

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

					ActionSelector actionSelection = new ActionSelectorGreedyLowerBound(tieBreakingOrder, true);// new
																												// ActionSelectorGreedyBoundsDiff(tieBreakingOrder);

					mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
					fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

					OutcomeSelector outcomeSelection = new OutcomeSelectorRandom();

					mainLog.println("Initialising Full Bellman Backup Function");
					fileLog.println("Initialising Full Bellman Backup Function");

					BackupNVI backupFunction = new BackupLabelledFullBelmanCap(tieBreakingOrder, actionSelection,
							epsilon, hval);

					mainLog.println("Initialising Reward Helper Function");
					fileLog.println("Initialising Reward Helper Function");

					RewardHelper rewardH = new RewardHelperMultiAgent(maModelGen, HelperClass.RewardCalculation.SUM);
//							new RewardHelperNestedSingleAgent(saModelGen);

					mainLog.println("Max Rollouts: " + maxRollouts);
					mainLog.println("Max TrialLen: " + trialLen);
					fileLog.println("Max Rollouts: " + maxRollouts);
					fileLog.println("Max TrialLen: " + trialLen);

					mainLog.println("\nInitialising THTS");
					fileLog.println("\nInitialising THTS");
					boolean doForwardBackup = true;
					TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) maModelGen,
							maxRollouts, trialLen, heuristicFunction, actionSelection, outcomeSelection, rewardH,
							backupFunction, doForwardBackup, tieBreakingOrder, mainLog, fileLog);

					mainLog.println("\nBeginning THTS");
					fileLog.println("\nBeginning THTS");
					thts.setName(example + algoIden);
					thts.setResultsLocation(resultsLocation);
					thts.run(false);

					mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
					fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");

					goalack = thts.runThrough(new ActionSelectorGreedyLowerBound(tieBreakingOrder, true),
							resultsLocation);
					goalFound = goalack[0];
					fileLog.println("Goal Found: " + goalack[0]);
					fileLog.println("Initial State Solved: " + goalack[1]);

					mainLog.println("Goal Found: " + goalack[0]);
					mainLog.println("Initial State Solved: " + goalack[1]);
//					System.in.read();

					mainLog.close();
					fileLog.close();

				}
				if (goalFound)
					break;
			}
			if (goalFound)
				break;
		}
		return goalack;
	}

	boolean[] avoidable(boolean debug) throws Exception {
		boolean goalFound = false;
		double[] hvals = { 50 };
		int[] rollouts = { 100 };
		int[] trialLens = { 50 };
		double hval = 20;
		boolean[] goalack = new boolean[2];

		for (int hvalnum = 0; hvalnum < hvals.length; hvalnum++) {
			hval = hvals[hvalnum];
			for (int rolloutnum = 0; rolloutnum < rollouts.length; rolloutnum++) {
				int maxRollouts = rollouts[rolloutnum];
				for (int trialLennum = 0; trialLennum < trialLens.length; trialLennum++) {
					boolean hasSharedState = false;
					int trialLen = trialLens[trialLennum];

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
					String combString = "_multi_" + tieBreakingOrderStr + "_costcutoff_" + hval + "_trialLen_"
							+ trialLen + "_rollouts_" + maxRollouts;
					String algoIden = "_avoid_lrtdp" + combString;
					PrismLog fileLog = new PrismFileLog(
							resultsLocation + "log_" + example + algoIden + "_justmdp" + ".txt");//

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

					MultiAgentNestedProductModelGenerator maModelGen = createNestedMultiAgentModelGen(prism, mainLog,
							filenames, propertiesFileName, resultsLocation, hasSharedState);

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

					ActionSelector actionSelection = new ActionSelectorGreedyLowerBound(tieBreakingOrder, true);// new
																												// ActionSelectorGreedyBoundsDiff(tieBreakingOrder);

					mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
					fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

					OutcomeSelector outcomeSelection = new OutcomeSelectorRandom();

					mainLog.println("Initialising Full Bellman Backup Function");
					fileLog.println("Initialising Full Bellman Backup Function");

					BackupNVI backupFunction = new BackupLabelledFullBelmanCap(tieBreakingOrder, actionSelection,
							epsilon, hval);

					mainLog.println("Initialising Reward Helper Function");
					fileLog.println("Initialising Reward Helper Function");

					RewardHelper rewardH = new RewardHelperMultiAgent(maModelGen, HelperClass.RewardCalculation.SUM);
//							new RewardHelperNestedSingleAgent(saModelGen);

					mainLog.println("Max Rollouts: " + maxRollouts);
					mainLog.println("Max TrialLen: " + trialLen);
					fileLog.println("Max Rollouts: " + maxRollouts);
					fileLog.println("Max TrialLen: " + trialLen);

					mainLog.println("\nInitialising THTS");
					fileLog.println("\nInitialising THTS");
					boolean doForwardBackup = true;
					TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) maModelGen,
							maxRollouts, trialLen, heuristicFunction, actionSelection, outcomeSelection, rewardH,
							backupFunction, doForwardBackup, tieBreakingOrder, mainLog, fileLog);

					mainLog.println("\nBeginning THTS");
					fileLog.println("\nBeginning THTS");
					thts.setName(example + algoIden);
					thts.setResultsLocation(resultsLocation);
					thts.run(false);

					mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
					fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");

					goalack = thts.runThrough(new ActionSelectorGreedyLowerBound(tieBreakingOrder, true),
							resultsLocation);
					goalFound = goalack[0];
					fileLog.println("Goal Found: " + goalack[0]);
					fileLog.println("Initial State Solved: " + goalack[1]);

					mainLog.println("Goal Found: " + goalack[0]);
					mainLog.println("Initial State Solved: " + goalack[1]);
//					System.in.read();

					mainLog.close();
					fileLog.close();

				}
				if (goalFound)
					break;
			}
			if (goalFound)
				break;
		}
		return goalack;
	}


	boolean[] noprobabilities(boolean debug) throws Exception {
		boolean goalFound = false;
		double[] hvals = { 50 };
		int[] rollouts = { 100 };
		int[] trialLens = { 50 };
		double hval = 20;
		boolean[] goalack = new boolean[2];

		for (int hvalnum = 0; hvalnum < hvals.length; hvalnum++) {
			hval = hvals[hvalnum];
			for (int rolloutnum = 0; rolloutnum < rollouts.length; rolloutnum++) {
				int maxRollouts = rollouts[rolloutnum];
				for (int trialLennum = 0; trialLennum < trialLens.length; trialLennum++) {
					boolean hasSharedState = false;
					int trialLen = trialLens[trialLennum];

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
					String combString = "_multi_" + tieBreakingOrderStr + "_costcutoff_" + hval + "_trialLen_"
							+ trialLen + "_rollouts_" + maxRollouts;
					String algoIden = "_avoid_lrtdp" + combString;
					PrismLog fileLog = new PrismFileLog(
							resultsLocation + "log_" + example + algoIden + "_justmdp" + ".txt");//

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

					MultiAgentNestedProductModelGenerator maModelGen = createNestedMultiAgentModelGen(prism, mainLog,
							filenames, propertiesFileName, resultsLocation, hasSharedState);

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

					ActionSelector actionSelection = new ActionSelectorGreedyLowerBound(tieBreakingOrder, true);// new
																												// ActionSelectorGreedyBoundsDiff(tieBreakingOrder);

					mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
					fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

					OutcomeSelector outcomeSelection = new OutcomeSelectorRandom();

					mainLog.println("Initialising Full Bellman Backup Function");
					fileLog.println("Initialising Full Bellman Backup Function");

					BackupNVI backupFunction = new BackupLabelledFullBelmanCap(tieBreakingOrder, actionSelection,
							epsilon, hval);

					mainLog.println("Initialising Reward Helper Function");
					fileLog.println("Initialising Reward Helper Function");

					RewardHelper rewardH = new RewardHelperMultiAgent(maModelGen, HelperClass.RewardCalculation.SUM);
//							new RewardHelperNestedSingleAgent(saModelGen);

					mainLog.println("Max Rollouts: " + maxRollouts);
					mainLog.println("Max TrialLen: " + trialLen);
					fileLog.println("Max Rollouts: " + maxRollouts);
					fileLog.println("Max TrialLen: " + trialLen);

					mainLog.println("\nInitialising THTS");
					fileLog.println("\nInitialising THTS");
					boolean doForwardBackup = true;
					TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) maModelGen,
							maxRollouts, trialLen, heuristicFunction, actionSelection, outcomeSelection, rewardH,
							backupFunction, doForwardBackup, tieBreakingOrder, mainLog, fileLog);

					mainLog.println("\nBeginning THTS");
					fileLog.println("\nBeginning THTS");
					thts.setName(example + algoIden);
					thts.setResultsLocation(resultsLocation);
					thts.run(false);

					mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
					fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");

					goalack = thts.runThrough(new ActionSelectorGreedyLowerBound(tieBreakingOrder, true),
							resultsLocation);
					goalFound = goalack[0];
					fileLog.println("Goal Found: " + goalack[0]);
					fileLog.println("Initial State Solved: " + goalack[1]);

					mainLog.println("Goal Found: " + goalack[0]);
					mainLog.println("Initial State Solved: " + goalack[1]);
//					System.in.read();

					mainLog.close();
					fileLog.close();

				}
				if (goalFound)
					break;
			}
			if (goalFound)
				break;
		}
		return goalack;
	}

	boolean[] noprobabilities_noavoid(boolean debug) throws Exception {
		boolean goalFound = false;
		double[] hvals = { 50 };
		int[] rollouts = { 100 };
		int[] trialLens = { 50 };
		double hval = 20;
		boolean[] goalack = new boolean[2];

		for (int hvalnum = 0; hvalnum < hvals.length; hvalnum++) {
			hval = hvals[hvalnum];
			for (int rolloutnum = 0; rolloutnum < rollouts.length; rolloutnum++) {
				int maxRollouts = rollouts[rolloutnum];
				for (int trialLennum = 0; trialLennum < trialLens.length; trialLennum++) {
					boolean hasSharedState = false;
					int trialLen = trialLens[trialLennum];

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
					String combString = "_multi_" + tieBreakingOrderStr + "_costcutoff_" + hval + "_trialLen_"
							+ trialLen + "_rollouts_" + maxRollouts;
					String algoIden = "_noavoid_lrtdp" + combString;
					PrismLog fileLog = new PrismFileLog(
							resultsLocation + "log_" + example + algoIden + "_justmdp" + ".txt");//

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

					MultiAgentNestedProductModelGenerator maModelGen = createNestedMultiAgentModelGen(prism, mainLog,
							filenames, propertiesFileName, resultsLocation, hasSharedState);

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

					ActionSelector actionSelection = new ActionSelectorGreedyLowerBound(tieBreakingOrder, true);// new
																												// ActionSelectorGreedyBoundsDiff(tieBreakingOrder);

					mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
					fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

					OutcomeSelector outcomeSelection = new OutcomeSelectorRandom();

					mainLog.println("Initialising Full Bellman Backup Function");
					fileLog.println("Initialising Full Bellman Backup Function");

					BackupNVI backupFunction = new BackupLabelledFullBelmanCap(tieBreakingOrder, actionSelection,
							epsilon, hval);

					mainLog.println("Initialising Reward Helper Function");
					fileLog.println("Initialising Reward Helper Function");

					RewardHelper rewardH = new RewardHelperMultiAgent(maModelGen, HelperClass.RewardCalculation.SUM);
//							new RewardHelperNestedSingleAgent(saModelGen);

					mainLog.println("Max Rollouts: " + maxRollouts);
					mainLog.println("Max TrialLen: " + trialLen);
					fileLog.println("Max Rollouts: " + maxRollouts);
					fileLog.println("Max TrialLen: " + trialLen);

					mainLog.println("\nInitialising THTS");
					fileLog.println("\nInitialising THTS");
					boolean doForwardBackup = true;
					TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) maModelGen,
							maxRollouts, trialLen, heuristicFunction, actionSelection, outcomeSelection, rewardH,
							backupFunction, doForwardBackup, tieBreakingOrder, mainLog, fileLog);

					mainLog.println("\nBeginning THTS");
					fileLog.println("\nBeginning THTS");
					thts.setName(example + algoIden);
					thts.setResultsLocation(resultsLocation);
					thts.run(false);

					mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
					fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");

					goalack = thts.runThrough(new ActionSelectorGreedyLowerBound(tieBreakingOrder, true),
							resultsLocation);
					goalFound = goalack[0];
					fileLog.println("Goal Found: " + goalack[0]);
					fileLog.println("Initial State Solved: " + goalack[1]);

					mainLog.println("Goal Found: " + goalack[0]);
					mainLog.println("Initial State Solved: " + goalack[1]);
//					System.in.read();

					mainLog.close();
					fileLog.close();

				}
				if (goalFound)
					break;
			}
			if (goalFound)
				break;
		}
		return goalack;
	}

}
