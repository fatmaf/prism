package thts.testing.testsuitehelper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class TestSet {
    public class SubTestSetConfig{
        int numGoals;
        int numRobots;
        int fsp;
        int numDoors;
        long meanSubTestTime;
        ArrayList<TestSuiteReadWrite> subTests;

        public SubTestSetConfig(int numRobots, int numGoals, int fsp, int numDoors)
        {
            this.numRobots = numRobots;
            this.numGoals = numGoals;
            this.numDoors = numDoors;
            this.fsp = fsp;
            subTests = new ArrayList<>();
        }
        public void setMeanTestTimes()
        {
            long sum = 0;
            for(TestSuiteReadWrite t: subTests)
            {
                sum+=t.timeInMS;
            }
            sum/=subTests.size();
            meanSubTestTime = sum;
        }
        public long getMeanTestTimes()
        {
            return meanSubTestTime;
        }
        @Override
        public boolean equals(Object o)
        {
            if (o == this)
                return true;
            else if (o instanceof  SubTestSetConfig)
            {
                SubTestSetConfig s1 = (SubTestSetConfig) o;
                return ((s1.numDoors==this.numDoors) && (s1.numGoals==this.numGoals) && (s1.numRobots==this.numRobots) && (s1.fsp == this.fsp));
            }
            else
                return false;
        }
        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + numDoors;
            result = prime * result + numGoals;
            result = prime * result + numRobots;
            result = prime * result + fsp;
            return result;

        }
        @Override
        public String toString()
        {
            return "r"+numRobots+"g"+numGoals+"f"+fsp+"d"+numDoors;
        }

        public static String getID(int numRobots,int numGoals,int fsp, int numDoors)
        {
            return "r"+numRobots+"g"+numGoals+"f"+fsp+"d"+numDoors;
        }
    }
    public String testSetID;
    public ArrayList<TestSuiteReadWrite> tests;
    public String location;



    public TestSet(String fn) {
        setTestIDFromFileName(fn);
        tests = parseTestSetFile(fn);
    }

    public TestSet(){}
    public void addTest(TestSuiteReadWrite t)
    {
        if(tests==null)
            tests = new ArrayList<>();
        tests.add(t);
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

    public void analyseTestSet()
    {
        ArrayList<SubTestSetConfig> subtestconfigs = new ArrayList<>();
        for (TestSuiteReadWrite t: tests)
        {
            SubTestSetConfig c = new SubTestSetConfig(t.numRobots,t.numGoals,t.fsp,t.numdoors);

        }

    }
}