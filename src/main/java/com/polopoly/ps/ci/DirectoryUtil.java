package com.polopoly.ps.ci;

import java.io.File;

import com.polopoly.ps.ci.exception.CIException;
import com.polopoly.ps.pcmd.FatalToolException;

public class DirectoryUtil {

	public void deleteDirectory(File directory) throws CIException {
		if (!directory.exists()) {
			System.out.println(directory.getAbsolutePath() + " does not exist");
		} else if (!directory.isDirectory()) {
			throw new CIException(directory.getAbsolutePath() + " is not a directory.");
		} else {
			new DirectoryUtil().clearDirectory(directory);
			if (!directory.delete()) {
				throw new CIException("Could not delete " + directory.getAbsolutePath());
			} else {
				System.out.println("Deleted " + directory.getAbsolutePath());
			}
		}
	}

	public void deleteDirectory(File parent, String child) throws CIException {
		deleteDirectory(new File(parent, child));
	}

	public void createDirectory(String path) throws CIException {
		createDirectory(new File(path));
	}

	public void createDirectory(File parent, String child) throws CIException {
		createDirectory(new File(parent, child));
	}

	public void createDirectory(File path) throws CIException {
		if (path.exists()) {
			return;
		}

		if (!path.mkdirs()) {
			throw new CIException("Could not create directory " + path);
		} else {
			System.out.println("Created " + path.getAbsolutePath());
		}
	}

	public boolean pathExists(String path) {
		return new File(path).exists();
	}

	public boolean isEmpty(File path) {
		for (File file : path.listFiles()) {
			if (!file.getName().startsWith(".")) {
				return false;
			}
		}

		return true;
	}

	public void clearDirectory(File directory) throws CIException {
		if (!directory.exists()) {
			throw new CIException("Directory " + directory.getAbsolutePath() + " does not exist.");
		}

		if (!isEmpty(directory)) {
			new Executor("rm -rf " + directory.getAbsolutePath() + "/*").execute();
		}
	}

	public void ensureDirectoryExistsAndIsEmpty(File dir) {
		if (dir.exists()) {
			if (!isEmpty(dir)) {
				System.out.println("The directory " + dir.getAbsolutePath() + " existed. Clearing it.");

				new DirectoryUtil().clearDirectory(dir);
			}
		} else {
			if (!dir.mkdirs()) {
				throw new FatalToolException("Cannot create " + dir + ".");
			}
		}
	}

	public void ensureDirectoryExists(File dir) {
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				throw new FatalToolException("Cannot create " + dir + ".");
			}
		}
	}
}
