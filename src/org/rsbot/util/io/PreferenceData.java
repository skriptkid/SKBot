package org.rsbot.util.io;

import org.rsbot.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class PreferenceData {
	private final int type;
	private final File file;

	public PreferenceData(final int type) {
		this.type = type;
		file = new File(Configuration.Paths.getSettingsDirectory() + File.separator + "pref2-" + type + ".dat");
		try {
			if (!file.exists()) {
				file.createNewFile();
				switch (type) {
					case 1:
						set(new byte[]{
								25, 0, 0, 4, 0, 1, 1, 1, 1, 2,
								1, 2, 0, 2, 2, 0, 1, 1, 0, 1,
								3, 1, 2, 2, 1, 0, 4, 5, 0, 127,
								127, 127, -4, -1, 1
						});
						break;
					case 2:
						set(new byte[]{
								1, 0, 21, 3, -38, 0, 0, 0, 1,
								3, -37, 0, 0, 0, 1, 3, -30, 0,
								0, 0, 2, 3, -28, -1, -1, -1,
								-1, 3, -27, -1, -1, -1, -1, 3,
								-26, -1, -1, -1, -1, 3, -25, -1,
								-1, -1, -1, 4, 11, 0, 0, 0, 74, 4,
								12, 0, 0, 0, 74, 4, 13, 0, 0,
								0, -114, 4, 28, -1, -1, -1, -1,
								4, -40, 0, 0, 0, 3, 4, -6,
								0, 0, 0, 67, 4, -3, 0, 0, 0,
								0, 5, 35, -1, -1, -1, -1, 5,
								36, -1, -1, -1, -1, 5, -122,
								0, 0, 0, 1, 5, -115, -1, -1, -1,
								-1, 5, -107, -1, -1, -1, -1, 5,
								-31, -1, -1, -1, -1, 5, -30, -1,
								-1, -1, -1
						});
						break;
				}
			}
		} catch (final IOException ignored) {
		}
	}

	public byte[] get() {
		try {
			final RandomAccessFile raf = new RandomAccessFile(file, "rw");
			final byte[] b = new byte[(int) raf.length()];
			raf.readFully(b);
			return checkPrefs(b);
		} catch (final IOException ioe) {
			return new byte[0];
		}
	}

	public void set(byte[] data) {
		data = checkPrefs(data);
		try {
			final RandomAccessFile raf = new RandomAccessFile(file, "rw");
			raf.write(data);
		} catch (final IOException ignored) {
		}
	}

	private byte[] checkPrefs(final byte[] data) {
		switch (type) {
			case 1: {
				if (data.length <= 40) {
					break;
				}
				data[19] = 1; //Graphics Mode
			}
		}
		return data;
	}
}
