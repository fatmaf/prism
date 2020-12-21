package thts.treesearch.configs.labelled.greedy.lgreedy;


public class ConfigLGreedyRandomFiniteCostNoMaxCostDE extends ConfigLGreedyRandom{
    public ConfigLGreedyRandomFiniteCostNoMaxCostDE(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi,boolean policyActSelGreedy)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,false,policyActSelGreedy);

    }


}
