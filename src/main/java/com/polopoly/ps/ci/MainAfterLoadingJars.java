package com.polopoly.ps.ci;

import static com.polopoly.ps.ci.Require.require;

import com.polopoly.ps.ci.configuration.AbstractConfiguration;
import com.polopoly.ps.ci.configuration.Configuration;
import com.polopoly.ps.ci.exception.NotConfiguredException;
import com.polopoly.ps.ci.tool.PolopolyTool;
import com.polopoly.ps.pcmd.argument.ArgumentException;
import com.polopoly.ps.pcmd.argument.CommandLineArgumentParser;
import com.polopoly.ps.pcmd.argument.DefaultArguments;
import com.polopoly.ps.pcmd.argument.NotProvidedException;
import com.polopoly.ps.pcmd.parser.ExistingDirectoryParser;
import com.polopoly.ps.pcmd.parser.ParseException;
import com.polopoly.ps.pcmd.tool.DoesNotRequireRunningPolopoly;
import com.polopoly.ps.pcmd.util.ToolRetriever;
import com.polopoly.ps.pcmd.util.ToolRetriever.NoSuchToolException;

/**
 * Called after Main has loaded the polopoly and pcmd JARs. We can't link to
 * PCMD classes before.
 */
public class MainAfterLoadingJars {
    private String[] args;
    private PolopolyJarLoader jarLoader;

    public MainAfterLoadingJars(String[] args, PolopolyJarLoader jarLoader) {
        this.args = require(args);
        this.jarLoader = require(jarLoader);
    }

    public void main() {
        ToolRetriever.addToolsPackage(PolopolyTool.class.getPackage().getName());

        DefaultArguments arguments = null;

        try {
            arguments = new CommandLineArgumentParser().parse(args);
        } catch (ArgumentException e) {
            System.err.println("Invalid parameters: " + e);
            System.exit(1);
        }

        VerboseLogging.setVerbose(isVerbose(arguments));

        try {
            AbstractConfiguration.setHomeDirectory(arguments.getOption("home", new ExistingDirectoryParser()));
        } catch (NotProvidedException e) {
            // fine. will use current directory.
        } catch (ArgumentException e) {
            System.err.println("Could not parse home parameter: " + e);
        }

        try {
            if (!(ToolRetriever.getTool(arguments.getToolName()) instanceof DoesNotRequireRunningPolopoly)) {
                setLoginData(arguments);
                loadProjectClasspath(arguments);
            }
            else {
            	new PolopolyJarLoader().loadJbossJars();
            }
        } catch (NotProvidedException e) {
            // ignore. it will be reported later.
        } catch (NoSuchToolException e) {
            setLoginData(arguments);
            // load the classpath; the tool could be in there.
            loadProjectClasspath(arguments);
        }

        com.polopoly.ps.pcmd.Main.main(arguments);
    }

    private void setLoginData(DefaultArguments arguments) {
        try {
            arguments.getOptionString("loginname");
        } catch (NotProvidedException e) {
            try {
                String sysadminPassword = new Configuration().getPolopolySysadminPassword().getValue();
                arguments.setOptionString("loginname", "sysadmin");
                arguments.setOptionString("loginpassword", sysadminPassword);
            } catch (NotConfiguredException e2) {
                // fine. cannot log in.
            }
        }
    }

    protected void loadProjectClasspath(DefaultArguments arguments) {
        jarLoader.loadJarsNeededToConnect();
        jarLoader.loadProjectClasspath(isTestScope(arguments));
        jarLoader.sanityCheckClasspath();
    }

    private boolean isVerbose(DefaultArguments arguments) {
        boolean verbose;

        try {
            verbose = arguments.getFlag("verbose", false);
        } catch (ParseException e) {
            System.err.println("Invalid verbose parameter. Assuming true: " + e);
            verbose = true;
        }
        return verbose;
    }

    private boolean isTestScope(DefaultArguments arguments) {
        try {
            return arguments.getFlag("testscope", false);
        } catch (ParseException e) {
            System.err.println("Invalid testscope parameter. Assuming true: " + e);
            return true;
        }
    }

}
