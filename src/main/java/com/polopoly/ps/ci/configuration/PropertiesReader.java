package com.polopoly.ps.ci.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class PropertiesReader {

    private File file;

    public PropertiesReader(File file) {
        this.file = file;
    }

    public Properties read() {
        Properties properties = new Properties();

        try {
            FileInputStream in = new FileInputStream(file);
            properties.load(in);
        } catch (FileNotFoundException e) {
            System.err.println("Could not locate properties file " + file.getAbsolutePath() + ". Using environment variables.");
        } catch (IOException e) {
            System.err.println("Could not read properties file " + file.getAbsolutePath() + ": " + e);
        }

        return properties;
    }
}
