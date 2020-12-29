package thts.treesearch.configs.labelled.egreedy.elugreedy;


public class ConfigLeLUGreedyRandomFiniteCost extends ConfigLeLUGreedyRandom {

    public ConfigLeLUGreedyRandomFiniteCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean greedyActSel)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,true,greedyActSel);

    }
    public ConfigLeLUGreedyRandomFiniteCost(boolean timeBound, boolean useSASH,
                                            boolean useActSelForBackup, boolean dointervalvi,
                                            boolean greedyActSel, boolean domaxcostdeadend)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,domaxcostdeadend,greedyActSel);

    }
}
