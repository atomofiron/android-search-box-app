package ru.atomofiron.regextool.Fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

import ru.atomofiron.regextool.Adapters.ListAdapter;
import ru.atomofiron.regextool.I;
import ru.atomofiron.regextool.R;
import ru.atomofiron.regextool.TextActivity;

public class ResultsFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

	private Activity ac;
	private ListView rootView;
	private ListAdapter listAdapter;
	private I.OnSnackListener onSnackListener;

	private ArrayList<String> resultsList;

	public ResultsFragment() {}

	public static ResultsFragment newInstance(Bundle bundle) {
		ResultsFragment fragment = new ResultsFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		ac = getActivity();

		if (rootView == null) {
			rootView = new ListView(ac);
			resultsList = getArguments().getStringArrayList(I.RESULT_LIST);

			listAdapter = new ListAdapter(ac);
			listAdapter.setList(resultsList);
			listAdapter.setCountsList(getArguments().getIntegerArrayList(I.RESULT_LIST_COUNTS));

			rootView.setOnItemLongClickListener(this);
			rootView.setOnItemClickListener(this);
			rootView.setAdapter(listAdapter);
		}
		return rootView;
	}


	private void Snack(final String str) {
		if (onSnackListener != null)
			onSnackListener.onSnack(str, R.string.copy, new View.OnClickListener() {
				public void onClick(View v) {
					((android.content.ClipboardManager) ac.getSystemService(Context.CLIPBOARD_SERVICE))
							.setPrimaryClip(android.content.ClipData.newPlainText("RegexFinder", str));
					I.Toast(ac, R.string.copied);
				}
			});
	}

	public void setOnSnackListener(I.OnSnackListener listener) {
		onSnackListener = listener;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		File file = new File(listAdapter.getItem(position));
		String format = I.getFormat(file.getName());
		String[] extra = I.SP(getActivity()).getString(I.PREF_EXTRA_FORMATS, "").split(" ");

		if (I.isTextFile(format, extra)) {
			ArrayList<String> resultLinePositions = getArguments().getStringArrayList(I.RESULT_LINE_NUMS);
			String lineNums = resultLinePositions != null && resultLinePositions.size() > 0 ?
					resultLinePositions.get(position) : "0";

			startActivity(new Intent(ac, TextActivity.class)
					.putExtra(I.SEARCH_REGEX, getArguments().getBoolean(I.SEARCH_REGEX))
					.putExtra(I.RESULT_PATH, resultsList.get(position))
					.putExtra(I.TARGET, getArguments().getString(I.TARGET))
					.putExtra(I.RESULT_LINE_NUMS, lineNums));
		} else
			try {
				Uri uri = Build.VERSION.SDK_INT < 24 ? Uri.fromFile(file) :
						FileProvider.getUriForFile(ac, ac.getApplicationContext().getPackageName() + ".provider", file);
				Intent intent = new Intent()
						.setAction(android.content.Intent.ACTION_VIEW)
						.setDataAndType(uri, MimeTypeMap.getSingleton().getMimeTypeFromExtension(format));
				if (intent.resolveActivity(ac.getPackageManager()) != null)
					startActivityForResult(intent, 10);
				else
					Snack(getString(R.string.no_activity));
			} catch (Exception e) {
				if (e.getMessage().startsWith("Failed to find configured root that contains")) {
					I.Toast(ac, R.string.fucking_provider, true);
					Snack(file.getAbsolutePath());
				} else
					I.Toast(ac, R.string.error);
			}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		Snack(listAdapter.getItem(position));
		return false;
	}
}
