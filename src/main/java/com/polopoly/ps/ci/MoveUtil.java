package com.polopoly.ps.ci;

import java.io.File;

import com.polopoly.ps.ci.tool.FileExistence;
import com.polopoly.ps.pcmd.FatalToolException;

public class MoveUtil {

	public void moveFile(File file, Host fromHost, File toDir, Host toHost) {
		if (fromHost.equals(toHost)) {
			String command = "mv " + file.getAbsolutePath() + " " + toDir.getAbsolutePath() + "/";

			new Executor(command).setHost(fromHost).execute();
		} else {
			new CopyUtil().copyFile(file, fromHost, toDir, toHost);
			new Executor("rm -rf " + file).setHost(fromHost).execute();
		}
	}

	/**
	 * Takes the directory fromDir and puts it inside toDir (so the resulting
	 * directory is toDir/fromDir.getName).
	 */
	public void moveWholeDirectory(File fromDir, Host fromHost, File toDir, Host toHost) {
		if (fromHost.equals(toHost)) {
			new Executor("mv " + fromDir.getAbsolutePath() + "/ " + toDir.getAbsolutePath() + "/").setHost(fromHost)
					.execute();
		} else {
			new CopyUtil().copyWholeDirectory(fromDir, fromHost, toDir, toHost);

			new Executor("rm -rf " + fromDir).setHost(fromHost).execute();
		}
	}

	public void move(File fileOrDir, Host fromHost, File toDir, Host toHost) {
		if (!fromHost.isLocalHost()) {
			// TODO
			throw new UnsupportedOperationException();
		}

		if (!fileOrDir.exists()) {
			throw new FatalToolException(fileOrDir + " that should be moved to " + toDir + " on host " + toHost
					+ " did not exist.");
		}

		if (fileOrDir.isDirectory()) {
			moveWholeDirectory(fileOrDir, fromHost, toDir, toHost);
		} else {
			moveFile(fileOrDir, fromHost, toDir, toHost);
		}
	}

	public void moveContentsOfDirectory(File fromDir, Host fromHost, File toDir, Host toHost) {
		if (fromHost.equals(toHost)) {
			if (!new FileExistence(fromHost).isDirectoryEmpty(fromDir)) {
				new Executor("mv " + fromDir.getAbsolutePath() + "/* " + toDir.getAbsolutePath() + "/").setHost(
						fromHost).execute();
			}
			else {
				System.out.println("While moving content of " + fromDir + ": directory was empty so ignoring it.");
			}
		} else {
			new CopyUtil().copyContentsOfDirectory(fromDir, fromHost, toDir, toHost);

			new Executor("rm -rf " + fromDir + "/*").setHost(fromHost).execute();
		}
	}

}
