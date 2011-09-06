package org.rsbot.script.provider;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.rsbot.Configuration;
import org.rsbot.service.Preferences;
import org.rsbot.util.StringUtil;
import org.rsbot.util.io.HttpClient;
import org.rsbot.util.io.IOHelper;

public final class ScriptUserList {
	private static ScriptUserList instance = null;
	private boolean available = false;
	private URL base;
	private final File cache = new File(Configuration.Paths.getCacheDirectory(), "sdn-user.txt");
	private String lastUser;
	List<String> list;
	public boolean enabled = true;

	public static ScriptUserList getInstance() {
		if (instance == null) {
			instance = new ScriptUserList();
		}
		return instance;
	}

	private ScriptUserList() {
		try {
			base = HttpClient.getFinalURL(new URL(Configuration.Paths.URLs.SDN_USER));
			available = true;
		} catch (final IOException ignored) {
		}
		lastUser = Preferences.getInstance().sdnUser;
		update();
	}

	public boolean isAvailable() {
		return available;
	}

	public boolean update() {
		if (lastUser == null || !lastUser.equalsIgnoreCase(Preferences.getInstance().sdnUser)) {
			if (cache.exists()) {
				cache.delete();
			}
		}
		try {
			lastUser = Preferences.getInstance().sdnUser;
			if (lastUser == null || lastUser.length() == 0) {
				return false;
			}
			final URL url = new URL(base, "?u=" + StringUtil.urlEncode(lastUser.toLowerCase()));
			HttpClient.download(url, cache);
		} catch (final IOException ignored) {
			available = false;
		}
		loadList();
		return available && list != null;
	}

	private void loadList() {
		if (!cache.canRead()) {
			list = null;
			return;
		}
		list = new ArrayList<String>(8);
		final String text = IOHelper.readString(cache);
		final String[] items = text.split("\n");
		for (final String item : items) {
			list.add(item);
		}
	}

	public boolean isSelected() {
		return enabled && list != null;
	}

	public boolean isListed(final ScriptDefinition item) {
		if (item.path == null || item.path.length() == 0) {
			return false;
		}
		for (final String name : list) {
			if (name.equalsIgnoreCase(StringUtil.fileNameWithoutExtension(item.path))) {
				return true;
			}
		}
		return false;
	}
}
