package thts.treesearch.outcomeselector;

import thts.treesearch.utils.ChanceNode;
import thts.treesearch.utils.DecisionNode;

import java.util.ArrayList;
import java.util.Random;

public class OutcomeSelectorRandomSkipSolved implements OutcomeSelector {

	Random rgen;

	@Override
	public ArrayList<DecisionNode> selectOutcome(ChanceNode cn) {
		ArrayList<DecisionNode> selectedDNs = new ArrayList<>();
		// so now we just do this thing that was in the brtdp paper
		DecisionNode selectedDecisionNode = null;

	
		rgen = new Random();
		ArrayList<DecisionNode> initChildren = cn.getChildren();

		ArrayList<Integer> selectedNodes = new ArrayList<>();
		int chosenChild = rgen.nextInt(initChildren.size());
		selectedNodes.add(chosenChild);
		
		selectedDecisionNode = initChildren.get(chosenChild);
		while(!selectedDecisionNode.canHaveChildren() || selectedDecisionNode.isSolved())
		{
			if(selectedNodes.size() == initChildren.size())
			{
				selectedDecisionNode = null; 
				break;
			}
			chosenChild = rgen.nextInt(initChildren.size());
			while(selectedNodes.contains(chosenChild))
				chosenChild = rgen.nextInt(initChildren.size());
			selectedNodes.add(chosenChild);

			selectedDecisionNode = initChildren.get(chosenChild);
		}
		selectedDNs.add(selectedDecisionNode);
		return selectedDNs;

	}

}
