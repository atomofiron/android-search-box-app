package ru.atomofiron.regextool.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ru.atomofiron.regextool.I;
import ru.atomofiron.regextool.R;

public class ListAdapter extends BaseAdapter implements CompoundButton.OnCheckedChangeListener { // Почему BaseAdapter, потому что он прост и выполняет свою задачу

    private Context co;
	private SharedPreferences sp;
    private final ArrayList<String> selectedPathsList = new ArrayList<>();
	private final ArrayList<Integer> countsList = new ArrayList<>();
	private final ArrayList<String> checkedPathsList = new ArrayList<>();

    public boolean counted = false;
    public boolean checkable = false;

    public ListAdapter(Context context) {
        co = context;
		sp = I.SP(co);
	}

	public void setCountsList(ArrayList<Integer> countsList) {
		this.countsList.clear();
		if (countsList != null)
			this.countsList.addAll(countsList);
	}

	public void setList(ArrayList<String> paths) {
		selectedPathsList.clear();
		if (paths != null)
			selectedPathsList.addAll(paths);

		notifyDataSetChanged();
	}

	public void remove(int position) {
		checkedPathsList.remove(selectedPathsList.remove(position));

		Set<String> set = new HashSet<>(selectedPathsList);
		sp.edit().putStringSet(I.SELECTED_LIST, set).apply();

		notifyDataSetChanged();
	}

	public void update() {
		selectedPathsList.clear();
		Set<String> set = sp.getStringSet(I.SELECTED_LIST, null);
		if (set != null && set.size() > 0)
			selectedPathsList.addAll(set);

		notifyDataSetChanged();
	}

	public ArrayList<String> getPathArray() {
		return checkedPathsList;
	}

	public void onItemClick(int position) {
		String path = selectedPathsList.get(position);

		if (!checkedPathsList.remove(path)) // я... ну это)
			checkedPathsList.add(path);

		notifyDataSetChanged();
	}

	public int getCheckedCount() {
		return checkedPathsList.size();
	}

	@Override
	public int getCount() {
		return selectedPathsList.size();
	}

	@Override
	public String getItem(int position) {
		return selectedPathsList.get(position);
	}

	@Override
    public long getItemId(int position) {
        return 0;
    }

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		String path = (String)buttonView.getTag();

		if (isChecked) {
			checkedPathsList.add(path);
		} else
			checkedPathsList.remove(path);
	}

	private static class ViewHolder {
        TextView text;
        TextView count;
        ImageView icon;
        CheckBox check;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View myView = convertView;
        ViewHolder holder;

        if (myView == null) {
            LayoutInflater inflater = LayoutInflater.from(co);
            myView = inflater.inflate(R.layout.layout_item, null, true);
            holder = new ViewHolder();
            holder.text = (TextView)myView.findViewById(R.id.title);
            holder.count = (TextView)myView.findViewById(R.id.counter);
            holder.icon = (ImageView)myView.findViewById(R.id.icon);
            holder.check = (CheckBox)myView.findViewById(R.id.checkbox);
            holder.check.setVisibility(counted ? View.GONE : View.VISIBLE);

			if (countsList.size() > position) {
				holder.icon.setVisibility(View.GONE);
				holder.count.setVisibility(View.VISIBLE);
				holder.check.setVisibility(View.GONE);
			}
			if (checkable)
				holder.check.setOnCheckedChangeListener(this);
            myView.setTag(holder);
        } else holder = (ViewHolder) myView.getTag();

		String path = selectedPathsList.get(position);
        holder.text.setText(path);
        holder.icon.setImageResource(new File(path).isDirectory() ?
				R.drawable.ic_folder : R.drawable.ic_file);
        holder.check.setTag(path);

		if (countsList.size() > position)
			holder.count.setText(String.valueOf(countsList.get(position)));
		if (checkable)
        	holder.check.setChecked(checkedPathsList.contains(path));

		return myView;
    }

}
