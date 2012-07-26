package com.polopoly.ps.ci;

import com.polopoly.ps.ci.exception.CIException;
import com.polopoly.util.client.ConnectException;
import com.polopoly.util.client.PolopolyClient;
import com.polopoly.util.client.PolopolyContext;

public class PolopolyConnector {
    private static PolopolyContext context;

    public PolopolyContext connect(boolean importTestContent) {
        if (context != null) {
            return context;
        }

        try {
            PolopolyJarLoader jarLoader = new PolopolyJarLoader();
            jarLoader.loadJarsNeededToConnect();
            jarLoader.loadProjectClasspath(importTestContent);
            jarLoader.sanityCheckClasspath();

            PolopolyClient client = new PolopolyClient();
            client.setAttachSearchService(false);
            client.setAttachStatisticsService(false);
            client.setAttachSolrSearchClient(false);

            context = client.connect();

            return context;
        } catch (ConnectException e) {
            throw new CIException(e);
        }
    }

}
