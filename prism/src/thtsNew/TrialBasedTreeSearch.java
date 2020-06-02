package thtsNew;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import parser.State;
import prism.PrismException;
import prism.PrismLog;
import prism.DefaultModelGenerator;
import thts.Bounds;
import thts.Objectives;

public class TrialBasedTreeSearch {

	DefaultModelGenerator productModelGen;
	int maxRollouts;
	int maxTrialLen;
	Heuristic hf;
	ActionSelector actSel;
	OutcomeSelector outSel;
	RewardHelper rewH;
	Backup backup;
	PrismLog mainLog;

	HashMap<String, Node> nodesAddedSoFar;
	private int decisionNodesExplored;
	private int chanceNodesExplored;
	private int numRollouts;
	private int trialLen;
	ArrayList<Objectives> tieBreakingOrder;

	public TrialBasedTreeSearch(DefaultModelGenerator pmg, int maxRollouts, int maxTrialLen,
			Heuristic heuristicFunction, ActionSelector actionSelection, OutcomeSelector outcomeSelection,
			RewardHelper rewardHelper, Backup backupFunction, ArrayList<Objectives> tieBreakingOrder, PrismLog ml) {
		productModelGen = pmg;
		this.maxRollouts = maxRollouts;
		this.maxTrialLen = maxTrialLen;
		mainLog = ml;
		hf = heuristicFunction;
		actSel = actionSelection;
		outSel = outcomeSelection;
		rewH = rewardHelper;
		backup = backupFunction;
		decisionNodesExplored = 0;
		chanceNodesExplored = 0;
		this.tieBreakingOrder = tieBreakingOrder;
	}

	public Node getRootNode() throws PrismException {
		List<State> initialStates = productModelGen.getInitialStates();
		if (initialStates.size() > 1) {
			// just pick 0
			System.out.println("More than 1 root node!!!");
		}
		State s = initialStates.get(0);
		return createDecisionNode(null, s, 1.0);

	}

	public boolean checkNodeInHash(String k) {
		if (nodesAddedSoFar == null) {
			nodesAddedSoFar = new HashMap<>();
		}
		return nodesAddedSoFar.containsKey(k);
	}

	private Node getNodeFromHash(String k) {
		if (nodesAddedSoFar == null) {
			nodesAddedSoFar = new HashMap<>();
		}
		if (!nodesAddedSoFar.containsKey(k))
			return null;
		else
			return nodesAddedSoFar.get(k);
	}

	DecisionNode createDecisionNode(Node ps, State s, double tprob) throws PrismException {

		DecisionNode dn = null;
		// check if this node exists
		String k = s.toString();
		if (checkNodeInHash(k)) {
			dn = (DecisionNode) getNodeFromHash(k);
			dn.addParent(ps, tprob);
			mainLog.println("DN exists " + dn);
			return dn;

		}
		dn = new DecisionNode(ps, s, tprob);

		addNodeToHash(dn);
		return dn;
	}

	public Node addNodeToHash(Node n) {
		String k = n.getShortName();
		return addNodeToHash(n, k);

	}

	public Node addNodeToHash(Node n, String k) {
		Node nodeInMap = n;
		boolean added = false;
		if (!nodesAddedSoFar.containsKey(k)) {
			if (n instanceof DecisionNode)
				this.decisionNodesExplored++;
			else
				this.chanceNodesExplored++;

			nodesAddedSoFar.put(k, n);
			added = true;
		} else {
			nodeInMap = nodesAddedSoFar.get(k);
		}
		return nodeInMap;
	}

	public void run() throws PrismException {

		Node n0 = getRootNode();
		while (!n0.isSolved() & notTimedOut()) {
			visitDecisionNode((DecisionNode) n0);

		}
	}

	void visitDecisionNode(DecisionNode n) throws PrismException {
		if (!n.isSolved() & notTimedOut()) {
			trialLen++;

			String ps =trialLen+":"+n.getShortName();
			
			if (n.numVisits == 0) {
				setNodeHeuristics(n);
			}
			n.numVisits++;
			// mark for goal or deadend
			n.isGoal = rewH.isGoal(n);
			n.isDeadend = rewH.isDeadend(n);
			if (n.canHaveChildren()) {
				// lrtdp has a forward backup
//				backup.backupDecisionNode(n);
				// select an action
				// so we've got to check all the actions associated with this node
				// and randomly select one
				
				ChanceNode selectedAction = selectAction(n);
				ps+="-"+selectedAction.getAction().toString();
				System.out.println(ps);
				visitChanceNode(selectedAction);
				// backupDecisionNode(n)
				backup.backupDecisionNode(n);
			}

		}
	}

	void visitChanceNode(ChanceNode n) throws PrismException {

		if (!n.isSolved() & notTimedOut()) {
			n.numVisits++;
			ArrayList<DecisionNode> selectedOutcome = selectOutcome(n);
			
			// first we've got to initialise this chance node
			// meaning get its children really
			// or we could just select an outcome ?
			// but we still need to know how many children it has
			// and then we've got to select one of them
			// and in prism that means going over all of them
//			backup.backupChanceNode(n);
			// outcome = selectOutcome(n)
			// for nd in outcome
			for (DecisionNode nd : selectedOutcome) {
				visitDecisionNode(nd);
			}
			// visitDecisionNode(nd)
			// backupChanceNode(n)
			backup.backupChanceNode(n);
		}
	}

	boolean notTimedOut() {
		return (this.trialLen < this.maxTrialLen);// (this.numRollouts < this.maxRollouts);
	}

	void setNodeHeuristics(Node n0) throws PrismException {
		State s = n0.getState();
		HashMap<Objectives, Bounds> nodehs = hf.getStateBounds(tieBreakingOrder, s);
		if (n0 instanceof DecisionNode)
			((DecisionNode) n0).setBounds(nodehs);

	}

	ChanceNode selectAction(DecisionNode n0) throws PrismException {
		boolean doMin = false;
		// we set the children and their bounds
		// it would be interesting to do this on the fly
		// but i'm not sure how to without sending mapmg to action
		// selector
		// perhaps thats a good idea
		// no I dont think so
		// i think its better to do it here
		if (actSel instanceof ActionSelectorGreedyBoundsDiff) {
			// so first we've got to see if it has no children
			generateChildrenDecisionNode(n0);
			// then we've got to make sure we initialise
			// the bounds for all children

		}
		return actSel.selectAction(n0, doMin);
	}

	ArrayList<DecisionNode> selectOutcome(ChanceNode n) throws PrismException {
		generateChildrenChanceNode(n);
		return outSel.selectOutcome(n);
	}

	void generateChildrenChanceNode(ChanceNode n0) throws PrismException {
		if (n0.getChildren() == null) {
			// we need its children
			productModelGen.exploreState(n0.getState());
			// now we get the children
			int c = n0.actionChoice;
			int numtransitions = productModelGen.getNumTransitions(c);
			n0.numChildren = numtransitions;
			for (int t = 0; t < numtransitions; t++) {
				double prob = productModelGen.getTransitionProbability(c, t);
				State ns = productModelGen.computeTransitionTarget(c, t);
				DecisionNode child = new DecisionNode(n0, ns, prob);
				n0.addChild(child);

			}
		}

	}

	void generateChildrenDecisionNode(DecisionNode n0) throws PrismException {
		// generate the children of a decision node
		if (n0.canHaveChildren()) {
			if (n0.numActions == 0) {
				// generate its children
				// how do we do that ?
				// well we've got a bunch of actions
				// hmmm the quicker way to do this is to just choose an action
				// assuming they all have uninitialised bounds
				// and then you randomly choose one
				// and then you can go ahead and get bounds for them
				// that would make sense
				productModelGen.exploreState(n0.s);
				int numActions = productModelGen.getNumChoices();
				n0.numActions = numActions;
				// each action is a chance node
				for (int i = 0; i < numActions; i++) {
					// we can get the name and choice index
					Object action = productModelGen.getChoiceAction(i);
					ChanceNode cn = new ChanceNode(n0, n0.s, action, i);
					for (Objectives obj : tieBreakingOrder) {
						double rew = rewH.getReward(obj, cn);
						cn.setReward(obj, rew);
					}
					n0.addChild(action, cn);

				}
			}
		}
	}
}
