package thtsNew;

import java.util.ArrayList;

import prism.PrismDevNullLog;
import prism.PrismFileLog;
import prism.PrismLog;
import thts.Objectives;

//a class used to save information so we can visualise it 

public class VisualiserLog {
	PrismLog mainLog;
	ArrayList<Objectives> boundsOrder;
	String currentStep;
	String currentRollout;

	public enum THTSStep {
		visitDecisionNode, ActionSelection, visitChanceNode, outcomeSelection
	}

	THTSStep curr = THTSStep.visitDecisionNode;

	public VisualiserLog(String logName, ArrayList<Objectives> boundsOrder) {
		mainLog = new PrismFileLog(logName);
		this.boundsOrder = boundsOrder;
	}

	public VisualiserLog(ArrayList<Objectives> boundsOrder, boolean donull) {
		if (donull)
			mainLog = new PrismDevNullLog();
		else
			mainLog = new PrismFileLog("stdout");
		this.boundsOrder = boundsOrder;
	}
	// each bit has a step
	// we want to save the following
	// the THTS exploration tree
	// along with the bounds and their updates
	// each trial is a tree with no branches so that's easy to do really
	// we save a list of state , state action pairs
	// for each state we have bounds and for each state action pair we have bounds
	// the process is state, action sel, action, outcome sel, outcome and repeat
	// we've got to save this really
	// for action sel and outcome sel we've got to save all the nodes and their
	// bounds
	// so a json thing would look like an array of size numt

	public String createKeyValueString(String key, Object value) {
		return "'" + key + "':" + value.toString();
	}

	public void newRollout(int rNum) {
		currentRollout = "[beginRollout]{" + createKeyValueString("rollout", rNum);
	}

	public void endRollout() {
		currentRollout += currentStep + "}[endRollout]\n";
		mainLog.println(currentRollout);
		currentRollout = "";
	}

	public void newStep(int trialNum) {
		currentStep = "[beginStep]{" + createKeyValueString("step", trialNum);

	}

	public void endStep() {
		currentStep += "}[endStep]\n";
		currentRollout += currentStep;
		currentStep = "";
	}

	public void addStateBit(DecisionNode d) {
		currentStep += "state:{" + decisionNodeString(d) + "}";
	}

	public void beginActionSelection() {
		currentStep += "[beginActSel]{";

	}

	public void endActionSelectin() {
		currentStep += "}[endActSel]";
	}

	public void writeSelectedAction(ChanceNode d) {
		currentStep += "selected:{" + chanceNodeString(d) + "}";
	}

	public void writeActSelChoices(DecisionNode d) {
		currentStep += "node:{" + decisionNodeString(d) + "}";
		currentStep += "children:{";
		if (d.getChildren() != null) {

			boolean addSep = false;
			for (Object a : d.getChildren().keySet()) {
				if (addSep)
					currentStep += ",";
				if (d.getChild(a) != null) {
					currentStep += "{" + chanceNodeString(d.getChild(a)) + "}";
					addSep = true;
				}
			}
		}
		currentStep += "}";
	}

	public String decisionNodeString(DecisionNode d) {
		String towrite = "state:";
		towrite += d.getState();
		towrite += " bounds:" + getBoundsString(d);
		return towrite;

	}

	public String chanceNodeString(ChanceNode d) {
		String towrite = "state:";
		towrite += d.getState();
		towrite += " action:";

		towrite += d.getAction() != null ? d.getAction().toString() : "null";
		towrite += " bounds:" + getBoundsString(d);
		return towrite;

	}

	public String getBoundsString(Node d) {
		String toret = "{";
		boolean addcomma = false;
		if (d.boundsInitialised()) {
			for (Objectives obj : boundsOrder) {
				if (addcomma)
					toret += ",";
				toret += obj.name() + ":" + d.getBounds(obj);
				addcomma = true;
			}
		}
		return toret + "}";
	}

	public void closeLog() {
		this.mainLog.close();
	}
}
