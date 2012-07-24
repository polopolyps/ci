package com.polopoly.ps.ci.tool;

import com.polopoly.pcmd.tool.Tool;
import com.polopoly.ps.ci.Builder;
import com.polopoly.ps.ci.ClientLibUpdater;
import com.polopoly.ps.ci.EmptyBackupId;
import com.polopoly.ps.ci.Host;
import com.polopoly.ps.ci.configuration.Configuration;
import com.polopoly.ps.pcmd.FatalToolException;
import com.polopoly.ps.pcmd.tool.DoesNotRequireRunningPolopoly;
import com.polopoly.util.client.PolopolyContext;

public class UpdateClientLibTool implements Tool<UpdateClientLibParameters>, DoesNotRequireRunningPolopoly {

    @Override
    public UpdateClientLibParameters createParameters() {
        return new UpdateClientLibParameters();
    }

    @Override
    public void execute(PolopolyContext context, UpdateClientLibParameters parameters) throws FatalToolException {
        if (parameters.isCompile()) {
            new Builder().installAll(parameters.isClean());
        }
        
        BackupId backupId;
        
		if (parameters.isBackup()) {
        	backupId = new BackupId();
        }
        else {
        	 backupId = new EmptyBackupId();
        }
        
        new ClientLibUpdater(backupId).updateClientLib(true);

        Host indexServerHost = new Configuration().getIndexServerHost().getValue();
        
        if (!indexServerHost.isLocalHost()) {
            new ClientLibUpdater(backupId, indexServerHost).updateClientLib(false);
        }
    }

    @Override
    public String getHelp() {
        return "Updates the custom/config and custom/client-lib directories in "
                + "Polopoly with the assembly of the client-lib project.";
    }

}
