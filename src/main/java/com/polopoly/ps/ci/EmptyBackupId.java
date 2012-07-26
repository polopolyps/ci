package com.polopoly.ps.ci;

import java.io.File;

import com.polopoly.ps.ci.tool.BackupId;

public class EmptyBackupId extends BackupId {
	
	public void backup(File directory) {
		// doesn't back up.
	}

}
