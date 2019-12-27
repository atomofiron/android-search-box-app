package ru.atomofiron.regextool.adapters;

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
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ru.atomofiron.regextool.I;
import ru.atomofiron.regextool.R;
import ru.atomofiron.regextool.utils.FileComparator;
import ru.atomofiron.regextool.models.RFile;

public class FilesAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, CompoundButton.OnCheckedChangeListener {

	private final Context co;
	private final SharedPreferences sp;
	private final ArrayList<RFile> filesList = new ArrayList<>();
	private final ArrayList<String> selectedList = new ArrayList<>();
	private final FileComparator fileComparator = new FileComparator();
	private RFile curDir = null;

	public FilesAdapter(Context context, ListView listView) {
		this.co = context;
		listView.setOnItemClickListener(this);

		sp = I.sp(co);
		setDir(getDefaultDir());
	}

	private RFile getDefaultDir() {
		boolean useSu = sp.getBoolean(I.PREF_USE_SU, false);
		RFile rFile = new RFile(sp.getString(I.PREF_STORAGE_PATH, RFile.ROOT), useSu);
		// защита от дурака, который может указать несуществующую директорию или файл
		return (rFile.isDirectory() ? rFile : new RFile(RFile.ROOT, useSu));
	}

	private void setDir(RFile dir) {
		if (dir != null) {
			curDir = dir;
			refresh();
		}
	}

	public void refreshIfNeeded() {
		boolean useSu = sp.getBoolean(I.PREF_USE_SU, false);
		boolean needed = curDir.setUseSu(useSu);

		if (!curDir.canRead()) {
			curDir = getDefaultDir();
			needed = true;
		}

		if (needed)
			refresh();
	}

	public void refresh() {
		filesList.clear();

		RFile[] files = curDir.listFiles();
		if (files != null)
			for (RFile rFile : files) {
				filesList.add(rFile);
				rFile.flag = rFile.containsFiles();
			}

		Collections.sort(filesList, fileComparator);

		RFile parent = curDir.getParentFile();
		if (parent != null) {
			filesList.add(0, curDir);
			curDir.flag = parent.canRead();
		}

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
		RFile rFile = filesList.get(position);
		if (!rFile.flag)
			return;

		setDir(position == 0 && !curDir.isRoot() ? rFile.getParentFile() : rFile);

		if (position != 0)
			parent.setSelection(0);
	}

	@Override
	public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
		if (isChecked)
			selectedList.add((String) compoundButton.getTag());
		else
			selectedList.remove((String) compoundButton.getTag());

		sp.edit().putStringSet(I.SELECTED_LIST, new HashSet<>(selectedList)).apply();
	}

	private static class ViewHolder {
		ImageView icon;
		TextView title;
		CheckBox check;

		ViewHolder(View view) {
			icon = view.findViewById(R.id.icon);
			title = view.findViewById(R.id.title);
			check = view.findViewById(R.id.checkbox);
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
			holder.check.setOnCheckedChangeListener(this);
			view.setTag(holder);
		} else
			holder = (ViewHolder) view.getTag();

		RFile rFile = filesList.get(position);
		holder.title.setText(position == 0 && !curDir.isRoot() ?
				rFile.getParent() + " [" + rFile.getName() + "]" : rFile.getName());
		holder.icon.setImageResource(
				!rFile.isDirectory() ? R.drawable.ic_file :
						rFile.flag ? R.drawable.ic_folder : R.drawable.ic_folder_empty);

		holder.check.setTag(rFile.getAbsolutePath());
		holder.check.setChecked(selectedList.contains(rFile.getAbsolutePath()));

		return view;
	}
}
