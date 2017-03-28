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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

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
    int curPos = 0;
    boolean ready = false;
	private int target_n;
	private int length;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);
        co = this;

        counter = (TextView)findViewById(R.id.counter);
        textView = (TextView)findViewById(R.id.text);
        scrollView = (NestedScrollView)findViewById(R.id.scroll_text);

        String[] curCounts = getIntent().getStringExtra(I.RESULT_LINE_COUNTS).split(" ");
        count = curCounts.length;
        counter.setText(String.format("0/%d", count));
        counts = new int[count];
        for (int i = 0; i < curCounts.length; i++)
            counts[i] = Integer.parseInt(curCounts[i]);
        final String target = getIntent().getStringExtra(I.TARGET);

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
				file.readFile(co, new RFile.OnReadLineListener() {
					public void onReadLine(String line) {
						lines.add(String.format("%s\n", line));
						if (isTarget(lines.size())) {
							int start = line.indexOf(target);
							int end = start + target.length();
							if (start == -1) {
								start = 0;
								end = line.length() - 1; // ???          -1          ???
							}
							start += length;
							end += length;
							pares[target_n*2] = start;
							pares[target_n*2+1] = end;
							target_n++;
						}
						length += line.length() + 1;
					}
				});
                /*try {
                    InputStream fis = new FileInputStream(new File(getIntent().getStringExtra(I.RESULT_PATH)));
                    InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
                    BufferedReader br = new BufferedReader(isr);
                    String line;
                    int line_n = 0;
                    int target_n = 0;
                    while ((line = br.readLine()) != null) {
                        line_n++;
                        if (isTarget(line_n)) {
                            I.Log("isTarget(k)");
                            int start = line.indexOf(target);
                            int end = start + target.length();
                            if (start == -1) {
                                start = 0;
                                end = line.length() - 1; // ???          -1          ???
                            }
                            start += exitText.length();
                            end += exitText.length();
                            //int offset = (line_n+"_").length();
                            pares[target_n*2] = start;//+offset;
                            pares[target_n*2+1] = end;//+offset;
                            target_n++;
                        }
                        exitText += line+"\n";
                    }
                } catch (Exception e) {
					I.Log(e.toString());
					exitText = e.toString();
                }*/

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
