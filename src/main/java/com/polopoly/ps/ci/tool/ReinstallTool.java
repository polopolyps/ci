package com.polopoly.ps.ci.tool;

import com.polopoly.pcmd.tool.Tool;
import com.polopoly.ps.ci.Reinstaller;
import com.polopoly.ps.pcmd.FatalToolException;
import com.polopoly.ps.pcmd.tool.DoesNotRequireRunningPolopoly;
import com.polopoly.util.client.PolopolyContext;

public class ReinstallTool implements Tool<ReinstallParameters>, DoesNotRequireRunningPolopoly {

    @Override
    public void execute(PolopolyContext context, ReinstallParameters parameters) throws FatalToolException {
        Reinstaller reinstaller = new Reinstaller();

        reinstaller.setBuild(parameters.isBuild());
        reinstaller.setCreateDump(parameters.isDump());

        if (parameters.isFullReinstall()) {
            reinstaller.fullInstall();
        } else {
            reinstaller.install();
        }
    }

    @Override
    public ReinstallParameters createParameters() {
        return new ReinstallParameters();
    }

    @Override
    public String getHelp() {
        return "Reinstalls Polopoly and project code.";
    }

}
