package thtsNew;

import java.util.ArrayList;
import java.util.Random;

import thts.Bounds;
import thts.Objectives;

public class ActionSelectorGreedySimpleUpperLowerBound implements ActionSelector {

	Random rgen;
	ArrayList<Objectives> tieBreakingOrder;

	public ActionSelectorGreedySimpleUpperLowerBound(ArrayList<Objectives> tieBreakingOrder) {
		this.tieBreakingOrder = tieBreakingOrder;
	}

	public ChanceNode selectActionSimple(DecisionNode nd) {
		rgen = new Random();
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
			ArrayList<ChanceNode> sameNodes = new ArrayList<>(); 
			sameNodes.add(greedyAction);
			for (Object a : nd.getChildren().keySet()) {
				ChanceNode cn = nd.getChild(a);
				if(cn!=greedyAction)
				{
					if(sameBounds(cn,greedyAction))
					{
						sameNodes.add(cn);
					}
				}
				
			}
			if(sameNodes.size()>1)
			{
				int choice = rgen.nextInt(sameNodes.size()); 
				greedyAction = sameNodes.get(choice);
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

	boolean sameBounds(ChanceNode c1, ChanceNode c2) {
		if (c1 == c2)
			return true;

		boolean same = true;
		for (Objectives obj : tieBreakingOrder) {
			// we just want the difference between the bounds to be samller
			// so we choose the one with greater diff
			Bounds c2Bounds = c2.getBounds(obj);
			Bounds c1Bounds = c1.getBounds(obj);

			if (obj == Objectives.TaskCompletion || obj == Objectives.Probability || obj == Objectives.Progression) {
				if (c2Bounds.getUpper() != c1Bounds.getUpper()) {
					same = false;
					break;
				}
			} else if (obj == Objectives.Cost) {
				if (c2Bounds.getLower() != c1Bounds.getLower()) {
					same = false;
					break;
				}
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
				if (c2Bounds.getUpper() > c1Bounds.getUpper()) {
					toret = c2;
					breakNow = true;

				} else if (c2Bounds.getUpper() != c1Bounds.getUpper()) {
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
