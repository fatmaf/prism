package thts.rewardhelper;


import prism.PrismException;
import thts.treesearch.Objectives;
import thts.modelgens.NestedProductModelGenerator;
import thts.treesearch.ChanceNode;

public class RewardHelperNestedSingleAgent implements RewardHelper {

	NestedProductModelGenerator singleAgentNestedProduct;


	public RewardHelperNestedSingleAgent(NestedProductModelGenerator singleAgentNestedProduct) {
		this.singleAgentNestedProduct = singleAgentNestedProduct;

	}


	@Override
	public double getReward(Objectives obj, ChanceNode n) throws PrismException {
		singleAgentNestedProduct.exploreState(n.getState());
		double rew=0;

		switch (obj) {
		case TaskCompletion: {

			rew = singleAgentNestedProduct.getStateActionTaskCompletionReward(n.getState(), n.actionChoice);

			break;
		}
		case Cost: {
			rew = singleAgentNestedProduct.getStateActionReward(0,n.getState(),n.getAction());
			break;
		}
		case Progression:
		case Probability: {
			rew = 0;
			break;
		}

		}
		return rew;
	}

//	@Override
//	public boolean isGoal(DecisionNode n) {
//		return mapmg.isAccState(n.getState());
//		
//	}
//
//	@Override
//	public boolean isDeadend(DecisionNode n) throws PrismException {
//		
//		//a simple deadend is a an avoid state 
//		boolean toret = false; 
//		if(mapmg.isAvoidState(n.getState()))
//			toret = true; 
//		else
//		{
//			//how do we check if its a dead end 
//			//basically if this state leads to itself 
//			mapmg.exploreState(n.getState());
//			int numc = mapmg.getNumChoices(); 
//			if(numc == 0)
//				toret = true; 
//			else
//			{
//				if(numc == 1)
//				{
//					int numt = mapmg.getNumTransitions(0); 
//					if(numt == 1)
//					{
//						State ns = mapmg.computeTransitionTarget(0, 0); 
//						if(ns.compareTo(n.getState())==0)
//							toret = true; 
//					}
//				}
//			}
//		}
//		return toret; 
//	}

}
