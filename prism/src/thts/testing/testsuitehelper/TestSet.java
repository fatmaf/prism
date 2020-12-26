package thts.testing.testsuitehelper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

public class TestSet {

    final static long DEFAULTTIMEINMS=30*60*1000;
    public String testSetID;
    public ArrayList<TestSuiteReadWrite> tests;
    public String location;
    public HashMap<String,SubTestSetConfig> subtestconfigs;


    public TestSet(String fn) {
        setTestIDFromFileName(fn);
        tests = parseTestSetFile(fn);
    }

    public TestSet(){}
    public void addTest(TestSuiteReadWrite t)
    {
        if(tests==null)
            tests = new ArrayList<>();
        if(!tests.contains(t))
        tests.add(t);
        else
        {
            //get the time and pick the one with the max
            int tindex=tests.indexOf(t);
            tests.get(tindex).timeInMS = Math.max(tests.get(tindex).timeInMS,t.timeInMS);
            System.out.println("Multiple tests, just changing the max time on them.");
        }
    }
    public void setTestSetID(String id)
    {
        testSetID = id;
    }
    public String setTestIDFromFileName(String fn) {
        String[] splitStrings = fn.split("/");

        testSetID = splitStrings[splitStrings.length - 1].split("_")[0];
        return testSetID;
    }

    public String getTestSetID() {
        return testSetID;
    }

    public ArrayList<TestSuiteReadWrite> parseTestSetFile(String fn) {
        ArrayList<TestSuiteReadWrite> testsList = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(fn));
            String line = br.readLine();
            line = br.readLine();
            while (line != null) {
                TestSuiteReadWrite t = new TestSuiteReadWrite();
//                System.out.println(line);
                t.parseTestFile(line);
                testsList.add(t);
                //each line is a test //except the first line
                line = br.readLine();
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return testsList;
    }

    public void findAllTestFiles(String loc)
    {
        for(TestSuiteReadWrite t: tests)
        {
            System.out.println(t.id);
            t.findTestFiles(loc);
        }
    }
    public ArrayList<String> getStrings()
    {
        ArrayList<String> toret = new ArrayList<>();
        for(TestSuiteReadWrite t: tests)
        {

            toret.add(testSetID+ TestSuiteReadWrite.delim+t.getString());
        }
        return toret;
    }

    public void generateSubTestConfigs()
    {
        subtestconfigs = new HashMap<>();
        for (TestSuiteReadWrite t: tests)
        {
            String id = SubTestSetConfig.getID(t.numRobots,t.numGoals,t.fsp,t.numdoors);
            SubTestSetConfig c ;
            if(subtestconfigs.containsKey(id))
            {
                c = subtestconfigs.get(id);
            }
            else {
                c = new SubTestSetConfig(t.numRobots,t.numGoals,t.fsp,t.numdoors);
                subtestconfigs.put(id,c);
            }
            c.addTest(t);
        }
        for(String id: subtestconfigs.keySet())
        {
            subtestconfigs.get(id).setMeanTestTimes();
        }

    }
    public long getMeanSubConfigTime(TestSuiteReadWrite t)
    {
        String id = SubTestSetConfig.getID(t.numRobots,t.numGoals,t.fsp,t.numdoors);
        if(subtestconfigs==null)
            generateSubTestConfigs();

            if(subtestconfigs.containsKey(id))
            {
                return subtestconfigs.get(id).getMeanTestTimes();
            }
            else
                return DEFAULTTIMEINMS;

    }

    public String getConfigID(TestSuiteReadWrite t)
    {
        String id = SubTestSetConfig.getID(t.numRobots,t.numGoals,t.fsp,t.numdoors);
        return id;
    }
}