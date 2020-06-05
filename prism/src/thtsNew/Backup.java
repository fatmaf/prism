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

	public default double getObjectiveExtremeValueInit(Objectives obj) {
		double toret=0;
		switch (obj) {
		case Cost:
			toret = Double.MAX_VALUE;
			break;
		case Progression:
		case TaskCompletion:
		case Probability:
			toret = (Double.MAX_VALUE - 1) * -1.0;
			break;

		}
		return toret;
	}

	public default boolean isEqual(double b1, double b2) {
		return (b1 == b2);
	}

	public default HashMap<Objectives, Bounds> residualDecision(DecisionNode n) {
		HashMap<Objectives, Bounds> residual = null;
		// returns the difference
		// current bounds
		if (n.getChildren() != null) {
			HashMap<Objectives, Bounds> bestBoundsH = new HashMap<>();
			for (Objectives obj : n.bounds.keySet()) {
				Bounds bestBounds = new Bounds();
				bestBounds.setUpper(getObjectiveExtremeValueInit(obj));
				bestBounds.setLower(getObjectiveExtremeValueInit(obj));
				bestBoundsH.put(obj, bestBounds);
			}

			for (Object a : n.getChildren().keySet()) {
				ChanceNode cn = n.getChild(a);
				boolean updateUpperBounds = false;

				for (Objectives obj : n.bounds.keySet()) {

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
					for (Objectives obj : n.bounds.keySet()) {
						Bounds b = cn.getBounds(obj);
						Bounds bestBounds = bestBoundsH.get(obj);
						bestBounds.setUpper(b.getUpper());
					}
				}

			}
			for (Object a : n.getChildren().keySet()) {
				ChanceNode cn = n.getChild(a);
				boolean updateLowerBounds = false;
				for (Objectives obj : n.bounds.keySet()) {

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

					for (Objectives obj : n.bounds.keySet()) {
						Bounds b = cn.getBounds(obj);
						Bounds bestBounds = bestBoundsH.get(obj);
						bestBounds.setLower(b.getLower());
					}
				}

			}
			residual = new HashMap<>();
			for (Objectives obj : n.bounds.keySet()) {

				Bounds currentBounds = n.getBounds(obj);
				Bounds bestBounds = bestBoundsH.get(obj);
				Bounds boundDiff = currentBounds.subtract(bestBounds);
				residual.put(obj, boundDiff);
			}

		}
		return residual;
	}

}
