package ru.atomofiron.regextool;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.atomofiron.regextool.Utils.RFile;

public class SearchService extends IntentService implements RFile.OnReadLineListener {

    public SearchService() {
        super("SearchService");
    }

    ArrayList<String> resultListOfFilePaths = new ArrayList<>();
    ArrayList<Integer> resultListOfLineCounts = new ArrayList<>();
    ArrayList<String> resultListOfLineNums = new ArrayList<>();
    Pattern pattern;

    boolean done = false; // у меня на Meizu без этого сервис при его закрытии не умирает // это вообще по-русски написано?..
    boolean inFiles = false;
    boolean isRegex = false;
    boolean caseSense = false;
    int maxSize = 1024*1024;
    String target;
	private SharedPreferences sp;
	private boolean useRoot;
	private Context co;
	private String lineNumsStr;
	private String[] extraFormats;
	private final ArrayList<String> doneList = new ArrayList<>();
	private String tmp;

    @Override
    protected void onHandleIntent(Intent intent) {
        I.Log("onHandleIntent()");
		co = getBaseContext();
		sp = I.SP(co);
		extraFormats = sp.getString(I.PREF_EXTRA_FORMATS, "").split(" ");

		useRoot = sp.getBoolean(I.PREF_USE_ROOT, false);
        target = intent.getStringExtra(I.REGEX);
        caseSense = intent.getBooleanExtra(I.CASE_SENSE, false);
        if (!caseSense)
        	target = target.toLowerCase();
		try {
			pattern = caseSense ?
					Pattern.compile(target) :
					Pattern.compile(target, Pattern.CASE_INSENSITIVE);
		} catch (Exception e) {
			I.Toast(co, e.getMessage() == null ? e.toString() : e.getMessage(), Toast.LENGTH_LONG);
			return;
		}
        inFiles = intent.getBooleanExtra(I.SEARCH_IN_FILES, false);
        isRegex = intent.getBooleanExtra(I.SEARCH_REGEX, false);

        startForeground();
		for (File file : co.getFilesDir().listFiles())
			file.delete();
        maxSize = I.SP(co).getInt(I.MAX_SIZE, maxSize);
        try {
            for (RFile rfile : Strings2RFiles(intent.getStringArrayListExtra(I.SEARCH_LIST)))
                if (inFiles)
                	searchInFiles(rfile);
				else
					search(rfile);
            if (!done)
            	sendResults(I.SEARCH_OK);
        } catch (Exception e) {
            I.Log(e.getMessage());
            if (!done)
            	sendResults(I.SEARCH_ERROR);
        }
        done = true;
    }
    RFile[] Strings2RFiles(ArrayList<String> stringsList) {
        int n = stringsList.size();
		RFile[] filesList = new RFile[n];
        for (int i = 0; i < n; i++) {
			filesList[i] = new RFile(stringsList.get(i));
			filesList[i].useRoot = useRoot;
		}
        return filesList;
    }

    void search(File file) {
		if (doneList.contains(tmp = file.getAbsolutePath()) || !doneList.add(tmp)) // !doneList.add() always false
			return;

        if (pattern.matcher(file.getName()).find())
        	resultListOfFilePaths.add(file.getAbsolutePath());
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (file.listFiles() != null && file.listFiles().length > 0)
                for (File f : files)
                	search(f);
        }
    }
    void searchInFiles(RFile rfile) {
		if (doneList.contains(tmp = rfile.getAbsolutePath()) || !doneList.add(tmp)) // !doneList.add() always false
			return;

        if (rfile.isDirectory()) {
            File[] files = rfile.listFiles();
            if (files != null)
                for (File f : files)
                	searchInFiles((RFile) f);
        } else if (rfile.length() < maxSize && I.isTextFile(rfile.getName(), extraFormats)) {
			lineNumsStr = "";

			rfile.readFile(co, this);

			if (!lineNumsStr.isEmpty()) {
				resultListOfFilePaths.add(rfile.getAbsolutePath());
				resultListOfLineCounts.add(lineNumsStr.split(",").length);
				resultListOfLineNums.add(lineNumsStr);
			}
        }
    }
	@Override
	public void onReadLine(String line, int lineNum) {
		if (!caseSense)
			line = line.toLowerCase();
		if (isRegex) {
			Matcher m;
			while ((m = pattern.matcher(line)).find()) {
				lineNumsStr += String.format("%d,", lineNum);
				line = line.substring(m.end());
			}
		} else {
			while (line.contains(target)) {
				lineNumsStr += String.format("%d,", lineNum);
				line = line.substring(line.indexOf(target) + target.length());
			}
		}
	}

    void sendResults(int code) {
        I.Log("sendResults() "+lineNumsStr);
        Intent intent = new Intent(I.toMainActivity).putExtra(I.SEARCH_IN_FILES,inFiles);
        if (code == I.SEARCH_OK)
            intent
                    .putExtra(I.SEARCH_CODE, resultListOfFilePaths.size())
                    .putExtra(I.SEARCH_REGEX, isRegex)
                    .putExtra(I.TARGET, target)
                    .putExtra(I.RESULT_LIST, resultListOfFilePaths)
                    .putExtra(I.RESULT_LIST_COUNTS, resultListOfLineCounts)
                    .putExtra(I.RESULT_LINE_NUMS, resultListOfLineNums);
        else
        	intent.putExtra(I.SEARCH_CODE, code);

        LocalBroadcastManager.getInstance(co).sendBroadcast(intent);
    }

    void startForeground() {
        PendingIntent pintent = PendingIntent.getActivity(this, 0,
				new Intent(this, MainActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);
        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.searching))
                .setContentIntent(pintent)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic);
        Notification notif;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        	notif = builder.build();
        else
        	notif = builder.getNotification();
        notif.defaults = Notification.DEFAULT_LIGHTS;
        notif.sound = null;
        notif.flags |= Notification.FLAG_NO_CLEAR;
        notif.vibrate = new long[] { 0, 0 };
        startForeground(2016, notif);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!done)
        	sendResults(I.SEARCH_OK);
        done = true;
    }
}
