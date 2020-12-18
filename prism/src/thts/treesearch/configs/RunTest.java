package thts.treesearch.configs;


import thts.treesearch.configs.egreedy.elgreedy.*;
import thts.treesearch.configs.egreedy.elugreedy.*;
import thts.treesearch.configs.greedy.lgreedy.*;
import thts.treesearch.configs.greedy.lugreedy.ConfigLUGreedy;
import thts.treesearch.configs.greedy.lugreedy.ConfigLUGreedyRandom;
import thts.treesearch.configs.greedy.lugreedy.ConfigLUGreedyRandomRelFiniteCostJustCost;
import thts.treesearch.configs.uct.ConfigUCTRelFiniteCostJustCost;
import thts.treesearch.configs.uct.uctl.ConfigUCTL;
import thts.treesearch.configs.uct.uctl.ConfigUCTLFiniteCost;
import thts.treesearch.configs.uct.uctl.ConfigUCTLFiniteCostNoMaxCostDE;
import thts.treesearch.configs.uct.uctl.ConfigUCTLRelFiniteCost;
import thts.treesearch.configs.uct.uctlu.*;

import java.lang.reflect.Array;
import java.util.ArrayList;

//PRISM_MAINCLASS=thts.treesearch.configs.RunTest prism/bin/prism
public class RunTest {

    public static ArrayList<Configuration> getSelectedConfigs(boolean timeBound, boolean dointervalvi, long timeLimit) {
        ArrayList<Configuration> configs = new ArrayList<>();
        //1        Cost_UCTRelFC_MCD_GP	FALSE	FALSE
        Configuration costUCTRelFC_MCD_GP = new ConfigUCTRelFiniteCostJustCost(timeBound, false, false, dointervalvi);
        ((ConfigUCTRelFiniteCostJustCost) costUCTRelFC_MCD_GP).doGreedyPolActSel();
        configs.add(costUCTRelFC_MCD_GP);
        //2        Cost_UCTRelFC_MCD_ASBU_GP	FALSE	TRUE
        Configuration costUCTRelFC_MCD_ASBU_GP = new ConfigUCTRelFiniteCostJustCost(timeBound, false, true, dointervalvi);
        ((ConfigUCTRelFiniteCostJustCost) costUCTRelFC_MCD_ASBU_GP).doGreedyPolActSel();
        configs.add(costUCTRelFC_MCD_ASBU_GP);
        //3        Cost_UCTRelFC_MCD_SASH_GP	TRUE	FALSE
        Configuration costUCTRelFC_MCD_SASH_GP = new ConfigUCTRelFiniteCostJustCost(timeBound, true, false, dointervalvi);
        ((ConfigUCTRelFiniteCostJustCost) costUCTRelFC_MCD_SASH_GP).doGreedyPolActSel();
        configs.add(costUCTRelFC_MCD_SASH_GP);
        //4        Cost_UCTRelFC_MCD_SASH_ASBU_GP	TRUE	TRUE
        Configuration costUCTRelFC_MCD_SASH_ASBU_GP = new ConfigUCTRelFiniteCostJustCost(timeBound, true, true, dointervalvi);
        ((ConfigUCTRelFiniteCostJustCost) costUCTRelFC_MCD_SASH_ASBU_GP).doGreedyPolActSel();
        configs.add(costUCTRelFC_MCD_SASH_ASBU_GP);
        //5        UCTLU_GAllActions_ASBU	FALSE	TRUE
        Configuration uctlu_GAllActions_ASBU = new ConfigUCTLU(timeBound, false, true, dointervalvi);
        configs.add(uctlu_GAllActions_ASBU);
        //6        eLUGreedyRandomRelFC_MCD_GAllActions	FALSE	FALSE
        Configuration eluGreedyRandomRelFC_MCD_GAllActions = new ConfigeLUGreedyRandomRelFiniteCost(timeBound, false, false, dointervalvi);
        configs.add(eluGreedyRandomRelFC_MCD_GAllActions);
        //7        eLUGreedyRandomFC_GAllActions	FALSE	FALSE

        Configuration eluGreedyRandomFC_GAllActions = new ConfigeLUGreedyRandom(timeBound, false, false, dointervalvi, true, false);
        configs.add(eluGreedyRandomFC_GAllActions);
        //  8      eLUGreedyRandomFC_MCD_GAllActions_SASH	TRUE	FALSE
        Configuration eLUGreedyRandomFC_MCD_GAllActions_SASH = new ConfigeLUGreedyRandom(timeBound, true, false, dointervalvi, true, true);
        configs.add(eLUGreedyRandomFC_MCD_GAllActions_SASH);
        //  9      Cost_eLUGreedyRandomRelFC_MCD_SASH_GP	TRUE	FALSE
        Configuration cost_eLUGreedyRandomRelFC_MCD_SASH_GP = new ConfigeLUGreedyRandomRelFiniteCostJustCost(timeBound, true, false, dointervalvi);
        ((ConfigeLUGreedyRandomRelFiniteCostJustCost) cost_eLUGreedyRandomRelFC_MCD_SASH_GP).doGreedyPolActSel();
        configs.add(cost_eLUGreedyRandomRelFC_MCD_SASH_GP);
        //  10     Cost_eLUGreedyRandomRelFC_MCD_ASBU_GP	FALSE	TRUE
        Configuration cost_eLUGreedyRandomRelFC_MCD_ASBU_GP = new ConfigeLUGreedyRandomRelFiniteCostJustCost(timeBound, false, true, dointervalvi);
        ((ConfigeLUGreedyRandomRelFiniteCostJustCost) cost_eLUGreedyRandomRelFC_MCD_ASBU_GP).doGreedyPolActSel();
        configs.add(cost_eLUGreedyRandomRelFC_MCD_ASBU_GP);
        if (timeBound && timeLimit > 0) {
            for (Configuration config : configs) {
                config.setTimeTimeLimitInMS(timeLimit);
            }
        }
        return configs;
    }

    public static void runSmallExampleSelConfigs() {

        String resFolderExt = "tro_examples/";
        String filename = "tro_example_new_small";
        boolean hasSharedState = true;
        boolean timeBound = false;
        boolean dointervalvi = false;
        int maxRuns = 100;
        boolean debug = false;
        String resSuffix = "_re" + maxRuns + "_";

        ArrayList<Configuration> configs = getSelectedConfigs(timeBound, dointervalvi, 0);

        for (int i = 0; i < configs.size(); i++) {
            Configuration config = configs.get(i);
            RunConfiguration runconfig = new RunConfiguration();
            try {
//                System.out.println(config.getConfigname());
                System.out.println("\nRunning configuration " + config.getConfigname() + " - " + i + "/" + configs.size() + "\n");
                runconfig.run(resFolderExt, config,
                        2, 2, filename, hasSharedState, debug, resSuffix, maxRuns);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    public static void runSmallExample() {
        String resFolderExt = "tro_examples/";
        String filename = "tro_example_new_small";
        boolean[] boolvals = new boolean[]{true, false};
        boolean hasSharedState = true;
        boolean timeBound = false;
        boolean domaxcostdeadends = false;
        boolean dointervalvi = false;
        String resSuffix = "_epsilon50_";
        int maxruns = 50;
        for (boolean doPolGActSel : boolvals) {
            for (boolean useActSelForBackup : boolvals) {
                for (boolean useSASH : boolvals) {
                    ArrayList<Configuration> configs = new ArrayList<>();

                    Configuration configeLGreedyFC = new ConfigeLGreedyFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi, doPolGActSel);


                    Configuration configeLGreedyRandomFC = new ConfigeLGreedyRandomFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi, doPolGActSel);


                    Configuration configeLUGreedyFC = new ConfigeLUGreedyFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi, doPolGActSel);
                    Configuration configeLUGreedyRandomFC = new ConfigeLUGreedyRandomFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi, doPolGActSel);
                    Configuration configeLGreedy = new ConfigeLGreedy(timeBound, useSASH, useActSelForBackup, dointervalvi);
                    if (doPolGActSel)
                        ((ConfigeLGreedy) configeLGreedy).doGreedyPolActSel();
                    Configuration configeLGreedyRandom = new ConfigeLGreedyRandom(timeBound, useSASH, useActSelForBackup, dointervalvi);
                    if (doPolGActSel)
                        ((ConfigeLGreedyRandom) configeLGreedyRandom).doGreedyPolActSel();

                    //yay
                    Configuration configeLUGreedy = new ConfigeLUGreedy(timeBound, useSASH, useActSelForBackup, dointervalvi);
                    if (doPolGActSel)
                        ((ConfigeLUGreedy) configeLUGreedy).doGreedyPolActSel();
                    //yay
                    Configuration configeLUGreedyRandom = new ConfigeLUGreedyRandom(timeBound, useSASH, useActSelForBackup, dointervalvi);
                    if (doPolGActSel)
                        ((ConfigeLUGreedyRandom) configeLUGreedyRandom).doGreedyPolActSel();
                    Configuration configeLGreedyFCDE = new ConfigeLGreedyFiniteCostNoMaxCostDE(timeBound, useSASH, useActSelForBackup, dointervalvi, doPolGActSel);
                    Configuration configeLGreedyRandomFCDE = new ConfigeLGreedyRandomFiniteCostNoMaxCostDE(timeBound, useSASH, useActSelForBackup, dointervalvi, doPolGActSel);
                    Configuration configeLUGreedyFCDE = new ConfigeLUGreedyFiniteCostNoMaxCostDE(timeBound, useSASH, useActSelForBackup, dointervalvi, doPolGActSel);
                    Configuration configeLUGreedyRandomFCDE = new ConfigeLUGreedyRandomFiniteCostNoMaxCostDE(timeBound, useSASH, useActSelForBackup, dointervalvi, doPolGActSel);
                    Configuration aur = new ConfigeLGreedyRandomRelFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi);
                    if (doPolGActSel)
                        ((ConfigeLGreedyRandomRelFiniteCost) aur).doGreedyPolActSel();
                    Configuration auraur = new ConfigeLGreedyRelFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi);
                    if (doPolGActSel)
                        ((ConfigeLGreedyRelFiniteCost) auraur).doGreedyPolActSel();
                    Configuration aik = new ConfigeLUGreedyRelFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi);
                    if (doPolGActSel)
                        ((ConfigeLUGreedyRelFiniteCost) aik).doGreedyPolActSel();
                    Configuration aikaik = new ConfigeLUGreedyRandomRelFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi);
                    if (doPolGActSel)
                        ((ConfigeLUGreedyRandomRelFiniteCost) aikaik).doGreedyPolActSel();

                    //yay
                    Configuration justcost = new ConfigeLUGreedyRandomRelFiniteCostJustCost(timeBound, useSASH, useActSelForBackup, dointervalvi);
                    if (doPolGActSel)
                        ((ConfigeLUGreedyRandomRelFiniteCostJustCost) justcost).doGreedyPolActSel();
                    configs.add(configeLGreedy);
                    configs.add(configeLGreedyFC);
                    configs.add(configeLGreedyRandomFC);
                    configs.add(configeLUGreedyFC);
                    configs.add(configeLUGreedyRandomFC);

                    configs.add(configeLGreedyRandom);
                    configs.add(configeLUGreedy);
                    configs.add(configeLUGreedyRandom);
                    configs.add(configeLGreedyFCDE);
                    configs.add(configeLGreedyRandomFCDE);
                    configs.add(configeLUGreedyFCDE);
                    configs.add(configeLUGreedyRandomFCDE);
                    configs.add(aur);
                    configs.add(auraur);
                    configs.add(aik);
                    configs.add(aikaik);
                    configs.add(justcost);

                    for (Configuration config : configs) {
                        RunConfiguration runconfig = new RunConfiguration();
                        try {
                            runconfig.run(resFolderExt, config,
                                    2, 2, filename, hasSharedState, false, resSuffix, maxruns);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public static void runSmallExampleUCT() {
        String resFolderExt = "tro_examples/";
        String filename = "tro_example_new_small";
        boolean[] boolvals = new boolean[]{true, false};
        boolean hasSharedState = true;
        boolean timeBound = false;
        boolean domaxcostdeadends = false;
        boolean dointervalvi = false;
        int maxRuns = 50;
        String resSuffix = "_uct" + maxRuns + "_";
        for (boolean doPolGActSel : boolvals) {
            for (boolean useActSelForBackup : boolvals) {
                for (boolean useSASH : boolvals) {
                    ArrayList<Configuration> configs = new ArrayList<>();

                    Configuration configUCTFC = new ConfigUCTLFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi, doPolGActSel);


                    Configuration configUCTLUFC = new ConfigUCTLUFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi, doPolGActSel);

                    Configuration configUCTL = new ConfigUCTL(timeBound, useSASH, useActSelForBackup, dointervalvi);
                    if (doPolGActSel)
                        ((ConfigUCTL) configUCTL).doGreedyPolActSel();

                    //yay
                    Configuration configUCTLU = new ConfigUCTLU(timeBound, useSASH, useActSelForBackup, dointervalvi);
                    if (doPolGActSel)
                        ((ConfigUCTLU) configUCTLU).doGreedyPolActSel();
                    //yay

                    Configuration configUCTLFCDE = new ConfigUCTLFiniteCostNoMaxCostDE(timeBound, useSASH, useActSelForBackup, dointervalvi, doPolGActSel);

                    Configuration configUCTLUFCDE = new ConfigUCTLUFiniteCostNoMaxCostDE(timeBound, useSASH, useActSelForBackup, dointervalvi, doPolGActSel);


                    Configuration auraur = new ConfigUCTLRelFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi);
                    if (doPolGActSel)
                        ((ConfigUCTLRelFiniteCost) auraur).doGreedyPolActSel();
                    Configuration aik = new ConfigUCTLURelFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi);
                    if (doPolGActSel)
                        ((ConfigUCTLURelFiniteCost) aik).doGreedyPolActSel();


                    //yay
                    Configuration justcost = new ConfigUCTRelFiniteCostJustCost(timeBound, useSASH, useActSelForBackup, dointervalvi);
                    if (doPolGActSel)
                        ((ConfigUCTRelFiniteCostJustCost) justcost).doGreedyPolActSel();
                    configs.add(configUCTL);

                    configs.add(configUCTLUFC);


                    configs.add(configUCTLU);

                    configs.add(configUCTLFCDE);

                    configs.add(configUCTLUFCDE);


                    configs.add(auraur);
                    configs.add(aik);

                    configs.add(justcost);

                    for (Configuration config : configs) {
                        RunConfiguration runconfig = new RunConfiguration();
                        try {
                            runconfig.run(resFolderExt, config,
                                    2, 2, filename, hasSharedState, false, resSuffix, maxRuns);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }


    public static void testGridExample() {
        int fsp = 0;
        for (fsp = 30; fsp < 110; fsp += 30) {
            String[] examples = {"r10_g10_a1_grid_5_fsp_0_0_", "r10_g10_a1_grid_5_fsp_10_1_",
                    "r10_g10_a1_grid_5_fsp_20_2_", "r10_g10_a1_grid_5_fsp_30_3_", "r10_g10_a1_grid_5_fsp_40_4_",
                    "r10_g10_a1_grid_5_fsp_50_5_", "r10_g10_a1_grid_5_fsp_60_6_", "r10_g10_a1_grid_5_fsp_70_7_",
                    "r10_g10_a1_grid_5_fsp_80_8_", "r10_g10_a1_grid_5_fsp_90_9_", "r10_g10_a1_grid_5_fsp_100_0_"};

            String filename = examples[fsp / 10];

            String propsuffix = "mult";
            String resFolderExt = "grid5/" + fsp + "/";
            boolean[] boolvals = new boolean[]{true, false};
            int numRobots = 3;
            int numGoals = 3;
            int maxRuns = 3;
            boolean debug = false;
            boolean hasSharedState = false;
            boolean timeBound = true;
            boolean dointervalvi = false;
            long timeLimit = 30 * 60 * 1000;

            String resSuffix = "_gridruns"+maxRuns+"_";

            ArrayList<Configuration> configs = getSelectedConfigs(timeBound, dointervalvi, timeLimit);


            for (int i = 0; i < configs.size(); i++) {
                Configuration config = configs.get(i);
                System.out.println("\nRunning configuration " + config.getConfigname() + " - " + i + "/" + configs.size() + "\n");
                RunConfiguration runconfig = new RunConfiguration();
                try {
                    runconfig.run(resFolderExt, config,
                            numRobots, numGoals, filename, hasSharedState, debug, resSuffix, propsuffix, maxRuns, fsp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        }

    }

    public static void main(String[] args) {

	//        testGridExample();
        //       runSmallExample();
         runSmallExampleSelConfigs();
        //       runSmallExampleUCT();
    }


}
