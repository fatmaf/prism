package thts.treesearch.configs.labelled.greedy.lgreedy;


public class ConfigLGreedyRandomFiniteCost extends ConfigLGreedyRandom{
    public ConfigLGreedyRandomFiniteCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup,  boolean dointervalvi, boolean policyActSelGreedy)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,true,policyActSelGreedy);

    }
    public ConfigLGreedyRandomFiniteCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup,  boolean dointervalvi,
                                         boolean policyActSelGreedy,boolean domaxcostdeadends)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,domaxcostdeadends,policyActSelGreedy);

    }

}
