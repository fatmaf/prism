package thts.treesearch.configs.plain.egreedy.elgreedy;


public class ConfigeLGreedyFiniteCost extends ConfigeLGreedy {

    public ConfigeLGreedyFiniteCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean polGreedyActSel)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,true,polGreedyActSel);

    }

    public ConfigeLGreedyFiniteCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean polGreedyActSel, boolean maxCostDeadends)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,maxCostDeadends,polGreedyActSel);

    }
}
