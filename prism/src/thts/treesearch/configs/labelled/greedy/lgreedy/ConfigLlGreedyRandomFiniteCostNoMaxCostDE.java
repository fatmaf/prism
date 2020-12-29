package thts.treesearch.configs.labelled.greedy.lgreedy;


public class ConfigLlGreedyRandomFiniteCostNoMaxCostDE extends ConfigLlGreedyRandom {
    public ConfigLlGreedyRandomFiniteCostNoMaxCostDE(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean policyActSelGreedy)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,false,policyActSelGreedy);

    }


}
