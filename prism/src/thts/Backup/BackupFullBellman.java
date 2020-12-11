package thts.Backup;

import java.util.ArrayList;
import java.util.HashMap;

import prism.PrismException;
import thts.Old.Bounds;
import thts.Old.Objectives;
import thts.TreeSearch.ChanceNode;
import thts.TreeSearch.DecisionNode;

public class BackupFullBellman extends BackupNVI {


	double deadendCost; 
	public BackupFullBellman(ArrayList<Objectives> tieBreakingOrder,double deadendCost) {
//		this.tieBreakingOrder = tieBreakingOrder;
		super(tieBreakingOrder);
		this.deadendCost = deadendCost;
	}

	@Override
	public boolean backupChanceNode(ChanceNode cn, boolean doBackup) throws PrismException {
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
				cn.setBounds(obj, sumHere);
			}
		}
		return true;
	}

	@Override
	public boolean backupDecisionNode(DecisionNode dn, boolean doBackup) {
		if (dn.isGoal) {
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
				dn.setBounds(obj, b);

			}

		} else {
			if (dn.isDeadend) {
				Bounds b = null;
				for (Objectives obj : tieBreakingOrder) {
					switch (obj) {
					case Probability:
					case TaskCompletion:
					case Progression: {
						double lb = 0.0;
						double ub = 0.0;
						b = new Bounds(ub, lb);
						break;
					}
					case Cost: {
						double lb = deadendCost;
						double ub = deadendCost;
						b = new Bounds(ub, lb);
						break;
					}
					}
					dn.setBounds(obj, b);

				}
			} else {
				if (dn.getChildren() != null) {

					HashMap<Objectives, Bounds> bestBoundsH = new HashMap<>();
					for (Objectives obj : tieBreakingOrder) {
						Bounds bestBounds = new Bounds();
						bestBounds.setUpper(getObjectiveExtremeValueInit(obj));
						bestBounds.setLower(getObjectiveExtremeValueInit(obj));
						bestBoundsH.put(obj, bestBounds);
					}

					for (Object a : dn.getChildren().keySet()) {
						ChanceNode cn = dn.getChild(a);
						boolean updateUpperBounds = false;

						for (Objectives obj : tieBreakingOrder) {

							Bounds bestBounds = bestBoundsH.get(obj);
							if (cn.hasBounds()) {
								Bounds b = cn.getBounds(obj);
								if (isBetter(b.getUpper(), bestBounds.getUpper(), obj)) {

									updateUpperBounds = true;
									break;
								} else {
									if (!isEqual(b.getUpper(), bestBounds.getUpper()))
										break;
								}

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
		}

		return true;
	}

	@Override
	public boolean forwardbackupChanceNode(ChanceNode cn) throws PrismException {
		return backupChanceNode(cn,true);
	}

	@Override
	public boolean forwardbackupDecisionNode(DecisionNode dn) {
		return backupDecisionNode(dn,true);
	}

}
