package ru.atomofiron.regextool;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchService extends IntentService {

    public SearchService() {
        super("SearchService");
    }

    ArrayList<String> resultsList = new ArrayList<>();
    ArrayList<String> resultsListLines = new ArrayList<>();
    ArrayList<String> arrayOfPositions = new ArrayList<>();
    Pattern pattern;

    boolean done = false; // у меня на Meizu без этого сервис при его закрытии не умирает // это вообще по-русски написано?..
    boolean inFiles = false;
    boolean regex = false;
    boolean caseSense = false;
    int maxSize = 1024*1024;
    String target;

    @Override
    protected void onHandleIntent(Intent intent) {
        I.Log("onHandleIntent()");
        target = intent.getStringExtra(I.REGEX);
        caseSense = intent.getBooleanExtra(I.CASE_SENSE, false);
        if (!caseSense)
        	target = target.toLowerCase();
        pattern = caseSense ?
                Pattern.compile(target) :
                Pattern.compile(target, Pattern.CASE_INSENSITIVE);
        inFiles = intent.getBooleanExtra(I.SEARCH_IN_FILES, false);
        regex = intent.getBooleanExtra(I.SEARCH_REGEX, false);

        startForeground();
        maxSize = I.SP(getBaseContext()).getInt(I.MAX_SIZE, maxSize);
        try {
            for (File file : Strings2Files(intent.getStringArrayListExtra(I.SEARCH_LIST)))
                if (inFiles)
                	searchInFiles(file);
				else
					search(file);
            if (!done)
            	sendResults(I.SEARCH_OK);
        } catch (Exception e) {
            I.Log(e.getMessage());
            if (!done)
            	sendResults(I.SEARCH_ERROR);
        }
        done = true;
    }
    File[] Strings2Files(ArrayList<String> stringsList) {
        int n = stringsList.size();
        File[] filesList = new File[n];
        for (int i = 0; i < n; i++)
        	filesList[i] = new File(stringsList.get(i));
        return filesList;
    }

    void search(File file) {
        if (pattern.matcher(file.getName()).find())
        	resultsList.add(file.getAbsolutePath());
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (file.listFiles() != null && file.listFiles().length > 0)
                for (File nextfile : files)
                	search(nextfile);
        }
    }
    void searchInFiles(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (file.listFiles() != null && file.listFiles().length > 0)
                for (File nextfile : files)
                	searchInFiles(nextfile);
        } else if (file.length() < maxSize) {
            //I.Log("file: "+file.getName());
            String str = file.getName();
            str = str.substring(str.lastIndexOf('.')+1);
            if (I.isTextFile(str)) {
				//I.Log("look: "+file.getName());
				String line = "";
				int k = 0;
				int lineCount = 0;
				String lineCounts = "";
				try {
					InputStream fis = new FileInputStream(file);
					InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
					BufferedReader br = new BufferedReader(isr);
					boolean end = true;
					while (!end || (line = br.readLine()) != null) { // хитрая конструкция
						lineCount++;
						end = true; // оно тут нужно!
						if (!caseSense)
							line = line.toLowerCase();
						if (!regex) {
							if (line.contains(target)) {
								I.Log(" catch: " + line);
								lineCounts += lineCount + " ";
								k++;
								// end = false; // ну эт потом
							}
						} else {
							Matcher m = pattern.matcher(line);
							if (m.find()) {
								I.Log(" catch: " + line);
								if (BuildConfig.DEBUG)
									try {
										I.Log("start() = " + m.start());
										I.Log("regionStart() = " + m.regionStart());
										I.Log("end() = " + m.end());
										I.Log("regionEnd() = " + m.regionEnd());
										I.Log("groupCount() = " + m.groupCount());
										if (m.groupCount() > 0)
											I.Log("group(1) = " + m.group(1));
										else I.Log("group() = " + m.group());
									} catch (Exception e) {
										I.Log("group: " + e.toString());
									}
								lineCounts += lineCount + " ";
								k++;
								if (m.end() != m.regionEnd()) {
									end = false;
									line = line.substring(m.end());
								}
							}
						}
					}
					if (k > 0) {
						resultsList.add(file.getAbsolutePath());
						resultsListLines.add(String.valueOf(k)); // исправить нормально
						arrayOfPositions.add(lineCounts);
					}
				} catch (Exception e) {I.Log(e.toString());}
            } else
				I.Log("FORMAT: "+str);

        }
    }


    void sendResults(int code) {
        I.Log("sendResults("+code+") "+resultsList.size()+" "+resultsListLines.size());
        Intent intent = new Intent(I.toMainActivity).putExtra(I.SEARCH_IN_FILES,inFiles);
        if (code == I.SEARCH_OK)
            intent
                    .putExtra(I.SEARCH_CODE, resultsList.size())
                    .putExtra(I.TARGET, target)
                    .putExtra(I.RESULT_LIST, resultsList)
                    .putExtra(I.RESULT_LIST_COUNTS, resultsListLines)
                    .putExtra(I.RESULT_LIST_LINE_POSITIONS, arrayOfPositions);
        else intent.putExtra(I.SEARCH_CODE,code);
        sendBroadcast(intent);
    }

    void startForeground() {
        PendingIntent pintent = PendingIntent.getActivity(this, 0,
				new Intent(this, MainActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);
        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.searching))
                .setContentIntent(pintent)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher);
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
