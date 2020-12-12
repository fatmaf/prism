package thts.backup;

import thts.treesearch.ChanceNode;
import thts.treesearch.DecisionNode;

public interface Backup {
	// take in a decisionnode
	// take in a chancenode
	// just update them
	// the boolean dobackup is always true // except for lrtdp style backups
	// just return whether a node has been backed up or not
	// again always true unless lrtdp style
	boolean forwardbackupChanceNode(ChanceNode cn) throws Exception;

	boolean forwardbackupDecisionNode(DecisionNode dn) throws Exception;

	boolean backupChanceNode(ChanceNode cn, boolean doBackup) throws Exception;

	boolean backupDecisionNode(DecisionNode dn, boolean doBackup) throws Exception;
}
