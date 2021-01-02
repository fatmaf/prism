package thts.treesearch.utils;

import java.util.HashMap;

public  class SolutionResults{
    String actSelMethod;
    boolean earlyTerm = false;
    HashMap<Objectives, Double> valuesForInitialState;
    String initialState;
    long timeTaken;
    public static long timeLimit=0;

    public String getObjVal(Objectives obj)
    {
        String toret = "";
        if(valuesForInitialState!=null)
        {
            if(valuesForInitialState.containsKey(obj))
                toret = valuesForInitialState.get(obj).toString();
        }
        return toret;
    }
    public void addResults(HashMap<Objectives, Double> initialStateValues)
    {
        valuesForInitialState = initialStateValues;
    }
    public void setInitialState(String state)
    {
        initialState = state;
    }
    public void setActSelMethod(String actSelMethod)
    {
        this.actSelMethod = actSelMethod;
    }
    public void setEarlyTerm(boolean earlyTerm)
    {
        this.earlyTerm = earlyTerm;
    }
    public void setTimeTaken(long timeTaken)
    {
        this.timeTaken = timeTaken;
    }

    public long getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(long timeLimit) {
        this.timeLimit = timeLimit;
    }

    public String getActSelMethod() {
        return actSelMethod;
    }

    public boolean isEarlyTerm() {
        return earlyTerm;
    }

    public HashMap<Objectives, Double> getValuesForInitialState() {
        return valuesForInitialState;
    }

    public String getInitialState() {
        return initialState;
    }

    public long getTimeTaken() {
        return timeTaken;
    }

    @Override
    public String toString() {
        return "SolutionResults{" +
                "actSelMethod='" + actSelMethod + '\'' +
                ", earlyTerm=" + earlyTerm +
                ", valuesForInitialState=" + valuesForInitialState +
                ", initialState='" + initialState + '\'' +
                ", timeTaken=" + timeTaken +
                '}';
    }
}
