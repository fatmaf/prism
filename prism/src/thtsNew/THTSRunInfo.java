package thtsNew;

public class THTSRunInfo {
	boolean goalFound=false; 
	boolean initialStateSolved=false; 
	int numRolloutsTillSolved=-1; 
	boolean goalOnProbablePath=false; 
	
	boolean goalFoundAndSolved()
	{
		return (goalFound & initialStateSolved);
	}

}
