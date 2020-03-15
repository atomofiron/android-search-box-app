package ru.atomofiron.regextool.screens.result.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ru.atomofiron.regextool.utils.Const;
import ru.atomofiron.regextool.R;
import ru.atomofiron.regextool.utils.Util;

public class ListAdapter extends BaseAdapter implements CompoundButton.OnCheckedChangeListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener { // Почему BaseAdapter, потому что он прост и выполняет свою задачу

    private Context co;
	private SharedPreferences sp;
    protected final ArrayList<String> pathsList = new ArrayList<>();
	private final ArrayList<String> checkedPathsList = new ArrayList<>();

    protected boolean counted = false;
    protected boolean checkable = true;

    public ListAdapter(Context context) {
        co = context;
		sp = Util.sp(co);
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

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
		checkedPathsList.remove(pathsList.remove(i));

		Set<String> set = new HashSet<>(pathsList);
		sp.edit().putStringSet(Const.SELECTED_LIST, set).apply();

		notifyDataSetChanged();
		return true;
	}

	public void update() {
		pathsList.clear();
		Set<String> set = sp.getStringSet(Const.SELECTED_LIST, null);
		if (set != null)
			for (String path : set)
				if (new File(path).exists())
					pathsList.add(path);

		for (int i = 0; i < checkedPathsList.size(); i++)
			if (!pathsList.contains(checkedPathsList.get(i)))
				checkedPathsList.remove(i--);

		notifyDataSetChanged();
	}

	public ArrayList<String> getCheckedPathArray() {
		return checkedPathsList;
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
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
			int size = checkedPathsList.size();
			for (int i = 0; i < checkedPathsList.size(); i++) {
				String checkedPath = checkedPathsList.get(i);
				if (path.startsWith(checkedPath) || checkedPath.startsWith(path))
					checkedPathsList.remove(i--);
			}
			checkedPathsList.add(path);

			if (size >= checkedPathsList.size())
				notifyDataSetChanged();
		} else
			checkedPathsList.remove(path);
	}

	protected static class ViewHolder {
        public TextView text;
		public TextView count;
		public ImageView icon;
		public CheckBox check;
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

			holder.icon.setVisibility(counted ? View.GONE : View.VISIBLE);
			holder.count.setVisibility(counted ? View.VISIBLE : View.GONE);
			holder.check.setVisibility(checkable ? View.VISIBLE : View.GONE);

            myView.setTag(holder);
        } else
        	holder = (ViewHolder) myView.getTag();

		String path = pathsList.get(position);
        holder.text.setText(path);
        if (new File(path).isDirectory())
        	holder.icon.setImageResource(R.drawable.ic_explorer_folder);
        holder.check.setTag(path);

		if (checkable) {
			holder.check.setOnCheckedChangeListener(null);
			holder.check.setChecked(checkedPathsList.contains(path));
			holder.check.setOnCheckedChangeListener(this);
		}

		return myView;
    }

}
