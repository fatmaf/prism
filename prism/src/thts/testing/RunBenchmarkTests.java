package thts.testing;


import thts.testing.testsuitehelper.GetTestInfo;
import thts.testing.testsuitehelper.TestSet;
import thts.testing.testsuitehelper.TestSuite;
import thts.testing.testsuitehelper.TestSuiteReadWrite;
import thts.treesearch.configs.Configuration;
import thts.treesearch.configs.RunConfiguration;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

//PRISM_MAINCLASS=thts.testing.RunBenchmarkTests prism/bin/prism
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
        String[] testSuites = {"Failstates","Goals","Robots"};
        for (int i = 0; i < configs.size(); i++) {
            for (int j = 0; j<testSuites.length; j++) {

                String fts = testSuites[j];
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
    public void getAllTestSuiteHours()
    {
        long totalTime = 0;
        String[] testSuites = {"Failstates","Goals","Robots"};
        for (int j = 0; j<testSuites.length; j++) {

            String fts = testSuites[j];
            long ftstime = getTestSuiteHours(filteredTestSuites.get(fts));
            totalTime+=ftstime;
            String x = String.format("%s: ms %d s %d min %d hours %d",fts,ftstime, TimeUnit.SECONDS.convert(ftstime,TimeUnit.MILLISECONDS)
            ,TimeUnit.MINUTES.convert(ftstime, TimeUnit.MILLISECONDS),TimeUnit.HOURS.convert(ftstime,TimeUnit.MILLISECONDS));
            System.out.println(x);
        }
        String x = String.format("Total: ms %d s %d min %d hours %d",totalTime, TimeUnit.SECONDS.convert(totalTime,TimeUnit.MILLISECONDS)
                ,TimeUnit.MINUTES.convert(totalTime, TimeUnit.MILLISECONDS),TimeUnit.HOURS.convert(totalTime,TimeUnit.MILLISECONDS));
        System.out.println(x);
    }
    public long getTestSuiteHours(TestSuite ts)
    {
        int i = 0;
        long testSuiteHours = 0;
        for (String testSetID : ts.testSets.keySet()) {
            TestSet testSet = ts.getTestSet(testSetID);
            if (testSet.subtestconfigs == null) {
                testSet.generateSubTestConfigs();
            }
            System.out.println(ts.suitID);
            testSuiteHours+=getTestSetHours(testSet,ts.suitID);
            i++;
        }
        return testSuiteHours;
    }
    long getTestSetHours(TestSet testSet,String testSuiteID)
    {
        ArrayList<TestSuiteReadWrite> subtestset = testSet.tests;
        int numTests = subtestset.size();
        long testSetTime = 0;

        for (int i = 0; i < numTests; i++) {
            TestSuiteReadWrite singleTest = subtestset.get(i);
            String configID = testSet.getConfigID(singleTest);
            if (!testSuiteID.contentEquals("Failstates")) {
                if (singleTest.fsp < 90) {
                  //  System.out.print("Running Test " + i + "/" + numTests + " " + configID + " : " + filename + "\n");
                    continue;
                }
            }
            System.out.println(configID);
            testSetTime+=testSet.getMeanSubConfigTime(singleTest);
        }
        return testSetTime;
    }

    public static void main(String[] args) {
        RunBenchmarkTests rbt = new RunBenchmarkTests();
        try {
            rbt.runTestSuite();
          //  rbt.getAllTestSuiteHours();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
