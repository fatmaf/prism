package thts.treesearch.backup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Stack;

import prism.PrismLog;
import thts.treesearch.utils.Bounds;
import thts.treesearch.utils.Objectives;
import thts.treesearch.actionselector.ActionSelector;
import thts.treesearch.utils.ChanceNode;
import thts.treesearch.utils.DecisionNode;

public class BackupLabelledFullBelmanCap extends BackupFullBelmanCap {


    public BackupLabelledFullBelmanCap(ArrayList<Objectives> tieBreakingOrder, ActionSelector actSel, float epsilon, HashMap<Objectives, Entry<Double, Double>> minMaxVals, PrismLog backUpLog, boolean doUpdatePerActSel) {
        super(tieBreakingOrder, actSel, epsilon, minMaxVals, backUpLog, doUpdatePerActSel);
    }

    public BackupLabelledFullBelmanCap(ArrayList<Objectives> tieBreakingOrder, ActionSelector actSel, float epsilon, HashMap<Objectives, Entry<Double, Double>> minMaxVals, boolean doUpdatePerActSel) {
        super(tieBreakingOrder, actSel, epsilon, minMaxVals, doUpdatePerActSel);
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
                HashMap<Objectives, Bounds> bounds;
                if (doUpdatePerActSel)
                    bounds = BackupHelper.residualDecision((DecisionNode) s, tieBreakingOrder, actSel);
                else
                    bounds = BackupHelper.residualDecision((DecisionNode) s, tieBreakingOrder);

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
        if(debugLog!=null)
        debugLog.println("--------LRTDP Backup End " + dn.toString() + "-------------");
        return backupToRet;

    }


}
