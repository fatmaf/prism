package thts.treesearch.configs.labelled.egreedy.elgreedy;


public class ConfigLeLGreedyRandomFiniteCost extends ConfigLeLGreedyRandom {
    public ConfigLeLGreedyRandomFiniteCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean policyActSelGreedy)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,true,policyActSelGreedy);

    }
    public ConfigLeLGreedyRandomFiniteCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi,
                                           boolean policyActSelGreedy, boolean domaxcostdeadends)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,domaxcostdeadends,policyActSelGreedy);

    }

}
