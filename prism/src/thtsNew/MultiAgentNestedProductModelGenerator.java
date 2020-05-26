package thtsNew;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

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

	protected ArrayList<Integer> numModelVars;
	protected int numDAs;
	protected int numModels;

	protected int safetyDAIndex;

	public MultiAgentNestedProductModelGenerator(ArrayList<ModulesFileModelGenerator> modelGens,
			ArrayList<DA<BitSet, ? extends AcceptanceOmega>> das, ArrayList<List<Expression>> labelExprs,
			int safetyDAIndex) {
		initialise(modelGens, das, labelExprs, safetyDAIndex);

	}

	// why is this here
	// because i love overloading and it just makes things easy
	// cuz I lazy!!! suraj ka darwaza khula!!
	private void initialise(ArrayList<ModulesFileModelGenerator> modelGens,
			ArrayList<DA<BitSet, ? extends AcceptanceOmega>> das, ArrayList<List<Expression>> labelExprs,
			int safetyDAIndex) {
		this.modelGens = modelGens;
		this.das = das;
		this.labelExprs = labelExprs;
		this.safetyDAIndex = safetyDAIndex;

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

		// find the beginning of the da variable
		// just incase we've been passed a model with a da
		int daStartNumber = 0;
		String expectedDAVar = "_da" + daStartNumber;
		ModulesFileModelGenerator modelGen;

		for (int mdnum = 0; mdnum < modelGens.size(); mdnum++) {
			modelGen = modelGens.get(mdnum);
			varNames.addAll(modelGen.getVarNames());
			varTypes.addAll(modelGen.getVarTypes());
			numModelVars.add(modelGen.getNumVars());
			labelNames.addAll(modelGen.getLabelNames());
			while (modelGen.getVarIndex(expectedDAVar) != -1) {
				daStartNumber = daStartNumber + 1;
				expectedDAVar = "_da" + daStartNumber;
			}
		}

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

	public State createCombinedRobotState(ArrayList<State> robotStates) {
		State combinedRobotState = new State(modelGens.size());
		for (int r = 0; r < modelGens.size(); r++) {
			// assuming no shared states
			// TODO: Fix this for shared states man
			combinedRobotState.setValue(r, robotStates.get(r));
		}
		return combinedRobotState;
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

	private State createCombinedRobotDAState(ArrayList<State> robotStates, boolean daInitStates) throws PrismException {
		State combinedRobotState = createCombinedRobotState(robotStates);
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
		return generateCombinations(allrobotstates);
	}

	private ArrayList<ArrayList<State>> generateCombinations(ArrayList<List<State>> robotStates) {
		// so lets get the number of states for each robot
		int[] numStates = new int[robotStates.size()];
		int[] currStateNum = new int[robotStates.size()];
		int numcombs = 1;
		int lastrobotnum = robotStates.size() - 1;
		for (int r = 0; r < robotStates.size(); r++) {
			List<State> rs = robotStates.get(r);
			numStates[r] = rs.size();

			currStateNum[r] = 0;
			numcombs *= rs.size();
		}
		ArrayList<ArrayList<State>> combs = new ArrayList<>();
		boolean docomb = true;
		while (docomb) {
			// so now we just loop over things
			// its a lot of while loops
			ArrayList<State> currcomb = new ArrayList<>();
			for (int r = 0; r < robotStates.size(); r++) {
				State rs = robotStates.get(r).get(currStateNum[r]);
				currcomb.add(rs);
			}
			combs.add(currcomb);

			boolean doInc = true;
			for (int lr = lastrobotnum; lr >= 0; lr--) {
				if (currStateNum[lr] + 1 == numStates[lr]) {
					currStateNum[lr] = 0;
				} else {
					currStateNum[lr]++;
					doInc = false;
				}
				if (!doInc) {
					break;
				}
			}
			int indsum = 0;
			for (int r = 0; r < numStates.length; r++) {
				indsum += currStateNum[r];
			}
			if (indsum == 0)
				docomb = false;
		}

		return combs;
	}

	@Override
	public List<State> getInitialStates() throws PrismException {
		List<State> initStates = new ArrayList<>();
		ArrayList<ArrayList<State>> robotinitstatecombs = this.getInitialRobotStatesCombinations();
		for ( ArrayList<State> sInit : robotinitstatecombs) {
			// automaton init states

			initStates.add(createCombinedRobotDAState(sInit, true));
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
			toret = createCombinedRobotDAState(robotStates, true);
		} catch (Exception e) {

			e.printStackTrace();
		}
		return toret;
	}

	@Override
	public void exploreState(State exploreState) throws PrismException {
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

	}

	@Override
	public int getNumChoices() throws PrismException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumTransitions(int i) throws PrismException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getTransitionAction(int i, int offset) throws PrismException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getTransitionProbability(int i, int offset) throws PrismException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public State computeTransitionTarget(int i, int offset) throws PrismException {
		// TODO Auto-generated method stub
		return null;
	}

}
