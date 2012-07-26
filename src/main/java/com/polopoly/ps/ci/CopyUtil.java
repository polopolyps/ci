package com.polopoly.ps.ci;

import java.io.File;

import com.polopoly.ps.pcmd.FatalToolException;

public class CopyUtil {
	public void copyFile(File file, Host fromHost, File toDir, Host toHost) {
		if (fromHost.equals(toHost)) {
			new Executor("cp " + file.getAbsolutePath() + " "
				+ toDir.getAbsolutePath() + "/").setHost(fromHost).execute();
		}
		else {
			new Executor("scp " + file.getAbsolutePath() + " " + toHost + ":" + toDir.getAbsolutePath() + "/").setHost(fromHost).execute();
		}
	}

	/**
	 * Takes the directory fromDir and puts it inside toDir (so the resulting directory is toDir/fromDir.getName).
	 */
	public void copyWholeDirectory(File fromDir, Host fromHost, File toDir, Host toHost) {		
		if (fromHost.equals(toHost)) {
			new Executor("cp -R " + fromDir.getAbsolutePath() + "/ "
				+ toDir.getAbsolutePath() + "/").setHost(fromHost).execute();
		}
		else {
			if (!fromHost.isLocalHost()) {
				throw new UnsupportedOperationException();
			}
			
			new Executor("scp -r " + fromDir.getAbsolutePath() + "/ " + toHost + ":" + toDir.getAbsolutePath() + "/").setHost(fromHost).execute();
		}
		
	}

	/**
	 * Takes everything inside directory fromDir and puts it inside toDir ("cp -r fromDir/* toDir").
	 */
	public void copyContentsOfDirectory(File fromDir, Host fromHost, File toDir, Host toHost) {		
		if (fromHost.isLocalHost() && new DirectoryUtil().isEmpty(fromDir)) {
			return;
		}
		
		if (fromHost.equals(toHost)) {
			new Executor("cp -R " + fromDir.getAbsolutePath() + "/* "
				+ toDir.getAbsolutePath() + "/").setHost(fromHost).execute();
		}
		else {
			if (!fromHost.isLocalHost()) {
				throw new UnsupportedOperationException();
			}
			
			new Executor("scp -r " + fromDir.getAbsolutePath() + "/* " + toHost + ":" + toDir.getAbsolutePath() + "/").setHost(fromHost).execute();
		}
		
	}

	public void copy(File fileOrDir, Host fromHost, File toDir, Host toHost) {
		if (!fromHost.isLocalHost()) {
			// TODO
			throw new UnsupportedOperationException(); 
		}
		
		if (!fileOrDir.exists()) {
			throw new FatalToolException(fileOrDir + " that should be copied to " + toDir + " on host " + toHost + " did not exist.");
		}
		
		if (fileOrDir.isDirectory()) {
			copyWholeDirectory(fileOrDir, fromHost, toDir, toHost);
		}
		else {
			copyFile(fileOrDir, fromHost, toDir, toHost);
		}
	}

}
