package thts.treesearch.configs;


import thts.treesearch.configs.plain.egreedy.elgreedy.*;
import thts.treesearch.configs.plain.egreedy.elugreedy.*;
import thts.treesearch.configs.plain.uct.ConfigUCTRelFiniteCostJustCost;
import thts.treesearch.configs.plain.uct.uctl.ConfigUCTL;
import thts.treesearch.configs.plain.uct.uctl.ConfigUCTLFiniteCost;
import thts.treesearch.configs.plain.uct.uctl.ConfigUCTLFiniteCostNoMaxCostDE;
import thts.treesearch.configs.plain.uct.uctl.ConfigUCTLRelFiniteCost;
import thts.treesearch.configs.plain.uct.uctlu.ConfigUCTLU;
import thts.treesearch.configs.plain.uct.uctlu.ConfigUCTLUFiniteCost;
import thts.treesearch.configs.plain.uct.uctlu.ConfigUCTLUFiniteCostNoMaxCostDE;
import thts.treesearch.configs.plain.uct.uctlu.ConfigUCTLURelFiniteCost;

import java.util.ArrayList;

//PRISM_MAINCLASS=thts.treesearch.configs.RunInitialTestsPlain prism/bin/prism
public class RunInitialTestsPlain {

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
        String resSuffix = "_plain_re" + maxRuns + "_";

        ArrayList<Configuration> configs = getSelectedConfigs(timeBound, dointervalvi, 0);

        for (int i = 0; i < configs.size(); i++) {
            Configuration config = configs.get(i);
            RunConfiguration runconfig = new RunConfiguration();
            try {

                System.out.println("\n\nRunning configuration " + config.getConfigname() + " - " + i + "/" + configs.size() + "\n");
                runconfig.run(resFolderExt, config,
                        2, 3, filename, debug, resSuffix, "_mult", maxRuns, 0, 1);

            } catch (Exception e) {
                e.printStackTrace();
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


            String resSuffix = "_plain_reruns" + maxRuns + "_";

            System.out.println(String.format("\nRunning Tests on FSP %3d (%2d/%2d)", fsp, fspNum, fsps.length));
            System.out.println(String.format("\t %5d/%5d of total", numTets, maxTests));
            for (int i = 0; i < configs.size(); i++) {
                Configuration config = configs.get(i);
                if (config.getConfigname().contentEquals("P_UCTLU_GAllActions_ASBU"))
                    continue;
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

        //runSmallExampleSelConfigs();

    }


}
