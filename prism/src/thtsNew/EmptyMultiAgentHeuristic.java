package thtsNew;

import java.util.ArrayList;
import java.util.HashMap;

import parser.State;
import prism.PrismException;
import thts.Bounds;
import thts.Objectives;

//so needs to be initialised with state action values for each robot  
//and a mapmg to help it do state stuff 
public class EmptyMultiAgentHeuristic implements Heuristic {

	MultiAgentNestedProductModelGenerator mapmg;

	double deadendCost; 
	boolean doDeadEnds;
double costH; 

	public EmptyMultiAgentHeuristic(double costH,MultiAgentNestedProductModelGenerator mapmg,double deadendCost,boolean doDeadEnds) {
		this.mapmg = mapmg;
		this.deadendCost = deadendCost;
		this.doDeadEnds = doDeadEnds; 
		this.costH = costH;
	}

	public void setChanceNodeBounds(ArrayList<Objectives> objs, ChanceNode n)
	{
//		HashMap<Objectives,Bounds> emptyBounds = new HashMap<>(); 
//		for(Objectives obj: objs)
//		{
//			emptyBounds.put(obj, new Bounds());
//		}
		HashMap<Objectives,Bounds> emptyBounds = new HashMap<>(); 
		for(Objectives obj: objs)
		{
			emptyBounds.put(obj, new Bounds());
			if(obj == Objectives.Cost)
			{
				emptyBounds.get(obj).setLower(costH);
				emptyBounds.get(obj).setUpper(costH);
			}
//			if(obj == Objectives.Probability)
//			{
//				emptyBounds.get(obj).setLower(1.0);
//				emptyBounds.get(obj).setUpper(1.0);
//			}
		
		}
//		return emptyBounds;

		n.setBounds(emptyBounds);
	}
	
	@Override
	public HashMap<Objectives, Bounds> getStateBounds(ArrayList<Objectives> objs, DecisionNode n) throws PrismException {
		State s = n.getState();
		



		boolean isAvoid = mapmg.isAvoidState(s);
//		boolean isDeadend = isDeadend(n); 
		
		n.isGoal = isGoal (s);
		if(doDeadEnds)
		n.isDeadend = isAvoid;// | isDeadend;
		if(n.isGoal)
			System.out.println("Goal Found "+n.getShortName());
		if((int)s.varValues[0] == -1)
			n.isDeadend = true; 
		
		HashMap<Objectives,Bounds> emptyBounds = new HashMap<>(); 
		for(Objectives obj: objs)
		{
			emptyBounds.put(obj, new Bounds());
			if(obj == Objectives.Cost)
			{
				if(n.isGoal)
				{
					emptyBounds.get(obj).setLower(0);
					emptyBounds.get(obj).setUpper(0);
				}
				else if(n.isDeadend)
				{
					emptyBounds.get(obj).setLower(deadendCost);
					emptyBounds.get(obj).setUpper(deadendCost);
				}
				else {
				emptyBounds.get(obj).setLower(costH);
				emptyBounds.get(obj).setUpper(costH);
				}
			}
			if(obj == Objectives.Probability)
			{
				if(n.isGoal)
				{
					emptyBounds.get(obj).setLower(1.0);
					emptyBounds.get(obj).setUpper(1.0);
				}
				else 
				{
					if(!n.isDeadend) {
					emptyBounds.get(obj).setLower(1.0);
					emptyBounds.get(obj).setUpper(1.0);
					}
				}
			}
		
		}
		return emptyBounds;
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

		
		HashMap<Objectives,Bounds> emptyBounds = new HashMap<>(); 
		for(Objectives obj: objs)
		{
			emptyBounds.put(obj, new Bounds());
		}
		return emptyBounds;

	}

	@Override
	public boolean isGoal(State s) {
		// TODO Auto-generated method stub
		boolean isAcc = mapmg.isAccState(s);
		return isAcc;
	}


}
