package thts.outcomeselector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import thts.treesearch.Objectives;
import thts.treesearch.ChanceNode;
import thts.treesearch.DecisionNode;

public class OutcomeSelectorBoundsGreedyBRTDP implements OutcomeSelector {
	ArrayList<Objectives> tieBreakingOrder;
	Random rgen;
	double tau = 10; // so the algorithm says its got to be greater than 1 and experimental evidence
						// says between 10 to 100

	public OutcomeSelectorBoundsGreedyBRTDP(ArrayList<Objectives> tieBreakingOrder, double tau) {
		this.tieBreakingOrder = tieBreakingOrder;
		this.tau = tau;
	}

	@Override
	public ArrayList<DecisionNode> selectOutcome(ChanceNode cn) {
		ArrayList<DecisionNode> selectedDNs = new ArrayList<>();
		// so now we just do this thing that was in the brtdp paper
		DecisionNode selectedDecisionNode = null;

		// select an action and choice
		// so for this decision node
		// we'll do a softmax kind of thing
		// if none of the nodes are initialised go ahead and choose one at random
		// if some are not initialised go a softmax
		// otherwise always choose greedily
		if (cn.allChildrenInitialised()) {
			selectedDecisionNode = doBoundsDiff(cn,cn.getChildren());
		} else {
			// do softmax
			rgen = new Random();
			ArrayList<DecisionNode> initChildren = cn.childrenWithInitialisedBounds();
			boolean pickFromInitialisedChildren = false;
			if (initChildren.size() > 0) {
				int choice = rgen.nextInt(2);
				if (choice == 0)
					pickFromInitialisedChildren = true;
			}
			if (pickFromInitialisedChildren) {
				// dogreedy from initialised kids
				selectedDecisionNode = doBoundsDiff(cn,initChildren);

			} else {
				// dorandom from unitialised kids
				initChildren = cn.childrenWithuninitialisedBounds();
				int chosenChild = rgen.nextInt(initChildren.size());
				selectedDecisionNode = initChildren.get(chosenChild);
			}
		}
		selectedDNs.add(selectedDecisionNode);
		return selectedDNs;

	}

	DecisionNode doBoundsDiff(ChanceNode cn, ArrayList<DecisionNode> dns) {
		HashMap<Objectives, Double> cnTermCrit = new HashMap<>();

		for (Objectives obj : tieBreakingOrder) {
			double val = cn.getBounds(obj).diff();
			val = val / tau;
			cnTermCrit.put(obj, val);
		}
		DecisionNode chosendn = null;
		// so there are two ways to do this
		// one i could just care about the task completion bit or the first objective
		// and when task completion is all done, you do the other one and so on till
		// all done
		// two i could care about all of them at the same time, but that would mean
		// a weighted sum
		// so i'm going to do one

		HashMap<Objectives, Double> sumdiffs = new HashMap<>();
		HashMap<Objectives, ArrayList<Double>> diffs = new HashMap<>();
		double currentSumDiff = 0;
		double currentDiff = 0;
		for (Objectives obj : tieBreakingOrder) {
			currentSumDiff = 0;
			ArrayList<Double> currentDiffs = new ArrayList<>();
			for (DecisionNode dn : dns) {
				currentDiff = dn.getBounds(obj).diff();
				currentDiffs.add(currentDiff);
				currentSumDiff += currentDiff;
			}
			diffs.put(obj, currentDiffs);
			sumdiffs.put(obj, currentSumDiff);
		}
		// normalising
		for (Objectives obj : tieBreakingOrder) {
			ArrayList<Double> currentDiffs = diffs.get(obj);
			double sum = sumdiffs.get(obj);
			for (int i = 0; i < currentDiffs.size(); i++) {
				double cdiff = currentDiffs.get(i);
				cdiff = cdiff / sum;
				currentDiffs.set(i, cdiff);
			}
			diffs.put(obj, currentDiffs);
		}
		Objectives objToConsider = null;
		for (Objectives obj : tieBreakingOrder) {
			if (sumdiffs.get(obj) >= cnTermCrit.get(obj)) {
				objToConsider = obj;
				break;
			}
		}
		if (objToConsider != null) {
			ArrayList<Double> sums = diffs.get(objToConsider);
			// Weighting this so we can do the whole random thing
			rgen = new Random();
			double cumulativesum = 0;
			double randVal = rgen.nextDouble();
			for (int i = 0; i < sums.size(); i++) {
				cumulativesum += sums.get(i);
				if (cumulativesum > randVal) {
					chosendn = dns.get(i);
					break;
				}

			}
		}
//		else
//		{
//			//everything here doesnt need to be explored
//		}

		return chosendn;
	}
//	DecisionNode doBoundsDiff(ArrayList<DecisionNode> dns) {
//		DecisionNode chosendn = null;
//		// first we've got to do the whole smoothing thing
//		// so for each decision node
//		// we get the difference in prob bounds
//		// generate a new prob
//		// and choose according to that
//
//		double sumdiffs = 0;
//		double diff = 0;
//		ArrayList<Double> diffs = null;
//		for (Objectives obj : tieBreakingOrder) {
//			diffs = new ArrayList<>();
//			sumdiffs = 0;
//			for (DecisionNode dn : dns) {
//				diff = dn.getBounds(obj).diff();
//				diffs.add(diff);
//				sumdiffs += diff;
//			}
//			if (sumdiffs != 0) {
//				break;
//			}
//		}
//		if (sumdiffs != 0) {
//			// create a new order
//			rgen = new Random();
//			double rand = rgen.nextDouble();
//			double weightedsum = 0;
//			for (int i = 0; i < dns.size(); i++) {
//				double weightedp = diffs.get(i) / sumdiffs;
//				weightedsum += weightedp;
//				if (weightedsum > rand) {
//					chosendn = dns.get(i);
//					break;
//				}
//			}
//		}
//
//		return chosendn;
//	}
}
