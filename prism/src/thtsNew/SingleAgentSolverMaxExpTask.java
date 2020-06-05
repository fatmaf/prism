package thtsNew;

import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;

import acceptance.AcceptanceType;
import explicit.DTMCFromMDPMemorylessAdversary;
import explicit.LTLModelChecker;
import explicit.MDP;
import explicit.MDPModelChecker;
import explicit.ProbModelChecker;
import explicit.rewards.ConstructRewards;
import explicit.rewards.MDPRewardsSimple;
import parser.State;
import parser.ast.Expression;
import parser.ast.ExpressionReward;
import parser.ast.ExpressionQuant;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import prism.Prism;
import prism.PrismException;
import prism.PrismFileLog;
import prism.PrismLog;
import prism.RewardGenerator;
import simulator.ModulesFileModelGenerator;
import thts.Objectives;
import thts.PolicyCreator;
import thtsNew.MDPValIter.ModelCheckerMultipleResult;

public class SingleAgentSolverMaxExpTask {

	Prism prism;
	PrismLog mainLog;
	ModulesFile modulesFile;
	PropertiesFile propertiesFile;
	List<ExpressionReward> exprRews;
	Expression exprSafety;
	List<Expression> exprOthers;
	String resLoc;
	String name = null;

	// pretty much does nothing but get the nvi solution for the single agent
	// yeah
	// this can be an interface later
	// but all this should do is return the vi values for each state
	// or values for each state action pair
	public SingleAgentSolverMaxExpTask(String resLoc) {

		initialise(null, null, resLoc);
	}

	public SingleAgentSolverMaxExpTask(Prism prism, PrismLog log, String resLoc) {
		initialise(prism, log, resLoc);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	private void initialise(Prism prism, PrismLog log, String resLoc) {
		this.resLoc = resLoc;
		if (log != null)
			this.mainLog = log;
		else {
			mainLog = new PrismFileLog("stdout");
			// initialise a new mainlog cuz we need it
		}

		if (prism != null)
			this.prism = prism;
		else {
			prism = new Prism(mainLog);
			try {
				prism.initialise();
				prism.setEngine(Prism.EXPLICIT);
			} catch (PrismException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// initialise a new prism thing cuz we need it
		}

	}

	public void loadModelAndProperties(String modelFileName, String propertiesFileName) throws Exception {
		loadModel(modelFileName);
		loadProperties(propertiesFileName);

	}

	public void loadModel(String modelFileName) throws Exception {
		// loads the prism model
		modulesFile = prism.parseModelFile(new File(modelFileName));
		prism.loadPRISMModel(modulesFile);
		mainLog.println("Loaded model "+modelFileName);
	}

	public void loadProperties(String propertiesFileName) throws Exception {
		// loads the properties
		exprRews = null;
		exprSafety = null;
		propertiesFile = prism.parsePropertiesFile(modulesFile, new File(propertiesFileName));

		exprOthers = new ArrayList<>();
		// lets process all the expressions here
		// cuz we want to do nvi
		for (int i = 0; i < propertiesFile.getNumProperties(); i++) {
			System.out.println(propertiesFile.getProperty(i));
//				// so reward + safety
			Expression exprHere = propertiesFile.getProperty(i);
			if (exprHere instanceof ExpressionReward) {
				ExpressionReward exprRew = (ExpressionReward) exprHere;
				if (exprRews == null) // we're not really expecting multiple rewards
				// but what the hell //lets make it a list
				{
					exprRews = new ArrayList<>();
				}

				exprRews.add(exprRew);

			} 
			
				Expression daExpr = ((ExpressionQuant) exprHere).getExpression();
				boolean isSafeExpr = !Expression.isCoSafeLTLSyntactic(daExpr, true);
				if (isSafeExpr) {
					if (exprSafety != null) // more than one safety expr , we did not expect this
					// so we might and it okay
					// okay
					{
						exprSafety = Expression.And(exprSafety, exprHere);
					} else {
						exprSafety = exprHere;
					}
				} else {
					exprOthers.add(exprHere);
				}
			}
		mainLog.println("Properities "+exprOthers.toString()+" safety "+exprSafety.toString());

	}

	public HashMap<Objectives, HashMap<State, Double>> getSolution() throws Exception {
	
		mainLog.println("Beginning NVI setup");
		AcceptanceType[] allowedAcceptance = { AcceptanceType.RABIN, AcceptanceType.REACH };

		// create the model generator thing for rewards
		ModulesFileModelGenerator prismModelGen = new ModulesFileModelGenerator(modulesFile, prism);
		// build the model
		prism.buildModel();
		// get the mdp
		MDP mdp = (MDP) prism.getBuiltModelExplicit();

		mainLog.println("Built model MDP\n"+mdp.infoStringTable());
		// things we need
		// some of this is probably superfluous and should be changed
		// but alas i'm a bit of a loser
		LTLModelChecker ltlMC = new LTLModelChecker(prism);
		ProbModelChecker pmc = new ProbModelChecker(prism);
		pmc.setModelCheckingInfo(modulesFile, propertiesFile, (RewardGenerator) prismModelGen);
		// an object to use to construct rewards
		ConstructRewards constructRewards = new ConstructRewards(pmc);

		mainLog.println("Creating nested product");
		// create a nestedmdp object for storing the nested mdp and doing stuff
		NestedProductMDP npMDP = new NestedProductMDP(mdp);

		// so do the nested product stuff for all the other expressions
//		so doing this inreverse order so it matches what we would have for mapmg
//		for(int exprnum = exprOthers.size()-1; exprnum>=0; exprnum--)
//		for (Expression exprHere : exprOthers)
		for(int exprnum = 0; exprnum<exprOthers.size(); exprnum++)
		{
			Expression exprHere = exprOthers.get(exprnum);
			npMDP.constructProductModel(exprHere, ltlMC, pmc, allowedAcceptance, resLoc);
			mainLog.println("Added "+exprHere.toString()+" to product \n"+npMDP.getProductModel().infoStringTable());
		}
		// now do it for the reward ones

//		for (ExpressionReward rewExpr : exprRews) {
//			npMDP.constructProductModel(rewExpr, ltlMC, pmc, allowedAcceptance, resLoc);
//		}

		// now do it for the safety one //remember we've anded all the safety ones
		// we've got to not the safetyexpression
//		Expression notSafety = Expression.Not(exprSafety);
		npMDP.constructProductModel(exprSafety, ltlMC, pmc, allowedAcceptance, resLoc, true);
		mainLog.println("Added "+exprSafety.toString()+" to product \n"+npMDP.getProductModel().infoStringTable());

		ArrayList<MDPRewardsSimple> rewardsList = new ArrayList<>();
		ArrayList<Boolean> minRewards = new ArrayList<>();
		// now we're going to create our reward models
		for (ExpressionReward rewExpr : exprRews) {
			mainLog.println("Creating rewards model for " + rewExpr.toString());
			Object rewStructName = rewExpr.getRewardStructIndex();
			int rewStructIndex = prismModelGen.getRewardStructIndex((String) rewStructName);
			MDPRewardsSimple costsModel = (MDPRewardsSimple) constructRewards.buildRewardStructure(mdp,
					(RewardGenerator) prismModelGen, rewStructIndex);
			MDPRewardsSimple mappedRewards = npMDP.mapRewardsToCurrentProduct(costsModel);
			rewardsList.add(mappedRewards);
			// honestly this is hard coding
			minRewards.add(rewExpr.isMin());
		}
		// now get the max task prog stuff
		// uff ye np wali cheez bohat kuch hai
		mainLog.println("Building task rewards");

		MDPRewardsSimple taskProgRewards = npMDP.createTaskRewards();
		rewardsList.add(0, taskProgRewards);
		minRewards.add(0, false);
		// omg omg
		// we're so so close to doing nvi!!!
		// haye haye!!!
		// but furst v must get de target and remain
		if (name != null) {
			PrismLog out = new PrismFileLog(resLoc + "jointmdpstates" + name + ".sta");
			for (int i = 0; i < npMDP.getProductModel().getStatesList().size(); i++) {
				out.println(i + ":" + npMDP.getProductModel().getStatesList().get(i));
			}
			out.close();

			npMDP.getProductModel().exportToPrismExplicitTra(resLoc + "jointmdpstates" + name + ".tra");
		}
		mainLog.println("Getting acc states and states to avoid");
		npMDP.createTargetStates();
		BitSet remain = npMDP.getRemainStates();
		BitSet target = npMDP.getTargetStates();
		// now we're ready for WAR ? what is it good for absolutely nothing

		MDPValIter vi = new MDPValIter();
		// just another thing we need and we should really look into changing this cuz
		// liek why do you need this?
		MDPModelChecker mdpmc = new MDPModelChecker(pmc);
		mainLog.println("Performing NVI");
		ModelCheckerMultipleResult result = vi.computeNestedValIterArray(mdpmc, npMDP.getProductModel(), target, remain,
				rewardsList, null, minRewards, null, 1, null, this.mainLog, resLoc,name);
		// now this is possibly the MOST important bit
		// we've got to go over all the states and save them
		// cuz who the man ?
		// you the man ? no no no no no iits hulk hogan
		// in the corner
		// i hate life
		// the last one is the probability
		// now this is really important to remember so don't mess it up
		// the order is prob,maxtask,other rewards
		// lets just draw the result to check what happens
		if (name != null) {
			PolicyCreator pc = new PolicyCreator();
			pc.createPolicyAllStates(npMDP.getProductModel(), result.strat);
			pc.savePolicy(resLoc, "nvipol" + name);
		}
		ArrayList<Objectives> objs = new ArrayList<Objectives>();
		objs.add(Objectives.Probability);
		objs.add(Objectives.TaskCompletion);
		objs.add(Objectives.Cost);
		HashMap<Objectives, HashMap<State, Double>> solution = new HashMap<Objectives, HashMap<State, Double>>();
		for (int i = 0; i < result.solns.size(); i++) {
			Objectives currobj = objs.get(i);
			HashMap<State, Double> currsolnmap = new HashMap<State, Double>();
			double[] currsoln = result.solns.get(i);
			for (int s = 0; s < currsoln.length; s++) {
				State state = npMDP.getState(s);
				double val = currsoln[s];
				currsolnmap.put(state, val);
				if (npMDP.isInitialState(s)) {
					// print the values
					mainLog.println(currobj.toString() + " value in initial state " + state.toString() + " " + val);
				}
			}
			solution.put(currobj, currsolnmap);
		}
		return solution;

	}
//	public HashMap<Objectives, HashMap<State, Double>> solveUsingPartSatMaxTask(String alternatepropfile)
//			throws PrismException, FileNotFoundException {
//
//		// load the model
//		// get the right function for creating a nested da with the model
//		// do vi on it
//		// done
//
//		ModelGenerator modelGen = this.prismModelGen;
//		ModelInfo modelInfo = this.modulesFile;
//
//		prism.buildModel();
//		MDP mdp = (MDP) prism.getBuiltModelExplicit();
//
//		MDPModelChecker mc = new MDPModelChecker(prism);
//
//		mc.setGenStrat(true);
//		mc.setExportAdv(true);
//
//		// now we've got to load all the properties
//		// then we create a da for each property
//		// if its a safety property we set a flag
//		// and then we go on
//		PropertiesFile altPropertiesFile = prism.parsePropertiesFile(modulesFile, new File(alternatepropfile));
//
//		mc.setModelCheckingInfo(modelInfo, altPropertiesFile, (RewardGenerator) prismModelGen);
//		// so lets find out how many properties there are
//		MDP oldproduct = null;
//
//		ExpressionReward rewExpr = null;
//		Expression safetyExpr = null;
//		ArrayList<Expression> otherExpressions = new ArrayList<Expression>();
//		// assumption a safety expression can not be a reward expression
//
//		for (int i = 0; i < altPropertiesFile.getNumProperties(); i++) {
//			System.out.println(altPropertiesFile.getProperty(i));
//			// so reward + safety
//			Expression exprHere = altPropertiesFile.getProperty(i);
//			if (exprHere instanceof ExpressionReward)
//				rewExpr = (ExpressionReward) exprHere;
//			else {
//				Expression daExpr = ((ExpressionQuant) exprHere).getExpression();
//				boolean isSafeExpr = !Expression.isCoSafeLTLSyntactic(daExpr, true);
//				if (isSafeExpr)
//					safetyExpr = exprHere;
//				else
//					otherExpressions.add(exprHere);
//			}
//		}
//		ProbModelChecker pmc = new ProbModelChecker(prism);
//		oldproduct = mdp;
//		NestedProductMDP npMDP = new NestedProductMDP(mdp);
//		// so now we do the da stuff
//		for (Expression exprHere : otherExpressions) {
//			npMDP.constructProductModel(exprHere, ltlMC, pmc, allowedAcceptance, resLoc);
//		}
//		npMDP.constructProductModel(safetyExpr, ltlMC, pmc, allowedAcceptance, resLoc, true);
////		oldproduct = getProduct(oldproduct, safetyExpr,pmc);
//		// product before
////		MDP prodbeforerew = oldproduct;
//		npMDP.constructProductModel(rewExpr, ltlMC, pmc, allowedAcceptance, resLoc);
////		oldproduct = getProduct(oldproduct, rewExpr,pmc);
//		// so I have the modelinfo and the rewstructure index
//		// lets check this out
//		Object rewStructName = rewExpr.getRewardStructIndex();
//
//		int rewStructIndex = prismModelGen.getRewardStructIndex((String) rewStructName);
//		pmc.setModelCheckingInfo(modelInfo, altPropertiesFile, (RewardGenerator) prismModelGen);
//		ConstructRewards constructRewards = new ConstructRewards(pmc);
//
//		MDPRewardsSimple costsModel = (MDPRewardsSimple) constructRewards.buildRewardStructure(mdp,
//				(RewardGenerator) prismModelGen, rewStructIndex);
//
//		// map this costsModel to the actual MDP model
//		// lets do this
//		MDPRewardsSimple mappedRewards = npMDP.mapRewardsToCurrentProduct(costsModel);
//		MDPRewardsSimple taskProgRewards = npMDP.createTaskRewards();
//// 		computeNestedValIterArray(MDPModelChecker mc,MDP mdp, BitSet target, BitSet remain,
//// 		ArrayList<MDPRewardsSimple> rewards,
//// 				ArrayList<double[]> rewardsInitVal, 
//// 		ArrayList<Boolean> minRewards, BitSet statesToIgnoreForVI, int probPreference, 
//// 		double[] probInitVal,PrismLog mainLog)
//
//		MDPValIter vi = new MDPValIter();
//		ArrayList<Boolean> minrews = new ArrayList<Boolean>();
//		minrews.add(false);
//		minrews.add(true);
//		ArrayList<MDPRewardsSimple> rews = new ArrayList<MDPRewardsSimple>();
//		rews.add(taskProgRewards);
//		rews.add(mappedRewards);
//		npMDP.createTargetStates();
//		BitSet remain = npMDP.getRemainStates();
//		BitSet target = (BitSet) npMDP.acc.clone();
//		MDPModelChecker mdpmc = new MDPModelChecker(pmc);
//
//		vi.computeNestedValIterArray(mdpmc, npMDP.getProductModel(), target, remain, rews, null, minrews, null, 1, null,
//				this.mainLog);
//
////		RewardStruct costStruct = (rewExpr).getRewardStructByIndexObject(modulesFile, modulesFile.getConstantValues());
//
//		// commenting this out because its giving the error Error: Could not evaluate
//		// constant ("failstate", line 166, column 20).
//		// we know this is because I'm not intializing this properly cuz i'm lazy and
//		// prism code is confusing
//		// but its okay we can do this later
//
////		MDPRewardsSimple costsModel = (MDPRewardsSimple) pmc.constructRewards(prodbeforerew, costStruct);
////		for (int i = 0; i < altPropertiesFile.getNumProperties(); i++) {
////			System.out.println(altPropertiesFile.getProperty(i));
////			// so reward + safety
////			Expression exprHere = altPropertiesFile.getProperty(i);
////			ExpressionReward daExprRew = null;
////			// so if its a reward expression simplest to save it for the end honestly
////			// no work to do and who cares
////			// saves time
////
////			if (exprHere instanceof ExpressionReward)
////				daExprRew = (ExpressionReward) exprHere;
////
////			Expression daExpr = ((ExpressionQuant) exprHere).getExpression();
////			boolean isSafeExpr = !Expression.isCoSafeLTLSyntactic(daExpr, true);
////			if (isSafeExpr) {
////				daExpr = Expression.Not(daExpr);
////			}
////
////			Vector<BitSet> labelBS = new Vector<BitSet>();
////			ProbModelChecker pmc = new ProbModelChecker(prism);
////			if (oldproduct == null)
////				oldproduct = mdp;
////			DA<BitSet, ? extends AcceptanceOmega> daHere = ltlMC.constructDAForLTLFormula(pmc, oldproduct, daExpr,
////					labelBS, allowedAcceptance);
////
////			LTLProduct<MDP> product = ltlMC.constructProductModel(daHere, oldproduct, labelBS, null);
////			oldproduct = product.getProductModel();
////			BitSet daHereAccStates = daHere.getAccStates();
////			PrismLog out = new PrismFileLog(resLoc + "da" + i + ".dot");
////			// printing the da
////			daHere.print(out, "dot");
////			out.close();
////			// prodModelGen = new ProductModelGenerator(prismModelGen, daHere, labelExprs);
////			// prism.loadModelGenerator(prismModelGen);
////
////		}
//		// so now that we have all the DAs what do we do ?
//		// now we do the nested product stuff
//		// we don't have to do any essential state stuff so thats good
//		// we just need to know the final and initial states for each DA and then we can
//		// move on
//		// step 1 for each da lets do it in the loop
//		// then we can save whatever we want okay
//		// yes!!!
//
//		return null;
//	}

}
