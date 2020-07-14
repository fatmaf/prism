package thtsNew;

import prism.DefaultModelGenerator;
import prism.PrismException;
import thts.Objectives;

public class RewardHelperMDP implements RewardHelper {
	DefaultModelGenerator mg;

	public RewardHelperMDP(DefaultModelGenerator mapmg) {
		this.mg = mapmg;

	}

	@Override
	public double getReward(Objectives obj, ChanceNode n) throws PrismException {
		// TODO Auto-generated method stub
		double rew = 0.0;
		if (obj == Objectives.Cost) {
		//	rew = mg.getStateActionReward(0, n.getState(), n.getAction());
			rew = 1.0;

		}
		return rew;
	}

}
