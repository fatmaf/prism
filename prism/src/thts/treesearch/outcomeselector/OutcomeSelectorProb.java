package thts.treesearch.outcomeselector;

import thts.treesearch.utils.ChanceNode;
import thts.treesearch.utils.DecisionNode;

import java.util.ArrayList;
import java.util.Random;

public class OutcomeSelectorProb implements OutcomeSelector {

	Random rgen;

	@Override
	public ArrayList<DecisionNode> selectOutcome(ChanceNode cn) {
		ArrayList<DecisionNode> selectedDNs = new ArrayList<>();
		// so now we just do this thing that was in the brtdp paper
		DecisionNode selectedDecisionNode = null;

		rgen = new Random();
		ArrayList<DecisionNode> initChildren = cn.getChildren();

		double prob = rgen.nextDouble();
		double psum = 0;
		for (DecisionNode d : initChildren) {
			double phere = d.getTranProb(cn);
			psum += phere;
			if (psum > prob) {
				selectedDecisionNode = d;
				break;
			}

		}

		selectedDNs.add(selectedDecisionNode);
		return selectedDNs;

	}

}
