package thtsNew;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class SCCFinder {
	ActionSelector actSel;
	public SCCFinder(ActionSelector actSel)
	{
		this.actSel = actSel;
	}
	// return the set of sccs as nodes
	void doTarjan(DecisionNode r) {
		ArrayList<DecisionNode> visited = new ArrayList<>();
		Queue<DecisionNode> tovisit = new LinkedList<>();
		HashMap<DecisionNode, Integer> indices = new HashMap<>();
		HashMap<DecisionNode, Integer> lowlinks = new HashMap<>();
		Stack<DecisionNode> stack = new Stack<>();
		int index = 0;

		tovisit.add(r);
		while (!tovisit.isEmpty()) {
			DecisionNode dn = tovisit.remove();
			visited.add(dn);
			if (!indices.containsKey(dn)) {
				index = strongconnect(dn, indices, lowlinks, index, stack);

			}
			if (dn.getChildren()!=null) {
//				for (Object a : dn.getChildren().keySet()) {
			ChanceNode cn = actSel.selectAction(dn, false);
//					ChanceNode cn = dn.getChild(a);
//					{

						for (DecisionNode cdn : cn.getChildren()) {
							if (!tovisit.contains(cdn) & !visited.contains(cdn))
								tovisit.add(cdn);
						}
					}
//				}
//			}
		}

//		return null;
	}
//	algorithm tarjan is
//    input: graph G = (V, E)
//    output: set of strongly connected components (sets of vertices)
//   
//    index := 0
//    S := empty stack
//    for each v in V do
//        if v.index is undefined then
//            strongconnect(v)
//        end if
//    end for
//   

	int strongconnect(DecisionNode dn, HashMap<DecisionNode, Integer> indices, HashMap<DecisionNode, Integer> lowlinks,
			int index, Stack<DecisionNode> stack) {
//	    function strongconnect(v)
//        // Set the depth index for v to the smallest unused index
//        v.index := index
		indices.put(dn, index);
//        v.lowlink := index
		lowlinks.put(dn, index);
//        index := index + 1
		index++;
//        S.push(v)
		stack.push(dn);
//        v.onStack := true
//      
//        // Consider successors of v
//        for each (v, w) in E do
		if (dn.getChildren()!=null) {
			ChanceNode cn = actSel.selectAction(dn, false);
//			for (Object a : dn.getChildren().keySet()) {
//				ChanceNode cn = dn.getChild(a);
//				{

					for (DecisionNode cdn : cn.getChildren()) {
//			            if w.index is undefined then
//		                // Successor w has not yet been visited; recurse on it
//		                strongconnect(w)
						if (!indices.containsKey(cdn)) {
							index = strongconnect(cdn, indices, lowlinks, index, stack);
//		                v.lowlink := min(v.lowlink, w.lowlink)
							int dnlowlink = Math.min(lowlinks.get(dn), lowlinks.get(cdn));
							lowlinks.put(dn, dnlowlink);
						}
//		            else if w.onStack then
						else {
							if (stack.contains(cdn)) {
								// Successor w is in stack S and hence in the current SCC
//				                // If w is not on stack, then (v, w) is an edge pointing to an SCC already found and must be ignored
//				                // Note: The next line may look odd - but is correct.
//				                // It says w.index not w.lowlink; that is deliberate and from the original paper
//				                v.lowlink := min(v.lowlink, w.index)
								int dnlowlink = Math.min(lowlinks.get(dn), indices.get(cdn));
								lowlinks.put(dn, dnlowlink);
//				            end if
							}
						}
//		                
//		        end for
					}
				}
//			}
//		}

//      
//        // If v is a root node, pop the stack and generate an SCC
//        if v.lowlink = v.index then
		if (lowlinks.get(dn) == indices.get(dn)) {
			ArrayList<DecisionNode> scc = new ArrayList<DecisionNode>();
			scc.add(dn);
//            start a new strongly connected component
//            repeat
			DecisionNode cdn = null;
			do {
				cdn = stack.pop();
				scc.add(cdn);
			} while (dn != cdn);
//                w := S.pop()
//                w.onStack := false
//                add w to current strongly connected component
//            while w ≠ v
			String strPrint = "";
			for(int i = 0; i<scc.size(); i++)
				strPrint+=scc.get(i).getState()+" ";
			System.out.println(strPrint);
//            output the current strongly connected component
//        end if
		}
//    end function
		// strongconnect dn

		return index;

	}
}
