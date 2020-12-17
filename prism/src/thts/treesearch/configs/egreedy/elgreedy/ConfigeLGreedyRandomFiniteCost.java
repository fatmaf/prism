package thts.treesearch.configs.elgreedy;


import thts.treesearch.configs.lgreedy.ConfigLGreedyRandom;

public class ConfigeLGreedyRandomFiniteCost extends ConfigeLGreedyRandom {
    public ConfigeLGreedyRandomFiniteCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean policyActSelGreedy)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,true,policyActSelGreedy);

    }
    public ConfigeLGreedyRandomFiniteCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi,
                                          boolean policyActSelGreedy, boolean domaxcostdeadends)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,domaxcostdeadends,policyActSelGreedy);

    }

}
