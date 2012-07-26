package com.polopoly.ps.ci;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.polopoly.ps.ci.configuration.Configuration;
import com.polopoly.ps.ci.configuration.JbossDirectories;
import com.polopoly.ps.ci.exception.CIException;
import com.polopoly.ps.ci.exception.NoSuchFileException;

public class JBossReinstaller {

    public interface StringTransformer {

        String transform(String line);

    }

    public void reinstall() {
        try {
            boolean success = false;

            final String mysqlUser = new Configuration().getDatabaseUser().getValue();
            final String mysqlPassword = new Configuration().getDatabasePassword().getValue();
            final String mysqlSchema = new Configuration().getDatabaseSchema().getValue();
            final String mysqlHost = new Configuration().getDatabaseHost().getValue();

            File jbossDistribution = new JBossDistribution().getJBossDistribution();

            File mySQLConnectorFile = new MySQLConnectorDistribution().getMySQLConnectorFile();

            File jbossDir = new JbossDirectories().getJbossDirectory(false);

            if (!jbossDir.getParentFile().exists()) {
                new DirectoryUtil().createDirectory(jbossDir.getParentFile());
            }

            if (jbossDir.exists()) {
                new DirectoryUtil().clearDirectory(jbossDir);
                jbossDir.delete();
            }

            try {
                new Executor("unzip -o " + jbossDistribution.getAbsoluteFile()).setDirectory(jbossDir.getParentFile()).execute();

                new Executor("mv jboss-4.0.5.GA " + jbossDir.getName()).setDirectory(jbossDir.getParentFile()).execute();

                try {
                    new JbossDirectories().getJbossPolopolyDeployDirectory();
                    new JbossDirectories().getJbossServerLibDirectory();
                } catch (NoSuchFileException e) {
                    throw new CIException("There seems to be something wrong with the Jboss distribution "
                            + jbossDistribution.getAbsoluteFile() + ": " + e.getMessage());
                }

                new Executor("cp " + mySQLConnectorFile.getAbsolutePath() + " "
                        + new JbossDirectories().getJbossServerLibDirectory().getAbsolutePath()).execute();

                writePatchedFile(new File(new JbossDirectories().getJbossDeployDirectory(), "ear-deployer.xml"));
                writePatchedFile(new File(new JbossDirectories().getJbossDeployDirectory(),
                        "jbossweb-tomcat55.sar/META-INF/jboss-service.xml"));

                writePatchedFile(new File(new JbossDirectories().getJbossDeployDirectory(), "jms/mysql-jdbc2-service.xml"));

                new Executor("rm " + new File(new JbossDirectories().getJbossDeployDirectory(), "jms/hsqldb-jdbc2-service.xml"))
                        .execute();

                writePatchedFile(new File(new JbossDirectories().getJbossPolopolyDeployDirectory(), "polopoly-service.xml"));
                // Remove this file from old, incorrect deploy dir
                new DirectoryUtil().deleteDirectory(new JbossDirectories().getJbossDeployDirectory(), "polopoly-service.xml");

                writePatchedFile(new File(new JbossDirectories().getJbossConfDirectory(), "log4j.xml"));

                writePatchedFile(new File(new JbossDirectories().getJbossPolopolyDeployDirectory(), "polopolyds-ds.xml"),
                        new StringTransformer() {

                            @Override
                            public String transform(String line) {
								return line.replace("cmuser", mysqlUser).replace("cmpasswd", mysqlPassword)
                                        .replace("mysql://localhost:3306/mvntemplate", "mysql://"+mysqlHost+":3306/" + mysqlSchema);
                            }
                        });
                // Remove this file from old, incorrect deploy dir
                new DirectoryUtil().deleteDirectory(new JbossDirectories().getJbossDeployDirectory(), "polopolyds-ds.xml");

                writePatchedFile(new File(new JbossDirectories().getJbossDeployDirectory(), "jbossweb-tomcat55.sar/context.xml"));
                writePatchedFile(new File(new JbossDirectories().getJbossDeployDirectory(), "jbossweb-tomcat55.sar/server.xml"));
                writePatchedFile(new File(new JbossDirectories().getJbossDeployDirectory(), "jbossweb-tomcat55.sar/conf/web.xml"));

                success = true;
            } finally {
                if (!success) {
                    System.out.println("Since patching failed, deleting JBoss directory since it is likely to be broken.");
                    new DirectoryUtil().clearDirectory(jbossDir);
                }
            }
        } catch (CIException e) {
            throw new CIException("While reinstalling JBoss: " + e.getMessage(), e);
        }
    }

    private void writePatchedFile(File file, StringTransformer... transformers) {
        System.out.println("Patching " + file.getAbsolutePath() + "...");

        InputStream fromIS = getClass().getResourceAsStream("/jbosspatch/" + file.getName());

        try {
            BufferedReader from = new BufferedReader(new InputStreamReader(fromIS, "UTF-8"));
            Writer to = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");

            String line;

            while ((line = from.readLine()) != null) {
                for (StringTransformer transformer : transformers) {
                    line = transformer.transform(line);
                }

                to.write(line);
                to.write("\n");
            }

            to.close();
        } catch (IOException e) {
            throw new CIException("While patching " + file + ": " + e.getMessage(), e);
        }
    }
}
