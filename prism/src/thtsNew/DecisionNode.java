package thtsNew;

import java.util.ArrayList;
import java.util.HashMap;
import parser.State;
import prism.PrismException;
import thts.Bounds;
import thts.Objectives;


public class DecisionNode extends Node {
	HashMap<Object, ChanceNode> children;
	HashMap<Node, Double> transitionProbability;
	int numActions;
	boolean isDeadend=false;
	boolean isGoal=false;
	int numChildrenInitialised = 0; 
	//a list of the base actions - their value and count for each robot 
	//count = how many actions this base action is in 
	//this is not going to be used unless we have multiple robots 
	//and honestly even then if we use a specific heuristic and action selection function 
	ArrayList<HashMap<Object,BaseActionInfo>> baseActionsForRobot = null; 
	
	public void reset()
	{
	
		this.numVisits = 0; 
		this.solved=false; 
		this.isGoal=false; 
		this.isDeadend=false; 
		this.bounds = null; 
		
	}
	public DecisionNode(Node parent, State s, double tprob, HashMap<Objectives, Bounds> bounds, ArrayList<Bounds> cost,
			boolean deadend, boolean goal) {
		this.setState(s);
		this.setBounds(bounds);
//		for (int i = 0; i < cost.size(); i++)
//			setRew(cost.get(i), i);
		boundsInitialised = true; 
		if (parent != null) {
			addParent(parent);
			if (transitionProbability == null) {
				transitionProbability = new HashMap<Node, Double>();

			}

			transitionProbability.put(parent, tprob);
		}
		children = null;
		numActions = 0;
		solved = false;
		isDeadend = deadend;
		isGoal = goal;

		if (deadend | goal) {

			solved = true;
		}
		numVisits = 0;

	}

	public DecisionNode(Node ps, State s, double tprob) {
		this.setState(s);
		children = null;
		numActions = 0;
		solved = false;

		numVisits = 0;
		if (ps != null) {
			addParent(ps);
			if (transitionProbability == null) {
				transitionProbability = new HashMap<Node, Double>();

			}

			transitionProbability.put(ps, tprob);
		}

	}

	public boolean canHaveChildren()
	{
		return (!isDeadend & !isGoal);
	}

	@Override
	public void setBounds(HashMap<Objectives,Bounds> b)
	{
		if(!boundsInitialised)
		boundsInitialised = true; 
		super.setBounds(b);
	}
	public void addParent(Node n, double tprob) {
		addParent(n);
		addTranProb(n, tprob);

	}

	public Bounds getBoundsValueTimesTranProb(Objectives obj, Node p) {
		if (p != null)
			return this.getBounds(obj).multiply(transitionProbability.get(p));
		else
			return this.getBounds(obj).multiply(1.0);
	}

	public void addChild(Object a, ChanceNode child) throws PrismException {

		if (isLeafNode())

			children = new HashMap<Object, ChanceNode>();

		// check if this action exists in the children
		if (!children.containsKey(a)) {
			children.put(a, child);
		}

	}

	ChanceNode getChild(Object a) {
		if (isLeafNode())
			return null;
		if (!children.containsKey(a))
			return null;
		return children.get(a);
	}

	HashMap<Object, ChanceNode> getChildren() {
		return children;
	}

	@Override
	public boolean isLeafNode() {
		return children == null;
	}

	public double getTranProb(Node p) {
		return transitionProbability.get(p);
	}

	@Override
	public boolean equals(Node n) {
		boolean equal = false;
		if (n instanceof DecisionNode) {

			// sate action parent
			if (this.getState().compareTo(n.getState()) == 0) {

				equal = true;

			}
		}
		return equal;

	}

	@Override
	public NodeType nodeType() {
		// TODO Auto-generated method stub
		return NodeType.Decision;
	}

	void addTranProb(Node p, double tprob) {
		if (transitionProbability == null)
			transitionProbability = new HashMap<Node, Double>();
		transitionProbability.put(p, tprob);
	}

	public boolean allActionsAdded() {
		return children.size() == numActions;
	}

	@Override
	public String getShortName() {
		return this.getState().toString();
	}

	@Override
	public String toString() {
		String str = "DN{" +getPartialString();
		if (this.isDeadend)
			str += ",de:True";
		if (this.isGoal)
			str += ",g:True";
		str += "}";

		return str;
	}
	public ArrayList<ChanceNode> childrenWithInitialisedBounds()
	{
		ArrayList<ChanceNode> toret = new ArrayList<>();
		for (Object a:children.keySet())
		{
			if(children.get(a).boundsInitialised())
			{
				toret.add(children.get(a));
			}
			
		}
		return toret; 
	}
	public ArrayList<ChanceNode> childrenWithuninitialisedBounds()
	{
		ArrayList<ChanceNode> toret = new ArrayList<>();
		for (Object a:children.keySet())
		{
			if(!children.get(a).boundsInitialised())
			{
				toret.add(children.get(a));
			}
			
		}
		return toret; 
	}
	public boolean allChildrenInitialised()
	{
		
		boolean toret = (childrenWithuninitialisedBounds().size()==0); 
		return toret;
	}



}
