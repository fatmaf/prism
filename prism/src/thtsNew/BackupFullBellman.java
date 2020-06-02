package thtsNew;

import java.util.HashMap;
import java.util.Set;

import thts.Bounds;
import thts.Objectives;

public class BackupFullBellman implements Backup {

	@Override
	public void backupChanceNode(ChanceNode cn) {
		if (cn.getChildren() != null) {
			Set<Objectives> objSet = cn.parents.get(0).bounds.keySet();
			for (Objectives obj : objSet) {
				double rewHere = cn.getReward(obj);
				Bounds sumHere = new Bounds();
				for (DecisionNode dn : cn.getChildren()) {
//					if (dn.canHaveChildren()) {
					if(dn.hasBounds())
						sumHere.add(dn.getBoundsValueTimesTranProb(obj, cn));
//					}
				}
				sumHere.add(rewHere);
				cn.setBounds(obj, sumHere);
			}
		}
	}

	@Override
	public void backupDecisionNode(DecisionNode dn) {
		if (dn.isDeadend || dn.isGoal) {
			Bounds b = null;
			for (Objectives obj : dn.bounds.keySet()) {
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
			if (dn.getChildren() != null) {
				for (Objectives obj : dn.bounds.keySet()) {

					Bounds bestBounds = new Bounds();
					bestBounds.setUpper(Double.MIN_VALUE);
					bestBounds.setLower(Double.MAX_VALUE);
					for (Object a : dn.getChildren().keySet()) {
						Bounds b = dn.getChild(a).getBounds(obj);
						if (isBetter(b.getUpper(), bestBounds.getUpper(), obj)) {
							bestBounds.setUpper(b.getUpper());
						}
						if (isBetter(b.getLower(), bestBounds.getLower(), obj)) {
							bestBounds.setLower(b.getLower());
						}

					}

					dn.setBounds(obj, bestBounds);

				}
			}
		}

	}

}
