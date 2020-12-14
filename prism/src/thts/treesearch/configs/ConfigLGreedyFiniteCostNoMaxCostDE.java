package thts.treesearch.configs;


public class ConfigLGreedyFiniteCostNoMaxCostDE extends ConfigLGreedy{

    public ConfigLGreedyFiniteCostNoMaxCostDE(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,true);

    }


}
