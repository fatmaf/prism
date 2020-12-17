package thts.treesearch.configs.greedy.lgreedy;


public class ConfigLGreedyFiniteCostNoMaxCostDE extends ConfigLGreedy{

    public ConfigLGreedyFiniteCostNoMaxCostDE(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi,boolean doGreedyActSel)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,false,doGreedyActSel);

    }



}
