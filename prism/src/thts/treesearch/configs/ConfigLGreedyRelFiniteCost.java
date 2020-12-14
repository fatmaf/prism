package thts.treesearch.configs;

import prism.PrismLog;
import thts.treesearch.actionselector.*;
import thts.treesearch.backup.BackupLabelledFullBelmanCap;
import thts.treesearch.heuristic.MultiAgentHeuristicTC;
import thts.treesearch.outcomeselector.OutcomeSelectorProb;
import thts.treesearch.utils.Objectives;

import java.util.HashMap;
import java.util.Map;

public class ConfigLGreedy extends Configuration {

    boolean domaxcost = false;
    boolean maxcostdeadends = false;

    public ConfigLGreedy(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi) {
        super(timeBound, useSASH, useActSelForBackup);
        setDovipolcheckonintervals(dointervalvi);
        String configname = "LGreedy";
        if (domaxcost)
            configname += "FC";
        if (maxcostdeadends)
            configname += "_MCD_";
        setConfigname(configname);
    }


    public ConfigLGreedy(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean domaxcost, boolean maxcostdeadends) {
        super(timeBound, useSASH, useActSelForBackup);
        setDovipolcheckonintervals(dointervalvi);
        this.domaxcost = domaxcost;
        this.maxcostdeadends = maxcostdeadends;
        String configname = "LGreedy";
        if (domaxcost)
            configname += "FC";
        if (maxcostdeadends)
            configname += "_MCD_";
        setConfigname(configname);
    }

    @Override
    protected void initialiseConfiguration(PrismLog fileLog) {

        trialLength = -1;

        maxCost = 0;
        if (domaxcost)
            maxCost = stateActions.get(0).size() * Math.pow(maModelGen.numModels, 2);
        createMinMaxVals();
        heuristic = new MultiAgentHeuristicTC(maModelGen, singleAgentStateValues, getMinMaxVals(), useSASH);
        ActionSelector greedyActSel = new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder, false);
        ActionSelector rolloutPol = new ActionSelectorSASRolloutPol(maModelGen, stateActions);
        actSel = new ActionSelectorMCTS(greedyActSel, rolloutPol);
        outSel = new OutcomeSelectorProb();
        backup = new BackupLabelledFullBelmanCap(tieBreakingOrder, greedyActSel, epsilon, getMinMaxVals(), fileLog, useActSelForBackupUpdate);
        ((BackupLabelledFullBelmanCap) backup).setMarkMaxCostAsDeadend(maxcostdeadends);
        polActSel = new ActionSelectorMultiGreedySimpleLowerBound(tieBreakingOrder);//greedyActSel;

    }
}
