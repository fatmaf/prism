package thts.treesearch.configs.plain.egreedy.elgreedy;


public class ConfigeLGreedyFiniteCostNoMaxCostDE extends ConfigeLGreedy {

    public ConfigeLGreedyFiniteCostNoMaxCostDE(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean doGreedyActSel)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,false,doGreedyActSel);

    }



}
