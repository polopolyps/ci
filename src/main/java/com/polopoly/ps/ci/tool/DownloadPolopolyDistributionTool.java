package com.polopoly.ps.ci.tool;

import com.polopoly.pcmd.tool.Tool;
import com.polopoly.ps.ci.PolopolyDistribution;
import com.polopoly.ps.pcmd.FatalToolException;
import com.polopoly.ps.pcmd.argument.EmptyParameters;
import com.polopoly.ps.pcmd.tool.DoesNotRequireRunningPolopoly;
import com.polopoly.util.client.PolopolyContext;

public class DownloadPolopolyDistributionTool implements Tool<EmptyParameters>, DoesNotRequireRunningPolopoly {

    @Override
    public EmptyParameters createParameters() {
        return new EmptyParameters();
    }

    @Override
    public void execute(PolopolyContext arg0, EmptyParameters arg1) throws FatalToolException {
        System.out.println(new PolopolyDistribution().getPolopolyDistribution().getAbsolutePath());
    }

    @Override
    public String getHelp() {
        return "Downloads the Polopoly distribution if not available locally and print its location.";
    }

}
