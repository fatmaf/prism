package thtsNew;

import java.util.ArrayList;
import java.util.Random;
import thts.Bounds;
import thts.Objectives;

public class ActionSelectorGreedyTieBreakRandomLowerBound implements ActionSelector {

	Random rgen;
	ArrayList<Objectives> tieBreakingOrder;
	

	public ActionSelectorGreedyTieBreakRandomLowerBound(ArrayList<Objectives> tieBreakingOrder) {
		this.tieBreakingOrder = tieBreakingOrder;
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
					tempChoice = getNodeWithBetterLowerBound(greedyAction, cn);
					if (tempChoice != null) {
						greedyAction = cn;
					}
				}
			}
			ArrayList<ChanceNode> allTheSame = new ArrayList<>();
			allTheSame.add(greedyAction);
			// perhaps there are others with equal bounds
			for (Object a : nd.getChildren().keySet()) {
				ChanceNode cn = nd.getChild(a);
				if (cn != greedyAction) {
					if (this.sameBounds(greedyAction, cn)) {
						allTheSame.add(cn);
					}
				}
			}
			if (allTheSame.size() > 1) {
				// then randomly select a node
				rgen = new Random();
				int chosenChild = rgen.nextInt(allTheSame.size());
				greedyAction = allTheSame.get(chosenChild);
			}
			
			
			//just a check to see if there are anyones that dont have s
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


	boolean sameBounds(ChanceNode c1, ChanceNode c2) {
		if (c1 == c2)
			return true;

		boolean same = true;
		for (Objectives obj : tieBreakingOrder) {
			// we just want the difference between the bounds to be samller
			// so we choose the one with greater diff
			Bounds c2Bounds = c2.getBounds(obj);
			Bounds c1Bounds = c1.getBounds(obj);

			if (c2Bounds.getLower() != c1Bounds.getLower()) {
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
}
