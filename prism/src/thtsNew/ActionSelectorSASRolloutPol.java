package thtsNew;

import java.util.ArrayList;
import java.util.HashMap;

import parser.State;

public class ActionSelectorSASRolloutPol implements ActionSelector {

	// so we want mapmg 
	MultiAgentNestedProductModelGenerator mapmg;
	// and the stateactions 
	ArrayList<HashMap<State,Object>> stateActions;
	
	public ActionSelectorSASRolloutPol(MultiAgentNestedProductModelGenerator mapmg,
			ArrayList<HashMap<State,Object>> stateActions)
	{
		this.mapmg = mapmg; 
		this.stateActions = stateActions; 
	}
	@Override
	public ChanceNode selectAction(DecisionNode nd, boolean doMin) throws Exception {
		//so for a node 
		//we split it into its single agent states 
		State s = nd.getState(); 
		ArrayList<State> robotStates = mapmg.getModelAndDAStates(s, true);
		//then for that agent we get the best action 
		ArrayList<String> robotActions = new ArrayList<>(); 
		for(int i = 0; i<robotStates.size();i++)
		{
			robotActions.add(stateActions.get(i).get(robotStates.get(i)).toString()); 
		}
		//then we create a joint action using that 
		String ja = mapmg.createJointActionFromString(robotActions);
		//check if such a joint action exists 
		//return the matching chance node =D 
		if(nd.children.containsKey(ja))
		{
			return nd.getChild(ja);
		}

		return null;
	}

}
