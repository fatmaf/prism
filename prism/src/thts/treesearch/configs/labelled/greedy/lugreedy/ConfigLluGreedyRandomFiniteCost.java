package thts.treesearch.configs.labelled.greedy.lugreedy;


public class ConfigLluGreedyRandomFiniteCost extends ConfigLluGreedyRandom {

    public ConfigLluGreedyRandomFiniteCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean greedyActSel)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,true,greedyActSel);

    }
    public ConfigLluGreedyRandomFiniteCost(boolean timeBound, boolean useSASH,
                                           boolean useActSelForBackup, boolean dointervalvi,
                                           boolean greedyActSel, boolean domaxcostdeadend)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,domaxcostdeadend,greedyActSel);

    }
}
