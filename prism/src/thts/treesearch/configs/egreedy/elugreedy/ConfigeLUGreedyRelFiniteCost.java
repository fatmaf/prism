package thts.treesearch.configs.egreedy.elugreedy;

import prism.PrismLog;
import thts.treesearch.actionselector.*;
import thts.treesearch.backup.BackupLabelledFullBelmanCapRelPenalty;
import thts.treesearch.configs.Configuration;
import thts.treesearch.heuristic.MultiAgentHeuristicTC;
import thts.treesearch.outcomeselector.OutcomeSelectorProb;

public class ConfigeLUGreedyRelFiniteCost extends Configuration {


    // boolean maxcostdeadends = true;

    public ConfigeLUGreedyRelFiniteCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi) {
        this(timeBound, useSASH, useActSelForBackup, dointervalvi, true, false);
    }


    public ConfigeLUGreedyRelFiniteCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean maxcostdeadends) {
        this(timeBound, useSASH, useActSelForBackup, dointervalvi, maxcostdeadends, false);
    }

    public ConfigeLUGreedyRelFiniteCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup,
                                        boolean dointervalvi, boolean maxcostdeadends, boolean policyActSelGreedy) {
        super(timeBound, useSASH, useActSelForBackup);
        setDovipolcheckonintervals(dointervalvi);
        setDomaxcost(true);
        setMaxcostdeadends(maxcostdeadends);
        setPolicyActSelGreedy(policyActSelGreedy);
        String configname = "eLUGreedyRelFC";
        createConfigName(configname);
    }

    public void doGreedyPolActSel() {
        setPolicyActSelGreedy(true);
    }

    @Override
    protected void initialiseConfiguration(PrismLog fileLog) {

        setTrialLength(DEFAULTTRIALLEN);

        setMaxCost(estimateModelStateSize());
        createMinMaxVals();
        setHeuristic(new MultiAgentHeuristicTC(getMaModelGen(), getSingleAgentStateValues(), getMinMaxVals(), isUseSASH()));
        ActionSelector greedyActSel = new ActionSelectorGreedySimpleUpperLowerBound(getTieBreakingOrder(), false);
        ActionSelector rolloutPol = new ActionSelectorSASRolloutPol(getMaModelGen(), getStateActions());
        setActSel(new ActionSelectorMCTS(greedyActSel, rolloutPol));
        setOutSel(new OutcomeSelectorProb());

        setBackup(new BackupLabelledFullBelmanCapRelPenalty(getMaModelGen(), getTieBreakingOrder(), greedyActSel, getEpsilon(), getMinMaxVals(), fileLog, isUseActSelForBackupUpdate()));
        ((BackupLabelledFullBelmanCapRelPenalty) getBackup()).setMarkMaxCostAsDeadend(isMaxcostdeadends());
        setPolActSel(new ActionSelectorMultiGreedySimpleLowerBound(getTieBreakingOrder()));
        if (isPolicyActSelGreedy())
            setPolActSel(greedyActSel);

    }
}
