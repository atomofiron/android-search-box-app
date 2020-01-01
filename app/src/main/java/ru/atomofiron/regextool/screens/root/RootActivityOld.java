package ru.atomofiron.regextool.screens.root;
/*

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;

import com.google.android.material.navigation.NavigationView;

import ru.atomofiron.regextool.screens.main.FinderFragment;
import ru.atomofiron.regextool.screens.preferences.PrefsFragment;
import ru.atomofiron.regextool.screens.result.ResultsFragment;
import ru.atomofiron.regextool.screens.viewer.TextFragment;

public class MainActivity2 extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
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

		sp = Util.sp(this);
		setRequestedOrientation(Integer.parseInt(sp.getString(Util.PREF_ORIENTATION, "2")) - 1);

		setTheme(new int[] {
				R.style.AppTheme_White,
				R.style.AppTheme_Dark,
				R.style.AppTheme_Black,
		}[Integer.parseInt(sp.getString(Util.PREF_THEME, "0"))]);

		setContentView(R.layout.activity_root);

		init();
		updatePrefs();
	}

	private void updatePrefs() {
		SharedPreferences.Editor editor = sp.edit();

		// каскадное обновление настроек в зависимости от номера версии
		switch (sp.getInt(Util.PREF_LAST_VERSION, 0)) {
			case BuildConfig.VERSION_CODE: // текущая
				break;
			case 0: // 0 - 7
				editor.putInt(Util.PREF_MAX_SIZE, sp.getInt(Util.PREF_MAX_SIZE, 10) * 1048576)
						.putString(Util.PREF_STORAGE_PATH, Environment.getExternalStorageDirectory().getAbsolutePath())
						.putString(Util.PREF_EXTRA_FORMATS, Util.DEFAULT_EXTRA_FORMATS);
			default:
				editor.putInt(Util.PREF_LAST_VERSION, BuildConfig.VERSION_CODE);
		}

		editor.apply();
	}

	private void init() {
		Toolbar toolbar = (Toolbar) findViewById(0);
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

		FinderFragment mainFragment;
		fragmentManager = getSupportFragmentManager();

		if (fragmentManager.findFragmentById(R.id.container) == null)
			setFragment(mainFragment = new FinderFragment(), false);
		else
			mainFragment = (FinderFragment) fragmentManager.findFragmentByTag(FinderFragment.class.getName());

		//mainFragment.setDrawerViewWithHistory(drawer);

		if (fragmentManager.getBackStackEntryCount() > 0)
			showArrow(true);

		SharedPreferences sp = Util.sp(this);
		if (sp.getBoolean(Util.PREF_FIRST_START, true)) {
			Util.showHelp(this);
			sp.edit().putBoolean(Util.PREF_FIRST_START, false).apply();
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
					setFragment(new ResultsFragment(), true);
					break;
				case ACTION_SHOW_RESULT:
					setFragment(new TextFragment(), true);
					break;
			}
	}
}
*/
