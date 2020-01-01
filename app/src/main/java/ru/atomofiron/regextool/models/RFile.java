package ru.atomofiron.regextool.models;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import ru.atomofiron.regextool.Util;
import ru.atomofiron.regextool.utils.Cmd;

public class RFile extends File {
	public final static String ROOT = "/";

	private boolean useSu = false;
	public boolean flag = false;

	public RFile(@NonNull String path, boolean useSu) {
		super(path);
		this.useSu = useSu;
	}

	private RFile(String dirPath, @NonNull String name, boolean useSu) {
		super(dirPath, name);
		this.useSu = useSu;
	}

	public boolean isRoot() {
		return ROOT.equals(getAbsolutePath());
	}

	/** @return useSu was changed */
	public boolean setUseSu(boolean useSu) {
		boolean changed = this.useSu != useSu;
		this.useSu = useSu;
		return changed;
	}

	@Override
	public boolean canRead() {
		return super.canRead() || useSu;
	}

	@Override
	public RFile getParentFile() {
		String parent = super.getParent();
		return parent == null ? null : new RFile(parent, useSu);
	}

	public boolean containsFiles() {
		if (!isDirectory())
			return false;

		String[] list = super.list();
		if (list != null && list.length != 0)
			return true;

		if (!super.canRead() && useSu) {
			String ans = Cmd.exec(formatLs(getAbsolutePath()));
			return !ans.isEmpty() && !ans.equals(".\n..\n");
		} else
			return false;
	}

	@Override
	public RFile[] listFiles() {
		String[] list = list();
		if (list == null || list.length == 0 || list[0].isEmpty())
			return null;

		RFile[] files = new RFile[list.length];
		for (int i = 0; i < list.length; i++)
			files[i] = new RFile(getAbsolutePath(), list[i], useSu);

		return files;
	}

	@Override
	public String[] list() {
		if (super.canRead() || !isDirectory() || !useSu)
			return super.list();

		return Cmd.exec(formatLs(getAbsolutePath()))
				.replace(".\n..\n", "").split("\n");
	}

	private static String formatLs(String path) {
		return String.format("ls -a \"%s\"", path);
	}

	public String readText() {
		if (!super.canRead())
			return useSu ? Cmd.exec(String.format("cat \"%s\"", getAbsolutePath())) : "";

		String result = "";

		InputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		try {
			fis = new FileInputStream(this);
			isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
			br = new BufferedReader(isr);

			String line;
			while ((line = br.readLine()) != null)
				result = result.concat(line).concat("\n");
		} catch (Exception e) {
			Util.log(e.toString());
		} finally {
			try {
				if (br != null) br.close();
				if (isr != null) isr.close();
				if (fis != null) fis.close();
			} catch (Exception ignored) {}
		}

		return result;
	}
}
