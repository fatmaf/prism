package thts.treesearch.configs.egreedy.elugreedy;


public class ConfigeLUGreedyRandomFiniteCost extends ConfigeLUGreedyRandom {

    public ConfigeLUGreedyRandomFiniteCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean greedyActSel)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,true,greedyActSel);

    }
    public ConfigeLUGreedyRandomFiniteCost(boolean timeBound, boolean useSASH,
                                           boolean useActSelForBackup, boolean dointervalvi,
                                           boolean greedyActSel, boolean domaxcostdeadend)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,domaxcostdeadend,greedyActSel);

    }
}
