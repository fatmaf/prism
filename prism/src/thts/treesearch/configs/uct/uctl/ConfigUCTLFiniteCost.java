package thts.treesearch.configs.uctl;


public class ConfigUCTLFiniteCost extends ConfigUCTL {

    public ConfigUCTLFiniteCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean polGreedyActSel)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,true,polGreedyActSel);

    }

    public ConfigUCTLFiniteCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean polGreedyActSel, boolean maxCostDeadends)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,maxCostDeadends,polGreedyActSel);

    }
}
