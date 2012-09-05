package com.polopoly.ps.ci.tool;

import java.util.ArrayList;
import java.util.List;

import com.polopoly.pcmd.tool.Tool;
import com.polopoly.ps.ci.Host;
import com.polopoly.ps.ci.TomcatController;
import com.polopoly.ps.ci.configuration.AbstractConfiguration.ConfigurationHostValue;
import com.polopoly.ps.ci.configuration.Configuration;
import com.polopoly.ps.ci.exception.CIException;

public class FrontTool extends TomcatTool implements Tool<ProcessParameters> {
	@Override
	protected Iterable<TomcatController> createControllers() {
		List<ConfigurationHostValue> frontHosts = new Configuration().getFrontHosts().getValue();

		if (frontHosts.isEmpty()) {
			throw new CIException("No front servers configured.");
		}

		List<TomcatController> result = new ArrayList<TomcatController>();

		for (ConfigurationHostValue frontHostConfValue : frontHosts) {
			Host frontHost = frontHostConfValue.getValue();

			result.add(new TomcatController(frontHost));
		}

		return result;
	}
}
