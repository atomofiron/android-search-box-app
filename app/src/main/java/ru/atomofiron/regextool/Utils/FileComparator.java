package ru.atomofiron.regextool.Utils;

import java.io.File;
import java.util.Comparator;

public class FileComparator implements Comparator {
	public final int compare(Object pFirst, Object pSecond) {
		File first = (File) pFirst;
		File second = (File) pSecond;
		if (first.isDirectory() && !second.isDirectory())
			return -1;
		else if (!first.isDirectory() && second.isDirectory())
			return 1;

		return first.getName().compareToIgnoreCase(second.getName()) < 0 ? -1 : 1;
	}
}
