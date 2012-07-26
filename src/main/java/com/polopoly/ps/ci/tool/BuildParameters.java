package com.polopoly.ps.ci.tool;

import com.polopoly.ps.pcmd.argument.ArgumentException;
import com.polopoly.ps.pcmd.argument.Arguments;
import com.polopoly.ps.pcmd.argument.NotProvidedException;
import com.polopoly.ps.pcmd.argument.ParameterHelp;
import com.polopoly.ps.pcmd.argument.Parameters;
import com.polopoly.util.client.PolopolyContext;

public class BuildParameters implements Parameters {
    private String project;

    @Override
    public void parseParameters(Arguments args, PolopolyContext context) throws ArgumentException {
        try {
            setProject(args.getArgument(0));
        } catch (NotProvidedException e) {
            // optional
        }
    }

    @Override
    public void getHelp(ParameterHelp help) {
        help.setArguments(null, "The project to build (optional, a directory name evaluated relative to the project root). "
                + "Expected to contain a pom file.");
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }
}
