package thts.testing;


import thts.treesearch.configs.Configuration;
import thts.treesearch.configs.RunConfiguration;
import thts.treesearch.configs.labelled.egreedy.elgreedy.*;
import thts.treesearch.configs.labelled.egreedy.elugreedy.*;
import thts.treesearch.configs.labelled.greedy.lgreedy.ConfigLlGreedy;
import thts.treesearch.configs.labelled.greedy.lgreedy.ConfigLlGreedyRandom;
import thts.treesearch.configs.labelled.greedy.lgreedy.ConfigLlGreedyRandomRelFiniteCost;
import thts.treesearch.configs.labelled.greedy.lgreedy.ConfigLlGreedyRelFiniteCost;
import thts.treesearch.configs.labelled.greedy.lugreedy.*;
import thts.treesearch.configs.labelled.uct.ConfigLUCTRelFiniteCostJustCost;
import thts.treesearch.configs.labelled.uct.uctl.ConfigLUCTL;
import thts.treesearch.configs.labelled.uct.uctl.ConfigLUCTLFiniteCost;
import thts.treesearch.configs.labelled.uct.uctl.ConfigLUCTLFiniteCostNoMaxCostDE;
import thts.treesearch.configs.labelled.uct.uctl.ConfigLUCTLRelFiniteCost;
import thts.treesearch.configs.labelled.uct.uctlu.ConfigLUCTLU;
import thts.treesearch.configs.labelled.uct.uctlu.ConfigLUCTLUFiniteCost;
import thts.treesearch.configs.labelled.uct.uctlu.ConfigLUCTLUFiniteCostNoMaxCostDE;
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

//PRISM_MAINCLASS=thts.testing.RunAllConfigsOnSmallExample prism/bin/prism
public class RunAllConfigsOnSmallExample {
    public static ArrayList<Configuration> getAllLabelledConfigs() {
        //generating all possible configurations
        boolean[] boolvals = new boolean[]{true, false};
        boolean timeBound = false;
        boolean dointervalvi = false;
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

        return allconfigs;
    }

    public static ArrayList<Configuration> getAllPlainConfigs() {
        //generating all possible configurations
        boolean[] boolvals = new boolean[]{true, false};
        boolean timeBound = false;
        boolean dointervalvi = false;
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

        return allconfigs;
    }

    public static ArrayList<Configuration> getAllConfigs() {
        ArrayList<Configuration> allconfigs = getAllPlainConfigs();
        allconfigs.addAll(getAllLabelledConfigs());
        return allconfigs;
    }

    public static ArrayList<Configuration> getSelectedConfigsFromFiltered(ArrayList<Configuration> allconfigs)
    {
        String[] configNames = {"L_eLUGreedyRandomRelFC_GP",
                "L_eLUGreedyRandomFC_MCD_GP_SASH",
                "L_eLUGreedyRandomFC_GAllActions_SASH",
                "L_eLUGreedyRandomFC_GP",
                "L_eLUGreedyFC_GAllActions_SASH"
                ,"L_eLUGreedyFC_MCD_GAllActions_SASH"
                ,"L_eLUGreedyRandomRelFC_MCD_GAllActions_SASH"
                ,"L_eLUGreedyRandomRelFC_MCD_GP"
                ,"L_eLUGreedyRandomFC_MCD_GAllActions_SASH"
                ,"L_eLUGreedyFC_MCD_GP_SASH"
                ,"L_eLUGreedyRandomRelFC_MCD_GP_SASH"
                ,"L_eLUGreedyRandomRelFC_GAllActions"
                ,"L_eLUGreedyRandomRelFC_MCD_GAllActions"
                ,"L_Cost_eLUGreedyRandomRelFC_GP_ASBU"
                ,"L_Cost_eLUGreedyRandomRelFC_MCD_GP_ASBU"
                ,"L_eLUGreedyFC_MCD_GAllActions"
                ,"L_eLUGreedyRandomRelFC_GAllActions_SASH"
                ,"L_eLUGreedyRandomFC_MCD_GP"
                ,"L_eLUGreedyFC_MCD_GP"
                ,"L_Cost_eLUGreedyRandomRelFC_GP_SASH_ASBU"
                ,"L_eLUGreedyRandomFC_MCD_GAllActions"
                ,"L_eLUGreedyRandomRelFC_GP_SASH"
                ,"L_eLUGreedyRandomFC_GP_SASH"
                ,"L_eLUGreedyFC_GP_SASH"
                ,"L_eLUGreedyFC_GAllActions"
                ,"L_eLUGreedyRandomFC_GAllActions"
                ,"L_Cost_LUGreedyRandomRelFC_GP_ASBU"
                ,"L_Cost_LUGreedyRandomRelFC_GP_SASH_ASBU"
                ,"L_Cost_LUGreedyRandomRelFC_MCD_GP_SASH_ASBU"
                ,"L_Cost_LUGreedyRandomRelFC_MCD_GP_ASBU"
                ,"L_Cost_eLUGreedyRandomRelFC_MCD_GP_SASH_ASBU"};

        ArrayList<String> selectedConfigNames = new ArrayList<>();
        for(String cname:configNames)
            selectedConfigNames.add(cname);
        ArrayList<Configuration> filteredConfigs = new ArrayList<>();
        for(Configuration config: allconfigs)
        {
            if(selectedConfigNames.contains(config.getConfigname()))
            {
                filteredConfigs.add(config);
            }
        }
        return filteredConfigs;

    }
    public static void runSmallExample() {
        String resFolderExt = "tro_examples/";
        String filename = "tro_example_new_small";
        boolean[] boolvals = new boolean[]{true, false};
        boolean hasSharedState = true;
        boolean timeBound = false;
        boolean domaxcostdeadends = false;
        boolean dointervalvi = false;

        int maxruns = 10;
        String resSuffix = "gocrazy_" + maxruns;

        ArrayList<Configuration> configs = getAllConfigs();
        int maxTests = configs.size()*maxruns;
        int testsSofar = 0;
        System.out.println(String.format("Running %d configurations with %d tests each for a total of %d tests",configs.size(),maxruns,maxTests));

        for (int i = 0; i<configs.size(); i++) {
            Configuration config = configs.get(i);
            RunConfiguration runconfig = new RunConfiguration();
            try {
                System.out.println("\n\nRunning configuration " + config.getConfigname() + " - " + i + "/" + configs.size() + "\n");
                runconfig.run(resFolderExt, config,
                        2, 2, filename, false, resSuffix, "_mult", maxruns, 0, 1);
                testsSofar += maxruns;
                System.out.println(String.format("Finished Configuration. %d / %d tests run.",testsSofar,maxTests));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static void runSmallExampleSelectedConfig() {
        String resFolderExt = "tro_examples/";
        String filename = "tro_example_new_small";
        boolean[] boolvals = new boolean[]{true, false};
        boolean hasSharedState = true;
        boolean timeBound = false;
        boolean domaxcostdeadends = false;
        boolean dointervalvi = false;

        int maxruns = 100;
        String resSuffix = "_moresel_" + maxruns;

        ArrayList<Configuration> configs = getSelectedConfigsFromFiltered(getAllConfigs());
        int maxTests = configs.size()*maxruns;
        int testsSofar = 0;
        System.out.println(String.format("Running %d configurations with %d tests each for a total of %d tests",configs.size(),maxruns,maxTests));

        for (int i = 0; i<configs.size(); i++) {
            Configuration config = configs.get(i);
            RunConfiguration runconfig = new RunConfiguration();
            try {
                System.out.println("\n\nRunning configuration " + config.getConfigname() + " - " + i + "/" + configs.size() + "\n");
                runconfig.run(resFolderExt, config,
                        2, 2, filename, false, resSuffix, "_mult", maxruns, 0, 1);
                testsSofar += maxruns;
                System.out.println(String.format("\nFinished Configuration. %d / %d tests run, %d to go",testsSofar,maxTests,maxTests-testsSofar));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    public static void main(String[] args) {

       // runSmallExample();
        runSmallExampleSelectedConfig();
    }


}
