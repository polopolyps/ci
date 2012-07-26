package com.polopoly.ps.ci.tool;

import java.io.File;

import com.polopoly.ps.pcmd.argument.ArgumentException;
import com.polopoly.ps.pcmd.argument.Arguments;
import com.polopoly.ps.pcmd.argument.NotProvidedException;
import com.polopoly.ps.pcmd.argument.ParameterHelp;
import com.polopoly.ps.pcmd.argument.Parameters;
import com.polopoly.ps.pcmd.parser.ExistingFileParser;
import com.polopoly.util.client.PolopolyContext;

public class RestoreDumpParameters implements Parameters {

    private File dumpFile;

    @Override
    public void parseParameters(Arguments args, PolopolyContext context) throws ArgumentException {
        try {
            dumpFile = args.getArgument(0, new ExistingFileParser());
        } catch (NotProvidedException e) {
            // fine. use default name.
        }
    }

    @Override
    public void getHelp(ParameterHelp help) {
        help.setArguments(new ExistingFileParser(), "The dump file to restore.");
    }

    public File getDumpFile() throws NotProvidedException {
        if (dumpFile == null) {
            throw new NotProvidedException("Dump file");
        }

        return dumpFile;
    }
}
