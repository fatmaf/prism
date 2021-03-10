package thts.treesearch.outcomeselector;

import thts.treesearch.utils.ChanceNode;
import thts.treesearch.utils.DecisionNode;

import java.util.ArrayList;
import java.util.Random;

public class OutcomeSelectorProbSkipSolved implements OutcomeSelector {

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
			double phere = 0;
			if (d.canHaveChildren())
				phere = d.getTranProb(cn);
			psum += phere;
		}

		double pcumsum = 0;
		if (psum > 0) {
			for (DecisionNode d : initChildren) {
				double phere = 0;
				if (d.canHaveChildren()) {
					phere = d.getTranProb(cn);
					phere = phere / psum;
				}
				pcumsum += phere;
				if (pcumsum > prob) {
					selectedDecisionNode = d;
					break;
				}
			}
		}

		selectedDNs.add(selectedDecisionNode);
		return selectedDNs;

	}

}
