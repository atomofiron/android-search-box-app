package ru.atomofiron.regextool.screens.result.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import ru.atomofiron.regextool.utils.finder.Result;

public class ResultAdapter extends ListAdapter {
	private final ArrayList<Result> resultsList = new ArrayList<>();

	public ResultAdapter(Context context) {
		super(context);
		checkable = false;
	}

	public void setResults(ArrayList<Result> results) {
		resultsList.clear();
		pathsList.clear();

		for (Result result : results) {
			resultsList.add(result);
			pathsList.add(result.path);
		}

		counted = results.size() > 0 && !results.get(0).isEmpty();
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);

		if (counted) {
			ViewHolder holder = (ViewHolder) view.getTag();
			holder.count.setText(String.valueOf(resultsList.get(position).size()));
			holder.icon.setVisibility(View.GONE);
		}

		return view;
	}
}
