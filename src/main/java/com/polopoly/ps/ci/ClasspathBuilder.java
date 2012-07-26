package com.polopoly.ps.ci;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class ClasspathBuilder {

    protected List<File> jarsToLoad = new ArrayList<File>();
    protected List<File> classDirectoriesToLoad = new ArrayList<File>();

    protected void addJarFile(File jarFile) {
        jarsToLoad.add(jarFile);
    }

    public void addClassDirectory(File classDirectory) {
        classDirectoriesToLoad.add(classDirectory);
    }

    public void addJarDirectory(File directory) {
        for (File fileInDirectory : directory.listFiles()) {
            if (fileInDirectory.getName().endsWith(".jar")) {
                addJarFile(fileInDirectory);
            }
        }
    }

    public void load() {
        for (File jar : getJarsToLoad()) {
            load(jar);
        }

        jarsToLoad.clear();

        for (File classDirectory : classDirectoriesToLoad) {
            load(classDirectory);
        }

        classDirectoriesToLoad.clear();
    }

    public List<File> getJarsToLoad() {
        return jarsToLoad;
    }

    protected void load(File file) {
        VerboseLogging.log("Classpath: " + file.getAbsolutePath());

        try {
            URL url;

            if (!file.isDirectory()) {
                url = new URL("jar:file://" + file.getAbsolutePath() + "!/");
            } else {
                url = new URL("file://" + file.getAbsolutePath() + "/");
            }

            addURL(url);
        } catch (MalformedURLException e) {
            System.out.println("Could not add " + file + " to the classpath: " + e);
        }
    }

    public void addURL(URL u) {
        URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        URL[] urls = sysLoader.getURLs();
        for (int i = 0; i < urls.length; i++) {
            if (u.toString().equalsIgnoreCase(urls[i].toString())) {
                return;
            }
        }

        Class<URLClassLoader> sysclass = URLClassLoader.class;

        try {
            Method method = sysclass.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(sysLoader, new Object[] { u });
        } catch (Throwable t) {
            System.err.println("Could not add " + u + " to system classloader: " + t);
        }
    }
}
