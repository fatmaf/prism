package thtsNew;

import java.util.ArrayList;
import java.util.Random;

import thts.Bounds;
import thts.Objectives;

public class ActionSelectorGreedyLowerBound implements ActionSelector {

	Random rgen;
	ArrayList<Objectives> tieBreakingOrder;
	boolean simple = false;

	public ActionSelectorGreedyLowerBound(ArrayList<Objectives> tieBreakingOrder) {
		this.tieBreakingOrder = tieBreakingOrder;
	}

	public ActionSelectorGreedyLowerBound(ArrayList<Objectives> tieBreakingOrder, boolean dosimple) {
		this.tieBreakingOrder = tieBreakingOrder;
		simple = dosimple;
	}

	public ChanceNode selectActionSimpleArrayList(ArrayList<ChanceNode> cns) {
		ChanceNode selectedActionNode = null;
		// if bounds are not initialised choose the one with uninitialised bounds
		// just the next one
		ChanceNode greedyAction = null;
		ChanceNode tempChoice = null;
		for (ChanceNode cn : cns) {

			if (greedyAction == null)
				greedyAction = cn;
			else {
				tempChoice = getNodeWithBetterLowerBound(greedyAction, cn);
				if (tempChoice != null) {
					greedyAction = cn;
				}
			}
		}
		selectedActionNode = greedyAction;

		return selectedActionNode;

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
				if (greedyAction == null)
					greedyAction = cn;
				else {
					tempChoice = getNodeWithBetterLowerBound(greedyAction, cn);
					if (tempChoice != null) {
						greedyAction = cn;
					}
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
		if (simple) {
			return this.selectActionSimple(nd);
		} else {
			return this.selectActionSoftmax(nd, doMin);
		}
	}

	public ChanceNode selectActionSoftmax(DecisionNode nd, boolean doMin) {
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
					tempChoice = getNodeWithBetterLowerBound(greedyAction, cn);
					if (tempChoice != null) {
						greedyAction = cn;
					}
				}
			}
//			ArrayList<ChanceNode> allTheSame = new ArrayList<>();
//			allTheSame.add(greedyAction);
//			// perhaps there are others with equal bounds
//			for (Object a : nd.getChildren().keySet()) {
//				ChanceNode cn = nd.getChild(a);
//				if (cn != greedyAction) {
//					if (this.sameBounds(greedyAction, cn)) {
//						allTheSame.add(cn);
//					}
//				}
//			}
//			if (allTheSame.size() > 1) {
//				// then randomly select a node
//				rgen = new Random();
//				int chosenChild = rgen.nextInt(allTheSame.size());
//				greedyAction = allTheSame.get(chosenChild);
//			}
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
						tempChoice = getNodeWithBetterLowerBound(greedyAction, cn);
						if (tempChoice != null) {
							greedyAction = cn;
						}
					}
				}
				selectedActionNode = greedyAction;
			} else {
				// dorandom from unitialised kids
				initChildren = nd.childrenWithuninitialisedBounds();
				int chosenChild = rgen.nextInt(initChildren.size());
				selectedActionNode = initChildren.get(chosenChild);
			}
		}
//		if(selectedActionNode == null)
//			System.out.println("Error");
		return selectedActionNode;
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
