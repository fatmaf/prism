package thts.testing.testsuitehelper;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class GetTestInfo {

	public HashMap<String, TestSuite> readTestsAndWriteToCSV() {

		// steps
		// read test files
		// parse each test file
		// each test file is a test set
		// each test set needs to be added to a test suite

		String testinfoloc = "/home/fatma/Data/PhD/code/stapussi_prelim/xkcdStyle/testInfo/";
		String testSetLoc = "/home/fatma/Data/PhD/ongit/generatedTests/wkspace/";
		HashMap<String, String> testsuiteids = new HashMap<>();
		testsuiteids.put("d", "Doors");
		testsuiteids.put("r", "Robots");
		testsuiteids.put("g", "Goals");
		testsuiteids.put("f", "Failstates");
		HashMap<String, TestSuite> testSuites = new HashMap<>();
		for (String testsuite_ids : testsuiteids.keySet()) {
			testSuites.put(testsuiteids.get(testsuite_ids), new TestSuite(testsuiteids.get(testsuite_ids)));
		}
		File folder = new File(testinfoloc);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {

				System.out.println(listOfFiles[i]);
				String fn = listOfFiles[i].getAbsolutePath();
				String name = listOfFiles[i].getName();
				name = name.replace(".csv", "");
				String ts_id = name.split("_")[1];
				TestSet ts = new TestSet(fn);
				TestSuite testSuite = testSuites.get(testsuiteids.get(ts_id));
				testSuite.addTestSet(ts.testSetID, ts);
			}
		}
		System.out.println("Done parsing test files\nFinding file locs now");
		// for each test suite find the files
		for (String ts_id : testSuites.keySet()) {
			System.out.println("Finding test files for suite: " + ts_id);
			TestSuite testSuite = testSuites.get(ts_id);
			testSuite.findAllTestFiles(testSetLoc);

		}
		try {
			PrintWriter writer = new PrintWriter(testinfoloc + "allTests/allTests.csv");
			String header = TestSuiteReadWrite.getCSVHeader();
			header = "TestSuite,TestSet," + header;
			writer.println(header);
			for (String ts_id : testSuites.keySet()) {
				String ts;
				TestSuite testSuite = testSuites.get(ts_id);
				ts = testSuite.getString();
				writer.print(ts);
			}
			writer.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return testSuites;
	}

	public HashMap<String, TestSuite> readTestSuitesFromCSV() {
		String testinfoloc = "./tests/";
		String fn = "allTests.csv";
		HashMap<String, TestSuite> testSuites = new HashMap<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(testinfoloc + fn));
			String line = br.readLine();
			line = br.readLine();
			while (line != null) {
				String[] vals = line.split(",");

				String testSuiteID = vals[0];
				if (!testSuites.containsKey(testSuiteID))
					testSuites.put(testSuiteID, new TestSuite(testSuiteID));
				TestSuite currentTestSuite = testSuites.get(testSuiteID);
				String testSetID = vals[1];
				TestSet ts;
				if (!currentTestSuite.hasTestSet(testSetID)) {
					ts = new TestSet();
					ts.setTestSetID(testSetID);
					currentTestSuite.addTestSet(testSetID, ts);
				}
				ts = currentTestSuite.getTestSet(testSetID);

				TestSuiteReadWrite t = new TestSuiteReadWrite();
				String testID = vals[2];
				t.id = testID;
				String testLoc = vals[3];
				t.location = testLoc;
				if (ts.location == null)
					ts.location = testLoc;
				else {
					if (!ts.location.contentEquals(testLoc)) {
						System.out.println(
								"Incorrect assumption about test set: old loc:" + ts.location + " new loc:" + testLoc);
					}
				}
				int numModelFiles = Integer.parseInt(vals[4]);
				t.numModels = numModelFiles;
				int numPropFiles = Integer.parseInt(vals[5]);
				t.numProps = numPropFiles;
				int currentValNum = 6;
				int maxValNum = currentValNum + numModelFiles;
				ArrayList<String> modelFiles = new ArrayList<>();
				while (currentValNum < maxValNum) {
					String mn = vals[currentValNum];
					mn = mn.replace("[", "");
					mn = mn.replace("]", "");
					modelFiles.add(mn);
					currentValNum++;
				}
				t.modelFiles = modelFiles;
				maxValNum = currentValNum + numPropFiles;
				ArrayList<String> propFiles = new ArrayList<>();
				while (currentValNum < maxValNum) {
					String mn = vals[currentValNum];
					mn = mn.replace("[", "");
					mn = mn.replace("]", "");
					propFiles.add(mn);
					currentValNum++;
				}
				t.propertiesFiles = propFiles;
				int numRobots = Integer.parseInt(vals[currentValNum++]);
				t.numRobots = numRobots;
				int numGoals = Integer.parseInt(vals[currentValNum++]);
				t.numGoals = numGoals;
				// numrobots first

				maxValNum = currentValNum + numRobots;
				ArrayList<Integer> robots = new ArrayList<>();
				if (numRobots != 0) {
					while (currentValNum < maxValNum) {
						String mn = vals[currentValNum];
						mn = mn.replace("[", "");
						mn = mn.replace("]", "");
						if (mn != "") {
							mn = mn.trim();
							robots.add(Integer.parseInt(mn));
						}
						currentValNum++;
					}
				} else
					currentValNum++;
				t.robotsList = robots;
				ArrayList<Integer> goals = new ArrayList<>();
				maxValNum = currentValNum + numGoals;
				if (numGoals != 0) {
					while (currentValNum < maxValNum) {
						String mn = vals[currentValNum];
						mn = mn.replace("[", "");
						mn = mn.replace("]", "");
						if (mn != "") {
							mn = mn.trim();
							goals.add(Integer.parseInt(mn));
						}
						currentValNum++;
					}
				} else
					currentValNum++;
				t.goalsList = goals;
				t.numdoors = Integer.parseInt(vals[currentValNum++]);
				t.fsp = Integer.parseInt(vals[currentValNum++]);
				t.timeInMS = Long.parseLong(vals[currentValNum]);
				ts.addTest(t);

				line = br.readLine();
			}

			br.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return testSuites;
	}

	public HashMap<String, TestSuite> filterTestSuitesForTest(String tid, HashMap<String, TestSuite> testSuites) {
		HashMap<String, TestSuite> filteredTestSuites = new HashMap<String, TestSuite>();
		for (String testSuiteID : testSuites.keySet()) {
			TestSuite testSuite = testSuites.get(testSuiteID);
			for (String testSetID : testSuite.testSets.keySet()) {
				if (testSetID.contentEquals(tid)) {
					if (!filteredTestSuites.containsKey(testSuiteID))
						filteredTestSuites.put(testSuiteID, new TestSuite(testSuiteID));
					TestSuite filteredTestSuite = filteredTestSuites.get(testSuiteID);
					if (!filteredTestSuite.hasTestSet(tid)) {
						TestSet ts = testSuite.getTestSet(tid);
						filteredTestSuite.addTestSet(tid, ts);
					}

				}
			}
		}
		return filteredTestSuites;
	}

	public static void main(String[] args) {

		GetTestInfo gti = new GetTestInfo();
        HashMap<String, TestSuite> testSuites= gti.readTestSuitesFromCSV();
	//	HashMap<String, TestSuite> testSuites = gti.readTestsAndWriteToCSV();

	}

}
