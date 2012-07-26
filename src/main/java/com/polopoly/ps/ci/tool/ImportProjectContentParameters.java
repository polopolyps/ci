package com.polopoly.ps.ci.tool;

import com.polopoly.ps.pcmd.argument.ArgumentException;
import com.polopoly.ps.pcmd.argument.Arguments;
import com.polopoly.ps.pcmd.argument.ParameterHelp;
import com.polopoly.ps.pcmd.argument.Parameters;
import com.polopoly.ps.pcmd.parser.BooleanParser;
import com.polopoly.util.client.PolopolyContext;

public class ImportProjectContentParameters implements Parameters {

	private boolean includeTestContent;
	private boolean demoData;
	private boolean force;

	@Override
	public void parseParameters(Arguments args, PolopolyContext context) throws ArgumentException {
		setIncludeTestContent(args.getFlag("testcontent", false));
		setDemoData(args.getFlag("demodata", false));
		setForce(args.getFlag("force", false));
	}

	@Override
	public void getHelp(ParameterHelp help) {
		help.addOption("testcontent", new BooleanParser(), "Whether to also import test content (defaults to false).");
		help.addOption("demodata", new BooleanParser(),
				"Whether to import demo data rather than the main project content (defaults to false).");
		help.addOption("force", new BooleanParser(),
				"Whether to force a reimport of all data, independent of whether it has changed or not.");
	}

	public void setIncludeTestContent(boolean includeTestContent) {
		this.includeTestContent = includeTestContent;
	}

	public boolean isIncludeTestContent() {
		return includeTestContent;
	}

	public void setDemoData(boolean demoData) {
		this.demoData = demoData;
	}

	public boolean isDemoData() {
		return demoData;
	}

	public boolean isForce() {
		return force;
	}

	public void setForce(boolean force) {
		this.force = force;
	}
}
