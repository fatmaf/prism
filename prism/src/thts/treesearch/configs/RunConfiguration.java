package thts.treesearch.configs;

import thts.testing.testsuitehelper.TestFileInfo;
import thts.testing.testsuitehelper.TestSet;
import thts.testing.testsuitehelper.TestSuite;
import thts.testing.testsuitehelper.TestSuiteReadWrite;
import thts.treesearch.utils.Objectives;
import thts.treesearch.utils.THTSRunInfo;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class RunConfiguration {

    String currentDir;
    String testsLocation;
    String resultsLocation;
    String logFilesLocation;

    FileWriter fw;
    BufferedWriter bw;
    PrintWriter out;

    final String delim = "|";

    public void runTestSuite(TestSuite ts, Configuration config, boolean debug, String fnSuffix) throws Exception {
        int i = 0;
        for (String testSetID : ts.testSets.keySet()) {
            TestSet testSet = ts.getTestSet(testSetID);
            if (testSet.subtestconfigs == null) {
                testSet.generateSubTestConfigs();
            }


            System.out.println("\nRunning Test Set " + testSetID + " (" + i + "/" + ts.testSets.size() + ")");
            i++;
            runTestSet(testSet, config, debug, fnSuffix, ts.suitID);

        }
    }

    void runTestSet(TestSet testSet, Configuration config, boolean debug, String reslogSuffix, String testSuiteID) throws Exception {

        String propsuffix = "mult";
        String testLoc = testSet.location;
        String resFolderExt = "benchmarks/" + testSet.testSetID.replace(" ", "_") + "/";
        String logFilesExt = "results/configs/" + config.getConfigname() + "/";
        String resFileName = testSet.testSetID + "_" + config.getConfigname() + reslogSuffix;
        initialiseResultsLocations(resFolderExt, logFilesExt);

        ArrayList<TestSuiteReadWrite> subtestset = testSet.tests;
        if (!openResultsFile(resFileName))
            printResultsHeader();
        closeResultsFile();
        int numTests = subtestset.size();
        for (int i = 0; i < numTests; i++) {
            TestSuiteReadWrite singleTest = subtestset.get(i);
            String filename = singleTest.id;
            int numRobots = singleTest.numRobots;
            int numGoals = singleTest.numGoals;
            int fsp = singleTest.fsp;
            int numDoors = singleTest.numdoors;
            String configID = testSet.getConfigID(singleTest);
            if (!testSuiteID.contentEquals("Failstates")) {
                if (fsp < 90) {
                    System.out.print("Running Test " + i + "/" + numTests + " " + configID + " : " + filename + "\n");
                    continue;
                }
            }

            TestFileInfo tfi = new TestFileInfo(testLoc, filename, propsuffix, testLoc, singleTest.numModels,
                    fsp, numDoors);
            tfi.setGoals(singleTest.goalsList);
            tfi.setRobots(singleTest.robotsList);
            config.setTimeTimeLimitInMS(testSet.getMeanSubConfigTime(singleTest));
            System.out.println("Setting max time to " + config.getTimeTimeLimitInMS() + "ms (" + TimeUnit.MINUTES.convert(config.getTimeTimeLimitInMS(),
                    TimeUnit.MILLISECONDS) + " min)");

            System.out.print("Running Test " + i + "/" + numTests + " " + configID + " : " + filename + "\n");
            long startTime = System.currentTimeMillis();
            THTSRunInfo rinfo = config.run(tfi, getLogFilesLocation(), debug, i, testSuiteID + "_" + configID);
            rinfo.setNumDoors(numDoors);
            rinfo.setNumRobots(numRobots);
            rinfo.setNumGoals(numGoals);
            rinfo.setFsp(fsp);
            long endTime = System.currentTimeMillis();
            openResultsFile(resFileName);
            printResult(config, i, rinfo, endTime - startTime);
            closeResultsFile();
        }
    }

    public void run(String resFolderExt, Configuration config, int numRobots, int numGoals,
                    String filename, boolean debug,
                    String fnSuffix, String propsuffix,
                    int maxRuns, int fsp, int numdoors) throws Exception {
        //formaking things pretty
        int numConsoleChars = 80;
        int numCharsSoFar = 0;

        String logFilesExt = "results/configs/" + config.getConfigname() + "/";
        String resFileName = filename + "_" + config.getConfigname() + fnSuffix;
        initialiseResultsLocations(resFolderExt, logFilesExt);

        TestFileInfo tfi = new TestFileInfo(testsLocation, filename, propsuffix, testsLocation,
                numRobots, fsp, numdoors);
        if (!openResultsFile(resFileName))
            printResultsHeader();
        closeResultsFile();

        for (int i = 0; i < maxRuns; i++) {
            String outputString = String.format("Running Test %4d/%4d%4s", i, maxRuns, "");
            numCharsSoFar += outputString.length();
            if (numCharsSoFar > numConsoleChars) {
                numCharsSoFar = 0;
                outputString = "\n" + outputString;
            }
            System.out.print(outputString);
            long startTime = System.currentTimeMillis();

            THTSRunInfo rinfo = config.run(tfi, getLogFilesLocation(), debug, i, "");
            rinfo.setNumRobots(numRobots);
            rinfo.setNumGoals(numGoals);
            rinfo.setFsp(fsp);
            long endTime = System.currentTimeMillis();
            openResultsFile(resFileName);
            printResult(config, i, rinfo, endTime - startTime);
            closeResultsFile();
        }

    }

    public void run(String resFolderExt, Configuration config, int numRobots, int numGoals,
                    String filename, boolean debug,
                    String fnSuffix,
                    int maxRuns) throws Exception {

        String propsuffix = "_mult";
        run(resFolderExt, config, numRobots, numGoals, filename, debug, fnSuffix, propsuffix, maxRuns, 0, 0);


    }

    void initialiseResultsLocations(String resFolderext, String logFilesExt) {
        currentDir = System.getProperty("user.dir");
        if (currentDir.contains("/prism/prism"))
            currentDir = currentDir.replace("/prism/", "/");
        testsLocation = currentDir + "/tests/wkspace/" + resFolderext;
        logFilesLocation = testsLocation + logFilesExt;
        resultsLocation = testsLocation + "results/configs/csvs/";
        createDirIfNotExist(logFilesLocation);
        createDirIfNotExist(resultsLocation);

    }

    void printResultsHeader() {
        String header = "\nConfiguration" + delim +
                "UseSASH" + delim +
                "UseActSelForBU" + delim +
                "FSP" + delim +
                "Robots" + delim +
                "Goals" + delim +
                "FN" + delim +
                "Epsilon" + delim +
                "TC_U" + delim +
                "TC_L" + delim +
                "C_U" + delim +
                "C_L" + delim
                + "Solved" + delim +
                "Goal" + delim +
                "ProbGoal" + delim +
                "NumRollouts" + delim +
                "SOError" + delim +
                "VI_TC_GAC" + delim +
                "VI_C_GAC" + delim +
                "VI_P_GAC" + delim +
                "PolVI_GAC_EarlyTerm" + delim +
                "VI_TC_MVAC" + delim +
                "VI_C_MVAC" + delim +
                "VI_P_MVAC" + delim +
                "PolVI_MVAC_EarlyTerm" + delim +
                "TimeBound" + delim +
                "TimeLimit" + delim +
                "THTSTimeTaken" + delim +
                "MaxTLen" + delim +
                "MinTLen" + delim +
                "AvgTLen" + delim +
                "DNExp" + delim +
                "CNExp" + delim +
                "TotalTime" + delim +
                "VIPolAtIntervals" + delim +
                "TLens";

        if (out != null)
            out.println(header);

    }

    void printResult(Configuration config, int run, THTSRunInfo rinfo,
                     long totalTime) {
        String resLine = config.getConfigname() + "" + delim
                + config.isUseSASH() + "" + delim
                + config.isUseActSelForBackupUpdate() + "" + delim
                + rinfo.getFsp() + "" + delim
                + rinfo.getNumRobots() + "" + delim
                + rinfo.getNumGoals() + "" + delim
                + run + "" + delim
                + config.getEgreedy() + "" + delim
                + rinfo.getBoundsString(Objectives.TaskCompletion, "" + delim) + "" + delim
                + rinfo.getBoundsString(Objectives.Cost, "" + delim) + "" + delim
                + rinfo.isInitialStateSolved() + "" + delim
                + rinfo.isGoalFound() + "" + delim +
                rinfo.isGoalOnProbablePath() + "" + delim +
                rinfo.getNumRolloutsTillSolved() + "" + delim
                + rinfo.isStackoverflowerror() + "" + delim +
                rinfo.getVIPolGreedyActSelInfo(Objectives.TaskCompletion) + "" + delim
                + rinfo.getVIPolGreedyActSelInfo(Objectives.Cost) + "" + delim
                + rinfo.getVIPolGreedyActSelInfo(Objectives.Probability) + "" + delim
                + rinfo.isViPolGreedyActSelTerminatedEarly() + delim +
                rinfo.getVIPolMostVisActSelInfo(Objectives.TaskCompletion) + "" + delim
                + rinfo.getVIPolMostVisActSelInfo(Objectives.Cost) + "" + delim
                + rinfo.getVIPolMostVisActSelInfo(Objectives.Probability) + "" + delim
                + rinfo.isViPolMostVisActSelTerminatedEarly() + delim
                + rinfo.isTimeLimited() + "" + delim
                + rinfo.getMaxTimeLimit() +
                "" + delim + rinfo.getDuration() +
                "" + delim + rinfo.getMaxTrialLen()
                + "" + delim + rinfo.getMinTrialLen() +
                "" + delim + rinfo.getAverageTrialLen() +
                "" + delim + rinfo.getDecisionNodesExp() + "" + delim
                + rinfo.getChanceNodesExp() + "" + delim
                + totalTime + "" + delim
                + rinfo.getVIPolIntervalString() +
                "" + delim + rinfo.gettLensString()
                + "" + delim + rinfo.isViPolGreedyActSelTerminatedEarly();
        if (out != null)
            out.println(resLine);
    }

    boolean openResultsFile(String fn) throws IOException {
        boolean check = new File(resultsLocation + fn + ".csv").exists();
        fw = new FileWriter(resultsLocation + fn + ".csv", true);
        bw = new BufferedWriter(fw);
        out = new PrintWriter(bw);
        return check;
    }

    void closeResultsFile() throws IOException {

        out.close();
        bw.close();
        fw.close();
    }


    public void createDirIfNotExist(String directoryName) {
        File directory = new File(directoryName);
        if (!directory.exists()) {

            directory.mkdirs();
        }

    }

    public String getCurrentDir() {
        return currentDir;
    }

    public void setCurrentDir(String currentDir) {
        this.currentDir = currentDir;
    }

    public String getTestsLocation() {
        return testsLocation;
    }

    public void setTestsLocation(String testsLocation) {
        this.testsLocation = testsLocation;
    }

    public String getResultsLocation() {
        return resultsLocation;
    }

    public void setResultsLocation(String resultsLocation) {
        this.resultsLocation = resultsLocation;
    }

    public String getLogFilesLocation() {
        return logFilesLocation;
    }

    public void setLogFilesLocation(String logFilesLocation) {
        this.logFilesLocation = logFilesLocation;
    }

    public FileWriter getFw() {
        return fw;
    }

    public void setFw(FileWriter fw) {
        this.fw = fw;
    }

    public BufferedWriter getBw() {
        return bw;
    }

    public void setBw(BufferedWriter bw) {
        this.bw = bw;
    }

    public PrintWriter getOut() {
        return out;
    }

    public void setOut(PrintWriter out) {
        this.out = out;
    }
}
