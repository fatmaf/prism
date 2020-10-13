package thtsNew;

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
import java.util.concurrent.TimeUnit;

import acceptance.AcceptanceOmega;
import acceptance.AcceptanceType;
import automata.DA;
import explicit.LTLModelChecker;
import explicit.MDP;
import explicit.MDPModelChecker;
import explicit.MDPSimple;
import explicit.ProbModelChecker;
import explicit.rewards.ConstructRewards;
import explicit.rewards.MDPRewardsSimple;
import parser.State;
import parser.ast.Expression;
import parser.ast.ExpressionQuant;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import prism.Model;
import prism.Prism;
import prism.PrismDevNullLog;
import prism.PrismException;
import prism.PrismFileLog;
import prism.PrismLog;
import prism.RewardGenerator;
import simulator.ModulesFileModelGenerator;
import thts.Objectives;
import thts.PolicyCreator;
import thtsNew.MDPValIter.ModelCheckerMultipleResult;

public class TestProdGenVI {
	
	// PRISM_MAINCLASS=thtsNew.TestProdGenVI prism/bin/prism
	public static void main(String[] args) {
		try {
			String resString = "";
			String resLine;
			ArrayList<Double> res;
			String currentDir = System.getProperty("user.dir");
			String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
			String resultsLocation = testsLocation + "results/csvs/";
			
			FileWriter fw = new FileWriter(resultsLocation+"vi_res.txt", true);
		    BufferedWriter bw = new BufferedWriter(fw);
		    PrintWriter out = new PrintWriter(bw);
		    
			TestProdGenVI tpgvi = new TestProdGenVI(); 
			res = tpgvi.unavoidable(false);
			resLine="\nunavoidable:\tprob:"+res.get(0)+"\ttc:"+res.get(1)+"\tcost:"+res.get(2);
			resString+=resLine;
			out.print(resLine);
			
			for(int i = 0; i<=100; i+=10)
			{
		
				res = tpgvi.grid5(false, i); 
				resLine="\ngrid5-"+i+"-:\tprob:"+res.get(0)+"\ttc:"+res.get(1)+"\tcost:"+res.get(2);
				out.print(resLine);
				resString+=resLine;
			}
			out.close();
			bw.close();
			fw.close();
			System.out.println(resString);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public ArrayList<Double> grid5(boolean debug,int fsp) throws Exception
	{
	

		boolean hasSharedState = false;




		
		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/grid5/"+fsp+"/";
		String resultsLocation = testsLocation + "results/justvi/";
		// making sure resultsloc exits
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);

		String[] examples= {"r10_g10_a1_grid_5_fsp_0_0_", "r10_g10_a1_grid_5_fsp_10_1_"
				,"r10_g10_a1_grid_5_fsp_20_2_","r10_g10_a1_grid_5_fsp_30_3_","r10_g10_a1_grid_5_fsp_40_4_"
				,"r10_g10_a1_grid_5_fsp_50_5_","r10_g10_a1_grid_5_fsp_60_6_","r10_g10_a1_grid_5_fsp_70_7_"
				,"r10_g10_a1_grid_5_fsp_80_8_","r10_g10_a1_grid_5_fsp_90_9_","r10_g10_a1_grid_5_fsp_100_0_"};
		String example = examples[fsp/10];//r10_g10_a1_grid_5_fsp_0_0_9//"tro_example_new_small";

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
		String combString = "_vi_" + tieBreakingOrderStr ;
		String algoIden = "_avoid_lrtdp" + combString;
		PrismLog fileLog = new PrismFileLog(resultsLocation + "log_" + example + algoIden + "_justmdp" + ".txt");//

		prism.initialise();
		prism.setEngine(Prism.EXPLICIT);

		mainLog.println("Initialised Prism");

		int numModels = 3;
		ArrayList<String> filenames = new ArrayList<>();

		for (int numModel = 0; numModel < numModels; numModel++) {
			String modelFileName = testsLocation + example + numModel + ".prism";
			filenames.add(modelFileName);
		}

		String propertiesFileName = testsLocation + example + "mult.prop";

		mainLog.println("Generating Single Agent Solutions using Nested Products and NVI");
		fileLog.println("Generating Single Agent Solutions using Nested Products and NVI");



		MultiAgentNestedProductModelGenerator maModelGen = createNestedMultiAgentModelGen(prism, mainLog, filenames,
				propertiesFileName, resultsLocation, hasSharedState);

		

		prism.loadModelGenerator(maModelGen);
		prism.buildModel();
		MDP mdp = (MDP)prism.getBuiltModelExplicit();
		System.out.println(mdp.infoStringTable());
		List<State> statesList = mdp.getStatesList(); 
		BitSet accStates = new BitSet(); 
		BitSet avoidStates = new BitSet();
		for(int i = 0; i<statesList.size(); i++) {
			State s = statesList.get(i);
			if(maModelGen.isAccState(s))
				accStates.set(i);
			if(maModelGen.isAvoidState(s))
				avoidStates.set(i);
			
		}
		mainLog.println("AccStates:"+accStates.toString()); 
		mainLog.println("AvoidStates:"+avoidStates.toString());
		ProbModelChecker pmc = new ProbModelChecker(prism);
		
//		pmc.setModelCheckingInfo(modulesFile, propertiesFile, (RewardGenerator) maModelGen);
		MDPModelChecker mdpmc = new MDPModelChecker(pmc);
		ConstructRewards constructRewards = new ConstructRewards(pmc);
		
		MDPRewardsSimple tasksModel = (MDPRewardsSimple) constructRewards.buildRewardStructure(mdp,
				(RewardGenerator) maModelGen, -1);
		
		
		MDPRewardsSimple costsModel = (MDPRewardsSimple) constructRewards.buildRewardStructure(mdp,
				(RewardGenerator) maModelGen, 0);
		

		
		MDPValIter vi = new MDPValIter();
		ArrayList<MDPRewardsSimple> rewardsList = new ArrayList<>(); 
		rewardsList.add(tasksModel); 
		rewardsList.add(costsModel); 
		ArrayList<Boolean> minRewards = new ArrayList<>(); 
		minRewards.add(false); 
		minRewards.add(true); 
		BitSet remain = (BitSet)avoidStates.clone(); 
		remain.flip(0, mdp.getNumStates());
		ModelCheckerMultipleResult result = vi.computeNestedValIterArray(mdpmc, mdp, accStates, remain,
				rewardsList, null, minRewards, null, 1, null, mainLog, resultsLocation,"vistuff");
		
		System.out.println("Probability: "+result.solns.get(0)[mdp.getFirstInitialState()]);
		System.out.println("Task Completition: "+result.solns.get(1)[mdp.getFirstInitialState()]);
		System.out.println("Cost: "+result.solns.get(2)[mdp.getFirstInitialState()]);
		
		PolicyCreator pc = new PolicyCreator(); 
//		pc.createPolicy(mdp, result.strat);
		ArrayList<Double> resVals = pc.createPolicyPrintValues(mdp, result, fileLog);
		pc.savePolicy(resultsLocation, "mehres");
		System.out.println("meh");
		mainLog.close();
		fileLog.close();
		return resVals; 

		
	}
	
	public ArrayList<Double> unavoidable(boolean debug) throws Exception
	{

		boolean hasSharedState = false;

		
		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/justvi/";
		// making sure resultsloc exits
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);

		String example =  "tro_example_new_small";//"tro_example_new_small_onefailaction";
		hasSharedState = true; 
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
		String combString = "_justvi_" + tieBreakingOrderStr;
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

		mainLog.println("Generating Single Agent Solutions using Nested Products and NVI");
		fileLog.println("Generating Single Agent Solutions using Nested Products and NVI");



		MultiAgentNestedProductModelGenerator maModelGen = createNestedMultiAgentModelGen(prism, mainLog, filenames,
				propertiesFileName, resultsLocation, hasSharedState);


		prism.loadModelGenerator(maModelGen);
		prism.buildModel();
		MDP mdp = (MDP)prism.getBuiltModelExplicit();
		System.out.println(mdp.infoStringTable());
		List<State> statesList = mdp.getStatesList(); 
		BitSet accStates = new BitSet(); 
		BitSet avoidStates = new BitSet();
		for(int i = 0; i<statesList.size(); i++) {
			State s = statesList.get(i);
			if(maModelGen.isAccState(s))
				accStates.set(i);
			if(maModelGen.isAvoidState(s))
				avoidStates.set(i);
			
		}

		mainLog.println("AccStates:"+accStates.toString()); 
		mainLog.println("AvoidStates:"+avoidStates.toString());
		ProbModelChecker pmc = new ProbModelChecker(prism);
		
//		pmc.setModelCheckingInfo(modulesFile, propertiesFile, (RewardGenerator) maModelGen);
		MDPModelChecker mdpmc = new MDPModelChecker(pmc);
		ConstructRewards constructRewards = new ConstructRewards(pmc);
		
		MDPRewardsSimple tasksModel = (MDPRewardsSimple) constructRewards.buildRewardStructure(mdp,
				(RewardGenerator) maModelGen, -1);
		
		
		MDPRewardsSimple costsModel = (MDPRewardsSimple) constructRewards.buildRewardStructure(mdp,
				(RewardGenerator) maModelGen, 0);
		

		
		MDPValIter vi = new MDPValIter();
		ArrayList<MDPRewardsSimple> rewardsList = new ArrayList<>(); 
		rewardsList.add(tasksModel); 
		rewardsList.add(costsModel); 
		ArrayList<Boolean> minRewards = new ArrayList<>(); 
		minRewards.add(false); 
		minRewards.add(true); 
		BitSet remain = (BitSet)avoidStates.clone(); 
		remain.flip(0, mdp.getNumStates());
		ModelCheckerMultipleResult result = vi.computeNestedValIterArray(mdpmc, mdp, accStates, remain,
				rewardsList, null, minRewards, null, 1, null, mainLog, resultsLocation,"vistuff");
		
		System.out.println("Probability: "+result.solns.get(0)[mdp.getFirstInitialState()]);
		System.out.println("Task Completition: "+result.solns.get(1)[mdp.getFirstInitialState()]);
		System.out.println("Cost: "+result.solns.get(2)[mdp.getFirstInitialState()]);
		
		PolicyCreator pc = new PolicyCreator(); 
//		pc.createPolicy(mdp, result.strat);
		ArrayList<Double> resVals = pc.createPolicyPrintValues(mdp, result, fileLog);
		pc.savePolicy(resultsLocation, "mehres");
		System.out.println("meh");
		mainLog.close();
		fileLog.close();
		return resVals; 

		
	}
	private void testString(int testNum, int maxTests,long durMS)
	{
		System.out.println("Test "+testNum+"/"+maxTests); 
		long times = TimeUnit.SECONDS.convert(durMS, TimeUnit.MILLISECONDS);
		long timeM = TimeUnit.MINUTES.convert(durMS, TimeUnit.MILLISECONDS);
		long timeH = TimeUnit.HOURS.convert(durMS, TimeUnit.MILLISECONDS);
		System.out.println("Time: "+times+"(s)/"+timeM+"(min)/"+timeH+"(h)");
		
		long avgTime = 0; 
		if(testNum> 0)
			avgTime=durMS/(testNum);
		long estTime = avgTime*(maxTests-testNum);

		long etimes = TimeUnit.SECONDS.convert(estTime, TimeUnit.MILLISECONDS);
		long etimeM = TimeUnit.MINUTES.convert(estTime, TimeUnit.MILLISECONDS);
		long etimeH = TimeUnit.HOURS.convert(estTime, TimeUnit.MILLISECONDS);
		System.out.println("ET End: "+etimes+"(s)/"+etimeM+"(min)/"+etimeH+"(h)");
	}

		public void createDirIfNotExist(String directoryName) {
			File directory = new File(directoryName);
			if (!directory.exists()) {
//				directory.mkdir();
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
//			System.in.read();
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
//				mainLog.println("Writing to " + resultsLocation + "da_" + i + ".dot");
//				PrismLog out = new PrismFileLog(resultsLocation + "da_" + i + ".dot");
//				// printing the da
//				da.print(out, "dot");
//				out.close();
				labelExprsList.add(labelExprs);
				das.add(da);
				mainLog.println("Created DA for " + expr.toString());
			}

			ArrayList<String> sharedStateVars = new ArrayList<String>();
			if (hasSharedState)
				sharedStateVars.add("door0");
//			sharedStateVars = null;
			MultiAgentNestedProductModelGenerator mapmg = new MultiAgentNestedProductModelGenerator(mfmodgens, das,
					labelExprsList, safetydaind, sharedStateVars);

			return mapmg;
		}


		

}
