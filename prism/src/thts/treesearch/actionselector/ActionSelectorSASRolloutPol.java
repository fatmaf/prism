package thts.treesearch.actionselector;

import java.util.ArrayList;
import java.util.HashMap;

import parser.State;
import thts.treesearch.utils.ChanceNode;
import thts.treesearch.utils.DecisionNode;
import thts.modelgens.MultiAgentNestedProductModelGenerator;

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
		boolean setsatowait=false;
		State s = nd.getState(); 
		ArrayList<State> robotStates = mapmg.getModelAndDAStates(s, true);
		//then for that agent we get the best action 
		ArrayList<String> robotActions = new ArrayList<>(); 
		for(int i = 0; i<robotStates.size();i++)
		{
		    if(!stateActions.get(i).containsKey(robotStates.get(i)))
		        System.out.println(String.format("No state in Action selector sas rollout pol for robot %d, state %s",i,robotStates.get(i).toString()));
			String sa = stateActions.get(i).get(robotStates.get(i)).toString();
			if(sa.contentEquals("?")) {
				//does it have a failed or wait
				boolean waitfound = false;
				ArrayList<Object> thisRobotsActions = mapmg.getAvailableRobotActionsInState(s, i);
				for(Object o: thisRobotsActions)
				{
					if(o!=null)
					{
						if(o.toString().contentEquals("wait")) {
							sa = o.toString();
							//waitfound=true;
							break;
						}

							if(o.toString().contentEquals("failed"))
							{
								sa=o.toString();

							}

					}
				}
				//sa = "wait";
				//sa = "failed";
				setsatowait=true;
			}
			robotActions.add(sa); 
		}
		//then we create a joint action using that 
		String ja = mapmg.createJointActionFromString(robotActions);
		//check if such a joint action exists 
		//return the matching chance node =D 
		if(nd.getChildren().containsKey(ja))
		{
			return nd.getChild(ja);
		}


		String msg="Unable to find action "+ja + " for state "+s.toString();
		System.out.println(msg); 
		throw new Exception(msg);
//		return null;
	}

}
