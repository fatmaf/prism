package thts.scratch;

import prism.DefaultModelGenerator;
import prism.PrismException;
import thts.treesearch.utils.Objectives;
import thts.treesearch.utils.ChanceNode;
import thts.treesearch.rewardhelper.RewardHelper;

//cuz i'm too lazy
public class RewardHelperGSSPPaper implements RewardHelper {
	DefaultModelGenerator mg;

	public RewardHelperGSSPPaper(DefaultModelGenerator mapmg) {
		this.mg = mapmg;

	}

	@Override
	public double getReward(Objectives obj, ChanceNode n) throws PrismException {
		// TODO Auto-generated method stub
		double rew = 0.0;
		if (obj == Objectives.Cost) {
		//	rew = mg.getStateActionReward(0, n.getState(), n.getAction());
			String actS = n.action.toString();
			if (actS.contentEquals("v0_v1"))
				rew = 0.5; 
			else if (actS.contentEquals("v0_v3"))
				rew = 2; 
			else if (actS.contentEquals("v4_v5"))
				rew = 1; 

		}
		return rew;
	}

}
