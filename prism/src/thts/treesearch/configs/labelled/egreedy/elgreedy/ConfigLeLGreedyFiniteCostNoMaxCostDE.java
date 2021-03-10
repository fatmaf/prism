package thts.treesearch.configs.labelled.egreedy.elgreedy;


public class ConfigLeLGreedyFiniteCostNoMaxCostDE extends ConfigLeLGreedy {

    public ConfigLeLGreedyFiniteCostNoMaxCostDE(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean doGreedyActSel)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,false,doGreedyActSel);

    }



}
