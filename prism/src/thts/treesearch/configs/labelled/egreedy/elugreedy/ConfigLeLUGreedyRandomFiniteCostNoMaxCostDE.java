package thts.treesearch.configs.labelled.egreedy.elugreedy;

public class ConfigLeLUGreedyRandomFiniteCostNoMaxCostDE extends ConfigLeLUGreedyRandom {

    public ConfigLeLUGreedyRandomFiniteCostNoMaxCostDE(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean greedyActSel)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,false,greedyActSel);

    }

}
