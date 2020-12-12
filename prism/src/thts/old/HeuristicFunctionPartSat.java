package thts.old;

import java.util.ArrayList;

import parser.State;
import prism.PrismException;

public class HeuristicFunctionPartSat implements HeuristicFunction {
	MultiAgentProductModelGenerator maProdModGen;
	Bounds prob;
	Bounds prog;
	ArrayList<Bounds> costs;

	public HeuristicFunctionPartSat(MultiAgentProductModelGenerator jpmg) {
		maProdModGen = jpmg;
	}

	@Override
	public void calculateBounds(State s) throws PrismException {
		double maxP = 1.0;

		double maxPr = 1.0;

		if (maProdModGen.isDeadend(s)) {
			maxPr = 0.0;
			maxP = 0.0;
		}
		if (maProdModGen.isGoal(s)) {
			maxPr = 0.0;
			maxP = 1.0;
		}
		prob = new Bounds(maxP, maProdModGen.getSolutionValue(s, Objectives.Probability, thts.old.MultiAgentProductModelGenerator.RewardCalculation.MAX));
		prog = new Bounds(maxPr, maProdModGen.getSolutionValue(s, Objectives.Progression, thts.old.MultiAgentProductModelGenerator.RewardCalculation.MAX));

		Bounds cost = new Bounds(maProdModGen.getSolutionValue(s, Objectives.Cost, thts.old.MultiAgentProductModelGenerator.RewardCalculation.SUM), 0.0);

		costs = new ArrayList<Bounds>();
		costs.add(cost);

		System.out.println("Calculating bounds:" + s + " p " + prob + " pr " + prog + " c " + costs.get(0));
	}

	@Override
	public void calculateBounds(State s, Object a, ArrayList<DecisionNode> dns, THTSNode parent) throws PrismException {
		double costHere = maProdModGen.getStateActionReward(s, a, "time", thts.old.MultiAgentProductModelGenerator.RewardCalculation.MAX);
		double progRew = maProdModGen.getProgressionReward(s, a);

		System.out.println(s.toString() + "," + a.toString() + " pr: " + progRew);
		prob = new Bounds();
		prog = new Bounds();
		int numRews = dns.get(0).getRews().size();
		costs = new ArrayList<Bounds>();
		Bounds cost;
		for (DecisionNode dn : dns) {
			prob = prob.add(dn.getProbValueTimesTranProb(parent));
			prog = prog.add(dn.getProgValueTimesTranProb(parent));

			for (int i = 0; i < numRews; i++) {
				if (costs.size() <= i) {
					cost = new Bounds();
					costs.add(cost);
				}

				if (!dn.isDeadend && !dn.isGoal) {
					cost = costs.get(i);
					cost = cost.add(dn.getRewValueTimesTranProb(i, parent));
					costs.set(i, cost);
					costHere = 0; 
				}
			}

		}
		prog = prog.add(progRew);
		
		for (int i = 0; i < numRews; i++) {
			cost = costs.get(i);
			cost = cost.add(costHere);
			costs.set(i, cost);

		}
		System.out.println(
				"Calculating bounds:" + s + "," + a.toString() + " p " + prob + " pr " + prog + " c " + costs.get(0));
	}

	public Bounds getProbabilityBounds() {
		// TODO Auto-generated method stub
		return prob;
	}

	public Bounds getProgressionBounds() {
		// TODO Auto-generated method stub
		return prog;
	}

	public ArrayList<Bounds> getRewardBounds() {
		// TODO Auto-generated method stub
		return costs;
	}

	@Override
	public Bounds getBounds(Objectives obj) {
		Bounds toret=null; 
		switch (obj)
		{
		case Probability:
			toret= getProbabilityBounds(); 
			break; 
		case Progression:
			toret= getProgressionBounds(); 
			break; 
		case Cost:
			toret= getRewardBounds().get(0); 
			break;
			default:
				break;
		}
		return toret;
	}

	@Override
	public Bounds getRewardBounds(int r) {
		// TODO Auto-generated method stub
		return costs.get(r);
	}

	@Override
	public ArrayList<Bounds> getAllRewardBounds() {
		// TODO Auto-generated method stub
		return costs;
	}

}
