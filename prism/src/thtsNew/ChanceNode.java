package thtsNew;

import java.util.ArrayList;
import java.util.HashMap;

import parser.State;
import prism.PrismException;
import thts.Bounds;

import thts.Objectives;



public class ChanceNode extends Node {

	Object action;
	int actionChoice; 
	ArrayList<DecisionNode> children;
	boolean leadToDeadend =false; 
	int numChildren = 0; 
	HashMap<Objectives,Double> rewards;
	boolean ignoreAction = false;

	
	public ChanceNode(Node parent, State s, Object a, int actionChoice,HashMap<Objectives, Bounds> bounds, ArrayList<Bounds> cost) {
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
	public ChanceNode(Node parent, State s, Object a,int actionChoice) {
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
	public void addChild(DecisionNode child)
	{
		if(children == null)
			children = new ArrayList<DecisionNode>();
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
//	public void updateBounds(HashMap<Objectives,Bounds> bounds, ArrayList<Bounds> cost) {
//		this.setBounds(bounds);
//		for (int i = 0; i < cost.size(); i++) {
//			if (this.getNumRews() > i) {
//				if (this.getRew(i).getUpper() < cost.get(i).getUpper()) {
//					System.out.println("Updating upper bounds to be higher than initialised: " + this.toString() + 
//							" to \n" + cost.get(i).toString()+ "\n diff = "+ (this.getRew(i).getUpper() - cost.get(i).getUpper()));
//				}
//			}
//			if(this.leadToDeadend)
//				setRew(new Bounds(),i);
//			else
//				setRew(cost.get(i), i);
//		}
//	}

	public void setChildren(ArrayList<DecisionNode> children) {
		this.children = children;
	}

	public ArrayList<DecisionNode> getChildren() {
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
		return NodeType.Chance;
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
