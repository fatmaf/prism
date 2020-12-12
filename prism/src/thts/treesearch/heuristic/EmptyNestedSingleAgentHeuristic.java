package thts.heuristic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import parser.State;
import prism.PrismException;
import thts.old.Bounds;
import thts.treesearch.Objectives;
import thts.modelgens.NestedProductModelGenerator;
import thts.treesearch.ChanceNode;
import thts.treesearch.DecisionNode;

public class EmptyNestedSingleAgentHeuristic implements Heuristic {
	List<State> goalStates = null;

	List<State> deadends = null;

	double baseVal = 0.0;
	NestedProductModelGenerator sa;

	public EmptyNestedSingleAgentHeuristic(NestedProductModelGenerator sa, List<State> goalStates, List<State> deadends,
			double initialVal) {
		this.goalStates = goalStates;
		this.deadends = deadends;
		this.baseVal = initialVal;
		this.sa = sa;
	}

	public EmptyNestedSingleAgentHeuristic(NestedProductModelGenerator sa, List<State> goalStates,
			List<State> deadends) {
		this.goalStates = goalStates;
		this.deadends = deadends;
		this.sa = sa;
	}

	@Override
	public HashMap<Objectives, Bounds> getStateBounds(ArrayList<Objectives> objs, DecisionNode n)
			throws PrismException {

		n.isGoal = isGoal(n.getState());
		n.isDeadend = isDeadend(n.getState());

		HashMap<Objectives, Bounds> emptyBounds = new HashMap<>();
		for (Objectives obj : objs) {

			if (obj == Objectives.Probability)

			{
				if (n.isGoal)
					emptyBounds.put(obj, new Bounds(1.0, 1.0));
				else
					emptyBounds.put(obj, new Bounds());
			} else {
				if (obj == Objectives.Cost) {
					if (n.isGoal)
						emptyBounds.put(obj, new Bounds());
					else {
						if (n.isDeadend)
							emptyBounds.put(obj, new Bounds(baseVal, baseVal));
						else
							emptyBounds.put(obj, new Bounds());
					}
				} else // if(obj == Objectives.TaskCompletion)
				{
					emptyBounds.put(obj, new Bounds());
				}

			}

		}
		if(!n.canHaveChildren())
			n.setSolved();
		return emptyBounds;
	}

	@Override
	public HashMap<Objectives, Bounds> getStateBounds(ArrayList<Objectives> objs, State s) throws PrismException {
		HashMap<Objectives, Bounds> emptyBounds = new HashMap<>();
		for (Objectives obj : objs) {
			emptyBounds.put(obj, new Bounds());
		}
		return emptyBounds;
	}

	@Override
	public ArrayList<Bounds> getCostStateBounds(DecisionNode n) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setChanceNodeBounds(ArrayList<Objectives> objs, ChanceNode c) throws PrismException {
		HashMap<Objectives, Bounds> emptyBounds = new HashMap<>();
		for (Objectives obj : objs) {
			emptyBounds.put(obj, new Bounds());
		}
		c.setBounds(emptyBounds);

	}

	public boolean isDeadend(State s) throws PrismException {

		if (deadends == null) {
			if (sa == null)
				return false;
			else {
				boolean deadend = sa.isAvoid(s);
				if (!deadend) {
					deadend = sa.isDeadend(s);// check if no outgoing or incoming

				}
				return deadend;
			}
		} else {
			boolean toret = false;
			for (State de : deadends) {
				if (s.compareTo(de) == 0) {
					toret = true;
					break;
				}
			}
			return toret;
		}

	}

	@Override
	public boolean isGoal(State s) {
		return sa.isReachAcceptanceGoalState(s);
//		if (goalStates == null) { // TODO Auto-generated method stub
//			return false;
//		} else {
//			boolean toret = false;
//			for (State gs : goalStates)
//				if (s.compareTo(gs) == 0) {
//					toret = true;
//					break;
//				}
//			return toret;
//		}
	}

}
