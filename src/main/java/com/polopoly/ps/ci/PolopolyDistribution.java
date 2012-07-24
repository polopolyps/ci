package com.polopoly.ps.ci;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

import com.polopoly.ps.ci.configuration.Configuration;
import com.polopoly.ps.ci.exception.CIException;
import com.polopoly.ps.ci.exception.NoSuchFileException;

public class PolopolyDistribution extends AbstractDistribution {
    /**
     * Retrieves the Polopoly distribution, downloading it from the support site
     * if necessary.
     */
    public File getPolopolyDistribution() {
        try {
            return new Configuration().getPolopolyDistributionFile().getValue();
        } catch (NoSuchFileException e) {
            System.out.println("The Polopoly distribution did not exist locally. Trying to download it...");
        }

        URL distributionUrl = new Configuration().getPolopolyDistributionUrl().getValue();

        if (!distributionUrl.getProtocol().equals("http")) {
            throw new CIException("Polopoly distribution URL " + distributionUrl + " was not an HTTP URL.");
        }

        HttpClient client = new HttpClient();

        if (distributionUrl.getHost().equals("support.polopoly.com")) {
            logInToSupportSite(client, distributionUrl);
        }

        System.out.println("Downloading from " + distributionUrl + "...");

        GetMethod get = new GetMethod(distributionUrl.toString());

        try {
            client.executeMethod(get);

            if (get.getStatusCode() != 200) {
                throw new CIException("Tried to download the " + "Polopoly distribution but failed with status code "
                        + get.getStatusCode() + " (" + get.getStatusText() + "). The server reported: " + getError(get));
            }

            InputStream distributionStream = get.getResponseBodyAsStream();

            File distributionFile = new Configuration().getPolopolyDistributionFile().getNonExistingFile();

            downloadToFile(distributionStream, distributionFile, getContentLength(get));

            return distributionFile;
        } catch (HttpException e) {
            throw new CIException("While trying to download Polopoly distribution: " + e, e);
        } catch (IOException e) {
            throw new CIException("While trying to download Polopoly distribution: " + e, e);
        }
    }

    private void logInToSupportSite(HttpClient client, URL url) throws CIException {
        System.out.println("Logging in to " + url.getHost() + "...");

        PostMethod post = new PostMethod("http://support.polopoly.com/confluence/login.action");

        NameValuePair[] data = { new NameValuePair("os_username", new Configuration().getSupportUsername().getValue()),
                new NameValuePair("os_password", new Configuration().getSupportPassword().getValue()),
                new NameValuePair("os_destination", url.getFile()) };

        post.setRequestBody(data);
        // execute method and handle any error responses.

        try {
            client.executeMethod(post);
        } catch (HttpException e) {
            throw new CIException("While trying to log in to the support site: " + e, e);
        } catch (IOException e) {
            throw new CIException("While trying to log in to the support site: " + e, e);
        }

        if (post.getStatusCode() != 302) {
            throw new CIException("Tried to log in to the support site to download the "
                    + "Polopoly distribution but failed with status code " + post.getStatusCode() + " (" + post.getStatusText()
                    + " - was expecting 302 redirect). The server reported: " + getError(post));
        }
    }

}
