package thts.treesearch.configs.labelled.greedy.lugreedy;



public class ConfigLluGreedyFiniteCost extends ConfigLluGreedy {


    public ConfigLluGreedyFiniteCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean greedyActSel) {
        super(timeBound, useSASH, useActSelForBackup,dointervalvi,true,true,greedyActSel);

    }
    public ConfigLluGreedyFiniteCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup,
                                     boolean dointervalvi, boolean greedyActSel, boolean domaxcostdeadends) {
        super(timeBound, useSASH, useActSelForBackup,dointervalvi,true,domaxcostdeadends,greedyActSel);

    }

}
