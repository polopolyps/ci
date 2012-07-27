package com.polopoly.ps.ci.tool;

import java.io.File;
import java.util.Scanner;

import com.polopoly.pcmd.tool.Tool;
import com.polopoly.ps.ci.ClientLibUpdater;
import com.polopoly.ps.ci.CopyUtil;
import com.polopoly.ps.ci.DirectoryUtil;
import com.polopoly.ps.ci.Executor;
import com.polopoly.ps.ci.Host;
import com.polopoly.ps.ci.ProcessUtil;
import com.polopoly.ps.ci.Require;
import com.polopoly.ps.ci.TomcatController;
import com.polopoly.ps.ci.configuration.Configuration;
import com.polopoly.ps.ci.configuration.PolopolyDirectories;
import com.polopoly.ps.ci.exception.CIException;
import com.polopoly.ps.pcmd.FatalToolException;
import com.polopoly.ps.pcmd.tool.DoesNotRequireRunningPolopoly;
import com.polopoly.util.client.PolopolyContext;

public class DeployTool implements Tool<DeployParameters>, DoesNotRequireRunningPolopoly {
	private BackupId backupId = new BackupId();

	private File clientLibWebInfLib;
	private File deployDirectory;

	@Override
	public DeployParameters createParameters() {
		return new DeployParameters();
	}

	@Override
	public void execute(PolopolyContext context, DeployParameters parameters) throws FatalToolException {
		File projectHome = new Configuration().getProjectHomeDirectory().getValue();

		deployDirectory = new File(projectHome, "/deploy");

		if (parameters.isBuild()) {
			new DirectoryUtil().ensureDirectoryExistsAndIsEmpty(deployDirectory);

			System.out.println("Building the code...");

			new Executor("mvn clean install -DskipTests").setDirectory(projectHome).execute();

			System.out.println("Generating WARs...");

			clientLibWebInfLib = new File(Require.require(System.getProperty("java.io.tmpdir")), "WEB-INF/lib");

			new DirectoryUtil().ensureDirectoryExistsAndIsEmpty(clientLibWebInfLib);

			new Executor("cp " + new PolopolyDirectories().getContainerClientlibDirectory().getAbsolutePath()
					+ "/*.jar " + clientLibWebInfLib.getAbsolutePath()).execute();

			handleWebappModule(new Configuration().getWebPomDirectory().getValue());
			handleWebappModule(new Configuration().getGuiPomDirectory().getValue());
		} else {
			assertBuildWarExists("ROOT.war");
			assertBuildWarExists("polopoly.war");
			assertBuildWarExists("front.war");
		}

		if (new Configuration().isRunInNitro()) {
			throw new FatalToolException(
					"Deployment is not possible when running in Nitro. An external JBoss and web server is needed.");
		}

		Host frontHost = new Configuration().getFrontHost().getValue();

		TomcatController controller = new TomcatController();

		System.out.println("Making sure caches are warm by pinging servers...");

		warmUpCaches(controller);

		TomcatController frontController = new TomcatController(frontHost);

		warmUpCaches(frontController);

		System.out.println("Shutting down GUI server...");

		new TomcatController().kill();

		if (!frontHost.isLocalHost()) {
			System.out.println("Shutting down front server...");

			frontController.kill();
		}

		System.out.println("Shutting down index server...");

		new ProcessUtil().stopIndexServer();

		// TODO: other processes (stats, poll etc) missing.

		// TODO this will unnecessarily assemble the webapp again if
		// webapp-dispatcher is the clientlib.

		if (parameters.isUpdateClientLib()) {
			new ClientLibUpdater(backupId).updateClientLib(true);
		}

		Host indexServerHost = new Configuration().getIndexServerHost().getValue();

		if (!indexServerHost.isLocalHost()) {
			new ClientLibUpdater(backupId, indexServerHost).updateClientLib(false);
		}

		System.out.println("Starting index server...");

		new ProcessUtil().startIndexServer();

		System.out.println("Importing project content...");

		try {
			new ImportProjectContentTool().execute(context, new ImportProjectContentParameters());
		} catch (FatalToolException e) {
			System.err.println("Content import failed: " + e + " Do you wish to proceed anyway (y/n)? ");

			Scanner in = new Scanner(System.in);

			String input;
			do {
				input = in.nextLine().toLowerCase();
			} while (!input.startsWith("y") && !input.startsWith("n"));

			if (input.startsWith("n")) {
				throw e;
			}
		}

		File webappsDirectory = new File(new Configuration().getTomcatDirectory().getValue(), "webapps");

		deploy(new File(deployDirectory, "ROOT.war"), webappsDirectory, new Host());
		deploy(new File(deployDirectory, "polopoly.war"), webappsDirectory, new Host());

		// the front.war must be called ROOT.war on the front. this is
		// unfortunately the easiest way of doing that copy.
		File frontTmpDir = new File(deployDirectory, "front_tmp");
		new DirectoryUtil().createDirectory(frontTmpDir);

		File frontTmpWar = new File(frontTmpDir, "ROOT.war");
		new Executor("cp " + new File(deployDirectory, "front.war").getAbsolutePath() + " "
				+ frontTmpWar.getAbsolutePath()).execute();
		deploy(frontTmpWar, webappsDirectory, frontHost);

		new DirectoryUtil().deleteDirectory(frontTmpDir);

		new TomcatController().start();

		if (!frontHost.isLocalHost()) {
			frontController.start();
		}

		System.out.println("Deployment done.");
	}

	private void assertBuildWarExists(String warName) {
		File warFile = new File(deployDirectory, warName);

		if (!warFile.exists()) {
			throw new FatalToolException("Build was skipped but the built WAR " + warFile + " did not exist.");
		}
	}

	private void warmUpCaches(TomcatController controller) {
		try {
			tolerateLongResponseTimesButNoExceptions(controller);
			controller.verifyServerResponding(null);
		} catch (CIException e) {
			System.out.println(e.getMessage() + " Web server at " + controller.getHost()
					+ " doesn«t seem to be running. Proceeding anyway...");
		}
	}

	private void tolerateLongResponseTimesButNoExceptions(TomcatController controller) {
		// if we get can't connect at all we don't wait but if the
		// connection hangs because
		// the cache is updating, we wait a long time.
		controller.setUrlTimeoutSeconds(2);
		controller.setSingleRequestTimeoutSeconds(600);
	}

	private void deploy(File war, File webappsDirectory, Host host) {
		File oldWar = new File(webappsDirectory, war.getName());

		if (new FileExistence(host).exists(oldWar)) {
			backupId.backupFile(oldWar, host, true);
		}

		// delete the unpacked directory; tomcat doesn't always recognize the
		// new WAR.
		new Executor("rm -rf " + webappsDirectory.getAbsolutePath() + "/" + stripExtension(war.getName()))
				.setHost(host).execute();

		new CopyUtil().copyFile(war, new Host(), webappsDirectory, host);
	}

	private String stripExtension(String name) {
		int i = name.lastIndexOf('.');

		return name.substring(0, i);
	}

	protected void handleWebappModule(File webappDir) {
		System.out.println("Building webapp project in " + webappDir.getAbsolutePath() + "...");

		buildWar(webappDir);

		int found = 0;

		File targetDir = new File(webappDir, "target");

		for (String war : targetDir.list()) {
			if (!war.endsWith(".war")) {
				continue;
			}

			found++;

			if (found == 2) {
				throw new FatalToolException("There were more than one WARs in the target directory of " + webappDir
						+ ".");
			}

			File warFile = new File(targetDir, war);

			addClientLibs(clientLibWebInfLib, warFile);
			moveWarToDeployDir(deployDirectory, warFile);

			if (war.equals("ROOT.war")) {
				moveWebInfFile("web.xml", "preview_web.xml", webappDir);
				moveWebInfFile("front_web.xml", "web.xml", webappDir);

				buildWar(webappDir);

				new Executor("mv " + war + " front.war").setDirectory(targetDir).execute();

				warFile = new File(targetDir, "front.war");

				addClientLibs(clientLibWebInfLib, warFile);
				moveWarToDeployDir(deployDirectory, warFile);

				moveWebInfFile("web.xml", "front_web.xml", webappDir);
				moveWebInfFile("preview_web.xml", "web.xml", webappDir);
			}
		}

		if (found == 0) {
			throw new FatalToolException("No WARs was generated in the target directory of " + webappDir + ".");
		}
	}

	protected void moveWarToDeployDir(File deployDirectory, File warFile) {
		new Executor("cp " + warFile.getAbsolutePath() + " " + deployDirectory.getAbsolutePath()).execute();
	}

	protected void addClientLibs(File clientLibWebInfLib, File warFile) {
		File tmpDir = clientLibWebInfLib.getParentFile().getParentFile();
		new Executor("zip -r " + warFile.getAbsolutePath() + " WEB-INF").setDirectory(tmpDir).execute();
	}

	protected void buildWar(File webappDir) {
		new Executor("mvn war:war").setDirectory(webappDir).execute();
	}

	protected void moveWebInfFile(String from, String to, File webappDir) {
		new Executor("mv src/main/webapp/WEB-INF/" + from + " " + "src/main/webapp/WEB-INF/" + to).setDirectory(
				webappDir).execute();
	}

	@Override
	public String getHelp() {
		return "Does a deployment of a new project code version";
	}

}
