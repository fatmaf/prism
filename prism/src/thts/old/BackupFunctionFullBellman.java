package thts.old;

import java.util.ArrayList;
import java.util.Map.Entry;

import parser.State;
import prism.PrismException;
import thts.treesearch.utils.Bounds;
import thts.treesearch.utils.Objectives;

public class BackupFunctionFullBellman implements BackupFunction {
	ArrayList<Objectives> tieBreakingOrder;
	MultiAgentProductModelGenerator maProdModGen;
	boolean capProgression = true;
	double maxProgression = 1.0;

	public BackupFunctionFullBellman(MultiAgentProductModelGenerator maProdModGen,
			ArrayList<Objectives> tieBreakingOrder) {
		this.maProdModGen = maProdModGen;
		this.tieBreakingOrder = tieBreakingOrder;
	}

	@Override
	public void backup(ChanceNode n) throws PrismException {
		// backup a chance node
		// tie breaking oder doesnt matter since this is state action
		// so we'll just back up everything
		System.out.println("Backing up " + n);
		State s = n.getState();
		Object a = n.getAction();
		ArrayList<DecisionNode> dns = n.getChildren();
		double costHere = maProdModGen.getStateActionReward(s, a, "time", thts.old.MultiAgentProductModelGenerator.RewardCalculation.MAX);
		double progRew = maProdModGen.getProgressionReward(s, a);
		System.out.println("Prog rew " + progRew);
		Bounds prob = new Bounds();
		Bounds prog = new Bounds();
		int numRews = dns.get(0).getRews().size();
		ArrayList<Bounds> costs = new ArrayList<Bounds>();
		Bounds cost;
		for (DecisionNode dn : dns) {
			prob = prob.add(dn.getProbValueTimesTranProb(n));
			prog = prog.add(dn.getProgValueTimesTranProb(n));

			for (int i = 0; i < numRews; i++) {
				if (costs.size() <= i) {
					cost = new Bounds();
					costs.add(cost);
				}

				cost = costs.get(i);
				cost = cost.add(dn.getRewValueTimesTranProb(i, n));
				costs.set(i, cost);
				// TODO This may be wrong
//				if (dn.isGoal || dn.isDeadend)
//					costHere = 0;
			}

		}
		if (n.leadToDeadend)
			costHere = 0;

		prog = prog.add(progRew);
		for (int i = 0; i < numRews; i++) {
			cost = costs.get(i);
			cost = cost.add(costHere);
			costs.set(i, cost);

		}
		prog = this.capProgressionRewards(prog);

		n.updateBounds(prob, prog, costs);
		System.out.println("Backed up " + n);
	}

	@Override
	public void backup(DecisionNode n) throws PrismException {
		// TODO Auto-generated method stub

		System.out.println("Backing up " + n);
		// go over all children get min or max
		// has to be done in tie breaking order
		Entry<Object, ArrayList<Bounds>> upperBoundUpdate = Helper.updatedBoundsAndAction(n, true, tieBreakingOrder);
		Entry<Object, ArrayList<Bounds>> lowerBoundUpdate = Helper.updatedBoundsAndAction(n, false, tieBreakingOrder);
		if (upperBoundUpdate.getValue().size() == 0) {
			throw new PrismException("Ye kya hua?" + n.toString());
		}
		if (lowerBoundUpdate.getValue().size() == 0) {
			throw new PrismException("Ye kya hua?" + n.toString());
		}
		for (int i = 0; i < tieBreakingOrder.size(); i++) {
			Objectives obj = tieBreakingOrder.get(i);

			switch (obj) {
			case Probability:
				n.setProbValue(
						new Bounds(upperBoundUpdate.getValue().get(i).upper, lowerBoundUpdate.getValue().get(i).lower));
				break;
			case Progression:
				if (capProgression) {
					upperBoundUpdate.getValue().get(i).upper = Math.min(maxProgression,
							upperBoundUpdate.getValue().get(i).upper);
					lowerBoundUpdate.getValue().get(i).lower = Math.min(maxProgression,
							lowerBoundUpdate.getValue().get(i).lower);
				}
				n.setProg(
						new Bounds(upperBoundUpdate.getValue().get(i).upper, lowerBoundUpdate.getValue().get(i).lower));
				break;
			case Cost:
				n.setRew(new Bounds(upperBoundUpdate.getValue().get(i).upper, lowerBoundUpdate.getValue().get(i).lower),
						0);
				break;
			default:
				throw new PrismException("Unimplemented Bounds update");
			}
		}
		System.out.println("Backed up " + n);

	}

	@Override
	public double residual(DecisionNode n, boolean upperBound, float epsilon) throws PrismException {
		System.out.println("Residual " + n);
		if (n.isGoal || n.isDeadend)
			return 0.0;
		Entry<Object, ArrayList<Bounds>> boundUpdate = Helper.updatedBoundsAndAction(n, upperBound, tieBreakingOrder);
		// Entry<Object, ArrayList<Bounds>> lowerBoundUpdate =
		// Helper.updatedBoundsAndAction(n, false, tieBreakingOrder);
		double res = 0;
		for (int i = 0; i < tieBreakingOrder.size(); i++) {
			Objectives obj = tieBreakingOrder.get(i);
			Bounds b = boundUpdate.getValue().get(i);
			Bounds nb;
			switch (obj) {

			case Probability:
				nb = n.getProbValue();
				break;
			case Progression:
				nb = n.getProg();
				nb = capProgressionRewards(nb);
				break;
			case Cost:
				nb = n.getRew(0);
				break;
			default:
				throw new PrismException("Unimplemented");
			}
			if (upperBound)
				res = Math.abs(nb.subtractUpper(b));
			else
				res = Math.abs(nb.subtractLower(b));
			if (res > epsilon) {
				break;
			}
		}
		System.out.println(res);
		return res;
	}

	Bounds capProgressionRewards(Bounds nb) {
		if (capProgression) {
			nb.upper = Math.min(maxProgression, nb.upper);
			nb.lower = Math.min(maxProgression, nb.lower);
		}
		return nb;
	}

	@Override
	public double residual(ChanceNode n, boolean upperBound, float epsilon) throws PrismException {
		// backup a chance node
		// tie breaking oder doesnt matter since this is state action
		// so we'll just back up everything
		System.out.println("Residual " + n);
		State s = n.getState();
		Object a = n.getAction();
		ArrayList<DecisionNode> dns = n.getChildren();
		double costHere = maProdModGen.getStateActionReward(s, a, "time", thts.old.MultiAgentProductModelGenerator.RewardCalculation.MAX);
		double progRew = maProdModGen.getProgressionReward(s, a);
		Bounds prob = new Bounds();
		Bounds prog = new Bounds();
		int numRews = dns.get(0).getRews().size();
		ArrayList<Bounds> costs = new ArrayList<Bounds>();
		Bounds cost;
		for (DecisionNode dn : dns) {
			prob = prob.add(dn.getProbValueTimesTranProb(n));
			prog = prog.add(dn.getProgValueTimesTranProb(n));

			for (int i = 0; i < numRews; i++) {
				if (costs.size() <= i) {
					cost = new Bounds();
					costs.add(cost);
				}

				cost = costs.get(i);
				cost = cost.add(dn.getRewValueTimesTranProb(i, n));
				costs.set(i, cost);

			}
//			if (dn.isGoal || dn.isDeadend)
//				costHere = 0;
		}
		prog = prog.add(progRew);
		if (n.leadToDeadend)
			costHere = 0;
		for (int i = 0; i < numRews; i++) {
			cost = costs.get(i);
			cost = cost.add(costHere);
			costs.set(i, cost);

		}

		prog = this.capProgressionRewards(prog);

		double res = 0;
		for (int i = 0; i < tieBreakingOrder.size(); i++) {
			Objectives obj = tieBreakingOrder.get(i);
			Bounds b;
			Bounds nb;
			switch (obj) {

			case Probability:
				nb = n.getProbValue();
				b = prob;
				break;
			case Progression:
				nb = n.getProg();
				nb = capProgressionRewards(nb);
				b = prog;
				break;
			case Cost:
				nb = n.getRew(0);
				b = costs.get(0);
				break;
			default:
				throw new PrismException("Unimplemented");
			}
			if (upperBound)
				res = Math.abs(nb.subtractUpper(b));
			else
				res = Math.abs(nb.subtractLower(b));
			if (res > epsilon) {
				break;
			}
		}
		System.out.println(res);
		return res;
	}

	@Override
	public double residual(THTSNode n, boolean upperBound, float epsilon) throws PrismException {

		if (n instanceof DecisionNode) {
			return residual((DecisionNode) n, upperBound, epsilon);
		} else if (n instanceof ChanceNode) {
			return residual((ChanceNode) n, upperBound, epsilon);
		} else {
			throw new PrismException("Residual not implemented for this node");
		}
	}

	@Override
	public void backup(THTSNode n) throws PrismException {
		if (n instanceof DecisionNode) {
			backup((DecisionNode) n);
		} else if (n instanceof ChanceNode) {
			backup((ChanceNode) n);
		}
	}

}
