package ru.atomofiron.regextool;

import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.atomofiron.regextool.Utils.RFile;

public class TextActivity extends AppCompatActivity {

    FloatingActionButton fabPrev;
    FloatingActionButton fabNext;
    TextView counter;
    NestedScrollView scrollView;
    TextView textView;

    Listener listener;
    TextActivity co;

    int[] counts;
    int count;
    int[] pares;
    int curPos = -1;
    boolean ready = false;
	private int target_n;
	private int length;
	private String target;
	private boolean isRegex;
	private boolean caseSense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setTheme(I.SP(this).getString(I.PREF_THEME, "0").equals("0") ? R.style.AppTheme_Light : R.style.AppTheme);
        setContentView(R.layout.activity_text);
        co = this;

        counter = (TextView)findViewById(R.id.counter);
        textView = (TextView)findViewById(R.id.text);
        scrollView = (NestedScrollView)findViewById(R.id.scroll_text);

		isRegex = getIntent().getBooleanExtra(I.SEARCH_REGEX, false);
		caseSense = getIntent().getBooleanExtra(I.CASE_SENSE, false);
        String[] curCounts = getIntent().getStringExtra(I.RESULT_LINE_NUMS).split(",");
        count = curCounts.length;
        counter.setText(String.format("0/%d", count));
        counts = new int[count];
        for (int i = 0; i < curCounts.length; i++)
            counts[i] = Integer.parseInt(curCounts[i]);
        target = getIntent().getStringExtra(I.TARGET);

        listener = new Listener();
        fabPrev = (FloatingActionButton) findViewById(R.id.fab_prev);
        fabNext = (FloatingActionButton) findViewById(R.id.fab_next);
        fabPrev.setOnClickListener(listener);
        fabNext.setOnClickListener(listener);
        //((NestedScrollView)findViewById(R.id.scroll_text)).setOnScrollChangeListener(listener);

        new Thread(new Runnable() {
            @Override
            public void run() {
				final ArrayList<String> lines = new ArrayList<>();
                pares = new int[count*2];
				target_n = 0;
				length = 0;
				RFile file = new RFile(getIntent().getStringExtra(I.RESULT_PATH));
				file.useRoot = I.SP(co).getBoolean(I.PREF_USE_ROOT, false);
				final Pattern pattern = caseSense ?
						Pattern.compile(target) :
						Pattern.compile(target, Pattern.CASE_INSENSITIVE);
				file.readFile(co, new RFile.OnReadLineListener() {
					public void onReadLine(String line, int lineNum) {
						lines.add(line);
						int len = line.length();
						if (isTarget(lineNum)) {

							int sumLastSubStrLen = 0;
							if (isRegex) {
								Matcher m;
								while ((m = pattern.matcher(line)).find()) {
									pares[target_n*2] = length + m.start() + sumLastSubStrLen;
									pares[target_n*2+1] = length + m.end() + sumLastSubStrLen;
									target_n++;
									line = line.substring(m.end());
									sumLastSubStrLen += m.end();
								}
							} else {
								while (line.contains(target)) {
									int index = line.indexOf(target);
									pares[target_n*2] = length + index + sumLastSubStrLen;
									pares[target_n*2+1] = pares[target_n*2] + target.length();
									target_n++;
									line = line.substring(index + target.length());
									sumLastSubStrLen += index + target.length();
								}
							}
						}
						length += len;
					}
				});

				StringBuilder sb = new StringBuilder("");
				for (String line : lines)
					sb.append(line);
                final Spannable spanRange = new SpannableString(sb.toString());
                for (int i = 0; i < count; i++)
                	spanRange.setSpan(new BackgroundColorSpan(Color.argb(128, 0, 128, 0)),
							pares[i*2], pares[i*2+1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                co.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(spanRange);
                        findViewById(R.id.progressbar).setVisibility(View.GONE);
                        findViewById(R.id.fab_layout).setVisibility(View.VISIBLE);
                        ready = true;
                    }
                });
            }
        }).start();

    }

    private boolean isTarget(int k) {
        for (int t : counts)
        	if (t == k)
        		return true;
        return false;
    }
    private class Listener implements View.OnClickListener/*, NestedScrollView.OnScrollChangeListener*/ {
        @Override
        public void onClick(View v) {
            I.Log("onClick() "+curPos+" count="+count);
            switch (v.getId()) {
                case R.id.fab_prev:
                    curPos--;
					if (curPos < 0)
						curPos = count - 1;
                    break;
                case R.id.fab_next:
					curPos++;
					if (curPos == count)
						curPos = 0;
                    break;
            }
            Layout layout = textView.getLayout();
            //scrollView.scrollTo(0, layout.getLineTop(counts[curPos-1]));//layout.getLineForOffset(startPos)));
            scrollView.scrollTo(0, layout.getLineTop(layout.getLineForOffset(pares[curPos*2])));
            counter.setText(String.format("%1$d/%2$d", (curPos+1), count));
        }

        /*
        int lastDiff=0;
        @Override
        public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
            I.Log("onScrollChange() "+lastDiff);
            if (isJump) {
                isJump=false;
                return;
            }
            int newDiff = (scrollY - oldScrollY) > 0 ? 1 : -1;
            I.Log("newDiff = "+newDiff);
            I.Log("newDiff>lastDiff = "+(newDiff>lastDiff));
            if (newDiff>lastDiff) {
                fabPrev.hide();
                fabNext.hide();
                counter.setVisibility(View.INVISIBLE);
            } else if (newDiff<lastDiff) {
                fabPrev.show();
                fabNext.show();
                counter.setVisibility(View.VISIBLE);
            }
            lastDiff = newDiff;
        }*/
    }
}
