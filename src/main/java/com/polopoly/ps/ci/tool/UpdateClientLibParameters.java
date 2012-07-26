package com.polopoly.ps.ci.tool;

import com.polopoly.ps.pcmd.argument.ArgumentException;
import com.polopoly.ps.pcmd.argument.Arguments;
import com.polopoly.ps.pcmd.argument.ParameterHelp;
import com.polopoly.ps.pcmd.argument.Parameters;
import com.polopoly.ps.pcmd.parser.BooleanParser;
import com.polopoly.util.client.PolopolyContext;

public class UpdateClientLibParameters implements Parameters {
    private boolean compile;
    private boolean clean;
    private boolean backup;
    
	@Override
    public void getHelp(ParameterHelp help) {
        help.addOption("compile", new BooleanParser(), "Whether to build the project first. Defaults to false.");
        help.addOption("clean", new BooleanParser(), "If compiling, whether to clean beore compiling. Defaults to false.");
        help.addOption("backup", new BooleanParser(), "Whether to backup the existing custom directory.");
    }

    @Override
    public void parseParameters(Arguments arguments, PolopolyContext context) throws ArgumentException {
        setCompile(arguments.getFlag("compile", false));
        setClean(arguments.getFlag("clean", false));
        setBackup(arguments.getFlag("backup", false));
    }

    public void setClean(boolean clean) {
        this.clean = clean;
    }

    public boolean isClean() {
        return clean;
    }

    public void setCompile(boolean compile) {
        this.compile = compile;
    }

    public boolean isCompile() {
        return compile;
    }

    public boolean isBackup() {
		return backup;
	}

	public void setBackup(boolean backup) {
		this.backup = backup;
	}

}
