package thtsNew;

import java.util.ArrayList;
import java.util.Random;

import thts.Objectives;

public class OutcomeSelectorBoundsGreedy implements OutcomeSelector {
	ArrayList<Objectives> tieBreakingOrder;
	Random rgen;

	public OutcomeSelectorBoundsGreedy(ArrayList<Objectives> tieBreakingOrder) {
		this.tieBreakingOrder = tieBreakingOrder;
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
			selectedDecisionNode = doBoundsDiff(cn.getChildren());
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
				selectedDecisionNode = doBoundsDiff(initChildren);

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

	DecisionNode doBoundsDiff(ArrayList<DecisionNode> dns) {
		DecisionNode chosendn = null;
		// first we've got to do the whole smoothing thing
		// so for each decision node
		// we get the difference in prob bounds
		// generate a new prob
		// and choose according to that

		double sumdiffs = 0;
		double diff = 0;
		ArrayList<Double> diffs = null;
		for (Objectives obj : tieBreakingOrder) {
			diffs = new ArrayList<>();
			sumdiffs = 0;
			for (DecisionNode dn : dns) {
				diff = dn.getBounds(obj).diff();
				diffs.add(diff);
				sumdiffs += diff;
			}
			if (sumdiffs != 0) {
				break;
			}
		}
		if(sumdiffs !=0) {
		// create a new order
		rgen = new Random();
		double rand = rgen.nextDouble();
		double weightedsum = 0;
		for (int i = 0; i < dns.size(); i++) {
			double weightedp = diffs.get(i) / sumdiffs;
			weightedsum += weightedp;
			if (weightedsum > rand) {
				chosendn = dns.get(i);
				break;
			}
		}
		}
		else
		{
			//choose any or return null ? 
			//for now choose any 
			rgen = new Random(); 
			int chosenind = rgen.nextInt(dns.size());
			chosendn = dns.get(chosenind);
		}
		return chosendn;
	}
}
