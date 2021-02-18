package thts.testing;


import thts.treesearch.configs.Configuration;
import thts.treesearch.configs.RunConfiguration;
import thts.treesearch.configs.labelled.egreedy.elgreedy.ConfigLeLGreedy;
import thts.treesearch.configs.labelled.egreedy.elgreedy.ConfigLeLGreedyRandom;
import thts.treesearch.configs.labelled.egreedy.elgreedy.ConfigLeLGreedyRandomRelFiniteCost;
import thts.treesearch.configs.labelled.egreedy.elgreedy.ConfigLeLGreedyRelFiniteCost;
import thts.treesearch.configs.labelled.egreedy.elugreedy.*;
import thts.treesearch.configs.labelled.greedy.lgreedy.ConfigLlGreedy;
import thts.treesearch.configs.labelled.greedy.lgreedy.ConfigLlGreedyRandom;
import thts.treesearch.configs.labelled.greedy.lgreedy.ConfigLlGreedyRandomRelFiniteCost;
import thts.treesearch.configs.labelled.greedy.lgreedy.ConfigLlGreedyRelFiniteCost;
import thts.treesearch.configs.labelled.greedy.lugreedy.*;
import thts.treesearch.configs.labelled.uct.ConfigLUCTRelFiniteCostJustCost;
import thts.treesearch.configs.labelled.uct.uctl.ConfigLUCTL;
import thts.treesearch.configs.labelled.uct.uctl.ConfigLUCTLRelFiniteCost;
import thts.treesearch.configs.labelled.uct.uctlu.ConfigLUCTLU;
import thts.treesearch.configs.labelled.uct.uctlu.ConfigLUCTLURelFiniteCost;
import thts.treesearch.configs.plain.egreedy.elgreedy.ConfigeLGreedy;
import thts.treesearch.configs.plain.egreedy.elgreedy.ConfigeLGreedyRandom;
import thts.treesearch.configs.plain.egreedy.elgreedy.ConfigeLGreedyRandomRelFiniteCost;
import thts.treesearch.configs.plain.egreedy.elgreedy.ConfigeLGreedyRelFiniteCost;
import thts.treesearch.configs.plain.egreedy.elugreedy.*;
import thts.treesearch.configs.plain.greedy.lgreedy.ConfigLGreedy;
import thts.treesearch.configs.plain.greedy.lgreedy.ConfigLGreedyRandom;
import thts.treesearch.configs.plain.greedy.lgreedy.ConfigLGreedyRandomRelFiniteCost;
import thts.treesearch.configs.plain.greedy.lgreedy.ConfigLGreedyRelFiniteCost;
import thts.treesearch.configs.plain.greedy.lugreedy.*;
import thts.treesearch.configs.plain.uct.ConfigUCTRelFiniteCostJustCost;
import thts.treesearch.configs.plain.uct.uctl.ConfigUCTL;
import thts.treesearch.configs.plain.uct.uctl.ConfigUCTLRelFiniteCost;
import thts.treesearch.configs.plain.uct.uctlu.ConfigUCTLU;
import thts.treesearch.configs.plain.uct.uctlu.ConfigUCTLURelFiniteCost;

import java.util.ArrayList;

//PRISM_MAINCLASS=thts.testing.RunSelConfigsOnSmallGrid prism/bin/prism
public class RunSelConfigsOnSmallGrid {
    public static ArrayList<Configuration> getAllLabelledConfigs(boolean timeBound, long timeLimit)
    {
        return getAllLabelledConfigs(timeBound,timeLimit);
    }
    public static ArrayList<Configuration> getAllLabelledConfigs(boolean timeBound, long timeLimit,boolean dointervalvi) {
        //generating all possible configurations
        boolean[] boolvals = new boolean[]{true, false};

        ArrayList<Configuration> allconfigs = new ArrayList<>();
        for (boolean doPolGActSel : boolvals) {
            for (boolean useActSelForBackup : boolvals) {
                for (boolean useSASH : boolvals) {
                    for (boolean domaxcost : boolvals) {
                        for (boolean maxcostdeadends : boolvals) {
                            Configuration lelgreedy = new ConfigLeLGreedy(timeBound, useSASH, useActSelForBackup, dointervalvi, domaxcost, maxcostdeadends, doPolGActSel);
                            allconfigs.add(lelgreedy);
                            Configuration lelgreedyrandom = new ConfigLeLGreedyRandom(timeBound, useSASH, useActSelForBackup, dointervalvi, domaxcost, maxcostdeadends, doPolGActSel);
                            allconfigs.add(lelgreedyrandom);
                            Configuration leLGreedyRandomRelFiniteCost = new ConfigLeLGreedyRandomRelFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi, maxcostdeadends, doPolGActSel);
                            allconfigs.add(leLGreedyRandomRelFiniteCost);
                            Configuration leLGreedyRelFiniteCost = new ConfigLeLGreedyRelFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi, maxcostdeadends, doPolGActSel);
                            allconfigs.add(leLGreedyRelFiniteCost);

                            Configuration leLUGreedy = new ConfigLeLUGreedy(timeBound, useSASH, useActSelForBackup, dointervalvi, domaxcost, maxcostdeadends, doPolGActSel);
                            allconfigs.add(leLUGreedy);
                            Configuration leLUGreedyrandom = new ConfigLeLUGreedyRandom(timeBound, useSASH, useActSelForBackup, dointervalvi, domaxcost, maxcostdeadends, doPolGActSel);
                            allconfigs.add(leLUGreedyrandom);
                            Configuration leLUGreedyRandomRelFiniteCost = new ConfigLeLUGreedyRandomRelFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi, maxcostdeadends, doPolGActSel);
                            allconfigs.add(leLUGreedyRandomRelFiniteCost);
                            Configuration leLUGreedyRelFiniteCost = new ConfigLeLUGreedyRelFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi, maxcostdeadends, doPolGActSel);
                            allconfigs.add(leLUGreedyRelFiniteCost);
                            Configuration leLUGreedyRandomRelFiniteCostJustCost = new ConfigLeLUGreedyRandomRelFiniteCostJustCost(timeBound, useSASH, useActSelForBackup, dointervalvi, maxcostdeadends, doPolGActSel);
                            allconfigs.add(leLUGreedyRandomRelFiniteCostJustCost);


                            Configuration LlGreedy = new ConfigLlGreedy(timeBound, useSASH, useActSelForBackup, dointervalvi, domaxcost, maxcostdeadends, doPolGActSel);
                            allconfigs.add(LlGreedy);
                            Configuration LlGreedyrandom = new ConfigLlGreedyRandom(timeBound, useSASH, useActSelForBackup, dointervalvi, domaxcost, maxcostdeadends, doPolGActSel);
                            allconfigs.add(LlGreedyrandom);
                            Configuration LlGreedyRandomRelFiniteCost = new ConfigLlGreedyRandomRelFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi, maxcostdeadends, doPolGActSel);
                            allconfigs.add(LlGreedyRandomRelFiniteCost);
                            Configuration LlGreedyRelFiniteCost = new ConfigLlGreedyRelFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi, maxcostdeadends, doPolGActSel);
                            allconfigs.add(LlGreedyRelFiniteCost);

                            Configuration LluGreedy = new ConfigLluGreedy(timeBound, useSASH, useActSelForBackup, dointervalvi, domaxcost, maxcostdeadends, doPolGActSel);
                            allconfigs.add(LluGreedy);
                            Configuration LluGreedyrandom = new ConfigLluGreedyRandom(timeBound, useSASH, useActSelForBackup, dointervalvi, domaxcost, maxcostdeadends, doPolGActSel);
                            allconfigs.add(LluGreedyrandom);
                            Configuration LluGreedyRandomRelFiniteCost = new ConfigLluGreedyRandomRelFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi, maxcostdeadends, doPolGActSel);
                            allconfigs.add(LluGreedyRandomRelFiniteCost);
                            Configuration LluGreedyRelFiniteCost = new ConfigLluGreedyRelFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi, maxcostdeadends, doPolGActSel);
                            allconfigs.add(LluGreedyRelFiniteCost);
                            Configuration LluGreedyRandomRelFiniteCostJustCost = new ConfigLluGreedyRandomRelFiniteCostJustCost(timeBound, useSASH, useActSelForBackup, dointervalvi, maxcostdeadends, doPolGActSel);
                            allconfigs.add(LluGreedyRandomRelFiniteCostJustCost);

                            Configuration luctl = new ConfigLUCTL(timeBound, useSASH, useActSelForBackup, dointervalvi, domaxcost, maxcostdeadends, doPolGActSel);
                            allconfigs.add(luctl);
                            Configuration luctlRelFiniteCost = new ConfigLUCTLRelFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi, maxcostdeadends, doPolGActSel);
                            allconfigs.add(luctlRelFiniteCost);

                            Configuration luctlu = new ConfigLUCTLU(timeBound, useSASH, useActSelForBackup, dointervalvi, domaxcost, maxcostdeadends, doPolGActSel);
                            allconfigs.add(luctlu);
                            Configuration luctluRelFiniteCost = new ConfigLUCTLURelFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi, maxcostdeadends, doPolGActSel);
                            allconfigs.add(luctlRelFiniteCost);

                            Configuration luctrelfinitecostjustcost = new ConfigLUCTRelFiniteCostJustCost(timeBound, useSASH, useActSelForBackup, dointervalvi, maxcostdeadends, doPolGActSel);
                            allconfigs.add(luctrelfinitecostjustcost);


                        }
                    }
                }


            }
        }
        for (Configuration c : allconfigs) {
            c.setTimeTimeLimitInMS(timeLimit);
        }
        return allconfigs;
    }
    public static ArrayList<Configuration> getAllPlainConfigs(boolean timeBound, long timeLimit)
    {
        return getAllPlainConfigs(timeBound,timeLimit);
    }
    public static ArrayList<Configuration> getAllPlainConfigs(boolean timeBound, long timeLimit,boolean dointervalvi) {
        //generating all possible configurations
        boolean[] boolvals = new boolean[]{true, false};

        ArrayList<Configuration> allconfigs = new ArrayList<>();
        for (boolean doPolGActSel : boolvals) {
            for (boolean useActSelForBackup : boolvals) {
                for (boolean useSASH : boolvals) {
                    for (boolean domaxcost : boolvals) {
                        for (boolean maxcostdeadends : boolvals) {
                            Configuration eLgreedy = new ConfigeLGreedy(timeBound, useSASH, useActSelForBackup, dointervalvi, domaxcost, maxcostdeadends, doPolGActSel);
                            allconfigs.add(eLgreedy);
                            Configuration eLgreedyrandom = new ConfigeLGreedyRandom(timeBound, useSASH, useActSelForBackup, dointervalvi, domaxcost, maxcostdeadends, doPolGActSel);
                            allconfigs.add(eLgreedyrandom);
                            Configuration eLGreedyRandomRelFiniteCost = new ConfigeLGreedyRandomRelFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi, maxcostdeadends, doPolGActSel);
                            allconfigs.add(eLGreedyRandomRelFiniteCost);
                            Configuration eLGreedyRelFiniteCost = new ConfigeLGreedyRelFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi, maxcostdeadends, doPolGActSel);
                            allconfigs.add(eLGreedyRelFiniteCost);

                            Configuration eLUGreedy = new ConfigeLUGreedy(timeBound, useSASH, useActSelForBackup, dointervalvi, domaxcost, maxcostdeadends, doPolGActSel);
                            allconfigs.add(eLUGreedy);
                            Configuration eLUGreedyrandom = new ConfigeLUGreedyRandom(timeBound, useSASH, useActSelForBackup, dointervalvi, domaxcost, maxcostdeadends, doPolGActSel);
                            allconfigs.add(eLUGreedyrandom);
                            Configuration eLUGreedyRandomRelFiniteCost = new ConfigeLUGreedyRandomRelFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi, maxcostdeadends, doPolGActSel);
                            allconfigs.add(eLUGreedyRandomRelFiniteCost);
                            Configuration eLUGreedyRelFiniteCost = new ConfigeLUGreedyRelFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi, maxcostdeadends, doPolGActSel);
                            allconfigs.add(eLUGreedyRelFiniteCost);
                            Configuration eLUGreedyRandomRelFiniteCostJustCost = new ConfigeLUGreedyRandomRelFiniteCostJustCost(timeBound, useSASH, useActSelForBackup, dointervalvi, maxcostdeadends, doPolGActSel);
                            allconfigs.add(eLUGreedyRandomRelFiniteCostJustCost);


                            Configuration LGreedy = new ConfigLGreedy(timeBound, useSASH, useActSelForBackup, dointervalvi, domaxcost, maxcostdeadends, doPolGActSel);
                            allconfigs.add(LGreedy);
                            Configuration LGreedyrandom = new ConfigLGreedyRandom(timeBound, useSASH, useActSelForBackup, dointervalvi, domaxcost, maxcostdeadends, doPolGActSel);
                            allconfigs.add(LGreedyrandom);
                            Configuration LGreedyRandomRelFiniteCost = new ConfigLGreedyRandomRelFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi, maxcostdeadends, doPolGActSel);
                            allconfigs.add(LGreedyRandomRelFiniteCost);
                            Configuration LGreedyRelFiniteCost = new ConfigLGreedyRelFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi, maxcostdeadends, doPolGActSel);
                            allconfigs.add(LGreedyRelFiniteCost);

                            Configuration LuGreedy = new ConfigLUGreedy(timeBound, useSASH, useActSelForBackup, dointervalvi, domaxcost, maxcostdeadends, doPolGActSel);
                            allconfigs.add(LuGreedy);
                            Configuration LuGreedyrandom = new ConfigLUGreedyRandom(timeBound, useSASH, useActSelForBackup, dointervalvi, domaxcost, maxcostdeadends, doPolGActSel);
                            allconfigs.add(LuGreedyrandom);
                            Configuration LuGreedyRandomRelFiniteCost = new ConfigLUGreedyRandomRelFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi, maxcostdeadends, doPolGActSel);
                            allconfigs.add(LuGreedyRandomRelFiniteCost);
                            Configuration LuGreedyRelFiniteCost = new ConfigLUGreedyRelFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi, maxcostdeadends, doPolGActSel);
                            allconfigs.add(LuGreedyRelFiniteCost);
                            Configuration LuGreedyRandomRelFiniteCostJustCost = new ConfigLUGreedyRandomRelFiniteCostJustCost(timeBound, useSASH, useActSelForBackup, dointervalvi, maxcostdeadends, doPolGActSel);
                            allconfigs.add(LuGreedyRandomRelFiniteCostJustCost);

                            Configuration UCTl = new ConfigUCTL(timeBound, useSASH, useActSelForBackup, dointervalvi, domaxcost, maxcostdeadends, doPolGActSel);
                            allconfigs.add(UCTl);
                            Configuration UCTlRelFiniteCost = new ConfigUCTLRelFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi, maxcostdeadends, doPolGActSel);
                            allconfigs.add(UCTlRelFiniteCost);

                            Configuration UCTlu = new ConfigUCTLU(timeBound, useSASH, useActSelForBackup, dointervalvi, domaxcost, maxcostdeadends, doPolGActSel);
                            allconfigs.add(UCTlu);
                            Configuration UCTluRelFiniteCost = new ConfigUCTLURelFiniteCost(timeBound, useSASH, useActSelForBackup, dointervalvi, maxcostdeadends, doPolGActSel);
                            allconfigs.add(UCTlRelFiniteCost);

                            Configuration UCTrelfinitecostjustcost = new ConfigUCTRelFiniteCostJustCost(timeBound, useSASH, useActSelForBackup, dointervalvi, maxcostdeadends, doPolGActSel);
                            allconfigs.add(UCTrelfinitecostjustcost);


                        }
                    }
                }


            }
        }

        for (Configuration c : allconfigs) {
            c.setTimeTimeLimitInMS(timeLimit);
        }

        return allconfigs;
    }

    public static ArrayList<Configuration> getAllConfigs(boolean timeBound, long timeLimit) {
        ArrayList<Configuration> allconfigs = getAllPlainConfigs(timeBound, timeLimit);
        allconfigs.addAll(getAllLabelledConfigs(timeBound, timeLimit));
        return allconfigs;
    }
    public static ArrayList<Configuration> getAllConfigs(boolean timeBound, long timeLimit,boolean dointervalvi) {
        ArrayList<Configuration> allconfigs = getAllPlainConfigs(timeBound, timeLimit,dointervalvi);
        allconfigs.addAll(getAllLabelledConfigs(timeBound, timeLimit,dointervalvi));
        return allconfigs;
    }
    public static ArrayList<Configuration> getSelectedConfigsFromFiltered(ArrayList<Configuration> allconfigs) {
        String[] configNames = {
                "L_Cost_LUGreedyRandomRelFC_GP_ASBU",
                "L_Cost_LUGreedyRandomRelFC_MCD_GP_ASBU",
                "L_Cost_eLUGreedyRandomRelFC_GP_ASBU",
                "L_Cost_eLUGreedyRandomRelFC_MCD_GP_ASBU",
                "L_Cost_LUGreedyRandomRelFC_GP_SASH_ASBU",
                "L_Cost_eLUGreedyRandomRelFC_GP_SASH_ASBU",
                "L_Cost_LUGreedyRandomRelFC_MCD_GP_SASH_ASBU",
                "L_Cost_eLUGreedyRandomRelFC_MCD_GP_SASH_ASBU",
                "L_eLUGreedyRandom_MCD_GAllActions_ASBU",
                "L_eLUGreedyRandom_GAllActions_ASBU",
                "L_eLUGreedy_GAllActions_ASBU",
                "L_eLUGreedy_MCD_GAllActions_ASBU",
                "L_UCTLU_GAllActions_ASBU",
                "L_UCTLU_MCD_GAllActions_ASBU",
                "P_eLUGreedy_GAllActions_ASBU",
                "P_LUGreedy_GAllActions_ASBU",
                "P_LUGreedyRandom_GAllActions_ASBU",
                "P_LUGreedy_MCD_GAllActions_ASBU",
                "P_eLUGreedyRandom_GAllActions_ASBU",
                "P_LUGreedyRandom_MCD_GAllActions_ASBU",
                "P_eLUGreedy_MCD_GAllActions_ASBU",
                "P_UCTLU_MCD_GAllActions_ASBU",
                "P_UCTLU_GAllActions_ASBU",
                "P_eLUGreedyRandom_MCD_GAllActions_ASBU",};

        ArrayList<String> selectedConfigNames = new ArrayList<>();
        for (String cname : configNames)
            selectedConfigNames.add(cname);
        ArrayList<Configuration> filteredConfigs = new ArrayList<>();
        ArrayList<String> filteredConfigNames = new ArrayList<>();

        for (Configuration config : allconfigs) {
            if (selectedConfigNames.contains(config.getConfigname())) {
                if (!filteredConfigNames.contains(config.getConfigname())) {
                    filteredConfigs.add(config);
                    filteredConfigNames.add(config.getConfigname());
                }
            }
        }
        return filteredConfigs;

    }

    public static void runGridExamples() {
        int fsp = 0;
        int fsps[] = {0,30, 60, 90};
        boolean[] boolvals = new boolean[]{true, false};
        int numRobots = 3;
        int numGoals = 3;
        int maxRuns = 10;
        boolean debug = false;

        boolean timeBound = true;
        boolean dointervalvi = false;
        long timeLimit = 30 * 60 * 1000;
        String propsuffix = "mult";

        ArrayList<Configuration> configs = getSelectedConfigsFromFiltered(getAllConfigs(timeBound, timeLimit));
        int maxTests = configs.size() * maxRuns * fsps.length;
        int testsSofar = 0;
        System.out.println(String.format("Running %d configurations with %d tests each for a total of %d tests", configs.size(), maxRuns, maxTests));
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
            System.out.println(String.format("\t %5d/%5d of total", testsSofar, maxTests));
            for (int i = 0; i < configs.size(); i++) {
                Configuration config = configs.get(i);
                config.setJustLogs(true);
                System.out.println("\n\nRunning configuration " + config.getConfigname() + " - " + i + "/" + configs.size() + "\n");
                System.out.println(String.format("\t %5d/%5d of total", testsSofar, maxTests));
                RunConfiguration runconfig = new RunConfiguration();

                try {

                    runconfig.run(resFolderExt, config,
                            numRobots, numGoals, filename, debug, resSuffix, propsuffix, maxRuns, fsp, 0);
                    testsSofar += maxRuns;
                    System.out.println(String.format("\nFinished Configuration. %d / %d tests run, %d to go", testsSofar, maxTests, maxTests - testsSofar));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        }
    }


    public static void main(String[] args) {

        runGridExamples();
    }


}
