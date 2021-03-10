package thts.treesearch.configs.plain.uct.uctl;

import prism.PrismLog;
import thts.treesearch.actionselector.*;
import thts.treesearch.backup.BackupFullBelmanCap;
import thts.treesearch.configs.ConfigCategory;
import thts.treesearch.configs.Configuration;
import thts.treesearch.heuristic.MultiAgentHeuristicTC;
import thts.treesearch.outcomeselector.OutcomeSelectorProb;

import java.util.ArrayList;

public class ConfigUCTL extends Configuration {


    public ConfigUCTL(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi) {
        this(timeBound, useSASH, useActSelForBackup, dointervalvi, false, false, false);


    }


    public ConfigUCTL(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean domaxcost, boolean maxcostdeadends) {
        this(timeBound, useSASH, useActSelForBackup, dointervalvi, domaxcost, maxcostdeadends, false);
    }

    public ConfigUCTL(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean domaxcost, boolean maxcostdeadends
            , boolean policyActSelGreedy) {
        super(timeBound, useSASH, useActSelForBackup);
        setDovipolcheckonintervals(dointervalvi);
        setDomaxcost(domaxcost);
        setMaxcostdeadends(maxcostdeadends);
        setPolicyActSelGreedy(policyActSelGreedy);
        String configname = "P_UCTL";
        createConfigName(configname);
    }
    @Override
    protected void setCategories()
    {

        addCategory(ConfigCategory.UCT);
        addCategory(ConfigCategory.LOWER);
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
        ActionSelector greedyActSel = new ActionSelectorGreedySimpleLowerBound(getTieBreakingOrder(), false);
        ActionSelector rolloutPol = new ActionSelectorSASRolloutPol(getMaModelGen(), getStateActions());
        ArrayList<Boolean> tieBreakingOrderBools = new ArrayList<>();
        tieBreakingOrderBools.add(false);
        tieBreakingOrderBools.add(false);
        ActionSelector uctActSel = new ActionSelectorUCT(getTieBreakingOrder(),tieBreakingOrderBools);
        setActSel(new ActionSelectorMCTS(uctActSel, rolloutPol));
        setOutSel(new OutcomeSelectorProb());
        setBackup( new BackupFullBelmanCap(getTieBreakingOrder(), greedyActSel, getEpsilon(), getMinMaxVals(), fileLog, isUseActSelForBackupUpdate()));

        ((BackupFullBelmanCap) getBackup()).setMarkMaxCostAsDeadend(isMaxcostdeadends());
        setPolActSel( new ActionSelectorMultiGreedySimpleLowerBound(getTieBreakingOrder()));
        if (isPolicyActSelGreedy())
            setPolActSel(new ActionSelectorGreedySimpleLowerBound(getTieBreakingOrder(), false));
        setBaseActSel(greedyActSel);

    }
}
