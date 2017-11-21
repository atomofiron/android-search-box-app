package ru.atomofiron.regextool;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import java.util.ArrayList;

import ru.atomofiron.regextool.Fragments.MainFragment;
import ru.atomofiron.regextool.Models.Finder;
import ru.atomofiron.regextool.Models.RFile;
import ru.atomofiron.regextool.Models.Result;
import ru.atomofiron.regextool.Models.ResultsHolder;

public class SearchService extends IntentService {

    public SearchService() {
        super("SearchService");
    }

    private final ArrayList<Result> results = new ArrayList<>();

	private boolean excludeDirs = false;
	private int maxDepth = 0;
	private Finder finder;
	private PendingIntent mainPendingIntent;
	private Noticer noticer;

	@Override
	public void onCreate() {
		super.onCreate();

		mainPendingIntent = PendingIntent.getActivity(
				this,
				0,
				new Intent(this, MainActivity.class),
				PendingIntent.FLAG_UPDATE_CURRENT
		);
	}

	@Override
    protected void onHandleIntent(Intent intent) {
        I.log("onHandleIntent()");
		Context co = getBaseContext();
		SharedPreferences sp = I.sp(co);

		maxDepth = sp.getInt(I.PREF_MAX_DEPTH, 1024);
		boolean useRoot = sp.getBoolean(I.PREF_USE_ROOT, false);
		excludeDirs = sp.getBoolean(I.PREF_EXCLUDE_DIRS, false);
		boolean inTheContent = intent.getBooleanExtra(I.SEARCH_IN_FILES, false);
		finder = new Finder();
		finder.setExtraFormats(sp.getString(I.PREF_EXTRA_FORMATS, "").split(" "));
		finder.setQuery(intent.getStringExtra(I.QUERY));
		finder.setCaseSense(intent.getBooleanExtra(I.CASE_SENSE, false));
		finder.setMultiline(intent.getBooleanExtra(I.MULTILINE, false));
        if (!finder.setRegex(intent.getBooleanExtra(I.SEARCH_REGEX, false))) {
			Toast.makeText(co, finder.getLastException(), Toast.LENGTH_LONG).show();
			return;
		}
		finder.setMaxSize(I.sp(co).getInt(I.PREF_MAX_SIZE, 10485760));

		startForeground();

		LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(co);
		noticer = new Noticer(broadcastManager, results);
		noticer.execute();

		Intent resultIntent = new Intent(MainFragment.ACTION_RESULTS);
        try {
            for (String path : intent.getStringArrayListExtra(I.SEARCH_LIST))
                if (inTheContent)
                	searchInTheContent(new RFile(path).setUseRoot(useRoot), 0);
				else
					search(new RFile(path).setUseRoot(useRoot), 0);

			resultIntent.putExtra(I.SEARCH_COUNT, results.size());
			ResultsHolder.setResults(results);
        } catch (Exception e) {
            I.log(e.toString());
            resultIntent.putExtra(I.SEARCH_COUNT, MainFragment.SEARCH_ERROR).putExtra(MainFragment.KEY_ERROR_MESSAGE, e.toString());
        }

        stopForeground(true);
		showNotification(String.format("%1$s/%2$s", results.size(), noticer.count));
		broadcastManager.sendBroadcast(resultIntent);
    }

    void search(RFile file, int depth) {
		if (depth <= maxDepth && file.isDirectory()) {
			RFile[] files = file.listFiles();
			if (files != null)
				for (RFile f : files) {
					if (finder.isInterrupted())
						break;

					search(f, depth + 1);
				}
		}

		if (!file.isDirectory() || !excludeDirs) {
			if (finder.find(file.getName()))
				results.add(new Result(file.getAbsolutePath()));

			noticer.count++;
		}
    }
    void searchInTheContent(RFile rfile, int depth) {
        if (depth <= maxDepth && rfile.isDirectory()) {
            RFile[] files = rfile.listFiles();
            if (files != null)
                for (RFile f : files) {
					if (finder.isInterrupted())
						break;

					searchInTheContent(f, depth + 1);
				}
        } else if (rfile.isFile()) {
        	noticer.current = rfile.getAbsolutePath();

        	Result result = finder.search(rfile);
        	if (result != null && !result.isEmpty())
				results.add(result);

			noticer.count++;
        }
	}

    void startForeground() {
        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle(getString(R.string.searching))
                .setContentText(getString(R.string.app_name))
                .setContentIntent(mainPendingIntent)
                .setSmallIcon(R.drawable.ic_search_file);

        startForeground(1, Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ?
				builder.build() : builder.getNotification());
    }

    private void showNotification(String subtext) {
		Notification.Builder builder = new Notification.Builder(this)
				.setTicker(getString(R.string.search_completed))
				.setContentTitle(getString(R.string.search_completed))
				.setContentText(getString(R.string.app_name))
				.setContentIntent(mainPendingIntent)
				.setSmallIcon(R.drawable.ic_search_file);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			builder.setSubText(subtext);

		NotificationManager notifier = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if (notifier != null) {
			// API >= 16 getNotification() вызывает build()
			Notification notification = builder.getNotification();
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			notifier.notify(2, notification);
		}
	}
    @Override
    public void onDestroy() {
		super.onDestroy();

		finder.interrupt();
		noticer.cancel(false);
	}

	private static class Noticer extends AsyncTask<Void, Void, Boolean> {
		private static final long NOTICE_PERIOD = 100L;

		private final LocalBroadcastManager broadcastManager;
		private final ArrayList<Result> results;

		private long lastNoticed = 0L;
		long count = 0L;
		String current = "";

		Noticer(LocalBroadcastManager broadcastManager, ArrayList<Result> results) {
			this.broadcastManager = broadcastManager;
			this.results = results;
		}

		@Override
		protected Boolean doInBackground(Void... voids) {
			while (!isCancelled()) {
				long now = System.currentTimeMillis();

				if (now - lastNoticed >= NOTICE_PERIOD) {
					broadcastManager.sendBroadcast(
							new Intent(MainFragment.ACTION_RESULTS)
									.putExtra(MainFragment.KEY_NOTICE, String.format("%1$s/%2$s", results.size(), count))
									.putExtra(MainFragment.KEY_NOTICE_CURRENT, current)
					);

					lastNoticed = now;
				}
			}
			return false;
		}
	}
}
