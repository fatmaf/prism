package thts.treesearch.configs.labelled.greedy.lgreedy;


public class ConfigLlGreedyFiniteCostNoMaxCostDE extends ConfigLlGreedy {

    public ConfigLlGreedyFiniteCostNoMaxCostDE(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean doGreedyActSel)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,false,doGreedyActSel);

    }



}
