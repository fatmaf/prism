package thts.utils;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Stack;

import explicit.MDP;
import explicit.MDPSimple;
import parser.State;
import prism.PrismException;
import prism.PrismLog;
import strat.MDStrategyArray;
import strat.Strategy;
import thts.treesearch.actionselector.ActionSelector;
import thts.treesearch.utils.ChanceNode;
import thts.treesearch.utils.DecisionNode;
import thts.treesearch.utils.Node;
import thts.vi.MDPValIter.ModelCheckerMultipleResult;

public class PolicyCreator
{

	MDPCreator mdpCreator;

	public PolicyCreator()
	{
		mdpCreator = new MDPCreator();
	}
	
	public MDPSimple createPolicy(Queue<Node> nodeQ) throws PrismException
	{
		HashMap<Node,Integer> nodeCounter  = new HashMap<Node,Integer>();
		Node currNode = nodeQ.remove();
		DecisionNode currDecNode=null;//= (DecisionNode)currNode;
		ChanceNode currChanceNode = null;
		boolean onChanceNode = false;
		String actDelim = "!";
		while (!nodeQ.isEmpty()) {
			
			if (currNode instanceof DecisionNode) {
				currDecNode = (DecisionNode) currNode;
				onChanceNode = false;
			} else {
				currChanceNode = (ChanceNode) currNode;
				onChanceNode = true;
			}

			if (onChanceNode) {
				//we have the action and the state 
				//now we just get the successors and add all of them 
				//but we also need to save these so we can add them to the MDP 
				ArrayList<DecisionNode> successorDecNodes = currChanceNode.getChildren();
				ArrayList<Entry<State, Double>> successors = new ArrayList<Entry<State, Double>>();
				for (DecisionNode succDecNode : successorDecNodes) {
					successors.add(new AbstractMap.SimpleEntry<State, Double>(succDecNode.getState(), succDecNode.getTranProb(currChanceNode)));
					
				}
				if (!nodeCounter.containsKey(currChanceNode))
				{
					nodeCounter.put(currChanceNode, 0);
					mdpCreator.addAction(currChanceNode.getState(), currChanceNode.getAction(), successors);
				}
				else
				{
					nodeCounter.put(currChanceNode, nodeCounter.get(currChanceNode)+1);
					//rename this action 
					//int actIndex = mdpCreator.getActionIndexPartial(currChanceNode.getState(), currChanceNode.getAction(), actDelim);
					//if(actIndex != -1)
					//{
						//set a new name 
						int actIndex = mdpCreator.renameAction(currChanceNode.getState(), currChanceNode.getAction(), actDelim, nodeCounter.get(currChanceNode));
						if(actIndex == -1)
							throw new PrismException("Error!! Action not found");
					//}
					
				}
		

			}
			else 
			{
				mdpCreator.addState(currDecNode.getState());
			}
			currNode = nodeQ.remove();
		}
		return mdpCreator.mdp;

	}

	public MDPSimple createPolicy(DecisionNode rootNode, ActionSelector actSel, boolean upperbound) throws Exception
	{
		Stack<Node> toVisit = new Stack<Node>();
		Stack<Node> visited = new Stack<Node>();
		Node currNode = rootNode;
		DecisionNode currDecNode = rootNode;
		ChanceNode currChanceNode = null;
		boolean onChanceNode = false;
		toVisit.add(currNode);
		while (!toVisit.isEmpty()) {
			currNode = toVisit.pop();
			visited.add(currNode);
			if (currNode instanceof DecisionNode) {
				currDecNode = (DecisionNode) currNode;
				onChanceNode = false;
			} else {
				currChanceNode = (ChanceNode) currNode;
				onChanceNode = true;
			}

			if (onChanceNode) {
				//we have the action and the state 
				//now we just get the successors and add all of them 
				//but we also need to save these so we can add them to the MDP 
				ArrayList<DecisionNode> successorDecNodes = currChanceNode.getChildren();
				ArrayList<Entry<State, Double>> successors = new ArrayList<Entry<State, Double>>();
				for (DecisionNode succDecNode : successorDecNodes) {
					successors.add(new AbstractMap.SimpleEntry<State, Double>(succDecNode.getState(), succDecNode.getTranProb(currChanceNode)));
					if (!toVisit.contains(succDecNode) && !visited.contains(succDecNode)) {
						toVisit.add(succDecNode);
					}
				}
				mdpCreator.addAction(currChanceNode.getState(), currChanceNode.getAction(), successors);

			} else {
				currChanceNode = actSel.selectAction(currDecNode, upperbound);

				if (currChanceNode != null) {
					if (!toVisit.contains(currChanceNode) && !visited.contains(currChanceNode)) {
						toVisit.add(currChanceNode);
					}
				}
			}
		}
		return mdpCreator.mdp;
	}

	
	public ArrayList<Double> createPolicyPrintValues(MDP mdp,ModelCheckerMultipleResult result,PrismLog ml)
	{
		ArrayList<Double> valsInInitState = new ArrayList<>(); 
		
		int initialState = mdp.getFirstInitialState(); 
		MDStrategyArray strat = result.strat;
		Stack<Integer> toVisit = new Stack<Integer>();
		BitSet visited = new BitSet();
		toVisit.add(initialState);
		int s;
		for(int i = 0; i<result.solns.size(); i++)
		{
			valsInInitState.add(result.solns.get(i)[initialState]);
		}
		while (!toVisit.isEmpty()) {
			s = toVisit.pop();
			visited.set(s);
			String svals="";
			for(int i=0; i<result.solns.size(); i++)
			{
				svals+=i+"-"+result.solns.get(i)[s]+",";
			}
			State sState = mdp.getStatesList().get(s);
			 svals = s+":"+sState.toString()+"["+svals+"]";
			 ml.println(svals);
			strat.initialise(s);
			//			strat.initialise(s);
			Object action = strat.getChoiceAction();
			int actionIndex = findActionIndex(mdp, s, action);

			if (actionIndex > -1) {
				Iterator<Entry<Integer, Double>> tranIter = mdp.getTransitionsIterator(s, actionIndex);
				ArrayList<Entry<State, Double>> successors = new ArrayList<Entry<State, Double>>();
				while (tranIter.hasNext()) {
					Entry<Integer, Double> stateProbPair = tranIter.next();
					int succ = stateProbPair.getKey();
					State succState = mdp.getStatesList().get(stateProbPair.getKey());
					double prob = stateProbPair.getValue();
					successors.add(new AbstractMap.SimpleEntry<State, Double>(succState, prob));
					if (!toVisit.contains(succ) && !visited.get(succ)) {
						toVisit.add(succ);
					}
				}
				mdpCreator.addAction(sState, action, successors);
			}
		}
		return valsInInitState; 
	}
	public MDPSimple createPolicy(MDP productMdp, Strategy strat) throws PrismException
	{
		int initialState = productMdp.getFirstInitialState();
		if(productMdp instanceof MDPSimple)
		return createPolicy(initialState, (MDPSimple) productMdp, strat);
		else 
			return createPolicy(initialState,productMdp,strat);
	}

	int findActionIndex(MDPSimple mdp, int s, Object a)
	{
		int numChoices = mdp.getNumChoices(s);
		int actionIndex = -1;
		for (int i = 0; i < numChoices; i++) {
			Object action = mdp.getAction(s, i);
//			System.out.println(action.toString());
			if (action != null) {
				if (action.toString().contentEquals(a.toString())) {
					actionIndex = i;
					break;
				}
			}
		}
		return actionIndex;
	}
	int findActionIndex(MDP mdp, int s, Object a)
	{
		int numChoices = mdp.getNumChoices(s);
		int actionIndex = -1;
		for (int i = 0; i < numChoices; i++) {
			Object action = mdp.getAction(s, i);
//			System.out.println(action.toString());
			if (action != null) {
				if (action.toString().contentEquals(a.toString())) {
					actionIndex = i;
					break;
				}
			}
		}
		return actionIndex;
	}
	public MDPSimple createPolicyAllStates(MDP mdp,Strategy strat)
	{
		return createPolicyAllStates((MDPSimple)mdp,strat);
	}
	public MDPSimple createPolicyAllStates(MDPSimple mdp, Strategy strat)
	{
		Stack<Integer> toVisit = new Stack<Integer>();
		BitSet visited = new BitSet();
		for(int i = 0; i<mdp.getNumStates(); i++)
		toVisit.add(i);
		int s;
		while (!toVisit.isEmpty()) {
			s = toVisit.pop();
			visited.set(s);
			State sState = mdp.getStatesList().get(s);

			strat.initialise(s);
			//			strat.initialise(s);
			Object action = strat.getChoiceAction();
			int actionIndex = findActionIndex(mdp, s, action);

			if (actionIndex > -1) {
				Iterator<Entry<Integer, Double>> tranIter = mdp.getTransitionsIterator(s, actionIndex);
				ArrayList<Entry<State, Double>> successors = new ArrayList<Entry<State, Double>>();
				while (tranIter.hasNext()) {
					Entry<Integer, Double> stateProbPair = tranIter.next();
					int succ = stateProbPair.getKey();
					State succState = mdp.getStatesList().get(stateProbPair.getKey());
					double prob = stateProbPair.getValue();
					successors.add(new AbstractMap.SimpleEntry<State, Double>(succState, prob));
					if (!toVisit.contains(succ) && !visited.get(succ)) {
						toVisit.add(succ);
					}
				}
				mdpCreator.addAction(sState, action, successors);
			}
		}
		return mdpCreator.mdp;
	}
	public MDPSimple createPolicy(int initialState, MDPSimple mdp, Strategy strat)
	{
		Stack<Integer> toVisit = new Stack<Integer>();
		BitSet visited = new BitSet();
		toVisit.add(initialState);
		int s;
		while (!toVisit.isEmpty()) {
			s = toVisit.pop();
			visited.set(s);
			State sState = mdp.getStatesList().get(s);

			strat.initialise(s);
			//			strat.initialise(s);
			Object action = strat.getChoiceAction();
			int actionIndex = findActionIndex(mdp, s, action);

			if (actionIndex > -1) {
				Iterator<Entry<Integer, Double>> tranIter = mdp.getTransitionsIterator(s, actionIndex);
				ArrayList<Entry<State, Double>> successors = new ArrayList<Entry<State, Double>>();
				while (tranIter.hasNext()) {
					Entry<Integer, Double> stateProbPair = tranIter.next();
					int succ = stateProbPair.getKey();
					State succState = mdp.getStatesList().get(stateProbPair.getKey());
					double prob = stateProbPair.getValue();
					successors.add(new AbstractMap.SimpleEntry<State, Double>(succState, prob));
					if (!toVisit.contains(succ) && !visited.get(succ)) {
						toVisit.add(succ);
					}
				}
				mdpCreator.addAction(sState, action, successors);
			}
		}
		return mdpCreator.mdp;
	}
	public MDPSimple createPolicy(int initialState, MDP mdp, Strategy strat)
	{
		Stack<Integer> toVisit = new Stack<Integer>();
		BitSet visited = new BitSet();
		toVisit.add(initialState);
		int s;
		while (!toVisit.isEmpty()) {
			s = toVisit.pop();
			visited.set(s);
			State sState = mdp.getStatesList().get(s);

			strat.initialise(s);
			//			strat.initialise(s);
			Object action = strat.getChoiceAction();
			int actionIndex = findActionIndex(mdp, s, action);

			if (actionIndex > -1) {
				Iterator<Entry<Integer, Double>> tranIter = mdp.getTransitionsIterator(s, actionIndex);
				ArrayList<Entry<State, Double>> successors = new ArrayList<Entry<State, Double>>();
				while (tranIter.hasNext()) {
					Entry<Integer, Double> stateProbPair = tranIter.next();
					int succ = stateProbPair.getKey();
					State succState = mdp.getStatesList().get(stateProbPair.getKey());
					double prob = stateProbPair.getValue();
					successors.add(new AbstractMap.SimpleEntry<State, Double>(succState, prob));
					if (!toVisit.contains(succ) && !visited.get(succ)) {
						toVisit.add(succ);
					}
				}
				mdpCreator.addAction(sState, action, successors);
			}
		}
		return mdpCreator.mdp;
	}


	public void savePolicy(String saveLocation, String name)
	{
		mdpCreator.saveMDP(saveLocation, name);
		System.out.println(name+" saved to "+ saveLocation);

	}
}
