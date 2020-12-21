package thts.treesearch.configs;


import thts.testing.testsuitehelper.GetTestInfo;
import thts.testing.testsuitehelper.TestSuite;
import thts.treesearch.configs.labelled.egreedy.elugreedy.*;
import thts.treesearch.configs.labelled.uct.ConfigUCTRelFiniteCostJustCost;
import thts.treesearch.configs.labelled.uct.uctlu.ConfigUCTLU;

import java.util.ArrayList;
import java.util.HashMap;

//PRISM_MAINCLASS=thts.treesearch.configs.RunBenchmarkTests prism/bin/prism
public class RunBenchmarkTests {

    HashMap<String, TestSuite> filteredTestSuites;

    public RunBenchmarkTests() {

        GetTestInfo testInfo = new GetTestInfo();
        String tnid = "Warehouse Shelf to Depot";
        HashMap<String, TestSuite> testSuites = testInfo.readTestSuitesFromCSV();
        filteredTestSuites = testInfo.filterTestSuitesForTest(tnid, testSuites);

    }

    public ArrayList<Configuration> getSelectedConfigs(boolean timeBound, boolean dointervalvi, long timeLimit) {
        ArrayList<Configuration> configs = new ArrayList<>();
        //1        Cost_UCTRelFC_MCD_GP	FALSE	FALSE

        Configuration costUCTRelFC_MCD_GP = new ConfigUCTRelFiniteCostJustCost(timeBound, false, false, dointervalvi);
        ((ConfigUCTRelFiniteCostJustCost) costUCTRelFC_MCD_GP).doGreedyPolActSel();

        //2        Cost_UCTRelFC_MCD_ASBU_GP	FALSE	TRUE
        Configuration costUCTRelFC_MCD_ASBU_GP = new ConfigUCTRelFiniteCostJustCost(timeBound, false, true, dointervalvi);
        ((ConfigUCTRelFiniteCostJustCost) costUCTRelFC_MCD_ASBU_GP).doGreedyPolActSel();

        //3        Cost_UCTRelFC_MCD_SASH_GP	TRUE	FALSE
        Configuration costUCTRelFC_MCD_SASH_GP = new ConfigUCTRelFiniteCostJustCost(timeBound, true, false, dointervalvi);
        ((ConfigUCTRelFiniteCostJustCost) costUCTRelFC_MCD_SASH_GP).doGreedyPolActSel();

        //4        Cost_UCTRelFC_MCD_SASH_ASBU_GP	TRUE	TRUE
        Configuration costUCTRelFC_MCD_SASH_ASBU_GP = new ConfigUCTRelFiniteCostJustCost(timeBound, true, true, dointervalvi);
        ((ConfigUCTRelFiniteCostJustCost) costUCTRelFC_MCD_SASH_ASBU_GP).doGreedyPolActSel();

        //5        UCTLU_GAllActions_ASBU	FALSE	TRUE
        Configuration uctlu_GAllActions_ASBU = new ConfigUCTLU(timeBound, false, true, dointervalvi);

        //6        eLUGreedyRandomRelFC_MCD_GAllActions	FALSE	FALSE
        Configuration eluGreedyRandomRelFC_MCD_GAllActions = new ConfigeLUGreedyRandomRelFiniteCost(timeBound, false, false, dointervalvi);

        //7        eLUGreedyRandomFC_GAllActions	FALSE	FALSE

        Configuration eluGreedyRandomFC_GAllActions = new ConfigeLUGreedyRandom(timeBound, false, false, dointervalvi, true, false);

        //  8      eLUGreedyRandomFC_MCD_GAllActions_SASH	TRUE	FALSE
        Configuration eLUGreedyRandomFC_MCD_GAllActions_SASH = new ConfigeLUGreedyRandom(timeBound, true, false, dointervalvi, true, true);

        //  9      Cost_eLUGreedyRandomRelFC_MCD_SASH_GP	TRUE	FALSE
        Configuration cost_eLUGreedyRandomRelFC_MCD_SASH_GP = new ConfigeLUGreedyRandomRelFiniteCostJustCost(timeBound, true, false, dointervalvi);
        ((ConfigeLUGreedyRandomRelFiniteCostJustCost) cost_eLUGreedyRandomRelFC_MCD_SASH_GP).doGreedyPolActSel();
        configs.add(cost_eLUGreedyRandomRelFC_MCD_SASH_GP);
        //  10     Cost_eLUGreedyRandomRelFC_MCD_ASBU_GP	FALSE	TRUE
        Configuration cost_eLUGreedyRandomRelFC_MCD_ASBU_GP = new ConfigeLUGreedyRandomRelFiniteCostJustCost(timeBound, false, true, dointervalvi);
        ((ConfigeLUGreedyRandomRelFiniteCostJustCost) cost_eLUGreedyRandomRelFC_MCD_ASBU_GP).doGreedyPolActSel();
        configs.add(cost_eLUGreedyRandomRelFC_MCD_ASBU_GP);
        configs.add(uctlu_GAllActions_ASBU);
        configs.add(costUCTRelFC_MCD_GP);
        configs.add(costUCTRelFC_MCD_ASBU_GP);
        configs.add(costUCTRelFC_MCD_SASH_GP);
        configs.add(costUCTRelFC_MCD_SASH_ASBU_GP);
        configs.add(eluGreedyRandomRelFC_MCD_GAllActions);
        configs.add(eluGreedyRandomFC_GAllActions);
        configs.add(eLUGreedyRandomFC_MCD_GAllActions_SASH);
        if (timeBound && timeLimit > 0) {
            for (Configuration config : configs) {
                config.setTimeTimeLimitInMS(timeLimit);
            }
        }
        return configs;
    }

    public void runTestSuite(){
        for(String fts: filteredTestSuites.keySet())
        {
//        String fts = "Failstates";
            boolean timeBound = true;
            boolean dointervalvi = false;
            long timeLimit = 30 * 60 * 1000;
            boolean debug = false;
            String fnSuffix = fts;
            ArrayList<Configuration> configs = getSelectedConfigs(timeBound, dointervalvi, timeLimit);
            for (int i = 0; i < configs.size(); i++) {
                Configuration config = configs.get(i);
                System.out.println("\nRunning configuration " + config.getConfigname() + " - " + i + "/" + configs.size() + " on test suite "+fts+ "\n");
                RunConfiguration runconfig = new RunConfiguration();
                try {
                    runconfig.runTestSuite(filteredTestSuites.get(fts), config, debug, fnSuffix);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }



    public static void main(String[] args) {
        RunBenchmarkTests rbt = new RunBenchmarkTests();
        rbt.runTestSuite();
    }


}
