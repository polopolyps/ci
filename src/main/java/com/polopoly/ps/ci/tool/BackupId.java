package com.polopoly.ps.ci.tool;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.polopoly.ps.ci.CopyUtil;
import com.polopoly.ps.ci.Executor;
import com.polopoly.ps.ci.Host;
import com.polopoly.ps.ci.MoveUtil;
import com.polopoly.ps.ci.configuration.Configuration;

public class BackupId {
	private static final String FORMAT_STRING = "yyyyMMddHHmmss";
	private static final DateFormat FORMAT = new SimpleDateFormat(FORMAT_STRING);
	private String id;

	public BackupId() {
		this.id = FORMAT.format(new Date());
	}
	
	@Override
	public String toString() {
		return id;
	}

	public void backupFile(File fileOrDir) {
		backupFile(fileOrDir, new Host(), false);
	}
	
	public void backupFile(File fileOrDir, boolean deleteOriginal) {
		backupFile(fileOrDir, new Host(), deleteOriginal);
	}

	public void backupDir(File fileOrDir) {
		backupDir(fileOrDir, new Host(), false);
	}
	
	public void backupDir(File fileOrDir, boolean deleteOriginal) {
		backupDir(fileOrDir, new Host(), deleteOriginal);
	}

	public void backupDir(File dir, Host host, boolean deleteOriginal) {
		File toDir = new File(new Configuration().getBackupDir().getNonExistingFile(), id);

		System.out.println("Backing up " + dir.getAbsolutePath() + " into " + toDir.getAbsolutePath() + "/" + dir.getName());

		File newDir = new File(toDir, dir.getName());

		if (!deleteOriginal) {
			new Executor("mkdir -p " + newDir.getAbsolutePath()).setHost(host).execute();
			new CopyUtil().copyContentsOfDirectory(dir, host, newDir, host);
		}
		else {
			// we shouldn't remove the whole original directory as that is seldom what is wanted (if the operation
			// populating the directory afterwards fails, the directory will be gone which can cause subsequent runs to fail).
			new Executor("mkdir -p " + newDir.getAbsolutePath()).setHost(host).execute();
			new MoveUtil().moveContentsOfDirectory(dir, host, newDir, host);
		}
	}

	public void backupFile(File file, Host host, boolean deleteOriginal) {
		File toDir = new File(new Configuration().getBackupDir().getValue(), id);

		System.out.println("Backing up " + file.getAbsolutePath() + " into " + toDir.getAbsolutePath() + "/" + file.getName());

		new Executor("mkdir -p " + toDir.getAbsolutePath()).setHost(host).execute();
	
		// we can't just use copy or move here because we can't distinguish
		// between a file and a directory if they are remote.
		if (!deleteOriginal) {
			new CopyUtil().copyFile(file, host, toDir, host);
		}
		else {
			new MoveUtil().moveFile(file, host, toDir, host);
		}
	}
}
