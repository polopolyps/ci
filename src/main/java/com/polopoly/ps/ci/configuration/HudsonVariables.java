package com.polopoly.ps.ci.configuration;

import java.io.File;

import com.polopoly.ps.ci.exception.NotRunningInHudsonException;

public class HudsonVariables extends AbstractDirectories {

    private static String getHudsonVariable(String environmentVariable) throws NotRunningInHudsonException {
        String variable = System.getenv(environmentVariable);

        if (variable == null) {
            throw new NotRunningInHudsonException("The Hudson variable " + environmentVariable
                    + " was not set. Is the script running inside Hudson?");
        }

        return variable;
    }

    private String getJobName() throws NotRunningInHudsonException {
        return getHudsonVariable("JOB_NAME");
    }

    private String getBuildNumber() throws NotRunningInHudsonException {
        return getHudsonVariable("BUILD_NUMBER");
    }

    public File getHudsonHome() throws NotRunningInHudsonException {
        return verifyDirectoryExistence(getHudsonVariable("HUDSON_HOME"));
    }

    public File getThisBuildUserContentDirectory() {
        return new File(getHudsonHome(), "userContent/" + getJobName() + "/" + getBuildNumber());
    }

    public File getUserContentDirectory() {
        return verifyDirectoryExistence(new File(getHudsonHome(), "userContent"));
    }
}
