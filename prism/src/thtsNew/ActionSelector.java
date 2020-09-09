package thtsNew;

import java.util.Map.Entry;

public interface ActionSelector {

	//selects an action from a decision node 
	//the nodes children must exist 
	//since this does no checking etc 
	//the boolean doMin does min/max on bounds or whatever you'd like 
	public ChanceNode selectAction(DecisionNode nd,boolean doMin) throws Exception;
}
