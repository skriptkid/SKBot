package org.rsbot.service;

import org.rsbot.Configuration;
import org.rsbot.util.io.IniParser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class Preferences {
	private final Logger log = Logger.getLogger(Preferences.class.getName());
	private static Preferences instance;
	private final File store;

	public boolean hideAds = false;
	public boolean shutdown = false;
	public int shutdownTime = 10;
	public boolean sdnShow = true;
	public boolean likedScriptsOnly = false;

	private Preferences(final File store) {
		this.store = store;
	}

	public static Preferences getInstance() {
		if (instance == null) {
			instance = new Preferences(new File(Configuration.Paths.getSettingsDirectory(), "preferences.ini"));
		}
		return instance;
	}

	public void load() {
		Map<String, String> keys = null;
		try {
			if (!store.exists()) {
				if (!store.createNewFile()) {
					throw new IOException("Could not create a new file.");
				}
			}
			keys = IniParser.deserialise(store).get(IniParser.EMPTYSECTION);
		} catch (final IOException ignored) {
			log.severe("Failed to load preferences");
		}
		if (keys == null || keys.isEmpty()) {
			return;
		}
		if (keys.containsKey("hideAds")) {
			hideAds = IniParser.parseBool(keys.get("hideAds"));
		}
		if (keys.containsKey("shutdown")) {
			shutdown = IniParser.parseBool(keys.get("shutdown"));
		}
		if (keys.containsKey("shutdownTime")) {
			shutdownTime = Integer.parseInt(keys.get("shutdownTime"));
			shutdownTime = Math.max(Math.min(shutdownTime, 60), 3);
		}
		if (keys.containsKey("sdnShow")) {
			sdnShow = IniParser.parseBool(keys.get("sdnShow"));
		}
		if (keys.containsKey("likedScriptsOnly")) {
			likedScriptsOnly = IniParser.parseBool(keys.get("likedScriptsOnly"));
		}
	}

	public void save() {
		final Map<String, String> keys = new HashMap<String, String>(11);
		keys.put("hideAds", Boolean.toString(hideAds));
		keys.put("shutdown", Boolean.toString(shutdown));
		keys.put("shutdownTime", Integer.toString(shutdownTime));
		keys.put("sdnShow", Boolean.toString(sdnShow));
		keys.put("likedScriptsOnly", Boolean.toString(likedScriptsOnly));
		final Map<String, Map<String, String>> data = new HashMap<String, Map<String, String>>(1);
		data.put(IniParser.EMPTYSECTION, keys);
		try {
			IniParser.serialise(data, store);
		} catch (final IOException ignored) {
			log.severe("Could not save preferences");
		}
	}
}
