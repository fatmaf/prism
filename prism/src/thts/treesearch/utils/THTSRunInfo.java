package thts.treesearch.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class THTSRunInfo {
    boolean goalFound = false;
    boolean initialStateSolved = false;
    int numRolloutsTillSolved = -1;
    boolean goalOnProbablePath = false;
    HashMap<Objectives, Bounds> initialStateValues = null;
    HashMap<Objectives, Double> viPolGreedyActSel = null;
    HashMap<Objectives, Double> viPolMostVisActSel = null;

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
    HashMap<Long, HashMap<Objectives, Double>> vipolAtIntervals;
    int numRobots;
    int numGoals;
    int fsp;
    int numDoors = 0;
    boolean viTerminatedEarly = false;

    public HashMap<Objectives, Double> getViPolMostVisActSel() {
        return viPolMostVisActSel;
    }

    public void setViPolMostVisActSel(HashMap<Objectives, Double> viPolMostVisActSel) {
        this.viPolMostVisActSel = viPolMostVisActSel;
    }

    public boolean isViTerminatedEarly() {
        return viTerminatedEarly;
    }

    public void setViTerminatedEarly(boolean viTerminatedEarly) {
        this.viTerminatedEarly = viTerminatedEarly;
    }

    public int getNumDoors() {
        return numDoors;
    }

    public void setNumDoors(int numDoors) {
        this.numDoors = numDoors;
    }

    public boolean goalFoundAndSolved() {
        return (goalFound & initialStateSolved);
    }

    public String toString() {
        String toret = "\nNum of Rollouts\t" + numRolloutsTillSolved + "\nGoalFound\t" + goalFound
                + "\nInitialStateSolved\t" + initialStateSolved + "\nGoalOnProbablePath\t" + goalOnProbablePath;
        if (initialStateValues != null) {
            for (Objectives obj : initialStateValues.keySet())
                toret += "\t" + obj + ":" + getBoundsString(obj, "\t");
        }
        if (isViTerminatedEarly())
            toret += "VI Terminated early";
        return toret;
    }

    public String getVIPolGreedyActSelInfo(Objectives obj) {
        String toret = "";
        if (viPolGreedyActSel != null) {
            if (viPolGreedyActSel.containsKey(obj)) {
                toret = viPolGreedyActSel.get(obj).toString();
            }
        }
        return toret;

    }
    public String getVIPolMostVisActSelInfo(Objectives obj) {
        String toret = "";
        if (viPolMostVisActSel != null) {
            if (viPolMostVisActSel.containsKey(obj)) {
                toret = viPolMostVisActSel.get(obj).toString();
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
            for (long ts : vipolAtIntervals.keySet()) {
                toret += ts + ":" + vipolAtIntervals.get(ts).toString() + ",";
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

    public HashMap<Objectives, Double> getViPolGreedyActSel() {
        return viPolGreedyActSel;
    }

    public void setViPolGreedyActSel(HashMap<Objectives, Double> viPolGreedyActSel) {
        this.viPolGreedyActSel = viPolGreedyActSel;
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

    public HashMap<Long, HashMap<Objectives, Double>> getVipolAtIntervals() {
        return vipolAtIntervals;
    }

    public void setVipolAtIntervals(HashMap<Long, HashMap<Objectives, Double>> vipolAtIntervals) {
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
