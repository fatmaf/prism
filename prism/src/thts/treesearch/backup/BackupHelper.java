package thts.treesearch.backup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import thts.modelgens.MultiAgentNestedProductModelGenerator;
import thts.treesearch.actionselector.ActionSelector;
import thts.treesearch.utils.Bounds;
import thts.treesearch.utils.Objectives;
import thts.treesearch.utils.ChanceNode;
import thts.treesearch.utils.DecisionNode;

public class BackupHelper {
    public static double getCostPenalty(DecisionNode dn, HashMap<Objectives, Map.Entry<Double, Double>> minMaxVals) {
        Objectives obj = Objectives.Cost;
        return minMaxVals.get(obj).getValue();
    }

    public static double getRelativeCostPenalty(DecisionNode dn, HashMap<Objectives,
            Map.Entry<Double, Double>> minMaxVals,
                                                MultiAgentNestedProductModelGenerator mapmg) {
        Objectives obj = Objectives.Cost;
        return minMaxVals.get(obj).getValue() * mapmg.getRemainingTasksFraction(dn.getState());
    }

    public static boolean isBetter(double b1, double b2, Objectives obj) {
        boolean isBetter = false;
        switch (obj) {
            case Cost:
                isBetter = b1 < b2;
                break;
            case Progression:
            case TaskCompletion:
            case Probability:
                isBetter = b1 > b2;
                break;

        }
        return isBetter;
    }

    public static double getObjectiveExtremeValueInit(Objectives obj) {
        double toret = 0;
        switch (obj) {
            case Cost:
                toret = Double.MAX_VALUE;
                break;
            case Progression:
            case TaskCompletion:
            case Probability:
                toret = 0;
                break;

        }
        return toret;
    }

    public static boolean isEqual(double b1, double b2) {
        return (b1 == b2);
    }

    public static HashMap<Objectives, Bounds> residualDecision(DecisionNode n, ArrayList<Objectives> tieBreakingOrder, ActionSelector actSel) throws Exception {
        HashMap<Objectives, Bounds> residual = null;
        // returns the difference
        // current bounds


        if (n.getChildren() != null) {

            ChanceNode cn = actSel.selectAction(n, false);

            if (cn.hasBounds()) {
                residual = new HashMap<>();
                for (Objectives obj : tieBreakingOrder) {

                    Bounds currentBounds = n.getBounds(obj);


                    Bounds bestBounds = cn.getBounds(obj);

                    Bounds boundDiff = (currentBounds.subtract(bestBounds)).abs();

                    residual.put(obj, boundDiff);
                }
            }
        }


        return residual;
    }

    public static HashMap<Objectives, Bounds> residualDecision(DecisionNode n, ArrayList<Objectives> tieBreakingOrder) {
        HashMap<Objectives, Bounds> residual = null;
        // returns the difference
        // current bounds
        if (n.getChildren() != null) {
            if (n.allChildrenInitialised()) {
                HashMap<Objectives, Bounds> bestBoundsH = new HashMap<>();
                for (Objectives obj : tieBreakingOrder) {
                    Bounds bestBounds = new Bounds();
                    bestBounds.setUpper(getObjectiveExtremeValueInit(obj));
                    bestBounds.setLower(getObjectiveExtremeValueInit(obj));
                    bestBoundsH.put(obj, bestBounds);
                }

                for (Object a : n.getChildren().keySet()) {
                    ChanceNode cn = n.getChild(a);
                    if (cn.ignoreAction)
                        continue;
                    boolean updateUpperBounds = false;

                    for (Objectives obj : tieBreakingOrder) {

                        Bounds bestBounds = bestBoundsH.get(obj);
                        if (cn.hasBounds()) {
                            Bounds b = cn.getBounds(obj);
                            if (isBetter(b.getUpper(), bestBounds.getUpper(), obj)) {

                                updateUpperBounds = true;
                                break;
                            } else {
                                if (!isEqual(b.getUpper(), bestBounds.getUpper()))
                                    break;
                            }

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
                for (Object a : n.getChildren().keySet()) {
                    ChanceNode cn = n.getChild(a);
                    if (cn.ignoreAction)
                        continue;
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
                residual = new HashMap<>();
                for (Objectives obj : tieBreakingOrder) {

                    Bounds currentBounds = n.getBounds(obj);
                    Bounds bestBounds = bestBoundsH.get(obj);
                    Bounds boundDiff = (currentBounds.subtract(bestBounds)).abs();

                    residual.put(obj, boundDiff);
                }
            }
        }
        return residual;
    }

    public static boolean boundsLessThanEpsilon(HashMap<Objectives, Bounds> bounds, double epsilon,
                                                ArrayList<Objectives> tieBreakingOrder) {
        boolean toret = true;
        for (Objectives obj : tieBreakingOrder) {
            Bounds b = bounds.get(obj);
            if (b.getLower() > epsilon) {
                toret = false;
                break;
            }
        }
        return toret;

    }
}
