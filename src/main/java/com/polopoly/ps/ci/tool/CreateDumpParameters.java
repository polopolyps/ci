package com.polopoly.ps.ci.tool;

import java.io.File;

import com.polopoly.ps.ci.configuration.ProjectDumpDirectories;
import com.polopoly.ps.pcmd.argument.ArgumentException;
import com.polopoly.ps.pcmd.argument.Arguments;
import com.polopoly.ps.pcmd.argument.NotProvidedException;
import com.polopoly.ps.pcmd.argument.ParameterHelp;
import com.polopoly.ps.pcmd.argument.Parameters;
import com.polopoly.ps.pcmd.parser.FileParser;
import com.polopoly.util.client.PolopolyContext;

public class CreateDumpParameters implements Parameters {

    private File dumpFile;

    @Override
    public void parseParameters(Arguments args, PolopolyContext context) throws ArgumentException {
        try {
            dumpFile = args.getArgument(0, new FileParser());
        } catch (NotProvidedException e) {
            // fine. use default name.
        }
    }

    @Override
    public void getHelp(ParameterHelp help) {
        help.setArguments(new FileParser(), "The file to create the dump in.");
    }

    public File getDumpFile() {
        if (dumpFile == null) {
            return new ProjectDumpDirectories().getDefaultDumpFile(false);
        } else {
            return dumpFile;
        }
    }

}
