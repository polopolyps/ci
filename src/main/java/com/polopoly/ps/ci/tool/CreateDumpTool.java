package com.polopoly.ps.ci.tool;

import com.polopoly.pcmd.tool.Tool;
import com.polopoly.ps.ci.DumpExporter;
import com.polopoly.ps.pcmd.FatalToolException;
import com.polopoly.ps.pcmd.tool.DoesNotRequireRunningPolopoly;
import com.polopoly.util.client.PolopolyContext;

public class CreateDumpTool implements Tool<CreateDumpParameters>, DoesNotRequireRunningPolopoly {

    @Override
    public void execute(PolopolyContext context, CreateDumpParameters parameters) throws FatalToolException {
        new DumpExporter(parameters.getDumpFile()).dump();
    }

    @Override
    public CreateDumpParameters createParameters() {
        return new CreateDumpParameters();
    }

    @Override
    public String getHelp() {
        return "Creates a dump of the complete Polopoly state for later restoring.";
    }

}
