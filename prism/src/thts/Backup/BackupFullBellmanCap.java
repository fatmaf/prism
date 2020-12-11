package thts.Backup;

import java.util.ArrayList;
import java.util.HashMap;

import prism.PrismException;

import java.util.Map.Entry;

import thts.Old.Bounds;
import thts.Old.Objectives;
import thts.ActionSelector.ActionSelector;
import thts.TreeSearch.ChanceNode;
import thts.TreeSearch.DecisionNode;
import thts.TreeSearch.Node;

public class BackupFullBellmanCap extends BackupNVI {

	float epsilon;
	ActionSelector actSel;
	HashMap<Objectives, Entry<Double, Double>> minMaxVals;

	public BackupFullBellmanCap(ArrayList<Objectives> tieBreakingOrder, ActionSelector actSel, float epsilon,
			HashMap<Objectives, Entry<Double, Double>> minMaxVals) {

		super(tieBreakingOrder);
		this.epsilon = epsilon;
		this.actSel = actSel;
		this.minMaxVals = minMaxVals;
	}

	boolean boundsLessThanEpsilon(HashMap<Objectives, Bounds> bounds) {
		return boundsLessThanEpsilon(bounds, epsilon, tieBreakingOrder);

	}

	@Override
	public boolean backupChanceNode(ChanceNode cn, boolean doBackup) throws PrismException {
		if (!cn.isSolved()) {
			if (cn.getChildren() != null) {

				for (Objectives obj : tieBreakingOrder) {
					double rewHere = cn.getReward(obj);
					Bounds sumHere = new Bounds();
					boolean allChildrenAreDeadends = true;
					boolean allChildrenSolved = true;
					for (DecisionNode dn : cn.getChildren()) {

						if (dn.hasBounds()) {
							sumHere = sumHere.add(dn.getBoundsValueTimesTranProb(obj, cn));
							allChildrenAreDeadends = allChildrenAreDeadends & !dn.canHaveChildren();
							allChildrenSolved = allChildrenSolved & dn.isSolved();
						}

					}
					cn.leadToDeadend = allChildrenAreDeadends;
					if (allChildrenSolved || allChildrenAreDeadends)
						cn.setSolved();

					sumHere = sumHere.add(rewHere);

					sumHere = sumHere.min(minMaxVals.get(obj).getValue());

					cn.setBounds(obj, sumHere);

				}
				markSolved(cn);

			}
		}
		return true;
	}

	@Override
	public boolean backupDecisionNode(DecisionNode dn, boolean doBackup) {
		if (!dn.isSolved()) {
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

						double lb = minMaxVals.get(obj).getValue();// this.maxCost;
						double ub = minMaxVals.get(obj).getValue();// this.maxCost;
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

				}
			}
			markSolved(dn);
		}
		return true;
	}

	public void markSolved(Node n) {
		boolean allBoundsLessThanEpsilon = true;
		for (Objectives obj : tieBreakingOrder) {
			allBoundsLessThanEpsilon &= (Math.abs(n.getBounds(obj).diff()) < epsilon);
		}
		if (allBoundsLessThanEpsilon)
			n.setSolved();
	}

	@Override
	public boolean forwardbackupChanceNode(ChanceNode cn) throws PrismException {
		return backupChanceNode(cn, true);
	}

	@Override
	public boolean forwardbackupDecisionNode(DecisionNode dn) {
		return backupDecisionNode(dn, true);
	}

}
