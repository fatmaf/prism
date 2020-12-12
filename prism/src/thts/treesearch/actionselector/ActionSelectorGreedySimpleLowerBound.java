package thts.actionselector;

import java.util.ArrayList;
import java.util.Random;


import prism.PrismLog;
import thts.old.Bounds;
import thts.treesearch.Objectives;
import thts.treesearch.ChanceNode;
import thts.treesearch.DecisionNode;

public class ActionSelectorGreedySimpleLowerBound implements ActionSelector {

	Random rgen;
	ArrayList<Objectives> tieBreakingOrder;
	boolean tieBreak;

	public ActionSelectorGreedySimpleLowerBound(ArrayList<Objectives> tieBreakingOrder,boolean tieBreakRandom) {
		this.tieBreakingOrder = tieBreakingOrder;
		this.tieBreak = tieBreakRandom;
	}

	@Override
	public ChanceNode selectAction(DecisionNode nd, boolean doMin, PrismLog fileLog) throws Exception
	{
		return selectActionSimple(nd,fileLog);
	}

	public ChanceNode selectActionSimple(DecisionNode nd,PrismLog fileLog) {
		if(fileLog!=null)
		{
			fileLog.println("Selecting action for "+nd.getShortName());
		}
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
			if(tieBreak)
			{
				if(fileLog!=null)
				{
					fileLog.println("Attempting random tie break in act sel for "+nd.getShortName());
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
					rgen = new Random();
					int choice = rgen.nextInt(sameNodes.size()); 
					greedyAction = sameNodes.get(choice);
					if(fileLog!=null)
					{
						fileLog.println("Tie broken between  "+sameNodes.size()+" nodes ");
						fileLog.println("Nodes: "+sameNodes.toString());
						fileLog.println("Chosen: "+greedyAction.getShortName());
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
			return this.selectActionSimple(nd,null);
	
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
