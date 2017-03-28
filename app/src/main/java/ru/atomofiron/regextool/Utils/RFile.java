package ru.atomofiron.regextool.Utils;

import android.support.annotation.NonNull;

import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;

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

		String result = "";
		Process exec = null;
		InputStream execIn = null;
		OutputStream execOs = null;

		try {
			exec = Runtime.getRuntime().exec("su");
			execIn = exec.getInputStream();
			execOs = exec.getOutputStream();
			DataOutputStream dos = new DataOutputStream(execOs);

			dos.writeBytes(String.format("ls -A -1 %s\n", getAbsolutePath()));
			dos.flush();
			dos.close();

			result = inputStream2String(execIn, "utf-8");
		} catch (Exception e) {
			I.Log(e.toString());
		} finally {
			try {
				if (execIn != null) execIn.close();
				if (execOs != null) execOs.close();
				if (exec != null) exec.destroy();
			} catch (Exception e) { e.printStackTrace(); }
		}
		return result.split("\n");
	}

	private static String inputStream2String(InputStream in, String encoding) throws Exception  {
		StringBuilder out = new StringBuilder();
		InputStreamReader isr = new InputStreamReader(in, encoding);
		char[] b = new char[1024];
		int n;
		while ((n = isr.read(b)) !=  -1) {
			String s = new String(b, 0, n);
			out.append(s);
		}
		return out.toString();
	}
}
