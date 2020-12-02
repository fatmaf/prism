package thtsNew;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import parser.State;
import prism.PrismException;
import prism.PrismLog;
import thts.Bounds;
import thts.Objectives;

//so needs to be initialised with state action values for each robot  
//and a mapmg to help it do state stuff 
public class MultiAgentHeuristicTC implements Heuristic {

	MultiAgentNestedProductModelGenerator mapmg;
	ArrayList<HashMap<Objectives, HashMap<State, Double>>> singleAgentSolns;

	boolean tightBounds = false;
	HashMap<Objectives, Entry<Double, Double>> minMaxVals;
	boolean useSASForInit = false; 

	public MultiAgentHeuristicTC(MultiAgentNestedProductModelGenerator mapmg,
			ArrayList<HashMap<Objectives, HashMap<State, Double>>> singleAgentSolns,
			HashMap<Objectives, Entry<Double, Double>> minMaxVals,boolean useSASForInit) {
		this.mapmg = mapmg;
		this.singleAgentSolns = singleAgentSolns;
		this.minMaxVals = minMaxVals;
		this.useSASForInit = useSASForInit;
	}

	public MultiAgentHeuristicTC(MultiAgentNestedProductModelGenerator mapmg,
			ArrayList<HashMap<Objectives, HashMap<State, Double>>> singleAgentSolns,
			HashMap<Objectives, Entry<Double, Double>> minMaxVals,boolean useSASForInit, boolean tightBounds) {
		this.mapmg = mapmg;
		this.singleAgentSolns = singleAgentSolns;
		this.minMaxVals = minMaxVals;
		this.tightBounds = tightBounds;
		this.useSASForInit = useSASForInit;
	}

	public void setChanceNodeBounds(ArrayList<Objectives> objs, ChanceNode n) throws PrismException {
		// just the best
		// so the same as a backup
		if (n.getChildren() != null) {

			for (Objectives obj : objs) {
				double rewHere = n.getReward(obj);
				Bounds sumHere = new Bounds();
				for (DecisionNode dn : n.getChildren()) {

					if (dn.hasBounds()) {
						Bounds temp = dn.getBoundsValueTimesTranProb(obj, n);
						sumHere = sumHere.add(temp);
//						if (dn.isDeadend)
//							n.leadToDeadend = true;
					}
				}

				sumHere = sumHere.add(rewHere);
				// just bounding the upper bound
				if (obj == Objectives.TaskCompletion) {
					// not sure if this is the smartest
					sumHere.setUpper(Math.min(sumHere.getUpper(), minMaxVals.get(obj).getValue()));
				} else if (obj == Objectives.Cost) {
					sumHere = sumHere.min(minMaxVals.get(obj).getValue());
				}
				n.setBounds(obj, sumHere);
			}
		}
	}

	@Override
	public HashMap<Objectives, Bounds> getStateBounds(ArrayList<Objectives> objs, DecisionNode n)
			throws PrismException {
		State s = n.getState();
		HashMap<Objectives, Bounds> toret = new HashMap<>();

		boolean isAcc = isGoal(s);
		boolean isAvoid = mapmg.isAvoidState(s);
		boolean isDeadend = isDeadend(s);

		n.isGoal = isAcc;
		n.isDeadend = isAvoid | isDeadend;

		toret = this.getStateBounds(objs, s);
		boolean noTCRewards = noMoreTCRewards(s); 
		n.isDeadend = n.isDeadend | noTCRewards; 
		
		if (!n.canHaveChildren())
			n.setSolved();
		return toret;
	}

	public boolean noMoreTCRewards(State s)
	{
		ArrayList<State> robotStates = mapmg.getModelAndDAStates(s, true);
		ArrayList<Double> vals = getSingleAgentStateVals(robotStates, Objectives.TaskCompletion);
		double tcsum = getSum(vals); 
		if(tcsum == 0)
			return true; 
		else 
			return false; 


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
						toadd = minMaxVals.get(obj).getKey();
						break;
					case Cost:
						toadd = minMaxVals.get(obj).getValue();
						break;

					}
					valstoret.add(toadd);
				}
			} else {
				System.out.println("No such objective in single agent sol " + obj.toString());
				double toadd = Double.NaN;
				switch (obj) {
				case Probability:
				case Progression:
				case TaskCompletion:
					toadd = minMaxVals.get(obj).getKey();
					break;
				case Cost:
					toadd = minMaxVals.get(obj).getValue();
					break;

				}
				valstoret.add(toadd);
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
			toret = 0;
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
	public ArrayList<Bounds> getCostStateBounds(DecisionNode n) {

		// TODO Auto-generated method stub
		return null;
	}

//	@Override
	public boolean isGoal(DecisionNode n) {
		return mapmg.isAccState(n.getState());

	}

//	@Override
	public boolean isDeadend(State s) throws PrismException {

		// a simple deadend is a an avoid state
		boolean toret = false;
		if (mapmg.isAvoidState(s))
			toret = true;
		else {
			if (mapmg.isDeadend(s))
				toret = true;
//			//how do we check if its a dead end 
//			//basically if this state leads to itself 
//			mapmg.exploreState(n.getState());
//			int numc = mapmg.getNumChoices(); 
//			if(numc == 0)
//				toret = true; 
//			else
//			{
//				if(numc == 1)
//				{
//					int numt = mapmg.getNumTransitions(0); 
//					if(numt == 1)
//					{
//						State ns = mapmg.computeTransitionTarget(0, 0); 
//						if(ns.compareTo(n.getState())==0)
//							toret = true; 
//					}
//				}
//			}
		}
		return toret;
	}

	@Override
	public boolean isGoal(State s) {
		// TODO Auto-generated method stub
		return mapmg.isAccState(s);
	}

	@Override
	public HashMap<Objectives, Bounds> getStateBounds(ArrayList<Objectives> objs, State s) throws PrismException {
		HashMap<Objectives, Bounds> toret = new HashMap<>();

		boolean isAcc = isGoal(s);
		boolean isAvoid = mapmg.isAvoidState(s);
		boolean isDeadend = isDeadend(s);

		boolean isGoal = isAcc;
		isDeadend = isAvoid | isDeadend;

		if (isDeadend | isGoal) {
			Bounds b = null;
			for (Objectives obj : objs) {
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
					if (isDeadend) {
						double lb = minMaxVals.get(obj).getValue();
						double ub = minMaxVals.get(obj).getValue();
						b = new Bounds(ub, lb);

					} else {
						double lb = 0.0;
						double ub = 0.0;
						b = new Bounds(ub, lb);
					}
					break;
				}

				}
				toret.put(obj, b);
			}

			return toret;
		}

		if(this.useSASForInit) {
		ArrayList<State> robotStates = mapmg.getModelAndDAStates(s, true);

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
				double lb = tightBounds ? this.getMinMax(vals, false) : this.getMinMax(vals, true);
				double ub = minMaxVals.get(obj).getValue();
				b = new Bounds(ub, lb);
				//if all the values are 0 then we know its a deadend 
				double tcsum = getSum(vals); 

					
				break;
			}
			case Cost: {
				double ub = getSum(vals);
				ub = Math.min(ub, minMaxVals.get(obj).getValue());
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
		}
		else
		{
			for (Objectives obj : objs) {
				double lb = minMaxVals.get(obj).getKey();
				double ub = minMaxVals.get(obj).getValue();
				Bounds b = new Bounds(ub, lb);
				toret.put(obj, b);
			}
		}
		return toret;

	}

}
