package thts.scratch;

import java.util.HashMap;

import thts.treesearch.utils.Bounds;
import thts.treesearch.utils.Objectives;

public class BaseActionInfo {
	public int actionOccurrences;
	public HashMap<Objectives, Bounds> bounds;

	public BaseActionInfo(HashMap<Objectives,Bounds> b) {
		bounds = b; 
		actionOccurrences = 1; 
	}
	public void increaseOccurrence()
	{
		actionOccurrences++;
	}
	public void divideBoundsByOccurence()
	{
		for(Objectives obj: bounds.keySet())
		{
			bounds.put(obj, bounds.get(obj).divide(actionOccurrences));
		
		}
	}
}