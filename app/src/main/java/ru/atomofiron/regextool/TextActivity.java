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

import ru.atomofiron.regextool.Utils.RFile;
import ru.atomofiron.regextool.Utils.Result;

public class TextActivity extends AppCompatActivity {

    FloatingActionButton fabPrev;
    FloatingActionButton fabNext;
    TextView counter;
    NestedScrollView scrollView;
    TextView textView;

    Listener listener;
    TextActivity co;

    int curPos = -1;
    int count = 0;
    int[] startPositions;
    boolean ready = false;

	private Result result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setTheme(I.sp(this).getString(I.PREF_THEME, "0").equals("0") ? R.style.AppTheme_Light : R.style.AppTheme);
        setContentView(R.layout.activity_text);
        co = this;

        counter = (TextView)findViewById(R.id.counter);
        textView = (TextView)findViewById(R.id.text);
        scrollView = (NestedScrollView)findViewById(R.id.scroll_text);

        result = getIntent().getParcelableExtra(I.RESULT);
        count = result.size();
        startPositions = new int[count];
        counter.setText(String.format("0/%d", count));

        listener = new Listener();
        fabPrev = (FloatingActionButton) findViewById(R.id.fab_prev);
        fabNext = (FloatingActionButton) findViewById(R.id.fab_next);
        fabPrev.setOnClickListener(listener);
        fabNext.setOnClickListener(listener);
        //((NestedScrollView)findViewById(R.id.scroll_text)).setOnScrollChangeListener(listener);

        new Thread(new Runnable() {
            @Override
            public void run() {
				RFile file = new RFile(result.path);
				file.useRoot = I.sp(co).getBoolean(I.PREF_USE_ROOT, false);
				file.tmpDirPath = co.getFilesDir().getAbsolutePath();
				final Spannable spanRange = new SpannableString(file.readText());
				int i = 0;
				while (result.hasNext()) {
					int[] pare = result.next();
					startPositions[i++] = pare[0];
					spanRange.setSpan(new BackgroundColorSpan(Color.argb(128, 0, 128, 0)),
							pare[0], pare[1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}

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

    private class Listener implements View.OnClickListener/*, NestedScrollView.OnScrollChangeListener*/ {
        @Override
        public void onClick(View v) {
            I.log("onClick() "+curPos+" count="+result.size());
            switch (v.getId()) {
                case R.id.fab_prev:
                    curPos--;
					if (curPos < 0)
						curPos = result.size() - 1;
                    break;
                case R.id.fab_next:
					curPos++;
					if (curPos == result.size())
						curPos = 0;
                    break;
            }
            Layout layout = textView.getLayout();
            //scrollView.scrollTo(0, layout.getLineTop(counts[curPos-1]));//layout.getLineForOffset(startPos)));
            scrollView.scrollTo(0, layout.getLineTop(layout.getLineForOffset(startPositions[curPos])));
            counter.setText(String.format("%1$d/%2$d", (curPos+1), count));
        }

        /*
        int lastDiff=0;
        @Override
        public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
            I.log("onScrollChange() "+lastDiff);
            if (isJump) {
                isJump=false;
                return;
            }
            int newDiff = (scrollY - oldScrollY) > 0 ? 1 : -1;
            I.log("newDiff = "+newDiff);
            I.log("newDiff>lastDiff = "+(newDiff>lastDiff));
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
