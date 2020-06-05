package thtsNew;

import parser.State;
import prism.PrismException;
import thts.Objectives;

//because i've got objectives and not reward structures 
//so its kind of a layer you can use to get rewards using objectives
public interface RewardHelper {
	double getReward(Objectives obj,ChanceNode n) throws PrismException;
//	boolean isGoal(DecisionNode n); 
//	boolean isDeadend(DecisionNode n) throws PrismException; 
}
