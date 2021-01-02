package thts.treesearch.configs.plain.uct.uctl;

import prism.PrismLog;
import thts.treesearch.actionselector.*;
import thts.treesearch.backup.BackupFullBelmanCapRelPenalty;
import thts.treesearch.configs.ConfigCategory;
import thts.treesearch.configs.Configuration;
import thts.treesearch.heuristic.MultiAgentHeuristicTCRelPenalty;
import thts.treesearch.outcomeselector.OutcomeSelectorProb;

import java.util.ArrayList;

public class ConfigUCTLRelFiniteCost extends Configuration {



    public ConfigUCTLRelFiniteCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi) {
        this(timeBound, useSASH, useActSelForBackup, dointervalvi, true);


    }


    public ConfigUCTLRelFiniteCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean maxcostdeadends) {
        this(timeBound, useSASH, useActSelForBackup, dointervalvi, maxcostdeadends, false);

    }

    public ConfigUCTLRelFiniteCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi,
                                   boolean maxcostdeadends, boolean policyActSelGreedy) {
        super(timeBound, useSASH, useActSelForBackup);
        setDovipolcheckonintervals(dointervalvi);
        setDomaxcost(true);
        setMaxcostdeadends(maxcostdeadends);
        setPolicyActSelGreedy(policyActSelGreedy);
        String configname = "P_UCTLRelFC";
        createConfigName(configname);

    }
    @Override
    protected void setCategories()
    {

        addCategory(ConfigCategory.UCT);
        addCategory(ConfigCategory.RELATIVECOST);
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
        ActionSelector rolloutPol = new ActionSelectorSASRolloutPol(getMaModelGen(), getStateActions());
        ArrayList<Boolean> tieBreakingOrderBools = new ArrayList<>();
        tieBreakingOrderBools.add(false);
        tieBreakingOrderBools.add(false);
        ActionSelector uctActSel = new ActionSelectorUCT(getTieBreakingOrder(),tieBreakingOrderBools);
        setActSel(new ActionSelectorMCTS(uctActSel, rolloutPol));

        setOutSel(new OutcomeSelectorProb());
        ActionSelector greedyActSel = new ActionSelectorGreedySimpleLowerBound(getTieBreakingOrder(), false);
        setBackup(new BackupFullBelmanCapRelPenalty(getMaModelGen(), getTieBreakingOrder(), greedyActSel, getEpsilon(), getMinMaxVals(), fileLog, isUseActSelForBackupUpdate()));

        ((BackupFullBelmanCapRelPenalty) getBackup()).setMarkMaxCostAsDeadend(isMaxcostdeadends());
        setPolActSel(new ActionSelectorMultiGreedySimpleLowerBound(getTieBreakingOrder()));
        if (isPolicyActSelGreedy())
            setPolActSel(new ActionSelectorGreedySimpleLowerBound(getTieBreakingOrder(), false));
        setBaseActSel(greedyActSel);

    }
}
