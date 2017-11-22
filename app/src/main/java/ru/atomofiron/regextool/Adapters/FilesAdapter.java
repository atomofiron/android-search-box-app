package ru.atomofiron.regextool.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ru.atomofiron.regextool.I;
import ru.atomofiron.regextool.R;
import ru.atomofiron.regextool.Utils.FileComparator;
import ru.atomofiron.regextool.Models.RFile;

public class FilesAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, View.OnClickListener {

	private Context co;
	private SharedPreferences sp;
	private final ArrayList<RFile> filesList = new ArrayList<>();
	private final ArrayList<String> selectedList = new ArrayList<>();
	private RFile curDir = null;
	private FileComparator fileComparator = new FileComparator();

	public FilesAdapter(Context context, ListView listView) {
		this.co = context;
		listView.setOnItemClickListener(this);

		sp = I.sp(co);
		update(sp.getString(I.PREF_STORAGE_PATH, "/"));
	}

	private void update(RFile dir) {
		if (dir == null)
			return;

		if (dir.containsFiles() || curDir == null) {
			curDir = dir;
			update();
		}
	}

	private void update(String path) {
		update(new RFile(path).setUseRoot(sp.getBoolean(I.PREF_USE_ROOT, false)));
	}

	public void updateIfNeeded() {
		boolean useRoot = sp.getBoolean(I.PREF_USE_ROOT, false);
		boolean needed = useRoot != curDir.useRoot;

		if (!curDir.setUseRoot(useRoot).canRead()) {
			curDir = new RFile(sp.getString(I.PREF_STORAGE_PATH, "/"));
			needed = true;
		}

		if (needed)
			update();
	}

	public void update() {
		filesList.clear();

		RFile[] files = curDir.listFiles();
		if (files != null)
			for (RFile rFile : files) {
				filesList.add(rFile);
				rFile.flag = rFile.containsFiles();
			}

		Collections.sort(filesList, fileComparator);
		filesList.add(0, curDir);
		curDir.flag = curDir.getParentFile() != null && curDir.getParentFile().canRead();

		notifyDataSetChanged();
	}

	public void updateSelected() {
		Set<String> set = sp.getStringSet(I.SELECTED_LIST, null);
		if (selectedList.size() > 0 && set == null || set != null && !I.equivalent(selectedList, set)) {
			selectedList.clear();
			if (set != null && set.size() > 0)
				selectedList.addAll(set);

			notifyDataSetChanged();
		}
	}

	@Override
	public int getCount() {
		return filesList.size();
	}

	@Override
	public File getItem(int position) {
		return filesList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		RFile lastDir = curDir;
		update(position == 0 ? filesList.get(position).getParentFile() : filesList.get(position));

		if (lastDir != curDir)
			parent.setSelection(position == 0 ? filesList.indexOf(lastDir) : 0);
	}

	@Override
	public void onClick(View v) {
		boolean isChecked = ((CheckBox)v).isChecked();

		File file = (File)v.getTag();
		if (isChecked)
			selectedList.add(file.getAbsolutePath());
		else
			selectedList.remove(file.getAbsolutePath());

		Set<String> set = new HashSet<>(selectedList);
		sp.edit().putStringSet(I.SELECTED_LIST, set).apply();
	}

	private static class ViewHolder {
		TextView title;
		ImageView icon;
		CheckBox check;
		ViewHolder(View view) {
			title = (TextView)view.findViewById(R.id.title);
			icon = (ImageView)view.findViewById(R.id.icon);
			check = (CheckBox)view.findViewById(R.id.checkbox);
		}
	}
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ViewHolder holder;

		if (view == null) {
			LayoutInflater inflater = LayoutInflater.from(co);
			view = inflater.inflate(R.layout.layout_item, null, true);
			holder = new ViewHolder(view);

			holder.check.setVisibility(View.VISIBLE);
			holder.check.setOnClickListener(this);
			view.setTag(holder);
		} else
			holder = (ViewHolder) view.getTag();

		RFile rFile = filesList.get(position);
		holder.title.setText(position == 0 ? rFile.getParent() + " [" + rFile.getName() + "]" : rFile.getName());
		holder.icon.setImageResource(
				!rFile.isDirectory() ? R.drawable.ic_file :
						rFile.flag ? R.drawable.ic_folder : R.drawable.ic_folder_empty);

		holder.check.setTag(rFile);
		holder.check.setChecked(selectedList.contains(rFile.getAbsolutePath()));

		return view;
	}
}
