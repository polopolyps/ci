package com.polopoly.ps.ci;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.polopoly.ps.ci.exception.CIException;
import com.polopoly.ps.ci.exception.NoSuchJarException;

public class NonDuplicatingClasspathBuilder extends ClasspathBuilder {
    private Set<File> duplicates = new HashSet<File>();

    private Map<String, String> latestVersionByJar = new HashMap<String, String>();

    private Map<String, File> fileByName = new HashMap<String, File>();

    public void removeActuallyUsedJar(File file) {
        try {
            VersionedJar versionedJar = new VersionedJar(file);

            String actuallyUsedVersion = getVersion(versionedJar.getJarWithoutVersion());
            String loadedVersion = versionedJar.getVersion();

            if (!actuallyUsedVersion.equals(loadedVersion)) {
                VerboseLogging.log("The CI scripts use " + versionedJar.getJarWithoutVersion() + " in the version "
                        + toVersionString(actuallyUsedVersion) + ". The project declares " + toVersionString(loadedVersion)
                        + ", but the CI script version takes precendence. " + "To change version, update the CI scripts' POM.");

                removeAllVersions(versionedJar.getJarWithoutVersion());
            }
        } catch (NotAJarException e) {
            // fine. nothing to do.
        } catch (NoSuchJarException e) {
            // fine. not in jars to be loaded.
        }
    }

    protected void addJarFile(File file) {
        VersionedJar versionedJar;

        try {
            versionedJar = new VersionedJar(file);
        } catch (NotAJarException e) {
            return;
        }

        String jar = versionedJar.getJarWithoutVersion();
        String version = versionedJar.getVersion();

        String otherVersion = latestVersionByJar.get(jar);
        fileByName.put(file.getName(), file);

        if (otherVersion != null) {
            String deleteVersion;

            // we need to know all versions so we prefer versioned files to
            // unversioned. we can't tell which is newer anyway.
            if (version.equals("") || version.compareTo(otherVersion) < 0) {
                deleteVersion = version;
            } else {
                deleteVersion = otherVersion;
                latestVersionByJar.put(jar, version);
                jarsToLoad.add(file);
            }

            if (!version.equals(otherVersion) && !(jar.equals("polopoly") && otherVersion.equals(""))) {
                VerboseLogging.log("WARNING: There are two versions of the JAR " + jar + ": " + toVersionString(otherVersion)
                        + " and " + toVersionString(version) + ". " + toVersionString(deleteVersion) + " is probably oldest.");
            }

            String fileName = getFileName(jar, deleteVersion);

            File duplicateFile = fileByName.get(fileName);

            if (duplicateFile != null) {
                duplicates.add(duplicateFile);
                jarsToLoad.remove(duplicateFile);
            } else {
                throw new CIException("Internal error: Could not find duplicate jar " + fileName);
            }

        } else {
            latestVersionByJar.put(jar, version);
            jarsToLoad.add(file);
        }
    }

    private String getFileName(String jar, String deleteVersion) {
        return jar + (deleteVersion.equals("") ? "" : "-" + deleteVersion) + ".jar";
    }

    private String toVersionString(String version) {
        if (version.equals("")) {
            return "<unspecified version>";
        } else {
            return version;
        }
    }

    public Set<File> getDuplicates() {
        return duplicates;
    }

    public String getVersion(String jar) throws NoSuchJarException {
        String result = latestVersionByJar.get(jar);

        if (result == null) {
            throw new NoSuchJarException();
        }

        return result;
    }

    public boolean containsAnyVersion(String jar) {
        return latestVersionByJar.containsKey(jar);
    }

    public void removeAllVersions(String jarName) {
        try {
            File file = fileByName.get(getFileName(jarName, getVersion(jarName)));

            if (file == null) {
                throw new CIException("Internal error: Could not find jar " + jarName + " in version " + getVersion(jarName));
            }

            duplicates.add(file);
        } catch (NoSuchJarException e) {
            // nothing to remove.
        }

    }
}
