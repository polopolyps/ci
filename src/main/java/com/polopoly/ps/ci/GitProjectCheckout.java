package com.polopoly.ps.ci;

import java.io.File;

import com.polopoly.ps.ci.configuration.Configuration;
import com.polopoly.ps.ci.exception.CIException;

public class GitProjectCheckout implements ProjectCheckout {
    
	@Override
	public void checkout() {
        try {
            File projectHome = new Configuration().getProjectHomeDirectory().getValue(false);

            String sourceRepositoryUrl = new Configuration().getGitRepository().getValue();

            if (!projectHome.exists()) {
            	 projectHome.mkdirs();
            }

            if (new DirectoryUtil().isEmpty(projectHome)) {
            	new Executor("git clone " + sourceRepositoryUrl + " . ").setDirectory(projectHome).execute();
            } else {
            	new Executor("git pull").setDirectory(projectHome).execute();
            }

            // sanity checking.
            new Configuration().getClientLibPomDirectory().getValue();
            new Configuration().getGuiPomDirectory().getValue();
        } catch (CIException e) {
            throw new CIException("While checking out project code " + e.getMessage(), e);
        }
    }

}
