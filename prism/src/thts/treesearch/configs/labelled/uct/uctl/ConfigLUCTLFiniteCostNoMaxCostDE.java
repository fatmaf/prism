package thts.treesearch.configs.labelled.uct.uctl;


public class ConfigLUCTLFiniteCostNoMaxCostDE extends ConfigLUCTL {

    public ConfigLUCTLFiniteCostNoMaxCostDE(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean doGreedyActSel)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,false,doGreedyActSel);

    }



}
