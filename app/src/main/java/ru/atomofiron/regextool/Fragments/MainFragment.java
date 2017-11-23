package ru.atomofiron.regextool.Fragments;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.regex.Pattern;

import ru.atomofiron.regextool.Adapters.FilesAdapter;
import ru.atomofiron.regextool.Adapters.HistoryAdapter;
import ru.atomofiron.regextool.Adapters.ListAdapter;
import ru.atomofiron.regextool.Adapters.ViewPagerAdapter;
import ru.atomofiron.regextool.CustomViews.RegexText;
import ru.atomofiron.regextool.I;
import ru.atomofiron.regextool.MainActivity;
import ru.atomofiron.regextool.R;
import ru.atomofiron.regextool.SearchService;
import ru.atomofiron.regextool.Utils.PermissionHelper;
import ru.atomofiron.regextool.Utils.SnackbarHelper;


public class MainFragment extends Fragment implements View.OnClickListener {
	private static final int REQUEST_FOR_PROVIDER = 1;
	private static final int REQUEST_FOR_SEARCH = 2;

	public static final int SEARCH_NOTHING = 0;
	public static final int SEARCH_ERROR = -1;
	public static final int NOTIFICATION_ID = 2;

	public static final String ACTION_RESULTS = "ACTION_RESULTS";
	public static final String KEY_ERROR_MESSAGE = "KEY_ERROR_MESSAGE";
	public static final String KEY_NOTICE = "KEY_NOTICE";
	public static final String KEY_NOTICE_CURRENT = "KEY_NOTICE_CURRENT";

	private static final String KEY_QUERY = "KEY_QUERY";
	private static final String KEY_TEST = "KEY_TEST";
	private static final String KEY_SELECTED = "KEY_SELECTED";
	private static final String KEY_FLAG_CASE = "KEY_FLAG_CASE";
	private static final String KEY_FLAG_IN_FILES = "KEY_FLAG_IN_FILES";
	private static final String KEY_FLAG_REGEXP = "KEY_FLAG_REGEXP";
	private static final String KEY_FLAG_MULTILINE = "KEY_FLAG_MULTILINE";

	private Context co;
	private View fragmentView;
	private SnackbarHelper snackbarHelper;

	private RegexText regexText;
	private CheckBox caseToggle;
	private CheckBox contentToggle;
	private CheckBox regexToggle;
	private CheckBox multilineToggle;
	private ViewPager viewPager;
	private EditText testField;
	private ListView filesListView;

	private ListAdapter selectedListAdapter;
	private LocalBroadcastManager broadcastManager;
	private Receiver resultReceiver;
	private SharedPreferences sp;
	private HistoryAdapter historyAdapter;

	public MainFragment() {}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		co = getContext();
		sp = I.sp(co);

		resultReceiver = new Receiver(co);
		broadcastManager = LocalBroadcastManager.getInstance(co);
		broadcastManager.registerReceiver(resultReceiver, new IntentFilter(ACTION_RESULTS));
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_main, menu);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if ((fragmentView = getView()) != null) {
			ViewGroup parent = (ViewGroup) fragmentView.getParent();
			if (parent != null)
				parent.removeView(fragmentView);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		if (fragmentView != null) {
			if (PrefsFragment.changedPrefs.remove(I.PREF_SPECIAL_CHARACTERS))
				initCharacterButtons((ViewGroup) fragmentView.findViewById(R.id.characters_pane));

			return fragmentView;
		}

		View view = inflater.inflate(R.layout.fragment_main, container, false);

		regexText = (RegexText) view.findViewById(R.id.regex_text);
		caseToggle = (CheckBox) view.findViewById(R.id.case_sense);
		contentToggle = (CheckBox) view.findViewById(R.id.in_files);
		regexToggle = (CheckBox) view.findViewById(R.id.simple_search);
		multilineToggle = (CheckBox) view.findViewById(R.id.multiline);

		caseToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				regexText.setCaseSense(isChecked);
			}
		});
		regexToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				regexText.setRegex(isChecked);
				multilineToggle.setEnabled(isChecked && contentToggle.isChecked());
			}
		});
		contentToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				multilineToggle.setEnabled(isChecked && regexToggle.isChecked());
			}
		});
		multilineToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				regexText.setMultiline(isChecked);
			}
		});

		initCharacterButtons((ViewGroup) view.findViewById(R.id.characters_pane));

		viewPager = (ViewPager) view.findViewById(R.id.view_pager);
		ArrayList<View> viewList = new ArrayList<>();

		ListView selectedListView = new ListView(co);
		selectedListAdapter = new ListAdapter(co);
		selectedListAdapter.update();
		selectedListView.setAdapter(selectedListAdapter);
		selectedListView.setOnItemLongClickListener(selectedListAdapter);
		selectedListView.setOnItemClickListener(selectedListAdapter);

		filesListView = new ListView(co);
		final FilesAdapter filesListAdapter = new FilesAdapter(co, filesListView);
		filesListView.setAdapter(filesListAdapter);

		testField = (EditText) LayoutInflater.from(co).inflate(R.layout.edittext_test, null);
		regexText.setTestField(testField);

		viewList.add(testField);
		viewList.add(selectedListView);
		viewList.add(filesListView);
		ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(co, viewList);
		viewPager.setAdapter(pagerAdapter);

		((TabLayout) view.findViewById(R.id.tab_layout)).setupWithViewPager(viewPager);

		viewPager.setCurrentItem(1);
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
			public void onPageSelected(int position) {
				if (position == 1)
					selectedListAdapter.update();
				else if (position == 2)
					if (PermissionHelper.checkPerm(MainFragment.this, REQUEST_FOR_PROVIDER))
						filesListAdapter.updateSelected();
			}
			public void onPageScrollStateChanged(int state) {}
		});

		snackbarHelper = new SnackbarHelper(view);
		return view;
	}

	private void initCharacterButtons(ViewGroup pane) {
		pane.removeAllViews();

		String[] characters = sp.getString(I.PREF_SPECIAL_CHARACTERS, I.DEFAULT_SPECIAL_CHARACTERS)
				.trim().split("[ ]+");

		if (characters.length > 0 && !characters[0].isEmpty())
			for (String c : characters) {
				Button view = (Button) LayoutInflater.from(co).inflate(R.layout.button_character, pane, false);
				view.setText(c);
				view.setOnClickListener(this);
				pane.addView(view);
			}
	}

	public void setDrawerViewWithHistory(final DrawerLayout drawerView) {
		ListView historyList = drawerView.findViewById(R.id.history_list);
		historyAdapter = new HistoryAdapter(historyList, new HistoryAdapter.OnItemClickListener() {
			public void onItemClick(String node) {
				regexText.setText(node);
				regexText.setSelection(node.length());
				drawerView.closeDrawer(GravityCompat.START);
			}
		});
		historyList.setAdapter(historyAdapter);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString(KEY_QUERY, regexText.getText().toString());
		outState.putString(KEY_TEST, testField.getText().toString());
		outState.putStringArrayList(KEY_SELECTED, selectedListAdapter.getCheckedPathArray());
		outState.putBoolean(KEY_FLAG_CASE, caseToggle.isChecked());
		outState.putBoolean(KEY_FLAG_IN_FILES, contentToggle.isChecked());
		outState.putBoolean(KEY_FLAG_REGEXP, regexToggle.isChecked());
		outState.putBoolean(KEY_FLAG_MULTILINE, multilineToggle.isChecked());
	}

	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);

		if (savedInstanceState == null)
			return;

		regexText.setText(savedInstanceState.getString(KEY_QUERY, ""));
		testField.setText(savedInstanceState.getString(KEY_TEST, ""));
		selectedListAdapter.setCheckedPathsList(savedInstanceState.getStringArrayList(KEY_SELECTED));
		caseToggle.setChecked(savedInstanceState.getBoolean(KEY_FLAG_CASE));
		contentToggle.setChecked(savedInstanceState.getBoolean(KEY_FLAG_IN_FILES));
		regexToggle.setChecked(savedInstanceState.getBoolean(KEY_FLAG_REGEXP));
		multilineToggle.setChecked(savedInstanceState.getBoolean(KEY_FLAG_MULTILINE));
	}

	@Override
	public void onStart() {
		super.onStart();

		((FilesAdapter) filesListView.getAdapter()).refreshIfNeeded();
	}

	public void checkListForSearch() {
		if (selectedListAdapter.getCheckedCount() == 0)
			snackbarHelper.show(R.string.no_checked);
		else
			search();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			if (requestCode == REQUEST_FOR_SEARCH)
				checkListForSearch();
			else if (requestCode == REQUEST_FOR_PROVIDER)
				((FilesAdapter) filesListView.getAdapter()).refresh();
		} else if (requestCode == REQUEST_FOR_PROVIDER)
			if (!sp.getBoolean(I.PREF_USE_SU, false))
				viewPager.setCurrentItem(1);
	}

	public void search() {
		resultReceiver.counterView.setText("0/0");
		resultReceiver.processDialog.show();

		co.startService(new Intent(co, SearchService.class)
				.putExtra(I.CASE_SENSE, caseToggle.isChecked())
				.putExtra(I.SEARCH_LIST, selectedListAdapter.getCheckedPathArray())
				.putExtra(I.QUERY, regexText.getText().toString())
				.putExtra(I.SEARCH_IN_FILES, contentToggle.isChecked())
				.putExtra(I.SEARCH_REGEX, regexToggle.isChecked())
				.putExtra(I.MULTILINE, multilineToggle.isChecked()));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		broadcastManager.unregisterReceiver(resultReceiver);
		co.stopService(new Intent(co, SearchService.class));
	}

	@Override
	public void onClick(View v) {
		String symbol;
		switch (v.getId()) {
			case R.id.go:
				String regex = regexText.getText().toString();
				if (regex.isEmpty())
					return;

				historyAdapter.addItem(regex);

				if (regexToggle.isChecked())
					try { Pattern.compile(regex);
					} catch (Exception ignored) {
						snackbarHelper.show(R.string.bad_ex);
						return;
					}

				if (PermissionHelper.checkPerm(MainFragment.this, REQUEST_FOR_SEARCH))
					checkListForSearch();

				return;
			default:
				symbol = ((Button)v).getText().toString();
				break;
		}
		int start = regexText.getSelectionStart();
		int end = regexText.getSelectionEnd();

		if (start != end)
			regexText.getText().replace(start, end, "");

		regexText.getText().insert(start, symbol);
		regexText.setSelection(start + symbol.length(), start + symbol.length());
	}

	class Receiver extends BroadcastReceiver {
		private AlertDialog processDialog;
		private TextView counterView;
		private TextView currentView;

		Receiver(Context co) {
			View view = LayoutInflater.from(MainFragment.this.co).inflate(R.layout.layout_searching, null);
			counterView = view.findViewById(R.id.counter);
			currentView = view.findViewById(R.id.current);
			processDialog = new AlertDialog.Builder(co)
					.setView(view)
					.setCancelable(false)
					.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							SearchService.needToSendResults = false;
							MainFragment.this.co.stopService(new Intent(MainFragment.this.co, SearchService.class));
						}
					})
					.setNegativeButton(R.string.stop, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							MainFragment.this.co.stopService(new Intent(MainFragment.this.co, SearchService.class));
						}
					})
					.create();
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			if (!intent.getExtras().containsKey(KEY_NOTICE)) {
				processDialog.cancel();

				String text;
				int count = intent.getIntExtra(I.SEARCH_COUNT, SEARCH_ERROR);
				switch (count) {
					case SEARCH_ERROR:
						snackbarHelper.show(intent.getStringExtra(KEY_ERROR_MESSAGE));

						text = getString(R.string.error);
						break;
					case SEARCH_NOTHING:
						snackbarHelper.show(text = getString(R.string.nothing));
						break;
					default:
						startActivity(
								new Intent(co, MainActivity.class)
										.setAction(MainActivity.ACTION_SHOW_RESULTS)
										.putExtras(intent.getExtras())
						);

						text = getString(R.string.results, count);
						break;
				}

				if (!isResumed())
					showNotification(text);
			} else {
				counterView.setText(intent.getStringExtra(KEY_NOTICE));
				currentView.setText(intent.getStringExtra(KEY_NOTICE_CURRENT));
			}
		}

		private void showNotification(String text) {
			NotificationManager notifier = (NotificationManager) co.getSystemService(Context.NOTIFICATION_SERVICE);
			if (notifier != null)
				notifier.notify(NOTIFICATION_ID, new NotificationCompat.Builder(co, I.NOTIFICATION_CHANNEL_ID)
						.setTicker(getString(R.string.search_completed))
						.setContentTitle(getString(R.string.search_completed))
						.setContentText(text)
						.setSmallIcon(R.drawable.ic_search_file_done)
						.setColor(getResources().getColor(R.color.colorPrimaryLight))
						.setContentIntent(PendingIntent.getActivity(
								co,
								0,
								new Intent(co, MainActivity.class),
								PendingIntent.FLAG_UPDATE_CURRENT
						)).build()
				);
		}
	}
}
