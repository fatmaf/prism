package thts.treesearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import parser.State;
import thts.old.Bounds;

public abstract class Node {

	public State s;
	public HashMap<Objectives, Bounds> bounds;
//	protected HashMap<Integer, Bounds> rewsBounds;
	public List<Node> parents;
	public int numVisits;
	public boolean solved;
	public boolean boundsInitialised;

	public abstract boolean equals(Node n);

	public void addParent(Node n) {
		if (parents == null)
			parents = new ArrayList<Node>();
		if (!parents.contains(n))
			parents.add(n);
	}

	public boolean boundsInitialised() {
		return boundsInitialised;
	}

	public boolean isSolved() {
		return solved;
	}

	public void setSolved() {
		solved = true;
	}

	public void setUnsolved() {
		solved = false;
	}

	public int visited() {
		return numVisits;
	}

	public int increaseVisits() {
		numVisits++;
		return numVisits;
	}

	public State getState() {
		return s;
	}

	public void setState(State s) {
		this.s = s;
	}

	public Bounds getBounds(Objectives obj) {

		return bounds.get(obj);

	}

	public void setBounds(Objectives obj, Bounds b) {
		if (bounds == null) {
			bounds = new HashMap<>();
			boundsInitialised = true;
		}
		bounds.put(obj, b);

	}

	public boolean hasBounds() {
		return (bounds != null);
	}

	public void setBounds(HashMap<Objectives, Bounds> b) {
		this.bounds = b;
	}

//	public HashMap<Integer, Bounds> getRews() {
//		return rewsBounds;
//	}
//
//	public void setRews(HashMap<Integer, Bounds> rews) {
//		this.rewsBounds = rews;
//	}

//	public void setRews() {
//		initRews();
//	}

//	public Bounds getRew(int rewNum) {
//		return rewsBounds.get(rewNum);
//	}

//	public void setRew(Bounds b, int rewNum) {
//		if (rewsBounds == null)
//			setRews();
//		rewsBounds.put(rewNum, b);
//	}

//	public void initRews() {
//		rewsBounds = new HashMap<Integer, Bounds>();
//	}
//
//	public int getNumRews() {
//		if (rewsBounds == null)
//			return 0;
//		else
//			return rewsBounds.size();
//	}

	public abstract NodeType nodeType();

	public abstract boolean isLeafNode();

	public abstract String getShortName();

	@Override
	public String toString() {
		String str = "N{" + getPartialString();

		str += "}";

		return str;
	}

	public String getBoundsString()
	{
		String str="";
		if (bounds != null) {
			for (Objectives obj : bounds.keySet()) {
				str += obj.toString() + bounds.get(obj) + ", ";
			}
		}
		return str; 
	}
	public String getPartialString() {
		String str = "s:" + s + ", numVisits: " + numVisits + " , solved:" + solved + " ";
		str+=getBoundsString();
		if (parents == null || parents.size() == 0) {
			str += ", abus:[]";
		} else {
			str += ", abus:[";
			for (Node abu : parents) {
				if (abu != null) {
					str += abu.getState() + ",";
				}
			}
			str += "]";
		}

		return str;
	}

}
