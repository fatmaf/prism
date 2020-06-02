package thtsNew;

import java.util.HashMap;

import thts.Bounds;
import thts.Objectives;

public interface Backup {

	// take in a decisionnode
	// take in a chancenode
	// just update them
	void backupChanceNode(ChanceNode cn);

	void backupDecisionNode(DecisionNode dn);

	public default boolean isBetter(double b1, double b2, Objectives obj) {
		boolean isBetter = false;
		switch (obj) {
		case Cost:
			isBetter = b1 < b2;
			break;
		case Progression:
		case TaskCompletion:
		case Probability:
			isBetter = b1 > b2;
			break;

		}
		return isBetter;
	}

	public default boolean isEqual(double b1, double b2) {
		return (b1 == b2);
	}

	public default HashMap<Objectives, Bounds> residualDecision(DecisionNode n) {
		HashMap<Objectives, Bounds> residual = null;
		// returns the difference
		// current bounds
		if (n.getChildren() != null) {
			for (Objectives obj : n.bounds.keySet()) {

				residual = new HashMap<>();
				Bounds bestBounds = new Bounds();
				bestBounds.setUpper(Double.MIN_VALUE);
				bestBounds.setLower(Double.MAX_VALUE);
				for (Object a : n.getChildren().keySet()) {
					Bounds b = n.getChild(a).getBounds(obj);
					if (isBetter(b.getUpper(), bestBounds.getUpper(), obj)) {
						bestBounds.setUpper(b.getUpper());
					}
					if (isBetter(b.getLower(), bestBounds.getLower(), obj)) {
						bestBounds.setLower(b.getLower());
					}

				}
				Bounds currentBounds = n.getBounds(obj);
				Bounds boundDiff = currentBounds.subtract(bestBounds);
				residual.put(obj, boundDiff);

			}
		}
		return residual;
	}

}
