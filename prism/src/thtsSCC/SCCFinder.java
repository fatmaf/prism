package thtsSCC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import parser.State;
import prism.PrismException;
import thts.Bounds;
import thts.Objectives;
import thtsNew.ActionSelector;
import thtsNew.ActionSelectorGreedySimpleLowerBound;
import thtsNew.ChanceNode;
import thtsNew.DecisionNode;
import thtsNew.Node;

//lets first create our own thing from the GSSP example 
//then everything has 2 or 1 action 
//we do the greedy path 
//as here already 
//then we do the other stuff the paper says 

public class SCCFinder {

	ActionSelector actSel;

	enum SCCType {
		Permanent, Transient, Neither, Unexplored
	}

	public SCCFinder() {
	}

	public SCCFinder(ActionSelector actSel) {
		this.actSel = actSel;
	}

	// from the GSSP paper fig 2
	public DecisionNode createTestGraph() throws PrismException {
		ArrayList<State> states = new ArrayList<State>();
		for (int i = 0; i < 6; i++) {
			State s = new State(1);
			s.setValue(0, i);
			states.add(s);
		}
		Objectives obj = Objectives.Cost;

		// s0 => s1, s3
		DecisionNode s0 = new DecisionNode(null, states.get(0), 1.0);
		s0.setBounds(obj, new Bounds(4, 4));
		// s0 - s1
		ChanceNode a0 = new ChanceNode(s0, states.get(0), "a0", -1);
		a0.setReward(obj, 2.0);
		s0.addChild("a0", a0);
		DecisionNode s1 = new DecisionNode(a0, states.get(1), 1.0);
		s1.setBounds(obj, new Bounds(2, 2));
		a0.addChild(s1);
		a0.setBounds(obj, new Bounds(2, 2));
		// a1 -> s1=>s2
		ChanceNode a1 = new ChanceNode(s1, states.get(1), "a1", -1);
		a1.setReward(obj, 0);
		a1.setBounds(obj, new Bounds(2, 2));
		s1.addChild("a1", a1);
		DecisionNode s2 = new DecisionNode(a1, states.get(2), 1.0);
		s2.setBounds(obj, new Bounds(2, 2));
		a1.addChild(s2);

		// a2 -> s2=> s1
		ChanceNode a2 = new ChanceNode(s2, states.get(2), "a2", -1);
		a2.addChild(s1);
		a2.setReward(obj, 0);
		a2.setBounds(obj, new Bounds(2, 2));
		s2.addChild("a2", a2);

		// a3 -> s0 => s3
		ChanceNode a3 = new ChanceNode(s0, states.get(0), "a3", -1);
		a3.setReward(obj, 0.5);
		DecisionNode s3 = new DecisionNode(a3, states.get(3), 1.0);
		a3.addChild(s3);
		s3.setBounds(obj, new Bounds(1.0, 1.0));
		s0.addChild("a3", a3);

		// a4 -> s3 => s4
		ChanceNode a4 = new ChanceNode(s3, states.get(3), "a4", -1);
		a4.setReward(obj, 0);
		a4.setBounds(obj, new Bounds(1.0, 1.0));
		s3.addChild("a4", a4);
		DecisionNode s4 = new DecisionNode(a4, states.get(4), 1.0);
		a4.addChild(s4);
		s4.setBounds(obj, new Bounds(1.0, 1.0));
		// a5 -> s4 => s3
		ChanceNode a5 = new ChanceNode(s4, states.get(4), "a5", -1);
		a5.setReward(obj, 0);
		a5.addChild(s3);
		a5.setBounds(obj, new Bounds(1.0, 1.0));
		s4.addChild("a5", a5);
		// g -> s4 => g
		ChanceNode a6 = new ChanceNode(s4, states.get(4), "g", -1);
		a6.setReward(obj, -1);
		a6.setBounds(obj, new Bounds(1, 1));
		s4.addChild("g", a6);
		DecisionNode g = new DecisionNode(a6, states.get(5), 1.0);
		g.setBounds(obj, new Bounds(0, 0));
		g.isGoal = true;
		a6.addChild(g);
		return s0;
	}

	public static void main(String[] args) {
		try {
			new SCCFinder().test();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void test() throws Exception {
		ArrayList<Objectives> tieBreakingOrder = new ArrayList<Objectives>();
		tieBreakingOrder.add(Objectives.Cost);
		ActionSelector actionSelection = new ActionSelectorGreedySimpleLowerBound(tieBreakingOrder,false);
		actSel = actionSelection;
//		actSel = null;
		DecisionNode root = this.createTestGraph();
//		doTarjan(root);
		findSCCs(root, true);

	}

	public void findSCCs(DecisionNode r, boolean fixSCCs) throws Exception {
		// helpers
		ArrayList<DecisionNode> visitedList = new ArrayList<>();
		Stack<DecisionNode> visitedStack = new Stack<>();
		HashMap<DecisionNode, Integer> id = new HashMap<>();
		HashMap<DecisionNode, Integer> lowlink = new HashMap<>();
		ArrayList<ArrayList<DecisionNode>> sccs = new ArrayList<>();

		// detect sccs

		dfsTarjan(r, visitedList, visitedStack, id, lowlink, sccs);
		// normally dfsTarjan would be called on all unvisited nodes in the "graph"
		// but this graph starts at the root node and all visited nodes are "reachable"
		// from the root node so we don't have to go over things
		boolean anyFixed = false;
		System.out.println(sccs.size() + " SCCs detected");
		ArrayList<SCCType> scctypes = new ArrayList<>();
		for (int i = 0; i < sccs.size(); i++) {
			String sccString = i + ": ";
			for (int j = 0; j < sccs.get(i).size(); j++) {
				sccString += sccs.get(i).get(j).getShortName() + " ";
			}
			System.out.println(sccString);
			ArrayList<ChanceNode> exitActions = new ArrayList<>();
			ArrayList<ChanceNode> stayActions = new ArrayList<>();
			SCCType scctype = sccAnalyser2(sccs.get(i), exitActions, stayActions);
			scctypes.add(scctype);
			if (fixSCCs) {
				anyFixed = anyFixed | fixSCC(sccs.get(i), scctype, exitActions, stayActions);
			}
		}

		if (anyFixed) {

			// set all the dns and cns to not solved unless its permanent
			for (int i = 0; i < sccs.size(); i++) {
				if (scctypes.get(i) != SCCType.Permanent) {
					ArrayList<DecisionNode> scc = sccs.get(i);
					for (DecisionNode d : scc) {
						d.setUnsolved();
						if (d.getChildren() != null) {
							for (Object a : d.getChildren().keySet()) {
								d.getChild(a).setUnsolved();
							}
						}
					}
				}
			}

		}
	}

	boolean fixSCC(ArrayList<DecisionNode> scc, SCCType scctype, ArrayList<ChanceNode> exitActions,
			ArrayList<ChanceNode> stayActions) throws Exception {
		// so we just assign the min or max and thats it
		// we do nothing else
		// if permenant we assign + or - infinity
		// to all nodes and their children
		boolean fixed = false;
		if (scctype == SCCType.Permanent) {
			for (DecisionNode d : scc) {
				setBoundsMinMax(d);
				if (d.getChildren() != null) {
					for (Object a : d.getChildren().keySet()) {
						ChanceNode cn = d.getChild(a);
						setBoundsMinMax(cn);
						cn.setSolved();
					}
				}
				d.setSolved();

			}
			fixed = true;
		} else if (scctype == SCCType.Transient) {
			// if (scc.size() > 1) {
			// so technically we want to collapse these into one big state
			// but we just set the values to the best exit action
			// from the exit actions lets find the best exit action
			ChanceNode bestCN = findBestAction(exitActions);
			for (DecisionNode d : scc) {
				d.setBounds(bestCN.bounds);
//				if (d.getChildren() != null) {
//					for (Object a : d.getChildren().keySet()) {
//						ChanceNode cn = d.getChild(a);
//						
////						cn.setSolved();
//					}
//				}
//				d.setSolved();

			}

			for (ChanceNode cn : stayActions) {
				if (scc.size() > 1) {
					cn.setBounds(bestCN.bounds);
				}
				if (scc.size() == 1)
					cn.ignoreAction = true;

			}
			fixed = true;
			// }
		}
		return fixed;
	}

	ChanceNode findBestAction(ArrayList<ChanceNode> cns) throws Exception {
		ChanceNode bestCN = null;
		if (actSel != null) {
			DecisionNode dummy = new DecisionNode(null, null, 1.0);
			for (ChanceNode cn : cns) {
				dummy.addChild(cn.getAction(), cn);
			}

			bestCN = actSel.selectAction(dummy, false);
		} else {
			// lets just assume we have one objective cost
			// minimise cost
			Objectives obj = Objectives.Cost;
			Bounds bestB = new Bounds(Double.MAX_VALUE, Double.MAX_VALUE);

			for (ChanceNode cn : cns) {
				if (cn.boundsInitialised()) {
					Bounds b = cn.getBounds(obj);
					if (b.getLower() < bestB.getLower()) {
						bestB.setLower(b.getLower());
						bestB.setUpper(b.getUpper());
						bestCN = cn;
					}
				}
			}

		}
		System.out.println("Best Action:" + bestCN);
		return bestCN;

	}

	void setBoundsMinMax(Node n) {
		if (n.boundsInitialised()) {
			for (Objectives obj : n.bounds.keySet()) {
				switch (obj) {
				case Cost:
					// max
					n.setBounds(obj, new Bounds(Double.MAX_VALUE, Double.MAX_VALUE));
					break;
				case Probability:
				case TaskCompletion:
				case Progression:
					n.setBounds(obj, new Bounds(0, 0));
					break;
				default:
					break;
				}
			}
		}
	}

	SCCType sccAnalyser(ArrayList<DecisionNode> scc, ArrayList<ChanceNode> exitActions,
			ArrayList<ChanceNode> stayActions) throws Exception {

		// perm => no actions that lead to a state outside of the scc even without the
		// greedy ones //and goal isnt include
		// tran => some actions that lead to a state outside of the scc using greedy
		// policy //goal isnt part of the scc
		// neither => actions lead outside of the scc
		SCCType toret = SCCType.Neither;
		// does the scc have any actions that lead outside of the scc or to the goal?
		// if no - perm
		// if yes, are these actions actions on the greedy thing ?
		// if no - tran
		// if yes - neither

		boolean exitFound = false;
		boolean goalFound = false;
		boolean exitOnGreedyAction = false;
		boolean goalFoundOnExit = false;
		boolean unexplored = false;
		for (DecisionNode d : scc) {
			if (d.isGoal) {
				goalFound = true;

			}
			if (d.getChildren() != null) {
				// greedy action
				ChanceNode ga = null;
				if (actSel != null) {
					ga = actSel.selectAction(d, true);
				}
				for (Object a : d.getChildren().keySet()) {
					ChanceNode c = d.getChild(a);
					boolean isExitAction = false;
					boolean allExit = true;
					for (DecisionNode dc : c.getChildren()) {
						boolean dcExit = false;
						if (!scc.contains(dc)) {
							dcExit = true;
							isExitAction = true;
							exitFound = true;
							if (!exitActions.contains(c))
								exitActions.add(c);
							if (dc.isGoal)
								goalFoundOnExit = true;

							if (ga != null && c == ga) {
//								toret = tran;
								exitOnGreedyAction = true;
//								break;
							}
//							else {
//								break;
//							}
						} else {
							if (dc.isGoal) {
								goalFound = true;
//								break;
							}
						}
						allExit = allExit & dcExit;

					}
					if (!isExitAction) {
						stayActions.add(c);
					}
				}
			} else {
				// is it a deadend or a goal ?
				// if not then its neither
				// there is a possibility of an exit
				if (d.canHaveChildren()) {
					unexplored = true;
				}
			}

		}

		// if you dont find an exit and you find a goal then the goal is in the scc
		// so that scc is neither
		// if you find an exit and the exit is on a non greedy action
		// its a transient trap
		// if you find an exit and the exit is on a greedy action
		// its neither
		// if you dont find an exit and you dont find a goal then its a permenant trap
		if (!goalFound) {
			if (!exitFound) // no exit
			{
				if (!unexplored)
					toret = SCCType.Permanent;
				else
					toret = SCCType.Unexplored;
			} else {
				if (!exitOnGreedyAction) {
					toret = SCCType.Transient;
				}
			}
		}
		// else goal found in scc so not a trap
//		if (!goalFound & !exitFound) {
//			toret = perm;
//		}
		System.out.println("SCC analysed: " + toret.toString());
		return toret;

	}

	SCCType sccAnalyser2(ArrayList<DecisionNode> scc, ArrayList<ChanceNode> exitActions,
			ArrayList<ChanceNode> stayActions) throws Exception {

		// perm => no actions that lead to a state outside of the scc even without the
		// greedy ones //and goal isnt include
		// tran => some actions that lead to a state outside of the scc using greedy
		// policy //goal isnt part of the scc
		// neither => actions lead outside of the scc
		SCCType toret = SCCType.Neither;
		// does the scc have any actions that lead outside of the scc or to the goal?
		// if no - perm
		// if yes, are these actions actions on the greedy thing ?
		// if no - tran
		// if yes - neither

		boolean exitFound = false;
		boolean goalFound = false;
		boolean exitOnGreedyAction = false;
		boolean goalFoundOnExit = false;
		boolean unexplored = false;
		for (DecisionNode d : scc) {
			if (d.isGoal) {
				goalFound = true;

			}
			if (d.getChildren() != null) {
				// greedy action
				ChanceNode ga = null;
				if (actSel != null) {
					ga = actSel.selectAction(d, true);
				}
				for (Object a : d.getChildren().keySet()) {
					ChanceNode c = d.getChild(a);
					boolean isExitAction = false;
					boolean allExit = true;
					for (DecisionNode dc : c.getChildren()) {
						boolean dcExit = false;
						if (!scc.contains(dc)) {
							dcExit = true;
//							isExitAction = true;
//							exitFound = true;
//							if (!exitActions.contains(c))
//								exitActions.add(c);
							if (dc.isGoal)
								goalFoundOnExit = true;

//							else {
//								break;
//							}
						} else {
							if (dc.isGoal) {
								goalFound = true;
//								break;
							}
						}
						allExit = allExit & dcExit;

					}
					if (allExit) {
						exitFound = true;
						isExitAction = true;
						if (!exitActions.contains(c))
							exitActions.add(c);
						if (ga != null && c == ga) {
//							toret = tran;
							exitOnGreedyAction = true;
//							break;
						}
					}
					if (!isExitAction) {
						stayActions.add(c);
					}
				}
			} else {
				// is it a deadend or a goal ?
				// if not then its neither
				// there is a possibility of an exit
				if (d.canHaveChildren()) {
					unexplored = true;
				}
			}

		}

		// if you dont find an exit and you find a goal then the goal is in the scc
		// so that scc is neither
		// if you find an exit and the exit is on a non greedy action
		// its a transient trap
		// if you find an exit and the exit is on a greedy action
		// its neither
		// if you dont find an exit and you dont find a goal then its a permenant trap
		if (!goalFound) {
			if (!exitFound) // no exit
			{
				if (!unexplored)
					toret = SCCType.Permanent;
				else
					toret = SCCType.Unexplored;
			} else {
				if (!exitOnGreedyAction) {
					toret = SCCType.Transient;
				}
			}
		}
		// else goal found in scc so not a trap
//		if (!goalFound & !exitFound) {
//			toret = perm;
//		}
		System.out.println("SCC analysed: " + toret.toString());
		return toret;

	}

	void dfsTarjan(DecisionNode r, ArrayList<DecisionNode> visitedList, Stack<DecisionNode> visitedStack,
			HashMap<DecisionNode, Integer> id, HashMap<DecisionNode, Integer> lowlink,
			ArrayList<ArrayList<DecisionNode>> sccs) throws Exception {

//for each unvisited vertex u
//
//  DFS(u), s.push(u), num[u] = low[u] = DFSCount
//
//    for each neighbor v of u
//
//      if v is unvisited, DFS(v)
//
//      low[u] = min(low[u], low[v])
//
//    if low[u] == num[u] // root of an SCC
//
//      pop from stack s until we get u
//
//      
		if (!visitedList.contains(r)) {
			visitedList.add(r);
			visitedStack.push(r);
			id.put(r, id.size());
			lowlink.put(r, lowlink.size());

			if (r.getChildren() != null) {
				ArrayList<ChanceNode> cns = new ArrayList<>();
				if (actSel == null) {
					for (Object a : r.getChildren().keySet()) {
						cns.add(r.getChild(a));
					}
				} else {
					ChanceNode cn = actSel.selectAction(r, true);
					cns.add(cn);
				}
				for (ChanceNode c : cns) {
					if (c.getChildren() != null) {
						for (DecisionNode child : c.getChildren()) {
							dfsTarjan(child, visitedList, visitedStack, id, lowlink, sccs);
							if (visitedStack.contains(child)) {
								int minlowlink = Math.min(lowlink.get(r), lowlink.get(child));
								lowlink.put(r, minlowlink);
							}
						}
					}
				}
			}
			if (lowlink.get(r) == id.get(r)) {
				System.out.println("SCC detected!");
				String sccString = "";
				DecisionNode d;
				ArrayList<DecisionNode> scc = new ArrayList<>();
				do {
					d = visitedStack.pop();
					lowlink.put(d, lowlink.get(r));
					sccString += d.getShortName() + " ";
					scc.add(d);
				} while (d != r);
				System.out.println(sccString);
				sccs.add(scc);
			}

		}
	}

//	// return the set of sccs as nodes
//	void doTarjan(DecisionNode r) {
//		ArrayList<DecisionNode> visited = new ArrayList<>();
//		Queue<DecisionNode> tovisit = new LinkedList<>();
//		HashMap<DecisionNode, Integer> indices = new HashMap<>();
//		HashMap<DecisionNode, Integer> lowlinks = new HashMap<>();
//		Stack<DecisionNode> stack = new Stack<>();
//		int index = 0;
//
//		tovisit.add(r);
//		while (!tovisit.isEmpty()) {
//			DecisionNode dn = tovisit.remove();
//			visited.add(dn);
//			if (!indices.containsKey(dn)) {
//				index = strongconnect(dn, indices, lowlinks, index, stack);
//
//			}
//			if (dn.getChildren() != null) {
////				for (Object a : dn.getChildren().keySet()) {
//				ChanceNode cn = actSel.selectAction(dn, false);
////					ChanceNode cn = dn.getChild(a);
////					{
//
//				for (DecisionNode cdn : cn.getChildren()) {
//					if (!tovisit.contains(cdn) & !visited.contains(cdn))
//						tovisit.add(cdn);
//				}
//			}
////				}
////			}
//		}
//
////		return null;
//	}
////	algorithm tarjan is
////    input: graph G = (V, E)
////    output: set of strongly connected components (sets of vertices)
////   
////    index := 0
////    S := empty stack
////    for each v in V do
////        if v.index is undefined then
////            strongconnect(v)
////        end if
////    end for
////   
//
//	int strongconnect(DecisionNode dn, HashMap<DecisionNode, Integer> indices, HashMap<DecisionNode, Integer> lowlinks,
//			int index, Stack<DecisionNode> stack) {
////	    function strongconnect(v)
////        // Set the depth index for v to the smallest unused index
////        v.index := index
//		indices.put(dn, index);
////        v.lowlink := index
//		lowlinks.put(dn, index);
////        index := index + 1
//		index++;
////        S.push(v)
//		stack.push(dn);
////        v.onStack := true
////      
////        // Consider successors of v
////        for each (v, w) in E do
//		if (dn.getChildren() != null) {
//			ChanceNode cn = actSel.selectAction(dn, false);
////			for (Object a : dn.getChildren().keySet()) {
////				ChanceNode cn = dn.getChild(a);
////				{
//
//			for (DecisionNode cdn : cn.getChildren()) {
////			            if w.index is undefined then
////		                // Successor w has not yet been visited; recurse on it
////		                strongconnect(w)
//				if (!indices.containsKey(cdn)) {
//					index = strongconnect(cdn, indices, lowlinks, index, stack);
////		                v.lowlink := min(v.lowlink, w.lowlink)
//					int dnlowlink = Math.min(lowlinks.get(dn), lowlinks.get(cdn));
//					lowlinks.put(dn, dnlowlink);
//				}
////		            else if w.onStack then
//				else {
//					if (stack.contains(cdn)) {
//						// Successor w is in stack S and hence in the current SCC
////				                // If w is not on stack, then (v, w) is an edge pointing to an SCC already found and must be ignored
////				                // Note: The next line may look odd - but is correct.
////				                // It says w.index not w.lowlink; that is deliberate and from the original paper
////				                v.lowlink := min(v.lowlink, w.index)
//						int dnlowlink = Math.min(lowlinks.get(dn), indices.get(cdn));
//						lowlinks.put(dn, dnlowlink);
////				            end if
//					}
//				}
////		                
////		        end for
//			}
//		}
////			}
////		}
//
////      
////        // If v is a root node, pop the stack and generate an SCC
////        if v.lowlink = v.index then
//		if (lowlinks.get(dn) == indices.get(dn)) {
//			ArrayList<DecisionNode> scc = new ArrayList<DecisionNode>();
////			scc.add(dn);
////            start a new strongly connected component
////            repeat
//			DecisionNode cdn = null;
//			do {
//				cdn = stack.pop();
//				scc.add(cdn);
//			} while (dn != cdn);
////                w := S.pop()
////                w.onStack := false
////                add w to current strongly connected component
////            while w ≠ v
//			String strPrint = "";
//			for (int i = 0; i < scc.size(); i++)
//				strPrint += scc.get(i).getState() + " ";
//			System.out.println(strPrint);
////            output the current strongly connected component
////        end if
//		}
////    end function
//		// strongconnect dn
//
//		return index;
//
//	}
}
