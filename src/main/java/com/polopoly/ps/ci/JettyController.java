package com.polopoly.ps.ci;

import java.io.File;
import java.net.MalformedURLException;

import com.polopoly.ps.ci.configuration.Configuration;
import com.polopoly.ps.ci.exception.CIException;

public class JettyController extends AbstractWebserverController {
    private static final String LOG_FILE_NAME = "jetty.log";

    @Override
	protected boolean isWebserverProcess(ProcessInfo process) {
    	return process.isJettyProcess();
    }

	@Override
	protected Executor startWebserverProcess() {
		Executor result = startJetty();
		
		try {
			waitFor("/polopoly", result);
		} catch (MalformedURLException e) {
			throw new CIException(e);
		}
		
		return result;
	}

    @Override
	public File getLog() {
        return new File(new Configuration().getProjectHomeDirectory().getValue(), LOG_FILE_NAME);
    }

    private Executor startJetty() throws CIException {
        System.out.println("Starting Jetty...");

        String options = new Configuration().getJettyJDKOptions().getValue();
        String setOptions = "";

        if (!options.equals("")) {
            setOptions = "export MAVEN_OPTS=\"" + options + "\"; ";
        }

        Executor executor = new Executor(setOptions + "mvn jetty:run-all > " + LOG_FILE_NAME + " 2> " + LOG_FILE_NAME).setDirectory(
                new Configuration().getProjectHomeDirectory().getValue()).setWaitFor(false);
        executor.execute();

        checkForImmediateTermination(executor);
        
        return executor;
    }

}
