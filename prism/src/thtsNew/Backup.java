package thtsNew;

public interface Backup {
	// take in a decisionnode
	// take in a chancenode
	// just update them
	// the boolean dobackup is always true // except for lrtdp style backups
	// just return whether a node has been backed up or not
	// again always true unless lrtdp style
	boolean forwardbackupChanceNode(ChanceNode cn, boolean doBackup);

	boolean forwardbackupDecisionNode(DecisionNode dn, boolean doBackup);

	boolean backupChanceNode(ChanceNode cn, boolean doBackup);

	boolean backupDecisionNode(DecisionNode dn, boolean doBackup);
}
