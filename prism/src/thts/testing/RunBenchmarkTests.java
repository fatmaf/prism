package thts.testing;


import thts.testing.testsuitehelper.GetTestInfo;
import thts.testing.testsuitehelper.TestSet;
import thts.testing.testsuitehelper.TestSuite;
import thts.testing.testsuitehelper.TestSuiteReadWrite;
import thts.treesearch.configs.Configuration;
import thts.treesearch.configs.RunConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

//PRISM_MAINCLASS=thts.testing.RunBenchmarkTests prism/bin/prism
public class RunBenchmarkTests {

    HashMap<String, TestSuite> filteredTestSuitesFSP90R8G8;
    HashMap<String, TestSuite> filteredTestSuites;
    HashMap<String, TestSuite> filteredTestSuitesFSP90R4G4;

    public RunBenchmarkTests() {

        GetTestInfo testInfo = new GetTestInfo();
        String tnid = "Warehouse Shelf to Depot";
        String doorstnid = "Warehouse With Doors Shelf to Depot";
        HashMap<String, TestSuite> testSuites = testInfo.readTestSuitesFromCSV();
        filteredTestSuites = testInfo.filterTestSuitesFSP90ExceptFailstates(tnid, doorstnid, testSuites);
        filteredTestSuitesFSP90R4G4 = testInfo.filterTestSuitesFSP90R4G4(tnid, testSuites);
        filteredTestSuitesFSP90R8G8 = testInfo.filterTestSuitesFSP90R8G8(tnid, testSuites);

    }

    public static ArrayList<Configuration> getSelectedConfigs(boolean timeBound, boolean dointervalvi, long timeLimit) {
        String[] configNames = {
                //"L_Cost_LUGreedyRandomRelFC_MCD_GP_ASBU",
                //"L_Cost_eLUGreedyRandomRelFC_MCD_GP_ASBU",
                //"L_Cost_LUGreedyRandomRelFC_GP_ASBU",
                //"L_Cost_LUGreedyRandomRelFC_GP_SASH_ASBU",
                "L_Cost_LUGreedyRandomRelFC_MCD_GP_SASH_ASBU",

                //"L_Cost_eLUGreedyRandomRelFC_MCD_GP_SASH_ASBU",
                //"L_Cost_eLUGreedyRandomRelFC_GP_SASH_ASBU",
                //"L_Cost_eLUGreedyRandomRelFC_GP_ASBU",
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

    public void runFailstatesWithNoTimeBound() throws Exception {
        boolean timeBound = false;
        boolean dointervalvi = false;
        long timeLimit = 30 * 60 * 1000;
        boolean debug = false;
        ArrayList<Configuration> configs = getSelectedConfigs(timeBound, dointervalvi, timeLimit);
        String[] testSuites = {"Failstates"};
        long fixedTimeLimit = 180 * 60 * 1000;
        for (int i = 0; i < configs.size(); i++) {
            for (int j = 0; j < testSuites.length; j++) {

                String fts = testSuites[j];
                String fnSuffix = fts + "_not_time_bound";

                Configuration config = configs.get(i);
                System.out.println("\nRunning configuration " + config.getConfigname() + " - " + i + "/" + configs.size() + " on test suite " + fts + "\n");
                RunConfiguration runconfig = new RunConfiguration();

                try {
                    runconfig.runTestSuite(filteredTestSuites.get(fts), config, debug, fnSuffix, fixedTimeLimit);
                } catch (Exception e) {
                    throw e;// e.printStackTrace();
                }
            }
        }
    }

    public void runTest2hLimit() throws Exception {
        boolean timeBound = true;
        boolean dointervalvi = true;
        long timeLimit = 120 * 60 * 1000;
        boolean debug = false;
        ArrayList<Configuration> configs = getSelectedConfigs(timeBound, dointervalvi, timeLimit);
        String[] testSuites = {"Failstates"};
        long fixedTimeLimit = timeLimit;
        for (int j = 0; j < testSuites.length; j++) {
            for (int i = 0; i < configs.size(); i++) {


                String fts = testSuites[j];
                String fnSuffix = fts + "_tl2h";

                Configuration config = configs.get(i);

                config.setJustLogs(false);

                System.out.println("\nRunning configuration " + config.getConfigname() + " - " + i + "/" + configs.size() + " on test suite " + fts + "\n");
                RunConfiguration runconfig = new RunConfiguration();

                try {
                    runconfig.runTestSuite(filteredTestSuitesFSP90R4G4.get(fts), config, debug, fnSuffix, fixedTimeLimit);
                } catch (Exception e) {
                    throw e;// e.printStackTrace();
                }
            }
        }
    }

    public void runTestSuite() throws Exception {
        boolean timeBound = true;
        boolean dointervalvi = false;
        long timeLimit = 30 * 60 * 1000;
        boolean debug = false;
        ArrayList<Configuration> configs = getSelectedConfigs(timeBound, dointervalvi, timeLimit);
        String[] testSuites = {"Failstates", "Goals", "Robots"};
        long fixedTimeLimit = 0;
        for (int j = 0; j < testSuites.length; j++) {
            for (int i = 0; i < configs.size(); i++) {


                String fts = testSuites[j];
                String fnSuffix = fts;

                Configuration config = configs.get(i);
                config.setJustLogs(true);

                System.out.println("\nRunning configuration " + config.getConfigname() + " - " + i + "/" + configs.size() + " on test suite " + fts + "\n");
                RunConfiguration runconfig = new RunConfiguration();

                try {
                    runconfig.runTestSuite(filteredTestSuites.get(fts), config, debug, fnSuffix, fixedTimeLimit);
                } catch (Exception e) {
                    throw e;// e.printStackTrace();
                }
            }
        }
    }


    public void runTestSuiteDoors() throws Exception {
        boolean timeBound = true;
        boolean dointervalvi = false;
        long timeLimit = 30 * 60 * 1000;
        boolean debug = false;
        ArrayList<Configuration> configs = getSelectedConfigs(timeBound, dointervalvi, timeLimit);
        String[] testSuites = {"Doors"};
        long fixedTimeLimit = 0;
        for (int j = 0; j < testSuites.length; j++) {
            for (int i = 0; i < configs.size(); i++) {


                String fts = testSuites[j];
                String fnSuffix = fts;

                Configuration config = configs.get(i);
                config.setJustLogs(true);

                System.out.println("\nRunning configuration " + config.getConfigname() + " - " + i + "/" + configs.size() + " on test suite " + fts + "\n");
                RunConfiguration runconfig = new RunConfiguration();

                try {
                    runconfig.runTestSuite(filteredTestSuites.get(fts), config, debug, fnSuffix, fixedTimeLimit);
                } catch (Exception e) {
                    throw e;// e.printStackTrace();
                }
            }
        }
    }

    public void findProblemWithRobots() throws Exception {
        boolean timeBound = true;
        boolean dointervalvi = false;
        long timeLimit = 30 * 60 * 1000;
        boolean debug = false;
        ArrayList<Configuration> configs = getSelectedConfigs(timeBound, dointervalvi, timeLimit);
        String[] testSuites = {"Goals", "Robots"};//{"Failstates", "Goals", "Robots"};
        long fixedTimeLimit = 0;
        for (int j = 0; j < testSuites.length; j++) {
            for (int i = 0; i < configs.size(); i++) {


                String fts = testSuites[j];
                String fnSuffix = fts + "_r8g8problems";

                Configuration config = configs.get(i);

                config.setJustLogs(false);


                System.out.println("\nRunning configuration " + config.getConfigname() + " - " + i + "/" + configs.size() + " on test suite " + fts + "\n");
                RunConfiguration runconfig = new RunConfiguration();

                try {
                    runconfig.runTestSuite(filteredTestSuitesFSP90R8G8.get(fts), config, debug, fnSuffix, fixedTimeLimit);
                } catch (Exception e) {
                    throw e;// e.printStackTrace();
                }
            }
        }
    }

    public void getAllTestSuiteHours() {
        long totalTime = 0;
        String[] testSuites = {"Failstates", "Goals", "Robots"};
        for (int j = 0; j < testSuites.length; j++) {

            String fts = testSuites[j];
            long ftstime = getTestSuiteHours(filteredTestSuites.get(fts));
            totalTime += ftstime;
            String x = String.format("%s: ms %d s %d min %d hours %d", fts, ftstime, TimeUnit.SECONDS.convert(ftstime, TimeUnit.MILLISECONDS)
                    , TimeUnit.MINUTES.convert(ftstime, TimeUnit.MILLISECONDS), TimeUnit.HOURS.convert(ftstime, TimeUnit.MILLISECONDS));
            System.out.println(x);
        }
        String x = String.format("Total: ms %d s %d min %d hours %d", totalTime, TimeUnit.SECONDS.convert(totalTime, TimeUnit.MILLISECONDS)
                , TimeUnit.MINUTES.convert(totalTime, TimeUnit.MILLISECONDS), TimeUnit.HOURS.convert(totalTime, TimeUnit.MILLISECONDS));
        System.out.println(x);
    }

    public long getTestSuiteHours(TestSuite ts) {
        int i = 0;
        long testSuiteHours = 0;
        for (String testSetID : ts.testSets.keySet()) {
            TestSet testSet = ts.getTestSet(testSetID);
            if (testSet.subtestconfigs == null) {
                testSet.generateSubTestConfigs();
            }
            System.out.println(ts.suitID);
            testSuiteHours += getTestSetHours(testSet, ts.suitID);
            i++;
        }
        return testSuiteHours;
    }

    long getTestSetHours(TestSet testSet, String testSuiteID) {
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
            testSetTime += testSet.getMeanSubConfigTime(singleTest);
        }
        return testSetTime;
    }

    public static void main(String[] args) {
        RunBenchmarkTests rbt = new RunBenchmarkTests();
        try {
            if (args.length > 0) {
                if (args[0].contentEquals("d"))
                    rbt.runTestSuiteDoors();
                else if (args[0].contentEquals("a"))
                    rbt.runTestSuite();
                else if ((args[0].contentEquals("l")))
                    rbt.runTest2hLimit();
                else if (args[0].contentEquals("p"))
                    rbt.findProblemWithRobots();
                else {
                    System.out.println(String.format("Options are\n\t%s:%s\t%s:%s\t%s:%s\t%s:%s", "d", "Doors", "a", "All Tests with fsp 90 except failstates, excludes doors", "l", "Limit each test to 2hours", "p", "Run the thing with problems"));
                    System.out.println("Running limit tests");
                    rbt.runTest2hLimit();
                }
            } else {
                //                System.out.println(String.format("Options are\n\t%s:%s\t%s:%s\t%s:%s", "d","Doors","a","All Tests with fsp 90 except failstates, excludes doors","l","Limit each test to 2hours"));
                System.out.println(String.format("Options are\n\t%s:%s\t%s:%s\t%s:%s\t%s:%s", "d", "Doors", "a", "All Tests with fsp 90 except failstates, excludes doors", "l", "Limit each test to 2hours", "p", "Run the thing with problems"));
                System.out.println("Running limit tests");

                rbt.runTest2hLimit();
// rbt.runTestSuite();
            }
            //    rbt.runTest2hLimit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
