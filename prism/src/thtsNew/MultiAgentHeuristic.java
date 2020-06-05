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

	public void setChanceNodeBounds(ArrayList<Objectives> objs, ChanceNode n)
	{
		//just the best 
		//so the same as a backup 
		if (n.getChildren() != null) {
			
			for (Objectives obj : objs) {
				double rewHere = n.getReward(obj);
				Bounds sumHere = new Bounds();
				for (DecisionNode dn : n.getChildren()) {

					if(dn.hasBounds())
					{
						Bounds temp = dn.getBoundsValueTimesTranProb(obj, n);
						sumHere = sumHere.add(temp);
						if (dn.isDeadend)
							n.leadToDeadend = true;
					}
				}
				
				sumHere = sumHere.add(rewHere);
				//just bounding the upper bound 
				if(obj == Objectives.TaskCompletion)
				{
					//not sure if this is the smartest 
					sumHere.setUpper(Math.min(sumHere.getUpper(),(double)this.numtasks));
				}
				n.setBounds(obj, sumHere);
			}
		}
	}
	
	@Override
	public HashMap<Objectives, Bounds> getStateBounds(ArrayList<Objectives> objs, DecisionNode n) throws PrismException {
		State s = n.getState();
		HashMap<Objectives, Bounds> toret = new HashMap<>();


		boolean isAcc = mapmg.isAccState(s);
		boolean isAvoid = mapmg.isAvoidState(s);
		boolean isDeadend = isDeadend(n); 
		
		n.isGoal = isAcc; 
		n.isDeadend = isAvoid | isDeadend;
		
		
		if (!n.canHaveChildren()) {
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
		
		ArrayList<State> robotStates = mapmg.getModelAndDAStates(s, true);
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
			toret = (Double.MAX_VALUE-1)*-1.0;
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
	public boolean isDeadend(DecisionNode n) throws PrismException {
		
		//a simple deadend is a an avoid state 
		boolean toret = false; 
		if(mapmg.isAvoidState(n.getState()))
			toret = true; 
		else
		{
			//how do we check if its a dead end 
			//basically if this state leads to itself 
			mapmg.exploreState(n.getState());
			int numc = mapmg.getNumChoices(); 
			if(numc == 0)
				toret = true; 
			else
			{
				if(numc == 1)
				{
					int numt = mapmg.getNumTransitions(0); 
					if(numt == 1)
					{
						State ns = mapmg.computeTransitionTarget(0, 0); 
						if(ns.compareTo(n.getState())==0)
							toret = true; 
					}
				}
			}
		}
		return toret; 
	}

	@Override
	public HashMap<Objectives, Bounds> getStateBounds(ArrayList<Objectives> objs, State s) throws PrismException {
		boolean isAcc = mapmg.isAccState(s);
		boolean isAvoid = mapmg.isAvoidState(s);
		
		
		HashMap<Objectives, Bounds> toret = new HashMap<>();
		if (isAcc || isAvoid) {
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
				toret .put(obj, b);
			}
			return toret;
		}
		
		ArrayList<State> robotStates = mapmg.getModelAndDAStates(s, true);
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


}
