package thts.treesearch.configs.labelled.egreedy.elugreedy;


public class ConfigLeLUGreedyFiniteCost extends ConfigLeLUGreedy {


    public ConfigLeLUGreedyFiniteCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup, boolean dointervalvi, boolean greedyActSel) {
        super(timeBound, useSASH, useActSelForBackup,dointervalvi,true,true,greedyActSel);

    }
    public ConfigLeLUGreedyFiniteCost(boolean timeBound, boolean useSASH, boolean useActSelForBackup,
                                      boolean dointervalvi, boolean greedyActSel, boolean domaxcostdeadends) {
        super(timeBound, useSASH, useActSelForBackup,dointervalvi,true,domaxcostdeadends,greedyActSel);

    }

}
