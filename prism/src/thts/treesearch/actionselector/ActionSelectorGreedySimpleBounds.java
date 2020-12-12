package thts.actionselector;

import java.util.ArrayList;
import java.util.Random;

import thts.old.Bounds;
import thts.treesearch.Objectives;
import thts.treesearch.ChanceNode;
import thts.treesearch.DecisionNode;

public class ActionSelectorGreedySimpleBounds implements ActionSelector {

	Random rgen;
	ArrayList<Objectives> tieBreakingOrder;
	boolean useUpperFirst = false;

	// pretty much the same as the greedy simple lower bounds one
	// but uses the upper bound as a tie breaker
	// another variant could be to choose the action that has the greatest
	// difference between bounds
	// here we're technically choosing the action that has the lowest difference
	// between bounds
	public ActionSelectorGreedySimpleBounds(ArrayList<Objectives> tieBreakingOrder, boolean useUpperFirst) {
		this.tieBreakingOrder = tieBreakingOrder;
		this.useUpperFirst = useUpperFirst;
	}

	public ChanceNode selectActionSimple(DecisionNode nd) {
		ChanceNode selectedActionNode = null;
		// if bounds are not initialised choose the one with uninitialised bounds
		// just the next one
		if (nd.allChildrenInitialised()) {
			ChanceNode greedyAction = null;
			ChanceNode tempChoice = null;
			for (Object a : nd.getChildren().keySet()) {
				ChanceNode cn = nd.getChild(a);
				if (cn.ignoreAction)
					continue;
				if (greedyAction == null)
					greedyAction = cn;
				else {
					if (this.useUpperFirst)
						tempChoice = getNodeWithBetterUpperBound(greedyAction, cn);
					else
						tempChoice = getNodeWithBetterLowerBound(greedyAction, cn);
					if (tempChoice != null) {
						greedyAction = cn;
					}
				}
			}

			ArrayList<ChanceNode> actionsWithTheSameLowerBound = new ArrayList<>();
//			actionsWithTheSameLowerBound.add(greedyAction); 
			for (Object a : nd.getChildren().keySet()) {
				ChanceNode cn = nd.getChild(a);
//				if(cn!=greedyAction)
//				{
//					
//				}
				if (sameBounds(cn, greedyAction, useUpperFirst)) {
					actionsWithTheSameLowerBound.add(cn);
				}
			}
			for (ChanceNode cn : actionsWithTheSameLowerBound) {
				if (this.useUpperFirst)
					tempChoice = getNodeWithBetterLowerBound(greedyAction, cn);
				else
					tempChoice = getNodeWithBetterUpperBound(greedyAction, cn);
				if (tempChoice != null) {
					greedyAction = cn;
				}
			}
			selectedActionNode = greedyAction;
		} else {
			ArrayList<ChanceNode> initChildren = nd.childrenWithuninitialisedBounds();
			selectedActionNode = initChildren.get(0);
		}
		return selectedActionNode;
	}

	@Override
	public ChanceNode selectAction(DecisionNode nd, boolean doMin) {
		return this.selectActionSimple(nd);

	}

	boolean sameBounds(ChanceNode c1, ChanceNode c2, boolean useUpper) {
		if (c1 == c2)
			return true;

		boolean same = true;
		for (Objectives obj : tieBreakingOrder) {
			// we just want the difference between the bounds to be samller
			// so we choose the one with greater diff
			Bounds c2Bounds = c2.getBounds(obj);
			Bounds c1Bounds = c1.getBounds(obj);
			double c2b = c2Bounds.getLower();
			double c1b = c1Bounds.getLower();
			if (useUpper) {
				c2b = c2Bounds.getUpper();
				c1b = c1Bounds.getUpper();
			}
			if (c2b != c1b) {
				same = false;
				break;
			}
		}
		return same;
	}

	private ChanceNode getNodeWithBetterLowerBound(ChanceNode c1, ChanceNode c2) {
		if (c1 == c2)
			return c1;
		ChanceNode toret = null;
		for (Objectives obj : tieBreakingOrder) {
			// we just want the difference between the bounds to be samller
			// so we choose the one with greater diff
			Bounds c2Bounds = c2.getBounds(obj);
			Bounds c1Bounds = c1.getBounds(obj);
			boolean breakNow = false;
			switch (obj) {
			case Probability:
			case TaskCompletion:
			case Progression: {
				if (c2Bounds.getLower() > c1Bounds.getLower()) {
					toret = c2;
					breakNow = true;

				} else if (c2Bounds.getLower() != c1Bounds.getLower()) {
					breakNow = true;
				}
				break;
			}
			case Cost: {
				if (c2Bounds.getLower() < c1Bounds.getLower()) {
					toret = c2;
					breakNow = true;

				} else if (c2Bounds.getLower() != c1Bounds.getLower()) {
					breakNow = true;
				}
				break;
			}
			}
			if (breakNow)
				break;

		}
		return toret;
	}

	private ChanceNode getNodeWithBetterUpperBound(ChanceNode c1, ChanceNode c2) {
		if (c1 == c2)
			return c1;
		ChanceNode toret = null;
		for (Objectives obj : tieBreakingOrder) {
			// we just want the difference between the bounds to be samller
			// so we choose the one with greater diff
			Bounds c2Bounds = c2.getBounds(obj);
			Bounds c1Bounds = c1.getBounds(obj);
			boolean breakNow = false;
			switch (obj) {
			case Probability:
			case TaskCompletion:
			case Progression: {
				if (c2Bounds.getUpper() > c1Bounds.getUpper()) {
					toret = c2;
					breakNow = true;

				} else if (c2Bounds.getUpper() != c1Bounds.getUpper()) {
					breakNow = true;
				}
				break;
			}
			case Cost: {
				if (c2Bounds.getUpper() < c1Bounds.getUpper()) {
					toret = c2;
					breakNow = true;

				} else if (c2Bounds.getUpper() != c1Bounds.getUpper()) {
					breakNow = true;
				}
				break;
			}
			}
			if (breakNow)
				break;

		}
		return toret;
	}

}
