package thts.actionselector;

import thts.treesearch.ChanceNode;
import thts.treesearch.DecisionNode;

import java.util.ArrayList;
import java.util.Random;

//takes as input an action selection class and a value for softmax 
public class ActionSelectorSoftmax implements ActionSelector {

	ActionSelector actSel;
	double epsilon;
	Random rgen;

	public ActionSelectorSoftmax(ActionSelector actSel, double epsilon) {
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
