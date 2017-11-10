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

import ru.atomofiron.regextool.Fragments.MainFragment;
import ru.atomofiron.regextool.Models.Finder;
import ru.atomofiron.regextool.Models.RFile;
import ru.atomofiron.regextool.Models.Result;

public class SearchService extends IntentService {

    public SearchService() {
        super("SearchService");
    }

    ArrayList<Result> results = new ArrayList<>();

    boolean stopped = false;
    boolean inFiles = false;
	private boolean useRoot;
	private Context co;
	private final Finder finder = new Finder();
	private final ArrayList<String> doneList = new ArrayList<>();
	private String tmp;
	private long lastNoticed = 0;
	private LocalBroadcastManager broadcastManager;

    @Override
    protected void onHandleIntent(Intent intent) {
        I.log("onHandleIntent()");
		co = getBaseContext();
		SharedPreferences sp = I.sp(co);
		broadcastManager = LocalBroadcastManager.getInstance(co);

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

		Intent resultIntent = new Intent(MainFragment.ACTION_RESULTS);
        try {
            for (RFile rfile : Strings2RFiles(intent.getStringArrayListExtra(I.SEARCH_LIST)))
                if (inFiles)
                	searchInFiles(rfile);
				else
					search(rfile);

			resultIntent.putExtra(I.SEARCH_COUNT, results.size()).putExtra(I.RESULT_LIST, results);
        } catch (Exception e) {
            I.log(e.toString());
            resultIntent.putExtra(I.SEARCH_COUNT, MainFragment.SEARCH_ERROR).putExtra(MainFragment.KEY_ERROR_MESSAGE, e.toString());
        }

		broadcastManager.sendBroadcast(resultIntent);
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
		if (stopped || doneList.contains(tmp = file.getAbsolutePath()))
			return;
		else
			doneList.add(tmp);

        if (finder.find(file.getName()))
        	addAndNotice(new Result(file.getAbsolutePath()));
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (file.listFiles() != null && file.listFiles().length > 0)
                for (File f : files)
                	search(f);
        }
    }
    void searchInFiles(RFile rfile) {
		if (stopped || doneList.contains(tmp = rfile.getAbsolutePath()))
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
        	if (result != null && !result.isEmpty())
        		addAndNotice(result);
        }
    }

    private void addAndNotice(Result result) {
		results.add(result);

		long now = System.currentTimeMillis();
		if (now - lastNoticed > 100) {
			lastNoticed = now;

			broadcastManager.sendBroadcast(
					new Intent(MainFragment.ACTION_RESULTS)
					.putExtra(MainFragment.KEY_NOTICE, String.format("%1$s/%2$s", results.size(), doneList.size()))
			);
		}
	}

    void startForeground() {
        PendingIntent pendingIntent = PendingIntent.getActivity(
        		this,
				0,
				new Intent(this, MainActivity.class),
				PendingIntent.FLAG_CANCEL_CURRENT
		);
        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle(getString(R.string.searching))
                .setContentText(getString(R.string.app_name))
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic);

        startForeground(1, Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ?
				builder.build() : builder.getNotification());
    }
    @Override
    public void onDestroy() {
		super.onDestroy();
		stopped = true;
	}
}
