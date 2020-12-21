package thts.treesearch.configs.uct.uctlu;


public class ConfigUCTLUFiniteCost extends ConfigUCTLU {

    public ConfigUCTLUFiniteCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean polGreedyActSel)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,true,polGreedyActSel);

    }

    public ConfigUCTLUFiniteCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean polGreedyActSel, boolean maxCostDeadends)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,maxCostDeadends,polGreedyActSel);

    }
}
