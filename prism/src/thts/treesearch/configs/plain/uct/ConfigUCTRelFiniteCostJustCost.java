package thts.treesearch.configs.plain.uct;

import prism.PrismLog;
import thts.treesearch.actionselector.*;
import thts.treesearch.backup.BackupFullBelmanCapRelPenalty;
import thts.treesearch.configs.ConfigCategory;
import thts.treesearch.configs.Configuration;
import thts.treesearch.heuristic.MultiAgentHeuristicTCRelPenalty;
import thts.treesearch.outcomeselector.OutcomeSelectorProb;
import thts.treesearch.utils.Objectives;

import java.util.ArrayList;

public class ConfigUCTRelFiniteCostJustCost extends Configuration {


//    boolean maxcostdeadends = true;

    public ConfigUCTRelFiniteCostJustCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi) {
        this(timeBound, useSASH, useActSelForBackup, dointervalvi, true, false);
    }


    public ConfigUCTRelFiniteCostJustCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean maxcostdeadends) {
        this(timeBound, useSASH, useActSelForBackup, dointervalvi, maxcostdeadends, false);
    }

    public ConfigUCTRelFiniteCostJustCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup,
                                          boolean dointervalvi, boolean maxcostdeadends, boolean policyActSelGreedy) {
        super(timeBound, useSASH, useActSelForBackup);
        setDovipolcheckonintervals(dointervalvi);
        setDomaxcost(true);
        setMaxcostdeadends(maxcostdeadends);
        setPolicyActSelGreedy(policyActSelGreedy);
        String configname = "Cost_UCTRelFC";
        createConfigName(configname);
    }
    @Override
    protected void setCategories()
    {
        addCategory(ConfigCategory.COST);
        addCategory(ConfigCategory.UCT);
        addCategory(ConfigCategory.RELATIVECOST);
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
        setHeuristic(new MultiAgentHeuristicTCRelPenalty(getMaModelGen(), getSingleAgentStateValues(), getMinMaxVals(), isUseSASH()));
        ActionSelector greedyActSelRandom = new ActionSelectorGreedySimpleUpperLowerBound(getTieBreakingOrder(), true);
        ActionSelector rolloutPol = new ActionSelectorSASRolloutPol(getMaModelGen(), getStateActions());
        ArrayList<Boolean> tieBreakingOrderBools = new ArrayList<>();
        tieBreakingOrderBools.add(false);
        ActionSelector uctActSel = new ActionSelectorUCT(getTieBreakingOrder(),tieBreakingOrderBools);
        setActSel(new ActionSelectorMCTS(uctActSel, rolloutPol));
        setActSel(new ActionSelectorMCTS(greedyActSelRandom, rolloutPol));
        setOutSel(new OutcomeSelectorProb());
        ActionSelector greedyActSel = new ActionSelectorGreedySimpleUpperLowerBound(getTieBreakingOrder(), false);
        setBackup(new BackupFullBelmanCapRelPenalty(getMaModelGen(), getTieBreakingOrder(), greedyActSel, getEpsilon(), getMinMaxVals(), fileLog, isUseActSelForBackupUpdate()));
        ((BackupFullBelmanCapRelPenalty) getBackup()).setMarkMaxCostAsDeadend(isMaxcostdeadends());
        setPolActSel(new ActionSelectorMultiGreedySimpleLowerBound(getTieBreakingOrder()));
        if (isPolicyActSelGreedy())
            setPolActSel(greedyActSel);

    }
}
