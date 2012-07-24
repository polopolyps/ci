/**
 * 
 */
package com.polopoly.ps.ci;

import java.net.MalformedURLException;
import java.net.URL;

import com.polopoly.ps.ci.configuration.Configuration;
import com.polopoly.ps.ci.exception.CIException;
import com.polopoly.ps.ci.exception.UrlMonitorFailedException;

/**
 * @author Atex
 * 
 */
public final class WarChecker {
    
    private static final int JBOSS_WEB_PORT = new Configuration().getJbossWebPort().getValue();
    private static final int DEFAULT_TIMEOUT = 3000;

    private WarChecker() {
    }

    public static boolean isJbossStatisticsRunning() {
        return isWarRunning("http://localhost:" + JBOSS_WEB_PORT + "/statistics");
    }

    public static boolean isJbossLoggerRunning() {
        return isWarRunning("http://localhost:" + JBOSS_WEB_PORT + "/logger");
    }

    public static boolean isJbossSolrRunning() {
        return isWarRunning("http://localhost:" + new Configuration().getSolrPort().getValue()
                + "/solr/public/select/?q=text:foo");
    }

    protected static boolean isWarRunning(String url) {
        try {
            UrlMonitor monitor = new UrlMonitor(new URL(url));

            monitor.setTimeoutMs(DEFAULT_TIMEOUT);
            monitor.waitForUrl();

            return true;
        } catch (UrlMonitorFailedException e) {
            return false;
        } catch (MalformedURLException e) {
            throw new CIException(e);
        }
    }
}
