package ru.atomofiron.regextool;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;

import ru.atomofiron.regextool.models.Finder;
import ru.atomofiron.regextool.models.RFile;
import ru.atomofiron.regextool.models.Result;
import ru.atomofiron.regextool.screens.root.RootActivityWhite;

public class SearchService extends IntentService {
	private static final int FOREGROUND_NOTIFICATION_ID = 1;

    public SearchService() {
        super("SearchService");
    }

    private final ArrayList<Result> results = new ArrayList<>();

    // компенсация отсутствия возможности получить Intent через stopService()
    public static boolean needToSendResults;
	private boolean excludeDirs = false;
	private int maxDepth = 0;
	private Finder finder;
	private Noticer noticer;

	@Override
    protected void onHandleIntent(Intent intent) {
        Util.log9("onHandleIntent()");
		Context co = getBaseContext();
		SharedPreferences sp = Util.sp(co);

		maxDepth = sp.getInt(Util.PREF_MAX_DEPTH, 1024);
		boolean useSu = sp.getBoolean(Util.PREF_USE_SU, false);
		excludeDirs = sp.getBoolean(Util.PREF_EXCLUDE_DIRS, false);
		boolean inTheContent = intent.getBooleanExtra(Util.SEARCH_IN_FILES, false);
		finder = new Finder();
		finder.setExtraFormats(sp.getString(Util.PREF_EXTRA_FORMATS, Util.DEFAULT_EXTRA_FORMATS).trim().split("[ ]+"));
		finder.setQuery(intent.getStringExtra(Util.QUERY));
		finder.setCaseSense(intent.getBooleanExtra(Util.CASE_SENSE, false));
		finder.setMultiline(intent.getBooleanExtra(Util.MULTILINE, false));
        if (!finder.setRegex(intent.getBooleanExtra(Util.SEARCH_REGEX, false))) {
			Toast.makeText(co, finder.getLastException(), Toast.LENGTH_LONG).show();
			return;
		}
		finder.setMaxSize(Util.sp(co).getInt(Util.PREF_MAX_SIZE, 10485760));

		startForeground();
		needToSendResults = true;

		LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(co);
		noticer = new Noticer(broadcastManager, results);
		noticer.execute();

		/*Intent resultIntent = new Intent(FinderFragment.Companion.getACTION_RESULTS());
        try {
            for (String path : intent.getStringArrayListExtra(Util.SEARCH_LIST))
                if (inTheContent)
                	searchInTheContent(new RFile(path, useSu), 0);
				else
					search(new RFile(path, useSu), 0);

			resultIntent.putExtra(Util.SEARCH_COUNT, results.size());
			ResultsHolder.setResults(results);
        } catch (Exception e) {
            Util.log9(e.toString());
            resultIntent.putExtra(Util.SEARCH_COUNT, FinderFragment.Companion.getSEARCH_ERROR()).putExtra(FinderFragment.Companion.getKEY_ERROR_MESSAGE(), e.toString());
        }

        stopForeground(true);

        if (needToSendResults)
			broadcastManager.sendBroadcast(resultIntent);*/
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
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
					.createNotificationChannel(new NotificationChannel(
							Util.NOTIFICATION_CHANNEL_ID,
							Util.NOTIFICATION_CHANNEL_ID,
							NotificationManager.IMPORTANCE_DEFAULT)
					);
		}
		startForeground(FOREGROUND_NOTIFICATION_ID, new NotificationCompat.Builder(this, Util.NOTIFICATION_CHANNEL_ID)
				.setContentTitle(getString(R.string.searching))
				.setSmallIcon(R.drawable.ic_search_file)
				.setColor(getResources().getColor(R.color.colorPrimaryLight))
				.setContentIntent(PendingIntent.getActivity(
						this,
						0,
						new Intent(this, RootActivityWhite.class),
						PendingIntent.FLAG_UPDATE_CURRENT
				)).build()
		);
	}

    @Override
    public void onDestroy() {
		super.onDestroy();

		finder.interrupt();
		noticer.cancel(false);
	}

	private static class Noticer extends AsyncTask<Void, Void, Void> {
		private static final long NOTICE_PERIOD = 100L;

		private final LocalBroadcastManager broadcastManager;
		private final ArrayList<Result> results;

		long count = 0L;
		String current = "";

		Noticer(LocalBroadcastManager broadcastManager, ArrayList<Result> results) {
			this.broadcastManager = broadcastManager;
			this.results = results;
		}

		@Override
		protected Void doInBackground(Void... voids) {
			/*while (!isCancelled()) {
				try {
					Thread.sleep(NOTICE_PERIOD);
				} catch (Exception e) {
					Util.log9(e.toString());

					broadcastManager.sendBroadcast(
							new Intent(FinderFragment.Companion.getACTION_RESULTS())
									.putExtra(FinderFragment.Companion.getKEY_NOTICE(), "-/-")
									.putExtra(FinderFragment.Companion.getKEY_NOTICE_CURRENT(), "")
					);
					return null;
				}

				broadcastManager.sendBroadcast(
						new Intent(FinderFragment.Companion.getACTION_RESULTS())
								.putExtra(FinderFragment.Companion.getKEY_NOTICE(), String.format("%1$s/%2$s", results.size(), count))
								.putExtra(FinderFragment.Companion.getKEY_NOTICE_CURRENT(), current)
				);
			}*/
			return null;
		}
	}
}
