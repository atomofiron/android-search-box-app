package ru.atomofiron.regextool.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import ru.atomofiron.regextool.Models.Result;

public class ResultAdapter extends ListAdapter {
	private final ArrayList<Result> resultsList = new ArrayList<>();

	public ResultAdapter(Context context) {
		super(context);
		countedNotCheckable = true;
	}

	public void setResults(ArrayList<Result> results) {
		resultsList.clear();
		pathsList.clear();

		for (Result result : results) {
			resultsList.add(result);
			pathsList.add(result.path);
		}

		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);

		((ViewHolder) view.getTag()).count.setText(String.valueOf(resultsList.get(position).size()));

		return view;
	}
}
