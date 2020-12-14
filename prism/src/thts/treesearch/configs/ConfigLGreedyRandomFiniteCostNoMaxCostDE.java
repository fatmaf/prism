package thts.treesearch.configs;


public class ConfigLGreedyRandomFiniteCost extends ConfigLGreedyRandom{
    public ConfigLGreedyRandomFiniteCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup,  boolean dointervalvi)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,true);

    }


}
