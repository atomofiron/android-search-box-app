package ru.atomofiron.regextool.utils;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import ru.atomofiron.regextool.Util;

public class Cmd {

	public static boolean checkSu() {
		boolean ok = false;
		Process process = null;
		OutputStream execOs = null;

		try {
			process = Runtime.getRuntime().exec("su");
			execOs = process.getOutputStream();
			DataOutputStream dos = new DataOutputStream(execOs);

			dos.writeBytes("su\n");
			dos.flush();

			dos.close();
			ok = process.waitFor() == 0;
		} catch (Exception e) {
			Util.log9(e.toString());
		} finally {
			try {
				if (execOs != null) execOs.close();
				if (process != null) process.destroy();
			} catch (Exception ignored) {}
		}
		return ok;
	}

	public static String exec(String cmd) {

		String result = "";
		Process process = null;
		InputStream execIs = null;
		OutputStream execOs = null;

		try {
			process = Runtime.getRuntime().exec("su");
			execIs = process.getInputStream();
			execOs = process.getOutputStream();
			DataOutputStream dos = new DataOutputStream(execOs);

			dos.writeBytes(String.format("%s\n", cmd));
			dos.flush();
			dos.close();

			result = inputStream2String(execIs);
		} catch (Exception e) {
			Util.log9(e.toString());
		} finally {
			try {
				if (execIs != null) execIs.close();
				if (execOs != null) execOs.close();
				if (process != null) process.destroy();
			} catch (Exception e) { e.printStackTrace(); }
		}
		return result;
	}

	private static String inputStream2String(InputStream stream) throws Exception  {
		StringBuilder builder = new StringBuilder();
		InputStreamReader reader = new InputStreamReader(stream, "utf-8");
		int read;
		char[] buffer = new char[1024];
		while ((read = reader.read(buffer)) !=  -1) {
			builder.append(buffer, 0, read);
		}

		return builder.toString();
	}
}
