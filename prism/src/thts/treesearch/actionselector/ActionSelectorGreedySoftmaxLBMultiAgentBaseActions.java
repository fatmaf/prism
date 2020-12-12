package thts.actionselector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import prism.PrismException;

import thts.old.Bounds;
import thts.treesearch.Objectives;
import thts.treesearch.ChanceNode;
import thts.treesearch.DecisionNode;
import thts.modelgens.MultiAgentNestedProductModelGenerator;
import thts.scratch.BaseActionInfo;

public class ActionSelectorGreedySoftmaxLBMultiAgentBaseActions implements ActionSelector {

	Random rgen;
	ArrayList<Objectives> tieBreakingOrder;
	MultiAgentNestedProductModelGenerator mapmg;

	public ActionSelectorGreedySoftmaxLBMultiAgentBaseActions(ArrayList<Objectives> tieBreakingOrder,
			MultiAgentNestedProductModelGenerator mapmg) {
		this.tieBreakingOrder = tieBreakingOrder;
		this.mapmg = mapmg;
	}

	public ChanceNode selectActionFromBaseActions(DecisionNode nd) throws PrismException {
		ChanceNode selectedActionNode = null;

		if (nd.allChildrenInitialised()) {
			rgen = new Random(); 
			if(rgen.nextDouble()<0.5) {

			// so now we get the best base action for each robot
			// for each robot
			// find the best base action
			ArrayList<String> bestRobotActions = new ArrayList<>();
			ArrayList<HashMap<Objectives, Bounds>> bestRobotBounds = new ArrayList<>();
			for (int r = 0; r < nd.baseActionsForRobot.size(); r++) {
				HashMap<Object, BaseActionInfo> robotActions = nd.baseActionsForRobot.get(r);
				Object bestAction = null;
				HashMap<Objectives, Bounds> bestBounds = null;
				for (Object a : robotActions.keySet()) {
					if (bestAction == null) {
						bestAction = a;
						bestBounds = robotActions.get(bestAction).bounds;
					} else {

						HashMap<Objectives, Bounds> currentBounds = robotActions.get(bestAction).bounds;
						if (secondBoundsBetterThanFirstBound(bestBounds, currentBounds))

						{
							bestAction = a;
							bestBounds = currentBounds;
						}

					}
				}
				bestRobotActions.add(bestAction.toString());
				bestRobotBounds.add(bestBounds);
			}
			// now create a joint action
			String ja = mapmg.createJointActionFromString(bestRobotActions);
			if(nd.children.containsKey(ja))
			selectedActionNode = nd.getChild(ja);
			if(selectedActionNode == null)
			{
				throw new PrismException("In action selector using base actions, can not find joint action");
			}
			}
			else
			{
				ArrayList<ChanceNode> initChildren = nd.childrenWithInitialisedBounds(); 
				rgen = new Random(); 
				int chosenChild=rgen.nextInt(initChildren.size()); 
				selectedActionNode = initChildren.get(chosenChild);
			}
			// now we've got to find it
			// I guess I just want to test this now.
//			System.out.println(ja);

		} else {
			ArrayList<ChanceNode> initChildren = nd.childrenWithuninitialisedBounds();
			selectedActionNode = initChildren.get(0);
		}

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

			// randomly choose this action or another
			rgen = new Random();
			if (rgen.nextDouble() > 0.5) {
				rgen = new Random();
				ArrayList<ChanceNode> initChildren = nd.childrenWithInitialisedBounds();

				int chosenChild = rgen.nextInt(initChildren.size());
				greedyAction = initChildren.get(chosenChild);

			}

			selectedActionNode = greedyAction;
		} else {
			ArrayList<ChanceNode> initChildren = nd.childrenWithuninitialisedBounds();
			selectedActionNode = initChildren.get(0);
		}
		return selectedActionNode;
	}

	@Override
	public ChanceNode selectAction(DecisionNode nd, boolean doMin) throws Exception{
		// how to select a node
		// first we check if the base action stuff is null
		// if it is we initialise our base action stuff to the action values
		// if not we just do an update
		if (!baseActionsInitialised(nd)) {
			updateBaseActionsBetter(nd, true);
		} else {
			updateBaseActionsBetter(nd, false);
		}
		// now we've got to use this to select the best action
		// so what do we do ?
		// just the same thing but we've got to do more
		 return selectActionFromBaseActions(nd);
//		return this.selectActionSimple(nd);

	}

	private void updateBaseActionsAverage(DecisionNode nd, boolean init) {
		// initialise base actions
		// so we go over all the actions
		// we need to break these into robot actions
		// then we need to do shit
		if (nd.canHaveChildren()) {
			for (Object a : nd.getChildren().keySet()) {

				String aString = a.toString();
				ArrayList<String> robotActions = mapmg.getRobotActions(aString);

				HashMap<Objectives, Bounds> bounds = nd.getChild(a).bounds;

				for (int r = 0; r < robotActions.size(); r++) {
					boolean addAction = false;
					String robotAction = robotActions.get(r);
					// an action for each robot
					if (nd.baseActionsForRobot.get(r) == null) {
						addAction = true;
					} else {
						if (!nd.baseActionsForRobot.get(r).containsKey(robotAction)) {
							addAction = true;
						}
					}
					if (addAction) {
						nd.baseActionsForRobot.get(r).put(robotAction, new BaseActionInfo(bounds));
					} else {
						if (init)
							nd.baseActionsForRobot.get(r).get(robotAction).increaseOccurrence();
						// now we can check if there's something better

						for (Objectives obj : bounds.keySet()) {
							nd.baseActionsForRobot.get(r).get(robotAction).bounds.get(obj).add(bounds.get(obj));
						}

					}

				}

			}
			for (int r = 0; r < nd.baseActionsForRobot.size(); r++) {

				HashMap<Object, BaseActionInfo> robotstuff = nd.baseActionsForRobot.get(r);
				for (Object a : robotstuff.keySet()) {
					BaseActionInfo robotAction = robotstuff.get(a);
					robotAction.divideBoundsByOccurence();

				}

			}
		}

	}

	private void updateBaseActionsBetter(DecisionNode nd, boolean init) {
		// initialise base actions
		// so we go over all the actions
		// we need to break these into robot actions
		// then we need to do shit
		if (init) {
			if (nd.baseActionsForRobot == null) {
				nd.baseActionsForRobot = new ArrayList<>();

			}
		}
		if (nd.canHaveChildren()) {
			boolean firstAction = true; 
			for (Object a : nd.getChildren().keySet()) {

				String aString = a.toString();
				ArrayList<String> robotActions = mapmg.getRobotActions(aString);

				HashMap<Objectives, Bounds> bounds = nd.getChild(a).bounds;

				for (int r = 0; r < robotActions.size(); r++) {
					boolean addAction = false;
					String robotAction = robotActions.get(r);
					// an action for each robot
					while (nd.baseActionsForRobot.size() <= r)
						nd.baseActionsForRobot.add(new HashMap<Object, BaseActionInfo>());

					if (!nd.baseActionsForRobot.get(r).containsKey(robotAction)) {
						addAction = true;
					}
					if(firstAction)
					{
						addAction = true; 
					}

					if (addAction) {

						nd.baseActionsForRobot.get(r).put(robotAction, new BaseActionInfo(bounds));
					} else {
						if (init)
							nd.baseActionsForRobot.get(r).get(robotAction).increaseOccurrence();
						
						// now we can check if there's something better
						HashMap<Objectives, Bounds> currentBounds = nd.baseActionsForRobot.get(r)
								.get(robotAction).bounds;
						if (secondBoundsBetterThanFirstBound(currentBounds, bounds)) {
							nd.baseActionsForRobot.get(r).get(robotAction).bounds = bounds;
						}

					}

				}
				firstAction = false; 
			}
		}

	}

	private boolean baseActionsInitialised(DecisionNode nd) {
		boolean actionsInitialised = true;
		if (nd.baseActionsForRobot != null) {
			for (int r = 0; r < nd.baseActionsForRobot.size(); r++) {
				if (nd.baseActionsForRobot.get(r) == null) {
					actionsInitialised = false;
					break;
				}
			}
		} else {
			actionsInitialised = false;
		}
		return actionsInitialised;
	}
//	public ChanceNode selectActionSoftmax(DecisionNode nd, boolean doMin) {
//		ChanceNode selectedActionNode = null;
//
//		// select an action and choice
//		// so for this decision node
//		// we'll do a softmax kind of thing
//		// if none of the nodes are initialised go ahead and choose one at random
//		// if some are not initialised go a softmax
//		// otherwise always choose greedily
//		if (nd.allChildrenInitialised()) {
//			// dogreedy softmax
//			double randomProb = 0;
//			rgen = new Random();
//			if (rgen.nextDouble() > randomProb) {
//				// greedy
//				ChanceNode greedyAction = null;
//				ChanceNode tempChoice = null;
//				for (Object a : nd.getChildren().keySet()) {
//					ChanceNode cn = nd.getChild(a);
//					if (cn.ignoreAction)
//						continue;
//					if (greedyAction == null)
//						greedyAction = cn;
//					else {
//						tempChoice = getNodeWithBetterLowerBound(greedyAction, cn);
//						if (tempChoice != null) {
//							greedyAction = cn;
//						}
//					}
//				}
//				ArrayList<ChanceNode> allTheSame = new ArrayList<>();
//				allTheSame.add(greedyAction);
//				// perhaps there are others with equal bounds
//				for (Object a : nd.getChildren().keySet()) {
//					ChanceNode cn = nd.getChild(a);
//					if (cn != greedyAction) {
//						if (this.sameBounds(greedyAction, cn)) {
//							allTheSame.add(cn);
//						}
//					}
//				}
//				if (allTheSame.size() > 1) {
//					// then randomly select a node
//					rgen = new Random();
//					int chosenChild = rgen.nextInt(allTheSame.size());
//					greedyAction = allTheSame.get(chosenChild);
//				}
//				selectedActionNode = greedyAction;
//			} else {
//				// do a random selection
//				rgen = new Random();
//				ArrayList<ChanceNode> initChildren = nd.childrenWithInitialisedBounds();
//				int chosenChild = rgen.nextInt(initChildren.size());
//				selectedActionNode = initChildren.get(chosenChild);
//			}
//		} else {
//			// do softmax
//			rgen = new Random();
//			ArrayList<ChanceNode> initChildren = nd.childrenWithInitialisedBounds();
//			boolean pickFromInitialisedChildren = false;
//			if (initChildren.size() > 0) {
//				int choice = rgen.nextInt(2);
//				if (choice == 0)
//					pickFromInitialisedChildren = true;
//			}
//			if (pickFromInitialisedChildren) {
//				// dogreedy from initialised kids
//				ChanceNode greedyAction = null;
//				ChanceNode tempChoice = null;
//				for (ChanceNode cn : initChildren) {
//					if (cn.ignoreAction)
//						continue;
//					if (greedyAction == null)
//						greedyAction = cn;
//					else {
//						tempChoice = getNodeWithBetterLowerBound(greedyAction, cn);
//						if (tempChoice != null) {
//							greedyAction = cn;
//						}
//					}
//				}
//				selectedActionNode = greedyAction;
//			} else {
//				// dorandom from unitialised kids
//				initChildren = nd.childrenWithuninitialisedBounds();
//				int chosenChild = rgen.nextInt(initChildren.size());
//				selectedActionNode = initChildren.get(chosenChild);
//			}
//		}
////		if(selectedActionNode == null)
////			System.out.println("Error");
//		return selectedActionNode;
//	}

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

	private boolean secondBoundsBetterThanFirstBound(HashMap<Objectives, Bounds> b1, HashMap<Objectives, Bounds> b2) {
		boolean isBetter = false;
		for (Objectives obj : tieBreakingOrder) {
			// we just want the difference between the bounds to be samller
			// so we choose the one with greater diff
			Bounds c2Bounds = b2.get(obj);
			Bounds c1Bounds = b1.get(obj);
			boolean breakNow = false;
			switch (obj) {
			case Probability:
			case TaskCompletion:
			case Progression: {
				if (c2Bounds.getLower() > c1Bounds.getLower()) {
					isBetter = true;
					breakNow = true;

				} else if (c2Bounds.getLower() != c1Bounds.getLower()) {
					breakNow = true;
				}
				break;
			}
			case Cost: {
				if (c2Bounds.getLower() < c1Bounds.getLower()) {
					isBetter = true;
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
		return isBetter;
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
