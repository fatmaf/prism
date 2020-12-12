package thts.treesearch.heuristic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import parser.State;
import prism.PrismException;
import thts.treesearch.utils.Bounds;
import thts.treesearch.utils.Objectives;
import thts.treesearch.backup.BackupNVI;
import thts.modelgens.MultiAgentNestedProductModelGenerator;
import thts.treesearch.utils.ChanceNode;
import thts.treesearch.utils.DecisionNode;

public class EmptyNestedMultiAgentHeuristicBounds implements Heuristic {
	List<State> goalStates = null;

	List<State> deadends = null;

	MultiAgentNestedProductModelGenerator ma;

	HashMap<Objectives, Entry<Double, Double>> minMaxVals;
	BackupNVI backup;

	public EmptyNestedMultiAgentHeuristicBounds(MultiAgentNestedProductModelGenerator ma, List<State> goalStates,
			List<State> deadends, HashMap<Objectives, Entry<Double, Double>> minMaxVals, BackupNVI backupFunction) {
		this.goalStates = goalStates;
		this.deadends = deadends;
		this.minMaxVals = minMaxVals;
		this.ma = ma;
		this.backup = backupFunction;
	}

	@Override
	public HashMap<Objectives, Bounds> getStateBounds(ArrayList<Objectives> objs, DecisionNode n)
			throws PrismException {

		n.isGoal = isGoal(n.getState());
		n.isDeadend = isDeadend(n.getState());

		HashMap<Objectives, Bounds> emptyBounds = new HashMap<>();
		for (Objectives obj : objs) {
			if (!minMaxVals.containsKey(obj))

			{
				throw new PrismException("Objective not in min max vals list " + obj);
			} else {
				if (n.isGoal) {
					// probability = upper bound
					// costs = lower bound , progression = lower bound
					switch (obj) {
					case Probability:
						emptyBounds.put(obj,
								new Bounds(minMaxVals.get(obj).getValue(), minMaxVals.get(obj).getValue()));
						break;
					case Progression:
					case TaskCompletion:
					case Cost:
						emptyBounds.put(obj, new Bounds(minMaxVals.get(obj).getKey(), minMaxVals.get(obj).getKey()));
						break;

					}
				} else if (n.isDeadend) {
					switch (obj) {
					case Progression:
					case TaskCompletion:
					case Probability:
						emptyBounds.put(obj, new Bounds(minMaxVals.get(obj).getKey(), minMaxVals.get(obj).getKey()));
						break;

					case Cost:
						emptyBounds.put(obj,
								new Bounds(minMaxVals.get(obj).getValue(), minMaxVals.get(obj).getValue()));
						break;

					}
				} else {
					emptyBounds.put(obj, new Bounds(minMaxVals.get(obj).getValue(), minMaxVals.get(obj).getKey()));
				}
			}
//			if (obj == Objectives.Probability)
//
//			{
//				if (n.isGoal)
//					emptyBounds.put(obj, new Bounds(1.0, 1.0));
//				else
//					emptyBounds.put(obj, new Bounds());
//			} else {
//				if (obj == Objectives.Cost) {
//					if (n.isGoal)
//						emptyBounds.put(obj, new Bounds());
//					else {
//						if (n.isDeadend)
//							emptyBounds.put(obj, new Bounds(baseVal, baseVal));
//						else
//							emptyBounds.put(obj, new Bounds());
//					}
//				} else // if(obj == Objectives.TaskCompletion)
//				{
//					emptyBounds.put(obj, new Bounds());
//				}
//
//			}

		}
		if (!n.canHaveChildren())
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

		try {
			backup.forwardbackupChanceNode(c);
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			throw new PrismException(e.toString());
		}
//		HashMap<Objectives, Bounds> emptyBounds = new HashMap<>();
//		for (Objectives obj : objs) {
//			emptyBounds.put(obj, new Bounds());
//		}
//		c.setBounds(emptyBounds);

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
		return ma.isAccState(s);
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
