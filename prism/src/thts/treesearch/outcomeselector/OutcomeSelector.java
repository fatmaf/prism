package thts.treesearch.outcomeselector;

import thts.treesearch.utils.ChanceNode;
import thts.treesearch.utils.DecisionNode;

import java.util.ArrayList;

public interface OutcomeSelector {

	ArrayList<DecisionNode> selectOutcome(ChanceNode cn);
}
