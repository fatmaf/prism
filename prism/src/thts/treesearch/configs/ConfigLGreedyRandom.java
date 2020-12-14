package thts.treesearch.configs;

import prism.PrismLog;
import thts.treesearch.actionselector.*;
import thts.treesearch.backup.BackupLabelledFullBelmanCap;
import thts.treesearch.heuristic.MultiAgentHeuristicTC;
import thts.treesearch.outcomeselector.OutcomeSelectorProb;
import thts.treesearch.utils.Objectives;

import java.util.HashMap;
import java.util.Map;

public class ConfigLGreedyRandomFiniteCost extends Configuration{
    public ConfigLGreedyRandomFiniteCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup,  boolean dointervalvi)
    {
        super(timeBound,useSASH,useActSelForBackup);
        setDovipolcheckonintervals(dointervalvi);
        setConfigname("LGreedyRandomFC");
    }

    @Override
    protected void initialiseConfiguration(HashMap<Objectives, Map.Entry<Double, Double>> minMaxVals, PrismLog fileLog) {

        setTrialLength(DEFAULTTRIALLEN);
        setMaxCost( stateActions.get(0).size() * Math.pow(maModelGen.numModels, 2));
        setHeuristic(new MultiAgentHeuristicTC(maModelGen, singleAgentStateValues, minMaxVals, useSASH));
        ActionSelector greedyActSel = new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder, true);
        ActionSelector rolloutPol = new ActionSelectorSASRolloutPol(maModelGen, stateActions);
        actSel = new ActionSelectorMCTS(greedyActSel, rolloutPol);
        outSel = new OutcomeSelectorProb();
        backup = new BackupLabelledFullBelmanCap(tieBreakingOrder,  new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder, false), epsilon, minMaxVals, fileLog, useActSelForBackupUpdate);
        polActSel = new ActionSelectorMultiGreedySimpleLowerBound(tieBreakingOrder);//greedyActSel;

    }
}
