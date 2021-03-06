package thts.treesearch.actionselector;

import thts.treesearch.utils.ChanceNode;
import thts.treesearch.utils.DecisionNode;

import java.util.ArrayList;
import java.util.Random;

//takes as input an action selection class and a value for softmax 
public class ActionSelectorEpsilonProb implements ActionSelector {

	ActionSelector actSel;
	double epsilon;
	Random rgen;

	public ActionSelectorEpsilonProb(ActionSelector actSel, double epsilon) {
		this.actSel = actSel;
		this.epsilon = epsilon;
	}

	@Override
	public ChanceNode selectAction(DecisionNode nd, boolean doMin) throws Exception {
		rgen = new Random();
		double prob = rgen.nextDouble();
		if (prob > epsilon) {
			ArrayList<ChanceNode> initChildren = nd.childrenWithInitialisedBounds();

			int chosenChild = rgen.nextInt(initChildren.size());
			return initChildren.get(chosenChild);

		} else
			return actSel.selectAction(nd, doMin);

	}

}
