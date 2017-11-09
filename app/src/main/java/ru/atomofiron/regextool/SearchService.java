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
import java.util.regex.Pattern;

import ru.atomofiron.regextool.Models.Finder;
import ru.atomofiron.regextool.Models.RFile;
import ru.atomofiron.regextool.Models.Result;

public class SearchService extends IntentService {

    public SearchService() {
        super("SearchService");
    }

    ArrayList<Result> results = new ArrayList<>();
    Pattern pattern;

    boolean done = false; // у меня на Meizu без этого сервис при его закрытии не умирает // это вообще по-русски написано?..
    boolean inFiles = false;
	private boolean useRoot;
	private Context co;
	private final Finder finder = new Finder();
	private final ArrayList<String> doneList = new ArrayList<>();
	private String tmp;

    @Override
    protected void onHandleIntent(Intent intent) {
        I.log("onHandleIntent()");
		co = getBaseContext();
		SharedPreferences sp = I.sp(co);

		useRoot = sp.getBoolean(I.PREF_USE_ROOT, false);
		inFiles = intent.getBooleanExtra(I.SEARCH_IN_FILES, false);
		finder.setExtraFormats(sp.getString(I.PREF_EXTRA_FORMATS, "").split(" "));
		finder.setQuery(intent.getStringExtra(I.QUERY));
		finder.setCaseSense(intent.getBooleanExtra(I.CASE_SENSE, false));
		finder.setMultiline(intent.getBooleanExtra(I.MULTILINE, false));
        if (!finder.setRegex(intent.getBooleanExtra(I.SEARCH_REGEX, false))) {
			I.toast(co, finder.getLastException(), Toast.LENGTH_LONG);
			return;
		}
		finder.setMaxSize(I.sp(co).getInt(I.MAX_SIZE, finder.getMaxSize()));
		finder.tmpDirPath = co.getFilesDir().getAbsolutePath();

		startForeground();
		for (File file : co.getFilesDir().listFiles())
			file.delete();

        try {
            for (RFile rfile : Strings2RFiles(intent.getStringArrayListExtra(I.SEARCH_LIST)))
                if (inFiles)
                	searchInFiles(rfile);
				else
					search(rfile);
            if (!done)
            	sendResults(I.SEARCH_OK);
        } catch (Exception e) {
            I.log(e.toString());
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
		if (doneList.contains(tmp = file.getAbsolutePath()))
			return;
		else
			doneList.add(tmp);

        if (pattern.matcher(file.getName()).find())
        	results.add(new Result(file.getAbsolutePath()));
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (file.listFiles() != null && file.listFiles().length > 0)
                for (File f : files)
                	search(f);
        }
    }
    void searchInFiles(RFile rfile) {
		if (doneList.contains(tmp = rfile.getAbsolutePath()))
			return;
		else
			doneList.add(tmp);

        if (rfile.isDirectory()) {
            File[] files = rfile.listFiles();
            if (files != null)
                for (File f : files)
                	searchInFiles((RFile) f);
        } else {
        	Result result = finder.search(rfile);
        	if (!result.isEmpty())
        		results.add(result);
        }
    }

    void sendResults(int code) {
        Intent intent = new Intent(I.toMainActivity).putExtra(I.SEARCH_IN_FILES,inFiles);
        if (code == I.SEARCH_OK)
            intent
                    .putExtra(I.SEARCH_CODE, results.size())
                    .putExtra(I.RESULT_LIST, results);
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
