package app.atomofiron.searchboxapp.screens.viewer;

/*

public class TextFragment extends Fragment implements View.OnClickListener {
	private int spanBackgroundColor;
	private BackgroundColorSpan focusSpan;

	private View fragmentView;
	private TextView counter;
	private NestedScrollView scrollView;
	private TextView textView;

	private int curPos = -1;
	private int[][] spanRegions;

	private Result result;

	public static TextFragment newInstance(Bundle bundle) {
		TextFragment textFragment = new TextFragment();
		textFragment.setArguments(bundle);
		return textFragment;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		Resources resources = context.getResources();
		spanBackgroundColor = resources.getColor(R.color.spanBlue);
		focusSpan = new BackgroundColorSpan(resources.getColor(R.color.spanGreen));
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		if (fragmentView != null) {
			ResultsHolder.resetResult();

			return fragmentView;
		}

		final View view = inflater.inflate(R.layout.fragment_text, container, false);

		counter = view.findViewById(R.id.counter);
		textView = view.findViewById(R.id.text);
		scrollView = view.findViewById(R.id.scroll_text);

		result = ResultsHolder.getResult();
		int count = result.size();
		spanRegions = new int[count][];
		counter.setText(String.format("0/%d", count));

		view.findViewById(R.id.fab_prev).setOnClickListener(this);
		view.findViewById(R.id.fab_next).setOnClickListener(this);
		//((NestedScrollView)findViewById(R.id.scroll_text)).setOnScrollChangeListener(listener);
		final RFile file = new RFile(result.path, Util.sp(getContext()).getBoolean(PreferenceKeys.KEY_USE_SU, false));

		if (file.canRead())
			new Thread(new Runnable() {
				@Override
				public void run() {
					final Spannable spanRange = new SpannableString(file.readText());
					int i = -1;
					result.moveToFirst();
					while (result.hasNext()) {
						spanRegions[++i] = result.next();
						spanRange.setSpan(new BackgroundColorSpan(spanBackgroundColor),
								spanRegions[i][0], spanRegions[i][1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					}

					Activity activity = getActivity();
					if (activity != null)
						activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								textView.setText(spanRange, TextView.BufferType.EDITABLE);
								view.findViewById(R.id.progressbar).setVisibility(View.GONE);
								view.findViewById(R.id.fab_layout).setVisibility(spanRegions.length > 0 ? View.VISIBLE : View.GONE);
							}
						});
				}
			}).start();
		else {
			view.findViewById(R.id.progressbar).setVisibility(View.GONE);
			view.findViewById(R.id.label).setVisibility(View.VISIBLE);
		}

		return view;
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);

		ResultsHolder.setResult(result);
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
		scrollView.scrollTo(0, layout.getLineTop(layout.getLineForOffset(spanRegions[curPos][0])) - scrollView.getHeight() / 2);
		counter.setText(String.format("%1$d/%2$d", curPos + 1, spanRegions.length));

		textView.getEditableText().removeSpan(focusSpan);
		textView.getEditableText().setSpan(focusSpan, spanRegions[curPos][0], spanRegions[curPos][1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	}
}*/
