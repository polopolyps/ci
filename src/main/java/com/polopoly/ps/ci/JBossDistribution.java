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
import com.polopoly.ps.ci.exception.NoSuchFileException;

public class JBossDistribution extends AbstractDistribution {
    public File getJBossDistribution() {
        try {
            return new Configuration().getJBossDistributionFile().getValue();
        } catch (NoSuchFileException e) {
            System.out.println("The JBoss distribution did not exist locally. Trying to download it...");
        }

        URL distributionUrl = new Configuration().getJBossDistributionUrl().getValue();

        if (!distributionUrl.getProtocol().equals("http")) {
            throw new CIException("JBoss distribution URL " + distributionUrl + " was not an HTTP URL.");
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
                throw new CIException("Tried to download the " + "JBoss distribution but failed with status code "
                        + get.getStatusCode() + " (" + get.getStatusText() + "). The server reported: " + getError(get));
            }

            InputStream distributionStream = get.getResponseBodyAsStream();

            File distributionFile = new Configuration().getJBossDistributionFile().getNonExistingFile();

            if (!distributionFile.getParentFile().exists()) {
                distributionFile.getParentFile().mkdirs();
            }

            downloadToFile(distributionStream, distributionFile, getContentLength(get));

            return distributionFile;
        } catch (HttpException e) {
            throw new CIException("While trying to download JBoss distribution: " + e, e);
        } catch (IOException e) {
            throw new CIException("While trying to download JBoss distribution: " + e, e);
        }
    }
}
