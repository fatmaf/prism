package thts.Heuristic;

import java.util.ArrayList;
import java.util.HashMap;

import parser.State;
import prism.PrismException;
import thts.Old.Bounds;
import thts.Old.Objectives;
import thts.TreeSearch.ChanceNode;
import thts.TreeSearch.DecisionNode;

public interface Heuristic {

	HashMap<Objectives, Bounds> getStateBounds(ArrayList<Objectives> objs, DecisionNode n) throws PrismException;
	HashMap<Objectives, Bounds> getStateBounds(ArrayList<Objectives> objs, State s) throws PrismException;

	ArrayList<Bounds> getCostStateBounds(DecisionNode n);
	void setChanceNodeBounds(ArrayList<Objectives> objs, ChanceNode c) throws PrismException;
	boolean isGoal(State s);


}
