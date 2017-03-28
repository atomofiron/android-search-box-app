package ru.atomofiron.regextool.Utils;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;

import ru.atomofiron.regextool.I;

public class RFile extends File {

	public boolean useRoot = false;

	public RFile(File dir, @NonNull String name) {
		super(dir, name);
	}

	public RFile(@NonNull String path) {
		super(path);
	}

	public RFile(String dirPath, @NonNull String name) {
		super(dirPath, name);
	}

	public RFile(@NonNull URI uri) {
		super(uri);
	}

	public RFile(File file) {
		super(file.getAbsolutePath());
	}

	@Override
	public File[] listFiles() {
		String[] list = list();
		if (list == null)
			return null;

		String current = getAbsolutePath();
		File[] files = new File[list.length];
		for (int i = 0; i < list.length; i++)
			files[i] = new RFile(String.format("%1$s/%2$s", current, list[i]));
		return files;
	}

	@Override
	public String[] list() {
		if (canRead() || !isDirectory() || !useRoot)
			return super.list();

		return Cmd.exec(String.format("ls -A -1 %s\n", getAbsolutePath())).split("\n");
	}

	public String readFile(Context co, OnReadLineListener listener) {
		if (listener == null)
			return null;

		File file = this;
		boolean needDelete = false;
		if (!canRead() && useRoot) {
			String newPath = String.format("%1$s/%2$s", co.getFilesDir().getAbsolutePath(), getName());
			if (Cmd.easyExec(String.format("cp -F %1$s %2$s", getAbsolutePath(), newPath)) == 0) {
				if (Cmd.easyExec(String.format("chmod 0777 %s", newPath)) != 0)
					Cmd.easyExec(String.format("rm %s", newPath));
				else {
					file = new File(newPath);
					needDelete = true;
				}
			}
		}
		String result = "";

		InputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		try {
			fis = new FileInputStream(file);
			isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
			br = new BufferedReader(isr);
			String line;
			while ((line = br.readLine()) != null) {
				I.Log("line = "+line);
				listener.onReadLine(line);
			}
		} catch (Exception e) {
			I.Log(e.toString());
			result = e.toString();
		} finally {
			try {
				if (br != null) br.close();
				if (isr != null) isr.close();
				if (fis != null) fis.close();
			} catch (Exception ignored) {}
		}
		if (needDelete)
			file.delete();
		I.Log("result: "+result);
		return result;
	}

	public interface OnReadLineListener {
		public void onReadLine(String line);
	}
}
