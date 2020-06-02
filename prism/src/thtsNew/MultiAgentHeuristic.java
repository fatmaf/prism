package thtsNew;

import java.util.ArrayList;
import java.util.HashMap;

import parser.State;
import prism.PrismException;
import thts.Bounds;
import thts.Objectives;

//so needs to be initialised with state action values for each robot  
//and a mapmg to help it do state stuff 
public class MultiAgentHeuristic implements Heuristic {

	MultiAgentNestedProductModelGenerator mapmg;
	ArrayList<HashMap<Objectives, HashMap<State, Double>>> singleAgentSolns;
	int numtasks = 0;

	public MultiAgentHeuristic(MultiAgentNestedProductModelGenerator mapmg,
			ArrayList<HashMap<Objectives, HashMap<State, Double>>> singleAgentSolns) {
		this.mapmg = mapmg;
		this.singleAgentSolns = singleAgentSolns;
		numtasks = mapmg.numDAs;
	}

	@Override
	public HashMap<Objectives, Bounds> getStateBounds(ArrayList<Objectives> objs, State s) throws PrismException {
		HashMap<Objectives, Bounds> toret = new HashMap<>();
		ArrayList<State> robotStates = mapmg.getModelAndDAStates(s, true);

		boolean isAcc = mapmg.isAccState(s);
		boolean isAvoid = mapmg.isAvoidState(s);

		if (isAcc & !isAvoid) {
			Bounds b = null;
			for (Objectives obj : objs) {
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
				toret.put(obj, b);
			}
			return toret;
		}
		if (!isAcc & isAvoid) {
			Bounds b = null;
			for (Objectives obj : objs) {
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
				toret.put(obj, b);
			}
			return toret;
		}
//		ArrayList<State> robotStates = mapmg.getModelAndDAStates(s, true);

		// get the corresponding state from the objectives
		// for probability the single agent solution is a lower bound
		// max single agent sol
		// for maxtask the single agent is a lower bound
		// max single agent sol
		// for cost the sum of all single agent sols is an upper bound
		// lowerbound = 0
		for (Objectives obj : objs) {
			ArrayList<Double> vals = getSingleAgentStateVals(robotStates, obj);
			Bounds b = null;
			switch (obj) {
			case Probability: {
				// lower bound //the worst we can do is a single agent sol
				double lb = this.getMinMax(vals, false);
				double ub = 1.0;
				b = new Bounds(ub, lb);
				break;
			}
			case TaskCompletion: {
				double lb = this.getMinMax(vals, false);
				double ub = numtasks;
				b = new Bounds(ub, lb);
				break;
			}
			case Cost: {
				double ub = this.getSum(vals);
				double lb = 0.0;
				b = new Bounds(ub, lb);
				break;
			}
			case Progression: {
				// redundant we dont have this right now
				// but if we did
				// ist like prob
				double lb = this.getMinMax(vals, false);
				double ub = 1.0;
				b = new Bounds(ub, lb);
				break;
			}
			default: {
				throw new PrismException("Not implemented");
			}
			}
			toret.put(obj, b);
		}

		return toret;
	}

	ArrayList<Double> getSingleAgentStateVals(ArrayList<State> rs, Objectives obj) {
		ArrayList<Double> valstoret = new ArrayList<>();
		for (int i = 0; i < rs.size(); i++) {
			HashMap<Objectives, HashMap<State, Double>> valshere = this.singleAgentSolns.get(i);
			if (valshere.containsKey(obj)) {
				if (valshere.get(obj).containsKey(rs.get(i))) {
					valstoret.add(valshere.get(obj).get(rs.get(i)));
				} else {
					System.out.println("Unable to find state " + rs.get(i));
//					valstoret.add(Double.NaN);
					double toadd = Double.NaN;
					switch (obj) {
					case Probability:
					case Progression:
					case TaskCompletion:
						toadd = 0;
						break;
					case Cost:
						toadd = Double.MAX_VALUE;
						break;

					}
					valstoret.add(toadd);
				}
			} else {
				System.out.println("No such objective in single agent sol " + obj.toString());
				valstoret.add(Double.NaN);
			}
		}
		return valstoret;
	}

	protected double getSum(ArrayList<Double> vals) {
		double toret = 0;
		for (double v : vals) {
			if (!Double.isNaN(v))
				toret += v;
		}
		return toret;
	}

	protected double getMinMax(ArrayList<Double> vals, boolean domin) {
		double toret = Double.MAX_VALUE;
		if (!domin)
			toret = Double.MIN_VALUE;
		for (double v : vals) {
			if (!Double.isNaN(v)) {
				if (domin) {
					if (v < toret)
						toret = v;
				} else {
					if (v > toret)
						toret = v;
				}
			}
		}
		return toret;
	}

	@Override
	public ArrayList<Bounds> getCostStateBounds(State s) {

		// TODO Auto-generated method stub
		return null;
	}

}
