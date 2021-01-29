package thts.treesearch.backup;


import prism.PrismLog;
import thts.modelgens.MultiAgentNestedProductModelGenerator;
import thts.treesearch.actionselector.ActionSelector;
import thts.treesearch.utils.DecisionNode;
import thts.treesearch.utils.Objectives;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;


public class BackupFullBelmanCapRelPenalty extends BackupFullBelmanCap {


    MultiAgentNestedProductModelGenerator mapmg;



    public BackupFullBelmanCapRelPenalty(MultiAgentNestedProductModelGenerator mapmg,
                                         ArrayList<Objectives> tieBreakingOrder, ActionSelector actSel, float epsilon,
                                         HashMap<Objectives, Entry<Double, Double>> minMaxVals, PrismLog backUpLog, boolean doUpdatePerActSel) {
        super(tieBreakingOrder,actSel,epsilon,minMaxVals,backUpLog,doUpdatePerActSel);
        this.mapmg = mapmg;
    }

    public BackupFullBelmanCapRelPenalty(MultiAgentNestedProductModelGenerator mapmg,
                                         ArrayList<Objectives> tieBreakingOrder, ActionSelector actSel, float epsilon,
                                         HashMap<Objectives, Entry<Double, Double>> minMaxVals, boolean doUpdatePerActSel) {
        super(tieBreakingOrder,actSel,epsilon,minMaxVals,doUpdatePerActSel);
        this.mapmg = mapmg;
    }


    @Override
    double getCostPenalty(DecisionNode dn)
    {
       return BackupHelper.getRelativeCostPenalty(dn,minMaxVals,mapmg);
    }


}
