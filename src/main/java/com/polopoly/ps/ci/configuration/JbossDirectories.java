package com.polopoly.ps.ci.configuration;

import java.io.File;

import com.polopoly.ps.ci.DirectoryUtil;
import com.polopoly.ps.ci.exception.CIException;
import com.polopoly.ps.ci.exception.NoSuchFileException;

public class JbossDirectories extends AbstractDirectories {

    public File getJbossDirectory(boolean mustExist) {
        return new Configuration().getJbossDirectory().getValue(mustExist);
    }

    public File getJbossDirectory() throws CIException {
        return getJbossDirectory(true);
    }

    private File getJbossServerFile(String relativeFileName, boolean mustExist) throws CIException {
        File result = new File(getServerDirectory(), relativeFileName);

        if (mustExist) {
            return verifyFileExistence(result);
        } else {
            return result;
        }
    }

    private File getJbossServerDirectory(String relativeFileName) throws NoSuchFileException {
        return verifyDirectoryExistence(new File(getServerDirectory(), relativeFileName));
    }

    private File getServerDirectory() {
        return new File(new Configuration().getJbossDirectory().getValue(), "server/"
                + new Configuration().getJbossProfile().getValue());
    }

    public File getJbossDeployDirectory() throws CIException {
        return getJbossServerDirectory("deploy");
    }

    public File getJbossPolopolyDeployDirectory() throws NoSuchFileException {
        File result = new File(getJbossDeployDirectory(), "polopoly");

        if (!result.exists()) {
            new DirectoryUtil().createDirectory(result);
        }

        return result;
    }

    public File getJbossLog(boolean mustExist) throws CIException {
    	return new Configuration().getJbossLogFile().getValue(mustExist);
    }

    public File getJbossWorkDirectory() throws NoSuchFileException {
        return getJbossServerDirectory("work");
    }

    public File getJbossLog() throws CIException {
        return getJbossLog(true);
    }

    public File getPolopolyEarFile(boolean mustExist) throws CIException {
        return getEarOfWarFile("cm-server.ear", mustExist);
    }

    public File getSolrWarFile(boolean mustExist) throws CIException {
        return getEarOfWarFile("solr.war", mustExist);
    }

    public File getStatisticsWarFile(boolean mustExist) throws CIException {
        return getEarOfWarFile("statistics.war", mustExist);
    }

    protected File getEarOfWarFile(String fileName, boolean mustExist) {
        File file = new File(getJbossPolopolyDeployDirectory(), fileName);

        if (mustExist) {
            try {
                return verifyFileExistence(file);
            } catch (NoSuchFileException e) {
                // In previous versions of this project we used this dir to
                // deploy polopoly. This is for backward compatibility.
                return verifyFileExistence(new File(getJbossDeployDirectory(), fileName));
            }
        } else {
            return file;
        }
    }

    public File getClientDirectory() {
        return verifyDirectoryExistence(new File(getJbossDirectory(), "client"));
    }

    public File getJbossStartupDirectory() {
        return new Configuration().getJbossStartupDirectory().getValue();
    }

    public File getJbossServerLibDirectory() {
        return getJbossServerDirectory("lib");
    }

    public File getJbossConfDirectory() {
        return getJbossServerDirectory("conf");
    }
}
