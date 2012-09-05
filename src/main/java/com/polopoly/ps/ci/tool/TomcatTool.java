package com.polopoly.ps.ci.tool;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;

import com.polopoly.pcmd.tool.Tool;
import com.polopoly.ps.ci.ProcessInfo;
import com.polopoly.ps.ci.TomcatController;
import com.polopoly.ps.ci.configuration.Configuration;
import com.polopoly.ps.ci.exception.CIException;

public class TomcatTool extends SimpleProcessTool implements Tool<ProcessParameters> {

	@Override
	protected void stopProcess() throws CIException {
		killProcess();
	}

	@Override
	protected void startProcess() throws CIException {
		for (TomcatController controller : createControllers()) {
			if (isProcessRunning(controller)) {
				System.out.println("Tomcat was already running. Killing the existing process...");
				controller.kill();
			}

			controller.start();
		}
	}

	protected Iterable<TomcatController> createControllers() {
		return Collections.singleton(new TomcatController());
	}

	@Override
	protected void killProcess() throws CIException {
		for (TomcatController controller : createControllers()) {
			controller.kill();
		}
	}

	/**
	 * Checks if the process is running and if the port configured in the
	 * property guiserver.port is available.
	 * 
	 * @return true if the process is running or if the port is already in use.
	 */
	@Override
	protected boolean isProcessRunning() throws CIException {
		for (TomcatController controller : createControllers()) {
			if (isProcessRunning(controller)) {
				return false;
			}
		}

		return true;
	}

	protected boolean isProcessRunning(TomcatController controller) throws CIException {
		boolean isRunning = controller.isRunning();

		if (!isRunning && controller.isPortAlreadyInUse()) {
			throw new CIException("The Tomcat process was not found but the Tomcat port ("
					+ new Configuration().getGuiServerPort().getValue() + ") was already in use.");
		}

		return isRunning;
	}

	@Override
	protected String getProcessName() {
		return "Tomcat";
	}

	@Override
	protected File getLogFile() {
		Iterator<TomcatController> createControllers = createControllers().iterator();
		File result = createControllers.next().getLog();

		if (createControllers.hasNext()) {
			System.out.println("Cannot get log file for multiple fronts. Returning the first.");
		}

		return result;
	}

	@Override
	protected boolean isThisToolsProcess(ProcessInfo process) {
		return process.isTomcatProcess();
	}
}
