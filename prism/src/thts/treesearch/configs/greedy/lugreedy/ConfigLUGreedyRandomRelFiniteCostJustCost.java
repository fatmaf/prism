package thts.treesearch.configs.greedy.lugreedy;

import prism.PrismLog;
import thts.treesearch.actionselector.*;
import thts.treesearch.backup.BackupLabelledFullBelmanCapRelPenalty;
import thts.treesearch.configs.Configuration;
import thts.treesearch.heuristic.MultiAgentHeuristicTC;
import thts.treesearch.outcomeselector.OutcomeSelectorProb;
import thts.treesearch.utils.Objectives;

import java.util.ArrayList;

public class ConfigLUGreedyRandomRelFiniteCostJustCost extends Configuration {


//    boolean maxcostdeadends = true;

    public ConfigLUGreedyRandomRelFiniteCostJustCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi) {
        this(timeBound, useSASH, useActSelForBackup, dointervalvi, true, false);
    }


    public ConfigLUGreedyRandomRelFiniteCostJustCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean maxcostdeadends) {
        this(timeBound, useSASH, useActSelForBackup, dointervalvi, maxcostdeadends, false);
    }

    public ConfigLUGreedyRandomRelFiniteCostJustCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup,
                                                     boolean dointervalvi, boolean maxcostdeadends, boolean policyActSelGreedy) {
        super(timeBound, useSASH, useActSelForBackup);
        setDovipolcheckonintervals(dointervalvi);
        setDomaxcost(true);
        setMaxcostdeadends(maxcostdeadends);
        setPolicyActSelGreedy(policyActSelGreedy);
        String configname = "Cost_LUGreedyRandomRelFC";
        createConfigName(configname);
    }
    @Override
    public void setTieBreakingOrder()
    {
        ArrayList<Objectives> tieBreakingOrder = new ArrayList<>();
      //  tieBreakingOrder.add(Objectives.TaskCompletion);
        tieBreakingOrder.add(Objectives.Cost);
        setTieBreakingOrder(tieBreakingOrder);

    }
    public void doGreedyPolActSel()
    {
        setPolicyActSelGreedy(true);
    }
    @Override
    protected void initialiseConfiguration(PrismLog fileLog) {

        setTrialLength(DEFAULTTRIALLEN);

        setMaxCost(estimateModelStateSize());
        createMinMaxVals();
        setHeuristic(new MultiAgentHeuristicTC(getMaModelGen(), getSingleAgentStateValues(), getMinMaxVals(), isUseSASH()));
        ActionSelector greedyActSelRandom = new ActionSelectorGreedySimpleUpperLowerBound(getTieBreakingOrder(), true);
        ActionSelector rolloutPol = new ActionSelectorSASRolloutPol(getMaModelGen(), getStateActions());
        setActSel(new ActionSelectorMCTS(greedyActSelRandom, rolloutPol));
        setOutSel(new OutcomeSelectorProb());
        ActionSelector greedyActSel = new ActionSelectorGreedySimpleUpperLowerBound(getTieBreakingOrder(), false);
        setBackup(new BackupLabelledFullBelmanCapRelPenalty(getMaModelGen(), getTieBreakingOrder(), greedyActSel, getEpsilon(), getMinMaxVals(), fileLog, isUseActSelForBackupUpdate()));
        ((BackupLabelledFullBelmanCapRelPenalty) getBackup()).setMarkMaxCostAsDeadend(isMaxcostdeadends());
        setPolActSel(new ActionSelectorMultiGreedySimpleLowerBound(getTieBreakingOrder()));
        if (isPolicyActSelGreedy())
            setPolActSel(greedyActSel);

    }
}
