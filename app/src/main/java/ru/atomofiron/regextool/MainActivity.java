package ru.atomofiron.regextool;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import ru.atomofiron.regextool.Fragments.MainFragment;
import ru.atomofiron.regextool.Fragments.PrefsFragment;
import ru.atomofiron.regextool.Fragments.ResultsFragment;
import ru.atomofiron.regextool.Utils.Cmd;
import ru.atomofiron.regextool.Utils.Permissions;

public class MainActivity extends AppCompatActivity
		implements NavigationView.OnNavigationItemSelectedListener, I.OnSnackListener, MainFragment.OnResultListener {

	private FragmentManager fragmentManager;
	private MainFragment mainFragment;
	private FloatingActionButton fab;
	private DrawerLayout drawer;
	private AlertDialog helpDialog = null;
	private AlertDialog pathDialog = null;
	private SharedPreferences sp;
	private MenuItem useRootItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		sp = I.SP(this);

		//setRequestedOrientation(sp.getString());

		setTheme(sp.getBoolean(I.PREF_DARK_THEME, false) ? R.style.AppTheme : R.style.AppTheme_Light);
		setContentView(R.layout.activity_main);

		if (Permissions.checkPerm(this, I.REQUEST_FOR_INIT))
			init();

		updatePrefs();
	}

	private void updatePrefs() {
		sp.edit()
				.putString(I.PREF_STORAGE_PATH, sp.getString(I.PREF_STORAGE_PATH,
						Environment.getExternalStorageDirectory().getAbsolutePath()))
				.putString(I.PREF_EXTRA_FORMATS, sp.getString(I.PREF_EXTRA_FORMATS,
						"md mkd markdown cm ad adoc"))
				.putString(I.PREF_ORIENTATION, sp.getString(I.PREF_ORIENTATION,
						getString(R.string.orientation_def)))
				.apply();
	}

	private void init() {
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.setDrawerListener(toggle);
		toggle.syncState();

		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);

		fab = (FloatingActionButton)findViewById(R.id.fab);

		mainFragment = new MainFragment();
		mainFragment.setOnSnackListener(this);
		mainFragment.setOnResultListener(this);

		fragmentManager = getSupportFragmentManager();
		setFragment(mainFragment, false);

		SharedPreferences sp = I.SP(this);
		if (sp.getBoolean(I.PREF_FIRST_START, true)) {
			showHelp();
			sp.edit().putBoolean(I.PREF_FIRST_START, false).apply();
		}
	}

	private void setFragment(Fragment fragment, boolean back) {
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		if (back)
			transaction.addToBackStack(null);
		transaction
				.replace(R.id.container, fragment)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.commitAllowingStateLoss();

		drawer.setDrawerLockMode((fragment.getClass() == MainFragment.class) ?
				DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
	}

	@Override
	public void onBackPressed() {
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START))
			drawer.closeDrawer(GravityCompat.START);
		else {
			if (fragmentManager.getBackStackEntryCount() < 2)
				drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

			super.onBackPressed();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		updateUseRootIcon();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		menu.findItem(R.id.theme).setIcon(sp.getBoolean(I.PREF_DARK_THEME, false) ?
				R.drawable.ic_light : R.drawable.ic_dark);
		useRootItem = menu.findItem(R.id.use_root);
		updateUseRootIcon();
		return true;
	}

	private void updateUseRootIcon() {
		if (useRootItem != null)
			useRootItem.setIcon(sp.getBoolean(I.PREF_USE_ROOT, false) ?
					R.drawable.hash : R.drawable.hash_dis);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.help:
				showHelp();
				break;
			case R.id.theme:
				boolean value = !sp.getBoolean(I.PREF_DARK_THEME, false);
				sp.edit().putBoolean(I.PREF_DARK_THEME, value).apply();
				item.setIcon(value ? R.drawable.ic_light : R.drawable.ic_dark);
				I.Toast(this, getString(R.string.need_restart), Toast.LENGTH_SHORT);
				break;
			case R.id.def_path:
				//showConfig();
				setFragment(new PrefsFragment(), true);
				break;
			case R.id.use_root:
				boolean useRoot = !sp.getBoolean(I.PREF_USE_ROOT, false) && Cmd.easyExec("su") == 0;
				sp.edit().putBoolean(I.PREF_USE_ROOT, useRoot).apply();
				updateUseRootIcon();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void showHelp() {
		if (helpDialog == null)
			helpDialog = new AlertDialog.Builder(this)
					.setTitle(getString(R.string.action_help))
					.setMessage(getString(R.string.help))
					.setNegativeButton("Ok", null)
					.create();

		helpDialog.show();
	}

	@SuppressWarnings("StatementWithEmptyBody")
	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			switch (requestCode) {
				case I.REQUEST_FOR_INIT:
					init(); break;
				case I.REQUEST_FOR_SEARCH:
					mainFragment.search(); break;
			}
		} else if (requestCode == I.REQUEST_FOR_INIT)
			finish();
	}

	@Override
	public void onSnack(String str) {
		Snackbar.make(fab, str, Snackbar.LENGTH_LONG).show();
	}

	@Override
	public void onSnack(String str, int actionId, View.OnClickListener callback) {
		Snackbar.make(fab, str, Snackbar.LENGTH_LONG).setAction(actionId, callback).show();
	}

	public void Snack(int id) {
		Snackbar.make(fab, getString(id), Snackbar.LENGTH_LONG).show();
	}

	@Override
	public void onResult(Bundle bundle) {
		ResultsFragment fragment = ResultsFragment.newInstance(bundle);
		fragment.setOnSnackListener(this);
		setFragment(fragment, true);
	}
}
