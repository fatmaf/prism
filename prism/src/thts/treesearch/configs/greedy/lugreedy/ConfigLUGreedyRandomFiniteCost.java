package thts.treesearch.configs.lugreedy;


public class ConfigLUGreedyRandomFiniteCost extends ConfigLUGreedyRandom{

    public ConfigLUGreedyRandomFiniteCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup,  boolean dointervalvi,boolean greedyActSel)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,true,greedyActSel);

    }
    public ConfigLUGreedyRandomFiniteCost(boolean timeBound, boolean useSASH,
                                          boolean useActSelForBackup,  boolean dointervalvi,
                                          boolean greedyActSel,boolean domaxcostdeadend)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,domaxcostdeadend,greedyActSel);

    }
}
