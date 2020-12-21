package thts.treesearch.configs.egreedy.elgreedy;


public class ConfigeLGreedyRandomFiniteCostNoMaxCostDE extends ConfigeLGreedyRandom {
    public ConfigeLGreedyRandomFiniteCostNoMaxCostDE(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean policyActSelGreedy)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,false,policyActSelGreedy);

    }


}
