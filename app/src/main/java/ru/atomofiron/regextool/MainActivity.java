package ru.atomofiron.regextool;

import android.animation.ValueAnimator;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;

import ru.atomofiron.regextool.Fragments.MainFragment;
import ru.atomofiron.regextool.Fragments.PrefsFragment;
import ru.atomofiron.regextool.Fragments.ResultsFragment;
import ru.atomofiron.regextool.Fragments.TextFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
	public static final String ACTION_SHOW_RESULTS = "ACTION_SHOW_RESULTS";
	public static final String ACTION_SHOW_RESULT = "ACTION_SHOW_RESULT";

	private FragmentManager fragmentManager;
	private DrawerLayout drawer;
	private SharedPreferences sp;

	private ValueAnimator animArrowOn;
	private ValueAnimator animArrowOff;
	private boolean arrowIsShown = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		sp = I.sp(this);
		setRequestedOrientation(Integer.parseInt(sp.getString(I.PREF_ORIENTATION, "2")) - 1);

		if (sp.getString(I.PREF_THEME, "0").equals("0"))
			setTheme(R.style.AppTheme_Light);

		setContentView(R.layout.activity_main);

		init();
		updatePrefs();
	}

	private void updatePrefs() {
		SharedPreferences.Editor editor = sp.edit();

		editor
				.putString(I.PREF_STORAGE_PATH, sp.getString(I.PREF_STORAGE_PATH,
						Environment.getExternalStorageDirectory().getAbsolutePath()))
				.putString(I.PREF_EXTRA_FORMATS, sp.getString(I.PREF_EXTRA_FORMATS,
						"md mkd markdown cm ad adoc"));

		// каскадное обновление настроек в зависимости от номера версии
		switch (sp.getInt(I.PREF_LAST_VERSION, 0)) {
			case 0: // 0 - 7
				editor.putInt(I.PREF_MAX_SIZE, sp.getInt(I.PREF_MAX_SIZE, 10) * 1048576);
			default:
				editor.putInt(I.PREF_LAST_VERSION, BuildConfig.VERSION_CODE);
		}

		editor.apply();
	}

	private void init() {
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.setDrawerListener(toggle);
		toggle.syncState();
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (arrowIsShown)
					onBackPressed();
				else if (drawer.isDrawerOpen(GravityCompat.START))
					drawer.closeDrawer(GravityCompat.START, true);
				else
					drawer.openDrawer(GravityCompat.START, true);
			}
		});

		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);

		// init arrow animation //

		ValueAnimator.AnimatorUpdateListener arrowListener = new ValueAnimator.AnimatorUpdateListener() {
			public void onAnimationUpdate(ValueAnimator animation) {
				toggle.onDrawerSlide(drawer, (Float) animation.getAnimatedValue());
			}
		};
		animArrowOn = ValueAnimator.ofFloat(0.0f, 1.0f);
		animArrowOn.addUpdateListener(arrowListener);
		animArrowOn.setInterpolator(new DecelerateInterpolator());
		animArrowOn.setDuration(300);
		animArrowOff = ValueAnimator.ofFloat(1.0f, 0.0f);
		animArrowOff.addUpdateListener(arrowListener);
		animArrowOff.setInterpolator(new DecelerateInterpolator());
		animArrowOff.setDuration(300);

		// other //

		MainFragment mainFragment;
		fragmentManager = getSupportFragmentManager();

		if (fragmentManager.findFragmentById(R.id.container) == null)
			setFragment(mainFragment = new MainFragment(), false);
		else
			mainFragment = (MainFragment) fragmentManager.findFragmentByTag(MainFragment.class.getName());

		mainFragment.setDrawerViewWithHistory(drawer);

		if (fragmentManager.getBackStackEntryCount() > 0)
			showArrow(true);

		SharedPreferences sp = I.sp(this);
		if (sp.getBoolean(I.PREF_FIRST_START, true)) {
			I.showHelp(this);
			sp.edit().putBoolean(I.PREF_FIRST_START, false).apply();
		}
	}

	public void showArrow(final boolean showArrow) {
		if (showArrow == arrowIsShown)
			return;
		arrowIsShown = showArrow;

		if (showArrow)
			animArrowOn.start();
		else
			animArrowOff.start();

		drawer.setDrawerLockMode(showArrow ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED : DrawerLayout.LOCK_MODE_UNLOCKED);
	}

	private void setFragment(Fragment fragment, boolean back) {
		Fragment curFragment = fragmentManager.findFragmentById(R.id.container);
		if (curFragment != null && fragment.getClass().equals(curFragment.getClass()))
			return;

		if (back)
			showArrow(true);

		FragmentTransaction transaction = fragmentManager.beginTransaction();
		if (back)
			transaction.addToBackStack(fragment.getClass().getName());
		transaction
				.replace(R.id.container, fragment, fragment.getClass().getName())
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.commitAllowingStateLoss();
	}

	@Override
	public void onBackPressed() {
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START))
			drawer.closeDrawer(GravityCompat.START);
		else {
			if (fragmentManager.getBackStackEntryCount() < 2) {
				drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
				showArrow(false);
			}

			super.onBackPressed();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.settings:
				hideKeyboard();
				setFragment(new PrefsFragment(), true);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void hideKeyboard() {
		View view = getCurrentFocus();
		if (view != null)
			((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	@SuppressWarnings("StatementWithEmptyBody")
	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if (intent != null && intent.getAction() != null)
			switch (intent.getAction()) {
				case ACTION_SHOW_RESULTS:
					NotificationManager notifier = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					if (notifier != null)
						notifier.cancelAll();

					setFragment(new ResultsFragment(), true);
					break;
				case ACTION_SHOW_RESULT:
					setFragment(TextFragment.newInstance(intent.getExtras()), true);
					break;
			}
	}
}
