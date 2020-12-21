package thts.testing.testsuitehelper;


import java.util.ArrayList;

public class SubTestSetConfig{
        int numGoals;
        int numRobots;
        int fsp;
        int numDoors;
        long meanSubTestTime;
        ArrayList<TestSuiteReadWrite> subTests;

        public void addTest(TestSuiteReadWrite t)
        {
            subTests.add(t);
        }
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
