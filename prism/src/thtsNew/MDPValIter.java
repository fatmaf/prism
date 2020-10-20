package thtsNew;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;

import explicit.DTMCFromMDPMemorylessAdversary;
import explicit.MDP;
import explicit.MDPModelChecker;
import explicit.ProbModelChecker.TermCrit;
import explicit.rewards.MDPRewardsSimple;
import prism.PrismException;
import prism.PrismFileLog;
import prism.PrismLog;
import prism.PrismUtils;
import strat.MDStrategyArray;

public class MDPValIter {

	/**
	 * Class storing some info/data from a call to a model checking or numerical
	 * computation method in the explicit engine.
	 */
	public class ModelCheckerMultipleResult {

		// Solution vectors
		public ArrayList<double[]> solns = null;
		// Solution vectors from previous iteration
		public ArrayList<double[]> lastSolns = null;
		// Iterations performed
		public int numIters = 0;
		// Total time taken (secs)
		public double timeTaken = 0.0;
		// Time taken for any precomputation (secs)
		public double timePre = 0.0;
		// Time taken for Prob0-type precomputation (secs)
		public double timeProb0 = 0.0;
		// Strategy
		public MDStrategyArray strat = null;

		/**
		 * Clear all stored data, including setting of array pointers to null (which may
		 * be helpful for garbage collection purposes).
		 */
		public void clear() {
			solns = lastSolns = null;
			numIters = 0;
			timeTaken = timePre = timeProb0 = 0.0;
		}

	}

	/**
	 * Compute reachability probabilities using value iteration - arrays Optionally,
	 * store optimal (memoryless) strategy info.
	 * 
	 * @param mdp          The MDP
	 * @param target
	 * 
	 * @param remain
	 * 
	 * @param rewards      Arraylist of rewards in order of preference
	 * 
	 * @param minRewards   Arraylist of booleans corresponding to the rewards above,
	 *                     true = minimize, false = maximise
	 * 
	 * @param probPriority the priority of the probability (0 = highest,
	 *                     rewards.size() = lowest)
	 */
	public ModelCheckerMultipleResult computeNestedValIterArray(MDPModelChecker mc, MDP mdp, BitSet target,
			BitSet remain, ArrayList<MDPRewardsSimple> rewards, ArrayList<double[]> rewardsInitVal,
			ArrayList<Boolean> minRewards, BitSet statesToIgnoreForVI, int probPreference, double[] probInitVal,
			PrismLog mainLog, String resLoc, String name) throws PrismException {
		ModelCheckerMultipleResult res;
		int i, n, iters, numYes, numNo;
		double solnProb[];
		ArrayList<double[]> solnReward;
		boolean done;
		BitSet no, yes, unknown;
		long timerVI, timerGlobal;
		int strat[] = null;
		boolean min = false;
		//for debugging 
		//an array list of values for the soln 

		int numRewards = rewards.size();
		TermCrit termCrit = mc.getTermCrit();

		timerGlobal = System.currentTimeMillis();

		// Check for deadlocks in non-target state (because breaks e.g. prob1)
		mdp.checkForDeadlocks(target);

		// Store num states
		n = mdp.getNumStates();

		// If required, create/initialise strategy storage
		// Set choices to -1, denoting unknown
		// (except for target states, which are -2, denoting arbitrary)
		strat = new int[n];

		for (i = 0; i < n; i++) {
			strat[i] = target.get(i) ? -2 : -1;
		
		}

		// skipping precomputation

		no = new BitSet();
		if (remain != null) {
			no = (BitSet) remain.clone();
			no.flip(0, n);
		}

		yes = (BitSet) target.clone();

		// Print results of precomputation
		numYes = yes.cardinality();
		numNo = no.cardinality();
		mainLog.println("target=" + target.cardinality() + ", yes=" + numYes + ", no=" + numNo + ", maybe="
				+ (n - (numYes + numNo)));

		// If still required, store strategy for no/yes (0/1) states.
		// This is just for the cases max=0 and min=1, where arbitrary choices
		// suffice (denoted by -2)
		if (min) {
			for (i = yes.nextSetBit(0); i >= 0; i = yes.nextSetBit(i + 1)) {
				if (!target.get(i))
					strat[i] = -2;
			}
		} else {
			for (i = no.nextSetBit(0); i >= 0; i = no.nextSetBit(i + 1)) {
				strat[i] = -2;
			}
		}

		// Start value iteration
		timerVI = System.currentTimeMillis();
		mainLog.println("Starting prioritised value iteration (" + (min ? "min" : "max") + ")...");

		// Create solution vector(s)
		solnProb = new double[n];
		// soln2Prog = new double[n];
		solnReward = new ArrayList<double[]>();
		for (int rew = 0; rew < numRewards; rew++) {
			if (rewardsInitVal == null || rewardsInitVal.get(rew) == null)
			{	
				solnReward.add(new double[n]);
		
			}
			else
				solnReward.add(rewardsInitVal.get(rew).clone());
		}


		// Initialise solution vectors to initVal
		// where initVal is 0.0 or 1.0, depending on whether we converge from
		// below/above.

		// Determine set of states actually need to compute values for
		unknown = new BitSet();
		unknown.set(0, n);
		unknown.andNot(yes);
		unknown.andNot(no);
		for (i = 0; i < n; i++) {

			if (probInitVal == null)
				solnProb[i] = target.get(i) ? 1.0 : 0.0;
			else
				solnProb[i] = probInitVal[i];
			for (int rew = 0; rew < numRewards; rew++) {
				if (minRewards.get(rew)) {
					solnReward.get(rew)[i] = 0.0;
				} else
					solnReward.get(rew)[i] = 0.0;
			}
		}

		// Start iterations
		iters = 0;
		done = false;

		int j;
		int numChoices;
		double currentProbVal;
		ArrayList<Double> currentCostVal = new ArrayList<Double>();
		double currentCost;


		if (statesToIgnoreForVI == null) // set it to unknown
		{
			statesToIgnoreForVI = (BitSet) unknown.clone();
			statesToIgnoreForVI.flip(0, unknown.size());
		}

		double epsilon = mc.getTermCritParam();

		boolean doBigDecimal = false; 
		while (!done && iters < mc.getMaxIters()) {

			iters++;
			done = true;

//			if(iters>10)
				doBigDecimal = true; 

			for (i = 0; i < n; i++) {

				if (!statesToIgnoreForVI.get(i)) {

					numChoices = mdp.getNumChoices(i);

					for (j = 0; j < numChoices; j++) {
//						mainLog.println(iters);

						// for each reward
						// get the current value
						currentProbVal = mdp.mvMultJacSingle(i, j, solnProb);
						
						// get all rew vals
						for (int rew = 0; rew < numRewards; rew++) {
							if (rewards.get(rew) == null) {
								mainLog.println("Reward null!!!" + rew);
							}
							currentCost = mdp.mvMultRewSingle(i, j, solnReward.get(rew), rewards.get(rew));
							if (currentCostVal.size() > rew)
								currentCostVal.set(rew, currentCost);
							else
								currentCostVal.add(currentCost);
						}
					
						boolean updateVals = false;
						for(int rew=0; rew<numRewards;rew++)
						{
							int comparedVals = 
									compareDoubles(currentCostVal.get(rew),solnReward.get(rew)[i],minRewards.get(rew),epsilon,termCrit,doBigDecimal);
							//if isbetter set update to true 
							if(comparedVals > 0)
							{
								updateVals = true;
								break; 
							}
							else
							{
								if(comparedVals != 0)
									break;
							}
						}
						
						if(updateVals)
						{
							String prevVals  = null; 
							if(iters>2000)
							{
								prevVals=iters+"-"+i+":"+strat[i]+"["+solnProb[i]; 
								for (int rews = 0; rews < numRewards; rews++) {
									prevVals+=", "+solnReward.get(rews)[i];

								}
								prevVals+="]";
							}

							
							solnProb[i] = currentProbVal;
							for (int rews = 0; rews < numRewards; rews++) {

								solnReward.get(rews)[i] = currentCostVal.get(rews);
							}

							strat[i] = j;
							done = false;
		
							
							if(iters>2000)
							{
								prevVals+="->"+strat[i]+"["+solnProb[i]; 
								for (int rews = 0; rews < numRewards; rews++) {
									prevVals+=", "+solnReward.get(rews)[i];

								}
								prevVals+="]";
								mainLog.println(prevVals);
							}
						}
						

					}

				}
			}
			
				

		}

		// Finished value iteration
		timerVI = System.currentTimeMillis() - timerVI;
		mainLog.print("Prioritised value iteration (" + (min ? "min" : "max") + ")");
		mainLog.println(" took " + iters + " iterations and " + timerVI / 1000.0 + " seconds.");

		timerGlobal = System.currentTimeMillis() - timerGlobal;
		mainLog.println("Overall policy calculation took  " + timerGlobal / 1000.0 + " seconds.");

		// Non-convergence is an error (usually)
		if (!done && mc.geterrorOnNonConverge())

		{
			String msg = "Iterative method did not converge within " + iters + " iterations.";
			msg += "\nConsider using a different numerical method or increasing the maximum number of iterations";
			throw new PrismException(msg);
		}

		res = new ModelCheckerMultipleResult();
		// Store strategy
		res.strat = new MDStrategyArray(mdp, strat);


		solnReward.add(0, solnProb.clone());
		res.solns = solnReward;

		res.numIters = iters;
		res.timeTaken = timerGlobal / 1000.0;
		return res;
	}

	//compares two doubles upto a certain precision 
	//returns 0 if same 
	//returns 1 if d2 is better than d1 
	//returns -1 if d2 is worse 
	
	private int compareDoubles(double d1, double d2, boolean smallerIsBetter,double precision,TermCrit termCrit,boolean doBigD) {
		
		int toret = 0; 
		if(doBigD)
		{
//			double decimalPlaces = Math.log10(precision);
			BigDecimal bd1 = new BigDecimal(d1); 
			BigDecimal bd2 = new BigDecimal(d2); 
			BigDecimal sub = bd2.subtract(bd1); 
			if(sub.abs().compareTo(new BigDecimal(precision))>0)
			{
				toret = -1; 
				if(smallerIsBetter)
				{
					if(sub.compareTo(new BigDecimal(0))>0)
						toret = 1; 
				}
				else {
					if(sub.compareTo(new BigDecimal(0))<0)
						toret=1;
				}
			}
			return toret; 
		}
		else{
		boolean sameCostVal = PrismUtils.doublesAreClose(d1,
				d2, precision , termCrit == TermCrit.ABSOLUTE);
		if(!sameCostVal)
		{
			toret = -1; 
			if(smallerIsBetter)
			{
				if(d2>d1)
					toret = 1; 
			}
			else
			{
				if(d2<d1)
					toret = 1; 
			}
		}
		return toret;
		}
	}

}
