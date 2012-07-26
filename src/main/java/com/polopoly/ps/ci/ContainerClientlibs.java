package com.polopoly.ps.ci;

import java.io.File;

import com.polopoly.ps.ci.configuration.JbossDirectories;
import com.polopoly.ps.ci.configuration.PolopolyDirectories;
import com.polopoly.ps.ci.exception.CIException;
import com.polopoly.ps.ci.exception.RunningInMavenException;

public class ContainerClientlibs {

    private static String[] CONTAINER_CLIENT_LIBS = new String[] { "concurrent.jar", "jboss-client.jar",
            "jboss-common-client.jar", "jbossha-client.jar", "jboss-j2ee.jar", "jbossmq-client.jar", "jbosssx-client.jar",
            "jboss-system-client.jar", "jboss-transaction-client.jar", "jboss-serialization.jar", "jboss-remoting.jar",
            "jmx-client.jar", "jnp-client.jar", "commons-logging.jar",
            // Required by Jboss 4.2.3.GA
            "jbosscx-client.jar" };

    public void copyContainerClientLibs() throws RunningInMavenException {
        try {
            File containerClientLibDirectory = new PolopolyDirectories().getContainerClientlibDirectory();
            File jbossClientDirectory = new JbossDirectories().getClientDirectory();
            
            System.out.println("Copying JBoss client libs to Polopoly...");

            new DirectoryUtil().ensureDirectoryExistsAndIsEmpty(containerClientLibDirectory);

            for (String fileName : CONTAINER_CLIENT_LIBS) {
                File file = new File(jbossClientDirectory, fileName);

                if (!file.exists()) {
                    throw new CIException("Expected " + file.getAbsolutePath() + " to exist.");
                }

                if (!new File(containerClientLibDirectory, fileName).exists()) {
                    new Executor("cp " + file.getAbsolutePath() + " " + containerClientLibDirectory.getAbsolutePath()).execute();
                }
            }

            System.out.println("Done copying JBoss client libs.");
        } catch (CIException e) {
            throw new CIException("While copying container client libs: " + e.getMessage(), e);
        }
    }

}
