package thts.modelgens;

import java.io.PrintStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import acceptance.AcceptanceOmega;
import automata.DA;
import parser.State;
import parser.VarList;
import parser.ast.*;
import parser.type.Type;
import parser.type.TypeInt;
import prism.DefaultModelGenerator;
import prism.ModelType;
import prism.PrismException;
import prism.PrismLangException;
import simulator.ModulesFileModelGenerator;
import thts.treesearch.utils.HelperClass;
import thts.treesearch.rewardhelper.RewardCalculation;

public class MultiAgentNestedProductModelGenerator extends DefaultModelGenerator {// ModelGenerator, RewardGenerator {

    // a list of models
    // a list of das
    protected ArrayList<ModulesFileModelGenerator> modelGens = null;
    /**
     * The list of DAs used to build the product
     */
    protected ArrayList<DA<BitSet, ? extends AcceptanceOmega>> das = null;
    /**
     * The expressions for the labels (APs) in the DAs
     */
    protected ArrayList<List<Expression>> labelExprs = null;
    /**
     * Variable names for DAs states
     */
    protected ArrayList<String> daVars;
    /**
     * Number of APs in the DAs
     */
    protected ArrayList<Integer> numAPs;
    /**
     * Number of variables (num model vars + num das)
     */
    protected int numVars;
    /**
     * Variable names
     */
    protected List<String> varNames;
    /**
     * Variable types
     */
    protected List<Type> varTypes;
    /**
     * Name for new acceptance label
     */
    protected String accLabel;
    /**
     * Label names
     */
    protected List<String> labelNames;

    /**
     * BitSet
     */
    protected ArrayList<BitSet> bsLabels;

    /**
     * State to be explored in product
     */
    protected State exploreState;
    /**
     * The model part of exploreState
     */
    protected ArrayList<State> exploreModelState;
    /**
     * The DA part of exploreState
     */
    protected ArrayList<Integer> exploreDaState;
    /**
     * The choice string combinations
     **/
    protected ArrayList<ArrayList<Integer>> exploreStateChoiceCombs;
    /**
     * The transitions list for this choice
     **/

    protected ArrayList<ArrayList<Entry<State, Double>>> exploreStateChoiceTransitionCombs;

    // to think about
    // do we want to save things in memory to stop us from computing them ?
    // how is it in the previous stuff
    // we'll just generate it again
    // its fine

    protected ArrayList<Integer> numModelVars;
    public int numDAs;
    public int numModels;

    protected int safetyDAIndex;
    protected ArrayList<String> sharedVarsList;
    protected int numModelVarsAll;

    // this is just for ease of use
    // the string is basically the model number
    // excludes shared vars
    protected HashMap<Integer, ArrayList<Integer>> modelVarIndices;
    protected HashMap<Integer, ArrayList<Integer>> sharedVarIndices;
    private String jointActionSep = ",";
    ArrayList<State> initialStatesList = null;

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

    // hmmm reward structures
    // we could assume just one for now
    // TODO: what if they all have different reward structures ?
    @Override
    public int getNumRewardStructs() {
        return modelGens.get(0).getNumRewardStructs();
    }
    // TODO: what if they all have different reward structures ?

    @Override
    public List<String> getRewardStructNames() {
        return modelGens.get(0).getRewardStructNames();
    }
    // TODO: what if they all have different reward structures ?

    @Override
    public int getRewardStructIndex(String name) {
        return modelGens.get(0).getRewardStructIndex(name);
    }
    // TODO: what if they all have different reward structures ?

    @Override
    public RewardStruct getRewardStruct(int i) throws PrismException {
        return modelGens.get(0).getRewardStruct(i);
    }

    @Override
    public ModelType getModelType() {
        // TODO Auto-generated method stub
        return ModelType.MDP;
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

    public List<String> getSharedVars() {
        return sharedVarsList;
    }

    @Override
    public VarList createVarList() throws PrismException {

        ModulesFileModelGenerator modelGen;
        VarList modelVarList;
        VarList varList = new VarList();
        try {
            for (int mdnum = 0; mdnum < modelGens.size(); mdnum++) {
                modelGen = modelGens.get(mdnum);
                modelVarList = modelGen.createVarList();
                for (int vnum = 0; vnum < modelVarList.getNumVars(); vnum++) {
                    String varname = modelVarList.getName(vnum);
                    if (!sharedVarsList.contains(varname)) {
                        //Declaration decl = modelVarList.getDeclaration(vnum);
                       DeclarationType decltype = modelVarList.getDeclarationType(vnum);
                        varList.addVar(varname,decltype, 0, modelGen.getConstantValues());
                    }
                }
            }
            modelGen = modelGens.get(0);
            modelVarList = modelGen.createVarList();
            for (int ss = 0; ss < sharedVarsList.size(); ss++) {
                String varname = sharedVarsList.get(ss);
                int ssvarnum = sharedVarIndices.get(ss).get(0);
               // Declaration decl = modelVarList.getDeclaration(ssvarnum);
                varList.addVar(varname,modelVarList.getDeclarationType(ssvarnum), 0, modelGen.getConstantValues());
            }

            // NB: if DA only has one state, we add an extra dummy state
            for (int i = 0; i < daVars.size(); i++) {
                String daVar = daVars.get(i);
                Declaration decl = new Declaration(daVar,
                        new DeclarationInt(Expression.Int(0), Expression.Int(Math.max(das.get(i).size() - 1, 1))));

                varList.addVar(decl, 0, modelGen.getConstantValues());

            }
        } catch (PrismLangException e) {
            // Shouldn't happen
            return null;
        }
        return varList;
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
            else {
                if (danum == this.safetyDAIndex) // this may be a bit of a hardcoding thing // but or doesnt make sense
                    bsLabels.and(currentRobotLabels); // we're anding because all robots have to have the same labels!!!
                else
                    bsLabels.or(currentRobotLabels);

            }
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
                        System.out.println("Error in shared states");
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

    public State createCombinedRobotState(ArrayList<State> robotStates, ArrayList<State> prevStates) {
        // so now this is the tricky part
        // this is actually going to be fun
        int numRobotState = numModelVarsAll;
        if (sharedVarsList != null)
            numRobotState -= sharedVarsList.size();
        State combinedRobotState = new State(numRobotState);

        int offset = 0;
        for (int r = 0; r < modelGens.size(); r++) {
            // assuming no shared states
            // TODO: Fix this for shared states man
            State rs = robotStates.get(r);
            // we only care about state indices which are not shared state indices
            // for shared state ones we've got to do other things
            // so its useful to store the indices for things we care about

            ArrayList<Integer> indicesToCareAbout = modelVarIndices.get(r);

            if (r > 0)
                offset += this.numModelVars.get(r - 1);
            for (int i = 0; i < indicesToCareAbout.size(); i++) {
                int rsInd = indicesToCareAbout.get(i);
                combinedRobotState.setValue(offset + i, rs.varValues[rsInd]);
            }
//			combinedRobotState.setValue(r, robotStates.get(r));
        }
        State toret;
        if (sharedVarsList != null) {
            State combinedsharedstate = this.createSharedState(robotStates, prevStates);
            toret = new State(combinedRobotState, combinedsharedstate);
        } else {
            toret = combinedRobotState;
        }
        return toret;
    }

    public BitSet getDAAccsForState(State state) {
        // the bitset value for the corresponding da is true
        BitSet daAccs = new BitSet(das.size());
        for (int d = 0; d < das.size(); d++) {
            int daState = getDAState(d, state);
            //System.out.println(das.get(d).getAccStates());
            if (das.get(d).getAccStates().get(daState))
                daAccs.set(d);
        }
        return daAccs;
    }

    public boolean isAvoidState(State state) {
        if (safetyDAIndex != -1) {
            BitSet daAccs = getDAAccsForState(state);
            return daAccs.get(safetyDAIndex);
        }
        return false;
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

    public ArrayList<Object> getAvailableRobotActionsInState(State state, int r) throws PrismException
    {
        boolean resetState = false;

        State currentES = getExploreState();
        if (currentES == null) {
            exploreState(state);
        } else {
            if (state.compareTo(currentES) != 0) {
                exploreState(state);
                resetState = true;
            }
        }
        // for each robot we just want to get all the actions
       // for (int r = 0; r < modelGens.size(); r++) {
            ArrayList<Object> robotActions = new ArrayList<>();
            ModulesFileModelGenerator modelGen = modelGens.get(r);
            int numChoices = modelGen.getNumChoices();
            for (int c = 0; c < numChoices; c++) {
                Object action = modelGen.getChoiceAction(c);
                robotActions.add(action);

         //   }
           // availableRobotActionsInState.add(robotActions);

        }
        if (resetState)
            exploreState(currentES);

        return robotActions;//availableRobotActionsInState;

    }
    public ArrayList<ArrayList<Object>> getAvailableRobotsActionsInState(State state) throws PrismException {
        ArrayList<ArrayList<Object>> availableRobotActionsInState = new ArrayList<>();
        boolean resetState = false;

        State currentES = getExploreState();
        if (currentES == null) {
            exploreState(state);
        } else {
            if (state.compareTo(currentES) != 0) {
                exploreState(state);
                resetState = true;
            }
        }
        // for each robot we just want to get all the actions
        for (int r = 0; r < modelGens.size(); r++) {
            ArrayList<Object> robotActions = new ArrayList<>();
            ModulesFileModelGenerator modelGen = modelGens.get(r);
            int numChoices = modelGen.getNumChoices();
            for (int c = 0; c < numChoices; c++) {
                Object action = modelGen.getChoiceAction(c);
                robotActions.add(action);

            }
            availableRobotActionsInState.add(robotActions);

        }
        if (resetState)
            exploreState(currentES);

        return availableRobotActionsInState;
    }

    private State getExploreState() {
        // TODO Auto-generated method stub
        return exploreState;
    }

    public boolean isAccState(State state) {
        boolean acc = true;
        BitSet daAccs = getDAAccsForState(state);
        if (safetyDAIndex != -1) {
            if (daAccs.get(safetyDAIndex))
                acc = false;
        }
        if (acc) {
            for (int d = 0; d < das.size(); d++) {
                if (d != safetyDAIndex) {
                    acc = acc & daAccs.get(d);
                }
                if (!acc)
                    break;
            }
        }
        return acc;
    }

    public State getCombinedDAState(State exploreState) {
        return exploreState.substate(numModelVarsAll, numModelVarsAll + numDAs);
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
            int nextDAState = getDASuccessor(d, daStateHere, robotStates);

            combinedDAState.setValue(d, nextDAState);
        }
        return combinedDAState;
    }

    private State createCombinedRobotDAState(ArrayList<State> robotStates, ArrayList<State> previousStates,
                                             boolean daInitStates) throws PrismException {
        State combinedRobotState = createCombinedRobotState(robotStates, previousStates);
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
        if (this.initialStatesList == null) {
            List<State> initStates = new ArrayList<>();
            ArrayList<ArrayList<State>> robotinitstatecombs = this.getInitialRobotStatesCombinations();
            for (ArrayList<State> sInit : robotinitstatecombs) {
                // automaton init states

                initStates.add(createCombinedRobotDAState(sInit, null, true));
            }
            return initStates;
        } else
            return initialStatesList;
    }

    @Override
    public State getInitialState() {
        if (this.initialStatesList == null) {
            // first we must get the initial states of all the models
            ArrayList<State> robotStates = new ArrayList<State>();
            State toret = null;
            State sInit;
            try {

                for (ModulesFileModelGenerator modelGen : modelGens) {

                    sInit = modelGen.getInitialState();

                    robotStates.add(sInit);

                }
                toret = createCombinedRobotDAState(robotStates, null, true);
            } catch (Exception e) {

                e.printStackTrace();
            }
            return toret;
        } else {
            return this.initialStatesList.get(0);

        }
    }

    private State flipState(State s) {
        State flipped = new State(s.varValues.length);
        for (int i = 0; i < s.varValues.length; i++) {
            flipped.setValue(i, s.varValues[s.varValues.length - (i + 1)]);
        }
        return flipped;
    }

    public ArrayList<State> getModelAndDAStates(State exploreState, boolean dabeforemodel) {
        ArrayList<State> modelStates = getModelStates(exploreState);
        State daState = getCombinedDAState(exploreState);
        State flippedDaState = flipState(daState);
        ArrayList<State> robotStates = new ArrayList<>();
        for (int i = 0; i < modelStates.size(); i++) {
            State modelState = modelStates.get(i);
            State state;
            if (dabeforemodel) {
                state = new State(flippedDaState, modelState);
            } else {
                state = new State(modelState, daState);
            }
            robotStates.add(state);
        }
        return robotStates;
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
        if (sharedVarsList != null) {
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
        }
        return toret;

    }

    public void printExploreState()
    {
        printExploreState(System.out);
    }
    public void printExploreState(PrintStream out) {
        if (exploreState != null)
            out.println("State " + exploreState.toString());
        if (exploreModelState != null) {
            out.println("Robot States: ");
            for (State s : exploreModelState) {
                out.println(s.toString());
            }
        }
        if (exploreDaState != null) {
            out.println("DA States: ");
            for (int s : exploreDaState) {
               out.println(s);
            }
        }

    }

    @Override
    public void exploreState(State exploreState) throws PrismException {
        boolean update = true;
//		if(exploreState!=null)
//			update = (exploreState.compareTo(exploreState) != 0);
        if (update) {
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
    }

    protected int getDAState(int danum, State state) {
        return ((Integer) state.varValues[numModelVarsAll + danum]).intValue();

    }

    @Override
    public int getNumChoices() throws PrismException {
        // TODO Auto-generated method stub
        // return modelGen.getNumChoices();
        // so its a combination of all the choices
        // so just like x choices
        // so we just make an array of all the choices for each robot
        if (exploreStateChoiceCombs == null) {
            // go through all the model gens and get the choices
            ArrayList<Integer> numchoices = new ArrayList<>();
            //fixing bug for when not all are 0
            int sumChoices = 0;
            for (int r = 0; r < modelGens.size(); r++) {
                numchoices.add(modelGens.get(r).getNumChoices());
                sumChoices += numchoices.get(r);
            }
            if(sumChoices != 0) {
                for (int r = 0; r < modelGens.size(); r++) {
                    if (numchoices.get(r) == 0)
                        numchoices.set(r,1);
                }
            }
            HelperClass<Integer> hc = new HelperClass<>();
            // but surely this is not enough
            // we need to kind of save our choice indices
            // we could do this on the fly
            // or perhaps we could do this now
            // lets do it when we're asked to explore a choice

            return hc.getNumCombsFromSizes(numchoices);
        } else
            return exploreStateChoiceCombs.size();
    }

    public void generateChoiceCombs() throws PrismException {
        ArrayList<List<Integer>> allChoiceActionStrings = new ArrayList<>();
        for (int r = 0; r < modelGens.size(); r++) {
            ModulesFileModelGenerator modelGen = modelGens.get(r);
            int numchoice = modelGen.getNumChoices();
            ArrayList<Integer> choiceActionStrings = new ArrayList<>();
            for (int c = 0; c < numchoice; c++) {
//				String cas = modelGen.getChoiceActionString(c);
                choiceActionStrings.add(c);
            }
            allChoiceActionStrings.add(choiceActionStrings);
        }
        // so now we've got to save these cuz this is important
        // well the combinations
        HelperClass<Integer> hc = new HelperClass<>();
        ArrayList<ArrayList<Integer>> combs = hc.generateCombinations(allChoiceActionStrings);
        exploreStateChoiceCombs = combs;
    }

    @Override
    public Object getChoiceAction(int i) throws PrismException {
        if (exploreStateChoiceCombs == null) {
            generateChoiceCombs();
        }
        return (Object) createJointAction(exploreStateChoiceCombs.get(i));
        // but there is more
        // we need to get all the actions
    }

    @Override
    public int getNumTransitions(int i) throws PrismException {
        if (exploreStateChoiceCombs == null) {
            generateChoiceCombs();
        }
        exploreStateChoiceTransitionCombs = null;
        // get the number of transitions here
        ArrayList<Integer> robotTrans = exploreStateChoiceCombs.get(i);
        // so this is all the robot choices
        // now we've got to create combinations
        ArrayList<Integer> transNums = new ArrayList<>();

        for (int r = 0; r < modelGens.size(); r++) {
            int numTrans = 0;
            if(robotTrans.get(r)!=null)
              numTrans = modelGens.get(r).getNumTransitions(robotTrans.get(r));
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

    private ArrayList<ArrayList<Entry<State, Double>>> computeCurrentChoiceTransitionCombinations(
            ArrayList<Integer> robotChoices) throws PrismException {

        return computeCurrentChoiceTransitionCombinations(robotChoices, true);
    }

    private ArrayList<ArrayList<Entry<State, Double>>> computeCurrentChoiceTransitionCombinations(
            ArrayList<Integer> robotChoices, boolean setToExploreState) throws PrismException {
        // so we've got the choice and the states
        ArrayList<List<Entry<State, Double>>> allTransitionOptions = new ArrayList<>();
        for (int r = 0; r < modelGens.size(); r++) {
            ModulesFileModelGenerator modelGen = modelGens.get(r);
            ArrayList<Entry<State, Double>> choiceTransitionTargets = new ArrayList<>();

            if(robotChoices.get(r)!=null) {
                int currChoice = robotChoices.get(r);
                int numTransitions = modelGen.getNumTransitions(currChoice);
                for (int t = 0; t < numTransitions; t++) {
                    State target = modelGen.computeTransitionTarget(currChoice, t);
                    double prob = modelGen.getTransitionProbability(currChoice, t);
                    choiceTransitionTargets.add(new AbstractMap.SimpleEntry<State, Double>(target, prob));
                }
            }
            else
            {
                if(setToExploreState)
                {
                    State target = getModelStates(this.exploreState).get(r);
                    double prob = 1.0;
                    choiceTransitionTargets.add(new AbstractMap.SimpleEntry<State, Double>(target, prob));

                }
            }

            allTransitionOptions.add(choiceTransitionTargets);
        }
        HelperClass<Entry<State, Double>> hc = new HelperClass<>();
        ArrayList<ArrayList<Entry<State, Double>>> combs = hc.generateCombinations(allTransitionOptions);
        if (setToExploreState) {
            exploreStateChoiceTransitionCombs = combs;
        }
        return combs;

    }

    public double getStateActionTaskRewardAction(Object action) throws PrismException {
        ArrayList<ArrayList<Entry<State, Double>>> combs;

        if (exploreStateChoiceTransitionCombs == null) {
            if (exploreStateChoiceCombs == null)
                generateChoiceCombs();
//			combs = computeCurrentChoiceTransitionCombinations(exploreStateChoiceCombs.get(choice));
        }

//		else
//			combs = exploreStateChoiceTransitionCombs;
        // find the choice that matches this action
        int choice = -1;
        for (int i = 0; i < exploreStateChoiceCombs.size(); i++) {
            if (action.toString().contentEquals(this.getChoiceActionString(i))) {
                choice = i;
                break;
            }

        }

        if (choice == -1)
            return 0.0;
        else
            return getStateActionTaskReward(choice);
    }

    public double getRemainingTasksFraction(State state)
    {

            BitSet stateAccs = getDAAccsForState(state);
            int maxTasks = safetyDAIndex==-1?numDAs:numDAs-1;
            int tasksDone = 0;
            for(int i = 0; i<numDAs; i++)
            {
                if(i!=safetyDAIndex)
                {
                    if(stateAccs.get(i))
                        tasksDone++;
                }
            }
            int remainingTasks = maxTasks - tasksDone;
            double fraction = (double)remainingTasks/(double) maxTasks;
            //stateAccs.flip(safetyDAIndex);
            //int tasksDone =stateAccs.cardinality();
            return fraction;
    }
    // for the current state and current choice okay
    public double getStateActionTaskReward(int choice) throws PrismException {
        ArrayList<ArrayList<Entry<State, Double>>> combs;
        ArrayList<State> robotStates;
        robotStates = exploreModelState;
        if (exploreStateChoiceTransitionCombs == null) {
            if (exploreStateChoiceCombs == null)
                generateChoiceCombs();

        }
        combs = computeCurrentChoiceTransitionCombinations(exploreStateChoiceCombs.get(choice));
        BitSet parentStateAccs = getDAAccsForState(exploreState);
        double taskrew = 0;

        if(exploreState.toString().contentEquals("(4,-1,-1,1,1,0,2,0)"))
            System.out.println("Stop");
        for (int t = 0; t < combs.size(); t++) {
            int numtasks=0;
            Entry<State, Double> e = computeTransitionTargetAndProbability(choice, t);
            State ns = e.getKey();
            Double np = e.getValue();
            if(!isAvoidState(ns)) {
                BitSet stateAccs = getDAAccsForState(ns);
                numtasks = getTasksCompletedFromAccBitSets(parentStateAccs, stateAccs);
            }
            taskrew += (double) numtasks * np;


        }
        return taskrew;

    }

    public int getTasksCompletedFromAccBitSets(BitSet ps, BitSet s) {
        int numtasks = 0;
        for (int d = 0; d < numDAs; d++) {
            if (d != safetyDAIndex) {
                boolean isPacc = ps.get(d);
                boolean isSacc = s.get(d);
                if (isPacc != isSacc) {
                    if (isSacc)
                        numtasks++;
                }


            }

        }
        return numtasks;
    }

    @Override
    public double getTransitionProbability(int i, int offset) throws PrismException {
        if (exploreStateChoiceTransitionCombs == null) {
            computeCurrentChoiceTransitionCombinations(exploreStateChoiceCombs.get(i));
        }
        // the offset
        ArrayList<Entry<State, Double>> stateCombs = exploreStateChoiceTransitionCombs.get(offset);
        // probability = just those states together
        double prob = 1;
        for (Entry<State, Double> e : stateCombs) {
            prob *= e.getValue();
        }
        return prob;
    }

    private Entry<State, Double> computeTransitionTargetAndProbability(int i, int offset) throws PrismException {
        // TODO Auto-generated method stub
        if (exploreStateChoiceTransitionCombs == null) {
            computeCurrentChoiceTransitionCombinations(exploreStateChoiceCombs.get(i));
        }
        // the offset
        ArrayList<Entry<State, Double>> stateCombs = exploreStateChoiceTransitionCombs.get(offset);
        // probability = just those states together
        double prob = 1;
        ArrayList<State> robotStates = new ArrayList<>();
        for (Entry<State, Double> e : stateCombs) {
            prob *= e.getValue();
            robotStates.add(e.getKey());
        }
        State nextstate = createCombinedRobotDAState(robotStates, exploreModelState, false);
        Entry<State, Double> toret = new AbstractMap.SimpleEntry<State, Double>(nextstate, prob);
        return toret;
    }

    @Override
    public State computeTransitionTarget(int i, int offset) throws PrismException {
        // TODO Auto-generated method stub
        if (exploreStateChoiceTransitionCombs == null) {
            computeCurrentChoiceTransitionCombinations(exploreStateChoiceCombs.get(i));
        }
        ArrayList<Entry<State, Double>> stateCombs = exploreStateChoiceTransitionCombs.get(offset);
        ArrayList<State> robotStates = new ArrayList<>();

        for (Entry<State, Double> e : stateCombs) {
            robotStates.add(e.getKey());
        }
        State toret = createCombinedRobotDAState(robotStates, exploreModelState, false);
        return toret;
    }

    // get the reward for the current state
    public double getStateActionReward(int r, Object action) throws PrismException {

        if (r == -1)
            return getStateActionTaskRewardAction(action);

        else
            return getStateActionReward(r, exploreState, action);
    }

    @Override
    public double getStateActionReward(int r, State state, Object action) throws PrismException {
        if (r == -1) {
            boolean resetState = false;
            State currentES = getExploreState();
            if (currentES == null) {
                exploreState(state);
            } else {
                if (state.compareTo(currentES) != 0) {
                    exploreState(state);
                    resetState = true;
                }
            }
            double val = getStateActionTaskRewardAction(action);
            if (resetState) {
                exploreState(currentES);
            }
            return val;

        } else
            return getStateActionReward(r, state, action, RewardCalculation.SUM);
    }

    @Override
    public double getStateReward(int r, State state) throws PrismException {
        // Default implementation: error if not supported, or bad index
        if (!isRewardLookupSupported(RewardLookup.BY_STATE)) {
            throw new PrismException("Reward lookup by State not supported");
        }
        if (r < -1 || r >= getNumRewardStructs()) {
            throw new PrismException("Invalid reward index " + r);
        }
        // Otherwise default reward to 0
        return 0.0;
    }

    public double getStateActionReward(int r, State state, Object action, RewardCalculation rewCalc)
            throws PrismException {
        ArrayList<State> robotStates;
        if (state.compareTo(exploreState) == 0)
            robotStates = exploreModelState;
        else
            robotStates = getModelStates(state);
        ArrayList<String> robotActions = getRobotActions(action.toString());
        return getStateActionReward(r, robotStates, robotActions, rewCalc);
    }

    public ArrayList<String> getRobotActions(String action) {
        String sep = jointActionSep;
        String[] actionlist = action.split(jointActionSep);
        ArrayList<String> toret = new ArrayList<>();
        toret.addAll(Arrays.asList(actionlist));
        return toret;
    }

    public String createJointActionFromString(ArrayList<String> robotChoices) {
        String toret = "";

        String sep = jointActionSep;
        for (int r = 0; r < robotChoices.size(); r++) {
            toret += robotChoices.get(r) + sep;
        }
        return toret;
    }

    public String createJointAction(ArrayList<Integer> robotChoices) throws PrismException {

        String toret = "";

        String sep = jointActionSep;
        for (int r = 0; r < robotChoices.size(); r++) {
            String cas;
            if(robotChoices.get(r)!=null) {
                ModulesFileModelGenerator modelGen = modelGens.get(r);
                Object ca = modelGen.getChoiceAction(robotChoices.get(r));
                 cas = ca.toString();
            }
            else
            {
                cas = "*";
            }

            toret += cas + sep;

        }
        return toret;
    }

    private double calculateReward(ArrayList<Double> allrews, RewardCalculation rewCalc) {
        double fullrew = 0;
        switch (rewCalc) {
            case SUM: {
                for (double rewh : allrews) {
                    fullrew += rewh;
                }

                break;
            }
            case MAX: {
                fullrew = Double.MIN_VALUE;
                for (double rewh : allrews) {
                    if (rewh > fullrew)
                        fullrew = rewh;
                }
                break;
            }
            case MIN: {
                fullrew = Double.MAX_VALUE;
                for (double rewh : allrews) {
                    if (rewh < fullrew)
                        fullrew = rewh;
                }
                break;
            }
            default:
                break;
        }
        return fullrew;

    }

    private double getStateActionReward(int ri, ArrayList<State> robotstates, ArrayList<String> robotactions,
                                        RewardCalculation rewCalc) {

        ArrayList<Double> allrews = new ArrayList<>();
        double rew;
        // get all the rewards
        try {

            for (int rn = 0; rn < modelGens.size(); rn++) {

                rew = modelGens.get(rn).getStateActionReward(ri, robotstates.get(rn), robotactions.get(rn));

                allrews.add(rew);
            }
        } catch (PrismException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return calculateReward(allrews, rewCalc);
    }
}
