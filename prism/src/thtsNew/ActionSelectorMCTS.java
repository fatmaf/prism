package thtsNew;



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

}
