package thts.treesearch.configs.labelled.greedy.lugreedy;

import prism.PrismLog;
import thts.treesearch.actionselector.*;
import thts.treesearch.backup.BackupLabelledFullBelmanCapRelPenalty;
import thts.treesearch.configs.ConfigCategory;
import thts.treesearch.configs.Configuration;
import thts.treesearch.heuristic.MultiAgentHeuristicTCRelPenalty;
import thts.treesearch.outcomeselector.OutcomeSelectorProb;

public class ConfigLluGreedyRandomRelFiniteCost extends Configuration {


//    boolean maxcostdeadends = true;

    public ConfigLluGreedyRandomRelFiniteCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi) {
        this(timeBound, useSASH, useActSelForBackup, dointervalvi, true, false);
    }


    public ConfigLluGreedyRandomRelFiniteCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean maxcostdeadends) {
        this(timeBound, useSASH, useActSelForBackup, dointervalvi, maxcostdeadends, false);
    }

    public ConfigLluGreedyRandomRelFiniteCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup,
                                              boolean dointervalvi, boolean maxcostdeadends, boolean policyActSelGreedy) {
        super(timeBound, useSASH, useActSelForBackup);
        setDovipolcheckonintervals(dointervalvi);
        setDomaxcost(true);
        setMaxcostdeadends(maxcostdeadends);
        setPolicyActSelGreedy(policyActSelGreedy);
        String configname = "L_LUGreedyRandomRelFC";
        createConfigName(configname);
    }
    @Override
    protected void setCategories()
    {

        addCategory(ConfigCategory.GREEDY);
        addCategory(ConfigCategory.LOWER_UPPER);
        addCategory(ConfigCategory.RELATIVECOST);
    }
    public void doGreedyPolActSel() {
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
        setActSel(new ActionSelectorMCTS(greedyActSelRandom, rolloutPol));
        setOutSel(new OutcomeSelectorProb());
        ActionSelector greedyActSel = new ActionSelectorGreedySimpleUpperLowerBound(getTieBreakingOrder(), false);

        setBackup(new BackupLabelledFullBelmanCapRelPenalty(getMaModelGen(),
                getTieBreakingOrder(), greedyActSel, getEpsilon(), getMinMaxVals(),
                fileLog, isUseActSelForBackupUpdate()));
        ((BackupLabelledFullBelmanCapRelPenalty) getBackup()).setMarkMaxCostAsDeadend(isMaxcostdeadends());
        setPolActSel(new ActionSelectorMultiGreedySimpleLowerBound(getTieBreakingOrder()));
        if (isPolicyActSelGreedy())
            setPolActSel(  new ActionSelectorGreedySimpleLowerBound(getTieBreakingOrder(), false));


    }
}
