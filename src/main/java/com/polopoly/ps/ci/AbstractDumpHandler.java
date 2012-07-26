package com.polopoly.ps.ci;

import java.io.File;

public abstract class AbstractDumpHandler {
    protected File getSqlDumpFile() {
        return new File(getWorkDirectory(), "db/mysqldump.sql");
    }

    protected abstract File getWorkDirectory();

    protected File getIndexWorkDirectory() {
        return new File(getWorkDirectory(), "indexes");
    }

    protected File getSolrWorkDirectory() {
        return new File(getWorkDirectory(), "solr");
    }
}
