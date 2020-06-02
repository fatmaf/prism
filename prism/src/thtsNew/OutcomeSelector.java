package thtsNew;

import java.util.ArrayList;

public interface OutcomeSelector {

	ArrayList<DecisionNode> selectOutcome(ChanceNode cn);
}
