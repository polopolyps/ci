package com.polopoly.ps.ci;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;

import com.polopoly.ps.ci.configuration.Configuration;
import com.polopoly.ps.ci.exception.CIException;

public class MySQLConnectorDistribution extends AbstractDistribution {
    public File getMySQLConnectorFile() {
        File mySQLConnectorFile = new Configuration().getMySQLConnectorFile().getNonExistingFile();

        if (mySQLConnectorFile.exists()) {
            return mySQLConnectorFile;
        }

        System.out.println("The MySQL connector file did not exist locally. Extracting it from the distribution...");

        File distributionFile = getMySQLConnectorDistributionFile();

        String nameInZip = trimZip(distributionFile.getName()) + "/" + mySQLConnectorFile.getName();

        new Executor("unzip " + distributionFile.getAbsolutePath() + " " + nameInZip).setDirectory(
                distributionFile.getParentFile()).execute();

        try {
            new Executor("mv " + nameInZip + " " + mySQLConnectorFile.getAbsolutePath()).setDirectory(
                    distributionFile.getParentFile()).execute();
        } catch (CIException e) {
            throw new CIException("Expected the MySQL connector file " + mySQLConnectorFile.getName() + " to be in " + nameInZip
                    + " in the distribution " + distributionFile.getAbsolutePath() + ".");
        }

        return mySQLConnectorFile;
    }

    private String trimZip(String name) {
        if (name.endsWith(".zip")) {
            return name.substring(0, name.length() - 4);
        } else {
            throw new CIException("Expected MYSQL distribution file name " + name + " to end with \".zip\".");
        }
    }

    public File getMySQLConnectorDistributionFile() {
        File distributionFile = new Configuration().getMySQLConnectorDistributionFile().getNonExistingFile();

        if (distributionFile.exists()) {
            return distributionFile;
        }

        System.out.println("MySQL distribution did not exist locally... Downloading it...");

        URL distributionUrl = new Configuration().getMySQLConnectorDistributionUrl().getValue();

        if (!distributionUrl.getProtocol().equals("http")) {
            throw new CIException("MySQL distribution URL " + distributionUrl + " was not an HTTP URL.");
        }

        HttpClient client = new HttpClient();

        System.out.println("Downloading from " + distributionUrl + "...");

        GetMethod get = new GetMethod(distributionUrl.toString());
        get.setFollowRedirects(false);

        try {
            client.executeMethod(get);

            while (get.getStatusCode() == 302 || get.getStatusCode() == 301) {
                get = new GetMethod(get.getResponseHeader("Location").getValue());

                get.setFollowRedirects(false);
                client.executeMethod(get);
            }

            if (get.getStatusCode() != 200) {
                throw new CIException("Tried to download the " + "MySQL connector distribution but failed with status code "
                        + get.getStatusCode() + " (" + get.getStatusText() + "). The server reported: " + getError(get));
            }

            InputStream distributionStream = get.getResponseBodyAsStream();

            if (!distributionFile.getParentFile().exists()) {
                distributionFile.getParentFile().mkdirs();
            }

            downloadToFile(distributionStream, distributionFile, getContentLength(get));

            return distributionFile;
        } catch (HttpException e) {
            throw new CIException("While trying to download MySQL connector distribution: " + e, e);
        } catch (IOException e) {
            throw new CIException("While trying to download MySQL connector distribution: " + e, e);
        }
    }
}
