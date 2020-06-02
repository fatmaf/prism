package thtsNew;

import java.util.ArrayList;
import java.util.HashMap;

import parser.State;
import prism.PrismException;
import thts.Bounds;
import thts.Objectives;

public interface Heuristic {

	HashMap<Objectives, Bounds> getStateBounds(ArrayList<Objectives> objs, State s) throws PrismException;

	ArrayList<Bounds> getCostStateBounds(State s);

}
