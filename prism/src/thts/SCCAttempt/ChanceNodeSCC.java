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

public class ChanceNodeSCC extends ChanceNode {

	public ArrayList<DecisionNodeSCC> children;

	int sccID = -1; 
	SCCNodeType sccType =null;
	
	public ChanceNodeSCC(Node parent, State s, Object a, int actionChoice,HashMap<Objectives, Bounds> bounds, 
			ArrayList<Bounds> cost) {
		super(parent,s,a,actionChoice,bounds,cost);
		this.setState(s);
		this.setBounds(bounds);
//		for (int i = 0; i < cost.size(); i++)
//			setRew(cost.get(i), i);
		addParent(parent);
		action = a;
		children = null;
		solved = false;
		numVisits = 0;
		this.actionChoice = actionChoice;
		boundsInitialised = true; 
		rewards = null; 
	}
	public ChanceNodeSCC(Node parent, State s, Object a,int actionChoice) {
		super(parent, s, a, actionChoice);
		this.setState(s);
		addParent(parent);
		action = a;
		children = null;
		solved = false;
		this.actionChoice = actionChoice;
		boundsInitialised = false; 
		rewards = null; 

	}
	public double sumChildrenTProb() throws PrismException {
		double prob = 0;
		if (children != null) {
			for (int i = 0; i < children.size(); i++) {
				double cprob = children.get(i).getTranProb(this);
				prob = prob + cprob;
			}
		}
		if (prob > 1)
			throw new PrismException("Children have incorrenct probabilities");
		return prob;
	}
	public void addChild(DecisionNodeSCC child)
	{
		super.addChild((DecisionNode)child);
		if(children == null)
			children = new ArrayList<DecisionNodeSCC>();
		children.add(child);
	}
	@Override
	public void setBounds(HashMap<Objectives,Bounds> b)
	{
		if(!boundsInitialised)
		boundsInitialised = true; 
		super.setBounds(b);
	}
	public double getReward(Objectives obj)
	{
		return rewards.get(obj);
	}
	public void setRewards(HashMap<Objectives,Double> rews)
	{
		rewards = rews; 
	}
	public void setReward(Objectives obj,double rew)
	{
		if(rewards == null)
			rewards = new HashMap<>(); 
		rewards.put(obj, rew);
	}
	public ArrayList<DecisionNode> childrenWithInitialisedBounds()
	{
		ArrayList<DecisionNode> toret = new ArrayList<>();
		for (DecisionNode dn:children)
		{
			if(dn.boundsInitialised())
			{
				toret.add(dn);
			}
			
		}
		return toret; 
	}
	public ArrayList<DecisionNode> childrenWithuninitialisedBounds()
	{
		ArrayList<DecisionNode> toret = new ArrayList<>();
		for (DecisionNode dn:children)
		{
			if(!dn.boundsInitialised())
			{
				toret.add(dn);
			}
			
		}
		return toret; 
	}
	public boolean allChildrenInitialised()
	{
		boolean toret = (childrenWithuninitialisedBounds().size()==0); 
		return toret;
	}


	public void setChildrenSCC(ArrayList<DecisionNodeSCC> children) {
		this.children = children;
	}
	

	public ArrayList<DecisionNodeSCC> getChildrenSCC() {
		return this.children;
	}

	
	public Object getAction() {
		// TODO Auto-generated method stub
		return action;
	}

	
	@Override
	public boolean equals(Node n) {
		boolean equal = false;
		if (n instanceof ChanceNode) {

			// sate action parent
			if (this.getState().compareTo(n.getState()) == 0) {
				if (this.getAction() == ((ChanceNode) n).getAction()) {
					equal = true;
				}
			}
		}
		return equal;

	}

	@Override
	public NodeType nodeType() {
		return NodeType.ChanceSCC;
	}

	@Override
	public boolean isLeafNode() {
		// TODO Auto-generated method stub
		return children == null;
	}

	@Override
	public String toString(){
		String str = "CN{s:"+getPartialString(); 
		str += ",a:'" + action+"'";
		if(this.leadToDeadend)
			str+=",de:True";
		str += "}";

		return str;
	}
	@Override
	public String getShortName() {
		return this.getState().toString() + this.getAction().toString();
	}
	public String createShortName(State s, Object action)
	{
		return s.toString()+action.toString();
	}

}
