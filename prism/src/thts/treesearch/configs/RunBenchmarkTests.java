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

    public static ArrayList<Configuration> getSelectedConfigs(boolean timeBound, boolean dointervalvi, long timeLimit) {
        ArrayList<Configuration> configs = new ArrayList<>();

        //2        Cost_UCTRelFC_MCD_ASBU_GP	FALSE	TRUE
        Configuration costUCTRelFC_MCD_ASBU_GP = new ConfigUCTRelFiniteCostJustCost(timeBound, false, true, dointervalvi);
        ((ConfigUCTRelFiniteCostJustCost) costUCTRelFC_MCD_ASBU_GP).doGreedyPolActSel();
        configs.add(costUCTRelFC_MCD_ASBU_GP);

        //4        Cost_UCTRelFC_MCD_SASH_ASBU_GP	TRUE	TRUE
        Configuration costUCTRelFC_MCD_SASH_ASBU_GP = new ConfigUCTRelFiniteCostJustCost(timeBound, true, true, dointervalvi);
        ((ConfigUCTRelFiniteCostJustCost) costUCTRelFC_MCD_SASH_ASBU_GP).doGreedyPolActSel();
        configs.add(costUCTRelFC_MCD_SASH_ASBU_GP);

        //  8      eLUGreedyRandomFC_MCD_GAllActions_SASH	TRUE	FALSE
        Configuration eLUGreedyRandomFC_MCD_GAllActions_SASH = new ConfigeLUGreedyRandom(timeBound, true, false, dointervalvi, true, true);
        configs.add(eLUGreedyRandomFC_MCD_GAllActions_SASH);

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
                //config.setJustLogs(true);
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
