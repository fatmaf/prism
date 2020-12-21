package thts.treesearch.configs;

import thts.testing.testsuitehelper.TestFileInfo;
import thts.testing.testsuitehelper.TestSet;
import thts.testing.testsuitehelper.TestSuite;
import thts.testing.testsuitehelper.TestSuiteReadWrite;
import thts.treesearch.utils.Objectives;
import thts.treesearch.utils.THTSRunInfo;

import java.io.*;
import java.util.ArrayList;

public class RunConfiguration {

    String currentDir;
    String testsLocation;
    String resultsLocation;
    String logFilesLocation;

    FileWriter fw;
    BufferedWriter bw;
    PrintWriter out;

    public void runTestSuite(TestSuite ts,Configuration config,boolean debug,String fnSuffix) throws Exception
    {
        int i = 0;
        for(String testSetID : ts.testSets.keySet())
        {
            TestSet testSet = ts.getTestSet(testSetID);
            if (testSet.subtestconfigs==null)
            {
                testSet.generateSubTestConfigs();
            }
            System.out.println("\nRunning Test Set "+testSetID+" ("+i+"/"+ts.testSets.size()+")");
            i++;
            runTestSet(testSet,config,debug,fnSuffix,ts.suitID);

        }
    }
    void runTestSet(TestSet testSet,Configuration config,boolean debug,String reslogSuffix,String testSuiteID) throws Exception
    {

        String propsuffix = "mult";
        String testLoc = testSet.location;
        String resFolderExt = "benchmarks/"+testSet.testSetID.replace(" ","_") + "/";
        String logFilesExt = "results/configs/" + config.getConfigname() + "/";
        String resFileName = testSet.testSetID+ "_" + config.getConfigname() + reslogSuffix;
        initialiseResultsLocations(resFolderExt, logFilesExt);

        ArrayList<TestSuiteReadWrite> subtestset = testSet.tests;
        if (!openResultsFile(resFileName))
            printResultsHeader();
        closeResultsFile();
        int numTests = subtestset.size();
        for (int i = 0; i<numTests; i++) {
            TestSuiteReadWrite singleTest = subtestset.get(i);
            String filename = singleTest.id;
            int numRobots = singleTest.numRobots;
            int numGoals = singleTest.numGoals;
            int fsp = singleTest.fsp;
            int numDoors = singleTest.numdoors;


                if(config.isCategory(ConfigCategory.COST))
                {

                   if(fsp < 90) {
//                       if(!config.isCategory(ConfigCategory.RELATIVECOST)) {
                           System.out.print("Skipping Test " + i + "/" + numTests + " " + testSet.getConfigID(singleTest) + " : " + filename + "\n");
                           continue;
//                       }
                   }
                }

            TestFileInfo tfi = new TestFileInfo(testLoc,filename,propsuffix,testLoc,numRobots,
                   fsp,numDoors );
            config.setTimeTimeLimitInMS(testSet.getMeanSubConfigTime(singleTest));
            String configID = testSet.getConfigID(singleTest);
            System.out.print("Running Test "+i+"/"+numTests+" "+configID+" : "+filename+"\n");
            long startTime = System.currentTimeMillis();
            THTSRunInfo rinfo = config.run(tfi, getLogFilesLocation(), debug, i,testSuiteID+"_"+configID);
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
                    String fnSuffix,String propsuffix,
                    int maxRuns,int fsp,int numdoors) throws Exception
    {
        String logFilesExt = "results/configs/" + config.getConfigname() + "/";
        String resFileName = filename + "_" + config.getConfigname() + fnSuffix;
        initialiseResultsLocations(resFolderExt, logFilesExt);

        TestFileInfo tfi = new TestFileInfo(testsLocation, filename, propsuffix, testsLocation,
                numRobots, fsp,numdoors);
        if (!openResultsFile(resFileName))
            printResultsHeader();
        closeResultsFile();
        for (int i = 0; i < maxRuns; i++) {
            System.out.print("Running Test "+i+"/"+maxRuns+"\t");
            long startTime = System.currentTimeMillis();

            THTSRunInfo rinfo = config.run(tfi, getLogFilesLocation(), debug, i,"");
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
        run(resFolderExt,config,numRobots,numGoals,filename,debug,fnSuffix,propsuffix,maxRuns,0,0);


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
        String header = "\nConfiguration\t" +
                "UseSASH\t" +
                "UseActSelForBU\t" +
                "FSP\t" +
                "Robots\t" +
                "Goals\t" +
                "FN\t" +
                "Epsilon\t" +
                "TC_U\t" +
                "TC_L\t" +
                "C_U\t" +
                "C_L\t"
                + "Solved\t" +
                "Goal\t" +
                "ProbGoal\t" +
                "NumRollouts\t" +
                "SOError\t" +
                "VI_TC\t" +
                "VI_C\t" +
                "VI_P\t"
                + "TimeBound\t" +
                "TimeLimit\t" +
                "THTSTimeTaken\t" +
                "MaxTLen\t" +
                "MinTLen\t" +
                "AvgTLen\t" +
                "DNExp\t" +
                "CNExp\t" +
                "TotalTime\t" +
                "VIPolAtIntervals\t" +
                "TLens";
        if (out != null)
            out.println(header);

    }

    void printResult(Configuration config, int run, THTSRunInfo rinfo,
                     long totalTime) {
        String resLine = config.getConfigname() + "\t"
                + config.isUseSASH() + "\t"
                + config.isUseActSelForBackupUpdate() + "\t"
                + rinfo.getFsp() + "\t"
                + rinfo.getNumRobots() + "\t"
                + rinfo.getNumGoals() + "\t"
                + run + "\t"
                + config.getEgreedy() + "\t"
                + rinfo.getBoundsString(Objectives.TaskCompletion, "\t") + "\t"
                + rinfo.getBoundsString(Objectives.Cost, "\t") + "\t"
                + rinfo.isInitialStateSolved() + "\t"
                + rinfo.isGoalFound() + "\t" +
                rinfo.isGoalOnProbablePath() + "\t" +
                rinfo.getNumRolloutsTillSolved() + "\t"
                + rinfo.isStackoverflowerror() + "\t" +
                rinfo.getviInfo(Objectives.TaskCompletion) + "\t"
                + rinfo.getviInfo(Objectives.Cost) + "\t"
                + rinfo.getviInfo(Objectives.Probability) + "\t"
                + rinfo.isTimeLimited() + "\t"
                + rinfo.getMaxTimeLimit() +
                "\t" + rinfo.getDuration() +
                "\t" + rinfo.getMaxTrialLen()
                + "\t" + rinfo.getMinTrialLen() +
                "\t" + rinfo.getAverageTrialLen() +
                "\t" + rinfo.getDecisionNodesExp() + "\t"
                + rinfo.getChanceNodesExp() + "\t"
                + totalTime + "\t"
                + rinfo.getVIPolIntervalString() +
                "\t" + rinfo.gettLensString();
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
