package thtsNew;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
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
import prism.PrismException;
import prism.PrismFileLog;
import prism.PrismLog;
import simulator.ModulesFileModelGenerator;
import thts.ActionSelection;
import thts.ActionSelectionGreedy;
import thts.BackupFunction;
import thts.BackupFunctionFullBellman;
import thts.Bounds;
import thts.MDPCreator;
import thts.Objectives;
import thts.OutcomeSelection;
import thts.OutcomeSelectionBoundsGreedy;

public class TempTHTS {

	public static void main(String[] args) {
		try {
			new TempTHTS().run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() throws Exception {

//		thts();
//		rtdp();
		dothts();
	}

	public MultiAgentNestedProductModelGenerator createMAMG(Prism prism, PrismLog mainLog, ArrayList<String> filenames,
			String propertiesFileName, String resultsLocation) throws PrismException, FileNotFoundException {

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

		mainLog.println("Properties "+processedExprs.toString());
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
			String[] nameval= filename.split("/");
			sas.setName(nameval[nameval.length-1].replaceAll(".prism", ""));
			sas.loadModel(filename);
			sas.loadProperties(propFilename);
			HashMap<Objectives, HashMap<State, Double>> solution = sas.getSolution();
			results.add(solution);
		}
		return results;
	}

	public void testMAPMG(MultiAgentNestedProductModelGenerator mapmg) throws PrismException
	{
		List<State> initStates = mapmg.getInitialStates();
		Queue<State> q = new LinkedList<State>();
		Queue<State> visited = new LinkedList<State>();
		q.addAll(initStates);
		int numrewards = mapmg.getNumRewardStructs();
		ArrayList<State> accStates = new ArrayList<State>();
		ArrayList<State> avoidStates = new ArrayList<State>();
		ArrayList<Objectives> objs = new ArrayList<>(); 
		objs.add(Objectives.Probability); 
		objs.add(Objectives.TaskCompletion); 
		objs.add(Objectives.Cost);
		ArrayList<String> actionsWeCareAbout=new ArrayList<>(); 
		actionsWeCareAbout.add("v1_v2,v6_v4"); 
		actionsWeCareAbout.add("v2_cd,v4_v3"); 
		actionsWeCareAbout.add("v2_v0,v3_v4"); 
		actionsWeCareAbout.add("v0_v2,v4_v5"); 
		
		MDPCreator mc = new MDPCreator();
		while (!q.isEmpty()) {
			State s = q.remove();
			if (!visited.contains(s)) {
				visited.add(s);
		
				if (mapmg.isAccState(s))
					accStates.add(s);
				if (mapmg.isAvoidState(s))
					avoidStates.add(s);
				System.out.println("Visiting: " + s);
				// lets get its children ?
				mapmg.exploreState(s);
				mapmg.printExploreState();
				int choices = mapmg.getNumChoices();
				System.out.println("Choices: " + choices);
				for (int c = 0; c < choices; c++) {
					String choiceString = "";
					Object action = mapmg.getChoiceAction(c);
					
					boolean skipthis = true; 
					for(String pa: actionsWeCareAbout)
					{
						if(action.toString().contains(pa))
						{
							skipthis=false;
							break;
						}
					}
						if(skipthis)
							continue;
					choiceString += "Choice " + c + " - " + action.toString();
//					System.out.println(choiceString);
					int numtransitions = mapmg.getNumTransitions(c);
					ArrayList<Entry<State, Double>> children = new ArrayList<>();
					for (int t = 0; t < numtransitions; t++) {
						double prob = mapmg.getTransitionProbability(c, t);
						State ns = mapmg.computeTransitionTarget(c, t);
						choiceString += " " + ns.toString() + ":" + prob + " ";
						Entry<State, Double> child = new AbstractMap.SimpleEntry<State, Double>(ns, prob);
//					
						children.add(child);
//						System.out.println(ns.toString() + ":" + prob);
						// so what are the labels satisfied by each state ?
						// do we know ?
						// we should know
						// also which state is an accepting state ?
						// do we know ?
						// we should know

						q.add(ns);
					}
					for (int r = 0; r < numrewards; r++) {
						double rew = mapmg.getStateActionReward(r, action);
						if (rew != mapmg.getStateActionReward(r, s, action))
							choiceString += " rews not equal?? ";
						choiceString += " r" + r + ": " + rew;
					}
					double taskrew = mapmg.getStateActionTaskReward(c);
					choiceString += " r" + " task rew " + ": " + taskrew;
					mc.addAction(s, action, children);
				
					System.out.println(choiceString);

				}

			}
		}
		System.out.println("Acc States");
		for (State s : accStates)
			System.out.println(s.toString());
		System.out.println("Avoid States");
		for (State s : avoidStates)
			System.out.println(s.toString());


		
	}
	public void testMultiAgentH(MultiAgentNestedProductModelGenerator mapmg,Heuristic hf) throws PrismException
	{
		List<State> initStates = mapmg.getInitialStates();
		Queue<State> q = new LinkedList<State>();
		Queue<State> visited = new LinkedList<State>();
		q.addAll(initStates);
		int numrewards = mapmg.getNumRewardStructs();
		ArrayList<State> accStates = new ArrayList<State>();
		ArrayList<State> avoidStates = new ArrayList<State>();
		ArrayList<Objectives> objs = new ArrayList<>(); 
		objs.add(Objectives.Probability); 
		objs.add(Objectives.TaskCompletion); 
		objs.add(Objectives.Cost);
		
		MDPCreator mc = new MDPCreator();
		while (!q.isEmpty()) {
			State s = q.remove();
			if (!visited.contains(s)) {
				visited.add(s);
				HashMap<Objectives, Bounds> bounds = hf.getStateBounds(objs, s);
				System.out.println(bounds.toString());
				if (mapmg.isAccState(s))
					accStates.add(s);
				if (mapmg.isAvoidState(s))
					avoidStates.add(s);
				System.out.println("Visiting: " + s);
				// lets get its children ?
				mapmg.exploreState(s);
				mapmg.printExploreState();
				int choices = mapmg.getNumChoices();
				System.out.println("Choices: " + choices);
				for (int c = 0; c < choices; c++) {
					String choiceString = "";
					Object action = mapmg.getChoiceAction(c);
					choiceString += "Choice " + c + " - " + action.toString();
//					System.out.println(choiceString);
					int numtransitions = mapmg.getNumTransitions(c);
					ArrayList<Entry<State, Double>> children = new ArrayList<>();
					for (int t = 0; t < numtransitions; t++) {
						double prob = mapmg.getTransitionProbability(c, t);
						State ns = mapmg.computeTransitionTarget(c, t);
						choiceString += " " + ns.toString() + ":" + prob + " ";
						Entry<State, Double> child = new AbstractMap.SimpleEntry<State, Double>(ns, prob);
//					
						children.add(child);
//						System.out.println(ns.toString() + ":" + prob);
						// so what are the labels satisfied by each state ?
						// do we know ?
						// we should know
						// also which state is an accepting state ?
						// do we know ?
						// we should know

						q.add(ns);
					}
					for (int r = 0; r < numrewards; r++) {
						double rew = mapmg.getStateActionReward(r, action);
						if (rew != mapmg.getStateActionReward(r, s, action))
							choiceString += " rews not equal?? ";
						choiceString += " r" + r + ": " + rew;
					}
					double taskrew = mapmg.getStateActionTaskReward(c);
					choiceString += " r" + " task rew " + ": " + taskrew;
					mc.addAction(s, action, children);
				
					System.out.println(choiceString);

				}

			}
		}
		System.out.println("Acc States");
		for (State s : accStates)
			System.out.println(s.toString());
		System.out.println("Avoid States");
		for (State s : avoidStates)
			System.out.println(s.toString());

	}
	public void createDirIfNotExist(String directoryName)
	{
		 File directory = new File(directoryName);
		    if (! directory.exists()){
		        directory.mkdir();
		        // If you require it to make the entire directory path including parents,
		        // use directory.mkdirs(); here instead.
		    }

	}
	public void dothts() throws Exception
	{

		
		
		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "/results/";
		//making sure resultsloc exits 
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location "+resultsLocation);
		
		String example = "tro_example_new_small";
		String propertiesFileName = testsLocation + example + ".prop";

		PrismLog mainLog = new PrismFileLog("stdout");
			Prism prism = new Prism(mainLog);
		prism.initialise();
		prism.setEngine(Prism.EXPLICIT);

		mainLog.println("Initialised Prism");
//		fileLog.println("Initialised Prism");
	

		
		
		
		int numRobots = 1;
		ArrayList<String> filenames = new ArrayList<String>();
		for (int i = 0; i < numRobots; i++)
			filenames.add(testsLocation + example + i + ".prism");
		
		String algoIden = "rtdp";
		PrismLog fileLog = new PrismFileLog(resultsLocation+"log_"+example+"_"+algoIden+"_r_"+numRobots+".txt");//

		rtdp(prism,mainLog,fileLog,filenames,propertiesFileName,resultsLocation);
//		thtsBRTDP(prism,mainLog,fileLog,filenames,propertiesFileName,resultsLocation);
//		lrtdp(prism,mainLog,fileLog,filenames,propertiesFileName,resultsLocation);

		
	}
	public void rtdp(Prism prism,PrismLog mainLog,
			PrismLog fileLog,ArrayList<String> filenames,
			String propertiesFileName,String resultsLocation) throws Exception
	{
		//rtdp is simple 
		//heuristic the same 
		//action selection greedy on lower bound really 
		//outcome selction probabilistic 
		//update full bellman backup 
		mainLog.println("Generating Single Agent Solutions using Nested Products and NVI");
		fileLog.println("Generating Single Agent Solutions using Nested Products and NVI");
		
		ArrayList<HashMap<Objectives, HashMap<State, Double>>> singleAgentSolutions = solveMaxTaskForAllSingleAgents(prism, mainLog, resultsLocation, filenames,
				propertiesFileName);
		
		
		mainLog.println("Creating Multiagent Nested Product Model Generator");
		fileLog.println("Creating Multiagent Nested Product Model Generator");
		
		MultiAgentNestedProductModelGenerator mapmg = createMAMG(prism, mainLog, filenames, propertiesFileName,
				resultsLocation);
		mainLog.println("\nInitialising Multi Agent Heuristic Function");
		fileLog.println("\nInitialising Multi Agent Heuristic Function");
		
		Heuristic heuristicFunction = new MultiAgentHeuristic(mapmg,singleAgentSolutions);

		//lets see if we can get a heuristic for each state in the model 
		//from the initial state 
		//TODO: check results from nvi 
		//TODO: check state mapping for heuristic function 
//		this.testMAPMG(mapmg);
//		testMultiAgentH(mapmg, heuristicFunction);
		ArrayList<Objectives> tieBreakingOrder = new ArrayList<Objectives>();
		tieBreakingOrder.add(Objectives.TaskCompletion);
		tieBreakingOrder.add(Objectives.Cost);
		tieBreakingOrder.add(Objectives.Probability); // really just here so I can get this too
		
		mainLog.println("Tie Breaking Order "+tieBreakingOrder.toString());
		fileLog.println("Tie Breaking Order "+tieBreakingOrder.toString());
		
		mainLog.println("Initialising Greedy Bounds Difference Action Selector Function");
		fileLog.println("Initialising Greedy Bounds Difference Action Selector Function");

		ActionSelector actionSelection = new ActionSelectorGreedyLowerBound(tieBreakingOrder);//new ActionSelectorGreedyBoundsDiff(tieBreakingOrder);
		
		mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
		fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

		OutcomeSelector outcomeSelection = new OutcomeSelectorRandom();
		
		mainLog.println("Initialising Full Bellman Backup Function");
		fileLog.println("Initialising Full Bellman Backup Function");
		
		BackupNVI backupFunction = new BackupFullBellman(tieBreakingOrder);
		
		mainLog.println("Initialising Reward Helper Function");
		fileLog.println("Initialising Reward Helper Function");

		RewardHelper rewardH = new RewardHelperMultiAgent(mapmg,HelperClass.RewardCalculation.SUM);
		
		int maxRollouts = 10;
		int trialLen = 30;
		mainLog.println("Max Rollouts: "+maxRollouts);
		mainLog.println("Max TrialLen: "+trialLen);
		fileLog.println("Max Rollouts: "+maxRollouts);
		fileLog.println("Max TrialLen: "+trialLen);
		
		//
//		MultiAgentNestedProductModelGenerator mapmg = createMAMG(prism, mainLog, filenames, propertiesFileName,
//				resultsLocation);
//
		mainLog.println("\nInitialising THTS");
		fileLog.println("\nInitialising THTS");
		boolean doForwardBackup = true; 
		TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) mapmg, maxRollouts, trialLen,
				heuristicFunction,actionSelection,outcomeSelection,
				rewardH,backupFunction,doForwardBackup,
				tieBreakingOrder,mainLog,fileLog);
		
		mainLog.println("\nBeginning THTS");
		fileLog.println("\nBeginning THTS");
		thts.run();
		
		mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
		fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");

		thts.runThrough(new ActionSelectorGreedyLowerBound(tieBreakingOrder));


	}
	public void lrtdp(Prism prism,PrismLog mainLog,
			PrismLog fileLog,ArrayList<String> filenames,
			String propertiesFileName,String resultsLocation) throws Exception
	{
		//lrtdp is simple 
		//heuristic the same 
		//action selection greedy on lower bound really 
		//outcome selction probabilistic 
		//update full bellman backup 
		mainLog.println("Generating Single Agent Solutions using Nested Products and NVI");
		fileLog.println("Generating Single Agent Solutions using Nested Products and NVI");
		
		ArrayList<HashMap<Objectives, HashMap<State, Double>>> singleAgentSolutions = solveMaxTaskForAllSingleAgents(prism, mainLog, resultsLocation, filenames,
				propertiesFileName);
		
		
		mainLog.println("Creating Multiagent Nested Product Model Generator");
		fileLog.println("Creating Multiagent Nested Product Model Generator");
		
		MultiAgentNestedProductModelGenerator mapmg = createMAMG(prism, mainLog, filenames, propertiesFileName,
				resultsLocation);
		mainLog.println("\nInitialising Multi Agent Heuristic Function");
		fileLog.println("\nInitialising Multi Agent Heuristic Function");
		
		Heuristic heuristicFunction = new MultiAgentHeuristic(mapmg,singleAgentSolutions);

		//lets see if we can get a heuristic for each state in the model 
		//from the initial state 
		//TODO: check results from nvi 
		//TODO: check state mapping for heuristic function 
//		this.testMAPMG(mapmg);
//		testMultiAgentH(mapmg, heuristicFunction);
		ArrayList<Objectives> tieBreakingOrder = new ArrayList<Objectives>();
		tieBreakingOrder.add(Objectives.TaskCompletion);
		tieBreakingOrder.add(Objectives.Cost);
		tieBreakingOrder.add(Objectives.Probability); // really just here so I can get this too
		
		mainLog.println("Tie Breaking Order "+tieBreakingOrder.toString());
		fileLog.println("Tie Breaking Order "+tieBreakingOrder.toString());
		
		mainLog.println("Initialising Greedy Bounds Difference Action Selector Function");
		fileLog.println("Initialising Greedy Bounds Difference Action Selector Function");

		ActionSelector actionSelection = new ActionSelectorGreedyLowerBound(tieBreakingOrder);//new ActionSelectorGreedyBoundsDiff(tieBreakingOrder);
		
		mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
		fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

		OutcomeSelector outcomeSelection = new OutcomeSelectorRandom();
		
		mainLog.println("Initialising Full Bellman Backup Function");
		fileLog.println("Initialising Full Bellman Backup Function");
		
		float epsilon = 0.0001f; 
		BackupNVI backupFunction = new BackupLabelledFullBelman(tieBreakingOrder,actionSelection,epsilon);
		
		mainLog.println("Initialising Reward Helper Function");
		fileLog.println("Initialising Reward Helper Function");

		RewardHelper rewardH = new RewardHelperMultiAgent(mapmg,HelperClass.RewardCalculation.SUM);
		
		int maxRollouts = 10;
		int trialLen = 30;
		mainLog.println("Max Rollouts: "+maxRollouts);
		mainLog.println("Max TrialLen: "+trialLen);
		fileLog.println("Max Rollouts: "+maxRollouts);
		fileLog.println("Max TrialLen: "+trialLen);
		
		//
//		MultiAgentNestedProductModelGenerator mapmg = createMAMG(prism, mainLog, filenames, propertiesFileName,
//				resultsLocation);
//
		mainLog.println("\nInitialising THTS");
		fileLog.println("\nInitialising THTS");
		boolean doForwardBackup = true; 
		TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) mapmg, maxRollouts, trialLen,
				heuristicFunction,actionSelection,outcomeSelection,
				rewardH,backupFunction,doForwardBackup,
				tieBreakingOrder,mainLog,fileLog);
		
		mainLog.println("\nBeginning THTS");
		fileLog.println("\nBeginning THTS");
		thts.run();
		
		mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
		fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");

		thts.runThrough(new ActionSelectorGreedyLowerBound(tieBreakingOrder));


	}
	public void thtsBRTDP(Prism prism,PrismLog mainLog,
			PrismLog fileLog,ArrayList<String> filenames,
			String propertiesFileName,String resultsLocation) throws Exception {

	

		mainLog.println("Generating Single Agent Solutions using Nested Products and NVI");
		fileLog.println("Generating Single Agent Solutions using Nested Products and NVI");
		
		ArrayList<HashMap<Objectives, HashMap<State, Double>>> singleAgentSolutions = solveMaxTaskForAllSingleAgents(prism, mainLog, resultsLocation, filenames,
				propertiesFileName);
		
		
		mainLog.println("Creating Multiagent Nested Product Model Generator");
		fileLog.println("Creating Multiagent Nested Product Model Generator");
		
		MultiAgentNestedProductModelGenerator mapmg = createMAMG(prism, mainLog, filenames, propertiesFileName,
				resultsLocation);
		mainLog.println("\nInitialising Multi Agent Heuristic Function");
		fileLog.println("\nInitialising Multi Agent Heuristic Function");
		
		Heuristic heuristicFunction = new MultiAgentHeuristic(mapmg,singleAgentSolutions);

		//lets see if we can get a heuristic for each state in the model 
		//from the initial state 
		//TODO: check results from nvi 
		//TODO: check state mapping for heuristic function 
//		this.testMAPMG(mapmg);
//		testMultiAgentH(mapmg, heuristicFunction);
		ArrayList<Objectives> tieBreakingOrder = new ArrayList<Objectives>();
		tieBreakingOrder.add(Objectives.TaskCompletion);
		tieBreakingOrder.add(Objectives.Cost);
		tieBreakingOrder.add(Objectives.Probability); // really just here so I can get this too
		
		mainLog.println("Tie Breaking Order "+tieBreakingOrder.toString());
		fileLog.println("Tie Breaking Order "+tieBreakingOrder.toString());
		
		mainLog.println("Initialising Greedy Bounds Difference Action Selector Function");
		fileLog.println("Initialising Greedy Bounds Difference Action Selector Function");

		ActionSelector actionSelection = new ActionSelectorGreedyBoundsDiff(tieBreakingOrder);
		
		mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
		fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

		OutcomeSelector outcomeSelection = new OutcomeSelectorBoundsGreedyBRTDP(tieBreakingOrder);
		
		mainLog.println("Initialising Full Bellman Backup Function");
		fileLog.println("Initialising Full Bellman Backup Function");
		
		BackupNVI backupFunction = new BackupFullBellman(tieBreakingOrder);
		
		mainLog.println("Initialising Reward Helper Function");
		fileLog.println("Initialising Reward Helper Function");

		RewardHelper rewardH = new RewardHelperMultiAgent(mapmg,HelperClass.RewardCalculation.SUM);
		
		int maxRollouts = 10;
		int trialLen = 30;
		mainLog.println("Max Rollouts: "+maxRollouts);
		mainLog.println("Max TrialLen: "+trialLen);
		fileLog.println("Max Rollouts: "+maxRollouts);
		fileLog.println("Max TrialLen: "+trialLen);
		
		//
//		MultiAgentNestedProductModelGenerator mapmg = createMAMG(prism, mainLog, filenames, propertiesFileName,
//				resultsLocation);
//
		mainLog.println("\nInitialising THTS");
		fileLog.println("\nInitialising THTS");
		boolean doForwardBackup = true; 
		TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) mapmg, maxRollouts, trialLen,
				heuristicFunction,actionSelection,outcomeSelection,
				rewardH,backupFunction,doForwardBackup,
				tieBreakingOrder,mainLog,fileLog);
		
		mainLog.println("\nBeginning THTS");
		fileLog.println("\nBeginning THTS");
		thts.run();
		
		mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
		fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");

		thts.runThrough(new ActionSelectorGreedyLowerBound(tieBreakingOrder));

	}

}
