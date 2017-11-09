package ru.atomofiron.regextool.Fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.atomofiron.regextool.I;
import ru.atomofiron.regextool.Models.RFile;
import ru.atomofiron.regextool.Models.Result;
import ru.atomofiron.regextool.R;

public class TextFragment extends Fragment implements View.OnClickListener {

	private View fragmentView;
	private TextView counter;
	private NestedScrollView scrollView;
	private TextView textView;

	private int curPos = -1;
	private int count = 0;
	private int[] startPositions;

	private Result result;

	public static TextFragment newInstance(Bundle bundle) {
		TextFragment textFragment = new TextFragment();
		textFragment.setArguments(bundle);
		return textFragment;
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		if (fragmentView != null)
			return fragmentView;

		final View view = inflater.inflate(R.layout.fragment_text, container, false);

		counter = view.findViewById(R.id.counter);
		textView = view.findViewById(R.id.text);
		scrollView = view.findViewById(R.id.scroll_text);

		result = getArguments().getParcelable(I.RESULT);
		count = result.size();
		startPositions = new int[count];
		counter.setText(String.format("0/%d", count));

		view.findViewById(R.id.fab_prev).setOnClickListener(this);
		view.findViewById(R.id.fab_next).setOnClickListener(this);
		//((NestedScrollView)findViewById(R.id.scroll_text)).setOnScrollChangeListener(listener);

		new Thread(new Runnable() {
			@Override
			public void run() {
				RFile file = new RFile(result.path);
				file.useRoot = I.sp(getContext()).getBoolean(I.PREF_USE_ROOT, false);
				file.tmpDirPath = getContext().getFilesDir().getAbsolutePath();
				final Spannable spanRange = new SpannableString(file.readText());
				int i = 0;
				while (result.hasNext()) {
					int[] pare = result.next();
					startPositions[i++] = pare[0];
					spanRange.setSpan(new BackgroundColorSpan(Color.argb(128, 0, 128, 0)),
							pare[0], pare[1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}

				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						textView.setText(spanRange);
						view.findViewById(R.id.progressbar).setVisibility(View.GONE);
						view.findViewById(R.id.fab_layout).setVisibility(View.VISIBLE);
					}
				});
			}
		}).start();

		return view;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if ((fragmentView = getView()) != null) {
			ViewGroup parent = (ViewGroup) fragmentView.getParent();
			if (parent != null)
				parent.removeView(fragmentView);
		}
	}

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
		counter.setText(String.format("%1$d/%2$d", curPos + 1, count));
	}
}