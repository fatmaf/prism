package thts.old;

import java.util.ArrayList;

import parser.State;
import prism.PrismException;
import thts.treesearch.utils.Bounds;
import thts.treesearch.utils.Objectives;

public class MCTSTrial implements HeuristicFunction
{

	MultiAgentProductModelGenerator maProdModGen; 
	Bounds prob;
	Bounds prog; 
	ArrayList<Bounds> costs; 
	
	public MCTSTrial(MultiAgentProductModelGenerator jpmg)
	{
		maProdModGen = jpmg; 
	}

	@Override
	public void calculateBounds(State s) throws PrismException
	{
		// TODO Auto-generated method stub
		//do the trial here please 
		//then just use these 

	}

	@Override
	public void calculateBounds(State s, Object a, ArrayList<DecisionNode> dn,THTSNode parent) throws PrismException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public Bounds getBounds(Objectives obj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bounds getRewardBounds(int r) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Bounds> getAllRewardBounds() {
		// TODO Auto-generated method stub
		return null;
	}

}
