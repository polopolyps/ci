package com.polopoly.ps.ci.tool;

import java.io.File;

import com.polopoly.pcmd.tool.Tool;
import com.polopoly.ps.ci.DumpRestorer;
import com.polopoly.ps.ci.configuration.ProjectDumpDirectories;
import com.polopoly.ps.pcmd.FatalToolException;
import com.polopoly.ps.pcmd.argument.NotProvidedException;
import com.polopoly.ps.pcmd.tool.DoesNotRequireRunningPolopoly;
import com.polopoly.util.client.PolopolyContext;

public class RestoreDumpTool implements Tool<RestoreDumpParameters>, DoesNotRequireRunningPolopoly {

    @Override
    public void execute(PolopolyContext context, RestoreDumpParameters parameters) throws FatalToolException {
        File dumpFile;

        try {
            dumpFile = parameters.getDumpFile();
        } catch (NotProvidedException e1) {
            dumpFile = new ProjectDumpDirectories().getDefaultDumpFile(true);
        }

        new DumpRestorer(dumpFile).restore();
    }

    @Override
    public RestoreDumpParameters createParameters() {
        return new RestoreDumpParameters();
    }

    @Override
    public String getHelp() {
        return "Restores a dump of the complete Polopoly state created using create-dump";
    }

}
