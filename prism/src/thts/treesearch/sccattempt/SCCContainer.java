package thts.sccattempt;

import java.util.ArrayList;

import thts.treesearch.Node;

//each scc container has an id 
//and a list of nodes 
//and a type
public class SCCContainer {

	public enum SCCContainerType {
		Transient,Permanent
	}
	public int id; 
	public ArrayList<Node> nodes; 
	public SCCContainerType type; 
}
