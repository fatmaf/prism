package thts.treesearch.configs.plain.egreedy.elugreedy;


public class ConfigeLUGreedyFiniteCostNoMaxCostDE extends ConfigeLUGreedy {


    public ConfigeLUGreedyFiniteCostNoMaxCostDE(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean greedyActSel) {
        super(timeBound, useSASH, useActSelForBackup,dointervalvi,true,false,greedyActSel);

    }


}
