package thts.treesearch.configs.labelled.uct.uctl;


public class ConfigLUCTLFiniteCost extends ConfigLUCTL {

    public ConfigLUCTLFiniteCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean polGreedyActSel)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,true,polGreedyActSel);

    }

    public ConfigLUCTLFiniteCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean polGreedyActSel, boolean maxCostDeadends)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,maxCostDeadends,polGreedyActSel);

    }
}
