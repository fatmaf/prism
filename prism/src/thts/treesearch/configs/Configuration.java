package thts.treesearch.configs;

import acceptance.AcceptanceOmega;
import acceptance.AcceptanceType;
import automata.DA;
import explicit.LTLModelChecker;
import parser.State;
import parser.ast.Expression;
import parser.ast.ExpressionQuant;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import prism.*;
import simulator.ModulesFileModelGenerator;
import thts.modelgens.MultiAgentNestedProductModelGenerator;
import thts.testing.testsuitehelper.TestFileInfo;
import thts.treesearch.TrialBasedTreeSearch;
import thts.treesearch.actionselector.ActionSelector;
import thts.treesearch.backup.BackupNVI;
import thts.treesearch.heuristic.Heuristic;
import thts.treesearch.outcomeselector.OutcomeSelector;
import thts.treesearch.rewardhelper.RewardCalculation;
import thts.treesearch.rewardhelper.RewardHelper;
import thts.treesearch.rewardhelper.RewardHelperMultiAgent;
import thts.treesearch.utils.Objectives;
import thts.treesearch.utils.THTSRunInfo;
import thts.vi.SingleAgentSolverMaxExpTask;

import java.io.*;
import java.util.*;

public abstract class Configuration {
    final static int DEFAULTMAXROLLOUTS = 10000;
    final static float DEFAULTBACKUPEPSILON = 0.0001f;
    protected final static int DEFAULTTRIALLEN = -1;
    final static double DEFAULTEGREEDY = 0.8;
    final static long DEFAULTTIMELIMITINMS = 30 * 60 * 1000;

    String configname;
    ActionSelector actSel;
    Heuristic heuristic;
    BackupNVI backup;
    ActionSelector polActSel;
    OutcomeSelector outSel;
    boolean useSASH;
    boolean useActSelForBackupUpdate;
    int trialLength;
    int maxRollouts;
    double maxCost;
    float epsilon;
    double egreedy;
    long viOnPolIntervalInMS;
    long timeTimeLimitInMS;
    private boolean domaxcost;
    private boolean maxcostdeadends;
    private boolean policyActSelGreedy;

    public boolean isDomaxcost() {
        return domaxcost;
    }

    public void setDomaxcost(boolean domaxcost) {
        this.domaxcost = domaxcost;
    }

    public boolean isMaxcostdeadends() {
        return maxcostdeadends;
    }

    public void setMaxcostdeadends(boolean maxcostdeadends) {
        this.maxcostdeadends = maxcostdeadends;
    }

    public boolean isPolicyActSelGreedy() {
        return policyActSelGreedy;
    }

    public void setPolicyActSelGreedy(boolean policyActSelGreedy) {
        this.policyActSelGreedy = policyActSelGreedy;
        if (configname != null) {
            configname = configname.replace("_GP", "");
            configname = configname.replace("_GAllActions", "");
            if (policyActSelGreedy)
                setConfigname(configname + "_GP");
            else
                setConfigname(configname + "_GAllActions");
        }
    }

    protected void createConfigName(String configname) {

        if (domaxcost){
            if(!configname.contains("FC"))
            configname += "FC";}
        if (maxcostdeadends){
            if(!configname.contains("MCD"))
            configname += "_MCD";}
        if (policyActSelGreedy) {
            if(!configname.contains("GP"))
            configname += "_GP";
        }
        else {
            if(!configname.contains("_GAllActions"))
            configname += "_GAllActions";
        }
        setConfigname(configname);
    }

    private HashMap<Objectives, Map.Entry<Double, Double>> minMaxVals;

    public HashMap<Objectives, Map.Entry<Double, Double>> getMinMaxVals() {
        return minMaxVals;
    }

    public void setMinMaxVals(HashMap<Objectives, Map.Entry<Double, Double>> minMaxVals) {
        this.minMaxVals = minMaxVals;
    }

    public long getTimeTimeLimitInMS() {
        return timeTimeLimitInMS;
    }

    public void setTimeTimeLimitInMS(long timeTimeLimitInMS) {
        this.timeTimeLimitInMS = timeTimeLimitInMS;
    }

    public long getViOnPolIntervalInMS() {
        return viOnPolIntervalInMS;
    }

    public void setViOnPolIntervalInMS(long viOnPolIntervalInMS) {
        this.viOnPolIntervalInMS = viOnPolIntervalInMS;
    }

    boolean timeBound;

    MultiAgentNestedProductModelGenerator maModelGen;
    ArrayList<HashMap<State, Object>> stateActions;
    ArrayList<HashMap<Objectives, HashMap<State, Double>>> singleAgentStateValues;

    ArrayList<Objectives> tieBreakingOrder;
    private boolean dovipolcheckonintervals;

    public Configuration(boolean timeBound, boolean useSASH, boolean useActSelForBackup) {
        setTimeBound(timeBound);
        setUseSASH(useSASH);
        setUseActSelForBackupUpdate(useActSelForBackup);
        setMaxCost(maxCost);
        setActSel(null);
        setHeuristic(null);
        setBackup(null);
        setPolActSel(null);
        setOutSel(null);
        setTrialLength(-1);
        setMaxRollouts(DEFAULTMAXROLLOUTS);
        setEpsilon(DEFAULTBACKUPEPSILON);
        setEgreedy(DEFAULTEGREEDY);
        setTrialLength(DEFAULTTRIALLEN);
        setTimeTimeLimitInMS(DEFAULTTIMELIMITINMS);
        setDovipolcheckonintervals(false);
        setTieBreakingOrder();


    }

    public double estimateModelStateSize()
    {
        return stateActions.get(0).size() * Math.pow(maModelGen.numModels, 2);
    }

    THTSRunInfo run(TestFileInfo tfi, String logFilesLocation, boolean debug, int run) throws Exception {
        THTSRunInfo runInfo = new THTSRunInfo();

        PrismLog mainLog;
        if (debug)
            mainLog = new PrismFileLog("stdout");
        else
            mainLog = new PrismDevNullLog();

        String runName = configname + "_" + tfi.getFilename() + "_r" + run;
        Prism prism = new Prism(mainLog);
        PrismLog fileLog = new PrismFileLog(logFilesLocation + "log_" + runName + "_justmdp" + ".txt");

        prism.initialise();
        prism.setEngine(Prism.EXPLICIT);

        mainLog.println("Initialised Prism");

        stateActions = new ArrayList<>();
        singleAgentStateValues = solveMaxTaskForAllSingleAgents(prism, mainLog, logFilesLocation, tfi.getFilenames(),
                tfi.getPropertiesfile(), stateActions);

        maModelGen = createNestedMultiAgentModelGen(prism, mainLog, tfi.getFilenames(),
                tfi.getPropertiesfile(), logFilesLocation,
                tfi.isHasSharedState());


        mainLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
        fileLog.println("Tie Breaking Order " + tieBreakingOrder.toString());

        initialiseConfiguration(fileLog);

        RewardHelper rewardH = new RewardHelperMultiAgent(maModelGen, RewardCalculation.SUM);

        mainLog.println("Max Rollouts: " + maxRollouts);
        mainLog.println("Max TrialLen: " + trialLength);
        fileLog.println("Max Rollouts: " + maxRollouts);
        fileLog.println("Max TrialLen: " + trialLength);

        mainLog.println("\nInitialising THTS");
        fileLog.println("\nInitialising THTS");
        boolean doForwardBackup = true;
        mainLog.println("Running thts");
        TrialBasedTreeSearch thts = new TrialBasedTreeSearch(maModelGen, maxRollouts,
                trialLength, heuristic, actSel, outSel, rewardH, backup, doForwardBackup, tieBreakingOrder, mainLog,
                fileLog);
        if (dovipolcheckonintervals) {
            thts.enablePolCheckAtIntervals(getViOnPolIntervalInMS(), prism);
        }
        mainLog.println("\nBeginning THTS");
        fileLog.println("\nBeginning THTS");
        thts.setName(runName);
        thts.setResultsLocation(logFilesLocation);
        if (this.timeBound) {
            thts.setTimeBound(true);
            runInfo.setTimeLimited(true);
            runInfo.setMaxTimeLimit(getTimeTimeLimitInMS());

        }

        try {
            thts.run(debug);
        } catch (StackOverflowError e) {

            runInfo.setStackoverflowerror(true);
            thts.calculateDuration();
            thts.trialLenArray.add(thts.trialLen);

        }
        mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
        fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
        mainLog.println("Attempting Value Iteration on Policy");
        HashMap<Objectives, Double> tempres = thts.doVIOnPolicy(polActSel, logFilesLocation, run, prism);
        mainLog.println(tempres);

        mainLog.close();
        fileLog.close();
        prism.closeDown();
        runInfo.setInitialStateSolved(thts.getRootNode(0).isSolved());
        runInfo.setVipol(tempres);
        runInfo.setNumRolloutsTillSolved(thts.numRollouts);
        runInfo.setInitialStateValues(thts.getInitialStateBounds());
        runInfo.setDuration(thts.getDuration());
        runInfo.setTrialLenStuff(thts.trialLenArray);
        runInfo.setChanceNodesExp(thts.chanceNodesExplored);
        runInfo.setDecisionNodesExp(thts.decisionNodesExplored);
        runInfo.setVipolAtIntervals(thts.timeValues);

        return runInfo;
    }

    protected abstract void initialiseConfiguration(PrismLog fileLog);

    public void setTieBreakingOrder() {
        tieBreakingOrder = new ArrayList<>();
        tieBreakingOrder.add(Objectives.TaskCompletion);
        tieBreakingOrder.add(Objectives.Cost);
    }

    public MultiAgentNestedProductModelGenerator createNestedMultiAgentModelGen(Prism prism, PrismLog mainLog,
                                                                                ArrayList<String> filenames, String propertiesFileName, String resultsLocation, boolean hasSharedState)
            throws PrismException, IOException {

        AcceptanceType[] allowedAcceptance = {AcceptanceType.RABIN, AcceptanceType.REACH};

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
        List<Expression> processedExprs = new ArrayList<>();
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
//		System.in.read();
        // hmmmm so this is important I guess
        // and we have a single safety da okay
        // oooo reward structures we don't have to care about
        // right o -lets do this
        // for the honor of greyskull

        explicit.LTLModelChecker ltlMC = new LTLModelChecker(prism);

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

            labelExprsList.add(labelExprs);
            das.add(da);
            mainLog.println("Created DA for " + expr.toString());
        }

        ArrayList<String> sharedStateVars = new ArrayList<String>();
        if (hasSharedState)
            sharedStateVars.add("door0");

        MultiAgentNestedProductModelGenerator mapmg = new MultiAgentNestedProductModelGenerator(mfmodgens, das,
                labelExprsList, safetydaind, sharedStateVars);

        return mapmg;
    }

    public ArrayList<HashMap<Objectives, HashMap<State, Double>>> solveMaxTaskForAllSingleAgents(Prism prism,
                                                                                                 PrismLog mainLog, String resultsLocation, ArrayList<String> fns, String propFilename,
                                                                                                 ArrayList<HashMap<State, Object>> stateActions) throws Exception {
        SingleAgentSolverMaxExpTask sas = new SingleAgentSolverMaxExpTask(prism, mainLog, resultsLocation);
        // so now we can read in the model
        ArrayList<HashMap<Objectives, HashMap<State, Double>>> allStateValues = new ArrayList<>();
        for (String filename : fns) {
            String[] nameval = filename.split("/");
            sas.setName(nameval[nameval.length - 1].replaceAll(".prism", ""));
            sas.loadModel(filename);
            sas.loadProperties(propFilename);
            // so we need to edit this bit
            // so we need a new function with the strategy

            HashMap<Objectives, HashMap<State, Double>> stateValues = new HashMap<>();
            HashMap<State, Object> stateAction = new HashMap<>();
            sas.getStateValuesAndStrategies(stateValues, stateAction);
            stateActions.add(stateAction);
            allStateValues.add(stateValues);
        }
        return allStateValues;
    }

    public String getConfigname() {
        return configname;
    }

    public void setConfigname(String configname) {

        if (isUseSASH()) {
            if(!configname.contains("SASH"))
            configname += "_SASH";
        }
        if (isUseActSelForBackupUpdate()) {
            if(!configname.contains("ASBU"))
            configname += "_ASBU";
        }
        this.configname = configname;
    }

    public ActionSelector getActSel() {
        return actSel;
    }

    public void setActSel(ActionSelector actSel) {
        this.actSel = actSel;
    }

    public Heuristic getHeuristic() {
        return heuristic;
    }

    public void setHeuristic(Heuristic heuristic) {
        this.heuristic = heuristic;
    }

    public BackupNVI getBackup() {
        return backup;
    }

    public void setBackup(BackupNVI backup) {
        this.backup = backup;
    }

    public ActionSelector getPolActSel() {
        return polActSel;
    }

    public void setPolActSel(ActionSelector polActSel) {
        this.polActSel = polActSel;
    }

    public OutcomeSelector getOutSel() {
        return outSel;
    }

    public void setOutSel(OutcomeSelector outSel) {
        this.outSel = outSel;
    }

    public boolean isUseSASH() {
        return useSASH;
    }

    public void setUseSASH(boolean useSASH) {
        this.useSASH = useSASH;
    }

    public boolean isUseActSelForBackupUpdate() {
        return useActSelForBackupUpdate;
    }

    public void setUseActSelForBackupUpdate(boolean useActSelForBackupUpdate) {
        this.useActSelForBackupUpdate = useActSelForBackupUpdate;
    }

    public int getTrialLength() {
        return trialLength;
    }

    public void setTrialLength(int trialLength) {
        this.trialLength = trialLength;
    }

    public int getMaxRollouts() {
        return maxRollouts;
    }

    public void setMaxRollouts(int maxRollouts) {
        this.maxRollouts = maxRollouts;
    }

    public double getMaxCost() {
        return maxCost;
    }

    public void setMaxCost(double maxCost) {
        this.maxCost = maxCost;
    }

    public float getEpsilon() {
        return epsilon;
    }

    public void setEpsilon(float epsilon) {
        this.epsilon = epsilon;
    }


    public boolean isTimeBound() {
        return timeBound;
    }

    public void setTimeBound(boolean timeBound) {
        this.timeBound = timeBound;
    }

    public MultiAgentNestedProductModelGenerator getMaModelGen() {
        return maModelGen;
    }

    public void setMaModelGen(MultiAgentNestedProductModelGenerator maModelGen) {
        this.maModelGen = maModelGen;
    }

    public ArrayList<HashMap<State, Object>> getStateActions() {
        return stateActions;
    }

    public void setStateActions(ArrayList<HashMap<State, Object>> stateActions) {
        this.stateActions = stateActions;
    }

    public ArrayList<HashMap<Objectives, HashMap<State, Double>>> getSingleAgentStateValues() {
        return singleAgentStateValues;
    }

    public void setSingleAgentStateValues(ArrayList<HashMap<Objectives, HashMap<State, Double>>> singleAgentStateValues) {
        this.singleAgentStateValues = singleAgentStateValues;
    }

    public ArrayList<Objectives> getTieBreakingOrder() {
        return tieBreakingOrder;
    }

    public void setTieBreakingOrder(ArrayList<Objectives> tieBreakingOrder) {
        this.tieBreakingOrder = tieBreakingOrder;
    }

    public boolean isDovipolcheckonintervals() {
        return dovipolcheckonintervals;
    }

    public void setDovipolcheckonintervals(boolean dovipolcheckonintervals) {
        this.dovipolcheckonintervals = dovipolcheckonintervals;
    }

    public double getEgreedy() {
        return egreedy;
    }

    public void setEgreedy(double egreedy) {
        this.egreedy = egreedy;
    }

    public void createMinMaxVals() {
        minMaxVals = new HashMap<>();
        minMaxVals.put(Objectives.Cost, new AbstractMap.SimpleEntry<>(0., maxCost));
        minMaxVals.put(Objectives.TaskCompletion,
                new AbstractMap.SimpleEntry<>(0., (double) maModelGen.numDAs));
    }
}
