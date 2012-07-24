package com.polopoly.ps.ci;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;

public class AbstractDistribution {
    protected String getError(HttpMethodBase method) {
        try {
            return method.getResponseBodyAsString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    protected void downloadToFile(InputStream in, File file, long contentLength) throws IOException {
        File tempFile = new File(file.getAbsolutePath() + ".tmp");
        OutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile));

        int i;
        int count = 0;
        boolean success = false;

        try {
            while ((i = in.read()) != -1) {
                out.write(i);

                if (count == 0) {
                    System.out.println("Download of " + file.getAbsolutePath() + " started (" + (contentLength / 1024)
                            + " kb)...");
                }

                if (++count % (1024 * 1024 * 5) == 0) {
                    System.out
                            .println("Downloaded " + (count / 1024 / 1024) + " Mb (" + (100L * count / contentLength) + "%)...");
                }
            }

            success = true;

            file.delete();
            tempFile.renameTo(file);

            System.out.println("Got " + count + " bytes");
        } finally {
            if (!success) {
                file.delete();
            }
        }

        out.close();
    }

    protected long getContentLength(GetMethod get) {
        Header header = get.getResponseHeader("Content-Length");

        if (header == null) {
            return 1;
        }

        try {
            return Integer.parseInt(header.getValue());
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
