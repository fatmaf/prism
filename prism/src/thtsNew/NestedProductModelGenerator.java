package thtsNew;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import acceptance.AcceptanceOmega;
import acceptance.AcceptanceReach;
import automata.DA;
import parser.State;
import parser.Values;
import parser.VarList;
import parser.ast.Declaration;
import parser.ast.DeclarationInt;
import parser.ast.Expression;
import parser.ast.RewardStruct;
import parser.type.Type;
import parser.type.TypeInt;
import prism.DefaultModelGenerator;
import prism.ModelGenerator;
import prism.ModelType;
import prism.PrismException;
import prism.PrismLangException;
import prism.RewardGenerator;
import simulator.ModulesFileModelGenerator;

public class NestedProductModelGenerator extends DefaultModelGenerator {// implements ModelGenerator, RewardGenerator {

	protected ModulesFileModelGenerator modelGen = null;
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
	protected State exploreModelState;
	/** The DA part of exploreState */
	protected ArrayList<Integer> exploreDaState;

	protected int numModelVars;
	protected int numDAs;

	protected int safetyDAIndex;

	public NestedProductModelGenerator(ModulesFileModelGenerator modelGen,
			ArrayList<DA<BitSet, ? extends AcceptanceOmega>> das, ArrayList<List<Expression>> labelExprs,
			int safetyDAIndex) {

		this.modelGen = modelGen;
		this.das = das;
		this.labelExprs = labelExprs;
		this.safetyDAIndex = safetyDAIndex;

		// so we have a list of das
		// each has a number and an index
		int numDAs = das.size();
		// find the beginning of the da variable
		// just incase we've been passed a model with a da
		int daStartNumber = 0;
		String expectedDAVar = "_da" + daStartNumber;
		while (modelGen.getVarIndex(expectedDAVar) != -1) {
			daStartNumber = daStartNumber + 1;
			expectedDAVar = "_da" + daStartNumber;
		}
		daVars = new ArrayList<String>();
		numAPs = new ArrayList<Integer>();
		varNames = new ArrayList<>();
		varNames.addAll(modelGen.getVarNames());
		varTypes = new ArrayList<>();
		varTypes.addAll(modelGen.getVarTypes());
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
		labelNames = new ArrayList<String>(modelGen.getLabelNames());
		labelNames.add(accLabel);
		numVars = varNames.size();
		numModelVars = modelGen.getNumVars();
		numDAs = das.size();

	}
	// Accessors

	public List<String> getDAVarNames() {
		return daVars;
	}

	public String getDAVarName(int numda) {
		if (numda < numDAs)
			return daVars.get(numda);
		else
			return null;
	}

	// fatma added this
	// cuz I couldnt figure out how to get the label expressions otherwise
	// and I need them
	public int getNumLabelExprs(int numda) {
		return this.labelExprs.get(numda).size();
	}

	/**
	 * Assuming the product is build with a reach acceptance, is the state currently
	 * being explored a goal state?
	 */
	public boolean isReachAcceptanceGoalState() {
		// an accepting state is when every da is in an accepting state
		boolean toret = true;

		for (int i = 0; i < das.size(); i++) {
			DA<BitSet, ? extends AcceptanceOmega> da = das.get(i);
			AcceptanceOmega acc = da.getAcceptance();
			if (!(acc instanceof AcceptanceReach)) {
				toret = false;
				break; // kya?
			}
			if (i == safetyDAIndex) {
				continue;
			}

			AcceptanceReach accReach = (AcceptanceReach) acc;
			boolean isacc = accReach.getGoalStates().get(exploreDaState.get(i));
			toret = toret & isacc;
			if (!toret)
				break;
//		return accReach.getGoalStates().get(exploreDaState.get(numda));
		}
		return toret;
	}

	// get state action chance reward
	// step 1 we check which of the ones are accepting states and set those to 1
	public BitSet getDAAcceptanceBitSet(State state) {
		BitSet toret = new BitSet();
		for (int i = 0; i < das.size(); i++) {
			DA<BitSet, ? extends AcceptanceOmega> da = das.get(i);
			AcceptanceOmega acc = da.getAcceptance();
			if (!(acc instanceof AcceptanceReach)) {
				continue;
			}
			if (i == this.safetyDAIndex)
				continue;
			AcceptanceReach accReach = (AcceptanceReach) acc;
			boolean isacc = accReach.getGoalStates().get(getDAState(i, state));
			if (isacc)
				toret.set(i);

		}
		return toret;
	}

	// step 2 we get two of these bitsets and check if anyone of them have 0 to 1
	// count those and return the number
	public int getNumTasks(BitSet parentStateAcc, BitSet stateAcc) {
		int numTasks = 0;
		for (int i = 0; i < das.size(); i++) {
			if (!parentStateAcc.get(i)) {
				if (stateAcc.get(i)) {
					numTasks++;
				}
			}
		}
		return numTasks;
	}

	public double getStateActionTaskCompletionReward(State state, int actionIndex) throws PrismException {
		BitSet parentStateAccs = getDAAcceptanceBitSet(state);
		// okay so get the transition
		double rew = 0;
		if (this.getExploreState().compareTo(state) != 0)
			throw new PrismException("Explore state and state dont match for state action task completion reward");
		// for this state action get all the next stuff
		int numTran = getNumTransitions(actionIndex);
		for (int t = 0; t < numTran; t++) {
			State targetState = computeTransitionTarget(actionIndex, t);
			BitSet stateAccs = getDAAcceptanceBitSet(targetState);
			int numTasks = getNumTasks(parentStateAccs, stateAccs);
			double tranProb = getTransitionProbability(actionIndex, t);
			rew += (double) numTasks * tranProb;

		}
		return rew;

	}

	public boolean isAvoid(State state) {
		boolean toret = false;
		if (safetyDAIndex != -1) {
			DA<BitSet, ? extends AcceptanceOmega> da = das.get(safetyDAIndex);
			AcceptanceOmega acc = da.getAcceptance();
			AcceptanceReach accReach = (AcceptanceReach) acc;
			boolean isacc = accReach.getGoalStates().get(getDAState(safetyDAIndex, state));
			if (isacc) {
				toret = true;
			}
		}

		return toret;
	}

	public boolean isDeadend(State state) throws PrismException {
		boolean resetState = false;
		boolean toret = false;
		State currentES = getExploreState();
		if (currentES == null) {
			exploreState(state);
		} else {
			if (state.compareTo(currentES) != 0) {
				exploreState(state);
				resetState = true;
			}
		}
		int numC = this.getNumChoices();
		if (numC < 2) {
			if (numC == 0)
				toret = true;

			for (int c = 0; c < numC; c++) {
				int numT = this.getNumTransitions(c);
				if (numT == 1) {
					State endState = this.computeTransitionTarget(c, 0);
					if (endState.compareTo(state) == 0)
						toret = true;
				}
			}
		}
		if (resetState)
			exploreState(currentES);
		return toret;
	}

	public boolean isReachAcceptanceGoalState(State state) {

		boolean toret = true;

		for (int i = 0; i < das.size(); i++) {

			DA<BitSet, ? extends AcceptanceOmega> da = das.get(i);
			AcceptanceOmega acc = da.getAcceptance();
			if (!(acc instanceof AcceptanceReach)) {
				toret = false;
				break;
			}
			if (i == safetyDAIndex) {
				continue;
			}
			AcceptanceReach accReach = (AcceptanceReach) acc;
			boolean isacc = accReach.getGoalStates().get(getDAState(i, state));
			toret = isacc & toret;
			if (!toret)
				break;
		}
		return toret;
	}

	// Methods to implement ModelGenerator

	@Override
	public void setSomeUndefinedConstants(Values someValues) throws PrismException {
		modelGen.setSomeUndefinedConstants(someValues);
	}

	@Override
	public ModelType getModelType() {
		// TODO Auto-generated method stub
		return modelGen.getModelType();
	}

	@Override
	public List<String> getVarNames() {
		// TODO Auto-generated method stub
		return varNames;
	}

	@Override
	public List<Type> getVarTypes() {
		// TODO Auto-generated method stub
		return varTypes;
	}

	@Override
	public VarList createVarList() throws PrismException {

		VarList varListModel = modelGen.createVarList();
		VarList varList = (VarList) varListModel.clone();
		// NB: if DA only has one state, we add an extra dummy state
		for (int i = 0; i < daVars.size(); i++) {
			String daVar = daVars.get(i);
			Declaration decl = new Declaration(daVar,
					new DeclarationInt(Expression.Int(0), Expression.Int(Math.max(das.get(i).size() - 1, 1))));
			try {
				varList.addVar(decl, 1, null);
			} catch (PrismLangException e) {
				// Shouldn't happen
				return null;
			}
		}
		return varList;
	}

	/**
	 * Find the successor of state {@code q} in the DA, taking the edge whose
	 * labelling matches the state {@code s}.
	 */
	protected int getDASuccessor(int danum, int q, State s) throws PrismException {
		DA<BitSet, ? extends AcceptanceOmega> da = das.get(danum);
		BitSet bsLabelsda = bsLabels.get(danum);
		List<Expression> labelExprsda = labelExprs.get(danum);
		// Create BitSet representing APs (labels) satisfied by state s
		for (int k = 0; k < numAPs.get(danum); k++) {
			bsLabelsda.set(k,
					labelExprsda.get(Integer.parseInt(da.getAPList().get(k).substring(1))).evaluateBoolean(s));
		}
		// Find/return successor
		return da.getEdgeDestByLabel(q, bsLabelsda);
	}

	public State createCombinedDAState(State sInit, boolean daInitStates) throws PrismException {
		State combinedDAState = new State(das.size());
		for (int d = 0; d < das.size(); d++) {
			int daStateHere;
			if (daInitStates)
				daStateHere = das.get(d).getStartState();
			else
				daStateHere = exploreDaState.get(d);
			int daInitState = getDASuccessor(d, daStateHere, sInit);
			combinedDAState.setValue(d, daInitState);
		}
		return new State(sInit, combinedDAState);
	}

	@Override
	public List<State> getInitialStates() throws PrismException {
		List<State> initStates = new ArrayList<>();

		for (State sInit : modelGen.getInitialStates()) {
			// automaton init states

			initStates.add(createCombinedDAState(sInit, true));
		}
		return initStates;
	}

	@Override
	public State getInitialState() throws PrismException {
		State sInit = modelGen.getInitialState();

		return createCombinedDAState(sInit, true);
	}

	protected State getModelState(State state) {
		return state.substate(0, numModelVars);
	}

	protected int getDAState(int danum, State state) {
		return ((Integer) state.varValues[numModelVars + danum]).intValue();

	}

	@Override
	public double getStateActionReward(int r, State state, Object action) throws PrismException {
		State modelState = getModelState(state);
		return modelGen.getStateActionReward(r, modelState, action);
	}

	@Override
	public double getStateReward(int r, State state) throws PrismException {
		State modelState = getModelState(state);
		return modelGen.getStateReward(r, modelState);
	}

	@Override
	public void exploreState(State exploreState) throws PrismException {
		// TODO Auto-generated method stub
		this.exploreState = exploreState;
		exploreModelState = getModelState(exploreState);// exploreState.substate(0, numModelVars);
		modelGen.exploreState(exploreModelState);
		if (exploreDaState == null)
			exploreDaState = new ArrayList<Integer>();
		for (int d = 0; d < das.size(); d++) {
			int daState = getDAState(d, exploreState);// ((Integer) exploreState.varValues[numModelVars +
														// d]).intValue();

			if (exploreDaState.size() <= d)
				exploreDaState.add(daState);
			else
				exploreDaState.set(d, daState);
			// exploreDaState = exploreState.substate(numVars -das.size(),numVars);
		}
		// ((Integer) exploreState.varValues[numVars - 1]).intValue();
	}

	@Override
	public int getNumChoices() throws PrismException {
		// TODO Auto-generated method stub
		return modelGen.getNumChoices();
	}

	// get the reward for the current state
	public double getStateActionReward(int r, Object action) throws PrismException {
		return modelGen.getStateActionReward(r, exploreModelState, action);
	}

	@Override
	public int getNumTransitions(int i) throws PrismException {
		// TODO Auto-generated method stub
		return modelGen.getNumTransitions(i);
	}

	@Override
	public double getTransitionProbability(int i, int offset) throws PrismException {
		// TODO Auto-generated method stub
		return modelGen.getTransitionProbability(i, offset);
	}

	@Override
	public State computeTransitionTarget(int i, int offset) throws PrismException {
		// TODO Auto-generated method stub
		State sTarget = modelGen.computeTransitionTarget(i, offset);
		return createCombinedDAState(sTarget, false);
//		return new State(sTarget, new State(1).setValue(0, getDASuccessor(exploreDaState, sTarget)));

	}

	@Override
	public Object getTransitionAction(int i, int offset) throws PrismException {
		// TODO Auto-generated method stub
		return modelGen.getTransitionAction(i, offset);
	}

	// TODO FIX THIS FUNCTION!!!
	@Override
	public boolean isLabelTrue(String label) throws PrismException {
		System.out.println("ACC");
		if (accLabel.equalsIgnoreCase(label)) {
			return isReachAcceptanceGoalState(); // TODO non acceptance
		} else {
			return modelGen.isLabelTrue(label);
		}
	}

	// TODO FIX THIS FUNCTION!!!
	@Override
	public boolean isLabelTrue(int i) throws PrismException {
		if (i == modelGen.getNumLabels()) {
			return isReachAcceptanceGoalState(); // TODO non acceptance
		} else {
			return modelGen.isLabelTrue(i);
		}
	}

	protected double getProgressionRew(State source, State target, int numda) {
		DA<BitSet, ? extends AcceptanceOmega> da = das.get(numda);
		int daSource = this.getDAState(numda, source);
		int daTarget = this.getDAState(numda, target);

		double prog = 100 * (da.getDistsToAcc().get(daSource) - da.getDistsToAcc().get(daTarget));
		// if (prog < 0.0) System.out.println(prog);
		// return Math.max(prog, 0);
		return prog;
	}

	public double getProgressionRew(State source, State target) {

		double prog = 0;
		for (int i = 0; i < das.size(); i++) {
			prog += getProgressionRew(source, target, i);
		}
		prog = prog / das.size();

		return prog;
	}

	protected double getDaDistanceCost(State state, int numda) {
		DA<BitSet, ? extends AcceptanceOmega> da = das.get(0);
		int daVal = (int) state.varValues[numModelVars];
		double res = da.getDistsToAcc().get(daVal);
		return res;
	}

	public double getDaDistanceCost(State state) {

		double res = 0;
		for (int i = 0; i < das.size(); i++) {
			res += getDaDistanceCost(state, i);
		}
		res = res / das.size();
		return res;

	}
	/*
	 * Added to check if an expression is true for a state islabel true didnt work
	 * here fatma
	 */
	// TODO FIX THIS FUNCTION!!!

	public boolean isExprTrue(int exprNum, int danum) throws PrismLangException {
		// so technically an expression should belong to just one da
		// but if it doesn't we've got to make sure we have the right one
		// this is just a number though so ?
		// lets say we go through all the das to check
		Expression expr = this.labelExprs.get(danum).get(exprNum);
		return expr.evaluateBoolean(this.exploreState);
	}

	@Override
	public int getVarIndex(String name) {
		return varNames.indexOf(name);
	}

	@Override
	public String getVarName(int i) {
		return varNames.get(i);
	}

	@Override
	public int getNumLabels() {
		return labelNames.size();
	}

	@Override
	public List<String> getLabelNames() {
		return labelNames;
	}

	@Override
	public int getLabelIndex(String name) {
		return getLabelNames().indexOf(name);
	}

	@Override
	public String getLabelName(int i) throws PrismException {
		try {
			return getLabelNames().get(i);
		} catch (IndexOutOfBoundsException e) {
			throw new PrismException("Label number \"" + i + "\" not defined");
		}
	}

	@Override
	public int getNumRewardStructs() {
		return modelGen.getNumRewardStructs();
	}

	@Override
	public List<String> getRewardStructNames() {
		return modelGen.getRewardStructNames();
	}

	@Override
	public int getRewardStructIndex(String name) {
		return modelGen.getRewardStructIndex(name);
	}

	@Override
	public RewardStruct getRewardStruct(int i) throws PrismException {
		return modelGen.getRewardStruct(i);
	}

	@Override
	public boolean rewardStructHasTransitionRewards(int i) {
		return modelGen.rewardStructHasTransitionRewards(i);
	}

	public State getExploreState() {
		// TODO Auto-generated method stub
		return this.exploreState;
	}

	public BitSet getStateLabels(State s) {
		// TODO Auto-generated method stub
		System.out.println("Not Implemented!!! Fix this later");
		return new BitSet();
	}

}
