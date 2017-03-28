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
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import ru.atomofiron.regextool.Fragments.MainFragment;
import ru.atomofiron.regextool.Fragments.ResultsFragment;
import ru.atomofiron.regextool.Utils.Cmd;
import ru.atomofiron.regextool.Utils.Permissions;

public class MainActivity extends AppCompatActivity
		implements NavigationView.OnNavigationItemSelectedListener, I.SnackListener, MainFragment.OnResultListener {

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
		setTheme(sp.getBoolean(I.PREF_DARK_THEME, false) ? R.style.AppTheme : R.style.AppTheme_Light);
		setContentView(R.layout.activity_main);

		if (Permissions.checkPerm(this, I.REQUEST_FOR_INIT))
			init();
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
		fab.setVisibility(View.GONE);

		mainFragment = new MainFragment();
		mainFragment.setSnackListener(this);
		mainFragment.setOnResultListener(this);
		mainFragment.mainActivity = this; // да, всё оч плохо

		fragmentManager = getSupportFragmentManager();
		setFragment(mainFragment, false);

		SharedPreferences sp = I.SP(this);
		if (sp.getBoolean(I.PREF_FIRST_START, true)) {
			helpDialog.show();
			sp.edit().putBoolean(I.PREF_FIRST_START, false).apply();
		}
	}

	private void setFragment(Fragment fragment, boolean back) {
		FragmentTransaction transaction = fragmentManager.beginTransaction().replace(R.id.container, fragment);
		if (back)
			transaction.addToBackStack("results");
		transaction.commit();
	}

	@Override
	public void onBackPressed() {
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START))
			drawer.closeDrawer(GravityCompat.START);
		else
			super.onBackPressed();
	}

	@Override
	protected void onStart() {
		updateUseRootIcon();
		super.onStart();
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
				if (helpDialog == null)
					helpDialog = new AlertDialog.Builder(this)
							.setTitle(getString(R.string.action_help))
							.setMessage(getString(R.string.help))
							.setNegativeButton("Ok", null)
							.create();

				helpDialog.show();
				break;
			case R.id.theme:
				boolean value = !sp.getBoolean(I.PREF_DARK_THEME, false);
				sp.edit().putBoolean(I.PREF_DARK_THEME, value).apply();
				item.setIcon(value ? R.drawable.ic_light : R.drawable.ic_dark);
				I.Toast(this, getString(R.string.need_restart), Toast.LENGTH_SHORT);
				break;
			case R.id.def_path:
				if (pathDialog == null) {
					final String path = sp.getString(I.STORAGE_PATH, "/");
					final EditText et = new EditText(this);
					et.setText(path);
					pathDialog = new AlertDialog.Builder(this)
							.setTitle(getString(R.string.def_path))
							.setView(et)
							.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									String newPath = et.getText().toString();
									if (!newPath.equals(path) && !newPath.isEmpty())
										sp.edit().putString(I.STORAGE_PATH, newPath).apply();
									dialog.cancel();
								}
							})
							.setNeutralButton(getString(R.string.reset), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									et.setText(Environment.getExternalStorageDirectory().getAbsolutePath());
								}
							})
							.setNegativeButton(getString(R.string.cancel), null)
							.setCancelable(true).create();
				}
				pathDialog.show();
				break;
			case R.id.use_root:
				boolean useRoot = !sp.getBoolean(I.PREF_USE_ROOT, false) && Cmd.easyExec("su") == 0;
				sp.edit().putBoolean(I.PREF_USE_ROOT, useRoot).apply();
				updateUseRootIcon();
				break;
		}
		return super.onOptionsItemSelected(item);
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
		} else
			Permissions.checkPerm(this, I.REQUEST_FOR_INIT);
	}

	@Override
	public void Snack(String str) {
		Snackbar.make(fab, str, Snackbar.LENGTH_LONG).show();
	}
	public void Snack(int id) {
		Snackbar.make(fab, getString(id), Snackbar.LENGTH_LONG).show();
	}

	public void onSnack(String str, String action) {
		Snackbar.make(fab, str, Snackbar.LENGTH_LONG)
				.setAction(action, null).show();
	}


	@Override
	public void onResult(Bundle bundle) {
		ResultsFragment fragment = ResultsFragment.newInstance(bundle);
		fragment.setSnackListener(this);
		setFragment(fragment, true);
	}
}
