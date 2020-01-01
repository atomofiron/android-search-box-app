package ru.atomofiron.regextool.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ru.atomofiron.regextool.Util;
import ru.atomofiron.regextool.R;

public class HistoryAdapter extends BaseAdapter implements View.OnClickListener {
	private static final String PREF_HISTORY = "PREF_HISTORY";
	private static final String PREF_HISTORY_PINNED = "PREF_HISTORY_PINNED";

	private Context co;
	private SharedPreferences sp;
	private ArrayList<String> history = new ArrayList<>();
	private Set<String> pinned;

	private OnItemClickListener onItemClickListener;

	public HistoryAdapter(ListView listView, OnItemClickListener listener) {
		onItemClickListener = listener;

		co = listView.getContext();
		sp = Util.sp(co);

		pinned = sp.getStringSet(PREF_HISTORY_PINNED, new HashSet<String>());

		// переход от Set<String> на мультистрочный String для истории
		if (sp.getAll().get(PREF_HISTORY) instanceof Set) {
			history.addAll(sp.getStringSet(PREF_HISTORY, new HashSet<String>()));
			sp.edit().putStringSet(PREF_HISTORY, null).apply();

			int c = 0;
			for (int i = 0; i < history.size(); i++) {
				String s = history.get(i);
				if (pinned.contains(s) && history.remove(s))
					history.add(c++, s);
			}

			save(PREF_HISTORY);
		} else
			for (String note : sp.getString(PREF_HISTORY, "").split("\n"))
				if (!note.trim().isEmpty())
					history.add(note);

		sort();
	}

	@Override
	public int getCount() {
		return history.size();
	}

	@Override
	public String getItem(int position) {
		return history.get(position);
	}

	public void addItem(String node) {
		if (node.replace(" ", "").isEmpty())
			return;

		history.add(history.remove(node) && pinned.contains(node) ? 0 : pinned.size(), node);

		save(PREF_HISTORY);
		notifyDataSetChanged();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		String title = ((TextView) ((id == R.id.ll) ? v : ((View) v.getParent())).findViewById(R.id.title)).getText().toString(); // omg

		if (id == R.id.pin) {
			boolean activated = !v.isActivated();
			v.setActivated(activated);
			((View) v.getParent()).findViewById(R.id.del).setVisibility(activated ? View.GONE : View.VISIBLE);

			if (activated)
				pinned.add(title);
			else
				pinned.remove(title);

			history.add(history.remove(title) && pinned.contains(title) ? 0 : pinned.size(), title);

			save(PREF_HISTORY_PINNED);
			save(PREF_HISTORY);
			notifyDataSetChanged();
		} else if (id == R.id.del) {
			if (history.remove(title))
				save(PREF_HISTORY);

			notifyDataSetChanged();
		} else
			onItemClickListener.onItemClick(title);
	}

	private void save(String key) {
		if (key.equals(PREF_HISTORY)) {
			StringBuilder stringBuilder = new StringBuilder();
			for (String note : history)
				stringBuilder.append(note).append('\n');

			sp.edit().putString(key, stringBuilder.toString()).apply();
		} else
			sp.edit().putStringSet(key, pinned).apply();
	}

	public void sort() {
		int c = 0;
		for (int i = 0; i < history.size(); i++) {
			String s = history.get(i);
			if (pinned.contains(s) && history.remove(s))
				history.add(c++, s);
		}


		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(co);
			convertView = inflater.inflate(R.layout.item_history, parent, false);

			holder = new ViewHolder(convertView);
			convertView.setTag(holder);

			holder.ll.setOnClickListener(this);
			holder.pin.setOnClickListener(this);
			holder.del.setOnClickListener(this);
		} else
			holder = (ViewHolder) convertView.getTag();

		String node = history.get(position);

		boolean p = pinned.contains(node);
		holder.pin.setActivated(p);
		holder.title.setText(node);
		holder.del.setVisibility(p ? View.GONE : View.VISIBLE);

		return convertView;
	}

	private class ViewHolder {
		LinearLayout ll;
		ImageButton pin;
		TextView title;
		ImageButton del;
		ViewHolder(View v) {
			ll = (LinearLayout) v;
			pin = (ImageButton) v.findViewById(R.id.pin);
			title = (TextView) v.findViewById(R.id.title);
			del = (ImageButton) v.findViewById(R.id.del);
		}
	}

	public interface OnItemClickListener {
		void onItemClick(String node);
	}
}
