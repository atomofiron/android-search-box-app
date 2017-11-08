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
    protected final ArrayList<String> pathsList = new ArrayList<>();
	private final ArrayList<String> checkedPathsList = new ArrayList<>();

    public boolean countedNotCheckable = false;

    public ListAdapter(Context context) {
        co = context;
		sp = I.SP(co);
	}

	public void setList(ArrayList<String> paths) {
		pathsList.clear();
		if (paths != null)
			pathsList.addAll(paths);

		notifyDataSetChanged();
	}

	public void setCheckedPathsList(ArrayList<String> list) {
		checkedPathsList.clear();
		if (list != null)
			checkedPathsList.addAll(list);

		notifyDataSetChanged();
	}

	public void remove(int position) {
		checkedPathsList.remove(pathsList.remove(position));

		Set<String> set = new HashSet<>(pathsList);
		sp.edit().putStringSet(I.SELECTED_LIST, set).apply();

		notifyDataSetChanged();
	}

	public void update() {
		pathsList.clear();
		Set<String> set = sp.getStringSet(I.SELECTED_LIST, null);
		if (set != null && set.size() > 0)
			pathsList.addAll(set);

		notifyDataSetChanged();
	}

	public ArrayList<String> getCheckedPathArray() {
		return checkedPathsList;
	}

	public void onItemClick(View view) {
		// учёт отмеченных происходит в onCheckedChanged()
		CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
		checkBox.setChecked(!checkBox.isChecked());
	}

	public int getCheckedCount() {
		return checkedPathsList.size();
	}

	@Override
	public int getCount() {
		return pathsList.size();
	}

	@Override
	public String getItem(int position) {
		return pathsList.get(position);
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

	protected static class ViewHolder {
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

			if (countedNotCheckable) {
				holder.icon.setVisibility(View.GONE);
				holder.count.setVisibility(View.VISIBLE);
				holder.check.setVisibility(View.GONE);
			}

            myView.setTag(holder);
        } else
        	holder = (ViewHolder) myView.getTag();

		String path = pathsList.get(position);
        holder.text.setText(path);
        holder.icon.setImageResource(new File(path).isDirectory() ?
				R.drawable.ic_folder : R.drawable.ic_file);
        holder.check.setTag(path);

		if (!countedNotCheckable) {
			holder.check.setOnCheckedChangeListener(null);
			holder.check.setChecked(checkedPathsList.contains(path));
			holder.check.setOnCheckedChangeListener(this);
		}

		return myView;
    }

}
