package com.polopoly.ps.ci;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;

import com.polopoly.ps.ci.configuration.Configuration;
import com.polopoly.ps.ci.configuration.PolopolyDirectories;
import com.polopoly.ps.ci.exception.CIException;
import com.polopoly.ps.ci.exception.NoSuchFileException;
import com.polopoly.ps.ci.exception.RunningInMavenException;

public class PolopolyJarLoader {
    private NonDuplicatingClasspathBuilder classpath = new NonDuplicatingClasspathBuilder();

    public void loadJarsNeededForCI() {
        classpath.addJarFile(new DependencyResolver().resolve(new Configuration().getPolopolyArtifact().getValue()));
        classpath.addJarFile(new DependencyResolver().resolve(new Configuration().getPcmdArtifact().getValue()));
        classpath.addJarFile(new DependencyResolver().resolve(new Configuration().getHotdeployToolsArtifact().getValue()));
        classpath.addJarFile(new DependencyResolver().resolve(new Configuration().getHotdeployArtifact().getValue()));

        removeJarsAlreadyInClasspath();

        classpath.load();
    }

    public void loadJarsNeededToConnect() {
        try {
            classpath.addJarDirectory(new PolopolyDirectories().getLibDirectory());
            classpath.addJarDirectory(new PolopolyDirectories().getContainerClientlibDirectory());
        } catch (NoSuchFileException e) {
            System.out.println("While loading required JARs: " + e);
		} catch (RunningInMavenException e) {
			// ok. hope the files are in the clientlib POM.
		}

        removeJarsAlreadyInClasspath();

        classpath.load();
    }

    public void loadProjectClasspath(boolean testScope) {
        try {
            File clientlibPom = new Configuration().getClientLibPomDirectory().getValue();

            for (File jar : new MavenClassPathParser(clientlibPom).setTestScope(testScope).getClasspath()) {
                classpath.addJarFile(jar);
            }
        } catch (NoSuchFileException e) {
            try {
            	File clientLibDirectory = new PolopolyDirectories().getClientLibDirectory();

            	System.out.println("The clientlib POM directory doesn't exist. Using custom/clientlib: " + e.getMessage());
				classpath.addJarDirectory(clientLibDirectory);
			} catch (RunningInMavenException e2) {
				throw new CIException("The clientlib POM directory doesn't exist (" + e.getMessage() + 
						") and running the Maven Polopoly installation (" + e2.getMessage() +
						") so the custom/client-lib directory cannot be used to load JARs.");
			}
        }

        classpath.addClassDirectory(new File(new Configuration().getClientLibPomDirectory().getValue(), "target/classes"));

        classpath.load();
    }

    public void reportDuplicates() {
        Set<File> duplicates = classpath.getDuplicates();

        for (File duplicate : duplicates) {
            VerboseLogging.log("Duplicate JAR. Not adding to classpath: " + duplicate.getAbsolutePath());
        }
    }

    private void removeJarsAlreadyInClasspath() {
        URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();

        for (URL url : sysLoader.getURLs()) {
            classpath.removeActuallyUsedJar(new File(url.getFile()));
        }
    }

    /**
     * In case some JARs required by the tools are missing from the project
     * classpath, load them here.
     */
    public void sanityCheckClasspath() {
        try {
            Class.forName("org.jboss.naming.NamingContextFactory");
        } catch (ClassNotFoundException e) {
            VerboseLogging.log("JBoss client JARs don't seem to be present in project classpath. Adding jbossall-client.jar");

            loadJbossJars();
        }

        // needed for content import.
        try {
            Class.forName("org.jaxen.JaxenException");
        } catch (ClassNotFoundException e) {
            VerboseLogging.log("Jaxen doesn't seem to be present in project classpath. Adding it.");

            try {
                classpath.addJarFile(new DependencyResolver().resolve("jaxen:jaxen:1.1.1"));
            } catch (CIException e2) {
                System.err.println("While loading jaxen: " + e2.getMessage());
            }
        }

        classpath.load();
    }

    /**
     * Loads the jbossall JAR. Some Polopoly methods (e.g. content XML manipulation) need them even if they don't need a running Polopoly. This is dues to the javax.ejb.CreateException.
     */
	public void loadJbossJars() {
        try {
            classpath.addJarFile(new DependencyResolver().resolve("jboss:jbossall-client:4.0.5.GA"));
        } catch (CIException e2) {
            System.err.println("While loading jboss client JARs: " + e2.getMessage());
        }
        
        classpath.load();
	}
}
