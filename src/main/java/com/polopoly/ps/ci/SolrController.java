package com.polopoly.ps.ci;

import java.net.MalformedURLException;
import java.net.URL;

import com.polopoly.ps.ci.configuration.Configuration;
import com.polopoly.ps.ci.exception.CIException;
import com.polopoly.ps.ci.exception.UrlMonitorFailedException;

public class SolrController {

    public boolean isRunning() {
        try {
            UrlMonitor monitor = new UrlMonitor(new URL("http://localhost:" + new Configuration().getSolrPort().getValue()
                    + "/solr/public/select/?q=text:foo"));

            monitor.setTimeoutMs(3000);
            monitor.waitForUrl();

            return true;
        } catch (UrlMonitorFailedException e) {
            return false;
        } catch (MalformedURLException e) {
            throw new CIException(e);
        }
    }
}
