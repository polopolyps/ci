package com.polopoly.ps.ci.tool;

import com.polopoly.pcmd.tool.Tool;
import com.polopoly.ps.ci.Builder;
import com.polopoly.ps.ci.MavenVersionSanityChecker;
import com.polopoly.ps.pcmd.FatalToolException;
import com.polopoly.util.client.PolopolyContext;

public class BuildTool implements Tool<BuildParameters> {

    @Override
    public BuildParameters createParameters() {
        return new BuildParameters();
    }

    @Override
    public void execute(PolopolyContext arg0, BuildParameters parameters) throws FatalToolException {
        new MavenVersionSanityChecker().sanityCheckMavenVersion();

        Builder builder = new Builder();
        builder.setProject(parameters.getProject());
        builder.installAll(false);
    }

    @Override
    public String getHelp() {
        return "Runs mvn install on the project code.";
    }

}
