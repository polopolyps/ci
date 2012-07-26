package com.polopoly.ps.ci.tool;

import com.polopoly.pcmd.tool.Tool;
import com.polopoly.ps.ci.TomcatController;
import com.polopoly.ps.ci.configuration.Configuration;

public class FrontTool extends TomcatTool implements Tool<ProcessParameters> {
	protected TomcatController createController() {
		return new TomcatController(new Configuration().getFrontHost().getValue());
	}
}
