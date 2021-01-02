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
import thts.treesearch.backup.Backup;
import thts.treesearch.heuristic.Heuristic;
import thts.treesearch.outcomeselector.OutcomeSelector;
import thts.treesearch.rewardhelper.RewardCalculation;
import thts.treesearch.rewardhelper.RewardHelper;
import thts.treesearch.rewardhelper.RewardHelperMultiAgent;
import thts.treesearch.utils.*;
import thts.vi.SingleAgentSolverMaxExpTask;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public abstract class Configuration {
    final static int DEFAULTMAXROLLOUTS = 10000;
    final static float DEFAULTBACKUPEPSILON = 0.0001f;
    protected final static int DEFAULTTRIALLEN = -1;
    final static double DEFAULTEGREEDY = 0.8;
    final static long DEFAULTTIMELIMITINMS = 30 * 60 * 1000;

    String configname;
    ActionSelector actSel;
    Heuristic heuristic;
    Backup backup;
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
    private boolean justLogs = false;

    private ActionSelector baseActSel;

    protected ActionSelector getBaseActSel() {
        return baseActSel;
    }

    protected void setBaseActSel(ActionSelector baseActSel) {
        this.baseActSel = baseActSel;
    }

    ArrayList<ConfigCategory> categories;
    boolean timeBound;

    MultiAgentNestedProductModelGenerator maModelGen;
    ArrayList<HashMap<State, Object>> stateActions;
    ArrayList<HashMap<Objectives, HashMap<State, Double>>> singleAgentStateValues;

    ArrayList<Objectives> tieBreakingOrder;
    private boolean dovipolcheckonintervals;

    protected void addCategory(ConfigCategory c) {
        if (categories == null)
            categories = new ArrayList<>();
        if (!categories.contains(c))
            categories.add(c);
    }

    protected abstract void setCategories();

    public boolean isCategory(ConfigCategory c) {
        if (categories != null) {
            return categories.contains(c);
        } else
            return false;
    }

    public boolean isJustLogs() {
        return justLogs;
    }

    public void setJustLogs(boolean justLogs) {
        this.justLogs = justLogs;
    }

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

        if (domaxcost) {
            if (!configname.contains("FC"))
                configname += "FC";
        }
        if (maxcostdeadends) {
            if (!configname.contains("MCD"))
                configname += "_MCD";
        }
        if (policyActSelGreedy) {
            if (!configname.contains("GP"))
                configname += "_GP";
        } else {
            if (!configname.contains("_GAllActions"))
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
//        if (!isTimeBound()) {
//            if (timeTimeLimitInMS > 0)
//                setTimeBound(true);
//        }
        this.timeTimeLimitInMS = timeTimeLimitInMS;
    }

    public long getViOnPolIntervalInMS() {
        return viOnPolIntervalInMS;
    }

    public void setViOnPolIntervalInMS(long viOnPolIntervalInMS) {
        this.viOnPolIntervalInMS = viOnPolIntervalInMS;
    }


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
        setCategories();


    }

    public double estimateModelStateSize() {
        return stateActions.get(0).size() * Math.pow(maModelGen.numModels, 2);
    }

    THTSRunInfo run(TestFileInfo tfi, String logFilesLocation, boolean debug, int run, String runPrefix) throws Exception {
        THTSRunInfo runInfo = new THTSRunInfo();
        String runName = configname + "_" + tfi.getFilename() + "_" + runPrefix + "_" + run;
        PrismLog mainLog;
        if (debug)
            mainLog = new PrismFileLog(logFilesLocation + "debuglog_" + runName +  ".txt");
        else
            mainLog = new PrismDevNullLog();


        Prism prism = new Prism(mainLog);
        PrismLog fileLog;
        if (isJustLogs())
            fileLog = new PrismDevNullLog();
        else
            fileLog = new PrismFileLog(logFilesLocation + "log_" + runName +  ".txt");

        prism.initialise();
        prism.setEngine(Prism.EXPLICIT);

        mainLog.println(HelperClass.getTString() + "Initialised Prism");
        fileLog.println(HelperClass.getTString() + "Initialised Prism");
        stateActions = new ArrayList<>();
        fileLog.println(HelperClass.getTString() + "Beginning solutions for single agents");
        mainLog.println(HelperClass.getTString() + "Beginning solutions for single agents");
        long startTime = System.currentTimeMillis();
        if (tfi.getRobots() != null && tfi.getGoals() != null) {
            singleAgentStateValues = solveMaxTaskForAllSingleAgents(prism, fileLog, logFilesLocation, tfi.getFilenames(),
                    tfi.getPropertiesfile(), tfi.getRobots(), tfi.getGoals(), stateActions);
        } else {
            singleAgentStateValues = solveMaxTaskForAllSingleAgents(prism, fileLog, logFilesLocation, tfi.getFilenames(),
                    tfi.getPropertiesfile(), stateActions);
        }
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        fileLog.println(HelperClass.getTString() + "Finished Single Agent Solutions: " + duration + " ms ("
                + TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) + " s)");
        mainLog.println(HelperClass.getTString() + "Finished Single Agent Solutions: " + duration + " ms ("
                + TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) + " s)");
        maModelGen = createNestedMultiAgentModelGen(prism, mainLog, tfi.getFilenames(),
                tfi.getPropertiesfile(), logFilesLocation,
                tfi.getNumDoors(), tfi.getRobots(), tfi.getGoals());


        mainLog.println(HelperClass.getTString() + "Tie Breaking Order " + tieBreakingOrder.toString());
        fileLog.println(HelperClass.getTString() + "Tie Breaking Order " + tieBreakingOrder.toString());

        initialiseConfiguration(mainLog);

        RewardHelper rewardH = new RewardHelperMultiAgent(maModelGen, RewardCalculation.SUM);

        mainLog.println(HelperClass.getTString() + "Max Rollouts: " + maxRollouts);
        mainLog.println(HelperClass.getTString() + "Max TrialLen: " + trialLength);
        fileLog.println(HelperClass.getTString() + "Max Rollouts: " + maxRollouts);
        fileLog.println(HelperClass.getTString() + "Max TrialLen: " + trialLength);

        mainLog.println(HelperClass.getTString() + "Initialising THTS");
        fileLog.println(HelperClass.getTString() + "Initialising THTS");
        boolean doForwardBackup = true;
        mainLog.println(HelperClass.getTString() + "Running thts");
        TrialBasedTreeSearch thts = new TrialBasedTreeSearch(maModelGen, maxRollouts,
                trialLength, heuristic, actSel, outSel, rewardH, backup, doForwardBackup, tieBreakingOrder, mainLog,
                fileLog);
        if (dovipolcheckonintervals) {
            thts.enablePolCheckAtIntervals(getViOnPolIntervalInMS(), prism);
        }
        mainLog.println(HelperClass.getTString() + "Beginning THTS");
        fileLog.println(HelperClass.getTString() + "Beginning THTS");
        thts.setName(runName);
        thts.setResultsLocation(logFilesLocation);
        if (this.timeBound) {
            thts.setTimeBound(true);
            thts.setTimeLimitInMilliSeconds(getTimeTimeLimitInMS());
            runInfo.setTimeLimited(true);
            runInfo.setMaxTimeLimit(getTimeTimeLimitInMS());
            SolutionResults.timeLimit = Math.max(getTimeTimeLimitInMS()/4,5*60*1000);
        }

        try {
            thts.run(debug);

        } catch (StackOverflowError e) {

            runInfo.setStackoverflowerror(true);
            thts.calculateDuration();
            thts.trialLenArray.add(thts.trialLen);

        }
        runInfo.setInitialStateSolved(thts.getRootNode(0).isSolved());
        runInfo.setNumRolloutsTillSolved(thts.numRollouts);
        runInfo.setInitialStateValues(thts.getInitialStateBounds());
        runInfo.setDuration(thts.getDuration());
        runInfo.setTrialLenStuff(thts.trialLenArray);
        runInfo.setChanceNodesExp(thts.chanceNodesExplored);
        runInfo.setDecisionNodesExp(thts.decisionNodesExplored);
        runInfo.setVipolAtIntervals(thts.timeValues);


        mainLog.println(HelperClass.getTString() + "Getting actions with Greedy Lower Bound Action Selector");
        fileLog.println(HelperClass.getTString() + "Getting actions with Greedy Lower Bound Action Selector");
        mainLog.println(HelperClass.getTString() + "Attempting Value Iteration on Policy");
        fileLog.println(HelperClass.getTString() + "Attempting Value Iteration on Policy");
        //for a big model skip nodes
        //what is big say 30000
        boolean skipUnexploredNodes = false;
        boolean terminateearly = true;
        HashMap<Objectives, Double> tempres;
        if(getBaseActSel()!=polActSel)
        {

            fileLog.println(HelperClass.getTString() + "Attempting Value Iteration on Policy");
            fileLog.println(HelperClass.getTString() + "Using base act sel");
            SolutionResults baseSR =  thts.doVIOnPolicy(getBaseActSel(), logFilesLocation, run, prism, skipUnexploredNodes, terminateearly);

            mainLog.println(baseSR.getValuesForInitialState());
            runInfo.addSolutionResults(SolutionTypes.BaseAC,baseSR);

        }


        SolutionResults polActSR = thts.doVIOnPolicy(polActSel, logFilesLocation, run, prism, skipUnexploredNodes, terminateearly);

        runInfo.addSolutionResults(SolutionTypes.PolAC,polActSR);

        fileLog.println(HelperClass.getTString() + "Attempting Value Iteration on Policy");
        fileLog.println(HelperClass.getTString() + "Using most visited for policy instead");
        SolutionResults mvSR = thts.doVIOnPolicyMostVisitedActSel(logFilesLocation, run, prism, skipUnexploredNodes, terminateearly);

        runInfo.addSolutionResults(SolutionTypes.MostVisitedAC,mvSR);



        fileLog.println(HelperClass.getTString() + "Final Values: " + runInfo);
        mainLog.close();
        fileLog.close();
        prism.closeDown();
        return runInfo;
    }

    protected abstract void initialiseConfiguration(PrismLog fileLog);


    public void setTieBreakingOrder() {
        tieBreakingOrder = new ArrayList<>();
        tieBreakingOrder.add(Objectives.TaskCompletion);
        tieBreakingOrder.add(Objectives.Cost);
    }

    public MultiAgentNestedProductModelGenerator createNestedMultiAgentModelGen(Prism prism, PrismLog mainLog,
                                                                                ArrayList<String> filenames,
                                                                                String propertiesFileName,
                                                                                String resultsLocation, int numDoors, ArrayList<Integer> robotsList,
                                                                                ArrayList<Integer> goalsList)
            throws PrismException, IOException {

        AcceptanceType[] allowedAcceptance = {AcceptanceType.RABIN, AcceptanceType.REACH};

        String initString = "Initial States: [ ";
        // step 1
        // create the modulesfilemodelgenerators
        ArrayList<ModulesFileModelGenerator> mfmodgens = new ArrayList<>();
        ModulesFile modulesFile = null; // just here so we can use the last modules file for our properties

        for (int i = 0; i < filenames.size(); i++) {
            if (robotsList != null) {
                if (!robotsList.contains(i))
                    continue;
            }
            String modelFileName = filenames.get(i);
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
        if (goalsList != null) {
            if (!goalsList.contains(propertiesFile.getNumProperties() - 1)) {
                goalsList.add(propertiesFile.getNumProperties() - 1);
            }
        }
        for (int i = 0; i < propertiesFile.getNumProperties(); i++) {
            if (goalsList != null) {
                if (!goalsList.contains(i))
                    continue;
            }
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
        for (int i = 0; i < numDoors; i++)
            sharedStateVars.add("door" + i);

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

    public ArrayList<HashMap<Objectives, HashMap<State, Double>>> solveMaxTaskForAllSingleAgents(Prism prism,
                                                                                                 PrismLog mainLog, String resultsLocation,
                                                                                                 ArrayList<String> fns,
                                                                                                 String propFilename,
                                                                                                 ArrayList<Integer> robotsList,
                                                                                                 ArrayList<Integer> goalsList,
                                                                                                 ArrayList<HashMap<State, Object>> stateActions) throws Exception {
        SingleAgentSolverMaxExpTask sas = new SingleAgentSolverMaxExpTask(prism, mainLog, resultsLocation);
        // so now we can read in the model
        ArrayList<HashMap<Objectives, HashMap<State, Double>>> allStateValues = new ArrayList<>();
        for (int i = 0; i < fns.size(); i++) {
            if (!robotsList.contains(i))
                continue;
            String filename = fns.get(i);
            String[] nameval = filename.split("/");
            sas.setName(nameval[nameval.length - 1].replaceAll(".prism", ""));
            sas.loadModel(filename);
            sas.loadProperties(propFilename, goalsList);
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
            if (!configname.contains("SASH"))
                configname += "_SASH";
        }
        if (isUseActSelForBackupUpdate()) {
            if (!configname.contains("ASBU"))
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

    public Backup getBackup() {
        return backup;
    }

    public void setBackup(Backup backup) {
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
