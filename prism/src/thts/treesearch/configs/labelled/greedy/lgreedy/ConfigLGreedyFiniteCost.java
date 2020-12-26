package thts.treesearch.configs.labelled.greedy.lgreedy;


public class ConfigLGreedyFiniteCost extends ConfigLGreedy{

    public ConfigLGreedyFiniteCost(boolean timeBound,boolean useSASH,boolean useActSelForBackup,boolean dointervalvi,boolean polGreedyActSel)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,true,polGreedyActSel);

    }

    public ConfigLGreedyFiniteCost(boolean timeBound,boolean useSASH,boolean useActSelForBackup,boolean dointervalvi,boolean polGreedyActSel,boolean maxCostDeadends)
    {
        super(timeBound,useSASH,useActSelForBackup,dointervalvi,true,maxCostDeadends,polGreedyActSel);

    }
}
