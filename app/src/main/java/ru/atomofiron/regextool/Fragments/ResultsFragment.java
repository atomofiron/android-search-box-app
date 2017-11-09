package ru.atomofiron.regextool.Fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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

import ru.atomofiron.regextool.Adapters.ResultAdapter;
import ru.atomofiron.regextool.I;
import ru.atomofiron.regextool.R;
import ru.atomofiron.regextool.TextActivity;
import ru.atomofiron.regextool.Models.Result;
import ru.atomofiron.regextool.Utils.SnackbarHelper;

public class ResultsFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

	private Activity ac;
	private ListView fragmentView;
	private ResultAdapter listAdapter;
	private SnackbarHelper snackbarHelper;

	private ArrayList<Result> resultsList;
	private String startMessage = null;

	public ResultsFragment() {}

	public static ResultsFragment newInstance(Bundle bundle) {
		ResultsFragment fragment = new ResultsFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if ((fragmentView = (ListView) getView()) != null) {
			ViewGroup parent = (ViewGroup) fragmentView.getParent();
			if (parent != null)
				parent.removeView(fragmentView);
		}
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		ac = getActivity();

		if (fragmentView == null) {
			fragmentView = new ListView(ac);
			resultsList = getArguments().getParcelableArrayList(I.RESULT_LIST);

			listAdapter = new ResultAdapter(ac);
			listAdapter.setResults(resultsList);

			fragmentView.setOnItemLongClickListener(this);
			fragmentView.setOnItemClickListener(this);
			fragmentView.setAdapter(listAdapter);

			snackbarHelper = new SnackbarHelper(fragmentView);
			startMessage = getString(R.string.results, getArguments().getInt(I.SEARCH_CODE));
		}
		return fragmentView;
	}

	@Override
	public void onStart() {
		super.onStart();

		if (startMessage != null) {
			snackbarHelper.show(startMessage);
			startMessage = null;
		}
	}

	private void showPathWithCopyAction(final String str) {
		snackbarHelper.show(str, R.string.copy, true, new View.OnClickListener() {
			public void onClick(View v) {
				((android.content.ClipboardManager) ac.getSystemService(Context.CLIPBOARD_SERVICE))
						.setPrimaryClip(android.content.ClipData.newPlainText("RegexFinder", str));
				I.toast(ac, R.string.copied);
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		File file = new File(listAdapter.getItem(position));
		String format = I.getFormat(file.getName());
		String[] extra = I.sp(getActivity()).getString(I.PREF_EXTRA_FORMATS, "").split(" ");

		if (I.isTextFile(format, extra)) {
			Intent intent = new Intent(ac, TextActivity.class);
			intent.putExtra(I.RESULT, resultsList.get(position));
			startActivity(intent);
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
					snackbarHelper.show(R.string.no_activity);
			} catch (Exception e) {
				if (e.getMessage().startsWith("Failed to find configured root that contains")) {
					I.toast(ac, R.string.fucking_provider, true);
					showPathWithCopyAction(file.getAbsolutePath());
				} else
					I.toast(ac, R.string.error);
			}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		showPathWithCopyAction(listAdapter.getItem(position));
		return true;
	}
}
