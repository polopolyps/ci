package com.polopoly.ps.ci.tool;

import com.polopoly.pcmd.tool.Tool;
import com.polopoly.ps.ci.ProcessUtil;
import com.polopoly.ps.ci.SolrReindexer;
import com.polopoly.ps.pcmd.FatalToolException;
import com.polopoly.util.client.PolopolyContext;

public class ReindexSolrTool implements Tool<UpdateClientLibParameters> {

    @Override
    public void execute(PolopolyContext context, UpdateClientLibParameters parameters) throws FatalToolException {
        new ProcessUtil().stopIndexServer();

        new UpdateClientLibTool().execute(context, parameters);

        new ProcessUtil().startIndexServer();

        new SolrReindexer().reindexSolr();
    }

    @Override
    public UpdateClientLibParameters createParameters() {
        return new UpdateClientLibParameters();
    }

    @Override
    public String getHelp() {
        return "Updates client-lib, restarts the indexserver, and reindexes the solr index.";
    }

}
