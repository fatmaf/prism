package thts.testing;


import thts.treesearch.configs.Configuration;
import thts.treesearch.configs.RunConfiguration;
import thts.treesearch.configs.labelled.egreedy.elgreedy.*;
import thts.treesearch.configs.labelled.egreedy.elugreedy.*;
import thts.treesearch.configs.labelled.uct.ConfigLUCTRelFiniteCostJustCost;
import thts.treesearch.configs.labelled.uct.uctl.ConfigLUCTL;
import thts.treesearch.configs.labelled.uct.uctl.ConfigLUCTLFiniteCost;
import thts.treesearch.configs.labelled.uct.uctl.ConfigLUCTLFiniteCostNoMaxCostDE;
import thts.treesearch.configs.labelled.uct.uctl.ConfigLUCTLRelFiniteCost;
import thts.treesearch.configs.labelled.uct.uctlu.*;

import java.util.ArrayList;

//PRISM_MAINCLASS=thts.testing.RunInitialTestsLabelled prism/bin/prism
public class RunInitialTestsLabelled {

    public static ArrayList<Configuration> getSelectedConfigs(boolean timeBound, boolean dointervalvi, long timeLimit) {
        ArrayList<Configuration> configs = new ArrayList<>();
        //1        Cost_UCTRelFC_MCD_GP	FALSE	FALSE
        Configuration costUCTRelFC_MCD_GP = new ConfigLUCTRelFiniteCostJustCost(timeBound, false, false, dointervalvi);
        ((ConfigLUCTRelFiniteCostJustCost) costUCTRelFC_MCD_GP).doGreedyPolActSel();
        configs.add(costUCTRelFC_MCD_GP);
        //2        Cost_UCTRelFC_MCD_ASBU_GP	FALSE	TRUE
        Configuration costUCTRelFC_MCD_ASBU_GP = new ConfigLUCTRelFiniteCostJustCost(timeBound, false, true, dointervalvi);
        ((ConfigLUCTRelFiniteCostJustCost) costUCTRelFC_MCD_ASBU_GP).doGreedyPolActSel();
        configs.add(costUCTRelFC_MCD_ASBU_GP);
        //3        Cost_UCTRelFC_MCD_SASH_GP	TRUE	FALSE
        Configuration costUCTRelFC_MCD_SASH_GP = new ConfigLUCTRelFiniteCostJustCost(timeBound, true, false, dointervalvi);
        ((ConfigLUCTRelFiniteCostJustCost) costUCTRelFC_MCD_SASH_GP).doGreedyPolActSel();
        configs.add(costUCTRelFC_MCD_SASH_GP);
        //4        Cost_UCTRelFC_MCD_SASH_ASBU_GP	TRUE	TRUE
        Configuration costUCTRelFC_MCD_SASH_ASBU_GP = new ConfigLUCTRelFiniteCostJustCost(timeBound, true, true, dointervalvi);
        ((ConfigLUCTRelFiniteCostJustCost) costUCTRelFC_MCD_SASH_ASBU_GP).doGreedyPolActSel();
        configs.add(costUCTRelFC_MCD_SASH_ASBU_GP);
        //5        UCTLU_GAllActions_ASBU	FALSE	TRUE
        Configuration uctlu_GAllActions_ASBU = new ConfigLUCTLU(timeBound, false, true, dointervalvi);
        configs.add(uctlu_GAllActions_ASBU);
        //6        eLUGreedyRandomRelFC_MCD_GAllActions	FALSE	FALSE
        Configuration eluGreedyRandomRelFC_MCD_GAllActions = new ConfigLeLUGreedyRandomRelFiniteCost(timeBound, false, false, dointervalvi);
        configs.add(eluGreedyRandomRelFC_MCD_GAllActions);
        //7        eLUGreedyRandomFC_GAllActions	FALSE	FALSE

        Configuration eluGreedyRandomFC_GAllActions = new ConfigLeLUGreedyRandom(timeBound, false, false, dointervalvi, true, false);
        configs.add(eluGreedyRandomFC_GAllActions);
        //  8      eLUGreedyRandomFC_MCD_GAllActions_SASH	TRUE	FALSE
        Configuration eLUGreedyRandomFC_MCD_GAllActions_SASH = new ConfigLeLUGreedyRandom(timeBound, true, false, dointervalvi, true, true);
        configs.add(eLUGreedyRandomFC_MCD_GAllActions_SASH);
        //  9      Cost_eLUGreedyRandomRelFC_MCD_SASH_GP	TRUE	FALSE
        Configuration cost_eLUGreedyRandomRelFC_MCD_SASH_GP = new ConfigLeLUGreedyRandomRelFiniteCostJustCost(timeBound, true, false, dointervalvi);
        ((ConfigLeLUGreedyRandomRelFiniteCostJustCost) cost_eLUGreedyRandomRelFC_MCD_SASH_GP).doGreedyPolActSel();
        configs.add(cost_eLUGreedyRandomRelFC_MCD_SASH_GP);
        //  10     Cost_eLUGreedyRandomRelFC_MCD_ASBU_GP	FALSE	TRUE
        Configuration cost_eLUGreedyRandomRelFC_MCD_ASBU_GP = new ConfigLeLUGreedyRandomRelFiniteCostJustCost(timeBound, false, true, dointervalvi);
        ((ConfigLeLUGreedyRandomRelFiniteCostJustCost) cost_eLUGreedyRandomRelFC_MCD_ASBU_GP).doGreedyPolActSel();
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
//               if(!config.getConfigname().contentEquals("Cost_UCTRelFC_MCD_ASBU_GP"))
//                    continue;
//                System.out.println(config.getConfigname());
                System.out.println("\n\nRunning configuration " + config.getConfigname() + " - " + i + "/" + configs.size() + "\n");
                runconfig.run(resFolderExt, config,
                        2, 3, filename, debug, resSuffix, "_mult", maxRuns, 0, 1);

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
        int maxruns = 100;
        for (boolean doPolGActSel : boolvals) {
            for (boolean useActSelForBackup : boolvals) {
                for (boolean useSASH : boolvals) {
                    ArrayList<Configuration> configs = new ArrayList<>();

                    Configuration configeLGreedyFC = new ConfigLeLGreedyFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi, doPolGActSel);


                    Configuration configeLGreedyRandomFC = new ConfigLeLGreedyRandomFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi, doPolGActSel);


                    Configuration configeLUGreedyFC = new ConfigLeLUGreedyFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi, doPolGActSel);
                    Configuration configeLUGreedyRandomFC = new ConfigLeLUGreedyRandomFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi, doPolGActSel);
                    Configuration configeLGreedy = new ConfigLeLGreedy(timeBound, useSASH, useActSelForBackup, dointervalvi);
                    if (doPolGActSel)
                        ((ConfigLeLGreedy) configeLGreedy).doGreedyPolActSel();
                    Configuration configeLGreedyRandom = new ConfigLeLGreedyRandom(timeBound, useSASH, useActSelForBackup, dointervalvi);
                    if (doPolGActSel)
                        ((ConfigLeLGreedyRandom) configeLGreedyRandom).doGreedyPolActSel();

                    //yay
                    Configuration configeLUGreedy = new ConfigLeLUGreedy(timeBound, useSASH, useActSelForBackup, dointervalvi);
                    if (doPolGActSel)
                        ((ConfigLeLUGreedy) configeLUGreedy).doGreedyPolActSel();
                    //yay
                    Configuration configeLUGreedyRandom = new ConfigLeLUGreedyRandom(timeBound, useSASH, useActSelForBackup, dointervalvi);
                    if (doPolGActSel)
                        ((ConfigLeLUGreedyRandom) configeLUGreedyRandom).doGreedyPolActSel();
                    Configuration configeLGreedyFCDE = new ConfigLeLGreedyFiniteCostNoMaxCostDE(timeBound, useSASH, useActSelForBackup, dointervalvi, doPolGActSel);
                    Configuration configeLGreedyRandomFCDE = new ConfigLeLGreedyRandomFiniteCostNoMaxCostDE(timeBound, useSASH, useActSelForBackup, dointervalvi, doPolGActSel);
                    Configuration configeLUGreedyFCDE = new ConfigLeLUGreedyFiniteCostNoMaxCostDE(timeBound, useSASH, useActSelForBackup, dointervalvi, doPolGActSel);
                    Configuration configeLUGreedyRandomFCDE = new ConfigLeLUGreedyRandomFiniteCostNoMaxCostDE(timeBound, useSASH, useActSelForBackup, dointervalvi, doPolGActSel);
                    Configuration aur = new ConfigLeLGreedyRandomRelFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi);
                    if (doPolGActSel)
                        ((ConfigLeLGreedyRandomRelFiniteCost) aur).doGreedyPolActSel();
                    Configuration auraur = new ConfigLeLGreedyRelFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi);
                    if (doPolGActSel)
                        ((ConfigLeLGreedyRelFiniteCost) auraur).doGreedyPolActSel();
                    Configuration aik = new ConfigLeLUGreedyRelFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi);
                    if (doPolGActSel)
                        ((ConfigLeLUGreedyRelFiniteCost) aik).doGreedyPolActSel();
                    Configuration aikaik = new ConfigLeLUGreedyRandomRelFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi);
                    if (doPolGActSel)
                        ((ConfigLeLUGreedyRandomRelFiniteCost) aikaik).doGreedyPolActSel();

                    //yay
                    Configuration justcost = new ConfigLeLUGreedyRandomRelFiniteCostJustCost(timeBound, useSASH, useActSelForBackup, dointervalvi);
                    if (doPolGActSel)
                        ((ConfigLeLUGreedyRandomRelFiniteCostJustCost) justcost).doGreedyPolActSel();
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
                                    2, 2, filename, false, resSuffix, "_mult", maxruns, 0, 1);
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

                    Configuration configUCTFC = new ConfigLUCTLFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi, doPolGActSel);


                    Configuration configUCTLUFC = new ConfigLUCTLUFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi, doPolGActSel);

                    Configuration configUCTL = new ConfigLUCTL(timeBound, useSASH, useActSelForBackup, dointervalvi);
                    if (doPolGActSel)
                        ((ConfigLUCTL) configUCTL).doGreedyPolActSel();

                    //yay
                    Configuration configUCTLU = new ConfigLUCTLU(timeBound, useSASH, useActSelForBackup, dointervalvi);
                    if (doPolGActSel)
                        ((ConfigLUCTLU) configUCTLU).doGreedyPolActSel();
                    //yay

                    Configuration configUCTLFCDE = new ConfigLUCTLFiniteCostNoMaxCostDE(timeBound, useSASH, useActSelForBackup, dointervalvi, doPolGActSel);

                    Configuration configUCTLUFCDE = new ConfigLUCTLUFiniteCostNoMaxCostDE(timeBound, useSASH, useActSelForBackup, dointervalvi, doPolGActSel);


                    Configuration auraur = new ConfigLUCTLRelFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi);
                    if (doPolGActSel)
                        ((ConfigLUCTLRelFiniteCost) auraur).doGreedyPolActSel();
                    Configuration aik = new ConfigLUCTLURelFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi);
                    if (doPolGActSel)
                        ((ConfigLUCTLURelFiniteCost) aik).doGreedyPolActSel();


                    //yay
                    Configuration justcost = new ConfigLUCTRelFiniteCostJustCost(timeBound, useSASH, useActSelForBackup, dointervalvi);
                    if (doPolGActSel)
                        ((ConfigLUCTRelFiniteCostJustCost) justcost).doGreedyPolActSel();
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
                                    2, 2, filename, false, resSuffix, "_mult", maxRuns, 0, 1);
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
        int fsps[] = {30, 60, 90};
        boolean[] boolvals = new boolean[]{true, false};
        int numRobots = 3;
        int numGoals = 3;
        int maxRuns = 5;
        boolean debug = false;
        boolean hasSharedState = false;
        boolean timeBound = true;
        boolean dointervalvi = false;
        long timeLimit = 30 * 60 * 1000;
        String propsuffix = "mult";
        int maxTests = maxRuns * fsps.length * 10;
        int numTets = 0;
        ArrayList<Configuration> configs = getSelectedConfigs(timeBound, dointervalvi, timeLimit);

        for (int fspNum = 0; fspNum < fsps.length; fspNum++) {
            fsp = fsps[fspNum];
            String[] examples = {"r10_g10_a1_grid_5_fsp_0_0_", "r10_g10_a1_grid_5_fsp_10_1_",
                    "r10_g10_a1_grid_5_fsp_20_2_", "r10_g10_a1_grid_5_fsp_30_3_", "r10_g10_a1_grid_5_fsp_40_4_",
                    "r10_g10_a1_grid_5_fsp_50_5_", "r10_g10_a1_grid_5_fsp_60_6_", "r10_g10_a1_grid_5_fsp_70_7_",
                    "r10_g10_a1_grid_5_fsp_80_8_", "r10_g10_a1_grid_5_fsp_90_9_", "r10_g10_a1_grid_5_fsp_100_0_"};

            String filename = examples[fsp / 10];

            String resFolderExt = "grid5/" + fsp + "/";


            String resSuffix = "_reruns" + maxRuns + "_";

            System.out.println(String.format("\nRunning Tests on FSP %3d (%2d/%2d)", fsp, fspNum, fsps.length));
            System.out.println(String.format("\t %5d/%5d of total", numTets, maxTests));
            for (int i = 0; i < configs.size(); i++) {
                Configuration config = configs.get(i);
                System.out.println("\n\nRunning configuration " + config.getConfigname() + " - " + i + "/" + configs.size() + "\n");
                System.out.println(String.format("\t %5d/%5d of total", numTets, maxTests));
                RunConfiguration runconfig = new RunConfiguration();
                config.setJustLogs(true);
                try {

                    runconfig.run(resFolderExt, config,
                            numRobots, numGoals, filename, debug, resSuffix, propsuffix, maxRuns, fsp, 0);
                    numTets += maxRuns;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        }

    }

    public static void main(String[] args) {


        testGridExample();
        //       runSmallExample();
        // runSmallExampleSelConfigs();


        //       runSmallExampleUCT();
    }


}
