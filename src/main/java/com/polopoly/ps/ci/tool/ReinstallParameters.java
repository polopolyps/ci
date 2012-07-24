package com.polopoly.ps.ci.tool;

import com.polopoly.ps.pcmd.argument.ArgumentException;
import com.polopoly.ps.pcmd.argument.Arguments;
import com.polopoly.ps.pcmd.argument.ParameterHelp;
import com.polopoly.ps.pcmd.argument.Parameters;
import com.polopoly.ps.pcmd.parser.BooleanParser;
import com.polopoly.util.client.PolopolyContext;

public class ReinstallParameters implements Parameters {
    private boolean full;
    private boolean build;
    private boolean dump;

    @Override
    public void parseParameters(Arguments args, PolopolyContext context) throws ArgumentException {
        full = args.getFlag("full", false);
        build = args.getFlag("build", true);
        setDump(args.getFlag("dump", true));
    }

    @Override
    public void getHelp(ParameterHelp help) {
        help.addOption("full", new BooleanParser(),
                "Whether to force a full reinstall as opposed to restoring the clean dump (if it exists).");
        help.addOption("build", new BooleanParser(), "Whether to build the project first (set to false if it is already built).");
        help.addOption("dump", new BooleanParser(),
                "Whether to create a dump of the clean install to speed up future reinstalls.");
    }

    public boolean isFullReinstall() {
        return full;
    }

    public boolean isBuild() {
        return build;
    }

    public void setDump(boolean dump) {
        this.dump = dump;
    }

    public boolean isDump() {
        return dump;
    }

}
