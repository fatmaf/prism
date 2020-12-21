package thts.treesearch.configs.labelled.greedy.lugreedy;



public class ConfigLUGreedyFiniteCostNoMaxCostDE extends ConfigLUGreedy{


    public ConfigLUGreedyFiniteCostNoMaxCostDE(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi,boolean greedyActSel) {
        super(timeBound, useSASH, useActSelForBackup,dointervalvi,true,false,greedyActSel);

    }


}
