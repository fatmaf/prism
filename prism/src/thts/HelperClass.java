package thts;

import java.util.ArrayList;
import java.util.List;


public class HelperClass<T> {
	public int getNumCombsFromSizes(ArrayList<Integer> numElements)
	{
		int numcombs = 1; 
		for(int r = 0; r<numElements.size(); r++)
			numcombs*=numElements.get(r); 
		return numcombs; 
	}
	public int getNumCombs(ArrayList<List<T>> robotStates)
	{
	
		int numcombs = 1;

		for (int r = 0; r < robotStates.size(); r++) {
	
			numcombs *= robotStates.get(r).size();
		}
		return numcombs; 
	}
	public ArrayList<ArrayList<T>> generateCombinations(ArrayList<List<T>> robotStates) {
		// so lets get the number of states for each robot
		int[] numStates = new int[robotStates.size()];
		int[] currStateNum = new int[robotStates.size()];
		int numcombs = 1;
		int lastrobotnum = robotStates.size() - 1;
		for (int r = 0; r < robotStates.size(); r++) {
			List<T> rs = robotStates.get(r);
			numStates[r] = rs.size();

			currStateNum[r] = 0;
			numcombs *= rs.size();
		}
		ArrayList<ArrayList<T>> combs = new ArrayList<>();
		boolean docomb = true;
		while (docomb) {
			// so now we just loop over things
			// its a lot of while loops
			ArrayList<T> currcomb = new ArrayList<>();
			for (int r = 0; r < robotStates.size(); r++) {
				T rs = robotStates.get(r).get(currStateNum[r]);
				currcomb.add(rs);
			}
			combs.add(currcomb);

			boolean doInc = true;
			for (int lr = lastrobotnum; lr >= 0; lr--) {
				if (currStateNum[lr] + 1 == numStates[lr]) {
					currStateNum[lr] = 0;
				} else {
					currStateNum[lr]++;
					doInc = false;
				}
				if (!doInc) {
					break;
				}
			}
			int indsum = 0;
			for (int r = 0; r < numStates.length; r++) {
				indsum += currStateNum[r];
			}
			if (indsum == 0)
				docomb = false;
		}

		return combs;
	}
}
