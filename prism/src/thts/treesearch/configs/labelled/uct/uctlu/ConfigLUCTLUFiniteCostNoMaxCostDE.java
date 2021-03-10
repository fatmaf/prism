package thts.treesearch.configs.labelled.uct.uctlu;


public class ConfigLUCTLUFiniteCostNoMaxCostDE extends ConfigLUCTLU {

    public ConfigLUCTLUFiniteCostNoMaxCostDE(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean doGreedyActSel)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,false,doGreedyActSel);

    }



}
