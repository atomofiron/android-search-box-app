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

public class TextActivity extends AppCompatActivity {

    FloatingActionButton fabPrev;
    FloatingActionButton fabNext;
    TextView counter;
    NestedScrollView scrollView;
    TextView textView;

    Listner listener;
    TextActivity context;

    int[] counts;
    int count;
    int[] pares;
    int curPos=0;
    boolean ready = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);
        context = this;

        counter = (TextView)findViewById(R.id.counter);
        textView = (TextView)findViewById(R.id.text);
        scrollView = (NestedScrollView)findViewById(R.id.scroll_text);

        String[] counts_ = getIntent().getStringExtra(I.RESULT_LINE_COUNTS).split(" ");
        count = counts_.length;
        counter.setText("0/"+count);
        counts = new int[count];
        for (int i=0;i<counts_.length;i++) counts[i]=Integer.parseInt(counts_[i]);
        final String target = getIntent().getStringExtra(I.TARGET);

        listener = new Listner();
        fabPrev = (FloatingActionButton) findViewById(R.id.fab_prev);
        fabNext = (FloatingActionButton) findViewById(R.id.fab_next);
        fabPrev.setOnClickListener(listener);
        fabNext.setOnClickListener(listener);
        //((NestedScrollView)findViewById(R.id.scroll_text)).setOnScrollChangeListener(listener);

        new Thread(new Runnable() {
            @Override
            public void run() {
                String exitText = "";
                pares = new int[count*2];
                try {
                    InputStream fis = new FileInputStream(new File(getIntent().getStringExtra(I.RESULT_PATH)));
                    InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
                    BufferedReader br = new BufferedReader(isr);
                    String line;
                    int line_n=0;
                    int target_n=0;
                    while ((line = br.readLine()) != null) {
                        line_n++;
                        if (isTarget(line_n)) {
                            I.Log("isTarget(k)");
                            int start = line.indexOf(target);
                            int end = start+target.length();
                            if (start==-1) {
                                start = 0;
                                end = line.length()-1; // ???          -1          ???
                            }
                            start+=exitText.length();
                            end+=exitText.length();
                            //int offset = (line_n+"_").length();
                            pares[target_n*2]=start;//+offset;
                            pares[target_n*2+1]=end;//+offset;
                            target_n++;
                        }
                        exitText+=/*line_n+"_"+*/line+"\n";
                    }
                } catch (Exception e) {I.Log(e.toString());}
                final Spannable spanRange = new SpannableString(exitText);
                for (int i=0;i<count;i++) spanRange.setSpan(new BackgroundColorSpan(Color.argb(128,0,128,0)), pares[i*2], pares[i*2+1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                context.runOnUiThread(new Runnable() {
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

    boolean isTarget(int k) {
        for (int t : counts) if (t==k) return true;
        return false;
    }
    class Listner implements View.OnClickListener/*, NestedScrollView.OnScrollChangeListener*/ { // упс, listEner
        @Override
        public void onClick(View v) {
            I.Log("onClick()");
            I.Log("curPos="+curPos+" count="+count);
            switch (v.getId()) {
                case R.id.fab_prev:
                    if (curPos<2) return;
                    curPos--;
                    break;
                case R.id.fab_next:
                    if (curPos>=count) return;
                    curPos++;
                    break;
                default:
                    break;
            }
            Layout layout = textView.getLayout();
            I.Log("curPos-1)*2="+((curPos-1)*2));
            I.Log("pares[(curPos-1)*2]="+pares[(curPos-1)*2]);
            //scrollView.scrollTo(0, layout.getLineTop(counts[curPos-1]));//layout.getLineForOffset(startPos)));
            scrollView.scrollTo(0, layout.getLineTop(layout.getLineForOffset(pares[(curPos-1)*2])));
            counter.setText(curPos+"/"+count);
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
