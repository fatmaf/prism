package thts.treesearch.configs.greedy.lugreedy;

import prism.PrismLog;
import thts.treesearch.actionselector.*;
import thts.treesearch.backup.BackupLabelledFullBelmanCap;
import thts.treesearch.configs.Configuration;
import thts.treesearch.heuristic.MultiAgentHeuristicTC;
import thts.treesearch.outcomeselector.OutcomeSelectorProb;

public class ConfigLUGreedy extends Configuration {


    public ConfigLUGreedy(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi) {
        this(timeBound, useSASH, useActSelForBackup, dointervalvi, false, false, false);
    }

    public ConfigLUGreedy(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean domaxcost, boolean maxcostdeadends) {
        this(timeBound, useSASH, useActSelForBackup, dointervalvi, domaxcost, maxcostdeadends, false);
    }

    public ConfigLUGreedy(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean domaxcost, boolean maxcostdeadends, boolean policyActSelGreedy) {
        super(timeBound, useSASH, useActSelForBackup);
        setDovipolcheckonintervals(dointervalvi);
        setDomaxcost(domaxcost);
        setMaxcostdeadends(maxcostdeadends);
        setPolicyActSelGreedy(policyActSelGreedy);
        String configname = "LUGreedy";
        createConfigName(configname);
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
        ActionSelector greedyActSel = new ActionSelectorGreedySimpleUpperLowerBound(getTieBreakingOrder(), false);
        ActionSelector rolloutPol = new ActionSelectorSASRolloutPol(getMaModelGen(), getStateActions());
        setActSel(new ActionSelectorMCTS(greedyActSel, rolloutPol));
        setOutSel(new OutcomeSelectorProb());
        setBackup( new BackupLabelledFullBelmanCap(getTieBreakingOrder(), greedyActSel, getEpsilon(), getMinMaxVals(), fileLog, isUseActSelForBackupUpdate()));
        ((BackupLabelledFullBelmanCap) getBackup()).setMarkMaxCostAsDeadend(isMaxcostdeadends());
        setPolActSel( new ActionSelectorMultiGreedySimpleLowerBound(getTieBreakingOrder()));
        if (isPolicyActSelGreedy())
            setPolActSel(greedyActSel);

    }
}
