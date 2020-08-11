package thtsNew;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import thts.Bounds;
import thts.Objectives;

public class BackupLabelledFullBelmanCap extends BackupNVI {

//	ArrayList<Objectives> tieBreakingOrder;
	float epsilon;
	ActionSelector actSel;
	double maxCost;

	public BackupLabelledFullBelmanCap(ArrayList<Objectives> tieBreakingOrder, ActionSelector actSel, float epsilon,
			double maxCost) {
//		this.tieBreakingOrder = tieBreakingOrder;
		super(tieBreakingOrder);
		this.epsilon = epsilon;
		this.actSel = actSel;
		this.maxCost = maxCost;

	}

	boolean boundsLessThanEpsilon(HashMap<Objectives, Bounds> bounds) {
		return boundsLessThanEpsilon(bounds, epsilon, tieBreakingOrder);
//		boolean toret = true;
//		for(Objectives obj:tieBreakingOrder)
//		{
//			Bounds b = bounds.get(obj); 
//			if(b.getLower()>epsilon)
//			{
//				toret = false; 
//				break; 
//			}
//		}
//		return toret; 

	}

	@Override
	public boolean backupChanceNode(ChanceNode cn, boolean doBackup) {
		updateChanceNode(cn);
		return doBackup;
	}

	@Override
	public boolean backupDecisionNode(DecisionNode dn, boolean doBackup) {
		boolean backupToRet = false;
		if (doBackup) {

			boolean toret = true;
			Stack<DecisionNode> open = new Stack<DecisionNode>();
			Stack<DecisionNode> closed = new Stack<DecisionNode>();
			if (!dn.isSolved()) {
				open.push(dn);
			}
			while (!open.isEmpty()) {
				DecisionNode s = open.pop();
				closed.push(s);

				if (s.canHaveChildren()) {
					if (s.getChildren() != null) {
						for (Object a : s.getChildren().keySet()) {
							ChanceNode cn = s.getChild(a);
							updateChanceNode(cn);
						}
					}
//					else
//					{
//						System.out.println("bug");
//					}
				}
				HashMap<Objectives, Bounds> bounds = residualDecision((DecisionNode) s);

				if (bounds != null && boundsLessThanEpsilon(bounds)) {
					// get the best action
					// then add in all the successors
					// for which we need an action selector and a chance node
					ChanceNode cn = actSel.selectAction(s, false);
					for (DecisionNode dnc : cn.getChildren()) {
						if (!dnc.isSolved() & !open.contains(dnc) & !closed.contains(dnc)) {
							open.push(dnc);
						}
					}

				} else {
					toret = false;
				}

			}
			if (toret) {
				while (!closed.isEmpty()) {
					DecisionNode dns = closed.pop();
					ChanceNode cn = actSel.selectAction(dns, false);
					cn.setSolved();
					dns.setSolved();

				}
			} else {
				while (!closed.isEmpty()) {
					DecisionNode dns = closed.pop();
//					ChanceNode cn = actSel.selectAction(dns, false);
//					updateChanceNode(cn);
					updateDecisionNode(dns);
				}
			}
			backupToRet = toret;

		}
		return backupToRet;

	}

	public void updateChanceNode(ChanceNode cn) {
		if (cn.getChildren() != null) {

			for (Objectives obj : tieBreakingOrder) {
				double rewHere = cn.getReward(obj);
				Bounds sumHere = new Bounds();
				boolean allChildrenAreDeadends = true;
				for (DecisionNode dn : cn.getChildren()) {

					if (dn.hasBounds()) {
						sumHere = sumHere.add(dn.getBoundsValueTimesTranProb(obj, cn));
						allChildrenAreDeadends = allChildrenAreDeadends & dn.isDeadend;
					}

				}
				cn.leadToDeadend = allChildrenAreDeadends;
//				if (!cn.leadToDeadend) // Ignore the reward if its a deadend
				sumHere = sumHere.add(rewHere);
				if (obj == Objectives.Cost) {

					sumHere = sumHere.min(maxCost);
//					if (sumHere.getLower() >= maxCost)
//						cn.leadToDeadend = true;

				}
				cn.setBounds(obj, sumHere);
			}
		}
		// return true;
	}

	public void updateDecisionNode(DecisionNode dn) {
		if (dn.isDeadend || dn.isGoal) {
			Bounds b = null;
			for (Objectives obj : tieBreakingOrder) {
				switch (obj) {
				case Probability:
				case TaskCompletion:
				case Cost:
				case Progression: {
					double lb = 0.0;
					double ub = 0.0;
					b = new Bounds(ub, lb);
					break;
				}

				}
				if (dn.isDeadend && obj == Objectives.Cost) {
					double lb = this.maxCost;
					double ub = this.maxCost;
					b = new Bounds(ub, lb);

				}
				dn.setBounds(obj, b);

			}

		} else {
			if (dn.getChildren() != null) {

				HashMap<Objectives, Bounds> defaultBoundsH = new HashMap<>();
				HashMap<Objectives, Bounds> bestBoundsH = new HashMap<>();
				for (Objectives obj : tieBreakingOrder) {
					Bounds defaultBounds = new Bounds();
					defaultBounds.setUpper(getObjectiveExtremeValueInit(obj));
					defaultBounds.setLower(getObjectiveExtremeValueInit(obj));
					defaultBoundsH.put(obj, defaultBounds);
					bestBoundsH.put(obj, new Bounds(defaultBounds));
				}

				for (Object a : dn.getChildren().keySet()) {
					ChanceNode cn = dn.getChild(a);
					boolean updateUpperBounds = false;

					for (Objectives obj : tieBreakingOrder) {

						Bounds bestBounds = bestBoundsH.get(obj);
						Bounds b;
						if (cn.hasBounds()) {
							b = cn.getBounds(obj);
						} else {
							b = defaultBoundsH.get(obj);
						}
						if (isBetter(b.getUpper(), bestBounds.getUpper(), obj)) {

							updateUpperBounds = true;
							break;
						} else {
							if (!isEqual(b.getUpper(), bestBounds.getUpper()))
								break;
						}

					}

					if (updateUpperBounds) {
						for (Objectives obj : tieBreakingOrder) {
							Bounds b = cn.getBounds(obj);
							Bounds bestBounds = bestBoundsH.get(obj);
							bestBounds.setUpper(b.getUpper());
						}
					}

				}
				for (Object a : dn.getChildren().keySet()) {
					ChanceNode cn = dn.getChild(a);
					boolean updateLowerBounds = false;
					for (Objectives obj : tieBreakingOrder) {

						Bounds bestBounds = bestBoundsH.get(obj);
						if (cn.hasBounds()) {
							Bounds b = cn.getBounds(obj);

							if (isBetter(b.getLower(), bestBounds.getLower(), obj)) {
								updateLowerBounds = true;
								break;
							} else {
								if (!isEqual(b.getLower(), bestBounds.getLower()))
									break;
							}
						}

					}
					if (updateLowerBounds) {
						for (Objectives obj : tieBreakingOrder) {
							Bounds b = cn.getBounds(obj);
							Bounds bestBounds = bestBoundsH.get(obj);
							bestBounds.setLower(b.getLower());
						}
					}

				}
				dn.setBounds(bestBoundsH);
//				if (tieBreakingOrder.contains(Objectives.Cost)) {
//					Bounds cbounds = dn.getBounds(Objectives.Cost);
//					if (cbounds.getLower() == maxCost) {
//						dn.isDeadend = true;
//					}
//				}
			}
		}

		
//		return true;
	}

	@Override
	public boolean forwardbackupChanceNode(ChanceNode cn) {
		updateChanceNode(cn);
		return true;
	}

	@Override
	public boolean forwardbackupDecisionNode(DecisionNode dn) {
		// TODO Auto-generated method stub
		updateDecisionNode(dn);
		return true;
	}

}
