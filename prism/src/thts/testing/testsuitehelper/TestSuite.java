package thts.testing.testsuitehelper;

import java.util.ArrayList;
import java.util.HashMap;

public class TestSuite {
	public String suitID;
	public HashMap<String, TestSet> testSets;

	public boolean hasTestSet(String id) {
		boolean toret = false;
		if (testSets != null) {
			if (testSets.containsKey(id))
				toret = true;
		}
		return toret;
	}

	public TestSuite(String id) {
		suitID = id;
	}

	public void addTestSet(String tid, TestSet ts) {
		if (testSets == null)
			testSets = new HashMap<>();
		testSets.put(tid, ts);
	}

	public TestSet getTestSet(String tid) {
		TestSet ts = null;
		if (testSets != null) {
			if (testSets.containsKey(tid))
				ts = testSets.get(tid);
		}
		return ts;
	}

	public void findAllTestFiles(String loc) {
		for (String tid : testSets.keySet()) {
			System.out.println("Finding test files for " + tid);
			TestSet ts = testSets.get(tid);
			ts.findAllTestFiles(loc);

		}
	}

	public String getString() {
		String toret = "";
		for (String tid : testSets.keySet()) {
			TestSet ts = testSets.get(tid);
			ArrayList<String> tsStrings = ts.getStrings();
			for (String testString : tsStrings) {
				toret += this.suitID + TestSuiteReadWrite.delim + testString + "\n";
			}
		}
		return toret;
	}

}