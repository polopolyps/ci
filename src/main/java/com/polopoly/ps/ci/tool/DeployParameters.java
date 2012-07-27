package com.polopoly.ps.ci.tool;

import com.polopoly.ps.pcmd.argument.ArgumentException;
import com.polopoly.ps.pcmd.argument.Arguments;
import com.polopoly.ps.pcmd.argument.ParameterHelp;
import com.polopoly.ps.pcmd.argument.Parameters;
import com.polopoly.ps.pcmd.parser.BooleanParser;
import com.polopoly.util.client.PolopolyContext;

public class DeployParameters implements Parameters {

	private boolean build = true;
	private boolean updateClientLib = true;

	@Override
	public void getHelp(ParameterHelp help) {
		help.addOption("build", new BooleanParser(), "Whether to first compile the code and generate the WARs "
				+ "(defaults to true; only set to false to continue an interrupted deploy).");
		help.addOption(
				"update-client-lib",
				new BooleanParser(),
				"Whether to update custom/client-lib "
						+ "(defaults to true; only set to false if the clientlib has already been updated with the current code).");
	}

	@Override
	public void parseParameters(Arguments args, PolopolyContext context) throws ArgumentException {
		build = args.getFlag("build", build);
		updateClientLib = args.getFlag("update-client-lib", updateClientLib);
	}

	public boolean isBuild() {
		return build;
	}

	public boolean isUpdateClientLib() {
		return updateClientLib;
	}

}
