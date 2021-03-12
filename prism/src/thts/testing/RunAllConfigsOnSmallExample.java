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
        ArrayList<Configuration> plainConfigs = getAllPlainConfigs();
        ArrayList<Configuration> labelledConfigs = getAllLabelledConfigs();
        ArrayList<String> configNames = new ArrayList<>();

        ArrayList<Configuration> allconfigs = new ArrayList<>();//getAllPlainConfigs();
        //allconfigs.addAll(getAllLabelledConfigs());
        for (int i = 0; i < plainConfigs.size(); i++) {
            if (!configNames.contains(plainConfigs.get(i).getConfigname())) {
                allconfigs.add(plainConfigs.get(i));
                configNames.add(plainConfigs.get(i).getConfigname());
            }
        }
        for (int i = 0; i < labelledConfigs.size(); i++) {
            if (!configNames.contains(labelledConfigs.get(i).getConfigname())) {
                allconfigs.add(labelledConfigs.get(i));
                configNames.add(labelledConfigs.get(i).getConfigname());
            }

        }
        return allconfigs;
    }

    public static ArrayList<Configuration> filterConfigs(String[] configNames, ArrayList<Configuration> allconfigs,boolean exclude) {


        ArrayList<String> selectedConfigNames = new ArrayList<>();
        for (String cname : configNames)
            selectedConfigNames.add(cname);
        ArrayList<Configuration> filteredConfigs = new ArrayList<>();
        for (Configuration config : allconfigs) {
	    if(exclude){
            if (!selectedConfigNames.contains(config.getConfigname())) {
                filteredConfigs.add(config);
            }
	    }
	    else
		{
		        if (selectedConfigNames.contains(config.getConfigname())) {
			    filteredConfigs.add(config);
			}
		}

        }
        return filteredConfigs;

    }

    public static ArrayList<Configuration> getSelectedConfigsFromFiltered(ArrayList<Configuration> allconfigs) {
        String[] configNames = {"L_eLUGreedyRandomRelFC_GP",
                "L_eLUGreedyRandomFC_MCD_GP_SASH",
                "L_eLUGreedyRandomFC_GAllActions_SASH",
                "L_eLUGreedyRandomFC_GP",
                "L_eLUGreedyFC_GAllActions_SASH"
                , "L_eLUGreedyFC_MCD_GAllActions_SASH"
                , "L_eLUGreedyRandomRelFC_MCD_GAllActions_SASH"
                , "L_eLUGreedyRandomRelFC_MCD_GP"
                , "L_eLUGreedyRandomFC_MCD_GAllActions_SASH"
                , "L_eLUGreedyFC_MCD_GP_SASH"
                , "L_eLUGreedyRandomRelFC_MCD_GP_SASH"
                , "L_eLUGreedyRandomRelFC_GAllActions"
                , "L_eLUGreedyRandomRelFC_MCD_GAllActions"
                , "L_Cost_eLUGreedyRandomRelFC_GP_ASBU"
                , "L_Cost_eLUGreedyRandomRelFC_MCD_GP_ASBU"
                , "L_eLUGreedyFC_MCD_GAllActions"
                , "L_eLUGreedyRandomRelFC_GAllActions_SASH"
                , "L_eLUGreedyRandomFC_MCD_GP"
                , "L_eLUGreedyFC_MCD_GP"
                , "L_Cost_eLUGreedyRandomRelFC_GP_SASH_ASBU"
                , "L_eLUGreedyRandomFC_MCD_GAllActions"
                , "L_eLUGreedyRandomRelFC_GP_SASH"
                , "L_eLUGreedyRandomFC_GP_SASH"
                , "L_eLUGreedyFC_GP_SASH"
                , "L_eLUGreedyFC_GAllActions"
                , "L_eLUGreedyRandomFC_GAllActions"
                , "L_Cost_LUGreedyRandomRelFC_GP_ASBU"
                , "L_Cost_LUGreedyRandomRelFC_GP_SASH_ASBU"
                , "L_Cost_LUGreedyRandomRelFC_MCD_GP_SASH_ASBU"
                , "L_Cost_LUGreedyRandomRelFC_MCD_GP_ASBU"
                , "L_Cost_eLUGreedyRandomRelFC_MCD_GP_SASH_ASBU"};

        return filterConfigs(configNames, allconfigs,false);

    }

    public static ArrayList<Configuration> getridofdoneconfigs(ArrayList<Configuration> allconfigs) {
        String[] configNames = {"P_LUGreedyRelFC_GP",
                "P_UCTLU_GP_SASH",
                "P_eLUGreedyRandom_MCD_GP_SASH_ASBU",
                "P_LGreedy_MCD_GAllActions_ASBU"
                , "P_eLUGreedyRandomFC_MCD_GAllActions",
                "P_LUGreedyRandom_GAllActions_SASH_ASBU",
                "P_LGreedyRelFC_MCD_GAllActions", "P_LGreedyRandomRelFC_GP_ASBU", "L_eLUGreedyRelFC_GP_SASH", "P_LGreedyRandom_MCD_GP_SASH",
                "P_LUGreedyRelFC_MCD_GP", "P_eLGreedyFC_GAllActions", "P_LUGreedyRandom_GP_SASH", "P_Cost_UCTRelFC_GP_ASBU", "P_eLUGreedyRandomRelFC_MCD_GP_SASH",
                "P_LUGreedyFC_MCD_GP_SASH_ASBU", "L_eLUGreedyFC_GP_SASH_ASBU", "P_LGreedyRandomRelFC_MCD_GAllActions", "L_LUGreedyRelFC_GAllActions_SASH_ASBU",
                "L_Cost_eLUGreedyRandomRelFC_GAllActions_SASH_ASBU", "P_UCTLRelFC_GAllActions_ASBU", "P_LGreedyRandom_GAllActions_ASBU", "P_eLGreedy_MCD_GAllActions",
                "P_LGreedyFC_MCD_GP_SASH_ASBU", "P_UCTLU_GAllActions", "P_LGreedyRandomRelFC_GAllActions", "P_Cost_LUGreedyRandomRelFC_GP_ASBU",
                "P_eLUGreedy_GP_ASBU", "P_eLUGreedyRandom_GAllActions_SASH_ASBU", "P_eLGreedyRandom_GAllActions_ASBU", "P_Cost_eLUGreedyRandomRelFC_MCD_GP_ASBU",
                "L_UCTLRelFC_MCD_GP_SASH_ASBU", "P_eLGreedyRandom_GAllActions_SASH", "P_LUGreedyRelFC_MCD_GAllActions_SASH_ASBU",
                "P_LUGreedyRandomRelFC_MCD_GP_SASH_ASBU", "P_eLUGreedyRandomFC_GAllActions_SASH", "P_eLUGreedyRandomFC_MCD_GP_SASH",
                "P_LUGreedyRandomFC_MCD_GAllActions_SASH", "P_LUGreedyRandomRelFC_GAllActions_SASH", "L_LUGreedyRelFC_GP_SASH_ASBU",
                "P_eLUGreedyRandomRelFC_GP", "P_LUGreedyFC_MCD_GAllActions_ASBU", "P_LUGreedyRandomRelFC_GP", "L_LUGreedyRelFC_MCD_GP_SASH",
                "P_LGreedy_GP", "P_LUGreedyRandomRelFC_MCD_GAllActions", "L_eLUGreedyFC_MCD_GAllActions_SASH_ASBU", "L_Cost_LUGreedyRandomRelFC_MCD_GP_SASH",
                "P_eLUGreedyRandomFC_MCD_GAllActions_SASH", "P_LGreedyRandomFC_GP_ASBU", "P_LGreedyRelFC_GAllActions_SASH_ASBU", "P_LGreedyRandomFC_MCD_GP",
                "L_eLUGreedyFC_MCD_GP_SASH_ASBU", "P_LGreedy_GP_SASH", "L_LUGreedyFC_MCD_GAllActions_SASH_ASBU", "P_LGreedyRandomFC_GP_SASH_ASBU",
                "P_LUGreedyRandom_MCD_GP_SASH", "P_eLUGreedyFC_GP_ASBU", "P_Cost_UCTRelFC_GAllActions", "P_LUGreedyRandomFC_MCD_GP_ASBU", "P_LUGreedyRelFC_MCD_GP_SASH",
                "P_LGreedyRandom_GAllActions", "P_LGreedyRandomRelFC_MCD_GP", "P_LGreedy_MCD_GP_ASBU", "L_eLUGreedyFC_GAllActions_SASH_ASBU",
                "L_LUGreedyRandomRelFC_MCD_GAllActions_SASH_ASBU", "L_eLGreedy_GP_ASBU", "P_UCTLFC_GAllActions_ASBU", "P_LGreedyRandomRelFC_MCD_GAllActions_ASBU",
                "P_UCTLU_GAllActions_SASH_ASBU", "P_eLUGreedy_GP_SASH_ASBU", "P_Cost_LUGreedyRandomRelFC_MCD_GAllActions_SASH", "L_LGreedyRelFC_GP_SASH",
                "L_eLGreedyRandomFC_GAllActions_SASH_ASBU", "P_LGreedy_MCD_GP_SASH", "P_UCTLFC_MCD_GP_SASH", "P_eLUGreedyRandomFC_GP_SASH_ASBU",
                "L_LGreedyRandomFC_GP_SASH", "P_LUGreedy_GP_SASH", "P_LUGreedyFC_GP_SASH_ASBU", "P_Cost_eLUGreedyRandomRelFC_MCD_GAllActions_ASBU",
                "P_LUGreedyFC_GP", "L_LGreedyFC_GAllActions_SASH_ASBU", "P_LGreedyRandomFC_MCD_GP_ASBU", "P_Cost_eLUGreedyRandomRelFC_MCD_GAllActions_SASH_ASBU",
                "P_LUGreedy_GP_ASBU", "L_LUGreedyRandom_GP_ASBU", "P_Cost_LUGreedyRandomRelFC_GAllActions", "P_UCTL_GP_SASH_ASBU",
                "L_Cost_eLUGreedyRandomRelFC_MCD_GP_SASH_ASBU", "L_UCTLFC_GP_SASH_ASBU", "L_LUGreedyRelFC_MCD_GAllActions_SASH_ASBU",
                "P_LGreedyRandomRelFC_MCD_GP_ASBU", "P_LGreedyRelFC_MCD_GP_SASH", "L_eLUGreedyRandomRelFC_MCD_GP_SASH", "P_LGreedyRandom_GP_SASH_ASBU",
                "P_LUGreedyRelFC_GP_SASH_ASBU", "P_UCTLUFC_GP_ASBU", "P_LGreedyFC_GAllActions_SASH", "P_Cost_eLUGreedyRandomRelFC_MCD_GAllActions_SASH",
                "L_LUGreedy_GP_ASBU", "P_LGreedyRandom_MCD_GP_ASBU", "P_eLUGreedyRandomRelFC_GP_SASH", "L_LGreedyRandomRelFC_GAllActions_SASH_ASBU",
                "P_UCTLU_GAllActions_SASH", "P_eLGreedyRandom_MCD_GAllActions", "P_eLUGreedyRandomRelFC_MCD_GAllActions", "P_eLGreedy_MCD_GP_SASH_ASBU",
                "P_eLGreedyFC_GP", "P_Cost_eLUGreedyRandomRelFC_GAllActions_SASH_ASBU", "L_eLUGreedyRandomRelFC_GP_SASH", "P_LGreedy_MCD_GAllActions_SASH_ASBU",
                "P_Cost_UCTRelFC_MCD_GP_SASH", "P_LUGreedyRandom_GAllActions_SASH", "P_LGreedyRelFC_MCD_GAllActions_ASBU", "L_eLUGreedyRandomFC_MCD_GP_SASH",
                "P_eLUGreedyRelFC_GAllActions_ASBU", "L_LUGreedyFC_MCD_GP_SASH", "L_UCTLRelFC_MCD_GP_SASH", "L_UCTL_GP_ASBU", "L_LUGreedyFC_MCD_GP_SASH_ASBU",
                "P_eLGreedyFC_MCD_GAllActions_SASH", "P_LUGreedyRandomRelFC_MCD_GAllActions_SASH_ASBU", "P_UCTLUFC_MCD_GAllActions_SASH_ASBU",
                "P_LUGreedy_MCD_GAllActions_ASBU", "P_LUGreedy_MCD_GP", "P_UCTLRelFC_MCD_GAllActions", "P_UCTLFC_GP_SASH_ASBU", "P_eLUGreedyRandom_MCD_GP",
                "P_eLGreedyFC_GP_SASH_ASBU", "L_eLUGreedyRelFC_MCD_GP_SASH_ASBU", "P_UCTLU_MCD_GAllActions_ASBU", "P_eLUGreedyRelFC_MCD_GP_SASH_ASBU",
                "P_UCTL_GAllActions_SASH", "P_eLUGreedyFC_GAllActions", "P_LGreedyRandomFC_GAllActions_ASBU", "P_UCTLUFC_GP_SASH_ASBU",
                "P_eLGreedyRandomFC_GAllActions_SASH", "P_UCTLUFC_MCD_GAllActions_ASBU", "P_LGreedyFC_MCD_GAllActions_SASH_ASBU", "P_eLUGreedyRandom_GP_SASH",
                "P_LUGreedyRandomFC_MCD_GAllActions_ASBU", "L_Cost_LUGreedyRandomRelFC_GAllActions_SASH_ASBU", "L_LGreedyRandomFC_MCD_GP_SASH_ASBU",
                "P_UCTL_MCD_GP", "L_Cost_LUGreedyRandomRelFC_GP_SASH_ASBU", "P_eLGreedy_GAllActions_SASH", "P_LGreedyRelFC_MCD_GP_ASBU",
                "P_eLUGreedyRelFC_GAllActions", "P_UCTLFC_GP_ASBU", "P_Cost_eLUGreedyRandomRelFC_MCD_GP_SASH_ASBU", "P_Cost_UCTRelFC_MCD_GAllActions_ASBU",
                "P_UCTLFC_MCD_GP_SASH_ASBU", "L_UCTLUFC_MCD_GP_SASH_ASBU", "P_Cost_LUGreedyRandomRelFC_MCD_GP_SASH", "P_LUGreedyRandomRelFC_MCD_GP_SASH",
                "P_UCTLRelFC_MCD_GAllActions_ASBU", "P_eLGreedy_MCD_GP_ASBU", "L_LGreedyFC_MCD_GAllActions_SASH_ASBU", "P_LUGreedyFC_GP_SASH",
                "L_eLUGreedyRandomRelFC_GAllActions_SASH_ASBU", "P_Cost_UCTRelFC_MCD_GAllActions_SASH_ASBU", "L_eLGreedyFC_MCD_GP_SASH_ASBU",
                "P_UCTLUFC_MCD_GP_ASBU", "L_LGreedyRandomFC_GAllActions_SASH_ASBU", "P_LUGreedyRandomRelFC_MCD_GAllActions_SASH",
                "L_eLUGreedyRandomFC_GP_SASH_ASBU", "P_eLUGreedyRandomFC_MCD_GP_ASBU", "L_eLGreedyRandomFC_GP_SASH", "P_Cost_UCTRelFC_GAllActions_SASH_ASBU",
                "P_LUGreedyRandomFC_GAllActions_SASH", "P_LUGreedyRandomFC_GAllActions_SASH_ASBU", "L_LUGreedyRandom_GP",
                "L_Cost_eLUGreedyRandomRelFC_MCD_GAllActions_SASH_ASBU", "P_Cost_LUGreedyRandomRelFC_MCD_GAllActions_ASBU", "P_LUGreedyRandom_MCD_GP_ASBU",
                "P_LUGreedyRandomFC_GP_SASH", "L_LGreedyRandom_GP", "P_LUGreedyRelFC_GAllActions", "P_eLGreedyRandomFC_GP_SASH",
                "P_eLGreedyFC_MCD_GAllActions", "P_eLGreedyRandomFC_MCD_GAllActions", "P_LGreedyRelFC_MCD_GAllActions_SASH_ASBU", "P_LUGreedyRelFC_MCD_GP_ASBU",
                "P_Cost_eLUGreedyRandomRelFC_GAllActions", "P_eLUGreedyRandomFC_GP_ASBU", "P_eLGreedyRandom_MCD_GP_SASH",
                "P_eLUGreedyFC_MCD_GP_ASBU", "P_Cost_UCTRelFC_GP", "P_LUGreedyFC_GAllActions", "P_eLUGreedyRandom_GAllActions", "L_LUGreedyRandomRelFC_MCD_GP_SASH",
                "P_LUGreedy_GAllActions_ASBU", "P_eLUGreedyFC_MCD_GP_SASH", "P_eLUGreedyRandom_GP_ASBU", "P_Cost_LUGreedyRandomRelFC_GP_SASH_ASBU", "P_Cost_eLUGreedyRandomRelFC_MCD_GP", "P_eLUGreedyRelFC_MCD_GP", "L_Cost_UCTRelFC_MCD_GP_SASH_ASBU", "L_LUGreedyRandomRelFC_GP_SASH_ASBU", "P_Cost_LUGreedyRandomRelFC_MCD_GP_ASBU", "L_LGreedyRandomFC_MCD_GAllActions_SASH_ASBU", "P_UCTLU_GP_SASH_ASBU", "P_LGreedyFC_GP", "P_LUGreedy_GAllActions_SASH_ASBU", "P_eLGreedyRandomFC_MCD_GP_ASBU", "P_LUGreedy_MCD_GAllActions_SASH_ASBU", "P_UCTLUFC_GAllActions", "P_LUGreedyRandom_MCD_GAllActions_SASH", "P_LGreedy_GP_SASH_ASBU", "P_LGreedyRelFC_GP_SASH_ASBU", "P_eLGreedyRandomFC_GP_SASH_ASBU", "P_LGreedyRelFC_GP", "P_UCTLUFC_MCD_GP_SASH_ASBU", "P_eLUGreedyRelFC_GP_SASH", "P_eLUGreedyRandom_GP_SASH_ASBU", "P_eLUGreedyRelFC_GAllActions_SASH_ASBU", "P_UCTLU_GP", "P_LGreedy_GP_ASBU", "P_LUGreedyRandomRelFC_GP_SASH", "P_UCTLFC_MCD_GAllActions_SASH", "L_LGreedyRandomRelFC_MCD_GAllActions_SASH_ASBU", "P_LUGreedyRandomFC_MCD_GP_SASH", "P_Cost_LUGreedyRandomRelFC_MCD_GAllActions_SASH_ASBU", "L_Cost_eLUGreedyRandomRelFC_MCD_GP_SASH", "P_LGreedyRandomRelFC_GAllActions_ASBU", "L_LGreedyRelFC_MCD_GP_SASH", "P_UCTL_GP_ASBU", "P_eLUGreedyFC_GP_SASH", "P_eLUGreedyFC_MCD_GP_SASH_ASBU", "P_eLUGreedyFC_MCD_GAllActions", "P_eLUGreedyRandomFC_MCD_GAllActions_ASBU", "P_LGreedyRandomRelFC_GP_SASH_ASBU", "P_eLGreedy_MCD_GP", "P_Cost_LUGreedyRandomRelFC_MCD_GP_SASH_ASBU", "L_LUGreedyRandomFC_GP_SASH_ASBU", "P_eLUGreedyRelFC_MCD_GP_ASBU", "L_eLUGreedyRandom_GP_ASBU", "L_Cost_eLUGreedyRandomRelFC_GP_SASH_ASBU", "L_eLGreedyRandomFC_GP_SASH_ASBU", "P_LUGreedyRandom_MCD_GAllActions", "P_eLGreedy_MCD_GP_SASH", "P_eLUGreedyRandomRelFC_MCD_GP_SASH_ASBU", "P_eLUGreedy_GAllActions_SASH", "P_UCTL_GAllActions", "P_LGreedyRandomRelFC_GP_SASH", "L_eLUGreedy_GP_ASBU", "P_eLUGreedyRandomFC_MCD_GP_SASH_ASBU", "P_eLUGreedyRelFC_MCD_GAllActions_SASH_ASBU", "L_LGreedyRandomRelFC_MCD_GP_SASH", "P_eLUGreedyRandomRelFC_GAllActions_SASH_ASBU", "P_Cost_eLUGreedyRandomRelFC_GP_SASH_ASBU", "L_Cost_LUGreedyRandomRelFC_MCD_GAllActions_SASH_ASBU", "P_eLGreedyRandom_GAllActions", "P_UCTLUFC_GP", "P_LGreedy_MCD_GAllActions_SASH", "P_eLUGreedy_GAllActions_ASBU", "P_LUGreedyRandomRelFC_GAllActions_SASH_ASBU", "P_LUGreedyRandomFC_MCD_GP", "P_eLUGreedy_MCD_GP_SASH_ASBU", "L_LUGreedyRandomFC_MCD_GP_SASH", "P_eLUGreedyFC_GAllActions_SASH_ASBU", "P_eLGreedyRandomFC_GP", "P_UCTLU_MCD_GAllActions_SASH_ASBU", "P_LUGreedyRandom_GAllActions_ASBU", "P_eLUGreedyFC_GP_SASH_ASBU", "P_eLGreedyRandomFC_MCD_GP", "P_eLUGreedyRelFC_GP_ASBU", "L_LUGreedyRandomFC_MCD_GP_SASH_ASBU", "L_Cost_UCTRelFC_MCD_GAllActions_SASH_ASBU", "P_UCTLUFC_MCD_GP", "P_LUGreedyFC_GAllActions_SASH_ASBU", "P_eLGreedy_GP_ASBU", "P_UCTL_MCD_GAllActions_ASBU", "P_Cost_UCTRelFC_MCD_GAllActions_SASH", "P_UCTLUFC_GP_SASH", "P_eLUGreedyRandomFC_GAllActions_SASH_ASBU", "P_LGreedyRandom_MCD_GAllActions", "P_LUGreedyFC_MCD_GP_SASH", "P_LGreedyRandomFC_GP", "P_LUGreedyRandomRelFC_GP_ASBU", "L_LGreedyFC_MCD_GP_SASH_ASBU", "P_LGreedyRandomFC_MCD_GAllActions", "L_eLUGreedyRandomFC_MCD_GAllActions_SASH_ASBU", "P_LUGreedyRandomFC_MCD_GAllActions", "P_Cost_UCTRelFC_MCD_GP_ASBU", "L_UCTLUFC_MCD_GAllActions_SASH_ASBU", "P_eLGreedy_GP_SASH", "L_eLGreedyRandom_GP_ASBU", "P_UCTL_MCD_GP_ASBU", "P_eLUGreedyRandom_GAllActions_SASH", "P_eLUGreedy_MCD_GP", "P_LGreedyRelFC_MCD_GP_SASH_ASBU", "P_Cost_eLUGreedyRandomRelFC_GAllActions_ASBU", "P_LGreedyFC_GAllActions", "P_LGreedyRandom_GP_ASBU", "P_LGreedyRandomFC_MCD_GAllActions_SASH", "P_Cost_LUGreedyRandomRelFC_GP", "P_UCTL_MCD_GP_SASH_ASBU", "L_LGreedyRelFC_GAllActions_SASH_ASBU", "P_eLUGreedyRandom_MCD_GAllActions_ASBU", "P_LGreedyRandomFC_GAllActions", "P_Cost_LUGreedyRandomRelFC_GAllActions_ASBU", "P_eLUGreedyRandomFC_GAllActions", "L_LGreedyRelFC_MCD_GP_SASH_ASBU", "P_LUGreedyRandomRelFC_GP_SASH_ASBU", "L_UCTLFC_GAllActions_SASH_ASBU", "P_LGreedyRelFC_GP_SASH", "P_UCTLRelFC_GP", "P_eLGreedyRandom_GP_SASH_ASBU", "P_LGreedyFC_GAllActions_SASH_ASBU", "P_LUGreedyRelFC_MCD_GAllActions", "P_LGreedyFC_GP_SASH", "P_eLUGreedy_GP_SASH", "P_LGreedy_GAllActions", "P_eLUGreedy_MCD_GAllActions_SASH", "P_eLUGreedyFC_GP", "L_eLUGreedyRandomFC_GAllActions_SASH_ASBU", "L_eLGreedyFC_MCD_GAllActions_SASH_ASBU", "P_LUGreedyRelFC_MCD_GP_SASH_ASBU", "P_UCTLUFC_GAllActions_SASH", "P_Cost_LUGreedyRandomRelFC_GAllActions_SASH_ASBU", "P_LGreedyFC_MCD_GP", "P_eLGreedy_MCD_GAllActions_ASBU", "P_LUGreedyRandomRelFC_GAllActions", "L_UCTLFC_MCD_GAllActions_SASH_ASBU", "P_Cost_UCTRelFC_MCD_GP", "L_Cost_UCTRelFC_MCD_GP_SASH", "P_LUGreedyRandom_GP_SASH_ASBU", "P_LGreedyFC_MCD_GAllActions_SASH", "P_UCTLRelFC_MCD_GP_ASBU", "P_eLUGreedy_GP", "P_Cost_eLUGreedyRandomRelFC_GP", "P_LUGreedyRelFC_GAllActions_SASH_ASBU", "P_eLGreedyRandomFC_MCD_GAllActions_SASH", "P_eLUGreedyRandom_GAllActions_ASBU", "P_eLGreedyRandom_MCD_GAllActions_SASH", "L_LGreedyFC_GP_SASH", "P_eLGreedyRandom_GP", "P_eLUGreedyFC_MCD_GAllActions_SASH_ASBU", "P_LUGreedy_MCD_GP_ASBU", "L_LGreedyRelFC_GP_SASH_ASBU", "P_UCTLUFC_MCD_GAllActions", "P_Cost_eLUGreedyRandomRelFC_MCD_GP_SASH", "L_UCTLU_MCD_GP_ASBU", "P_LUGreedyRandom_MCD_GAllActions_SASH_ASBU", "L_eLUGreedyRandomFC_GP_SASH", "P_UCTLFC_GP", "P_LUGreedyRandomFC_GAllActions_ASBU", "P_eLGreedyRandom_GP_ASBU", "P_LUGreedyRandomFC_GP_SASH_ASBU", "P_LGreedyRandom_MCD_GP", "P_eLUGreedyFC_MCD_GAllActions_ASBU", "P_eLGreedyFC_MCD_GP_ASBU", "P_eLUGreedyFC_MCD_GP", "P_eLUGreedyRandomRelFC_MCD_GP", "P_eLUGreedyRandom_MCD_GP_ASBU", "P_LGreedyFC_GAllActions_ASBU", "P_LUGreedy_MCD_GAllActions_SASH", "P_eLGreedyFC_MCD_GP_SASH", "P_LGreedyRandomFC_GP_SASH", "P_LGreedy_MCD_GAllActions", "P_eLUGreedyRandomRelFC_MCD_GAllActions_SASH", "P_eLGreedyRandomFC_MCD_GAllActions_SASH_ASBU", "L_LUGreedyRelFC_MCD_GP_SASH_ASBU", "P_eLUGreedyRelFC_MCD_GAllActions_ASBU", "L_LGreedyRandomFC_GP_SASH_ASBU", "P_eLUGreedyRelFC_MCD_GAllActions_SASH", "P_LUGreedyRandomFC_MCD_GP_SASH_ASBU", "P_LUGreedyRandom_GAllActions", "P_Cost_LUGreedyRandomRelFC_MCD_GP", "L_LGreedyRelFC_MCD_GAllActions_SASH_ASBU", "P_LGreedyRandomFC_MCD_GAllActions_ASBU", "L_LGreedyFC_MCD_GP_SASH", "P_UCTLFC_MCD_GP_ASBU", "P_UCTLRelFC_GAllActions_SASH", "P_eLGreedyFC_GAllActions_ASBU", "P_LUGreedyRandom_MCD_GAllActions_ASBU", "L_UCTL_GP", "L_eLGreedyFC_MCD_GP_SASH", "P_eLGreedyRandom_MCD_GAllActions_ASBU", "P_LGreedyRandomRelFC_MCD_GP_SASH", "P_LGreedy_GAllActions_SASH_ASBU", "P_LGreedyRelFC_MCD_GP", "L_LUGreedyFC_GP_SASH", "L_LGreedyRandom_GP_ASBU", "P_UCTL_GP", "L_UCTLRelFC_MCD_GAllActions_SASH_ASBU", "P_LGreedy_GAllActions_ASBU", "L_UCTLRelFC_GP_SASH_ASBU", "P_LGreedyRandomFC_GAllActions_SASH_ASBU", "P_eLUGreedyRelFC_GP_SASH_ASBU", "P_Cost_UCTRelFC_GP_SASH", "P_LUGreedy_GAllActions_SASH", "P_LGreedyRelFC_GAllActions_ASBU", "P_UCTLFC_MCD_GAllActions_ASBU", "P_Cost_eLUGreedyRandomRelFC_MCD_GAllActions", "P_Cost_eLUGreedyRandomRelFC_GAllActions_SASH", "L_eLUGreedyRelFC_MCD_GAllActions_SASH_ASBU", "P_Cost_LUGreedyRandomRelFC_GAllActions_SASH", "P_UCTLFC_GP_SASH", "P_eLUGreedy_MCD_GP_ASBU", "P_UCTLRelFC_GP_ASBU", "P_LGreedyRandom_MCD_GP_SASH_ASBU", "L_eLUGreedyRandomRelFC_GP_SASH_ASBU", "P_eLUGreedyRandomRelFC_GAllActions_SASH", "P_LUGreedy_MCD_GP_SASH", "P_UCTLRelFC_MCD_GP", "P_UCTLRelFC_GAllActions", "P_eLUGreedyRandom_GP", "P_LGreedyRandom_GP", "P_LUGreedyFC_MCD_GAllActions_SASH", "P_UCTLFC_GAllActions_SASH", "P_LGreedyRandom_GP_SASH", "P_LGreedyRandom_MCD_GAllActions_SASH", "P_LGreedyRandom_MCD_GAllActions_ASBU", "P_eLGreedyFC_GP_SASH", "P_Cost_UCTRelFC_GAllActions_ASBU", "P_LUGreedy_MCD_GP_SASH_ASBU", "P_UCTLU_MCD_GP", "P_eLGreedyRandom_GP_SASH", "L_UCTLFC_MCD_GP_SASH_ASBU", "P_eLUGreedyRandomRelFC_GP_SASH_ASBU", "P_UCTLUFC_GAllActions_SASH_ASBU", "P_LUGreedyRelFC_GP_SASH", "P_LUGreedyFC_MCD_GP", "L_LUGreedyRandomFC_GAllActions_SASH_ASBU", "P_LGreedyRandomFC_MCD_GP_SASH_ASBU", "P_UCTLRelFC_MCD_GAllActions_SASH", "P_LUGreedyRandomFC_MCD_GAllActions_SASH_ASBU", "L_LUGreedyFC_GP_SASH_ASBU", "P_UCTLU_MCD_GAllActions_SASH", "P_LGreedy_MCD_GP_SASH_ASBU", "P_eLUGreedyRandomRelFC_GAllActions_ASBU", "P_UCTLU_MCD_GP_SASH", "L_eLGreedyRandomFC_MCD_GP_SASH", "P_LGreedyRelFC_MCD_GAllActions_SASH", "P_eLGreedyRandom_MCD_GP_ASBU", "P_LUGreedy_GP_SASH_ASBU", "P_LUGreedyRandomFC_GP", "L_eLGreedyRandomFC_MCD_GAllActions_SASH_ASBU", "P_Cost_UCTRelFC_MCD_GAllActions", "P_eLUGreedyRelFC_MCD_GP_SASH", "P_eLUGreedyRandom_MCD_GAllActions_SASH_ASBU", "L_eLUGreedyRandomRelFC_MCD_GP_SASH_ASBU", "P_UCTL_MCD_GAllActions_SASH_ASBU", "P_eLGreedyRandomFC_GAllActions", "P_eLGreedyRandomFC_GP_ASBU", "P_eLUGreedy_MCD_GP_SASH", "P_eLGreedyFC_MCD_GAllActions_SASH_ASBU", "P_LGreedyFC_GP_SASH_ASBU", "P_Cost_LUGreedyRandomRelFC_MCD_GAllActions", "P_UCTLFC_GAllActions", "P_LUGreedyRandomFC_GAllActions", "P_eLGreedy_GP", "P_eLGreedy_GAllActions", "P_eLUGreedyRandomRelFC_GAllActions", "P_UCTLU_GP_ASBU", "P_eLGreedyFC_MCD_GP", "P_LUGreedyRelFC_MCD_GAllActions_SASH", "L_LUGreedyRandomFC_MCD_GAllActions_SASH_ASBU", "P_eLGreedyFC_GAllActions_SASH_ASBU", "L_eLGreedyFC_GP_SASH_ASBU", "L_eLGreedyRandomFC_MCD_GP_SASH_ASBU", "P_LUGreedyRandomRelFC_MCD_GP_ASBU", "L_LGreedyFC_GP_SASH_ASBU", "P_eLUGreedyFC_GAllActions_SASH", "P_eLUGreedyRandomRelFC_GP_ASBU", "L_Cost_eLUGreedyRandomRelFC_GP_SASH", "P_LGreedyRandomRelFC_MCD_GAllActions_SASH", "P_UCTLRelFC_GAllActions_SASH_ASBU", "P_UCTLUFC_GAllActions_ASBU", "P_LGreedyRandomFC_MCD_GP_SASH", "P_eLGreedyFC_MCD_GP_SASH_ASBU", "L_eLGreedyFC_GAllActions_SASH_ASBU", "P_eLGreedy_GP_SASH_ASBU", "L_LGreedyRandomRelFC_MCD_GP_SASH_ASBU", "P_LGreedyRandomRelFC_GP", "P_LGreedyRandomRelFC_GAllActions_SASH_ASBU", "P_UCTLU_MCD_GAllActions", "P_LUGreedyRandom_GP", "P_LUGreedyRandomRelFC_MCD_GAllActions_ASBU", "P_LGreedyRandom_MCD_GAllActions_SASH_ASBU", "P_eLUGreedyRelFC_GAllActions_SASH", "P_LUGreedyRandomRelFC_MCD_GP", "P_LGreedyRandomRelFC_MCD_GP_SASH_ASBU", "P_eLGreedyFC_GP_ASBU", "P_LUGreedyRandom_MCD_GP_SASH_ASBU", "P_Cost_eLUGreedyRandomRelFC_GP_ASBU", "P_LUGreedyFC_GAllActions_ASBU", "P_eLUGreedyRandomFC_GP_SASH", "P_LGreedyFC_MCD_GP_SASH", "P_Cost_UCTRelFC_MCD_GP_SASH_ASBU", "P_eLUGreedyFC_MCD_GAllActions_SASH", "P_eLGreedyRandom_MCD_GP", "P_LGreedy_MCD_GP", "P_UCTL_GP_SASH", "P_eLGreedyRandom_GAllActions_SASH_ASBU", "P_LUGreedyFC_MCD_GAllActions_SASH_ASBU", "L_LUGreedyRandomRelFC_MCD_GP_SASH_ASBU", "P_eLGreedyFC_GAllActions_SASH", "L_LGreedy_GP_ASBU", "P_LGreedyRandom_GAllActions_SASH", "P_UCTLRelFC_GP_SASH_ASBU", "P_eLGreedyRandomFC_GAllActions_ASBU", "P_UCTLU_MCD_GP_SASH_ASBU", "L_eLUGreedyRelFC_GAllActions_SASH_ASBU", "P_LGreedyRandom_GAllActions_SASH_ASBU", "L_LGreedyRandomFC_MCD_GP_SASH", "P_LGreedyRandomRelFC_MCD_GAllActions_SASH_ASBU", "L_Cost_LUGreedyRandomRelFC_MCD_GP_SASH_ASBU", "P_eLUGreedyRandomFC_MCD_GP", "P_eLUGreedyRandomFC_GAllActions_ASBU", "P_eLUGreedyRandomRelFC_MCD_GAllActions_SASH_ASBU", "P_LGreedyFC_GP_ASBU", "P_LUGreedyRelFC_GAllActions_ASBU", "P_eLUGreedy_MCD_GAllActions", "P_eLUGreedy_GAllActions", "P_LUGreedyRelFC_MCD_GAllActions_ASBU", "P_eLUGreedy_MCD_GAllActions_SASH_ASBU", "P_eLUGreedyFC_GAllActions_ASBU", "P_Cost_UCTRelFC_GAllActions_SASH", "P_UCTL_MCD_GP_SASH", "P_eLGreedyRandomFC_MCD_GP_SASH", "P_UCTL_MCD_GAllActions", "L_LUGreedyRandomRelFC_GAllActions_SASH_ASBU", "P_eLUGreedyRandom_MCD_GAllActions_SASH", "L_eLUGreedyRelFC_GP_SASH_ASBU", "P_UCTLUFC_MCD_GAllActions_SASH", "P_eLUGreedyRelFC_MCD_GAllActions", "P_UCTLFC_MCD_GP", "L_UCTLU_GP_ASBU", "P_eLUGreedyRandomRelFC_MCD_GAllActions_ASBU", "P_UCTLU_MCD_GP_ASBU", "P_eLGreedyRandomFC_GAllActions_SASH_ASBU", "P_LGreedyRelFC_GP_ASBU", "P_eLUGreedyRandomRelFC_MCD_GP_ASBU", "P_eLGreedy_MCD_GAllActions_SASH", "L_UCTL_MCD_GP_ASBU", "P_UCTLU_GAllActions_ASBU", "P_eLUGreedy_MCD_GAllActions_ASBU", "P_LGreedyRelFC_GAllActions", "P_LUGreedyFC_MCD_GAllActions", "P_UCTLRelFC_MCD_GAllActions_SASH_ASBU", "P_eLUGreedyRandom_MCD_GAllActions", "L_LGreedyRandomRelFC_GP_SASH", "P_LUGreedyFC_MCD_GP_ASBU", "P_UCTLFC_MCD_GAllActions", "P_UCTL_GAllActions_ASBU", "P_LUGreedyRelFC_GP_ASBU", "P_UCTLFC_GAllActions_SASH_ASBU", "P_LUGreedyRandom_MCD_GP", "P_eLUGreedyRandom_MCD_GP_SASH", "P_LGreedyRandomFC_GAllActions_SASH", "P_LGreedyRandomFC_MCD_GAllActions_SASH_ASBU", "L_LUGreedy_GP", "P_LGreedy_GAllActions_SASH", "P_Cost_LUGreedyRandomRelFC_GP_SASH", "L_UCTLU_GP", "P_eLGreedy_GAllActions_ASBU", "P_LUGreedyRandomFC_GP_ASBU", "P_eLGreedyFC_MCD_GAllActions_ASBU", "L_eLUGreedyRelFC_MCD_GP_SASH", "P_LUGreedyFC_GAllActions_SASH", "P_LUGreedy_MCD_GAllActions", "P_Cost_eLUGreedyRandomRelFC_GP_SASH", "P_eLUGreedyRelFC_GP", "P_eLGreedyRandom_MCD_GAllActions_SASH_ASBU", "L_eLUGreedyRandomFC_MCD_GP_SASH_ASBU", "P_LUGreedyRandom_GP_ASBU", "P_eLGreedyRandom_MCD_GP_SASH_ASBU", "P_UCTL_GAllActions_SASH_ASBU", "P_UCTLRelFC_GP_SASH", "P_LUGreedy_GP", "P_eLGreedyRandomFC_MCD_GP_SASH_ASBU", "L_eLUGreedyFC_GP_SASH", "L_LGreedyRandomRelFC_GP_SASH_ASBU", "P_UCTL_MCD_GAllActions_SASH", "P_eLGreedy_GAllActions_SASH_ASBU", "L_eLUGreedyRandomRelFC_MCD_GAllActions_SASH_ASBU", "P_LGreedyRandomRelFC_GAllActions_SASH", "L_LUGreedyFC_GAllActions_SASH_ASBU", "P_LUGreedyRandomRelFC_GAllActions_ASBU", "P_eLGreedy_MCD_GAllActions_SASH_ASBU", "P_Cost_UCTRelFC_GP_SASH_ASBU", "P_LUGreedyFC_GP_ASBU", "L_eLUGreedyFC_MCD_GP_SASH", "P_LUGreedy_GAllActions", "P_LGreedyRelFC_GAllActions_SASH", "P_eLGreedyRandomFC_MCD_GAllActions_ASBU", "P_LGreedyFC_MCD_GAllActions", "P_LUGreedyRelFC_GAllActions_SASH", "P_eLUGreedyRandomFC_MCD_GAllActions_SASH_ASBU", "P_eLUGreedy_GAllActions_SASH_ASBU", "P_UCTLUFC_MCD_GP_SASH", "P_LGreedyFC_MCD_GP_ASBU", "P_eLUGreedyRandomFC_GP", "P_LGreedyFC_MCD_GAllActions_ASBU", "P_UCTLRelFC_MCD_GP_SASH", "L_eLGreedyFC_GP_SASH", "L_UCTLFC_MCD_GP_SASH", "P_UCTLRelFC_MCD_GP_SASH_ASBU", "P_UCTLFC_MCD_GAllActions_SASH_ASBU", "L_UCTLUFC_MCD_GP_SASH"};
        return filterConfigs(configNames, allconfigs,true);


    }

    public static void runSmallExample() {
        String resFolderExt = "tro_examples/";
        String filename = "tro_example_new_small";
        boolean[] boolvals = new boolean[]{true, false};
        boolean hasSharedState = true;
        boolean timeBound = false;
        boolean domaxcostdeadends = false;
        boolean dointervalvi = false;

        int maxruns = 100;
        String resSuffix = "small_example_runs_unexplored" + maxruns;

        ArrayList<String> configNames = new ArrayList<>();
        ArrayList<Configuration> configs = getridofdoneconfigs(getAllConfigs());
        int maxTests = configs.size() * maxruns;
        int testsSofar = 0;
        System.out.println(String.format("Running %d configurations with %d tests each for a total of %d tests", configs.size(), maxruns, maxTests));
        int startI = 0;
        int stopI = 1490;
        int repeatConfigNum = 0;
        boolean dryrun = false;
        for (int i = 0; i < configs.size(); i++) {

            Configuration config = configs.get(i);
            config.setJustLogs(false);
            config.skipUnexploredNodesInPolEval();
            if (!configNames.contains(config.getConfigname())) {
                configNames.add(config.getConfigname());
            } else {
                repeatConfigNum++;
                System.out.println(String.format("Skipping repeated config %s, %d repeats", config.getConfigname(), repeatConfigNum));
                continue;
            }
            if (i >= startI && i <= stopI) {
                RunConfiguration runconfig = new RunConfiguration();

                try {
                    System.out.println("\n\nRunning configuration " + config.getConfigname() + " - " + i + "/" + configs.size() + "\n");
                    if (!dryrun)
                        runconfig.run(resFolderExt, config,
                                2, 2, filename, false, resSuffix, "_mult", maxruns, 0, 1);
                    testsSofar += maxruns;
                    System.out.println(String.format("Finished Configuration. %d / %d tests run.", testsSofar, maxTests));
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
        int maxTests = configs.size() * maxruns;
        int testsSofar = 0;
        System.out.println(String.format("Running %d configurations with %d tests each for a total of %d tests", configs.size(), maxruns, maxTests));

        for (int i = 0; i < configs.size(); i++) {
            Configuration config = configs.get(i);
            RunConfiguration runconfig = new RunConfiguration();
            try {
                System.out.println("\n\nRunning configuration " + config.getConfigname() + " - " + i + "/" + configs.size() + "\n");
                runconfig.run(resFolderExt, config,
                        2, 2, filename, false, resSuffix, "_mult", maxruns, 0, 1);
                testsSofar += maxruns;
                System.out.println(String.format("\nFinished Configuration. %d / %d tests run, %d to go", testsSofar, maxTests, maxTests - testsSofar));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    public static void main(String[] args) {

        runSmallExample();
        // runSmallExampleSelectedConfig();
    }


}
