package thts.scratch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import parser.State;
import prism.PrismException;
import thts.treesearch.utils.Bounds;
import thts.treesearch.utils.Objectives;
import thts.treesearch.utils.ChanceNode;
import thts.treesearch.utils.DecisionNode;
import thts.treesearch.heuristic.Heuristic;

public class EmptyNestedMAHeuristicSubGoals implements Heuristic {
	List<State> goalStates = null;

	List<State> deadends = null;

	double baseVal = 0.0;
	MANestedProductModelGeneratorSubGoals ma;

	public int subGoalNum = 0;
	int maxGoals = -1;
	public void setMaxGoals()
	{
		if(maxGoals == -1) {
			maxGoals = ma.numDAs; 
			if(ma.safetyDAIndex!=-1)
			{
				maxGoals--;
			}
			}
	}
	public void incSubGoal()
	{
		
		if(subGoalNum < maxGoals)
		subGoalNum++; 
	}
	public boolean reachedAcc()
	{
		return (subGoalNum == maxGoals);
				
	}
	public EmptyNestedMAHeuristicSubGoals(MANestedProductModelGeneratorSubGoals ma, List<State> goalStates, List<State> deadends,
			double initialVal) {
		this.goalStates = goalStates;
		this.deadends = deadends;
		this.baseVal = initialVal;
		this.ma = ma;
		setMaxGoals();
	}

	public EmptyNestedMAHeuristicSubGoals(MANestedProductModelGeneratorSubGoals ma, List<State> goalStates,
			List<State> deadends) {
		this.goalStates = goalStates;
		this.deadends = deadends;
		this.ma = ma;
		setMaxGoals();
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
			if (ma == null)
				return false;
			else {
				boolean deadend = ma.isAvoidState(s);
				if (!deadend) {
					
					deadend = ma.isDeadend(s);// check if no outgoing or incoming

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
//		if(s.toString().contains(",1,1,0)"))
//			System.out.println("debug");
		return ma.isSubGoalState(s, subGoalNum);//ma.isAccState(s);
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
