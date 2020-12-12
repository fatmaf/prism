package thts.Scratch;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;

import parser.State;
import prism.PrismException;
import thts.old.Bounds;
import thts.old.Objectives;
import thts.TreeSearch.ChanceNode;
import thts.TreeSearch.DecisionNode;
import thts.heuristic.Heuristic;

//because I'm too lazy to do stuff
public class GSSPPaperHeuristic implements Heuristic {
	List<State> goalStates = null;

	public GSSPPaperHeuristic(List<State> goalStates) {
		this.goalStates = goalStates;
	}

	@Override
	public HashMap<Objectives, Bounds> getStateBounds(ArrayList<Objectives> objs, DecisionNode n)
			throws PrismException {

		n.isGoal = isGoal(n.getState());

		HashMap<Objectives, Bounds> emptyBounds = new HashMap<>();
		for (Objectives obj : objs) {
		
			if(obj == Objectives.Probability && n.isGoal)
			{
				emptyBounds.put(obj, new Bounds(1.0,1.0));
			}
			else
			{
				emptyBounds.put(obj, new Bounds());
			}
				
		}
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
