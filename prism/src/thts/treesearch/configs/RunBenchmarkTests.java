package thts.treesearch.configs;


import thts.testing.testsuitehelper.GetTestInfo;
import thts.testing.testsuitehelper.TestSuite;
import thts.treesearch.configs.labelled.egreedy.elugreedy.*;
import thts.treesearch.configs.labelled.uct.ConfigLUCTRelFiniteCostJustCost;

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
        String[] configNames = {
                "L_Cost_LUGreedyRandomRelFC_GP_ASBU"
                , "L_Cost_eLUGreedyRandomRelFC_GP_ASBU"
        };

        ArrayList<Configuration> allconfigs = RunSelConfigsOnSmallGrid.getAllConfigs(timeBound, timeLimit, dointervalvi);
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

    public void runTestSuite() throws Exception {
        boolean timeBound = true;
        boolean dointervalvi = false;
        long timeLimit = 30 * 60 * 1000;
        boolean debug = false;
        ArrayList<Configuration> configs = getSelectedConfigs(timeBound, dointervalvi, timeLimit);
        for (int i = 0; i < configs.size(); i++) {
            for (String fts : filteredTestSuites.keySet()) {


                String fnSuffix = fts;

                Configuration config = configs.get(i);
                System.out.println("\nRunning configuration " + config.getConfigname() + " - " + i + "/" + configs.size() + " on test suite " + fts + "\n");
                RunConfiguration runconfig = new RunConfiguration();

                try {
                    runconfig.runTestSuite(filteredTestSuites.get(fts), config, debug, fnSuffix);
                } catch (Exception e) {
                    throw e;// e.printStackTrace();
                }
            }
        }
    }


    public static void main(String[] args) {
        RunBenchmarkTests rbt = new RunBenchmarkTests();
        try {
            rbt.runTestSuite();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
