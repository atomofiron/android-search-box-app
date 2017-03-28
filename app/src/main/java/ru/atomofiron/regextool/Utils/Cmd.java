package ru.atomofiron.regextool.Utils;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import ru.atomofiron.regextool.I;

public class Cmd {

	public static int easyExec(String cmd) {
		int code = -1;
		Process exec = null;
		OutputStream execOs = null;

		try {
			exec = Runtime.getRuntime().exec("su");
			execOs = exec.getOutputStream();
			DataOutputStream dos = new DataOutputStream(execOs);

			dos.writeBytes(String.format("%s\n", cmd));
			dos.flush();

			dos.close();
			code = exec.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (execOs != null) execOs.close();
				if (exec != null) exec.destroy();
			} catch (Exception ignored) {}
		}
		return code;
	}

	public static String exec(String cmd) {

		String result = "";
		Process exec = null;
		InputStream execIn = null;
		OutputStream execOs = null;

		try {
			exec = Runtime.getRuntime().exec("su");
			execIn = exec.getInputStream();
			execOs = exec.getOutputStream();
			DataOutputStream dos = new DataOutputStream(execOs);

			dos.writeBytes(cmd);
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
		return result;
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
