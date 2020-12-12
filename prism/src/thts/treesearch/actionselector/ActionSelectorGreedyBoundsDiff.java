package thts.actionselector;

import java.util.ArrayList;
import java.util.Random;

import thts.treesearch.Objectives;
import thts.treesearch.ChanceNode;
import thts.treesearch.DecisionNode;

public class ActionSelectorGreedyBoundsDiff implements ActionSelector {

	Random rgen;
	ArrayList<Objectives> tieBreakingOrder;

//	DefaultModelGenerator modGen; 
//	
	public ActionSelectorGreedyBoundsDiff(ArrayList<Objectives> tieBreakingOrder) {
		this.tieBreakingOrder = tieBreakingOrder;
	}

	@Override
	public ChanceNode selectAction(DecisionNode nd, boolean doMin) {
		ChanceNode selectedActionNode = null;

		// select an action and choice
		// so for this decision node
		// we'll do a softmax kind of thing
		// if none of the nodes are initialised go ahead and choose one at random
		// if some are not initialised go a softmax
		// otherwise always choose greedily

		if (nd.allChildrenInitialised()) {
			// dogreedy
			ChanceNode greedyAction = null;
			ChanceNode tempChoice = null;
			for (Object a : nd.getChildren().keySet()) {
				ChanceNode cn = nd.getChild(a);
				if (greedyAction == null)
					greedyAction = cn;
				else {
					tempChoice = getNodeWithGreaterBoundsDiff(greedyAction, cn);
					if (tempChoice != null) {
						greedyAction = cn;
					}
				}
			}
			selectedActionNode = greedyAction;
		} else {
			// do softmax
			rgen = new Random();
			ArrayList<ChanceNode> initChildren = nd.childrenWithInitialisedBounds();
			boolean pickFromInitialisedChildren = false;
			if (initChildren.size() > 0) {
				int choice = rgen.nextInt(2);
				if (choice == 0)
					pickFromInitialisedChildren = true;
			}
			if (pickFromInitialisedChildren) {
				// dogreedy from initialised kids
				ChanceNode greedyAction = null;
				ChanceNode tempChoice = null;
				for (ChanceNode cn : initChildren) {
					if (greedyAction == null)
						greedyAction = cn;
					else {
						tempChoice = getNodeWithGreaterBoundsDiff(greedyAction, cn);
						if (tempChoice != null) {
							greedyAction = cn;
						}
					}
				}
			} else {
				// dorandom from unitialised kids
				initChildren = nd.childrenWithuninitialisedBounds();
				int chosenChild = rgen.nextInt(initChildren.size());
				selectedActionNode = initChildren.get(chosenChild);
			}
		}
		return selectedActionNode;

	}

	private ChanceNode getNodeWithGreaterBoundsDiff(ChanceNode c1, ChanceNode c2) {
		if (c1 == c2)
			return c1;
		ChanceNode toret = null;
		for (Objectives obj : tieBreakingOrder) {
			// we just want the difference between the bounds to be samller
			// so we choose the one with greater diff
			if (c2.getBounds(obj).diff() > c1.getBounds(obj).diff()) {
				toret = c2;
				break;
			}

		}
		return toret;
	}

}
