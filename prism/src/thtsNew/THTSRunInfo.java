package thtsNew;

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

}
