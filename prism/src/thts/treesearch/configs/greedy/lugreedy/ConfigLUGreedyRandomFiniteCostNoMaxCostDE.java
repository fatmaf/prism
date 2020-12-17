package thts.treesearch.configs.lugreedy;

public class ConfigLUGreedyRandomFiniteCostNoMaxCostDE extends ConfigLUGreedyRandom{

    public ConfigLUGreedyRandomFiniteCostNoMaxCostDE(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi,boolean greedyActSel)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,false,greedyActSel);

    }

}
