package ru.atomofiron.regextool.Adapters;

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

import ru.atomofiron.regextool.I;
import ru.atomofiron.regextool.R;

public class HistoryAdapter extends BaseAdapter implements View.OnClickListener {

	private Context co;
	private SharedPreferences sp;
	private ArrayList<String> history;
	private Set<String> pinned;
	private ListView listView;

	private OnItemClickListener onItemClickListener;

	public HistoryAdapter(ListView listView, OnItemClickListener listener) {
		this.listView = listView;
		onItemClickListener = listener;

		co = listView.getContext();
		sp = I.SP(co);

		history = new ArrayList<>(sp.getStringSet(I.PREF_HISTORY, new HashSet<String>()));
		pinned = sp.getStringSet(I.PREF_HISTORY_PINNED, new HashSet<String>());

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
		sort();

		history.add(history.remove(node) && pinned.contains(node) ? 0 : pinned.size(), node);

		save(I.PREF_HISTORY);
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
			if (activated)
				pinned.add(title);
			else
				pinned.remove(title);

			save(I.PREF_HISTORY_PINNED);
		} else if (id == R.id.del) {
			if (pinned.remove(title))
				save(I.PREF_HISTORY_PINNED);

			if (history.remove(title))
				save(I.PREF_HISTORY);

			notifyDataSetChanged();
		} else
			onItemClickListener.onItemClick(title);
	}

	private void save(String key) {
		sp.edit().putStringSet(key, key.equals(I.PREF_HISTORY_PINNED) ? pinned : new HashSet<>(history)).apply();
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

		holder.pin.setActivated(pinned.contains(node));
		holder.title.setText(node);

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
