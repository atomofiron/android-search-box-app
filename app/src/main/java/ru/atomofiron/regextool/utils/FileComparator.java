package ru.atomofiron.regextool.utils;

import java.util.Comparator;

import ru.atomofiron.regextool.utils.finder.RFile;

public class FileComparator implements Comparator<RFile> {
	public final int compare(RFile first, RFile second) {
		if (first.isDirectory() && !second.isDirectory())
			return -1;
		else if (!first.isDirectory() && second.isDirectory())
			return 1;

		return first.getName().compareToIgnoreCase(second.getName()) < 0 ? -1 : 1;
	}
}
