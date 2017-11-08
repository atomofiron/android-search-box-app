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
import ru.atomofiron.regextool.Utils.RFile;

public class FilesAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, View.OnClickListener {

	private Context co;
	private SharedPreferences sp;
	private final ArrayList<File> filesList = new ArrayList<>();
	private final ArrayList<String> selectedList = new ArrayList<>();
	private RFile curDir = null;
	private OnSelectedListener onSelectedListener = null;
	private FileComparator fileComparator = new FileComparator();

	public FilesAdapter(Context context, ListView listView) {
		this.co = context;
		listView.setOnItemClickListener(this);

		sp = I.sp(co);
		curDir = new RFile("/");
		update();
	}

	public void update(File dir) {
		RFile rfile = new RFile(dir).setUseRoot(sp.getBoolean(I.PREF_USE_ROOT, false));
		if (rfile.containsFiles()) {
			curDir = rfile;
			update(curDir.useRoot);
		}
	}

	public void update() {
		update(sp.getBoolean(I.PREF_USE_ROOT, false));
	}

	private void update(boolean useRoot) {
		filesList.clear();
		curDir.useRoot = useRoot;

		File[] files = curDir.listFiles();
		if (files != null) {
			Collections.addAll(filesList, files);
		}
		Collections.sort(filesList, fileComparator);
		filesList.add(0, curDir.getParentFile() == null ?
				new File("/") : curDir.getParentFile());

		notifyDataSetChanged();
	}

	private void updateSelectedHard() {
		I.log("updateSelectedHard");
		Set<String> set = sp.getStringSet(I.SELECTED_LIST, null);
		selectedList.clear();
		if (set != null && set.size() > 0)
			for (String path : set)
				selectedList.add(path);

		notifyDataSetChanged();
	}
	public void updateSelected() {
		Set<String> set = sp.getStringSet(I.SELECTED_LIST, null);
		if (selectedList.size() > 0 && set == null || set != null && !I.equivalent(selectedList, set))
			updateSelectedHard();
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
		update(filesList.get(position));
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

		if (onSelectedListener != null)
			onSelectedListener.onSelected(file, isChecked);
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

		File file = filesList.get(position);
		String name;
		name = file.getName();
		holder.title.setText(position == 0 ? ".." : name);
		holder.icon.setImageResource(
				!file.isDirectory() ? R.drawable.ic_file :
						RFile.containsFiles(file, curDir.useRoot) ?
								R.drawable.ic_folder : R.drawable.ic_folder_empty);

		if (position != 0) {
			holder.check.setVisibility(View.VISIBLE);
			holder.check.setTag(file);
			holder.check.setChecked(selectedList.contains(file.getAbsolutePath()));
		} else
			holder.check.setVisibility(View.GONE);

		return view;
	}

	public void setOnSelectedListener(OnSelectedListener listener) {
		onSelectedListener = listener;
	}

	public interface OnSelectedListener {
		public void onSelected(File file, boolean selected);
	}

}
