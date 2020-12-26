package thts.treesearch.backup;

import prism.PrismException;
import prism.PrismLog;
import thts.treesearch.actionselector.ActionSelector;
import thts.treesearch.utils.Bounds;
import thts.treesearch.utils.ChanceNode;
import thts.treesearch.utils.DecisionNode;
import thts.treesearch.utils.Objectives;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;


public class BackupFullBelmanCap implements Backup {

    ArrayList<Objectives> tieBreakingOrder;
    float epsilon;
    ActionSelector actSel;
    HashMap<Objectives, Entry<Double, Double>> minMaxVals;
    boolean doUpdatePerActSel;
    PrismLog debugLog = null;
    boolean markMaxCostAsDeadend = true;

    public boolean isMarkMaxCostAsDeadend() {
        return markMaxCostAsDeadend;
    }

    public void setMarkMaxCostAsDeadend(boolean markMaxCostAsDeadend) {
        this.markMaxCostAsDeadend = markMaxCostAsDeadend;
    }

    public BackupFullBelmanCap(ArrayList<Objectives> tieBreakingOrder, ActionSelector actSel, float epsilon,
                               HashMap<Objectives, Entry<Double, Double>> minMaxVals, PrismLog backUpLog, boolean doUpdatePerActSel) {
        this.tieBreakingOrder = tieBreakingOrder;

        this.epsilon = epsilon;
        this.actSel = actSel;
        this.minMaxVals = minMaxVals;
        this.debugLog = backUpLog;
        this.doUpdatePerActSel = doUpdatePerActSel;
    }

    public BackupFullBelmanCap(ArrayList<Objectives> tieBreakingOrder, ActionSelector actSel, float epsilon,
                               HashMap<Objectives, Entry<Double, Double>> minMaxVals, boolean doUpdatePerActSel) {
        this.tieBreakingOrder = tieBreakingOrder;

        this.epsilon = epsilon;
        this.actSel = actSel;
        this.minMaxVals = minMaxVals;
        this.doUpdatePerActSel = doUpdatePerActSel;
    }


    boolean boundsLessThanEpsilon(HashMap<Objectives, Bounds> bounds) {
        return BackupHelper.boundsLessThanEpsilon(bounds, epsilon, tieBreakingOrder);


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

        HashMap<Objectives, Bounds> bounds;
        if (doUpdatePerActSel)
            bounds = BackupHelper.residualDecision((DecisionNode) dn, tieBreakingOrder, actSel);
        else
            bounds = BackupHelper.residualDecision((DecisionNode) dn, tieBreakingOrder);

        if (bounds != null && boundsLessThanEpsilon(bounds)) {
            dn.setSolved();

        }
        return true;

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
                        if (dn.isDeadend && obj == Objectives.Progression) {
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

                    double lb = minMaxVals.get(obj).getValue();//this.maxCost;
                    double ub = minMaxVals.get(obj).getValue();//this.maxCost;
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
                        if (dn.isDeadend && obj == Objectives.Progression) {
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

                    double lb = minMaxVals.get(obj).getValue();//this.maxCost;
                    double ub = minMaxVals.get(obj).getValue();//this.maxCost;
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
                    defaultBounds.setUpper(BackupHelper.getObjectiveExtremeValueInit(obj));
                    defaultBounds.setLower(BackupHelper.getObjectiveExtremeValueInit(obj));
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
                        if (BackupHelper.isBetter(b.getUpper(), bestBounds.getUpper(), obj)) {

                            updateUpperBounds = true;
                            break;
                        } else {
                            if (!BackupHelper.isEqual(b.getUpper(), bestBounds.getUpper()))
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

                            if (BackupHelper.isBetter(b.getLower(), bestBounds.getLower(), obj)) {
                                updateLowerBounds = true;
                                break;
                            } else {
                                if (!BackupHelper.isEqual(b.getLower(), bestBounds.getLower()))
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
        if (isMarkMaxCostAsDeadend()) {
            //if decision node has cost
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
