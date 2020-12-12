package thts.outcomeselector;

import thts.treesearch.ChanceNode;
import thts.treesearch.DecisionNode;

import java.util.ArrayList;

public interface OutcomeSelector {

	ArrayList<DecisionNode> selectOutcome(ChanceNode cn);
}
