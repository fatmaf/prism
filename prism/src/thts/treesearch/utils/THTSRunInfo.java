package thts.treesearch.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class THTSRunInfo {

    HashMap<SolutionTypes, SolutionResults> solutionResults;


    public void addSolutionResults(SolutionTypes st, SolutionResults sr) {
        if (solutionResults == null) {
            solutionResults = new HashMap<>();
        }
        solutionResults.put(st, sr);
    }

    public long getSolutionResultsTimeTaken(SolutionTypes st) {
        long toret = 0;
        if (solutionResults != null) {
            if (solutionResults.containsKey(st)) {
                toret = solutionResults.get(st).getTimeTaken();
            }
        }
        return toret;
    }

    public long getSolutionResultsTimeLimit(SolutionTypes st) {
        long toret = 0;
        if (solutionResults != null) {
            if (solutionResults.containsKey(st)) {
                toret = solutionResults.get(st).getTimeLimit();
            }
        }
        return toret;
    }

    public boolean getSolutionResultsEarlyTerm(SolutionTypes st) {
        boolean toret = false;
        if (solutionResults != null) {
            if (solutionResults.containsKey(st)) {
                toret = solutionResults.get(st).isEarlyTerm();
            }
        }
        return toret;
    }

    public String getSolutionResultsObjSol(SolutionTypes st, Objectives obj) {
        String toret = "";
        if (solutionResults != null) {
            if (solutionResults.containsKey(st)) {
                toret = solutionResults.get(st).getObjVal(obj);
            }
        }
        return toret;
    }
    public String getSolutionResultsActSetName(SolutionTypes st) {
        String toret = "";
        if (solutionResults != null) {
            if (solutionResults.containsKey(st)) {
              toret = solutionResults.get(st).getActSelMethod();
            }
        }
        return toret;
    }

    boolean goalFound = false;
    boolean initialStateSolved = false;
    int numRolloutsTillSolved = -1;
    boolean goalOnProbablePath = false;
    HashMap<Objectives, Bounds> initialStateValues = null;


    boolean stackoverflowerror = false;
    boolean timeLimited = false;
    long maxTimeLimit = -1;
    long duration = -1;
    float averageTrialLen = -1;
    int maxTrialLen = 0;
    int minTrialLen = 0;
    int chanceNodesExp = -1;
    int decisionNodesExp = -1;
    String tLensString = "[]";
    public ArrayList<String> actSelNames;
    ArrayList<HashMap<Long, HashMap<Objectives, Double>>> vipolAtIntervals;
    int numRobots;
    int numGoals;
    int fsp;
    int numDoors = 0;


    public int getNumDoors() {
        return numDoors;
    }

    public void setNumDoors(int numDoors) {
        this.numDoors = numDoors;
    }

    public boolean goalFoundAndSolved() {
        return (goalFound & initialStateSolved);
    }

    @Override
    public String toString() {
        String toret = "\nNum of Rollouts\t" + numRolloutsTillSolved + "\nGoalFound\t" + goalFound
                + "\nInitialStateSolved\t" + initialStateSolved + "\nGoalOnProbablePath\t" + goalOnProbablePath;
        if (initialStateValues != null) {
            for (Objectives obj : initialStateValues.keySet())
                toret += "\t" + obj + ":" + getBoundsString(obj, "\t");
        }
        if(solutionResults!=null)
        {
            for(SolutionTypes st:solutionResults.keySet())
            {
                toret += "\n"+solutionResults.get(st);
            }
        }
        return toret;
    }


    public String getBoundsString(Objectives obj, String sep) {
        String toret = " " + sep + " ";
        if (initialStateValues != null) {
            if (initialStateValues.containsKey(obj)) {
                toret = initialStateValues.get(obj).getUpper() + sep + initialStateValues.get(obj).getLower();
            }
        }
        return toret;
    }

    public void setTrialLenStuff(ArrayList<Integer> tLens) {
        this.minTrialLen = Collections.min(tLens);
        this.maxTrialLen = Collections.max(tLens);
        int sum = 0;
        for (int tLen : tLens)
            sum += tLen;
        this.averageTrialLen = (float) (sum / (float) tLens.size());
        this.tLensString = tLens.toString();
    }

    public String getVIPolIntervalString() {
        String toret = "{}";
        if (this.vipolAtIntervals != null) {
            toret = "{";
            for (int i = 0; i < this.vipolAtIntervals.size(); i++) {
                toret += this.actSelNames.get(i) + ":{";
                for (long ts : vipolAtIntervals.get(i).keySet()) {
                    toret += ts + ":" + vipolAtIntervals.get(i).get(ts).toString() + ",";
                }
                toret += "},";
            }
            toret += "}";
        }
        return toret;
    }

    public boolean isGoalFound() {
        return goalFound;
    }

    public void setGoalFound(boolean goalFound) {
        this.goalFound = goalFound;
    }

    public boolean isInitialStateSolved() {
        return initialStateSolved;
    }

    public void setInitialStateSolved(boolean initialStateSolved) {
        this.initialStateSolved = initialStateSolved;
    }

    public int getNumRolloutsTillSolved() {
        return numRolloutsTillSolved;
    }

    public void setNumRolloutsTillSolved(int numRolloutsTillSolved) {
        this.numRolloutsTillSolved = numRolloutsTillSolved;
    }

    public boolean isGoalOnProbablePath() {
        return goalOnProbablePath;
    }

    public void setGoalOnProbablePath(boolean goalOnProbablePath) {
        this.goalOnProbablePath = goalOnProbablePath;
    }

    public HashMap<Objectives, Bounds> getInitialStateValues() {
        return initialStateValues;
    }

    public void setInitialStateValues(HashMap<Objectives, Bounds> initialStateValues) {
        this.initialStateValues = initialStateValues;
    }


    public boolean isStackoverflowerror() {
        return stackoverflowerror;
    }

    public void setStackoverflowerror(boolean stackoverflowerror) {
        this.stackoverflowerror = stackoverflowerror;
    }

    public boolean isTimeLimited() {
        return timeLimited;
    }

    public void setTimeLimited(boolean timeLimited) {
        this.timeLimited = timeLimited;
    }

    public long getMaxTimeLimit() {
        return maxTimeLimit;
    }

    public void setMaxTimeLimit(long maxTimeLimit) {
        this.maxTimeLimit = maxTimeLimit;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public float getAverageTrialLen() {
        return averageTrialLen;
    }

    public void setAverageTrialLen(float averageTrialLen) {
        this.averageTrialLen = averageTrialLen;
    }

    public int getMaxTrialLen() {
        return maxTrialLen;
    }

    public void setMaxTrialLen(int maxTrialLen) {
        this.maxTrialLen = maxTrialLen;
    }

    public int getMinTrialLen() {
        return minTrialLen;
    }

    public void setMinTrialLen(int minTrialLen) {
        this.minTrialLen = minTrialLen;
    }

    public int getChanceNodesExp() {
        return chanceNodesExp;
    }

    public void setChanceNodesExp(int chanceNodesExp) {
        this.chanceNodesExp = chanceNodesExp;
    }

    public int getDecisionNodesExp() {
        return decisionNodesExp;
    }

    public void setDecisionNodesExp(int decisionNodesExp) {
        this.decisionNodesExp = decisionNodesExp;
    }

    public String gettLensString() {
        return tLensString;
    }

    public void settLensString(String tLensString) {
        this.tLensString = tLensString;
    }

    public ArrayList<HashMap<Long, HashMap<Objectives, Double>>> getVipolAtIntervals() {
        return vipolAtIntervals;
    }

    public void setVipolAtIntervals(ArrayList<HashMap<Long, HashMap<Objectives, Double>>> vipolAtIntervals) {
        this.vipolAtIntervals = vipolAtIntervals;
    }

    public int getNumRobots() {
        return numRobots;
    }

    public void setNumRobots(int numRobots) {
        this.numRobots = numRobots;
    }

    public int getNumGoals() {
        return numGoals;
    }

    public void setNumGoals(int numGoals) {
        this.numGoals = numGoals;
    }

    public int getFsp() {
        return fsp;
    }

    public void setFsp(int fsp) {
        this.fsp = fsp;
    }
}
