package com.polopoly.ps.ci.configuration;

import java.io.File;

import com.polopoly.ps.ci.LocateDirectoryContaining;
import com.polopoly.ps.ci.exception.NoDirectoryFoundException;

public class JettyDirectories {

    public File getBuiltWebappDirectory() throws NoDirectoryFoundException {
        File guiPomDir = new Configuration().getGuiPomDirectory().getValue();
		File target = new File(guiPomDir, "target");
        
        if (!target.exists()) {
        	throw new NoDirectoryFoundException("Found no target directory in " + guiPomDir);
        }
        
        return new LocateDirectoryContaining().locateDirectoryContaining(target, "WEB-INF");
    }
}
