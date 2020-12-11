package thts.Old;

import java.util.ArrayList;

import parser.State;
import prism.PrismException;

public interface HeuristicFunction
{


	
	public void calculateBounds(State s) throws PrismException; 
//	public void calculateBounds(State s, Object a,ArrayList<DecisionNode> dn) throws PrismException;
	public Bounds getBounds(Objectives obj); 
	public Bounds getRewardBounds(int r);
	public ArrayList<Bounds> getAllRewardBounds();
	public void calculateBounds(State s, Object a, ArrayList<DecisionNode> dns, THTSNode parent) throws PrismException; 
	
}
