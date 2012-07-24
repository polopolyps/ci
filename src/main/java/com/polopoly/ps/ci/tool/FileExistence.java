package com.polopoly.ps.ci.tool;

import java.io.File;

import com.polopoly.ps.ci.DirectoryUtil;
import com.polopoly.ps.ci.Executor;
import com.polopoly.ps.ci.Host;
import com.polopoly.ps.ci.exception.CIException;

public class FileExistence {

	private Host host;

	public FileExistence(Host host) {
		this.host = host;
	}

	public boolean isDirectoryEmpty(File dir) {
		if (host.isLocalHost()) {
			return new DirectoryUtil().isEmpty(dir);
		}
		
		String command = "[ \"$(ls -A " + dir.getAbsolutePath() + ")\" ] && exit 4 || exit 3";
		
		return check(command);
	}
	
	public boolean exists(File file) {
		if (host.isLocalHost()) {
			return file.exists();
		}
		
		String command = "if [ -e " + file.getAbsolutePath() + " ]; then exit 3; else exit 4; fi";

		return check(command);
	}

	protected boolean check(String command) {
		Executor executor = new Executor(command);

		executor.setHost(host).setWaitFor(false).execute();

		Process process = executor.getProcess();

		try {
			process.waitFor();
		} catch (InterruptedException e) {
			throw new CIException(e);
		}

		if (process.exitValue() == 3) {
			return true;
		} else if (process.exitValue() == 4) {
			return false;
		} else {
			throw new CIException("The command " + executor + " failed.");
		}
	}

}
