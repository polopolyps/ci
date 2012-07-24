package com.polopoly.ps.ci;

import java.io.File;

import com.polopoly.ps.ci.configuration.Configuration;
import com.polopoly.ps.ci.exception.CIException;

public class Builder {
    private boolean skipTests = true;
    private String project = null;

    public void installAll(boolean clean) {
        try {
            File projectDirectory = new Configuration().getProjectParentPomDirectory().getValue();

            if (project != null) {
                projectDirectory = new File(projectDirectory, project);
            }

            new Executor("mvn " + (clean ? "clean " : "") + "install" + (skipTests ? " -DskipTests" : "")).setDirectory(
                    projectDirectory).execute();
        } catch (CIException e) {
            throw new CIException("While building project code: " + e.getMessage(), e);
        }
    }

    public boolean isSkipTests() {
        return skipTests;
    }

    public void setSkipTests(boolean skipTests) {
        this.skipTests = skipTests;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }
}
