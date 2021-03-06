package thts.treesearch.actionselector;


import prism.PrismLog;
import thts.treesearch.utils.ChanceNode;
import thts.treesearch.utils.DecisionNode;

//takes as input an action selection class and a value for softmax
public class ActionSelectorMCTS implements ActionSelector {

	ActionSelector actSel;
	ActionSelector rolPol; 
	public ActionSelectorMCTS(ActionSelector actSel, ActionSelector rolPol) {
		this.actSel = actSel;
		this.rolPol = rolPol; 
	}

	@Override
	public ChanceNode selectAction(DecisionNode nd, boolean doMin) throws Exception {
		// so if the decision node has 0 visits, we go ahead and follow our rollout policy 
		// if not we follow something else 
		if(nd.numVisits<=1)
			return rolPol.selectAction(nd, doMin); 
		else 
			return actSel.selectAction(nd,doMin); 
		

	}
	@Override
	public ChanceNode selectAction(DecisionNode nd, boolean doMin, PrismLog fileLog) throws Exception {
		// so if the decision node has 0 visits, we go ahead and follow our rollout policy
		// if not we follow something else
		if(nd.numVisits<=1)
			return rolPol.selectAction(nd, doMin,fileLog);
		else
			return actSel.selectAction(nd,doMin,fileLog);


	}
}
