package thts.treesearch.heuristic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import parser.State;
import prism.PrismException;
import thts.treesearch.utils.Bounds;
import thts.treesearch.utils.Objectives;
import thts.treesearch.utils.ChanceNode;
import thts.treesearch.utils.DecisionNode;

public class EmptyHeuristic implements Heuristic {
	List<State> goalStates = null;

	List<State> deadends = null;

	double baseVal = 0.0;

	public EmptyHeuristic(List<State> goalStates, List<State> deadends, double initialVal) {
		this.goalStates = goalStates;
		this.deadends = deadends;
		this.baseVal = initialVal;
	}

	public EmptyHeuristic(List<State> goalStates, List<State> deadends) {
		this.goalStates = goalStates;
		this.deadends = deadends;
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

	public boolean isDeadend(State s) {
		if (deadends == null)
			return false;
		else {
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
		if (goalStates == null) { // TODO Auto-generated method stub
			return false;
		} else {
			boolean toret = false;
			for (State gs : goalStates)
				if (s.compareTo(gs) == 0) {
					toret = true;
					break;
				}
			return toret;
		}
	}

}
