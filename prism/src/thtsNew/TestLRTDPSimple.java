package thtsNew;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import acceptance.AcceptanceOmega;
import acceptance.AcceptanceType;
import automata.DA;
import explicit.LTLModelChecker;
import parser.State;
import parser.ast.Expression;
import parser.ast.ExpressionProb;
import parser.ast.ExpressionQuant;
import parser.ast.ExpressionReward;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import prism.DefaultModelGenerator;
import prism.Prism;
import prism.PrismDevNullLog;
import prism.PrismException;
import prism.PrismFileLog;
import prism.PrismLog;
import prism.RewardGenerator;
import simulator.ModulesFileModelGenerator;
import thts.Objectives;

//a class that tests LRTDP and THTS
public class TestLRTDPSimple {
//
	//// PRISM_MAINCLASS=thtsNew.TestLRTDPSimple prism/bin/prism
	// testing trialbasedtree search with just an mdp
	// have the default product model generator
	public void createDirIfNotExist(String directoryName) {
		File directory = new File(directoryName);
		if (!directory.exists()) {
			directory.mkdirs();
			// If you require it to make the entire directory path including parents,
			// use directory.mkdirs(); here instead.
		}

	}

	public void runTests() throws Exception {
		String goalsNotFound = "";
		String notSolved = "";
		int tests = 1;
		int passed = 0;
		int goalFound = 0;
		int solved = 0;
		boolean debug = false;

		int[] goalStates = { 3, 6 };

		String[] examples = { "tro_example_new_small_noprob", "tro_example_new_small_onefailaction" };

		tests = tests + goalStates.length * examples.length;

		String[] examples2 = { "tro_example_new_small_noprob", "tro_example_new_small_onefailaction",
				"tro_example_new_small_allfailpaths_nowait", "tro_example_new_small_allfailpaths" };

		tests = tests + (goalStates.length * examples2.length) * 2;

		String[] examples3 = examples2;// { "tro_example_new_small_allfailpaths_nowait",
										// "tro_example_new_small_allfailpaths" };
		tests = tests + (goalStates.length * examples3.length) * 2;

		int[] goalStates2 = { 0, 4, 6 };
		tests = tests + goalStates2.length * 2;

		int currentTest = 1;

		System.out.println("Test " + currentTest++ + "/" + tests);
		boolean[] goalFoundAndSolved = gssp(debug);

		if (goalFoundAndSolved[0])
			goalFound++;
		else
			goalsNotFound += "GSSP\n";
		if (goalFoundAndSolved[1])
			solved++;
		else
			notSolved += "GSSP\n";
		if (goalFoundAndSolved[0] && goalFoundAndSolved[1])
			passed++;

		String infoString = "";
		infoString = " lrtdp no deadends\n";
		for (String example : examples) {
			for (int g : goalStates) {
				System.out.println("Test " + currentTest++ + "/" + tests);
				THTSRunInfo rinfo = simpleLRTDPNoDeadends(example, g, debug);
				if (rinfo.goalFound)
					goalFound++;
				else
					goalsNotFound += example + infoString;
				if (rinfo.initialStateSolved)
					solved++;
				else
					notSolved += example + infoString;
				if (rinfo.goalFoundAndSolved())
					passed++;

			}
		}

		infoString = " lrtdp with deadends\n";
		for (String example : examples2) {
			for (int g : goalStates) {
//with deadends	
				System.out.println("Test " + currentTest++ + "/" + tests);
				THTSRunInfo rinfo = simpleLRTDP(example, g, debug);
				if (rinfo.goalFound)
					goalFound++;
				else
					goalsNotFound += example + infoString;
				if (rinfo.initialStateSolved)
					solved++;
				else
					notSolved += example + infoString;
				if (rinfo.goalFoundAndSolved())
					passed++;

			}
		}

		infoString = " lrtdp with deadends and avoid\n";
		for (String example : examples2) {
			for (int g : goalStates) {
//with deadends	
				System.out.println("Test " + currentTest++ + "/" + tests);
				THTSRunInfo rinfo = simpleLRTDPAvoid(example, g, debug);
				if (rinfo.goalFound)
					goalFound++;
				else
					goalsNotFound += example + infoString;
				if (rinfo.initialStateSolved)
					solved++;
				else
					notSolved += example + infoString;
				if (rinfo.goalFoundAndSolved())
					passed++;

			}
		}
		infoString = " nested lrtdp deadends to 0\n";
		for (String example : examples3) {
			for (int g : goalStates) {
//with deadends
				System.out.println("Test " + currentTest++ + "/" + tests);
				THTSRunInfo rinfo = nestedLRTDP(example, g, debug);
				if (rinfo.goalFound)
					goalFound++;
				else
					goalsNotFound += example + infoString;
				if (rinfo.initialStateSolved)
					solved++;
				else
					notSolved += example + infoString;
				if (rinfo.goalFoundAndSolved())
					passed++;

			}
		}

		infoString = " nested lrtdp deadends to 20 and avoid ";
		for (String example : examples3) {
			for (int g : goalStates) {
//with deadends
				System.out.println("Test " + currentTest++ + "/" + tests);
				THTSRunInfo rinfo = nestedLRTDPAvoid(example, g, debug);
				if (rinfo.goalFound)
					goalFound++;
				else {
					goalsNotFound += example + infoString + "\n";
					System.out.println(example + " " + g + infoString + "goal not found");
				}
				if (rinfo.initialStateSolved)
					solved++;
				else {
					notSolved += example + infoString + "\n";
					System.out.println(example + " " + g + infoString + " not solved");
				}
				if (rinfo.goalFoundAndSolved())
					passed++;

			}
		}

		infoString = " nested lrtdp deadends to 0 and doors";

		for (int g : goalStates2) {
			// with deadends
			System.out.println("Test " + currentTest++ + "/" + tests);
			THTSRunInfo rinfo = nestedLRTDPDoors(g, debug);
			if (rinfo.goalFound)
				goalFound++;
			else {
				goalsNotFound += infoString + "\n";
				System.out.println(+g + infoString + "goal not found");
			}
			if (rinfo.initialStateSolved)
				solved++;
			else {
				notSolved += infoString + "\n";
				System.out.println(g + infoString + " not solved");
			}
			if (rinfo.goalFoundAndSolved())
				passed++;

		}
		infoString = " nested lrtdp deadends to 20 and doors and avoid";

		for (int g : goalStates2) {
			// with deadends
			System.out.println("Test " + currentTest++ + "/" + tests);
			THTSRunInfo rinfo= nestedLRTDPDoorsAvoid(g, debug);
			if (rinfo.goalFound)
				goalFound++;
			else {
				goalsNotFound += infoString + "\n";
				System.out.println(+g + infoString + "goal not found");
			}
			if (rinfo.initialStateSolved)
				solved++;
			else {
				notSolved += infoString + "\n";
				System.out.println(g + infoString + " not solved");
			}
			if (rinfo.goalFoundAndSolved())
				passed++;
		}

		System.out.println("Passed " + passed + "/" + tests);
		System.out.println("Goals Found " + goalFound + "/" + tests
				+ ((goalFound == tests) ? "" : "\nGoals Not Found:\n" + goalsNotFound));
		System.out.println("Initial State Solved " + solved + "/" + tests
				+ ((solved == tests) ? "" : "\nInitial State Not Solved:\n" + notSolved));

//		subTest();
	}

	public void testLRTDPWithDeadendsAvoid() throws Exception {
		int[] goalStates = { 3, 6 };
		String[] examples2 = { "tro_example_new_small_noprob", "tro_example_new_small_onefailaction",
				"tro_example_new_small_allfailpaths_nowait", "tro_example_new_small_allfailpaths" };

		boolean[] goalFoundAndSolved;
		String goalsNotFound = "";
		String notSolved = "";
		int tests = goalStates.length * examples2.length;
		int passed = 0;
		int goalFound = 0;
		int solved = 0;
		boolean debug = false;
		int currentTest = 1;
		String infoString = " lrtdp with deadends and avoid";
		for (String example : examples2) {
			for (int g : goalStates) {
//with deadends	
				System.out.println("Test " + currentTest++ + "/" + tests);
				THTSRunInfo rinfo = simpleLRTDPAvoid(example, g, debug);
				if (rinfo.goalFound)
					goalFound++;
				else
					goalsNotFound += example + infoString;
				if (rinfo.initialStateSolved)
					solved++;
				else
					notSolved += example + infoString;
				if (rinfo.goalFoundAndSolved())
					passed++;

			}
		}
		System.out.println("Passed " + passed + "/" + tests);
		System.out.println("Goals Found " + goalFound + "/" + tests
				+ ((goalFound == tests) ? "" : "\nGoals Not Found:\n" + goalsNotFound));
		System.out.println("Initial State Solved " + solved + "/" + tests
				+ ((solved == tests) ? "" : "\nInitial State Not Solved:\n" + notSolved));

	}

	public void testLRTDPWithDeadends() throws Exception {
		int[] goalStates = { 3, 6 };
		String[] examples2 = { "tro_example_new_small_noprob", "tro_example_new_small_onefailaction",
				"tro_example_new_small_allfailpaths_nowait", "tro_example_new_small_allfailpaths" };

		boolean[] goalFoundAndSolved;
		String goalsNotFound = "";
		String notSolved = "";
		int tests = goalStates.length * examples2.length;
		int passed = 0;
		int goalFound = 0;
		int solved = 0;
		boolean debug = false;
		int currentTest = 1;
		String infoString = " lrtdp with deadends";
		for (String example : examples2) {
			for (int g : goalStates) {
//with deadends	
				System.out.println("Test " + currentTest++ + "/" + tests);
				THTSRunInfo rinfo = simpleLRTDP(example, g, debug);
				if (rinfo.goalFound)
					goalFound++;
				else
					goalsNotFound += example + infoString;
				if (rinfo.initialStateSolved)
					solved++;
				else
					notSolved += example + infoString;
				if (rinfo.goalFoundAndSolved())
					passed++;

			}
		}
		System.out.println("Passed " + passed + "/" + tests);
		System.out.println("Goals Found " + goalFound + "/" + tests
				+ ((goalFound == tests) ? "" : "\nGoals Not Found:\n" + goalsNotFound));
		System.out.println("Initial State Solved " + solved + "/" + tests
				+ ((solved == tests) ? "" : "\nInitial State Not Solved:\n" + notSolved));

	}

	public void testNestedLRTDPDoors() throws Exception {

		boolean[] goalFoundAndSolved;
		String goalsNotFound = "";
		String notSolved = "";
		int[] goalStates2 = { 0, 4, 6 };
		int tests = goalStates2.length;
		int passed = 0;
		int goalFound = 0;
		int solved = 0;
		boolean debug = false;
		int currentTest = 1;

		String infoString = " nested lrtdp deadends to 0 and doors";

		for (int g : goalStates2) {
			// with deadends
			System.out.println("Test " + currentTest++ + "/" + tests);
			THTSRunInfo rinfo = nestedLRTDPDoors(g, debug);
			if (rinfo.goalFound)
				goalFound++;
			else {
				goalsNotFound += infoString + "\n";
				System.out.println(+g + infoString + "goal not found");
			}
			if (rinfo.initialStateSolved)
				solved++;
			else {
				notSolved += infoString + "\n";
				System.out.println(g + infoString + " not solved");
			}
			if (rinfo.goalFoundAndSolved())
				passed++;

		}

		System.out.println("Passed " + passed + "/" + tests);
		System.out.println("Goals Found " + goalFound + "/" + tests
				+ ((goalFound == tests) ? "" : "\nGoals Not Found:\n" + goalsNotFound));
		System.out.println("Initial State Solved " + solved + "/" + tests
				+ ((solved == tests) ? "" : "\nInitial State Not Solved:\n" + notSolved));

	}

	public void testNestedLRTDPDoorsAvoid() throws Exception {


		String goalsNotFound = "";
		String notSolved = "";
		int[] goalStates2 = { 0, 4, 6 };
		int tests = goalStates2.length;
		int passed = 0;
		int goalFound = 0;
		int solved = 0;
		boolean debug = false;
		int currentTest = 1;
		String infoString = " nested lrtdp deadends to 20 and doors and avoid";
		for (int g : goalStates2) {
			// with deadends
			System.out.println("Test " + currentTest++ + "/" + tests);
			THTSRunInfo rinfo = nestedLRTDPDoorsAvoid(g, debug);
			if (rinfo.goalFound)
				goalFound++;
			else {
				goalsNotFound += infoString + "\n";
				System.out.println(+g + infoString + "goal not found");
			}
			if (rinfo.initialStateSolved)
				solved++;
			else {
				notSolved += infoString + "\n";
				System.out.println(g + infoString + " not solved");
			}
			if (rinfo.goalFoundAndSolved())
				passed++;

		}
		System.out.println("Passed " + passed + "/" + tests);
		System.out.println("Goals Found " + goalFound + "/" + tests
				+ ((goalFound == tests) ? "" : "\nGoals Not Found:\n" + goalsNotFound));
		System.out.println("Initial State Solved " + solved + "/" + tests
				+ ((solved == tests) ? "" : "\nInitial State Not Solved:\n" + notSolved));

	}

	public void runNestedLRTDPWithDeadends20andDoorsAvoid() throws Exception {

		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/csvs/";
		// making sure resultsloc exits
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);
		boolean debug = false;
		int maxRuns = 10;
		long duration, startTime, endTime;

		String sep = ",";
		String results = "\nName" + sep + "Goal" + sep + "Run" + sep + "Duration" + sep + "GoalFound" + sep
				+ "InitialStateSolved" + sep + "numRollouts";

		int[] goalStates = { 3, 6 };

//		for (String name : examples) {
		String name = "unavoidable_doors";
		for (int goal : goalStates) {
			for (int run = 0; run < maxRuns; run++) {
				String result = "\n";
				result += name + sep + goal + sep + run;
				startTime = System.currentTimeMillis();
				// run things
				THTSRunInfo rinfo = nestedLRTDPDoorsAvoid(goal, debug);

				endTime = System.currentTimeMillis();
				duration = endTime - startTime;
				result += sep + duration + sep + rinfo.goalFound + sep + rinfo.initialStateSolved + sep
						+ rinfo.numRolloutsTillSolved;
				results += result;
//System.in.read();
			}
		}
//		}
		System.out.println(results);
		PrismLog csvRes = new PrismFileLog(resultsLocation + "simpleLRTDP_ProbCost_20_Doors_Avoid.csv");
		csvRes.println(results);
		csvRes.close();

	}
	public void runNestedLRTDPWithDeadends0andDoors() throws Exception {

		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/csvs/";
		// making sure resultsloc exits
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);
		boolean debug = false;
		int maxRuns = 10;
		long duration, startTime, endTime;

		String sep = ",";
		String results = "\nName" + sep + "Goal" + sep + "Run" + sep + "Duration" + sep + "GoalFound" + sep
				+ "InitialStateSolved" + sep + "numRollouts";

		int[] goalStates = { 3, 6 };

//		for (String name : examples) {
		String name = "unavoidable_doors";
		for (int goal : goalStates) {
			for (int run = 0; run < maxRuns; run++) {
				String result = "\n";
				result += name + sep + goal + sep + run;
				startTime = System.currentTimeMillis();
				// run things
				THTSRunInfo rinfo = nestedLRTDPDoors(goal, debug);

				endTime = System.currentTimeMillis();
				duration = endTime - startTime;
				result += sep + duration + sep + rinfo.goalFound + sep + rinfo.initialStateSolved + sep
						+ rinfo.numRolloutsTillSolved;
				results += result;
//System.in.read();
			}
		}
//		}
		System.out.println(results);
		PrismLog csvRes = new PrismFileLog(resultsLocation + "simpleLRTDP_ProbCost_0_Doors.csv");
		csvRes.println(results);
		csvRes.close();

	}

	public void runNestedLRTDPWithDeadends20AndAvoid() throws Exception {
		// TODO: debug to understand why onefailaction and allfailpaths with g 3 doesnt
		// always result in a solution!!!
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/csvs/";
		// making sure resultsloc exits
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);
		boolean debug = false;
		int maxRuns = 10;
		long duration, startTime, endTime;

		String sep = ",";
		String results = "\nName" + sep + "Goal" + sep + "Run" + sep + "Duration" + sep + "GoalFound" + sep
				+ "InitialStateSolved" + sep + "numRollouts";

		int[] goalStates = { 3, 6 };

		String[] examples = { "tro_example_new_small_noprob", "tro_example_new_small_onefailaction",
				"tro_example_new_small_allfailpaths_nowait", "tro_example_new_small_allfailpaths" };

		for (String name : examples) {
			for (int goal : goalStates) {
				for (int run = 0; run < maxRuns; run++) {
					String result = "\n";
					result += name + sep + goal + sep + run;
					startTime = System.currentTimeMillis();
					// run things
					THTSRunInfo rinfo = nestedLRTDPAvoid(name, goal, debug);

					endTime = System.currentTimeMillis();
					duration = endTime - startTime;
					result += sep + duration + sep + rinfo.goalFound + sep + rinfo.initialStateSolved + sep
							+ rinfo.numRolloutsTillSolved;
					results += result;
//System.in.read();
				}
			}
		}
		System.out.println(results);
		PrismLog csvRes = new PrismFileLog(resultsLocation + "simpleLRTDP_ProbCost_20.csv");
		csvRes.println(results);
		csvRes.close();

	}

	public void runNestedLRTDPWithDeadends0() throws Exception {
		// TODO: debug to understand why onefailaction with g 3 doesnt always result in
		// a solution!!!
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/csvs/";
		// making sure resultsloc exits
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);
		boolean debug = false;
		int maxRuns = 10;
		long duration, startTime, endTime;

		String sep = ",";
		String results = "\nName" + sep + "Goal" + sep + "Run" + sep + "Duration" + sep + "GoalFound" + sep
				+ "InitialStateSolved" + sep + "numRollouts";

		int[] goalStates = { 3, 6 };

		String[] examples = { "tro_example_new_small_noprob", "tro_example_new_small_onefailaction",
				"tro_example_new_small_allfailpaths_nowait", "tro_example_new_small_allfailpaths" };

		for (String name : examples) {
			for (int goal : goalStates) {
				for (int run = 0; run < maxRuns; run++) {
					String result = "\n";
					result += name + sep + goal + sep + run;
					startTime = System.currentTimeMillis();
					// run things
					THTSRunInfo rinfo = nestedLRTDP(name, goal, debug);

					endTime = System.currentTimeMillis();
					duration = endTime - startTime;
					result += sep + duration + sep + rinfo.goalFound + sep + rinfo.initialStateSolved + sep
							+ rinfo.numRolloutsTillSolved;
					results += result;
					System.in.read();
				}
			}
		}
		System.out.println(results);
		PrismLog csvRes = new PrismFileLog(resultsLocation + "simpleLRTDP_ProbCost.csv");
		csvRes.println(results);
		csvRes.close();

	}

	public void testNestedLRTDPWithDeadends() throws Exception {
		int[] goalStates = { 3, 6 };

		String[] examples3 = { "tro_example_new_small_noprob", "tro_example_new_small_onefailaction",
				"tro_example_new_small_allfailpaths_nowait", "tro_example_new_small_allfailpaths" };
		boolean[] goalFoundAndSolved;
		String goalsNotFound = "";
		String notSolved = "";
		int tests = goalStates.length * examples3.length;
		int passed = 0;
		int goalFound = 0;
		int solved = 0;
		boolean debug = false;
		int currentTest = 1;
		String infoString = " nested lrtdp with deadends ";
		for (String example : examples3) {
			for (int g : goalStates) {
//with deadends

				System.out.println("Test " + currentTest++ + "/" + tests);
				THTSRunInfo rinfo = nestedLRTDP(example, g, debug);
				if (rinfo.goalFound)
					goalFound++;
				else {
					goalsNotFound += example + infoString + "\n";
					System.out.println(example + " " + g + infoString + "goal not found");
				}
				if (rinfo.initialStateSolved)
					solved++;
				else {
					notSolved += example + infoString + "\n";
					System.out.println(example + " " + g + infoString + " not solved");
				}
				if (rinfo.goalFoundAndSolved())
					passed++;

			}
		}
		System.out.println("Passed " + passed + "/" + tests);
		System.out.println("Goals Found " + goalFound + "/" + tests
				+ ((goalFound == tests) ? "" : "\nGoals Not Found:\n" + goalsNotFound));
		System.out.println("Initial State Solved " + solved + "/" + tests
				+ ((solved == tests) ? "" : "\nInitial State Not Solved:\n" + notSolved));

	}

	public void testNestedLRTDPWithDeadendsAvoidLTLSpecs() throws Exception {

		int g = 4;

		String goalsNotFound = "";
		String notSolved = "";
		int tests = 1;
		int passed = 0;
		int goalFound = 0;
		int solved = 0;
		boolean debug = false;

		boolean[] goalFoundAndSolved = nestedLRTDPDoorsAvoidProductSingleAgent(g, debug);
		if (goalFoundAndSolved[0])
			goalFound++;
		else {
			goalsNotFound += "nested lrtdp doors with deadends and avoid with ltl specs\n";
			System.out.println(
					"nested lrtdp doors with deadends and avoid with ltl specs" + " " + g + " - goal not found");
		}
		if (goalFoundAndSolved[1])
			solved++;
		else {
			notSolved += "nested lrtdp doors with deadends and avoid with ltl specs\n";
			System.out.println("nested lrtdp doors with deadends and avoid with ltl specs" + " " + g + " - not solved");

		}
		if (goalFoundAndSolved[0] && goalFoundAndSolved[1])
			passed++;

		System.out.println("Passed " + passed + "/" + tests);
		System.out.println("Goals Found " + goalFound + "/" + tests
				+ ((goalFound == tests) ? "" : "\nGoals Not Found:\n" + goalsNotFound));
		System.out.println("Initial State Solved " + solved + "/" + tests
				+ ((solved == tests) ? "" : "\nInitial State Not Solved:\n" + notSolved));

	}

	public void debugInstance() throws Exception {

		String example = "tro_example_new_small_noprob";// "tro_example_new_small_allfailpaths";
		int g = 4;// 3;//6;
		boolean debug = true;
		THTSRunInfo rinfo = nestedLRTDPDoorsAvoid(g, debug);// nestedLRTDPAvoid(example, g, debug);
		if (rinfo.initialStateSolved == false)
			System.out.println("Not Solved");
	}

	public void currentWIP() throws Exception {

		boolean debug = true;
		boolean[] goalFoundAndSolved = nestedLRTDPDoorsAvoidProductNestedSingleAgentTaskCompletion(debug);
	}

	public void runSimpleLRTDP() throws Exception {

		// we time each test
		// we run it for atleast 5 times
		// we check how many times the goal was found
		// and how many times the intial state was solved
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/csvs/";
		// making sure resultsloc exits
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);
		boolean debug = false;
		int maxRuns = 5;
		long duration, startTime, endTime;

		String sep = ",";
		String results = "\nName" + sep + "Goal" + sep + "Run" + sep + "Duration" + sep + "GoalFound" + sep
				+ "InitialStateSolved" + sep + "numRollouts";

		int[] goalStates = { 3, 6 };

		String[] examples = { "tro_example_new_small_noprob", "tro_example_new_small_onefailaction",
				"tro_example_new_small_allfailpaths_nowait", "tro_example_new_small_allfailpaths" };
//			{ "tro_example_new_small_noprob", "tro_example_new_small_onefailaction" };

		for (String name : examples) {
			for (int goal : goalStates) {
				for (int run = 0; run < maxRuns; run++) {
					String result = "\n";
					result += name + sep + goal + sep + run;
					startTime = System.currentTimeMillis();
					// run things
					THTSRunInfo rinfo = /* simpleLRTDPNoDeadends */
							simpleLRTDP(name, goal, debug);

					endTime = System.currentTimeMillis();
					duration = endTime - startTime;
					result += sep + duration + sep + rinfo.goalFound + sep + rinfo.initialStateSolved + sep
							+ rinfo.numRolloutsTillSolved;
					results += result;
//System.in.read();
				}
			}
		}
		System.out.println(results);
		PrismLog csvRes = new PrismFileLog(resultsLocation + "simpleLRTDP.csv");
		csvRes.println(results);
		csvRes.close();
	}

	public void runSimpleLRTDPNoDeadends() throws Exception {

		// we time each test
		// we run it for atleast 5 times
		// we check how many times the goal was found
		// and how many times the intial state was solved
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/csvs/";
		// making sure resultsloc exits
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);
		boolean debug = false;
		int maxRuns = 5;
		long duration, startTime, endTime;

		String sep = ",";
		String results = "\nName" + sep + "Goal" + sep + "Run" + sep + "Duration" + sep + "GoalFound" + sep
				+ "InitialStateSolved" + sep + "numRollouts";

		int[] goalStates = { 3, 6 };

		String[] examples =
//			{ "tro_example_new_small_noprob", "tro_example_new_small_onefailaction",
//					"tro_example_new_small_allfailpaths_nowait", "tro_example_new_small_allfailpaths" };
				{ "tro_example_new_small_noprob", "tro_example_new_small_onefailaction" };

		for (String name : examples) {
			for (int goal : goalStates) {
				for (int run = 0; run < maxRuns; run++) {
					String result = "\n";
					result += name + sep + goal + sep + run;
					startTime = System.currentTimeMillis();
					// run things
					THTSRunInfo rinfo = simpleLRTDPNoDeadends(name, goal, debug);

					endTime = System.currentTimeMillis();
					duration = endTime - startTime;
					result += sep + duration + sep + rinfo.goalFound + sep + rinfo.initialStateSolved + sep
							+ rinfo.numRolloutsTillSolved;
					results += result;
//					System.in.read();
				}
			}
		}
		System.out.println(results);
		PrismLog csvRes = new PrismFileLog(resultsLocation + "simpleLRTDPNoDeadends.csv");
		csvRes.println(results);
		csvRes.close();
	}

	public void runSimpleLRTDPAvoid() throws Exception {

		// we time each test
		// we run it for atleast 5 times
		// we check how many times the goal was found
		// and how many times the intial state was solved
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/csvs/";
		// making sure resultsloc exits
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);
		boolean debug = false;
		int maxRuns = 5;
		long duration, startTime, endTime;

		String sep = ",";
		String results = "\nName" + sep + "Goal" + sep + "Run" + sep + "Duration" + sep + "GoalFound" + sep
				+ "InitialStateSolved" + sep + "numRollouts";

		int[] goalStates = { 3, 6 };

		String[] examples = { "tro_example_new_small_noprob", "tro_example_new_small_onefailaction",
				"tro_example_new_small_allfailpaths_nowait", "tro_example_new_small_allfailpaths" };
//			{ "tro_example_new_small_noprob", "tro_example_new_small_onefailaction" };

		for (String name : examples) {
			for (int goal : goalStates) {
				for (int run = 0; run < maxRuns; run++) {
					String result = "\n";
					result += name + sep + goal + sep + run;
					startTime = System.currentTimeMillis();
					// run things
					THTSRunInfo rinfo = // simpleLRTDPNoDeadends
							simpleLRTDPAvoid(name, goal, debug);

					endTime = System.currentTimeMillis();
					duration = endTime - startTime;
					result += sep + duration + sep + rinfo.goalFound + sep + rinfo.initialStateSolved + sep
							+ rinfo.numRolloutsTillSolved;
					results += result;

				}
			}
		}
		System.out.println(results);
		PrismLog csvRes = new PrismFileLog(resultsLocation + "simpleLRTDPAvoid.csv");
		csvRes.println(results);
		csvRes.close();
	}

	public static void main(String[] args) {
		try {
			TestLRTDPSimple tester = new TestLRTDPSimple();
			String options[] = { "all", "lrtdp_deadends", "nested_lrtdp_deadends_doors", "nested_lrtdp_deadends",
					"help", "-h", "nested_lrtdp_deadends_doors_avoid", "debugInstance", "currentWIP",
					"nested_lrtdp_deadends_doors_avoid_ltl", "runTests" };
			String option = "runTests";// "all";//"runTests";//"currentWIP";// "debugInstance";//"all";
			if (args.length > 1) {
				System.out.println(Arrays.deepToString(args));
				option = args[0];
				System.out.println("Running with argument: " + option);
				System.in.read();
			}

			if (option.contentEquals(options[0]))
				tester.runTests();
			else if (option.contentEquals(options[1]))
				tester.testLRTDPWithDeadends();
			else if (option.contentEquals(options[2]))
				tester.testNestedLRTDPDoors();
			else if (option.contentEquals(options[3]))
				tester.testNestedLRTDPWithDeadends();
			else if (option.contentEquals(options[4]) | option.contentEquals(options[5])) {
				System.out.println("Available options " + Arrays.toString(options));
			} else if (option.contentEquals(options[6])) {
				tester.testNestedLRTDPDoorsAvoid();
			} else if (option.contentEquals(options[7])) {
				tester.debugInstance();
			} else if (option.contentEquals(options[8])) {
				tester.currentWIP();
			} else if (option.contentEquals(options[9])) {
				tester.testNestedLRTDPWithDeadendsAvoidLTLSpecs();
			} else if (option.contentEquals(options[10])) {
//				tester.runSimpleLRTDP();
//				tester.runSimpleLRTDPNoDeadends();
//				tester.runSimpleLRTDPAvoid();
//				tester.runNestedLRTDPWithDeadends0();
//				tester.runNestedLRTDPWithDeadends20AndAvoid();
//				tester.runNestedLRTDPWithDeadends0andDoors();
				tester.runNestedLRTDPWithDeadends20andDoorsAvoid();
				
			} else {
				System.out
						.println("Unimplemented option " + option + "\nAvailable options " + Arrays.toString(options));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	THTSRunInfo nestedLRTDPDoors(int stateVal, boolean debug) throws Exception {
		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/";
		// making sure resultsloc exits
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);

		String example = "tro_example_new_small";

		PrismLog mainLog;
		if (debug)
			mainLog = new PrismFileLog("stdout");
		else
			mainLog = new PrismDevNullLog();

		Prism prism = new Prism(mainLog);
		String combString = "_prob_cost_lrtdp";
		String algoIden = "rtdp" + combString;
		PrismLog fileLog = new PrismFileLog(
				resultsLocation + "log_" + example + "_g_" + stateVal + "_" + algoIden + "_justmdp" + ".txt");//

		prism.initialise();
		prism.setEngine(Prism.EXPLICIT);

		mainLog.println("Initialised Prism");

		String modelFileName = testsLocation + example + "0.prism";
		ModulesFile modulesFile = prism.parseModelFile(new File(modelFileName)); // because the models are uniform

		ModulesFileModelGenerator modGen = new ModulesFileModelGenerator(modulesFile, prism);
		MDPModelGenerator mdpModGen = new MDPModelGenerator(modGen);

		int maxRollouts = 1000;
		int trialLen = 100;
		float epsilon = 0.0001f;

		List<State> gs = new ArrayList<State>();
		List<State> deadend = new ArrayList<State>();
		for (int i = -1; i < 2; i++) {

			State goalState1 = new State(2);
			goalState1.setValue(0, stateVal);
			goalState1.setValue(1, i);
			gs.add(goalState1);
		}

		int deadendval = -1;
		for (int i = -1; i < 2; i++) {
			State de1 = new State(2);
			de1.setValue(0, deadendval);
			de1.setValue(1, i);
			deadend.add(de1);
		}

		Heuristic heuristicFunction = new EmptyHeuristic(gs, deadend);
		ArrayList<Objectives> tieBreakingOrder = new ArrayList<Objectives>();
		tieBreakingOrder.add(Objectives.Probability);
		tieBreakingOrder.add(Objectives.Cost);

		mainLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
		fileLog.println("Tie Breaking Order " + tieBreakingOrder.toString());

		mainLog.println("Initialising Greedy Bounds Difference Action Selector Function");
		fileLog.println("Initialising Greedy Bounds Difference Action Selector Function");

		ActionSelector actionSelection = new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder);// new
																									// ActionSelectorGreedyBoundsDiff(tieBreakingOrder);

		mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
		fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

		OutcomeSelector outcomeSelection = new OutcomeSelectorRandom();

		mainLog.println("Initialising Full Bellman Backup Function");
		fileLog.println("Initialising Full Bellman Backup Function");

		BackupNVI backupFunction = new BackupLabelledFullBelman(tieBreakingOrder, actionSelection, epsilon);

		mainLog.println("Initialising Reward Helper Function");
		fileLog.println("Initialising Reward Helper Function");

		RewardHelper rewardH = new RewardHelperMDP(mdpModGen);

		mainLog.println("Max Rollouts: " + maxRollouts);
		mainLog.println("Max TrialLen: " + trialLen);
		fileLog.println("Max Rollouts: " + maxRollouts);
		fileLog.println("Max TrialLen: " + trialLen);

		mainLog.println("\nInitialising THTS");
		fileLog.println("\nInitialising THTS");
		boolean doForwardBackup = true;
		TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) mdpModGen, maxRollouts, trialLen,
				heuristicFunction, actionSelection, outcomeSelection, rewardH, backupFunction, doForwardBackup,
				tieBreakingOrder, mainLog, fileLog);

		mainLog.println("\nBeginning THTS");
		fileLog.println("\nBeginning THTS");
		thts.setName(example + "g_" + stateVal + "_rtdp" + combString);
		thts.setResultsLocation(resultsLocation);

		int numRolloutsTillSolved = thts.run(false);

		mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
		fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");

		boolean[] goalack = thts.runThrough(new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder),
				resultsLocation);
		THTSRunInfo rinfo = new THTSRunInfo();
		rinfo.numRolloutsTillSolved = numRolloutsTillSolved;
		rinfo.goalFound = goalack[0];
		rinfo.initialStateSolved = goalack[1];

		return rinfo;

//		thts.run(false);
//
//		mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
//		fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
//
//		boolean[] goalack = thts.runThrough(new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder),
//				resultsLocation);
//		return goalack;
	}

	public MultiAgentNestedProductModelGenerator createMAMG(Prism prism, PrismLog mainLog, ArrayList<String> filenames,
			String propertiesFileName, String resultsLocation, boolean hasSharedState)
			throws PrismException, FileNotFoundException {

		AcceptanceType[] allowedAcceptance = { AcceptanceType.RABIN, AcceptanceType.REACH };

		// step 1
		// create the modulesfilemodelgenerators
		ArrayList<ModulesFileModelGenerator> mfmodgens = new ArrayList<>();
		ModulesFile modulesFile = null; // just here so we can use the last modules file for our properties

		for (String modelFileName : filenames) {
			mainLog.println("Loading model gen for " + modelFileName);
			modulesFile = prism.parseModelFile(new File(modelFileName)); // because the models are uniform
			// we might have to find a way to change this later
			ModulesFileModelGenerator modGen = new ModulesFileModelGenerator(modulesFile, prism);
			mfmodgens.add(modGen);
		}
		// step 2
		// load all the exprs and remember to check them
		mainLog.println("Loading properties ");
		PropertiesFile propertiesFile = prism.parsePropertiesFile(modulesFile, new File(propertiesFileName));
		List<Expression> processedExprs = new ArrayList<Expression>();
		int safetydaind = -1;
		Expression safetyexpr = null;
		for (int i = 0; i < propertiesFile.getNumProperties(); i++) {
			mainLog.println(propertiesFile.getProperty(i));
			// so reward + safety
			boolean isSafeExpr = false;
			Expression exprHere = propertiesFile.getProperty(i);

			Expression daExpr = ((ExpressionQuant) exprHere).getExpression();
			isSafeExpr = !Expression.isCoSafeLTLSyntactic(daExpr, true);
			if (isSafeExpr) {
				if (safetyexpr != null) {
					// two safety exprs? lets and this stuff
					// TODO: could this cause problems ? //like if one was min and max since we're
					// ignoring those
					safetyexpr = Expression.And(safetyexpr, daExpr);
				} else
					safetyexpr = daExpr;

			}

			if (!isSafeExpr)
				processedExprs.add(daExpr);
		}
		// we've got the safety stuff left
		// we need to not it
		Expression notsafetyexpr = Expression.Not(safetyexpr);
		safetydaind = processedExprs.size();
		processedExprs.add(safetyexpr);

		mainLog.println("Properties " + processedExprs.toString());
		// hmmmm so this is important I guess
		// and we have a single safety da okay
		// oooo reward structures we don't have to care about
		// right o -lets do this
		// for the honor of greyskull

		LTLModelChecker ltlMC = new LTLModelChecker(prism);

		ArrayList<List<Expression>> labelExprsList = new ArrayList<List<Expression>>();
		ArrayList<DA<BitSet, ? extends AcceptanceOmega>> das = new ArrayList<DA<BitSet, ? extends AcceptanceOmega>>();
		for (int i = 0; i < processedExprs.size(); i++) {
			List<Expression> labelExprs = new ArrayList<Expression>();

			Expression expr = (Expression) processedExprs.get(i);
			// this will need to be changed if you've got different variables accross models
			// then its better to do the v=5 stuff in the prop files and just ignore labels
			expr = (Expression) expr.expandPropRefsAndLabels(propertiesFile, modulesFile.getLabelList());
			DA<BitSet, ? extends AcceptanceOmega> da = ltlMC.constructExpressionDAForLTLFormula(expr, labelExprs,
					allowedAcceptance);
			da.setDistancesToAcc();
			PrismLog out = new PrismFileLog(resultsLocation + "da_" + i + ".dot");
			// printing the da
			da.print(out, "dot");
			out.close();
			labelExprsList.add(labelExprs);
			das.add(da);
			mainLog.println("Created DA for " + expr.toString());
		}
		ArrayList<String> sharedStateVars = new ArrayList<String>();
		if (hasSharedState)
			sharedStateVars.add("door0");
//		sharedStateVars = null;
		MultiAgentNestedProductModelGenerator mapmg = new MultiAgentNestedProductModelGenerator(mfmodgens, das,
				labelExprsList, safetydaind, sharedStateVars);

		return mapmg;
	}

	boolean[] nestedLRTDPDoorsAvoidProductNestedSingleAgentTaskCompletion(boolean debug) throws Exception {
		boolean goalFound = false;
		double[] hvals = { 500, 1000, 2000, 10000 };
		int[] rollouts = { 1000, 2000, 5000, 10000 };
		int[] trialLens = { 50, 100, 200, 500 };
		double hval = 20;// trialLen;//trialLen*maxRollouts;
		boolean[] goalack = new boolean[2];

		for (int hvalnum = 0; hvalnum < hvals.length; hvalnum++) {
			hval = hvals[hvalnum];
			for (int rolloutnum = 0; rolloutnum < rollouts.length; rolloutnum++) {
				int maxRollouts = rollouts[rolloutnum];
				for (int trialLennum = 0; trialLennum < trialLens.length; trialLennum++) {
					int trialLen = trialLens[trialLennum];

					float epsilon = 0.0001f;

					System.out.println(System.getProperty("user.dir"));
					String currentDir = System.getProperty("user.dir");
					String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
					String resultsLocation = testsLocation + "results/";
					// making sure resultsloc exits
					createDirIfNotExist(resultsLocation);
					System.out.println("Results Location " + resultsLocation);

					String example = "tro_example_new_small";

//		example = "tro_example_new_small_allfailpaths";//"tro_example_new_small_onefailaction";//"tro_example_new_small_noprob";
					boolean hasSharedState = false;
					PrismLog mainLog;
					if (debug)
						mainLog = new PrismFileLog("stdout");
					else
						mainLog = new PrismDevNullLog();

					Prism prism = new Prism(mainLog);
					String combString = "_prob_cost_lrtdp_avoid_prod_ltl_specs_taskcomp_costcutoff_" + hval
							+ "_trialLen_" + trialLen + "_rollouts_" + maxRollouts;
					String algoIden = "rtdp" + combString;
					PrismLog fileLog = new PrismFileLog(
							resultsLocation + "log_" + example + "_ltl_specs_taskcomp_costcutoff_" + hval + "_trialLen_"
									+ trialLen + "_rollouts_" + maxRollouts + "_" + algoIden + "_justmdp" + ".txt");//

					prism.initialise();
					prism.setEngine(Prism.EXPLICIT);

					mainLog.println("Initialised Prism");

					// create a single agent model generator first

					AcceptanceType[] allowedAcceptance = { AcceptanceType.RABIN, AcceptanceType.REACH };
//
					List<Expression> labelExprs = new ArrayList<Expression>();

					String modelFileName = testsLocation + example + "0.prism";
					ModulesFile modulesFile = prism.parseModelFile(new File(modelFileName)); // because the models are
																								// uniform
					String propertiesFileName = testsLocation + example + "_mult.prop";

					ArrayList<String> filenames = new ArrayList<>();
					filenames.add(modelFileName);
					// so we want to create a single agent model generator
					// good test
					PropertiesFile propertiesFile = prism.parsePropertiesFile(modulesFile,
							new File(propertiesFileName));

					LTLModelChecker ltlMC = new LTLModelChecker(prism);

					ExpressionReward rewExpr = null;
					Expression safetyExpr = null;
					ArrayList<Expression> otherExpressions = new ArrayList<Expression>();
					// assumption a safety expression can not be a reward expression

					List<Expression> processedExprs = new ArrayList<Expression>();
					for (int i = 0; i < propertiesFile.getNumProperties(); i++) {

						boolean isSafeExpr = false;
						Expression exprHere = propertiesFile.getProperty(i);
						if (exprHere instanceof ExpressionReward)
							rewExpr = (ExpressionReward) exprHere;
						else {
							Expression daExpr = ((ExpressionQuant) exprHere).getExpression();
							isSafeExpr = !Expression.isCoSafeLTLSyntactic(daExpr, true);
							if (isSafeExpr)
								safetyExpr = daExpr;
							else
								otherExpressions.add(exprHere);
						}
						if (!isSafeExpr)
							processedExprs.add(((ExpressionQuant) exprHere).getExpression());
					}

					otherExpressions.add(((ExpressionQuant) rewExpr).getExpression());
					otherExpressions.add(safetyExpr);

					ArrayList<List<Expression>> labelExprsList = new ArrayList<List<Expression>>();
					ArrayList<DA<BitSet, ? extends AcceptanceOmega>> das = new ArrayList<DA<BitSet, ? extends AcceptanceOmega>>();

					DA<BitSet, ? extends AcceptanceOmega> da;
					for (int i = 0; i < processedExprs.size(); i++) {
						labelExprs = new ArrayList<Expression>();

						Expression expr = (Expression) processedExprs.get(i);
						expr = (Expression) expr.expandPropRefsAndLabels(propertiesFile, modulesFile.getLabelList());
						da = ltlMC.constructExpressionDAForLTLFormula(expr, labelExprs, allowedAcceptance);
						da.setDistancesToAcc();
						PrismLog out = new PrismFileLog(resultsLocation + "da_" + i + ".dot");
						// printing the da
						da.print(out, "dot");
						out.close();
						labelExprsList.add(labelExprs);
						das.add(da);
					}
					// lastly the safety expr
					Expression expr = Expression.Not(safetyExpr);
					expr = (Expression) expr.expandPropRefsAndLabels(propertiesFile, modulesFile.getLabelList());
					labelExprs = new ArrayList<Expression>();
					da = ltlMC.constructExpressionDAForLTLFormula(expr, labelExprs, allowedAcceptance);
					da.setDistancesToAcc();
					PrismLog out = new PrismFileLog(resultsLocation + "da_safety.dot");
					// printing the da
					da.print(out, "dot");
					out.close();
					labelExprsList.add(labelExprs);
					das.add(da);

					ModulesFileModelGenerator modGen = new ModulesFileModelGenerator(modulesFile, prism);

					NestedProductModelGenerator saModelGen = new NestedProductModelGenerator(modGen, das,
							labelExprsList, das.size() - 1);

					List<State> gs = new ArrayList<State>();
					List<State> deadend = new ArrayList<State>();

					int[] deadendvals = { -1, 5 };

					for (int j = -1; j < 2; j++) {
						for (int deadendval : deadendvals) {
							for (int i1 = 0; i1 < 2; i1++) {
								for (int i2 = 0; i2 < 2; i2++) {
									for (int i3 = 0; i3 < 2; i3++) {

										State de1 = new State(5);
										// State de1 = new State(2);
										de1.setValue(0, deadendval);
										de1.setValue(1, j);
										de1.setValue(2, i1);
										de1.setValue(3, i2);
										de1.setValue(4, i3);
										deadend.add(de1);
									}
								}
							}
						}
					}

					Heuristic heuristicFunction = new EmptyNestedSingleAgentHeuristic(saModelGen, gs, deadend, hval);
					ArrayList<Objectives> tieBreakingOrder = new ArrayList<Objectives>();
//		tieBreakingOrder.add(Objectives.Probability);
					tieBreakingOrder.add(Objectives.TaskCompletion);
					tieBreakingOrder.add(Objectives.Cost);
					HashMap<Objectives, Entry<Double, Double>> minMaxVals = new HashMap<>();
					minMaxVals.put(Objectives.Cost, new AbstractMap.SimpleEntry<Double, Double>(0., hval));
					minMaxVals.put(Objectives.TaskCompletion,
							new AbstractMap.SimpleEntry<Double, Double>(0., (double) saModelGen.numDAs));
					mainLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
					fileLog.println("Tie Breaking Order " + tieBreakingOrder.toString());

					mainLog.println("Initialising Greedy Bounds Difference Action Selector Function");
					fileLog.println("Initialising Greedy Bounds Difference Action Selector Function");

					ActionSelector actionSelection = new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder);// new
																												// ActionSelectorGreedyBoundsDiff(tieBreakingOrder);

					mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
					fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

					OutcomeSelector outcomeSelection = new OutcomeSelectorRandom();

					mainLog.println("Initialising Full Bellman Backup Function");
					fileLog.println("Initialising Full Bellman Backup Function");

					BackupNVI backupFunction = new BackupLabelledFullBelmanCap(tieBreakingOrder, actionSelection,
							epsilon, minMaxVals);

					mainLog.println("Initialising Reward Helper Function");
					fileLog.println("Initialising Reward Helper Function");

					RewardHelper rewardH = new RewardHelperNestedSingleAgent(saModelGen);

					mainLog.println("Max Rollouts: " + maxRollouts);
					mainLog.println("Max TrialLen: " + trialLen);
					fileLog.println("Max Rollouts: " + maxRollouts);
					fileLog.println("Max TrialLen: " + trialLen);

					mainLog.println("\nInitialising THTS");
					fileLog.println("\nInitialising THTS");
					boolean doForwardBackup = true;
					TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) saModelGen,
							maxRollouts, trialLen, heuristicFunction, actionSelection, outcomeSelection, rewardH,
							backupFunction, doForwardBackup, tieBreakingOrder, mainLog, fileLog);

					mainLog.println("\nBeginning THTS");
					fileLog.println("\nBeginning THTS");
					thts.setName(example + "_rtdp" + combString);
					thts.setResultsLocation(resultsLocation);
//			thts.run(true);
					thts.run(false);

					mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
					fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");

					goalack = thts.runThrough(new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder),
							resultsLocation);
					goalFound = goalack[0];
					mainLog.println("Goal Found: " + goalack[0]);
					mainLog.println("Initial State Solved: " + goalack[1]);
					System.in.read();

//					int numRolloutsTillSolved = thts.run(false);
//
//					mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
//					fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
//
//					boolean[] goalack = thts.runThrough(new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder),
//							resultsLocation);
//					THTSRunInfo rinfo = new THTSRunInfo(); 
//					rinfo.numRolloutsTillSolved = numRolloutsTillSolved; 
//					rinfo.goalFound = goalack[0];
//					rinfo.initialStateSolved = goalack[1];
//					
//					return rinfo;

				}
				if (goalFound)
					break;
			}
			if (goalFound)
				break;
		}
		return goalack;
	}

	boolean[] nestedLRTDPDoorsAvoidProductSingleAgent(int stateVal, boolean debug) throws Exception {
		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/";
		// making sure resultsloc exits
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);

		String example = "tro_example_new_small";

//		example = "tro_example_new_small_allfailpaths";//"tro_example_new_small_onefailaction";//"tro_example_new_small_noprob";
		boolean hasSharedState = false;
		PrismLog mainLog;
		if (debug)
			mainLog = new PrismFileLog("stdout");
		else
			mainLog = new PrismDevNullLog();

		Prism prism = new Prism(mainLog);
		String combString = "_prob_cost_lrtdp_avoid_prod";
		String algoIden = "rtdp" + combString;
		PrismLog fileLog = new PrismFileLog(
				resultsLocation + "log_" + example + "_g_" + stateVal + "_" + algoIden + "_justmdp" + ".txt");//

		prism.initialise();
		prism.setEngine(Prism.EXPLICIT);

		mainLog.println("Initialised Prism");

		// create a single agent model generator first

		AcceptanceType[] allowedAcceptance = { AcceptanceType.RABIN, AcceptanceType.REACH };
//
		List<Expression> labelExprs = new ArrayList<Expression>();

		String modelFileName = testsLocation + example + "0.prism";
		ModulesFile modulesFile = prism.parseModelFile(new File(modelFileName)); // because the models are uniform
		String propertiesFileName = testsLocation + example + ".prop";

		ArrayList<String> filenames = new ArrayList<>();
		filenames.add(modelFileName);
		// so we want to create a single agent model generator
		// good test
		PropertiesFile propertiesFile = prism.parsePropertiesFile(modulesFile, new File(propertiesFileName));

		// the assumption is that its a reward expr for now
		Expression exprHere = propertiesFile.getProperty(0);
		ExpressionReward exprRew = (ExpressionReward) exprHere;
		Expression expr = exprRew.getExpression();
		ModulesFileModelGenerator modGen = new ModulesFileModelGenerator(modulesFile, prism);
		LTLModelChecker ltlMC = new LTLModelChecker(prism);

		DA<BitSet, ? extends AcceptanceOmega> da = ltlMC.constructExpressionDAForLTLFormula(expr, // .getExpression(),
				labelExprs, allowedAcceptance);

		da.printDot(System.out);
		SingleAgentProductModelGenerator saModelGen = new SingleAgentProductModelGenerator(modGen, da, labelExprs);
//		MDPModelGenerator mdpModGen = new MDPModelGenerator(modGen);

		int maxRollouts = 1000;
		int trialLen = 50;
		float epsilon = 0.0001f;

		double hval = 20;

		List<State> gs = new ArrayList<State>();
		List<State> deadend = new ArrayList<State>();
		for (int j = -1; j < 2; j++) {
			for (int i = 1; i < 2; i++) {

				State goalState1 = new State(3);
				// State goalState1 = new State(2);
				goalState1.setValue(0, stateVal);
				goalState1.setValue(1, j);
				goalState1.setValue(2, i);
				gs.add(goalState1);
			}
		}
		int[] deadendvals = { -1, 5 };
		for (int j = -1; j < 2; j++) {
			for (int deadendval : deadendvals) {
				for (int i = 1; i < 4; i++) {
					State de1 = new State(3);
					// State de1 = new State(2);
					de1.setValue(0, deadendval);
					de1.setValue(1, j);
					de1.setValue(2, i);
					deadend.add(de1);
				}
			}
		}

		Heuristic heuristicFunction = new EmptySingleAgentHeuristic(saModelGen, gs, deadend, hval);
		// new EmptyHeuristic(gs, deadend, hval);
		ArrayList<Objectives> tieBreakingOrder = new ArrayList<Objectives>();
		tieBreakingOrder.add(Objectives.Probability);
		tieBreakingOrder.add(Objectives.Cost);

		mainLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
		fileLog.println("Tie Breaking Order " + tieBreakingOrder.toString());

		mainLog.println("Initialising Greedy Bounds Difference Action Selector Function");
		fileLog.println("Initialising Greedy Bounds Difference Action Selector Function");

		ActionSelector actionSelection = new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder);// new
																									// ActionSelectorGreedyBoundsDiff(tieBreakingOrder);

		mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
		fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

		OutcomeSelector outcomeSelection = new OutcomeSelectorRandom();

		mainLog.println("Initialising Full Bellman Backup Function");
		fileLog.println("Initialising Full Bellman Backup Function");

		BackupNVI backupFunction = new BackupLabelledFullBelman(tieBreakingOrder, actionSelection, epsilon);

		mainLog.println("Initialising Reward Helper Function");
		fileLog.println("Initialising Reward Helper Function");

		RewardHelper rewardH = new RewardHelperMDP(saModelGen);

		mainLog.println("Max Rollouts: " + maxRollouts);
		mainLog.println("Max TrialLen: " + trialLen);
		fileLog.println("Max Rollouts: " + maxRollouts);
		fileLog.println("Max TrialLen: " + trialLen);

		mainLog.println("\nInitialising THTS");
		fileLog.println("\nInitialising THTS");
		boolean doForwardBackup = true;
		TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) saModelGen, maxRollouts, trialLen,
				heuristicFunction, actionSelection, outcomeSelection, rewardH, backupFunction, doForwardBackup,
				tieBreakingOrder, mainLog, fileLog);

		mainLog.println("\nBeginning THTS");
		fileLog.println("\nBeginning THTS");
		thts.setName(example + "g_" + stateVal + "_rtdp" + combString);
		thts.setResultsLocation(resultsLocation);
		thts.run(false);

		mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
		fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");

		boolean[] goalack = thts.runThrough(new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder),
				resultsLocation);
		mainLog.println("Goal Found: " + goalack[0]);
		mainLog.println("Initial State Solved: " + goalack[1]);
		return goalack;

//		int numRolloutsTillSolved = thts.run(false);
//
//		mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
//		fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
//
//		boolean[] goalack = thts.runThrough(new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder),
//				resultsLocation);
//		THTSRunInfo rinfo = new THTSRunInfo(); 
//		rinfo.numRolloutsTillSolved = numRolloutsTillSolved; 
//		rinfo.goalFound = goalack[0];
//		rinfo.initialStateSolved = goalack[1];
//		
//		return rinfo;

	}

	THTSRunInfo nestedLRTDPDoorsAvoid(int stateVal, boolean debug) throws Exception {
		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/";
		// making sure resultsloc exits
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);

		String example = "tro_example_new_small";

		PrismLog mainLog;
		if (debug)
			mainLog = new PrismFileLog("stdout");
		else
			mainLog = new PrismDevNullLog();

		Prism prism = new Prism(mainLog);
		String combString = "_prob_cost_lrtdp_avoid";
		String algoIden = "rtdp" + combString;
		PrismLog fileLog = new PrismFileLog(
				resultsLocation + "log_" + example + "_g_" + stateVal + "_" + algoIden + "_justmdp" + ".txt");//

		prism.initialise();
		prism.setEngine(Prism.EXPLICIT);

		mainLog.println("Initialised Prism");

		String modelFileName = testsLocation + example + "0.prism";
		ModulesFile modulesFile = prism.parseModelFile(new File(modelFileName)); // because the models are uniform

		ModulesFileModelGenerator modGen = new ModulesFileModelGenerator(modulesFile, prism);
		MDPModelGenerator mdpModGen = new MDPModelGenerator(modGen);

		int maxRollouts = 1000;
		int trialLen = 100;
		float epsilon = 0.0001f;

		List<State> gs = new ArrayList<State>();
		List<State> deadend = new ArrayList<State>();
		for (int i = -1; i < 2; i++) {

			State goalState1 = new State(2);
			goalState1.setValue(0, stateVal);
			goalState1.setValue(1, i);
			gs.add(goalState1);
		}

		int[] deadendvals = { -1, 5 };
		for (int deadendval : deadendvals) {
			for (int i = -1; i < 2; i++) {
				State de1 = new State(2);
				de1.setValue(0, deadendval);
				de1.setValue(1, i);
				deadend.add(de1);
			}
		}
		double hval = 20;
		Heuristic heuristicFunction = new EmptyHeuristic(gs, deadend, hval);
		ArrayList<Objectives> tieBreakingOrder = new ArrayList<Objectives>();
		tieBreakingOrder.add(Objectives.Probability);
		tieBreakingOrder.add(Objectives.Cost);

		mainLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
		fileLog.println("Tie Breaking Order " + tieBreakingOrder.toString());

		mainLog.println("Initialising Greedy Bounds Difference Action Selector Function");
		fileLog.println("Initialising Greedy Bounds Difference Action Selector Function");

		ActionSelector actionSelection = new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder);// new
																									// ActionSelectorGreedyBoundsDiff(tieBreakingOrder);

		mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
		fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

		OutcomeSelector outcomeSelection = new OutcomeSelectorRandom();

		mainLog.println("Initialising Full Bellman Backup Function");
		fileLog.println("Initialising Full Bellman Backup Function");

		BackupNVI backupFunction = new BackupLabelledFullBelman(tieBreakingOrder, actionSelection, epsilon);

		mainLog.println("Initialising Reward Helper Function");
		fileLog.println("Initialising Reward Helper Function");

		RewardHelper rewardH = new RewardHelperMDP(mdpModGen);

		mainLog.println("Max Rollouts: " + maxRollouts);
		mainLog.println("Max TrialLen: " + trialLen);
		fileLog.println("Max Rollouts: " + maxRollouts);
		fileLog.println("Max TrialLen: " + trialLen);

		mainLog.println("\nInitialising THTS");
		fileLog.println("\nInitialising THTS");
		boolean doForwardBackup = true;
		TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) mdpModGen, maxRollouts, trialLen,
				heuristicFunction, actionSelection, outcomeSelection, rewardH, backupFunction, doForwardBackup,
				tieBreakingOrder, mainLog, fileLog);

		mainLog.println("\nBeginning THTS");
		fileLog.println("\nBeginning THTS");
		thts.setName(example + "g_" + stateVal + "_rtdp" + combString);
		thts.setResultsLocation(resultsLocation);
//		thts.run(false);
//
//		mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
//		fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
//
//		boolean[] goalack = thts.runThrough(new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder),
//				resultsLocation);
//		return goalack;

		int numRolloutsTillSolved = thts.run(false);

		mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
		fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");

		boolean[] goalack = thts.runThrough(new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder),
				resultsLocation);
		THTSRunInfo rinfo = new THTSRunInfo(); 
		rinfo.numRolloutsTillSolved = numRolloutsTillSolved; 
		rinfo.goalFound = goalack[0];
		rinfo.initialStateSolved = goalack[1];
		
		return rinfo;

	}

	THTSRunInfo nestedLRTDP(String example, int stateVal, boolean debug) throws Exception {

//		if (example.contains("tro_example_new_small_allfailpaths") & stateVal == 6) {
//			System.out.println("Debug");
//			debug = true;
//		}
		int minRollouts = 1000;
		boolean[] goalack = null;
		int numRollouts = minRollouts;
//		for (int numRollouts = minRollouts; numRollouts < maxRollouts; numRollouts += 1000) {
		int trialLen = 100;
		float epsilon = 0.0001f;

		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/";
		// making sure resultsloc exits
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);

//		String example = "tro_example_new_small_noprob";

		PrismLog mainLog;
		if (debug)
			mainLog = new PrismFileLog("stdout");
		else
			mainLog = new PrismDevNullLog();

		Prism prism = new Prism(mainLog);
		String combString = "_prob_cost";
		String algoIden = "rtdp" + combString;
		PrismLog fileLog = new PrismFileLog(
				resultsLocation + "log_" + example + "_g_" + stateVal + "_" + algoIden + "_justmdp" + ".txt");//

		prism.initialise();
		prism.setEngine(Prism.EXPLICIT);

		mainLog.println("Initialised Prism");

		String modelFileName = testsLocation + example + "0.prism";
		ModulesFile modulesFile = prism.parseModelFile(new File(modelFileName)); // because the models are uniform

		ModulesFileModelGenerator modGen = new ModulesFileModelGenerator(modulesFile, prism);
		MDPModelGenerator mdpModGen = new MDPModelGenerator(modGen);

		List<State> gs = new ArrayList<State>();
		List<State> deadend = new ArrayList<State>();
		State goalState1 = new State(1);
		goalState1.setValue(0, stateVal);
		gs.add(goalState1);
		State de1 = new State(1);
		de1.setValue(0, -1);
		deadend.add(de1);

		Heuristic heuristicFunction = new EmptyHeuristic(gs, deadend);

		ArrayList<Objectives> tieBreakingOrder = new ArrayList<Objectives>();
		tieBreakingOrder.add(Objectives.Probability);
		tieBreakingOrder.add(Objectives.Cost);

		mainLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
		fileLog.println("Tie Breaking Order " + tieBreakingOrder.toString());

		mainLog.println("Initialising Greedy Bounds Difference Action Selector Function");
		fileLog.println("Initialising Greedy Bounds Difference Action Selector Function");

		ActionSelector actionSelection = new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder);// new
																									// ActionSelectorGreedyBoundsDiff(tieBreakingOrder);

		mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
		fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

		OutcomeSelector outcomeSelection = new OutcomeSelectorRandom();

		mainLog.println("Initialising Full Bellman Backup Function");
		fileLog.println("Initialising Full Bellman Backup Function");

		BackupNVI backupFunction = new BackupLabelledFullBelman(tieBreakingOrder, actionSelection, epsilon);

		mainLog.println("Initialising Reward Helper Function");
		fileLog.println("Initialising Reward Helper Function");

		RewardHelper rewardH = new RewardHelperMDP(mdpModGen);

		mainLog.println("Max Rollouts: " + numRollouts);
		mainLog.println("Max TrialLen: " + trialLen);
		fileLog.println("Max Rollouts: " + numRollouts);
		fileLog.println("Max TrialLen: " + trialLen);

		mainLog.println("\nInitialising THTS");
		fileLog.println("\nInitialising THTS");
		boolean doForwardBackup = true;
		TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) mdpModGen, numRollouts, trialLen,
				heuristicFunction, actionSelection, outcomeSelection, rewardH, backupFunction, doForwardBackup,
				tieBreakingOrder, mainLog, fileLog);

		mainLog.println("\nBeginning THTS");
		fileLog.println("\nBeginning THTS");
		thts.setName(example + "g_" + stateVal + "_rtdp" + combString);
		thts.setResultsLocation(resultsLocation);
		int numRolloutsTillSolved = thts.run(false);

		mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
		fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");

		goalack = thts.runThrough(new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder), resultsLocation);
		THTSRunInfo rinfo = new THTSRunInfo();
		rinfo.numRolloutsTillSolved = numRolloutsTillSolved;
		rinfo.goalFound = goalack[0];
		rinfo.initialStateSolved = goalack[1];

		return rinfo;

	}

	THTSRunInfo nestedLRTDPAvoid(String example, int stateVal, boolean debug) throws Exception {

		int minRollouts = 1000;
		boolean[] goalack = null;
		int numRollouts = minRollouts;
//		for (int numRollouts = minRollouts; numRollouts < maxRollouts; numRollouts += 1000) {
		int trialLen = 100;
		float epsilon = 0.0001f;

		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/";
		// making sure resultsloc exits
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);

//		String example = "tro_example_new_small_noprob";

		PrismLog mainLog;
		if (debug)
			mainLog = new PrismFileLog("stdout");
		else
			mainLog = new PrismDevNullLog();

		Prism prism = new Prism(mainLog);
		String combString = "_prob_cost_avoid";
		String algoIden = "rtdp" + combString;
		PrismLog fileLog = new PrismFileLog(
				resultsLocation + "log_" + example + "_g_" + stateVal + "_" + algoIden + "_justmdp" + ".txt");//

		prism.initialise();
		prism.setEngine(Prism.EXPLICIT);

		mainLog.println("Initialised Prism");

		String modelFileName = testsLocation + example + "0.prism";
		ModulesFile modulesFile = prism.parseModelFile(new File(modelFileName)); // because the models are uniform

		ModulesFileModelGenerator modGen = new ModulesFileModelGenerator(modulesFile, prism);
		MDPModelGenerator mdpModGen = new MDPModelGenerator(modGen);

		List<State> gs = new ArrayList<State>();
		List<State> deadend = new ArrayList<State>();
		State goalState1 = new State(1);
		goalState1.setValue(0, stateVal);
		gs.add(goalState1);
		State de1 = new State(1);
		de1.setValue(0, -1);
		deadend.add(de1);
		de1 = new State(1);
		de1.setValue(0, 5);
		deadend.add(de1);

		double hval = 20;
		Heuristic heuristicFunction = new EmptyHeuristic(gs, deadend, hval);

		ArrayList<Objectives> tieBreakingOrder = new ArrayList<Objectives>();
		tieBreakingOrder.add(Objectives.Probability);
		tieBreakingOrder.add(Objectives.Cost);

		mainLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
		fileLog.println("Tie Breaking Order " + tieBreakingOrder.toString());

		mainLog.println("Initialising Greedy Bounds Difference Action Selector Function");
		fileLog.println("Initialising Greedy Bounds Difference Action Selector Function");

		ActionSelector actionSelection = new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder);// new
																									// ActionSelectorGreedyBoundsDiff(tieBreakingOrder);

		mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
		fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

		OutcomeSelector outcomeSelection = new OutcomeSelectorRandom();

		mainLog.println("Initialising Full Bellman Backup Function");
		fileLog.println("Initialising Full Bellman Backup Function");

		BackupNVI backupFunction = new BackupLabelledFullBelman(tieBreakingOrder, actionSelection, epsilon);

		mainLog.println("Initialising Reward Helper Function");
		fileLog.println("Initialising Reward Helper Function");

		RewardHelper rewardH = new RewardHelperMDP(mdpModGen);

		mainLog.println("Max Rollouts: " + numRollouts);
		mainLog.println("Max TrialLen: " + trialLen);
		fileLog.println("Max Rollouts: " + numRollouts);
		fileLog.println("Max TrialLen: " + trialLen);

		mainLog.println("\nInitialising THTS");
		fileLog.println("\nInitialising THTS");
		boolean doForwardBackup = true;
		TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) mdpModGen, numRollouts, trialLen,
				heuristicFunction, actionSelection, outcomeSelection, rewardH, backupFunction, doForwardBackup,
				tieBreakingOrder, mainLog, fileLog);

		mainLog.println("\nBeginning THTS");
		fileLog.println("\nBeginning THTS");
		thts.setName(example + "g_" + stateVal + "_rtdp" + combString);
		thts.setResultsLocation(resultsLocation);
//		thts.run(false);
//
//		mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
//		fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
//
//		goalack = thts.runThrough(new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder), resultsLocation);
//		isSolved = goalack[1];
////			if (isSolved)
////				break;
////		}
//		return goalack;

		int numRolloutsTillSolved = thts.run(false);

		mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
		fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");

		goalack = thts.runThrough(new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder), resultsLocation);
		THTSRunInfo rinfo = new THTSRunInfo();
		rinfo.numRolloutsTillSolved = numRolloutsTillSolved;
		rinfo.goalFound = goalack[0];
		rinfo.initialStateSolved = goalack[1];

		return rinfo;

	}

	THTSRunInfo simpleLRTDPNoDeadends(String example, int stateVal, boolean debug) throws Exception {

		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/";
		// making sure resultsloc exits
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);

		PrismLog mainLog;
		if (debug)
			mainLog = new PrismFileLog("stdout");
		else
			mainLog = new PrismDevNullLog();

		Prism prism = new Prism(mainLog);
		String combString = "_cost_nodeadends";
		String algoIden = "rtdp" + combString;
		PrismLog fileLog = new PrismFileLog(
				resultsLocation + "log_" + example + "_g_" + stateVal + "_" + algoIden + "_justmdp" + ".txt");//

		prism.initialise();
		prism.setEngine(Prism.EXPLICIT);

		mainLog.println("Initialised Prism");

		String modelFileName = testsLocation + example + "0.prism";
		ModulesFile modulesFile = prism.parseModelFile(new File(modelFileName)); // because the models are uniform

		ModulesFileModelGenerator modGen = new ModulesFileModelGenerator(modulesFile, prism);
		MDPModelGenerator mdpModGen = new MDPModelGenerator(modGen);

		int maxRollouts = 1000;
		int trialLen = 100;
		float epsilon = 0.0001f;

		List<State> gs = new ArrayList<State>();
		State goalState1 = new State(1);
		goalState1.setValue(0, stateVal);
		gs.add(goalState1);

		Heuristic heuristicFunction = new EmptyHeuristic(gs, null);

		ArrayList<Objectives> tieBreakingOrder = new ArrayList<Objectives>();
		tieBreakingOrder.add(Objectives.Cost);

		mainLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
		fileLog.println("Tie Breaking Order " + tieBreakingOrder.toString());

		mainLog.println("Initialising Greedy Bounds Difference Action Selector Function");
		fileLog.println("Initialising Greedy Bounds Difference Action Selector Function");

		ActionSelector actionSelection = new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder);// new
																									// ActionSelectorGreedyBoundsDiff(tieBreakingOrder);

		mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
		fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

		OutcomeSelector outcomeSelection = new OutcomeSelectorRandom();

		mainLog.println("Initialising Full Bellman Backup Function");
		fileLog.println("Initialising Full Bellman Backup Function");

		BackupNVI backupFunction = new BackupLabelledFullBelman(tieBreakingOrder, actionSelection, epsilon);

		mainLog.println("Initialising Reward Helper Function");
		fileLog.println("Initialising Reward Helper Function");

		RewardHelper rewardH = new RewardHelperMDP(mdpModGen);

		mainLog.println("Max Rollouts: " + maxRollouts);
		mainLog.println("Max TrialLen: " + trialLen);
		fileLog.println("Max Rollouts: " + maxRollouts);
		fileLog.println("Max TrialLen: " + trialLen);

		mainLog.println("\nInitialising THTS");
		fileLog.println("\nInitialising THTS");
		boolean doForwardBackup = true;
		TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) mdpModGen, maxRollouts, trialLen,
				heuristicFunction, actionSelection, outcomeSelection, rewardH, backupFunction, doForwardBackup,
				tieBreakingOrder, mainLog, fileLog);

		mainLog.println("\nBeginning THTS");
		fileLog.println("\nBeginning THTS");
		thts.setName(example + "g_" + stateVal + "_rtdp" + combString);
		thts.setResultsLocation(resultsLocation);
		int numRolloutsTillSolved = thts.run(false);

		mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
		fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");

		boolean[] goalack = thts.runThrough(new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder),
				resultsLocation);
		THTSRunInfo rinfo = new THTSRunInfo();
		rinfo.numRolloutsTillSolved = numRolloutsTillSolved;
		rinfo.goalFound = goalack[0];
		rinfo.initialStateSolved = goalack[1];

		return rinfo;

	}

	THTSRunInfo simpleLRTDP(String example, int stateVal, boolean debug) throws Exception {

		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/";
		// making sure resultsloc exits
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);

//		String example = "tro_example_new_small_noprob";

		PrismLog mainLog;
		if (debug)
			mainLog = new PrismFileLog("stdout");
		else
			mainLog = new PrismDevNullLog();

		Prism prism = new Prism(mainLog);
		String combString = "_cost_deadends";
		String algoIden = "rtdp" + combString;
		PrismLog fileLog = new PrismFileLog(
				resultsLocation + "log_" + example + "_g_" + stateVal + "_" + algoIden + "_justmdp" + ".txt");//

		prism.initialise();
		prism.setEngine(Prism.EXPLICIT);

		mainLog.println("Initialised Prism");

		String modelFileName = testsLocation + example + "0.prism";
		ModulesFile modulesFile = prism.parseModelFile(new File(modelFileName)); // because the models are uniform

		ModulesFileModelGenerator modGen = new ModulesFileModelGenerator(modulesFile, prism);
		MDPModelGenerator mdpModGen = new MDPModelGenerator(modGen);

		int maxRollouts = 1000;
		int trialLen = 100;
		float epsilon = 0.0001f;

		List<State> gs = new ArrayList<State>();
		List<State> deadend = new ArrayList<State>();
		State goalState1 = new State(1);
		goalState1.setValue(0, stateVal);
		gs.add(goalState1);
		State de1 = new State(1);
		de1.setValue(0, -1);
		deadend.add(de1);

		Heuristic heuristicFunction = new EmptyHeuristic(gs, deadend);

		ArrayList<Objectives> tieBreakingOrder = new ArrayList<Objectives>();
		tieBreakingOrder.add(Objectives.Cost);

		mainLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
		fileLog.println("Tie Breaking Order " + tieBreakingOrder.toString());

		mainLog.println("Initialising Greedy Bounds Difference Action Selector Function");
		fileLog.println("Initialising Greedy Bounds Difference Action Selector Function");

		ActionSelector actionSelection = new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder);// new
																									// ActionSelectorGreedyBoundsDiff(tieBreakingOrder);

		mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
		fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

		OutcomeSelector outcomeSelection = new OutcomeSelectorRandom();

		mainLog.println("Initialising Full Bellman Backup Function");
		fileLog.println("Initialising Full Bellman Backup Function");

		BackupNVI backupFunction = new BackupLabelledFullBelman(tieBreakingOrder, actionSelection, epsilon);

		mainLog.println("Initialising Reward Helper Function");
		fileLog.println("Initialising Reward Helper Function");

		RewardHelper rewardH = new RewardHelperMDP(mdpModGen);

		mainLog.println("Max Rollouts: " + maxRollouts);
		mainLog.println("Max TrialLen: " + trialLen);
		fileLog.println("Max Rollouts: " + maxRollouts);
		fileLog.println("Max TrialLen: " + trialLen);

		mainLog.println("\nInitialising THTS");
		fileLog.println("\nInitialising THTS");
		boolean doForwardBackup = true;
		TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) mdpModGen, maxRollouts, trialLen,
				heuristicFunction, actionSelection, outcomeSelection, rewardH, backupFunction, doForwardBackup,
				tieBreakingOrder, mainLog, fileLog);

		mainLog.println("\nBeginning THTS");
		fileLog.println("\nBeginning THTS");
		thts.setName(example + "g_" + stateVal + "_rtdp" + combString);
		thts.setResultsLocation(resultsLocation);

		int numRolloutsTillSolved = thts.run(false);

		mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
		fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");

		boolean[] goalack = thts.runThrough(new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder),
				resultsLocation);
		THTSRunInfo rinfo = new THTSRunInfo();
		rinfo.numRolloutsTillSolved = numRolloutsTillSolved;
		rinfo.goalFound = goalack[0];
		rinfo.initialStateSolved = goalack[1];

		return rinfo;

	}

	THTSRunInfo simpleLRTDPAvoid(String example, int stateVal, boolean debug) throws Exception {

		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/";
		// making sure resultsloc exits
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);

//		String example = "tro_example_new_small_noprob";

		PrismLog mainLog;
		if (debug)
			mainLog = new PrismFileLog("stdout");
		else
			mainLog = new PrismDevNullLog();

		Prism prism = new Prism(mainLog);
		String combString = "_cost_deadends_avoid";
		String algoIden = "rtdp" + combString;
		PrismLog fileLog = new PrismFileLog(
				resultsLocation + "log_" + example + "_g_" + stateVal + "_" + algoIden + "_justmdp" + ".txt");//

		prism.initialise();
		prism.setEngine(Prism.EXPLICIT);

		mainLog.println("Initialised Prism");

		String modelFileName = testsLocation + example + "0.prism";
		ModulesFile modulesFile = prism.parseModelFile(new File(modelFileName)); // because the models are uniform

		ModulesFileModelGenerator modGen = new ModulesFileModelGenerator(modulesFile, prism);
		MDPModelGenerator mdpModGen = new MDPModelGenerator(modGen);

		int maxRollouts = 1000;
		int trialLen = 100;
		float epsilon = 0.0001f;

		List<State> gs = new ArrayList<State>();
		List<State> deadend = new ArrayList<State>();
		State goalState1 = new State(1);
		goalState1.setValue(0, stateVal);
		gs.add(goalState1);
		State de1 = new State(1);
		de1.setValue(0, -1);

		deadend.add(de1);
		de1 = new State(1);
		de1.setValue(0, 5);

		deadend.add(de1);

		double hval = 20;
		Heuristic heuristicFunction = new EmptyHeuristic(gs, deadend, hval);

		ArrayList<Objectives> tieBreakingOrder = new ArrayList<Objectives>();
		tieBreakingOrder.add(Objectives.Cost);

		mainLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
		fileLog.println("Tie Breaking Order " + tieBreakingOrder.toString());

		mainLog.println("Initialising Greedy Bounds Difference Action Selector Function");
		fileLog.println("Initialising Greedy Bounds Difference Action Selector Function");

		ActionSelector actionSelection = new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder);// new
																									// ActionSelectorGreedyBoundsDiff(tieBreakingOrder);

		mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
		fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

		OutcomeSelector outcomeSelection = new OutcomeSelectorRandom();

		mainLog.println("Initialising Full Bellman Backup Function");
		fileLog.println("Initialising Full Bellman Backup Function");

		BackupNVI backupFunction = new BackupLabelledFullBelman(tieBreakingOrder, actionSelection, epsilon);

		mainLog.println("Initialising Reward Helper Function");
		fileLog.println("Initialising Reward Helper Function");

		RewardHelper rewardH = new RewardHelperMDP(mdpModGen);

		mainLog.println("Max Rollouts: " + maxRollouts);
		mainLog.println("Max TrialLen: " + trialLen);
		fileLog.println("Max Rollouts: " + maxRollouts);
		fileLog.println("Max TrialLen: " + trialLen);

		mainLog.println("\nInitialising THTS");
		fileLog.println("\nInitialising THTS");
		boolean doForwardBackup = true;
		TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) mdpModGen, maxRollouts, trialLen,
				heuristicFunction, actionSelection, outcomeSelection, rewardH, backupFunction, doForwardBackup,
				tieBreakingOrder, mainLog, fileLog);

		mainLog.println("\nBeginning THTS");
		fileLog.println("\nBeginning THTS");
		thts.setName(example + "g_" + stateVal + "_rtdp" + combString);
		thts.setResultsLocation(resultsLocation);
		int numRolloutsTillSolved = thts.run(false);

		mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
		fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");

		boolean[] goalack = thts.runThrough(new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder),
				resultsLocation);
		THTSRunInfo rinfo = new THTSRunInfo();
		rinfo.numRolloutsTillSolved = numRolloutsTillSolved;
		rinfo.goalFound = goalack[0];
		rinfo.initialStateSolved = goalack[1];

		return rinfo;

	}

	boolean[] gssp(boolean debug) throws Exception {

		System.out.println(System.getProperty("user.dir"));
		String currentDir = System.getProperty("user.dir");
		String testsLocation = currentDir + "/tests/wkspace/tro_examples/";
		String resultsLocation = testsLocation + "results/";
		// making sure resultsloc exits
		createDirIfNotExist(resultsLocation);
		System.out.println("Results Location " + resultsLocation);

		String example = "gssp_paper_example";
		int maxRollouts = 1000;
		int trialLen = 100;
		float epsilon = 0.0001f;
		int stateVal = 5;
		PrismLog mainLog;
		if (debug)
			mainLog = new PrismFileLog("stdout");
		else
			mainLog = new PrismDevNullLog();
		Prism prism = new Prism(mainLog);
		String combString = "_cost_fret_nodeadends";
		String algoIden = "rtdp" + combString;
		PrismLog fileLog = new PrismFileLog(
				resultsLocation + "log_" + example + "_g_" + stateVal + "_" + algoIden + "_justmdp" + ".txt");//

		prism.initialise();
		prism.setEngine(Prism.EXPLICIT);

		mainLog.println("Initialised Prism");

		String modelFileName = testsLocation + example + ".prism";
		ModulesFile modulesFile = prism.parseModelFile(new File(modelFileName)); // because the models are uniform

		ModulesFileModelGenerator modGen = new ModulesFileModelGenerator(modulesFile, prism);
		MDPModelGenerator mdpModGen = new MDPModelGenerator(modGen);

		List<State> gs = new ArrayList<State>();
		State goalState1 = new State(1);
		goalState1.setValue(0, stateVal);
		gs.add(goalState1);

		Heuristic heuristicFunction = new EmptyHeuristic(gs, null);

		ArrayList<Objectives> tieBreakingOrder = new ArrayList<Objectives>();
		tieBreakingOrder.add(Objectives.Cost);

		mainLog.println("Tie Breaking Order " + tieBreakingOrder.toString());
		fileLog.println("Tie Breaking Order " + tieBreakingOrder.toString());

		mainLog.println("Initialising Greedy Bounds Difference Action Selector Function");
		fileLog.println("Initialising Greedy Bounds Difference Action Selector Function");

		ActionSelector actionSelection = new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder);// new
																									// ActionSelectorGreedyBoundsDiff(tieBreakingOrder);

		mainLog.println("Initialising Greedy Bounds Outcome Selector Function");
		fileLog.println("Initialising Greedy Bounds Outcome Selector Function");

		OutcomeSelector outcomeSelection = new OutcomeSelectorRandom();

		mainLog.println("Initialising Full Bellman Backup Function");
		fileLog.println("Initialising Full Bellman Backup Function");

		BackupNVI backupFunction = new BackupLabelledFullBelman(tieBreakingOrder, actionSelection, epsilon);

		mainLog.println("Initialising Reward Helper Function");
		fileLog.println("Initialising Reward Helper Function");

		RewardHelper rewardH = new RewardHelperGSSPPaper(mdpModGen);

		mainLog.println("Max Rollouts: " + maxRollouts);
		mainLog.println("Max TrialLen: " + trialLen);
		fileLog.println("Max Rollouts: " + maxRollouts);
		fileLog.println("Max TrialLen: " + trialLen);

		mainLog.println("\nInitialising THTS");
		fileLog.println("\nInitialising THTS");
		boolean doForwardBackup = true;
		TrialBasedTreeSearch thts = new TrialBasedTreeSearch((DefaultModelGenerator) mdpModGen, maxRollouts, trialLen,
				heuristicFunction, actionSelection, outcomeSelection, rewardH, backupFunction, doForwardBackup,
				tieBreakingOrder, mainLog, fileLog);

		mainLog.println("\nBeginning THTS");
		fileLog.println("\nBeginning THTS");
		thts.setName(example + "g_" + stateVal + "_rtdp" + combString);
		thts.setResultsLocation(resultsLocation);
		boolean fixSCCs = true;
		thts.run(fixSCCs);

		mainLog.println("\nGetting actions with Greedy Lower Bound Action Selector");
		fileLog.println("\nGetting actions with Greedy Lower Bound Action Selector");

		boolean[] goalack = thts.runThrough(new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder),
				resultsLocation);
		return goalack;

	}

}
