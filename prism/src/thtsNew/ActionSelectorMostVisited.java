package thtsNew;

import java.util.ArrayList;

public class ActionSelectorMostVisited implements ActionSelector {

	@Override
	public ChanceNode selectAction(DecisionNode nd, boolean doMin) throws Exception {
		ChanceNode selectedActionNode = null;
		if (nd.allChildrenInitialised()) {
			ChanceNode greedyAction = null;

			for (Object a : nd.getChildren().keySet()) {
				ChanceNode cn = nd.getChild(a);
				if (cn.ignoreAction)
					continue;
				if (greedyAction == null)
					greedyAction = cn;
				else {
					if (greedyAction.numVisits < cn.numVisits) {
						greedyAction = cn;
					}
				}
			}

			selectedActionNode = greedyAction;
		} else {
			ArrayList<ChanceNode> initChildren = nd.childrenWithuninitialisedBounds();
			ChanceNode greedyAction = null;

			for (ChanceNode cn : initChildren) {
				if (greedyAction == null)
					greedyAction = cn;
				else {
					if (greedyAction.numVisits < cn.numVisits) {
						greedyAction = cn;
					}
				}
			}
			selectedActionNode = greedyAction;
		}
		return selectedActionNode;

	}

}
