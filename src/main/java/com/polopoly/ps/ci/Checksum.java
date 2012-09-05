package com.polopoly.ps.ci;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;

public class Checksum {
	private static final Logger LOGGER = Logger.getLogger(Checksum.class.getName());
	private static final int BUF_SIZE = 1024 * 64;
	private long checksum;

	public void add(String string) {
		checksum = 7 * checksum + string.hashCode();
	}

	public void add(File file) {
		checksum = 7 * checksum + getChecksum(file);
	}

	public long getChecksum(File file) {
		CRC32 checksum = new CRC32();

		byte[] buffer = new byte[BUF_SIZE];

		try {
			InputStream inputStream = new FileInputStream(file);

			int readBytes;

			do {
				readBytes = inputStream.read(buffer);

				if (readBytes > 0) {
					checksum.update(buffer, 0, readBytes);
				}
			} while (readBytes == BUF_SIZE);

			inputStream.close();

			return checksum.getValue();
		} catch (FileNotFoundException e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);

			return 4711;
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);

			return 4711;
		}
	}

	@Override
	public String toString() {
		return Long.toString(checksum);
	}
}
