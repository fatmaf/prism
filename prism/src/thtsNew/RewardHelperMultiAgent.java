package thtsNew;


import prism.PrismException;
import thts.Objectives;

public class RewardHelperMultiAgent implements RewardHelper {

	MultiAgentNestedProductModelGenerator mapmg;
	RewardCalculation rewCalc;


	public RewardHelperMultiAgent(MultiAgentNestedProductModelGenerator mapmg, RewardCalculation rewCalc) {
		this.mapmg = mapmg;
		this.rewCalc = rewCalc;

	}

	@Override
	public double getReward(Objectives obj, ChanceNode n) throws PrismException {
		mapmg.exploreState(n.getState());
		double rew=0;

		switch (obj) {
		case TaskCompletion: {
			rew = mapmg.getStateActionTaskReward(n.actionChoice);
			break;
		}
		case Cost: {
			rew = mapmg.getStateActionReward(0,n.getState(),n.getAction(), rewCalc);
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
