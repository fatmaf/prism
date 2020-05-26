package thtsNew;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import acceptance.AcceptanceOmega;
import acceptance.AcceptanceType;
import automata.DA;
import explicit.LTLModelChecker;
import explicit.MDP;
import explicit.MDPModelChecker;
import explicit.ProbModelChecker;
import parser.State;
import parser.ast.Expression;
import parser.ast.ExpressionProb;
import parser.ast.ExpressionQuant;
import parser.ast.ExpressionReward;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import prism.ModelGenerator;
import prism.ModelInfo;
import prism.Prism;
import prism.PrismException;
import prism.PrismFileLog;
import prism.PrismLog;
import prism.ProductModelGenerator;
import prism.RewardGenerator;
import simulator.ModulesFileModelGenerator;
import thts.Objectives;

//just test whatever I want here 
public class TestThings {
	private String testsLocation;
	private String resultsLocation;

	public static void main(String[] args) {
		try {
			new TestThings().run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() throws Exception {
//		npprodmodgen();
//		testMaxTaskNVISingleAgentSolver();
		testmaprodmodegen();
	}

	public void testmaprodmodegen() throws PrismException, Exception {
		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String dir = currentDir + "/tests/wkspace/tro_examples/";// "/home/fatma/Data/PhD/code/prism_ws/prism-svn/prism/tests/wkspace/simpleTests/";

		testsLocation = currentDir + "/tests/wkspace/tro_examples/";

		resultsLocation = testsLocation + "/results/";
		String example = "tro_example_new_small";

		PrismLog mainLog = new PrismFileLog("stdout");
		Prism prism = new Prism(mainLog);
		prism.initialise();
		prism.setEngine(Prism.EXPLICIT);

		String propertiesFileName = testsLocation + example + ".prop";

		int numRobots = 2;
		ArrayList<String> filenames = new ArrayList<String>();

		for (int i = 0; i < numRobots; i++)
			filenames.add(testsLocation + example + i + ".prism");

		AcceptanceType[] allowedAcceptance = { AcceptanceType.RABIN, AcceptanceType.REACH };
		
		// step 1
		// create the modulesfilemodelgenerators
		ArrayList<ModulesFileModelGenerator> mfmodgens = new ArrayList<>();
		ModulesFile modulesFile = null; //just here so we can use the last modules file for our properties 
		
		for (String modelFileName : filenames) {
			mainLog.println("Loading model gen for "+modelFileName);
			modulesFile = prism.parseModelFile(new File(modelFileName)); //because the models are uniform 
			//we might have to find a way to change this later 
			ModulesFileModelGenerator modGen = new ModulesFileModelGenerator(modulesFile, prism);
			mfmodgens.add(modGen);
		}
		// step 2
		// load all the exprs and remember to check them
		PropertiesFile propertiesFile = prism.parsePropertiesFile(modulesFile, new File(propertiesFileName));
		List<Expression> processedExprs = new ArrayList<Expression>();
		int safetydaind  = -1;
		Expression safetyexpr = null;
		for (int i = 0; i < propertiesFile.getNumProperties(); i++) {
			mainLog.println(propertiesFile.getProperty(i));
			// so reward + safety
			boolean isSafeExpr = false;
			Expression exprHere = propertiesFile.getProperty(i);
	
				Expression daExpr = ((ExpressionQuant) exprHere).getExpression();
				isSafeExpr = !Expression.isCoSafeLTLSyntactic(daExpr, true);
				if (isSafeExpr)
				{	
					if(safetyexpr != null)
					{
						//two safety exprs? lets and this stuff 
						//TODO: could this cause problems ? //like if one was min and max since we're ignoring those
						safetyexpr = Expression.And(safetyexpr, daExpr);
					}
					else 
						safetyexpr = daExpr; 
				
				}

			if (!isSafeExpr)
				processedExprs.add(daExpr);
		}
		//we've got the safety stuff left 
		//we need to not it 
		Expression notsafetyexpr = Expression.Not(safetyexpr); 
		safetydaind = processedExprs.size();
		processedExprs.add(safetyexpr);
		
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
			//this will need to be changed if you've got different variables accross models 
			//then its better to do the v=5 stuff in the prop files and just ignore labels 
			expr = (Expression) expr.expandPropRefsAndLabels(propertiesFile, modulesFile.getLabelList());
			DA<BitSet, ? extends AcceptanceOmega> da = ltlMC.constructExpressionDAForLTLFormula(expr, labelExprs, allowedAcceptance);
			da.setDistancesToAcc();
			PrismLog out = new PrismFileLog(resultsLocation + "da_" + i + ".dot");
			// printing the da
			da.print(out, "dot");
			out.close();
			labelExprsList.add(labelExprs);
			das.add(da);
			mainLog.println("Created DA for "+expr.toString());
		}
		MultiAgentNestedProductModelGenerator mapmg = new MultiAgentNestedProductModelGenerator(mfmodgens,das,labelExprsList,safetydaind);
		
		//so now we've got the model generator 
		//now to fill things up 
		//woohoo 
		List<State> initStates = mapmg.getInitialStates();
		Queue<State> q = new LinkedList<State>();
		Queue<State> visited = new LinkedList<State>();
		q.addAll(initStates);

		while (!q.isEmpty()) {
			State s = q.remove();
			if (!visited.contains(s)) {
				visited.add(s);
				System.out.println("Visiting: " + s);
			}
		}
//		int numrewards = mapmg.getNumRewardStructs();
	}

	public void testMaxTaskNVISingleAgentSolver() throws Exception {
		// TODO: double check the implementation of vi and everything
		// TODO: do not forget to do this or I kill you okay ?
		// TODO: death isnt bad you know
		// TODO: fine do not forget this or you will be sad and upset and worried
		// TODO: there is enough crap already with you doing this
		// TODO: okay fine wut iz liff

		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String dir = currentDir + "/tests/wkspace/tro_examples/";// "/home/fatma/Data/PhD/code/prism_ws/prism-svn/prism/tests/wkspace/simpleTests/";

		testsLocation = currentDir + "/tests/wkspace/tro_examples/";

		resultsLocation = testsLocation + "/results/";
		String example = "tro_example_new_small";

		PrismLog mainLog = new PrismFileLog("stdout");
		Prism prism = new Prism(mainLog);
		prism.initialise();
		prism.setEngine(Prism.EXPLICIT);

		SingleAgentSolverMaxExpTask sas = new SingleAgentSolverMaxExpTask(prism, mainLog, resultsLocation);
		// so now we can read in the model

		String filename = testsLocation + example + "0.prism";
		String alternatepropfn = testsLocation + example + ".prop";
		sas.loadModel(filename);
		sas.loadProperties(alternatepropfn);
		HashMap<Objectives, HashMap<State, Double>> solution = sas.getSolution();
		// so now we can take this and use it as a heuristic!!

	}

	public void npprodmodgen() throws PrismException, FileNotFoundException {
		AcceptanceType[] allowedAcceptance = { AcceptanceType.RABIN, AcceptanceType.REACH };
		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");

		String dir = currentDir + "/tests/wkspace/tro_examples/";// "/home/fatma/Data/PhD/code/prism_ws/prism-svn/prism/tests/wkspace/simpleTests/";
		// System.getProperty("user.dir");

		testsLocation = currentDir + "/tests/wkspace/tro_examples/";

		resultsLocation = testsLocation + "/results/";
		String example = "tro_example_new_small";

		String saveplace = testsLocation;
		String filename = example;

		PrismLog mainLog = new PrismFileLog("stdout");
		Long startTime = System.currentTimeMillis();

		// Initialise PRISM engine

		Prism prism = new Prism(mainLog);
		prism.initialise();
		prism.setEngine(Prism.EXPLICIT);

		ArrayList<String> filenames = new ArrayList<String>();
//		filenames.add(saveplace+filename+".prism");
		filenames.add(saveplace + filename + "0.prism");
//		filenames.add(saveplace + filename + "1.prism");
		String propfilename = saveplace + filename + ".props";
		String alternatepropfn = saveplace + filename + ".prop";
		DA<BitSet, ? extends AcceptanceOmega> da = null;
		String modelFileName = filenames.get(0);
		String propertiesFileName = alternatepropfn;

		ModulesFile modulesFile = prism.parseModelFile(new File(modelFileName));
		prism.loadPRISMModel(modulesFile);

		PropertiesFile propertiesFile = prism.parsePropertiesFile(modulesFile, new File(propertiesFileName));

		LTLModelChecker ltlMC = new LTLModelChecker(prism);

		ModulesFileModelGenerator prismModelGen = new ModulesFileModelGenerator(modulesFile, prism);

		ModelGenerator modelGen = prismModelGen;
		ModelInfo modelInfo = modulesFile;

		prism.buildModel();
		MDP mdp = (MDP) prism.getBuiltModelExplicit();

		MDPModelChecker mc = new MDPModelChecker(prism);

		mc.setGenStrat(true);
		mc.setExportAdv(true);

		// now we've got to load all the properties
		// then we create a da for each property
		// if its a safety property we set a flag
		// and then we go on
		PropertiesFile altPropertiesFile = propertiesFile;// prism.parsePropertiesFile(modulesFile, new
															// File(alternatepropfile));

		mc.setModelCheckingInfo(modelInfo, altPropertiesFile, (RewardGenerator) prismModelGen);
		// so lets find out how many properties there are
		MDP oldproduct = null;

		ExpressionReward rewExpr = null;
		Expression safetyExpr = null;
		ArrayList<Expression> otherExpressions = new ArrayList<Expression>();
		// assumption a safety expression can not be a reward expression

		List<Expression> processedExprs = new ArrayList<Expression>();
		for (int i = 0; i < altPropertiesFile.getNumProperties(); i++) {
			System.out.println(altPropertiesFile.getProperty(i));
			// so reward + safety
			boolean isSafeExpr = false;
			Expression exprHere = altPropertiesFile.getProperty(i);
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

		ProbModelChecker pmc = new ProbModelChecker(prism);
		oldproduct = mdp;

		ArrayList<List<Expression>> labelExprsList = new ArrayList<List<Expression>>();
		ArrayList<DA<BitSet, ? extends AcceptanceOmega>> das = new ArrayList<DA<BitSet, ? extends AcceptanceOmega>>();

		for (int i = 0; i < processedExprs.size(); i++) {
			List<Expression> labelExprs = new ArrayList<Expression>();

			Expression expr = (Expression) processedExprs.get(i);
			expr = (Expression) expr.expandPropRefsAndLabels(altPropertiesFile, modulesFile.getLabelList());
			da = ltlMC.constructExpressionDAForLTLFormula(expr, labelExprs, allowedAcceptance);
			da.setDistancesToAcc();
			BitSet daAccStates = da.getAccStates();
			PrismLog out = new PrismFileLog(resultsLocation + "da_" + i + ".dot");
			// printing the da
			da.print(out, "dot");
			out.close();
			labelExprsList.add(labelExprs);
			das.add(da);
		}
		// lastly the safety expr
		Expression expr = Expression.Not(safetyExpr);
		expr = (Expression) expr.expandPropRefsAndLabels(altPropertiesFile, modulesFile.getLabelList());
		List<Expression> labelExprs = new ArrayList<Expression>();
		da = ltlMC.constructExpressionDAForLTLFormula(expr, labelExprs, allowedAcceptance);
		da.setDistancesToAcc();
		BitSet daAccStates = da.getAccStates();
		PrismLog out = new PrismFileLog(resultsLocation + "da_safety.dot");
		// printing the da
		da.print(out, "dot");
		out.close();
		labelExprsList.add(labelExprs);
		das.add(da);

		NestedProductModelGenerator nppgen = new NestedProductModelGenerator(prismModelGen, das, labelExprsList,
				das.size() - 1);
		// so we get the initial state
		// but possibly lets do this the right way
		// so we want no not the safety da
		List<State> initStates = nppgen.getInitialStates();
		Queue<State> q = new LinkedList<State>();
		Queue<State> visited = new LinkedList<State>();
		q.addAll(initStates);
		int numrewards = nppgen.getNumRewardStructs();
		while (!q.isEmpty()) {
			State s = q.remove();
			if (!visited.contains(s)) {
				visited.add(s);
				System.out.println("Visiting: " + s);
				// lets get its children ?
				nppgen.exploreState(s);
				int choices = nppgen.getNumChoices();
				for (int c = 0; c < choices; c++) {
					String choiceString = "";
					Object action = nppgen.getChoiceAction(c);
					choiceString += "Choice " + c + " - " + action.toString();
//					System.out.println(action);
					int transitions = nppgen.getNumTransitions(c);
					for (int t = 0; t < transitions; t++) {
						double prob = nppgen.getTransitionProbability(c, t);
						State ns = nppgen.computeTransitionTarget(c, t);
						choiceString += " " + ns.toString() + ":" + prob + " ";
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
						double rew = nppgen.getStateActionReward(r, action);
						if (rew != nppgen.getStateActionReward(r, s, action))
							choiceString += " rews not equal?? ";
						choiceString += " r" + r + ": " + rew;
					}
					System.out.println(choiceString);
				}
			}
		}
//		prodModelGen = new ProductModelGenerator(prismModelGen, da, labelExprs);
//		prism.loadModelGenerator(prismModelGen);

	}

}
