package thts.treesearch.configs.labelled.egreedy.elugreedy;


public class ConfigeLUGreedyFiniteCostNoMaxCostDE extends ConfigLeLUGreedy {


    public ConfigeLUGreedyFiniteCostNoMaxCostDE(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean greedyActSel) {
        super(timeBound, useSASH, useActSelForBackup,dointervalvi,true,false,greedyActSel);

    }


}
