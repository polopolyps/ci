package com.polopoly.ps.ci;

import java.io.File;

import com.polopoly.ps.ci.configuration.Configuration;
import com.polopoly.ps.ci.configuration.TomcatDirectories;

public class TomcatController extends AbstractWebserverController {

	public TomcatController() {
	}

	public TomcatController(Host host) {
		super(host);
	}

	@Override
	protected boolean isWebserverProcess(ProcessInfo process) {
		return process.isTomcatProcess();
	}

	@Override
	protected Executor startWebserverProcess() {
		System.out.println("Starting Tomcat...");

		String options = new Configuration().getTomcatOptions().getValue();

		Executor executor = new Executor("./startup.sh " + options).setDirectory(
				new TomcatDirectories().getBinDirectory()).setWaitFor(true);

		if (host != null) {
			executor.setHost(host);
		}

		executor.execute();

		checkForImmediateTermination(executor);

		return executor;
	}

	@Override
	public File getLog() {
		return new TomcatDirectories().getLog();
	}

}
