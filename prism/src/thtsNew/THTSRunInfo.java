package thtsNew;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import thts.Bounds;
import thts.Objectives;

public class THTSRunInfo {
	public boolean goalFound=false; 
	public boolean initialStateSolved=false; 
	public int numRolloutsTillSolved=-1; 
	public boolean goalOnProbablePath=false; 
	public HashMap<Objectives,Bounds> initialStateValues=null;
	public HashMap<Objectives,Double> vipol = null; 
	public boolean stackoverflowerror = false; 
	public boolean timeLimited = false; 
	public long maxTimeLimit = -1; 
	public long duration = -1; 
	public double averageTrialLen = -1; 
	public int maxTrialLen = 0; 
	public int minTrialLen = 0; 
	public int chanceNodesExp = -1; 
	public int decisionNodesExp = -1; 
	
	boolean goalFoundAndSolved()
	{
		return (goalFound & initialStateSolved);
	}
	public String toString()
	{
		String toret = "\nNum of Rollouts\t"+numRolloutsTillSolved+"\nGoalFound\t"+goalFound+
				"\nInitialStateSolved\t"+initialStateSolved+"\nGoalOnProbablePath\t"+goalOnProbablePath;
		if(initialStateValues!=null) {
		for(Objectives obj:initialStateValues.keySet())
		toret+="\t"+obj+":"+getBoundsString(obj,"\t");
		}
		return toret; 
	}
	public String getviInfo(Objectives obj)
	{
		String toret = ""; 
		if(vipol!=null)
		{
			if(vipol.containsKey(obj))
			{
				toret = vipol.get(obj).toString();
			}
		}
		return toret; 
		
	}
	public String getBoundsString(Objectives obj,String sep)
	{
		String toret = " "+sep+" ";
		if(initialStateValues!=null)
		{
			if(initialStateValues.containsKey(obj))
			{
				toret=initialStateValues.get(obj).getUpper()+sep+initialStateValues.get(obj).getLower();
			}
		}
		return toret; 
	}
	public void setTrialLenStuff(ArrayList<Integer> tLens)
	{
		this.minTrialLen = Collections.min(tLens);
		this.maxTrialLen = Collections.max(tLens  );
	}

}
