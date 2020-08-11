package thtsNew;

import java.io.File;
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
import prism.PrismFileLog;
import prism.PrismLog;
import simulator.ModulesFileModelGenerator;
import thts.Objectives;

public class TestLRTDPNestedSingleAgent {

	public static void main(String[] args) {

		try {
			TestLRTDPNestedSingleAgent tester = new TestLRTDPNestedSingleAgent();
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
				tester.avoidable(false);

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

	public void currentWIP() throws Exception {
		boolean debug = true;
		unavoidable(debug);

	}

	boolean[] unavoidable(boolean debug) throws Exception {
		boolean goalFound = false;
		double[] hvals = { 50 };
		int[] rollouts = { 1000 };
		int[] trialLens = { 50 };
		double hval = 20;// trialLen;//trialLen*maxRollouts;
		boolean[] goalack = new boolean[2];

		for (int hvalnum = 0; hvalnum < hvals.length; hvalnum++) {
			hval = hvals[hvalnum];
			for (int rolloutnum = 0; rolloutnum < rollouts.length; rolloutnum++) {
				int maxRollouts = rollouts[rolloutnum];
				for (int trialLennum = 0; trialLennum < trialLens.length; trialLennum++) {
					int trialLen = trialLens[trialLennum];

					float epsilon = 0.0001f;

					System.out.println(System.getProperty("user.dir"));
					String currentDir = System.getProperty("user.dir");
					String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
					String resultsLocation = testsLocation + "results/sanp/";
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
					String combString = tieBreakingOrderStr + "_costcutoff_" + hval + "_trialLen_" + trialLen
							+ "_rollouts_" + maxRollouts;
					String algoIden = "_avoid_lrtdp" + combString;
					PrismLog fileLog = new PrismFileLog(
							resultsLocation + "log_" + example + algoIden + "_justmdp" + ".txt");//

					prism.initialise();
					prism.setEngine(Prism.EXPLICIT);

					mainLog.println("Initialised Prism");

					// create a single agent model generator first

					AcceptanceType[] allowedAcceptance = { AcceptanceType.RABIN, AcceptanceType.REACH };
//
					List<Expression> labelExprs = new ArrayList<Expression>();

					String modelFileName = testsLocation + example + "0.prism";
					ModulesFile modulesFile = prism.parseModelFile(new File(modelFileName)); // because the models are
																								// uniform
					String propertiesFileName = testsLocation + example + "_mult.prop";

					ArrayList<String> filenames = new ArrayList<>();
					filenames.add(modelFileName);
					// so we want to create a single agent model generator
					// good test
					PropertiesFile propertiesFile = prism.parsePropertiesFile(modulesFile,
							new File(propertiesFileName));

					LTLModelChecker ltlMC = new LTLModelChecker(prism);

					ExpressionReward rewExpr = null;
					Expression safetyExpr = null;
					int safetyInd = -1;
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
					if (safetyExpr != null) {
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
						safetyInd = das.size() - 1;
					}
					ModulesFileModelGenerator modGen = new ModulesFileModelGenerator(modulesFile, prism);

					NestedProductModelGenerator saModelGen = new NestedProductModelGenerator(modGen, das,
							labelExprsList, safetyInd);

					List<State> gs = new ArrayList<State>();
					List<State> deadend = new ArrayList<State>();

					int[] deadendvals = { -1, 5 };

					for (int deadendval : deadendvals) {
						for (int dv = -1; dv < 2; dv++) {
							for (int i1 = 0; i1 < 2; i1++) {
								for (int i2 = 0; i2 < 2; i2++) {
									for (int i3 = 0; i3 < 2; i3++) {

										State de1 = new State(5);
										de1.setValue(0, deadendval);
										de1.setValue(1, dv);
										de1.setValue(2, i1);
										de1.setValue(3, i2);
										de1.setValue(4, i3);
										deadend.add(de1);
									}
								}
							}
						}
					}
					deadend = null;
					Heuristic heuristicFunction = new EmptyNestedSingleAgentHeuristic(saModelGen, gs, deadend, hval);

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

					BackupNVI backupFunction =
							 new BackupLabelledFullBelmanCap(tieBreakingOrder, actionSelection, epsilon,hval);

							// new BackupLabelledFullBelman(tieBreakingOrder, actionSelection, epsilon);

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
					TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) saModelGen,
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
		double[] hvals = { 20 };
		int[] rollouts = { 100 };
		int[] trialLens = { 50 };
		double hval = 20;// trialLen;//trialLen*maxRollouts;
		boolean[] goalack = new boolean[2];

		for (int hvalnum = 0; hvalnum < hvals.length; hvalnum++) {
			hval = hvals[hvalnum];
			for (int rolloutnum = 0; rolloutnum < rollouts.length; rolloutnum++) {
				int maxRollouts = rollouts[rolloutnum];
				for (int trialLennum = 0; trialLennum < trialLens.length; trialLennum++) {
					int trialLen = trialLens[trialLennum];

					float epsilon = 0.0001f;

					System.out.println(System.getProperty("user.dir"));
					String currentDir = System.getProperty("user.dir");
					String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
					String resultsLocation = testsLocation + "results/sanp/";
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
					String combString = tieBreakingOrderStr + "_costcutoff_" + hval + "_trialLen_" + trialLen
							+ "_rollouts_" + maxRollouts;
					String algoIden = "_avoid_lrtdp" + combString;
					PrismLog fileLog = new PrismFileLog(
							resultsLocation + "log_" + example + algoIden + "_justmdp" + ".txt");//

					prism.initialise();
					prism.setEngine(Prism.EXPLICIT);

					mainLog.println("Initialised Prism");

					// create a single agent model generator first

					AcceptanceType[] allowedAcceptance = { AcceptanceType.RABIN, AcceptanceType.REACH };
//
					List<Expression> labelExprs = new ArrayList<Expression>();

					String modelFileName = testsLocation + example + "0.prism";
					ModulesFile modulesFile = prism.parseModelFile(new File(modelFileName)); // because the models are
																								// uniform
					String propertiesFileName = testsLocation + example + "_mult.prop";

					ArrayList<String> filenames = new ArrayList<>();
					filenames.add(modelFileName);
					// so we want to create a single agent model generator
					// good test
					PropertiesFile propertiesFile = prism.parsePropertiesFile(modulesFile,
							new File(propertiesFileName));

					LTLModelChecker ltlMC = new LTLModelChecker(prism);

					ExpressionReward rewExpr = null;
					Expression safetyExpr = null;
					int safetyInd = -1;
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
					if (safetyExpr != null) {
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
						safetyInd = das.size() - 1;
					}
					ModulesFileModelGenerator modGen = new ModulesFileModelGenerator(modulesFile, prism);

					NestedProductModelGenerator saModelGen = new NestedProductModelGenerator(modGen, das,
							labelExprsList, safetyInd);

					List<State> gs = new ArrayList<State>();
					List<State> deadend = new ArrayList<State>();

					int[] deadendvals = { -1, 5 };

					for (int deadendval : deadendvals) {
						for (int i1 = 0; i1 < 2; i1++) {
							for (int i2 = 0; i2 < 2; i2++) {
								for (int i3 = 0; i3 < 2; i3++) {

									State de1 = new State(4);
									de1.setValue(0, deadendval);
									de1.setValue(1, i1);
									de1.setValue(2, i2);
									de1.setValue(3, i3);
									deadend.add(de1);
								}
							}
						}
					}

					Heuristic heuristicFunction = new EmptyNestedSingleAgentHeuristic(saModelGen, gs, deadend, hval);

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

					RewardHelper rewardH = new RewardHelperNestedSingleAgent(saModelGen);

					mainLog.println("Max Rollouts: " + maxRollouts);
					mainLog.println("Max TrialLen: " + trialLen);
					fileLog.println("Max Rollouts: " + maxRollouts);
					fileLog.println("Max TrialLen: " + trialLen);

					mainLog.println("\nInitialising THTS");
					fileLog.println("\nInitialising THTS");
					boolean doForwardBackup = true;
					TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) saModelGen,
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
		double[] hvals = { 20 };
		int[] rollouts = { 100 };
		int[] trialLens = { 50 };
		double hval = 20;// trialLen;//trialLen*maxRollouts;
		boolean[] goalack = new boolean[2];

		for (int hvalnum = 0; hvalnum < hvals.length; hvalnum++) {
			hval = hvals[hvalnum];
			for (int rolloutnum = 0; rolloutnum < rollouts.length; rolloutnum++) {
				int maxRollouts = rollouts[rolloutnum];
				for (int trialLennum = 0; trialLennum < trialLens.length; trialLennum++) {
					int trialLen = trialLens[trialLennum];

					float epsilon = 0.0001f;

					System.out.println(System.getProperty("user.dir"));
					String currentDir = System.getProperty("user.dir");
					String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
					String resultsLocation = testsLocation + "results/sanp/";
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
					String combString = tieBreakingOrderStr + "_costcutoff_" + hval + "_trialLen_" + trialLen
							+ "_rollouts_" + maxRollouts;
					String algoIden = "_avoid_lrtdp" + combString;
					PrismLog fileLog = new PrismFileLog(
							resultsLocation + "log_" + example + algoIden + "_justmdp" + ".txt");//

					prism.initialise();
					prism.setEngine(Prism.EXPLICIT);

					mainLog.println("Initialised Prism");

					// create a single agent model generator first

					AcceptanceType[] allowedAcceptance = { AcceptanceType.RABIN, AcceptanceType.REACH };
//
					List<Expression> labelExprs = new ArrayList<Expression>();

					String modelFileName = testsLocation + example + "0.prism";
					ModulesFile modulesFile = prism.parseModelFile(new File(modelFileName)); // because the models are
																								// uniform
					String propertiesFileName = testsLocation + example + "_mult.prop";

					ArrayList<String> filenames = new ArrayList<>();
					filenames.add(modelFileName);
					// so we want to create a single agent model generator
					// good test
					PropertiesFile propertiesFile = prism.parsePropertiesFile(modulesFile,
							new File(propertiesFileName));

					LTLModelChecker ltlMC = new LTLModelChecker(prism);

					ExpressionReward rewExpr = null;
					Expression safetyExpr = null;
					int safetyInd = -1;
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
					if (safetyExpr != null) {
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
						safetyInd = das.size() - 1;
					}
					ModulesFileModelGenerator modGen = new ModulesFileModelGenerator(modulesFile, prism);

					NestedProductModelGenerator saModelGen = new NestedProductModelGenerator(modGen, das,
							labelExprsList, safetyInd);

					List<State> gs = new ArrayList<State>();
					List<State> deadend = new ArrayList<State>();

					int[] deadendvals = { -1, 5 };

					for (int deadendval : deadendvals) {
						for (int i1 = 0; i1 < 2; i1++) {
							for (int i2 = 0; i2 < 2; i2++) {
								for (int i3 = 0; i3 < 2; i3++) {

									State de1 = new State(4);
									de1.setValue(0, deadendval);
									de1.setValue(1, i1);
									de1.setValue(2, i2);
									de1.setValue(3, i3);
									deadend.add(de1);
								}
							}
						}
					}

					Heuristic heuristicFunction = new EmptyNestedSingleAgentHeuristic(saModelGen, gs, deadend, hval);

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

					RewardHelper rewardH = new RewardHelperNestedSingleAgent(saModelGen);

					mainLog.println("Max Rollouts: " + maxRollouts);
					mainLog.println("Max TrialLen: " + trialLen);
					fileLog.println("Max Rollouts: " + maxRollouts);
					fileLog.println("Max TrialLen: " + trialLen);

					mainLog.println("\nInitialising THTS");
					fileLog.println("\nInitialising THTS");
					boolean doForwardBackup = true;
					TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) saModelGen,
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
		double[] hvals = { 20 };
		int[] rollouts = { 100 };
		int[] trialLens = { 50 };
		double hval = 20;// trialLen;//trialLen*maxRollouts;
		boolean[] goalack = new boolean[2];

		for (int hvalnum = 0; hvalnum < hvals.length; hvalnum++) {
			hval = hvals[hvalnum];
			for (int rolloutnum = 0; rolloutnum < rollouts.length; rolloutnum++) {
				int maxRollouts = rollouts[rolloutnum];
				for (int trialLennum = 0; trialLennum < trialLens.length; trialLennum++) {
					int trialLen = trialLens[trialLennum];

					float epsilon = 0.0001f;

					System.out.println(System.getProperty("user.dir"));
					String currentDir = System.getProperty("user.dir");
					String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
					String resultsLocation = testsLocation + "results/sanp/";
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
					String combString = tieBreakingOrderStr + "_costcutoff_" + hval + "_trialLen_" + trialLen
							+ "_rollouts_" + maxRollouts;
					String algoIden = "_noavoid_lrtdp" + combString;
					PrismLog fileLog = new PrismFileLog(
							resultsLocation + "log_" + example + algoIden + "_justmdp" + ".txt");//

					prism.initialise();
					prism.setEngine(Prism.EXPLICIT);

					mainLog.println("Initialised Prism");

					// create a single agent model generator first

					AcceptanceType[] allowedAcceptance = { AcceptanceType.RABIN, AcceptanceType.REACH };
//
					List<Expression> labelExprs = new ArrayList<Expression>();

					String modelFileName = testsLocation + example + "0.prism";
					ModulesFile modulesFile = prism.parseModelFile(new File(modelFileName)); // because the models are
																								// uniform
					String propertiesFileName = testsLocation + example + "_mult_noavoid.prop";

					ArrayList<String> filenames = new ArrayList<>();
					filenames.add(modelFileName);
					// so we want to create a single agent model generator
					// good test
					PropertiesFile propertiesFile = prism.parsePropertiesFile(modulesFile,
							new File(propertiesFileName));

					LTLModelChecker ltlMC = new LTLModelChecker(prism);

					ExpressionReward rewExpr = null;
					Expression safetyExpr = null;
					int safetyInd = -1;
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
					if (safetyExpr != null) {
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
						safetyInd = das.size() - 1;
					}
					ModulesFileModelGenerator modGen = new ModulesFileModelGenerator(modulesFile, prism);

					NestedProductModelGenerator saModelGen = new NestedProductModelGenerator(modGen, das,
							labelExprsList, safetyInd);

					List<State> gs = new ArrayList<State>();
					List<State> deadend = new ArrayList<State>();

					int[] deadendvals = { -1 };

					for (int deadendval : deadendvals) {
						for (int i1 = 0; i1 < 2; i1++) {
							for (int i2 = 0; i2 < 2; i2++) {
								for (int i3 = 0; i3 < 2; i3++) {

									State de1 = new State(3);
									// State de1 = new State(2);
									de1.setValue(0, deadendval);
									de1.setValue(1, i1);
									de1.setValue(2, i2);
//									de1.setValue(3, i3);
									deadend.add(de1);
								}
							}
						}
					}

					Heuristic heuristicFunction = new EmptyNestedSingleAgentHeuristic(saModelGen, gs, deadend, hval);

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

					RewardHelper rewardH = new RewardHelperNestedSingleAgent(saModelGen);

					mainLog.println("Max Rollouts: " + maxRollouts);
					mainLog.println("Max TrialLen: " + trialLen);
					fileLog.println("Max Rollouts: " + maxRollouts);
					fileLog.println("Max TrialLen: " + trialLen);

					mainLog.println("\nInitialising THTS");
					fileLog.println("\nInitialising THTS");
					boolean doForwardBackup = true;
					TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) saModelGen,
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
