package org.rsbot.script.web;

import org.rsbot.script.methods.Web;
import org.rsbot.script.wrappers.RSTile;

import java.util.HashMap;
import java.util.Map;

public class BypassFlags {
	private static final Map<RSTile, Integer> bypass = new HashMap<RSTile, Integer>();

	static {
		bypass.put(new RSTile(3091, 3470, 0), -1);
	}

	public static int getKey(final RSTile tile) {
		if (BypassFlags.bypass.containsKey(tile) || Web.rs_map.containsKey(tile)) {
			if (BypassFlags.bypass.containsKey(tile)) {
				return BypassFlags.bypass.get(tile);
			}
			return Web.rs_map.get(tile);
		}
		return -1;
	}
}
