package thtsNew;

import java.util.ArrayList;
import java.util.HashMap;

import thts.Bounds;
import thts.Objectives;

public class ActionSelectorUCT implements ActionSelector {

	ArrayList<Objectives> tieBreakingOrder;
	ArrayList<Boolean> objectivesUseUpperBound;
//	boolean useUpperFirst = false;
//	double uctC = 10;

	public ActionSelectorUCT(ArrayList<Objectives> tieBreakingOrder,
			/* double uctC, */ ArrayList<Boolean> objectivesUseUpperBound) {
		this.tieBreakingOrder = tieBreakingOrder;
		this.objectivesUseUpperBound = objectivesUseUpperBound;
//		this.uctC = uctC;
	}

	public ChanceNode selectActionSimple(DecisionNode nd) {
		ChanceNode selectedActionNode = null;
		// if bounds are not initialised choose the one with uninitialised bounds
		// just the next one

		// so lets just create a copy
		HashMap<Object, HashMap<Objectives, Bounds>> uctBounds = new HashMap<>();
		if (nd.allChildrenInitialised()) {
			for (Object a : nd.getChildren().keySet()) {
				ChanceNode cn = nd.getChild(a);
				double bonus = 0;
				// C√2 logN(s)/N(a,s)ifN(a,s)>0
				if (cn.numVisits > 0) {
					double visitsLog = Math.log(nd.numVisits) / cn.numVisits;
					bonus = Math.sqrt(visitsLog);
				}
				HashMap<Objectives, Bounds> bounds = new HashMap<>();
				for (Objectives obj : tieBreakingOrder) {
					bounds.put(obj, new Bounds(cn.getBounds(obj).add(cn.getBounds(obj).multiply(bonus))));
				}
				uctBounds.put(a, bounds);
			}
			Object greedyAction = null;
			Object tempChoice = null;
			HashMap<Objectives, Bounds> greedyBounds = null;

			for (Object a : uctBounds.keySet()) {

				if (greedyAction == null) {
					greedyAction = a;
					greedyBounds = uctBounds.get(a);
				}

				else {
					tempChoice = getNodeWithBetterBound(greedyAction, a, greedyBounds, uctBounds.get(a));
							if (tempChoice != null) {
						greedyAction = a;
						greedyBounds = uctBounds.get(a);
					}
				}

			}
			selectedActionNode = nd.getChild(greedyAction);

		}
		return selectedActionNode;
	}

	private Object getNodeWithBetterBound(Object a1, Object a2, HashMap<Objectives, Bounds> c1,
			HashMap<Objectives, Bounds> c2) {

		Object toret = a1;
		boolean breakNow = false;
		for (int i = 0; i < tieBreakingOrder.size(); i++) {
			Objectives obj = tieBreakingOrder.get(i);
			boolean useUpperBound = this.objectivesUseUpperBound.get(i);
			// we just want the difference between the bounds to be samller
			// so we choose the one with greater diff
			Bounds c2Bounds = c2.get(obj);
			Bounds c1Bounds = c1.get(obj);
			
			if(isBoundBetter(c1Bounds,c2Bounds,obj,useUpperBound))
			{
				toret = a2; 
				breakNow = true; 
			}
			else 
			{
				//if they are equal continue 
				//if not break 
				if(!isBoundEqual(c1Bounds,c2Bounds,useUpperBound))
					breakNow = true; 
			}
			if (breakNow)
				break; 
		}
		return toret;
	}

	private boolean isBoundEqual(Bounds b1, Bounds b2,boolean useUpper)
	{
		if(useUpper)
			return isBoundEqual(b1.getUpper(),b2.getUpper()); 
		else 
			return isBoundEqual(b1.getLower(),b2.getLower());
	}
	private boolean isBoundEqual(double b1, double b2)
	{
		return b1==b2; 
	}
	private boolean isBoundBetter(Bounds b1, Bounds b2, Objectives obj,boolean useUpper)
	{
		if(useUpper)
			return isBoundBetter(b1.getUpper(),b2.getUpper(),obj); 
		else 
			return isBoundBetter(b1.getLower(),b2.getLower(),obj);
	}
	private boolean isBoundBetter(double b1, double b2, Objectives obj) {
		boolean toret = false; // b2 is not better than b1
		switch (obj) {
		case Probability:
		case TaskCompletion:
		case Progression: {
			if (b2 > b1) {
				toret = true;
			}
			break;
		}
		case Cost: {
			if (b2 < b1) {
				toret = true;

			}

			break;
		}
		}
		return toret;
	}

	

	@Override
	public ChanceNode selectAction(DecisionNode nd, boolean doMin) throws Exception {

		//

		return selectActionSimple(nd);
	}

}
