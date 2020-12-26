package thts.treesearch.configs.plain.greedy.lugreedy;

import prism.PrismLog;
import thts.treesearch.actionselector.*;
import thts.treesearch.backup.BackupFullBelmanCap;
import thts.treesearch.configs.ConfigCategory;
import thts.treesearch.configs.Configuration;
import thts.treesearch.heuristic.MultiAgentHeuristicTC;
import thts.treesearch.outcomeselector.OutcomeSelectorProb;

public class ConfigLUGreedyRandom extends Configuration {

    public ConfigLUGreedyRandom(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi) {
        this(timeBound, useSASH, useActSelForBackup, dointervalvi, false, false, false);
    }

    public ConfigLUGreedyRandom(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean domaxcost, boolean maxcostdeadends) {
        this(timeBound, useSASH, useActSelForBackup, dointervalvi, domaxcost, maxcostdeadends, false);
    }

    public ConfigLUGreedyRandom(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean domaxcost, boolean maxcostdeadends, boolean policyActSelGreedy) {
        super(timeBound, useSASH, useActSelForBackup);
        setDovipolcheckonintervals(dointervalvi);
        setDomaxcost(domaxcost);
        setMaxcostdeadends(maxcostdeadends);
        setPolicyActSelGreedy(policyActSelGreedy);
        String configname = "P_LUGreedyRandom";
        createConfigName(configname);
    }
    @Override
    protected void setCategories()
    {

        addCategory(ConfigCategory.GREEDY);
        addCategory(ConfigCategory.LOWER_UPPER);
    }
    public void doGreedyPolActSel()
    {
        setPolicyActSelGreedy(true);
    }
    public void doMaxCostDeadends()
    {
        this.setMaxcostdeadends(true);
    }
    @Override
    protected void initialiseConfiguration(PrismLog fileLog) {

        setTrialLength(DEFAULTTRIALLEN);
        setMaxCost(0);
        if (isDomaxcost())
            setMaxCost(estimateModelStateSize());
        createMinMaxVals();
        setHeuristic(new MultiAgentHeuristicTC(getMaModelGen(), getSingleAgentStateValues(), getMinMaxVals(), isUseSASH()));
        ActionSelector greedyActSelRandom = new ActionSelectorGreedySimpleUpperLowerBound(getTieBreakingOrder(), true);
        ActionSelector rolloutPol = new ActionSelectorSASRolloutPol(getMaModelGen(), getStateActions());
        setActSel(new ActionSelectorMCTS(greedyActSelRandom, rolloutPol));
        setOutSel(new OutcomeSelectorProb());
        ActionSelector greedyActSel = new ActionSelectorGreedySimpleUpperLowerBound(getTieBreakingOrder(), false);
        setBackup( new BackupFullBelmanCap(getTieBreakingOrder(), greedyActSel, getEpsilon(), getMinMaxVals(), fileLog, isUseActSelForBackupUpdate()));
        ((BackupFullBelmanCap) getBackup()).setMarkMaxCostAsDeadend(isMaxcostdeadends());
        setPolActSel( new ActionSelectorMultiGreedySimpleLowerBound(getTieBreakingOrder()));
        if (isPolicyActSelGreedy())
            setPolActSel(greedyActSel);

    }
}
