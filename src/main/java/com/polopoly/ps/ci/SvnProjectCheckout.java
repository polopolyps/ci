package com.polopoly.ps.ci;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import com.polopoly.ps.ci.configuration.Configuration;
import com.polopoly.ps.ci.exception.CIException;
import com.polopoly.ps.ci.exception.NoDirectoryCheckedOutException;

public class SvnProjectCheckout implements ProjectCheckout {
    
	/* (non-Javadoc)
	 * @see com.polopoly.ps.ci.ProjectCheckout#checkout()
	 */
	@Override
	public void checkout() {
        try {
            File projectHome = new Configuration().getProjectHomeDirectory().getValue(false);

            if (!projectHome.exists()) {
                projectHome.mkdirs();
            }

            URL sourceRepositoryUrl = new Configuration().getSourceRepositoryUrl().getValue();

            if (new DirectoryUtil().isEmpty(projectHome)) {
                new Executor("svn checkout --non-interactive " + sourceRepositoryUrl + " ." + " '--username="
                        + new Configuration().getSourceRepositoryUser().getValue() + "' '--password="
                        + new Configuration().getSourceRepositoryPassword().getValue() + "'").setDirectory(projectHome).execute();
            } else {
                try {
                    URL currentlyCheckedoutUrl = getCurrentlyCheckedoutUrl(projectHome);

                    if (!currentlyCheckedoutUrl.equals(sourceRepositoryUrl)) {
                        throw new CIException("WARNING: " + projectHome + " was checked out from " + currentlyCheckedoutUrl
                                + " rather than the " + sourceRepositoryUrl + " (configured in "
                                + new Configuration().getSourceRepositoryUrl() + ").");
                    }
                } catch (NoDirectoryCheckedOutException e) {
                    throw new CIException("The project home directory " + projectHome + " is not from SVN.");
                }
            }

            // sanity checking.
            new Configuration().getClientLibPomDirectory().getValue();
            new Configuration().getGuiPomDirectory().getValue();
        } catch (CIException e) {
            throw new CIException("While checking out project code " + e.getMessage(), e);
        }
    }

    private URL getCurrentlyCheckedoutUrl(File directory) throws NoDirectoryCheckedOutException {

        String result = new Executor("svn info").setDirectory(directory).setOutputOnConsole(false).execute();

        for (String line : result.split("\n")) {
            if (line.startsWith("URL: ")) {
                try {
                    return new URL(line.substring(5));
                } catch (MalformedURLException e) {
                    throw new CIException("While determining what project was checked out in " + directory + ": " + e
                            + "\n svn info returned: \n" + result, e);
                }
            }
        }

        throw new CIException("Could not determine what project was checked out in " + directory + ". svn info returned: \n"
                + result);
    }
}
