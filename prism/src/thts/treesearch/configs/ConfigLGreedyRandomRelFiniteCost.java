package thts.treesearch.configs;

import prism.PrismLog;
import thts.treesearch.actionselector.*;
import thts.treesearch.backup.BackupLabelledFullBelmanCap;
import thts.treesearch.backup.BackupLabelledFullBelmanCapRelPenalty;
import thts.treesearch.heuristic.MultiAgentHeuristicTC;
import thts.treesearch.outcomeselector.OutcomeSelectorProb;

public class ConfigLGreedyRelFiniteCost extends Configuration {


    boolean maxcostdeadends = true;

    public ConfigLGreedyRelFiniteCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi) {
        super(timeBound, useSASH, useActSelForBackup);
        setDovipolcheckonintervals(dointervalvi);
        String configname = "LGreedyRelFC";

        if (maxcostdeadends)
            configname += "_MCD_";
        setConfigname(configname);
    }


    public ConfigLGreedyRelFiniteCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi,  boolean maxcostdeadends) {
        super(timeBound, useSASH, useActSelForBackup);
        setDovipolcheckonintervals(dointervalvi);
        this.maxcostdeadends = maxcostdeadends;
        String configname = "LGreedy";

        if (maxcostdeadends)
            configname += "_MCD_";
        setConfigname(configname);
    }

    @Override
    protected void initialiseConfiguration(PrismLog fileLog) {

        trialLength = -1;

            maxCost = stateActions.get(0).size() * Math.pow(maModelGen.numModels, 2);
        createMinMaxVals();
        heuristic = new MultiAgentHeuristicTC(maModelGen, singleAgentStateValues, getMinMaxVals(), useSASH);
        ActionSelector greedyActSel = new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder, false);
        ActionSelector rolloutPol = new ActionSelectorSASRolloutPol(maModelGen, stateActions);
        actSel = new ActionSelectorMCTS(greedyActSel, rolloutPol);
        outSel = new OutcomeSelectorProb();
        backup = new BackupLabelledFullBelmanCapRelPenalty(maModelGen,tieBreakingOrder, greedyActSel, epsilon, getMinMaxVals(), fileLog, useActSelForBackupUpdate);
        ((BackupLabelledFullBelmanCapRelPenalty) backup).setMarkMaxCostAsDeadend(maxcostdeadends);
        polActSel = new ActionSelectorMultiGreedySimpleLowerBound(tieBreakingOrder);

    }
}
