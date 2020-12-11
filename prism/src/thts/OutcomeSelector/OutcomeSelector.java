package thts.OutcomeSelector;

import thts.TreeSearch.ChanceNode;
import thts.TreeSearch.DecisionNode;

import java.util.ArrayList;

public interface OutcomeSelector {

	ArrayList<DecisionNode> selectOutcome(ChanceNode cn);
}
