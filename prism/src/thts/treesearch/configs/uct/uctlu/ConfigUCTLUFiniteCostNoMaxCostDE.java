package thts.treesearch.configs.uct.uctlu;


public class ConfigUCTLUFiniteCostNoMaxCostDE extends ConfigUCTLU {

    public ConfigUCTLUFiniteCostNoMaxCostDE(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean doGreedyActSel)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,false,doGreedyActSel);

    }



}
