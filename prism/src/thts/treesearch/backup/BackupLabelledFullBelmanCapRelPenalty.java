package thts.treesearch.backup;

import prism.PrismException;
import prism.PrismLog;
import thts.modelgens.MultiAgentNestedProductModelGenerator;
import thts.treesearch.actionselector.ActionSelector;
import thts.treesearch.utils.Bounds;
import thts.treesearch.utils.ChanceNode;
import thts.treesearch.utils.DecisionNode;
import thts.treesearch.utils.Objectives;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Stack;

public class BackupLabelledFullBelmanCapRelPenalty extends BackupNVI {

    //	ArrayList<Objectives> tieBreakingOrder;
    float epsilon;
    ActionSelector actSel;
    HashMap<Objectives, Entry<Double, Double>> minMaxVals;
    boolean doUpdatePerActSel;
    PrismLog debugLog = null;
    MultiAgentNestedProductModelGenerator mapmg;

    boolean markMaxCostAsDeadend = true;

    public boolean isMarkMaxCostAsDeadend() {
        return markMaxCostAsDeadend;
    }

    public void setMarkMaxCostAsDeadend(boolean markMaxCostAsDeadend) {
        this.markMaxCostAsDeadend = markMaxCostAsDeadend;
    }

    public BackupLabelledFullBelmanCapRelPenalty(MultiAgentNestedProductModelGenerator mapmg,
                                                 ArrayList<Objectives> tieBreakingOrder, ActionSelector actSel, float epsilon,
                                                 HashMap<Objectives, Entry<Double, Double>> minMaxVals, PrismLog backUpLog, boolean doUpdatePerActSel) {
//		this.tieBreakingOrder = tieBreakingOrder;
        super(tieBreakingOrder);
        this.epsilon = epsilon;
        this.actSel = actSel;
        this.minMaxVals = minMaxVals;
        this.debugLog = backUpLog;
        this.doUpdatePerActSel = doUpdatePerActSel;
        this.mapmg = mapmg;
    }

    public BackupLabelledFullBelmanCapRelPenalty(MultiAgentNestedProductModelGenerator mapmg,
            ArrayList<Objectives> tieBreakingOrder, ActionSelector actSel, float epsilon,
                                                 HashMap<Objectives, Entry<Double, Double>> minMaxVals, boolean doUpdatePerActSel) {
//		this.tieBreakingOrder = tieBreakingOrder;
        super(tieBreakingOrder);
        this.epsilon = epsilon;
        this.actSel = actSel;
        this.minMaxVals = minMaxVals;
        this.doUpdatePerActSel = doUpdatePerActSel;
        this.mapmg = mapmg;
    }


    boolean boundsLessThanEpsilon(HashMap<Objectives, Bounds> bounds) {
        return boundsLessThanEpsilon(bounds, epsilon, tieBreakingOrder);


    }

    @Override
    public boolean backupChanceNode(ChanceNode cn, boolean doBackup) throws PrismException {
        if (debugLog != null)
            debugLog.println("Backing Up: " + cn.toString());
        updateChanceNode(cn);
        if (debugLog != null)
            debugLog.println("Backed Up: " + cn.toString());
        return doBackup;
    }

    @Override
    public boolean backupDecisionNode(DecisionNode dn, boolean doBackup) throws Exception {
        boolean backupToRet = false;
        if (doBackup) {

            if (debugLog != null) {
                debugLog.println("----------LRTDP Backup Begin " + dn.toString() + "----------------");
            }
            boolean toret = true;
            Stack<DecisionNode> open = new Stack<DecisionNode>();
            Stack<DecisionNode> closed = new Stack<DecisionNode>();
            if (!dn.isSolved()) {
                open.push(dn);
            }
            while (!open.isEmpty()) {
                DecisionNode s = open.pop();
                closed.push(s);

                if (s.canHaveChildren()) {
                    if (s.getChildren() != null) {
                        for (Object a : s.getChildren().keySet()) {
                            ChanceNode cn = s.getChild(a);
                            updateChanceNode(cn);
                        }
                    }

                }
                HashMap<Objectives, Bounds> bounds = residualDecision((DecisionNode) s);

                if (bounds != null && boundsLessThanEpsilon(bounds)) {
                    // get the best action
                    // then add in all the successors
                    // for which we need an action selector and a chance node
                    ChanceNode cn = actSel.selectAction(s, false);
                    //cn is this solved??? if its not then we cant do much

                    for (DecisionNode dnc : cn.getChildren()) {
                        if (!dnc.isSolved() & !open.contains(dnc) & !closed.contains(dnc)) {
                            if (debugLog != null)
                                debugLog.println("Adding to open list: " + dnc.toString());
                            open.push(dnc);
                        } else {

                            if (debugLog != null) {
                                if (dnc.isSolved())
                                    debugLog.println("Already Solved: " + dnc.toString());
                                if (open.contains(dnc))
                                    debugLog.println("In open list: " + dnc.toString());
                                if (closed.contains(dnc))
                                    debugLog.println("In close list: " + dnc.toString());
                            }
                        }
                    }

                } else {
                    toret = false;
                }

            }
            if (toret) {
                while (!closed.isEmpty()) {

                    DecisionNode dns = closed.pop();
                    ChanceNode cn = actSel.selectAction(dns, false);
                    updateChanceNode(cn);
//					cn.setSolved(); //so this is a problem 
                    if (debugLog != null)
                        debugLog.println("Best Action: " + cn.toString());
                    dns.setSolved();
                    if (debugLog != null)
                        debugLog.println("Set to Solved: " + dns.toString());

                }
            } else {
                while (!closed.isEmpty()) {
                    DecisionNode dns = closed.pop();
                    if (debugLog != null)
                        debugLog.println("Backing Up: " + dns.toString());
                    updateDecisionNode(dns);
                    if (debugLog != null)
                        debugLog.println("Backed Up: " + dns.toString());
                }
            }
            backupToRet = toret;

        }
        // TODO: this is not really what it says to do in lrtdp we need to check this
        else {
            // just back up this node
            updateDecisionNode(dn);
        }

        debugLog.println("--------LRTDP Backup End " + dn.toString() + "-------------");
        return backupToRet;

    }

    public void updateChanceNode(ChanceNode cn) throws PrismException {
        if (cn.getChildren() != null) {

            for (Objectives obj : tieBreakingOrder) {
                double rewHere = cn.getReward(obj);
                Bounds sumHere = new Bounds();
                boolean allChildrenAreDeadends = true;
                boolean allChildrenSolved = true;
                for (DecisionNode dn : cn.getChildren()) {

                    if (dn.hasBounds()) {
                        sumHere = sumHere.add(dn.getBoundsValueTimesTranProb(obj, cn));
                        allChildrenAreDeadends = allChildrenAreDeadends & !dn.canHaveChildren();
                        allChildrenSolved = allChildrenSolved & dn.isSolved();
                    }

                }
                cn.leadToDeadend = allChildrenAreDeadends;
                if (allChildrenSolved || allChildrenAreDeadends)
                    cn.setSolved();

                sumHere = sumHere.add(rewHere);

                sumHere = sumHere.min(minMaxVals.get(obj).getValue());

                cn.setBounds(obj, sumHere);
            }
        }

    }

    public void updateDecisionNodeAccordingToActSel(DecisionNode dn) throws Exception {
        if (dn.isDeadend || dn.isGoal) {
            Bounds b = null;
            for (Objectives obj : tieBreakingOrder) {
                switch (obj) {
                    case Probability:
                    case TaskCompletion:
                    case Cost:
                    case Progression: {
                        double lb = 0.0;
                        double ub = 0.0;
                        if(dn.isDeadend && obj == Objectives.Progression)
                        {
                            if (minMaxVals.get(Objectives.Cost).getValue() != 0) {
                                lb = dn.getBounds(obj).getLower();
                                ub = lb;
                            }
                        }
                        b = new Bounds(ub, lb);
                        break;
                    }

                }
                if (dn.isDeadend && obj == Objectives.Cost) {

                    double lb = minMaxVals.get(obj).getValue()*mapmg.getRemainingTasksFraction(dn.getState());//this.maxCost;
                    double ub = minMaxVals.get(obj).getValue()*mapmg.getRemainingTasksFraction(dn.getState());//this.maxCost;
                    b = new Bounds(ub, lb);

                }
                dn.setBounds(obj, b);

            }

        } else {
            if (dn.getChildren() != null) {

                ChanceNode cn = actSel.selectAction(dn, false);
                //so now these are our bounds
                for (Objectives obj : tieBreakingOrder) {
                    Bounds b = cn.getBounds(obj);

                    dn.setBounds(obj, new Bounds(b));
                }

            }
        }
    }

    public void updateDecisionNodeNoActSel(DecisionNode dn) {
        if (dn.isDeadend || dn.isGoal) {
            Bounds b = null;
            for (Objectives obj : tieBreakingOrder) {
                switch (obj) {
                    case Probability:
                    case TaskCompletion:
                    case Cost:
                    case Progression: {
                        double lb = 0.0;
                        double ub = 0.0;
                        if(dn.isDeadend && obj == Objectives.Progression)
                        {
                            if (minMaxVals.get(Objectives.Cost).getValue() != 0) {
                                lb = dn.getBounds(obj).getLower();
                                ub = lb;
                            }
                        }
                        b = new Bounds(ub, lb);
                        break;
                    }

                }
                if (dn.isDeadend && obj == Objectives.Cost) {

                    double lb = minMaxVals.get(obj).getValue()*mapmg.getRemainingTasksFraction(dn.getState());//this.maxCost;
                    double ub = minMaxVals.get(obj).getValue()*mapmg.getRemainingTasksFraction(dn.getState());//this.maxCost;
                    b = new Bounds(ub, lb);

                }
                dn.setBounds(obj, b);

            }

        } else {
            if (dn.getChildren() != null) {

                HashMap<Objectives, Bounds> defaultBoundsH = new HashMap<>();
                HashMap<Objectives, Bounds> bestBoundsH = new HashMap<>();
                for (Objectives obj : tieBreakingOrder) {
                    Bounds defaultBounds = new Bounds();
                    defaultBounds.setUpper(getObjectiveExtremeValueInit(obj));
                    defaultBounds.setLower(getObjectiveExtremeValueInit(obj));
                    defaultBoundsH.put(obj, defaultBounds);
                    bestBoundsH.put(obj, new Bounds(defaultBounds));
                }

                for (Object a : dn.getChildren().keySet()) {
                    ChanceNode cn = dn.getChild(a);
                    boolean updateUpperBounds = false;

                    for (Objectives obj : tieBreakingOrder) {

                        Bounds bestBounds = bestBoundsH.get(obj);
                        Bounds b;
                        if (cn.hasBounds()) {
                            b = cn.getBounds(obj);
                        } else {
                            b = defaultBoundsH.get(obj);
                        }
                        if (isBetter(b.getUpper(), bestBounds.getUpper(), obj)) {

                            updateUpperBounds = true;
                            break;
                        } else {
                            if (!isEqual(b.getUpper(), bestBounds.getUpper()))
                                break;
                        }

                    }

                    if (updateUpperBounds) {
                        for (Objectives obj : tieBreakingOrder) {
                            Bounds b = cn.getBounds(obj);
                            Bounds bestBounds = bestBoundsH.get(obj);
                            bestBounds.setUpper(b.getUpper());
                        }
                    }

                }
                for (Object a : dn.getChildren().keySet()) {
                    ChanceNode cn = dn.getChild(a);
                    boolean updateLowerBounds = false;
                    for (Objectives obj : tieBreakingOrder) {

                        Bounds bestBounds = bestBoundsH.get(obj);
                        if (cn.hasBounds()) {
                            Bounds b = cn.getBounds(obj);

                            if (isBetter(b.getLower(), bestBounds.getLower(), obj)) {
                                updateLowerBounds = true;
                                break;
                            } else {
                                if (!isEqual(b.getLower(), bestBounds.getLower()))
                                    break;
                            }
                        }

                    }
                    if (updateLowerBounds) {
                        for (Objectives obj : tieBreakingOrder) {
                            Bounds b = cn.getBounds(obj);
                            Bounds bestBounds = bestBoundsH.get(obj);
                            bestBounds.setLower(b.getLower());
                        }
                    }

                }
                dn.setBounds(bestBoundsH);


            }
        }
    }

    public void updateDecisionNode(DecisionNode dn) {
        if (this.doUpdatePerActSel)
            try {
                updateDecisionNodeAccordingToActSel(dn);
            } catch (Exception e) {
                if (debugLog != null)
                    debugLog.println(e.getStackTrace());
                else
                System.out.println(e.getStackTrace());
                updateDecisionNodeNoActSel(dn);
            }
        else
            updateDecisionNodeNoActSel(dn);
        //if decision node has cost
        if(isMarkMaxCostAsDeadend()) {
            if (dn.hasBounds()) {
                if (minMaxVals.get(Objectives.Cost).getValue() != 0) {
                    if (dn.bounds.containsKey(Objectives.Cost)) {
                        if (dn.getBounds(Objectives.Cost).diff() == 0 && dn.getBounds(Objectives.Cost).getUpper() == minMaxVals.get(Objectives.Cost).getValue()) {
                            //mark it as a deadend
                            dn.isDeadend = true;
                        }
                    }
                }
            }
        }

    }

    @Override
    public boolean forwardbackupChanceNode(ChanceNode cn) throws PrismException {
        updateChanceNode(cn);
        return true;
    }

    @Override
    public boolean forwardbackupDecisionNode(DecisionNode dn) {
        // TODO Auto-generated method stub
        updateDecisionNode(dn);
        return true;
    }

}
