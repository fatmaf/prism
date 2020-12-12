package thts.treesearch.heuristic;

import java.util.ArrayList;
import java.util.HashMap;

import parser.State;
import prism.PrismException;
import thts.treesearch.utils.Bounds;
import thts.treesearch.utils.Objectives;
import thts.treesearch.utils.ChanceNode;
import thts.treesearch.utils.DecisionNode;

public interface Heuristic {

	HashMap<Objectives, Bounds> getStateBounds(ArrayList<Objectives> objs, DecisionNode n) throws PrismException;
	HashMap<Objectives, Bounds> getStateBounds(ArrayList<Objectives> objs, State s) throws PrismException;

	ArrayList<Bounds> getCostStateBounds(DecisionNode n);
	void setChanceNodeBounds(ArrayList<Objectives> objs, ChanceNode c) throws PrismException;
	boolean isGoal(State s);


}
