package org.rsbot.script.provider;

import org.rsbot.util.StringUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Paris
 */
public class ScriptDefinition implements Comparable<ScriptDefinition> {

	public String getName() {
		String txt = StringUtil.stripHtml(name);
		txt = txt.replaceFirst("\\b[Vv]\\d+(?:\\.\\d+)?", "");
		txt = txt.replaceFirst("^\\W+", "");
		txt = txt.replaceFirst("\\W+$", "");
		return txt.trim();
	}

	public String getDescription() {
		return StringUtil.stripHtml(description);
	}

	String getAuthors() {
		final StringBuilder s = new StringBuilder(16);
		for (int i = 0; i < authors.length; i++) {
			if (i > 0) {
				s.append(i == authors.length - 1 ? " and " : ", ");
			}
			s.append(authors[i]);
		}
		return StringUtil.stripHtml(s.toString());
	}

	public List<String> getKeywords() {
		final ArrayList<String> list = new ArrayList<String>(keywords.length);
		if (keywords == null) {
			return list;
		}
		for (String keyword : keywords) {
			for (String sub : keyword.split("&|,|;|\\s")) {
				sub = sub.trim().toLowerCase();
				if (sub.length() != 0) {
					list.add(sub);
				}
			}
		}
		return list;
	}

	public int id;

	public long crc32;

	public String name;

	public double version;

	public String description;

	public String[] authors;

	public String[] keywords;

	public String website;

	public ScriptSource source;

	public String path;

	public boolean obfuscated;

	public int compareTo(final ScriptDefinition def) {
		final int c = getName().compareToIgnoreCase(def.getName());
		return c == 0 ? Double.compare(version, def.version) : c;
	}

	@Override
	public String toString() {
		final StringBuilder s = new StringBuilder(46);
		s.append(getName());
		s.append(" v");
		s.append(version);
		s.append(" by ");
		s.append(getAuthors());
		return s.toString();
	}
}
