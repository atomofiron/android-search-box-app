package ru.atomofiron.regextool.Models;

import android.support.annotation.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.atomofiron.regextool.I;

public class Finder {
	private int maxSize = 1024 * 1024;
	private String query = "";
	private String queryLowerCase = "";
	private String[] extraFormats;
	private boolean caseSense = false;
	private boolean multiline = false;

	private Pattern pattern;
	private String lastException = "";

	public String tmpDirPath = null;

	public boolean isRegex() {
		return pattern != null;
	}

	public boolean setRegex(boolean regex) {
		if (regex) {
			try {
				pattern = compile(query);
			} catch (Exception e) {
				pattern = Pattern.compile("");
				lastException = e.toString();

				return false;
			}
		} else
			pattern = null;

		return true;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public String getLastException() {
		return lastException;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	public void setQuery(String query) {
		this.query = query;
		queryLowerCase = query.toLowerCase();
		setRegex(isRegex());
	}

	public void setExtraFormats(String[] extraFormats) {
		this.extraFormats = extraFormats;
	}

	public void setCaseSense(boolean caseSense) {
		this.caseSense = caseSense;
		setRegex(isRegex());
	}

	public void setMultiline(boolean multiline) {
		this.multiline = multiline;
		setRegex(isRegex());
	}

	private Pattern compile(String regexp) {
		int flags = (caseSense ? 0 : Pattern.CASE_INSENSITIVE) | (multiline ? Pattern.MULTILINE : 0);
		return Pattern.compile(regexp, flags);
	}

	public boolean regexpIsValid() {
		try {
			compile(query);

			return true;
		} catch (Exception e) {
			lastException = e.toString();

			return false;
		}
	}

	@Nullable
	public Result search(RFile rFile) {
		rFile.tmpDirPath = tmpDirPath;

		if (rFile.length() < maxSize && I.isTextFile(rFile.getName(), extraFormats))
			return search(rFile.readText(), rFile);
		else
			return null;
	}

	public Result search(String text) {
		return search(text, null);
	}

	private Result search(String text, RFile rFile) {
		Result result = new Result(rFile == null ? null : rFile.getAbsolutePath());

		if (pattern != null && pattern.pattern().isEmpty() )
			return result;

		if (pattern == null) {
			if (query.isEmpty())
				return result;

			if (!caseSense)
				text = text.toLowerCase();

			int offset = 0;
			while ((offset = text.indexOf(caseSense ? this.query : this.queryLowerCase, offset)) != -1)
				result.add(offset, offset += query.length());
		} else {
			Matcher matcher = pattern.matcher(text);

			while (matcher.find())
				result.add(matcher.start(), matcher.end());
		}

		return result;
	}

	public boolean find(String name) {
		if (pattern != null && pattern.pattern().isEmpty() )
			return false;

		if (pattern == null)
			return !query.isEmpty() &&
					(caseSense ? name : name.toLowerCase())
							.contains(caseSense ? this.query : this.queryLowerCase);

		else
			return pattern.matcher(name).find();
	}
}
