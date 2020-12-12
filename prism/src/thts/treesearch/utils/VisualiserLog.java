package thts.treesearch.utils;

import java.util.ArrayList;

import prism.PrismDevNullLog;
import prism.PrismFileLog;
import prism.PrismLog;

//a class used to save information so we can visualise it 

public class VisualiserLog {
	PrismLog mainLog;
	ArrayList<Objectives> boundsOrder;
	String currentStep;
	String currentRollout;
	boolean donull = false;

	public enum THTSStep {
		visitDecisionNode, ActSel, visitChanceNode, OutSel, Heuristic, PolRun
	}

	THTSStep curr = THTSStep.visitDecisionNode;

	public VisualiserLog(String logName, ArrayList<Objectives> boundsOrder) {
		mainLog = new PrismFileLog(logName);
		this.boundsOrder = boundsOrder;

	}

	public VisualiserLog(String logName, ArrayList<Objectives> boundsOrder, boolean append) {
		mainLog = new PrismFileLog(logName, append);
		this.boundsOrder = boundsOrder;

	}

	public VisualiserLog(ArrayList<Objectives> boundsOrder, boolean donull) {
		this.donull = donull;
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
		if (!donull) {
			currentRollout = "[beginRollout]{" + createKeyValueString("rollout", rNum) + "\n";
		}
	}

	public void endRollout() {
		if (!donull) {
			currentRollout += currentStep + "}[endRollout]\n\n\n";
			mainLog.println(currentRollout);
			currentRollout = "";
		}
	}

	public void newStep(int trialNum) {
		if (!donull) {
			currentStep = "\n\t[beginStep]{\n" + createKeyValueString("step", trialNum);
		}
	}

	public void endStep() {
		if (!donull) {
			currentStep += "}[endStep]\n";
			currentRollout += currentStep;
			currentStep = "";
		}
	}

	public void addStateBit(DecisionNode d) {
		if (!donull) {
			currentStep += "\t\tstate:{" + decisionNodeString(d) + "}\n";
		}
	}

	private void begin(THTSStep t) {
		if (!donull)
			currentStep += "\n\t\t[begin" + t.toString() + "]{";
	}

	public void beginPolRun() {
		begin(THTSStep.PolRun);
	}

	public void endPolRun() {
		end(THTSStep.PolRun);
	}

	public void beginOutcomeSelection() {
		begin(THTSStep.OutSel);
//		if(!donull) {
//		currentStep += "\n\t\t[beginOutSel]{";
//		}
	}

	public void beginActionSelection() {
//		if(!donull) {
//		currentStep += "\n\t\t[beginActSel]{";}
		begin(THTSStep.ActSel);

	}

	public void beginHeuristicAssignment() {
		begin(THTSStep.Heuristic);
	}

	private void end(THTSStep t) {
		if (!donull) {
			currentStep += "}[end" + t.toString() + "]\n";
		}
	}

	public void endActionSelectin() {
//		if(!donull) {
//		currentStep += "}[endActSel]\n";}
		end(THTSStep.ActSel);
	}

	public void endOutcomeSelection() {
//		if(!donull) {
//		currentStep += "}[endOutSel]\n";}
		end(THTSStep.OutSel);
	}

	public void endHeuristicAssignment() {
		end(THTSStep.Heuristic);
	}

	public void writeAssignedHeuristic(Node d) {
		if (d instanceof DecisionNode)
			writeAssignedHeuristic((DecisionNode) d);
		else if (d instanceof ChanceNode)
			writeAssignedHeuristic((ChanceNode) d);
	}

	private void writeAssignedHeuristic(ChanceNode d) {
		if (!donull) {
			currentStep += "\n\t\t" + chanceNodeString(d);
		}
	}

	private void writeAssignedHeuristic(DecisionNode d) {
		if (!donull) {
			currentStep += "\n\t\t" + decisionNodeString(d);
		}
	}

	public void writeSelectedAction(ChanceNode d) {
		if (!donull) {
			currentStep += "\n\t\tselected:{" + chanceNodeString(d) + "}";
		}
	}

	public void writeSelectedOutcome(ArrayList<DecisionNode> ds) {
		if (!donull) {
			currentStep += "\n\t\tselected:{";
			boolean hasmore = false;
			for (DecisionNode d : ds) {
				if (hasmore)
					currentStep += "\n\t\t";
				if (d != null) {
					currentStep += decisionNodeString(d);
					hasmore = true;
				}
			}
			currentStep += "}";
		}
	}

	public void writeoutSelChoices(ChanceNode d) {
		if (!donull) {
			currentStep += "\tnode:{" + chanceNodeString(d) + "}";
			currentStep += "\n\t\t\tchildren:{\n";
			if (d.getChildren() != null) {

				boolean addSep = false;
				for (DecisionNode dn : d.getChildren()) {
					if (addSep)
						currentStep += "\n";

					currentStep += "\t\t\t{" + decisionNodeString(dn) + "}";
					addSep = true;

				}
			}
			currentStep += "}\n";
		}
	}

	public void writeActSelChoices(DecisionNode d) {
		if (!donull) {
			currentStep += "\tnode:{" + decisionNodeString(d) + "}";
			currentStep += "\n\t\t\tchildren:{\n";
			if (d.getChildren() != null) {

				boolean addSep = false;
				for (Object a : d.getChildren().keySet()) {
					if (addSep)
						currentStep += "\n";
					if (d.getChild(a) != null) {
						currentStep += "\t\t\t{" + chanceNodeString(d.getChild(a)) + "}";
						addSep = true;
					}
				}
			}
			currentStep += "}\n";
		}

	}

	public String decisionNodeString(DecisionNode d) {
		if (!donull) {
			String towrite = "state:";
			towrite += d.getState();
			towrite += " bounds:" + getBoundsString(d);
			if (d.isSolved())
				towrite += " solved ";
			return towrite;
		}
		return "";

	}

	public String chanceNodeString(ChanceNode d) {
		if (!donull) {
			String towrite = "state:";
			if (d != null) {
				towrite += d.getState();
				towrite += " action:";

				towrite += d.getAction() != null ? d.getAction().toString() : "null";
				towrite += " bounds:" + getBoundsString(d);
				if (d.isSolved())
					towrite += " solved ";
			}
			return towrite;
		}
		return "";

	}

	public String getBoundsString(Node d) {
		if (!donull) {
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
		return "";
	}

	public void closeLog() {

		this.mainLog.close();
	}
}
