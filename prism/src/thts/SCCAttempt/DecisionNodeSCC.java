package thts.SCCAttempt;

import java.util.ArrayList;
import java.util.HashMap;

import parser.State;
import prism.PrismException;
import thts.Old.Bounds;
import thts.Old.Objectives;
import thts.TreeSearch.ChanceNode;
import thts.TreeSearch.DecisionNode;
import thts.TreeSearch.Node;
import thts.TreeSearch.NodeType;

public class DecisionNodeSCC extends DecisionNode {
	protected HashMap<Object, ChanceNodeSCC> children;
	int sccID = -1; 
	SCCNodeType sccType = null;

	
	public void reset()
	{
	
		this.numVisits = 0; 
		this.solved=false; 
		this.isGoal=false; 
		this.isDeadend=false; 
		this.bounds = null; 
		
	}
	public DecisionNodeSCC(Node parent, State s, double tprob, HashMap<Objectives, Bounds> bounds, ArrayList<Bounds> cost,
			boolean deadend, boolean goal) {
	super(parent,s,tprob,bounds,cost,deadend,goal);
		setState(s);
		setBounds(bounds);

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

	public DecisionNodeSCC(Node ps, State s, double tprob) {
		super(ps,s,tprob);
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

	public void addChild(Object a, ChanceNodeSCC child) throws PrismException {

		super.addChild(a, child);
		if (isLeafNode())

			children = new HashMap<Object, ChanceNodeSCC>();

		// check if this action exists in the children
		if (!children.containsKey(a)) {
			children.put(a, child);
		}

	}

	public ChanceNodeSCC getChild(Object a) {
		if (isLeafNode())
			return null;
		if (!children.containsKey(a))
			return null;
		return children.get(a);
	}


	public HashMap<Object, ChanceNodeSCC> getChildrenSCC() {
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

	protected void addTranProb(Node p, double tprob) {
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
