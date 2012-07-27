package com.polopoly.ps.ci;

import static java.lang.Thread.sleep;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;

import com.polopoly.ps.ci.exception.CIException;
import com.polopoly.ps.ci.exception.UrlMonitorFailedException;
import com.polopoly.ps.pcmd.util.Plural;

public class UrlMonitor {
	private URL url;
	private long timeoutMs = 5000;
	private Runnable whatToDoInBreaks;
	private long singleRequestTimeoutMs;

	UrlMonitor(URL url) {
		this.url = Require.require(url);
	}

	public void waitForUrl() throws CIException {
		int counter = 0;

		System.out.println("Waiting for " + url + " to respond...");

		long startTime = System.currentTimeMillis();

		while (System.currentTimeMillis() - startTime <= timeoutMs) {
			try {
				sleep(100);
			} catch (InterruptedException e) {
			}

			try {
				if (isURLReady(url)) {
					System.out.println(url + " responded with status 200 (= ok).");

					return;
				} else if (counter % 10 == 0) {
					int seconds = (int) (System.currentTimeMillis() - startTime) / 1000;
					System.out.println("Waited " + Plural.count(seconds, "second") + "...");
				}
			} finally {
				if (whatToDoInBreaks != null) {
					whatToDoInBreaks.run();
				}
			}

			counter++;
		}

		throw new UrlMonitorFailedException("Could not load the URL " + url + " even after trying for "
				+ (timeoutMs / 1000) + " seconds.");
	}

	private boolean isURLReady(URL url) throws CIException {
		HttpClient httpclient = new HttpClient();

		httpclient.setConnectionTimeout((int) singleRequestTimeoutMs);
		httpclient.setTimeout((int) singleRequestTimeoutMs);

		GetMethod httpget = new GetMethod(url.toString());
		int status;

		try {
			status = httpclient.executeMethod(httpget);
		} catch (HttpException e) {
			throw new CIException("While calling " + url + ": " + e.getMessage(), e);
		} catch (IOException e) {
			return false;
		}

		if (status == 200) {
			return true;
		} else {
			throw new CIException("The URL " + url + " returned status code " + status + ".");
		}
	}

	public void setTimeoutMs(long timeoutMs) {
		this.timeoutMs = timeoutMs;
		singleRequestTimeoutMs = timeoutMs;
	}

	public void setWhatToDoInBreaks(Runnable whatToDoInBreaks) {
		this.whatToDoInBreaks = whatToDoInBreaks;
	}

	public void setSingleRequestTimeoutMs(int singleRequestTimeoutMs) {
		this.singleRequestTimeoutMs = singleRequestTimeoutMs;
	}

}
