package thts.treesearch.configs.labelled.uct.uctl;


public class ConfigUCTLFiniteCostNoMaxCostDE extends ConfigUCTL {

    public ConfigUCTLFiniteCostNoMaxCostDE(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean doGreedyActSel)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,false,doGreedyActSel);

    }



}
