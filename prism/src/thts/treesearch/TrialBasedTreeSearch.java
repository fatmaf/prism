package thts.treesearch;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.Stack;

import explicit.MDPModelChecker;
import explicit.ProbModelChecker;
import explicit.rewards.MDPRewardsSimple;
import parser.State;
import prism.PrismException;
import prism.PrismLog;
import prism.DefaultModelGenerator;
import prism.Prism;

import thts.utils.MDPCreator;
import thts.treesearch.actionselector.ActionSelector;
import thts.treesearch.actionselector.ActionSelectorMultiGreedySimpleLowerBound;
import thts.treesearch.backup.BackupNVI;
import thts.treesearch.heuristic.Heuristic;
import thts.treesearch.utils.*;
import thts.vi.MDPValIter;
import thts.vi.MDPValIter.ModelCheckerMultipleResult;
import thts.treesearch.outcomeselector.OutcomeSelector;
import thts.treesearch.rewardhelper.RewardHelper;

public class TrialBasedTreeSearch {

    DefaultModelGenerator productModelGen;
    protected int maxRollouts;
    protected int maxTrialLen;
    Heuristic hf;
    ActionSelector actSel;
    OutcomeSelector outSel;
    RewardHelper rewH;
    BackupNVI backup;
    protected PrismLog mainLog;
    protected PrismLog fileLog;

    protected HashMap<String, Node> nodesAddedSoFar;
    public ArrayList<Integer> trialLenArray = new ArrayList<Integer>();

    public HashMap<Long, HashMap<Objectives, Double>> timeValues;
    public int decisionNodesExplored;
    public int chanceNodesExplored;
    public int numRollouts;
    public int trialLen;
    private boolean doForwardBackup;
    protected ArrayList<Objectives> tieBreakingOrder;

    protected String resultsLocation;
    protected String name = null;
    protected MDPCreator trialMDP;
    protected VisualiserLog vl;
    protected boolean timeBound = false;
    public long timeLimitInMS = 15 * 60 * 1000; // 1000000; //15 mins

    int maxPolEvals = 0;
    boolean polEvals[];
    int currentPolEval = 0;

    public void setTimeBound(boolean t) {
        timeBound = t;
    }

    public boolean isTimeBound() {
        return timeBound;
    }

    public void setTimeLimitInMilliSeconds(long time) {
        timeLimitInMS = time;
    }

    public long getTimeLimitInMilliSeconds() {
        return timeLimitInMS;
    }

    long startTime = -1;
    long endTime = -1;
    long duration = -1;
    public int avgTrialLen;

    boolean doPolicyCheckAtIntervals = false;
    long polCheckIntervalInMS = 2 * 60 * 1000;// 5mins
    long polCheckIntervalErrorInMS = 1000; //
    long timeAtPrevPolCheck = 0;
    private Prism polPrism;

    boolean debug = false;

    public void enablePolCheckAtIntervals(long polCheckIntervalInMS, Prism mc) {
        if (polCheckIntervalInMS > 0)
            this.polCheckIntervalErrorInMS = polCheckIntervalInMS;
        this.doPolicyCheckAtIntervals = true;
        polPrism = mc;
        maxPolEvals = (int) (this.timeLimitInMS / this.polCheckIntervalInMS);
        currentPolEval = 0;
    }

    public void disablePolCheckAtIntervals() {
        this.doPolicyCheckAtIntervals = false;
    }

    public void startTimer() {
        startTime = System.currentTimeMillis();
    }

    public void calculateDuration() {
        endTime = System.currentTimeMillis();
        duration = endTime - startTime;

    }

    public long getDuration() {
        return duration;
    }

    public boolean hasReachedTimeLimit() {
        calculateDuration();
        if (doPolicyCheckAtIntervals) {
            long timePassedSincePrevCheck = duration - timeAtPrevPolCheck;
            long timeInterval = timePassedSincePrevCheck - polCheckIntervalInMS;

            if (timePassedSincePrevCheck > polCheckIntervalInMS) {

                try {
                    if (timeValues == null)
                        timeValues = new HashMap<>();

                    HashMap<Objectives, Double> res = doVIOnPolicy(actSel, polPrism);
                    timeValues.put(duration, res);
                    timeAtPrevPolCheck = duration;
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return (duration > timeLimitInMS);
    }

    public TrialBasedTreeSearch(DefaultModelGenerator pmg, int maxRollouts, int maxTrialLen,
                                Heuristic heuristicFunction, ActionSelector actionSelection, OutcomeSelector outcomeSelection,
                                RewardHelper rewardHelper, BackupNVI backupFunction, boolean doForwardBackup,
                                ArrayList<Objectives> tieBreakingOrder, PrismLog ml, PrismLog fileLog) {
        productModelGen = pmg;
        this.maxRollouts = maxRollouts;
        this.maxTrialLen = maxTrialLen;
        mainLog = ml;
        hf = heuristicFunction;
        actSel = actionSelection;
        outSel = outcomeSelection;
        rewH = rewardHelper;
        backup = backupFunction;
        decisionNodesExplored = 0;
        chanceNodesExplored = 0;
        this.tieBreakingOrder = tieBreakingOrder;
        this.fileLog = fileLog;
        this.doForwardBackup = doForwardBackup;

    }

    public Node getRootNode(int rootNodeNum) throws PrismException {
        List<State> initialStates = productModelGen.getInitialStates();
        State s = initialStates.get(0);
        if (initialStates.size() > 1) {

            if (rootNodeNum >= initialStates.size()) {
                rootNodeNum = initialStates.size() - 1;
            }
            s = initialStates.get(rootNodeNum);
        }

        return createDecisionNode(null, s, 1.0);

    }

    public boolean checkNodeInHash(String k) {
        if (nodesAddedSoFar == null) {
            nodesAddedSoFar = new HashMap<>();
        }
        return nodesAddedSoFar.containsKey(k);
    }

    private Node getNodeFromHash(String k) {
        if (nodesAddedSoFar == null) {
            nodesAddedSoFar = new HashMap<>();
        }
        if (!nodesAddedSoFar.containsKey(k))
            return null;
        else
            return nodesAddedSoFar.get(k);
    }

    DecisionNode createDecisionNode(Node ps, State s, double tprob) throws PrismException {

        DecisionNode dn = null;
        // check if this node exists
        String k = s.toString();
        if (checkNodeInHash(k)) {
            dn = (DecisionNode) getNodeFromHash(k);
            dn.addParent(ps, tprob);
            return dn;

        }
        dn = new DecisionNode(ps, s, tprob);

        addNodeToHash(dn);
        return dn;
    }

    public Node addNodeToHash(Node n) {
        String k = n.getShortName();
        return addNodeToHash(n, k);

    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setResultsLocation(String name) {
        this.resultsLocation = name;
    }

    public String getResultsLocation() {
        return this.resultsLocation;
    }

    public Node addNodeToHash(Node n, String k) {
        Node nodeInMap = n;
        if (!nodesAddedSoFar.containsKey(k)) {
            if (n instanceof DecisionNode)
                this.decisionNodesExplored++;
            else
                this.chanceNodesExplored++;
            nodesAddedSoFar.put(k, n);
        } else {
            nodeInMap = nodesAddedSoFar.get(k);
        }
        return nodeInMap;
    }

    public int run() throws Exception {
        return run(0, false);
    }

    public int run(boolean debug) throws Exception {
        return run(0, debug);
    }

    public HashMap<Objectives, Bounds> getInitialStateBounds() throws PrismException {
        Node n0 = getRootNode(0);
        return n0.bounds;

    }

    public int run(int rootNodeNum, boolean debug) throws Exception {

        this.debug = debug;
        if (!debug)
            vl = new VisualiserLog(/* resultsLocation + name + ".vl", */ this.tieBreakingOrder,!debug);
        else
            vl = new VisualiserLog(resultsLocation + name + ".vl", this.tieBreakingOrder);
        vl.newRollout(numRollouts);

        boolean initStateSolved = false;

        while (this.numRollouts < this.maxRollouts && !initStateSolved) {
            mainLog.println("Rollout: " + numRollouts);
            fileLog.println("Rollout: " + numRollouts);
            vl.newRollout(numRollouts);

            Node n0 = getRootNode(rootNodeNum);

            visitDecisionNode((DecisionNode) n0);

            mainLog.println("Trial Ended with steps:" + trialLen);
            fileLog.println("Trial Ended with steps:" + trialLen);
            trialLenArray.add(trialLen);


            if (n0.isSolved())
                initStateSolved = true;

            vl.endRollout();
            numRollouts++;
            trialLen = 0;
        }
        Node n0 = getRootNode(rootNodeNum);
        if (!n0.isSolved()) {
            // just checking
            vl.beginActionSelection();
            vl.writeActSelChoices((DecisionNode) n0);
            vl.endActionSelectin();
        }
        vl.closeLog();


        if (!isTimeBound()) {
            this.calculateDuration();
        }
        return numRollouts;
    }

    protected boolean visitDecisionNode(DecisionNode n) throws Exception {

        int prevTrialLen = trialLen;
        boolean doBackup = true;
        if (!n.isSolved() & notTimedOut()) {

            doBackup = true;
            trialLen++;
            prevTrialLen = trialLen;
            vl.newStep(trialLen);
            if (n.numVisits == 0 && !n.hasBounds()) {
                setNodeHeuristics(n);
            }
            n.numVisits++;
			mainLog.println(
					"Step:" + trialLen + "DN:" + n.getState() + "," + n.numVisits + ",B:" + n.getBoundsString());
            vl.addStateBit(n);

            if (n.canHaveChildren()) {
                // select an action
                // so we've got to check all the actions associated with this node
                // and randomly select one


                ChanceNode selectedAction = selectAction(n);
                // lrtdp has a forward backup
//				backup.backupDecisionNode(n);

                doBackup = visitChanceNode(selectedAction);

				mainLog.println("BackupStep:" + "DN:" + n.getState() + "," + n.numVisits + ",B:" + n.getBoundsString());


            } else {

                n.setSolved();
				mainLog.println("Setting " + n.getState() + " to solved");

            }

            doBackup = backup.backupDecisionNode(n, doBackup);
			mainLog.println("BackupStep:" + prevTrialLen + "DN:" + n.getState() + "," + n.numVisits + ",B:"
					+ n.getBoundsString());
//            fileLog.println("BackupStep:" + prevTrialLen + "DN:" + n.getState() + "," + n.numVisits + ",B:"
//                    + n.getBoundsString() + (n.isSolved() ? ", solved" : ""));

        } else {
            if (n != null) {
                mainLog.println("TimedOut/Solved:" + trialLen + "DN:" + n.getState() + "," + n.numVisits + ",B:"
						+ n.getBoundsString() + " solved:" + n.isSolved());
 //               fileLog.println("TimedOut/Solved:" + trialLen + "DN:" + n.getState() + "," + n.numVisits + ",B:"
//                        + n.getBoundsString() + " solved:" + n.isSolved());
            }
        }
        return doBackup;
    }

    boolean visitChanceNode(ChanceNode n) throws Exception {

        int prevTrialLen = trialLen; // just for book keeping
        boolean doBackup = true;
        if (!n.isSolved() & notTimedOut()) {
            vl.chanceNodeString(n);
            doBackup = true;
            n.numVisits++;
			mainLog.println("Step:" + trialLen + "CN:" + n.getState() + "," + n.getAction() + "," + n.numVisits + ",B:"
					+ n.getBoundsString());
//            fileLog.println("Step:" + trialLen + "CN:" + n.getState() + "," + n.getAction() + "," + n.numVisits + ",B:"
//                    + n.getBoundsString());

            ArrayList<DecisionNode> selectedOutcome = selectOutcome(n);
            vl.endStep();
            // first we've got to initialise this chance node
            // meaning get its children really
            // or we could just select an outcome ?
            // but we still need to know how many children it has
            // and then we've got to select one of them
            // and in prism that means going over all of them
//			backup.backupChanceNode(n);
            if (doForwardBackup) {
                backup.forwardbackupChanceNode(n);
                mainLog.println("ForwardBackupStep:" + trialLen + "CN:" + n.getState() + "," + n.getAction() + ","
                        + n.numVisits + ",B:" + n.getBoundsString());
//                fileLog.println("ForwardBackupStep:" + trialLen + "CN:" + n.getState() + "," + n.getAction() + ","
//                        + n.numVisits + ",B:" + n.getBoundsString());

            }
            // outcome = selectOutcome(n)
            // for nd in outcome
            for (DecisionNode nd : selectedOutcome) {
                if (nd != null)// why does this happen ?
                {
                    doBackup = doBackup & visitDecisionNode(nd);

                } else {
                    mainLog.println("No outcome selected.");
                }
            }
            // visitDecisionNode(nd)
            // backupChanceNode(n)
            backup.backupChanceNode(n, doBackup);
            mainLog.println("BackupStep:" + prevTrialLen + "CN:" + n.getState() + "," + n.getAction() + ","
                    + n.numVisits + ",B:" + n.getBoundsString());

        } else {
            mainLog.println("Step:" + trialLen + "CN:" + n.getState() + "," + n.getAction() + "," + n.numVisits + ",B:"
                    + n.getBoundsString());
            mainLog.println("Solved or trial done");
        }
        return doBackup;
    }

    boolean notTimedOut() {
        if (!isTimeBound()) {
            if (startTime == -1)
                startTimer();

            if (maxTrialLen < 0)
                return true;
            else
                return (this.trialLen < this.maxTrialLen);
        } else {
            if (startTime == -1)
                startTimer();
            return (!hasReachedTimeLimit());
        }
    }

    void setNodeHeuristics(Node n0) throws PrismException {
        if (!n0.boundsInitialised()) {
            vl.beginHeuristicAssignment();

            if (n0 instanceof DecisionNode) {

                HashMap<Objectives, Bounds> nodehs = hf.getStateBounds(tieBreakingOrder, (DecisionNode) n0);
                mainLog.println(n0.getShortName() + " Set Node H: " + nodehs.toString());


                ((DecisionNode) n0).setBounds(nodehs);

            } else if (n0 instanceof ChanceNode) {
                hf.setChanceNodeBounds(tieBreakingOrder, (ChanceNode) n0);
                mainLog.println(n0.getShortName() + " Set Node H: " + n0.getBoundsString());
            }
            vl.writeAssignedHeuristic(n0);
            vl.endHeuristicAssignment();
        }
    }

    ChanceNode selectAction(DecisionNode n0) throws Exception {
        boolean doMin = false;
        // we set the children and their bounds
        // it would be interesting to do this on the fly
        // but i'm not sure how to without sending mapmg to action
        // selector
        // perhaps thats a good idea
        // no I dont think so
        // i think its better to do it here
//		if (actSel instanceof ActionSelectorGreedyBoundsDiff) {
        // so first we've got to see if it has no children
        if (backup instanceof BackupNVI)
            generateChildrenDecisionNode(n0);
        // then we've got to make sure we initialise
        // the bounds for all children

//		}
        if (doForwardBackup) {
            // forward backups don't depend on anything else
            // so far
            if (trialLen > 100)
                mainLog.println("error");
            backup.forwardbackupDecisionNode(n0);
            mainLog.println("ForwardBackupStep:" + trialLen + "DN:" + n0.getState() + "," + n0.numVisits + ",B:"
                    + n0.getBoundsString());


        }

        vl.beginActionSelection();
        vl.writeActSelChoices(n0);
        ChanceNode selected = actSel.selectAction(n0, doMin, fileLog);

        vl.writeSelectedAction(selected);
        vl.endActionSelectin();

        return selected;
    }

    ArrayList<DecisionNode> selectOutcome(ChanceNode n) throws Exception {

        generateChildrenChanceNode(n);

        ArrayList<DecisionNode> res = outSel.selectOutcome(n);
        vl.beginOutcomeSelection();
        vl.writeoutSelChoices(n);
        vl.writeSelectedOutcome(res);
        vl.endOutcomeSelection();
        return res;
    }

    void generateChildrenChanceNode(ChanceNode n0) throws PrismException {
        if (n0.getChildren() == null) {
            // we need its children
            productModelGen.exploreState(n0.getState());
            // now we get the children
            int c = n0.actionChoice;
            int numtransitions = productModelGen.getNumTransitions(c);
            n0.numChildren = numtransitions;
            for (int t = 0; t < numtransitions; t++) {
                double prob = productModelGen.getTransitionProbability(c, t);
                State ns = productModelGen.computeTransitionTarget(c, t);
                DecisionNode child = createDecisionNode(n0, ns, prob);

                n0.addChild(child);

            }
            // TODO: double check if we need this???
            // I dont think we do // cuz we do two levels
            if (backup instanceof BackupNVI) {
                for (DecisionNode child : n0.getChildren()) {
                    setNodeHeuristics(child);
                }
            }

        }

    }

    ChanceNode createChanceNode(DecisionNode p, Object a, int actionIndex) throws PrismException {
        String k = p.getState().toString() + a.toString();
        ChanceNode cn;
        if (checkNodeInHash(k)) {
            cn = (ChanceNode) getNodeFromHash(k);
        } else {
            cn = new ChanceNode(p, p.s, a, actionIndex);
            for (Objectives obj : tieBreakingOrder) {
                double rew = rewH.getReward(obj, cn);

                cn.setReward(obj, rew);
            }
			mainLog.println(cn.getShortName()+" Set Rew: "+cn.rewards.toString());

            addNodeToHash(cn);
        }
        return cn;
    }

    void generateChildrenDecisionNode(DecisionNode n0) throws PrismException {
        // generate the children of a decision node
        if (n0.canHaveChildren()) {
            if (n0.numActions == 0) {
                // generate its children
                // how do we do that ?
                // well we've got a bunch of actions
                // hmmm the quicker way to do this is to just choose an action
                // assuming they all have uninitialised bounds
                // and then you randomly choose one
                // and then you can go ahead and get bounds for them
                // that would make sense
                productModelGen.exploreState(n0.s);
                int numActions = productModelGen.getNumChoices();
                n0.numActions = numActions;
                // each action is a chance node
                for (int i = 0; i < numActions; i++) {
                    // we can get the name and choice index
                    Object action = productModelGen.getChoiceAction(i);
                    ChanceNode cn = createChanceNode(n0, action, i);
                    n0.addChild(action, cn);

                }
                if (backup instanceof BackupNVI) {
                    for (Object a : n0.getChildren().keySet()) {
                        ChanceNode cn = n0.getChild(a);
                        // got to do this for full bellman backups
                        generateChildrenChanceNode(cn);
                        setNodeHeuristics(cn);
                    }
                }
            }
        }
    }


    public HashMap<Objectives, Double> doVIOnPolicy(ActionSelector actSelrt, String resultsLocation, int rnNum, Prism prism)
            throws Exception {
        fileLog.println("Extracting Policy");
        long viStartTime = System.currentTimeMillis();
        // need a rewards structure
        // need a costs structure
        HashMap<Objectives, Double> resvals = new HashMap<Objectives, Double>();
        BitSet accStates = new BitSet();
        BitSet avoidStates = new BitSet();
        if (resultsLocation != null)
            vl = new VisualiserLog(resultsLocation + name + "pol.vl", this.tieBreakingOrder, true,!debug);
        vl.beginPolRun();

        Node n0 = getRootNode(rnNum);
        mainLog.println("Root node solved: " + n0.isSolved());
        MDPCreator tempMDP = new MDPCreator();
        mainLog.println("Running through");

        Stack<DecisionNode> q = new Stack<DecisionNode>();
        ArrayList<DecisionNode> seen = new ArrayList<>();
        q.push((DecisionNode) n0);
        while (!q.isEmpty()) {
            DecisionNode d = q.pop();
            if (d.isGoal) {

                int si = tempMDP.getStateIndex(d.getState());
                accStates.set(si);

            }

            if (seen.contains(d))
                continue;
            seen.add(d);
            mainLog.println(d.getShortName() + d.getBoundsString());
//            fileLog.println(d.getShortName() + d.getBoundsString());
            if (d.canHaveChildren() /*&& !d.isLeafNode()*/) {
                if (d.isLeafNode()) {

                    mainLog.println("unexplored node - exploring " + d.getState());
                    setNodeHeuristics(d);
                    generateChildrenDecisionNode(d);
                }

                if (d.getChildren().size() < 5)
                    mainLog.println(d.getChildren());

                ArrayList<ChanceNode> as;
                if (actSelrt instanceof ActionSelectorMultiGreedySimpleLowerBound) {
                    as = ((ActionSelectorMultiGreedySimpleLowerBound) actSelrt).getAllBestActions(d);
                } else {
                    ChanceNode a = actSelrt.selectAction(d, false);
                    as = new ArrayList<ChanceNode>();
                    as.add(a);
                }
                vl.beginActionSelection();
                vl.writeActSelChoices(d);
                for (ChanceNode a : as) {

                    vl.writeSelectedAction(a);


                    // get these children
                    if (a != null) {

                        mainLog.println(a);

                        ArrayList<Entry<State, Double>> successors = new ArrayList<>();
                        if (a.getChildren() != null) {
                            for (DecisionNode dnc : a.getChildren()) {
                                q.push(dnc);
                                successors.add(
                                        new AbstractMap.SimpleEntry<State, Double>(dnc.getState(), dnc.getTranProb(a)));
                            }
                            ArrayList<Double> rews = new ArrayList<Double>();
                            rews.add(getCNReward(Objectives.TaskCompletion, a));
                            rews.add(getCNReward(Objectives.Cost, a));
                            tempMDP.addAction(d.getState(), a.getAction(), successors, rews);
                        }
                    } else {
                        mainLog.println("no action for " + d.getState());
                    }

                }
                vl.endActionSelectin();
            } else {
                mainLog.println(d.getState() + (d.canHaveChildren()
                        ? (d.isLeafNode() ? "unexplored" : "can have children so whats happeing here?")
                        : " is a goal or deadend"));
            }
        }

        tempMDP.setInitialState(n0.getState());
        if(debug) {
            if (resultsLocation != null)
                tempMDP.saveMDP(resultsLocation, getName() + "_runthru.dot");
        }
        vl.endRollout();
        vl.closeLog();
        ArrayList<MDPRewardsSimple> rews = tempMDP.createRewardStructures(2);
        ProbModelChecker pmc = new ProbModelChecker(prism);

        MDPModelChecker mdpmc = new MDPModelChecker(pmc);
        avoidStates.flip(0, tempMDP.getMDP().getNumStates());
        MDPValIter vi = new MDPValIter();
        ArrayList<Boolean> minRewards = new ArrayList<>();
        minRewards.add(false);
        minRewards.add(true);
        long viduration = System.currentTimeMillis() - viStartTime;
        fileLog.println("Extracting Policy: " + viduration + " ms ("
                + TimeUnit.SECONDS.convert(viduration, TimeUnit.MILLISECONDS) + " s)");
        fileLog.println("Beginning VI on Policy");
        ModelCheckerMultipleResult result = vi.computeNestedValIterArray(mdpmc, tempMDP.getMDP(), accStates,
                /* avoidStates */null, rews, null, minRewards, null, 1, null, mainLog);
       viduration = System.currentTimeMillis() - viStartTime;
        fileLog.println("VI on Policy took + extracting policy: " + viduration + " ms ("
                + TimeUnit.SECONDS.convert(viduration, TimeUnit.MILLISECONDS) + " s)");
        resvals.put(Objectives.Probability, result.solns.get(0)[tempMDP.getMDP().getFirstInitialState()]);
        resvals.put(Objectives.TaskCompletion, result.solns.get(1)[tempMDP.getMDP().getFirstInitialState()]);
        resvals.put(Objectives.Cost, result.solns.get(2)[tempMDP.getMDP().getFirstInitialState()]);
        return resvals;

    }

    double getCNReward(Objectives obj, ChanceNode cn) throws PrismException {
        if (cn.hasRewardObjective(obj))
            return cn.getReward(obj);
        else
            return rewH.getReward(obj, cn);
    }

    public HashMap<Objectives, Double> doVIOnPolicy(ActionSelector actSelrt, Prism prism) throws Exception {
        return doVIOnPolicy(actSelrt, null, 0, prism);

    }


    public ArrayList<State> runThroughRetFinalStatesList(ActionSelector actSelrt, String resultsLocation, int rnNum,
                                                         int subGoal) throws Exception {
        ArrayList<State> finalStatesList = new ArrayList<>();
        ArrayList<DecisionNode> finalDNs = new ArrayList<>();
        boolean goalFound = false;
        Node n0 = getRootNode(rnNum);
        System.out.println("Root node solved: " + n0.isSolved());
        MDPCreator tempMDP = new MDPCreator();
        mainLog.println("Running through");
        fileLog.println("Running through");
        Stack<DecisionNode> q = new Stack<DecisionNode>();
        ArrayList<DecisionNode> seen = new ArrayList<>();
        q.push((DecisionNode) n0);
        while (!q.isEmpty()) {
            DecisionNode d = q.pop();
            if (d.isGoal) {
                goalFound = true;

            }
            if (seen.contains(d))
                continue;
            seen.add(d);
            mainLog.println(d.getShortName() + d.getBoundsString());
            fileLog.println(d.getShortName() + d.getBoundsString());
//			if (d.getShortName().contains("4,0,0,1,0"))
//				mainLog.println("debug");
            if (d.canHaveChildren() && !d.isLeafNode()) {
                if (d.getChildren().size() < 5)
                    mainLog.println(d.getChildren());
                ChanceNode a = actSelrt.selectAction(d, false);
                // get these children
                if (a != null) {

                    mainLog.println(a);
                    fileLog.println(a);
                    ArrayList<Entry<State, Double>> successors = new ArrayList<>();
                    if (a.getChildren() != null) {
                        for (DecisionNode dnc : a.getChildren()) {
                            q.push(dnc);
                            successors.add(
                                    new AbstractMap.SimpleEntry<State, Double>(dnc.getState(), dnc.getTranProb(a)));
                        }
                        tempMDP.addAction(d.getState(), a.getAction(), successors);
                    }
                } else {
                    fileLog.println("no action for " + d.getState());
                }
            } else {
                fileLog.println(d.getState() + (d.canHaveChildren()
                        ? (d.isLeafNode() ? "unexplored" : "can have children so whats happeing here?")
                        : " is a goal or deadend"));
//				finalStatesList.add(d.getState());
                finalDNs.add(d);
            }
        }
        for (DecisionNode d : finalDNs) {
            if (d.isGoal) {
                finalStatesList.add(d.getState());

//			d.setUnsolved();
                d.reset();
            } else
                d.setSolved();

        }
        tempMDP.saveMDP(resultsLocation, getName() + "sg_" + subGoal + "_rn" + rnNum + "_runthru.dot");
        boolean[] toRet = {goalFound, n0.isSolved()};
        return finalStatesList;
    }

    ArrayList<State> runThroughRetFinalStatesList(ActionSelector actSelrt, String resultsLocation, int rnNum)
            throws Exception {
        ArrayList<State> finalStatesList = new ArrayList<>();
        ArrayList<DecisionNode> finalDNs = new ArrayList<>();
        boolean goalFound = false;
        Node n0 = getRootNode(rnNum);
        String sString = n0.getState().toString();
        System.out.println("Root node solved: " + n0.isSolved());
        MDPCreator tempMDP = new MDPCreator();
        mainLog.println("Running through");
        fileLog.println("Running through");
        Stack<DecisionNode> q = new Stack<DecisionNode>();
        ArrayList<DecisionNode> seen = new ArrayList<>();
        q.push((DecisionNode) n0);
        while (!q.isEmpty()) {
            DecisionNode d = q.pop();
            if (d.isGoal) {
                goalFound = true;

            }
            if (seen.contains(d))
                continue;
            seen.add(d);
            mainLog.println(d.getShortName() + d.getBoundsString());
            fileLog.println(d.getShortName() + d.getBoundsString());
//			if (d.getShortName().contains("4,0,0,1,0"))
//				mainLog.println("debug");
            if (d.canHaveChildren() && !d.isLeafNode()) {
                if (d.getChildren().size() < 5)
                    mainLog.println(d.getChildren());
                ChanceNode a = actSelrt.selectAction(d, false);
                // get these children
                if (a != null) {

                    mainLog.println(a);
                    fileLog.println(a);
                    ArrayList<Entry<State, Double>> successors = new ArrayList<>();
                    if (a.getChildren() != null) {
                        for (DecisionNode dnc : a.getChildren()) {
                            q.push(dnc);
                            successors.add(
                                    new AbstractMap.SimpleEntry<State, Double>(dnc.getState(), dnc.getTranProb(a)));
                        }
                        tempMDP.addAction(d.getState(), a.getAction(), successors);
                    }
                } else {
                    fileLog.println("no action for " + d.getState());
                }
            } else {
                fileLog.println(d.getState() + (d.canHaveChildren()
                        ? (d.isLeafNode() ? "unexplored" : "can have children so whats happeing here?")
                        : " is a goal or deadend"));
//				finalStatesList.add(d.getState());
                finalDNs.add(d);
            }
        }
        for (DecisionNode d : finalDNs) {
            if (d.canHaveChildren()) {
                finalStatesList.add(d.getState());

//			d.setUnsolved();
                d.reset();
            } else
                d.setSolved();

        }
        tempMDP.saveMDP(resultsLocation, getName() + "_" + sString + "_rn" + rnNum + "_runthru.dot");
//		boolean[] toRet = { goalFound, n0.isSolved() };
        return finalStatesList;
    }

}
