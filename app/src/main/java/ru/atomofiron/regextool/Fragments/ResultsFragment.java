package ru.atomofiron.regextool.Fragments;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
	private I.SnackListener snackListener;

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
			I.Log("resultsList: "+ resultsList.toString());

			listAdapter = new ListAdapter(ac);
			listAdapter.counted = true;
			listAdapter.setCountsList(getArguments().getStringArrayList(I.RESULT_LIST_COUNTS));
			listAdapter.setList(resultsList);

			rootView.setOnItemLongClickListener(this);
			rootView.setOnItemClickListener(this);
			rootView.setAdapter(listAdapter);
		}
		return rootView;
	}


	private void Snack(String str) {
		if (snackListener != null)
			snackListener.Snack(str);
	}

	public void setSnackListener(I.SnackListener listener) {
		snackListener = listener;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		File file = new File(listAdapter.getItem(position));
		String name = file.getName();

		if (name.contains("."))
			name = name.substring(name.lastIndexOf('.')+1);
		String format = name;
		name = MimeTypeMap.getSingleton().getMimeTypeFromExtension(name);
		Intent intent = new Intent()
				.setAction(android.content.Intent.ACTION_VIEW)
				.setDataAndType(Uri.fromFile(file), name);
		if (I.isTextFile(format))
			startActivity(new Intent(ac, TextActivity.class)
					.putExtra(I.RESULT_PATH, resultsList.get(position))
					.putExtra(I.TARGET, getArguments().getString(I.TARGET))
					.putExtra(I.RESULT_LINE_COUNTS,
							getArguments().getStringArrayList(I.RESULT_LIST_LINE_POSITIONS).get(position)));
		else if (intent.resolveActivity(ac.getPackageManager()) != null)
			startActivityForResult(intent, 10);
		else
			Snack(getString(R.string.no_activity));
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		Snack(listAdapter.getItem(position));
		return false;
	}
}
