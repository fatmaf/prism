package thtsNew;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import acceptance.AcceptanceOmega;
import automata.DA;
import parser.State;
import parser.VarList;
import parser.ast.Expression;
import parser.type.Type;
import parser.type.TypeInt;
import prism.ModelGenerator;
import prism.ModelType;
import prism.PrismException;
import prism.PrismLangException;
import prism.RewardGenerator;
import simulator.ModulesFileModelGenerator;

public class MultiAgentNestedProductModelGenerator implements ModelGenerator, RewardGenerator {

	// a list of models
	// a list of das
	protected ArrayList<ModulesFileModelGenerator> modelGens = null;
	/** The list of DAs used to build the product */
	protected ArrayList<DA<BitSet, ? extends AcceptanceOmega>> das = null;
	/** The expressions for the labels (APs) in the DAs */
	protected ArrayList<List<Expression>> labelExprs = null;
	/** Variable names for DAs states */
	protected ArrayList<String> daVars;
	/** Number of APs in the DAs */
	protected ArrayList<Integer> numAPs;
	/** Number of variables (num model vars + num das) */
	protected int numVars;
	/** Variable names */
	protected List<String> varNames;
	/** Variable types */
	protected List<Type> varTypes;
	/** Name for new acceptance label */
	protected String accLabel;
	/** Label names */
	protected List<String> labelNames;

	/** BitSet */
	protected ArrayList<BitSet> bsLabels;

	/** State to be explored in product */
	protected State exploreState;
	/** The model part of exploreState */
	protected ArrayList<State> exploreModelState;
	/** The DA part of exploreState */
	protected ArrayList<Integer> exploreDaState;
	/** The choice string combinations **/ 
	protected ArrayList<ArrayList<Integer>> exploreStateChoiceCombs; 
	/** The transitions list for this choice **/ 

	protected ArrayList<ArrayList<Entry<State,Double>>> exploreStateChoiceTransitionCombs; 
	

	//to think about 
	//do we want to save things in memory to stop us from computing them ?
	//how is it in the previous stuff 
	//we'll just generate it again 
	//its fine 
	
	
	
	protected ArrayList<Integer> numModelVars;
	protected int numDAs;
	protected int numModels;

	protected int safetyDAIndex;
	protected ArrayList<String> sharedVarsList;
	protected int numModelVarsAll;

	// this is just for ease of use
	// the string is basically the model number
	// excludes shared vars
	protected HashMap<Integer, ArrayList<Integer>> modelVarIndices;
	protected HashMap<Integer, ArrayList<Integer>> sharedVarIndices;

	public MultiAgentNestedProductModelGenerator(ArrayList<ModulesFileModelGenerator> modelGens,
			ArrayList<DA<BitSet, ? extends AcceptanceOmega>> das, ArrayList<List<Expression>> labelExprs,
			int safetyDAIndex, ArrayList<String> sharedvarslist) {
		initialise(modelGens, das, labelExprs, safetyDAIndex, sharedvarslist);

	}

	// why is this here
	// because i love overloading and it just makes things easy
	// cuz I lazy!!! suraj ka darwaza khula!!
	private void initialise(ArrayList<ModulesFileModelGenerator> modelGens,
			ArrayList<DA<BitSet, ? extends AcceptanceOmega>> das, ArrayList<List<Expression>> labelExprs,
			int safetyDAIndex, ArrayList<String> sharedvarslist) {
		this.modelGens = modelGens;
		this.das = das;
		this.labelExprs = labelExprs;
		this.safetyDAIndex = safetyDAIndex;
		this.sharedVarsList = sharedvarslist;
		daVars = new ArrayList<String>();
		numAPs = new ArrayList<Integer>();
		varNames = new ArrayList<>();
		varTypes = new ArrayList<>();
		numModelVars = new ArrayList<>();
		// so we have a list of das
		// each has a number and an index
		numDAs = das.size();
		numModels = modelGens.size();
		labelNames = new ArrayList<>();

		modelVarIndices = new HashMap<>();
		sharedVarIndices = new HashMap<>();
		// find the beginning of the da variable
		// just incase we've been passed a model with a da
		int daStartNumber = 0;
		String expectedDAVar = "_da" + daStartNumber;
		ModulesFileModelGenerator modelGen;

		ArrayList<Type> sharedVarTypes = null;
		if (this.sharedVarsList != null)
			sharedVarTypes = new ArrayList<>(this.sharedVarsList.size());
		for (int mdnum = 0; mdnum < modelGens.size(); mdnum++) {
			modelGen = modelGens.get(mdnum);
			int modelVarsExpSS = 0;
			ArrayList<Integer> modelVarIndicesHere = new ArrayList<>();
			// so we need to process the shared variables stuff here
			try {
				for (int vnum = 0; vnum < modelGen.getNumVars(); vnum++) {
					boolean issharedvar = false;
					// TODO: we might have to change or append to these names
					// given that they're similar
					// if they weren't it'd be fine
					// but we can do this later
					String varname = modelGen.getVarName(vnum);
					Type varType;

					varType = modelGen.getVarType(vnum);

					if (this.sharedVarsList != null) {
						if (this.sharedVarsList.contains(varname)) {
							// skip it
							issharedvar = true;

							// get the index
							int sharedVarInd = sharedVarsList.indexOf(varname);
							if (sharedVarInd != -1) {
								while (sharedVarTypes.size() <= sharedVarInd) {
									sharedVarTypes.add(null);
								}
								if (sharedVarTypes.get(sharedVarInd) == null) {
									sharedVarTypes.set(sharedVarInd, varType);
								}
								if (!sharedVarIndices.containsKey(sharedVarInd)) {
									sharedVarIndices.put(sharedVarInd, new ArrayList<>());
								}
								while (sharedVarIndices.get(sharedVarInd).size() <= mdnum)
									sharedVarIndices.get(sharedVarInd).add(null);
								sharedVarIndices.get(sharedVarInd).set(mdnum, vnum);

							}
						}
					}
					if (!issharedvar) {
						modelVarsExpSS++;
						varNames.add(varname);
						varTypes.add(varType);
						modelVarIndicesHere.add(vnum);
					}
//			varNames.addAll(modelGen.getVarNames());
//			varTypes.addAll(modelGen.getVarTypes());
//			numModelVars.add(modelGen.getNumVars());
					modelVarIndices.put(mdnum, modelVarIndicesHere);
				}
			} catch (PrismException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			numModelVars.add(modelVarsExpSS);
			labelNames.addAll(modelGen.getLabelNames());
			while (modelGen.getVarIndex(expectedDAVar) != -1) {
				daStartNumber = daStartNumber + 1;
				expectedDAVar = "_da" + daStartNumber;
			}

		}
		if (this.sharedVarsList != null) {
			// now lets add the shared states
			varNames.addAll(this.sharedVarsList);
			varTypes.addAll(sharedVarTypes);
		}
		numModelVarsAll = varNames.size();
		bsLabels = new ArrayList<BitSet>();
		// so our davars are the same size as numdas
		for (int d = 0; d < das.size(); d++) {
			expectedDAVar = "_da" + (daStartNumber + d);
			daVars.add(expectedDAVar);
			numAPs.add(das.get(d).getAPList().size());
			varNames.add(expectedDAVar);
			varTypes.add(TypeInt.getInstance());
			bsLabels.add(new BitSet(numAPs.get(d)));

		}
		accLabel = "_acc";

		labelNames.add(accLabel);
		numVars = varNames.size();
		// so really i've got to really understand this whole thing now which should be
		// fun

	}

	@Override
	public ModelType getModelType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getVarNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Type> getVarTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public VarList createVarList() throws PrismException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Find the successor of state {@code q} in the DA, taking the edge whose
	 * labelling matches the state {@code s}.
	 * 
	 * @throws Exception
	 */
	protected int getDASuccessor(int danum, int q, ArrayList<State> robotStates) throws PrismException {

		BitSet bsLabels = null;
		// so technically we've got to or the bit sets
		for (int r = 0; r < robotStates.size(); r++) {
			BitSet currentRobotLabels = getDABitSet(danum, robotStates.get(r));
			if (bsLabels == null)
				bsLabels = (BitSet) currentRobotLabels.clone();
			else
				bsLabels.or(currentRobotLabels);
		}
		// Find/return successor
		DA<BitSet, ? extends AcceptanceOmega> da = das.get(danum);
		return da.getEdgeDestByLabel(q, bsLabels);
	}

	private BitSet getDABitSet(int danum, State robotState) throws PrismException {
		State s = robotState;
		DA<BitSet, ? extends AcceptanceOmega> da = das.get(danum);
		BitSet bsLabelsda = new BitSet(numAPs.get(danum));
		List<Expression> labelExprsda = labelExprs.get(danum);
		for (int k = 0; k < numAPs.get(danum); k++) {
			bsLabelsda.set(k,
					labelExprsda.get(Integer.parseInt(da.getAPList().get(k).substring(1))).evaluateBoolean(s));
		}
		return bsLabelsda;

	}

	public State createSharedState(ArrayList<State> currentrobotStates, ArrayList<State> previousrobotStates) {
		// if previousrobotStates is null then we only care about these states
		// they should all be the same then
		// if they're not its an issue
		State toret = new State(sharedVarsList.size());
		if (previousrobotStates == null) {
			for (int ss = 0; ss < sharedVarsList.size(); ss++) {

				ArrayList<Integer> sharedvarrobotinds = sharedVarIndices.get(ss);
				int ssval = ((Integer) currentrobotStates.get(0).varValues[sharedvarrobotinds.get(0)]).intValue();
				for (int r = 1; r < sharedvarrobotinds.size(); r++) {
					int rind = sharedvarrobotinds.get(r);
					int valhere = ((Integer) currentrobotStates.get(r).varValues[rind]).intValue();
					if (valhere != ssval) {
						// error
						System.out.println("Error");
						ssval = valhere;
					}
				}
				toret.setValue(ss, ssval);
			}
		} else {
			for (int ss = 0; ss < sharedVarsList.size(); ss++) {

				ArrayList<Integer> sharedvarrobotinds = sharedVarIndices.get(ss);
				int ssval = ((Integer) currentrobotStates.get(0).varValues[sharedvarrobotinds.get(0)]).intValue();
				int psval = ((Integer) previousrobotStates.get(0).varValues[sharedvarrobotinds.get(0)]).intValue();
				boolean updated = false;
				if (psval != ssval) {
					updated = true;
				}
				for (int r = 1; r < sharedvarrobotinds.size(); r++) {
					int rind = sharedvarrobotinds.get(r);
					int cvalhere = ((Integer) currentrobotStates.get(r).varValues[rind]).intValue();
					int pvalhere = ((Integer) previousrobotStates.get(r).varValues[rind]).intValue();
					if (pvalhere != cvalhere) {
						if (cvalhere != ssval) {
							// if they're not the same
							// then we should probably take the new value
							// unless there's been an update already
							// if there is an update then we'll just take the bigger value
							// which is a bit silly but hey
							if (!updated) {
								ssval = cvalhere;
								updated = true;
							} else {
								// so we're taking the bigger value which is a bit silly and should not happen
								// perhaps we need a rule that rules out combinations where that happens
								// well that will need to happen later
								// TODO: rule out combinations where similar things happen forexample the same
								// door is open/closed
								if (cvalhere > ssval)
									ssval = cvalhere;
							}
						}
					}
//					if (cvalhere != ssval) {
//						// error
//						System.out.println("Error");
//						ssval = valhere;
//					}
				}
				toret.setValue(ss, ssval);
			}
		}
		return toret;
	}

	public State createCombinedRobotState(ArrayList<State> robotStates,ArrayList<State> prevStates) {
		// so now this is the tricky part
		// this is actually going to be fun

		State combinedRobotState = new State(numModelVarsAll - this.sharedVarsList.size());

		for (int r = 0; r < modelGens.size(); r++) {
			// assuming no shared states
			// TODO: Fix this for shared states man
			State rs = robotStates.get(r);
			// we only care about state indices which are not shared state indices
			// for shared state ones we've got to do other things
			// so its useful to store the indices for things we care about

			ArrayList<Integer> indicesToCareAbout = modelVarIndices.get(r);
			int offset = 0;
			if (r > 0)
				offset += this.numModelVars.get(r - 1);
			for (int i = 0; i < indicesToCareAbout.size(); i++) {
				int rsInd = indicesToCareAbout.get(i);
				combinedRobotState.setValue(offset + i, rs.varValues[rsInd]);
			}
//			combinedRobotState.setValue(r, robotStates.get(r));
		}
		State combinedsharedstate = this.createSharedState(robotStates, prevStates);
		State toret = new State(combinedRobotState, combinedsharedstate);
		return toret;
	}

	public State createCombinedDAState(ArrayList<State> robotStates, boolean daInitStates) throws PrismException {
		// so now we need to this for each da and each robot state
		State combinedDAState = new State(das.size());
		for (int d = 0; d < das.size(); d++) {
			int daStateHere;
			if (daInitStates)
				daStateHere = das.get(d).getStartState();
			else
				daStateHere = exploreDaState.get(d);
			int daInitState = getDASuccessor(d, daStateHere, robotStates);
			combinedDAState.setValue(d, daInitState);
		}
		return combinedDAState;
	}

	private State createCombinedRobotDAState(ArrayList<State> robotStates, ArrayList<State> previousStates,boolean daInitStates) throws PrismException {
		State combinedRobotState = createCombinedRobotState(robotStates,previousStates);
		State combinedDAState = createCombinedDAState(robotStates, daInitStates);
		return new State(combinedRobotState, combinedDAState);
	}

	protected ArrayList<ArrayList<State>> getInitialRobotStatesCombinations() throws PrismException {
		// for each robot - you've got to do some stuff
		// get the list of initial states
		ArrayList<List<State>> allrobotstates = new ArrayList<>();
		for (int r = 0; r < modelGens.size(); r++) {
			ModulesFileModelGenerator modelGen = modelGens.get(r);
			allrobotstates.add(modelGen.getInitialStates());
		}
		HelperClass<State> hc = new HelperClass<>();
		return hc.generateCombinations(allrobotstates);
	}




	@Override
	public List<State> getInitialStates() throws PrismException {
		List<State> initStates = new ArrayList<>();
		ArrayList<ArrayList<State>> robotinitstatecombs = this.getInitialRobotStatesCombinations();
		for (ArrayList<State> sInit : robotinitstatecombs) {
			// automaton init states

			initStates.add(createCombinedRobotDAState(sInit, null,true));
		}
		return initStates;
	}

	@Override
	public State getInitialState() {
		// first we must get the initial states of all the models
		ArrayList<State> robotStates = new ArrayList<State>();
		State toret = null;
		State sInit;
		try {

			for (ModulesFileModelGenerator modelGen : modelGens) {

				sInit = modelGen.getInitialState();

				robotStates.add(sInit);

			}
			toret = createCombinedRobotDAState(robotStates, null,true);
		} catch (Exception e) {

			e.printStackTrace();
		}
		return toret;
	}

	public ArrayList<State> getModelStates(State exploreState) {
		ArrayList<State> toret = new ArrayList<>();
		int offset = 0;
		for (int r = 0; r < numModelVars.size(); r++) {
			int numVars = modelGens.get(r).getNumVars();
			State modelState = new State(numVars);
			int robotStateVars = numModelVars.get(r);

			ArrayList<Integer> modelVarInds = modelVarIndices.get(r);
			for (int i = 0; i < robotStateVars; i++) {
				// get all the robot state indices
				int robotInd = modelVarInds.get(i);
				modelState.setValue(robotInd, exploreState.varValues[offset + i]);

			}

			offset += robotStateVars;
			while (toret.size() <= r)
				toret.add(null);
			toret.set(r, modelState);

		}

		for (int i = 0; i < sharedVarsList.size(); i++) {
			ArrayList<Integer> sharedVarsInds = sharedVarIndices.get(i);
			for (int r = 0; r < numModelVars.size(); r++) {

				if (sharedVarsInds.size() > r) {
					if (sharedVarsInds.get(r) != null) {
						toret.get(r).setValue(sharedVarsInds.get(r), exploreState.varValues[offset + i]);
					}
				}
			}
		}
		return toret;

	}

	public void printExploreState() {
		if (exploreState != null)
			System.out.println("State " + exploreState.toString());
		if (exploreModelState != null) {
			System.out.println("Robot States: ");
			for (State s : exploreModelState) {
				System.out.println(s.toString());
			}
		}
		if(exploreDaState != null)
		{
			System.out.println("DA States: ");
			for (int s : exploreDaState) {
				System.out.println(s);
			}
		}

	}

	@Override
	public void exploreState(State exploreState) throws PrismException {
		// so the explore state is simply like all the robot states
		// and all the da states
		this.exploreState = exploreState;
		// first we have to create robot states
		// then we've got to create the da states
		// o this is easy
		// we just use the stuff we had earlier
		// no big deal!! yay
		exploreModelState = getModelStates(exploreState);

		for (int r = 0; r < numModelVars.size(); r++) {
			modelGens.get(r).exploreState(exploreModelState.get(r));
		}

		if (exploreDaState == null)
			exploreDaState = new ArrayList<Integer>();
		for (int d = 0; d < das.size(); d++) {
			int daState = getDAState(d, exploreState);// ((Integer) exploreState.varValues[numModelVars +
//														// d]).intValue();
//
			if (exploreDaState.size() <= d)
				exploreDaState.add(daState);
			else
				exploreDaState.set(d, daState);

		}
		exploreStateChoiceCombs = null; 
		exploreStateChoiceTransitionCombs = null;

	}

	protected int getDAState(int danum, State state) {
		return ((Integer) state.varValues[numModelVarsAll + danum]).intValue();

	}

	@Override
	public int getNumChoices() throws PrismException {
		// TODO Auto-generated method stub
	//	return modelGen.getNumChoices();
		//so its a combination of all the choices 
		//so just like x choices 
		//so we just make an array of all the choices for each robot 
		if(exploreStateChoiceCombs == null) {
		 //go through all the model gens and get the choices 
		ArrayList<Integer> numchoices = new ArrayList<>();
		for(int r = 0; r<modelGens.size(); r++)
		{
			numchoices.add(modelGens.get(r).getNumChoices());
		}
		HelperClass<Integer> hc = new HelperClass<>(); 
		//but surely this is not enough 
		//we need to kind of save our choice indices 
		//we could do this on the fly 
		//or perhaps we could do this now 
		//lets do it when we're asked to explore a choice 
		
		return hc.getNumCombsFromSizes(numchoices);
		}
		else
			return exploreStateChoiceCombs.size();
	}
	
	public void generateChoiceCombs() throws PrismException
	{
		ArrayList<List<Integer>> allChoiceActionStrings = new ArrayList<>();
		for(int r = 0; r<modelGens.size(); r++)
		{
			ModulesFileModelGenerator modelGen = modelGens.get(r);
			int numchoice = modelGen.getNumChoices();
			ArrayList<Integer> choiceActionStrings = new ArrayList<>();
			for(int c = 0; c<numchoice; c++)
			{
//				String cas = modelGen.getChoiceActionString(c);
				choiceActionStrings.add(c);
			}
			allChoiceActionStrings.add(choiceActionStrings);	
		}
		//so now we've got to save these cuz this is important 
		//well the combinations 
		HelperClass<Integer> hc = new HelperClass<>(); 
		ArrayList<ArrayList<Integer>> combs = hc.generateCombinations(allChoiceActionStrings);
		exploreStateChoiceCombs = combs; 
	}
	@Override
	public  Object getChoiceAction(int i) throws PrismException
	{
		if(exploreStateChoiceCombs == null) {
			generateChoiceCombs();
		}
		return (Object) createJointAction(exploreStateChoiceCombs.get(i));
		//but there is more 
		//we need to get all the actions 
	}
	String createJointAction(ArrayList<Integer> robotChoices) throws PrismException
	{
		String toret = ""; 
		String sep = ","; 
		for(int r = 0; r<robotChoices.size(); r++)
		{
			ModulesFileModelGenerator modelGen = modelGens.get(r);
			String cas = modelGen.getChoiceActionString(robotChoices.get(r));
			toret +=cas+sep; 
		}
		return toret; 
	}
	@Override
	public int getNumTransitions(int i) throws PrismException {
		if(exploreStateChoiceCombs == null) {
			generateChoiceCombs();
		}
		exploreStateChoiceTransitionCombs = null;
		//get the number of transitions here 
		ArrayList<Integer> robotTrans = exploreStateChoiceCombs.get(i);
		//so this is all the robot choices 
		//now we've got to create combinations 
		ArrayList<Integer> transNums = new ArrayList<>(); 
		
		for(int r = 0; r<modelGens.size(); r++)
		{
			int numTrans = modelGens.get(r).getNumTransitions(robotTrans.get(r));
			transNums.add(numTrans);
		}
		HelperClass<Integer> hc = new HelperClass<>(); 
		
		return hc.getNumCombsFromSizes(transNums);
	}

	@Override
	public Object getTransitionAction(int i, int offset) throws PrismException {
		// TODO Auto-generated method stub
		return null;
	}

	private void computeCurrentChoiceTransitionCombinations(ArrayList<Integer> robotChoices) throws PrismException
	{
	//so we've got the choice and the states 
		ArrayList<List<Entry<State,Double>>> allTransitionOptions = new ArrayList<>();
		for(int r = 0; r<modelGens.size(); r++)
		{
			ModulesFileModelGenerator modelGen = modelGens.get(r);
			int currChoice = robotChoices.get(r); 
			int numTransitions =modelGen.getNumTransitions(currChoice); 
			ArrayList<Entry<State,Double>> choiceTransitionTargets = new ArrayList<>();
			for(int t = 0; t<numTransitions; t++)
			{
				State target = modelGen.computeTransitionTarget(currChoice, t);
				double prob = modelGen.getTransitionProbability(currChoice, t); 
				choiceTransitionTargets.add(new AbstractMap.SimpleEntry<State,Double>(target,prob));
			}
			

			allTransitionOptions.add(choiceTransitionTargets);	
		}
		HelperClass<Entry<State,Double>> hc = new HelperClass<>(); 
		ArrayList<ArrayList<Entry<State, Double>>> combs = hc.generateCombinations(allTransitionOptions); 
		exploreStateChoiceTransitionCombs = combs; 

		
	}
	@Override
	public double getTransitionProbability(int i, int offset) throws PrismException {
		// TODO Auto-generated method stub
		if(exploreStateChoiceTransitionCombs == null)
		{
			computeCurrentChoiceTransitionCombinations(exploreStateChoiceCombs.get(i));
		}
		//the offset 
		ArrayList<Entry<State, Double>> stateCombs = exploreStateChoiceTransitionCombs.get(offset);
		//probability = just those states together 
		double prob = 1; 
		for (Entry<State,Double> e : stateCombs)
		{
			prob *=e.getValue(); 
		}
		return prob;
	}

	@Override
	public State computeTransitionTarget(int i, int offset) throws PrismException {
		// TODO Auto-generated method stub
		if(exploreStateChoiceTransitionCombs == null)
		{
			computeCurrentChoiceTransitionCombinations(exploreStateChoiceCombs.get(i));
		}
		ArrayList<Entry<State, Double>> stateCombs = exploreStateChoiceTransitionCombs.get(offset);
		ArrayList<State> robotStates = new ArrayList<>(); 
		for (Entry<State,Double> e : stateCombs)
		{
			robotStates.add(e.getKey());
		}
		State toret = createCombinedRobotDAState(robotStates,exploreModelState, false);
		return toret; 
	}

}
