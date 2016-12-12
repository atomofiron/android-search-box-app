package ru.atomofiron.regextool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import java.io.File;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton fab;
    EditText regexText;
    CheckBox caseBox;
    CheckBox filesBox;
    CheckBox simpleBox;
    ListView listView;

    ListAdapter listAdapter;
    AlertDialog alertDialog;
    Receiver dirReceiver;
    SharedPreferences sp;
    AlertDialog helpDialog;
    boolean needShowResults=true;

    //private final ActivityCheckout checkout = Checkout.forActivity(this, App.get().getCheckout());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fab = (FloatingActionButton)findViewById(R.id.fab);
        if (fab != null) fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPerm(I.REQUEST_FOR_SELECT))
                    select();
            }
        });
        sp = getSharedPreferences(I.PREFS,MODE_PRIVATE);
        if (sp.getString(I.STORAGE_PATH,"").isEmpty()) sp.edit().putString(I.STORAGE_PATH, Environment.getExternalStorageDirectory().getAbsolutePath()).apply();

        regexText = (EditText)findViewById(R.id.regex_text);
        caseBox = (CheckBox)findViewById(R.id.case_senc);
        filesBox = (CheckBox)findViewById(R.id.in_files);
        simpleBox = (CheckBox)findViewById(R.id.simple_search);
        listAdapter = new ListAdapter(this);
        listAdapter.absolutePaths = true;
        listAdapter.checkable = true;
        listView = (ListView)findViewById(R.id.listview);
        listView.setAdapter(listAdapter);
        Listner listener = new Listner();
        findViewById(R.id.in_files).setOnClickListener(listener);
        listView.setOnItemLongClickListener(listener);
        listView.setOnItemClickListener(listener);
        findViewById(R.id.go).setOnClickListener(listener);
        findViewById(R.id.slash).setOnClickListener(listener);
        findViewById(R.id.box).setOnClickListener(listener);
        findViewById(R.id.nobox).setOnClickListener(listener);
        findViewById(R.id.dot).setOnClickListener(listener);
        findViewById(R.id.star).setOnClickListener(listener);
        findViewById(R.id.dash).setOnClickListener(listener);
        findViewById(R.id.roof).setOnClickListener(listener);
        findViewById(R.id.buck).setOnClickListener(listener);
        dirReceiver = new Receiver();
        registerReceiver(dirReceiver, new IntentFilter(I.toMainActivity));
        helpDialog = new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.action_help))
                        .setMessage(getString(R.string.help))
                        .setNegativeButton("Ok", null)
                        .create();
        if (sp.getBoolean(I.PREF_FIRST_START,true)) {
            helpDialog.show();
            sp.edit().putBoolean(I.PREF_FIRST_START,false).apply();
        }


        /*Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);*/

        /*checkout.start();
        // you only need this if this activity starts purchase process
        checkout.createPurchaseFlow(new PurchaseListewner());
        // you only need this if this activity needs information about purchases/SKUs
        Inventory inventory = checkout.loadInventory();
        inventory.whenLoaded(new Inventory.Listener() {
            @Override
            public void onLoaded(@NonNull Inventory.Products products) {
                I.Log("onLoaded(): "+products.toString());
            }
        });*/
        /*app = (App) getApplication();
        mTracker = app.getDefaultTracker();
        mTracker.setScreenName("ReGeX tOoL");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());*/
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        I.Log("onActivityResult(): ");
        switch (resultCode) {
            case I.OK:
                listAdapter.addFile(new File(data.getStringExtra("path")));
                break;
            case I.NO_OK:
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.help:
                helpDialog.show();
                break;
            case R.id.settings:
                final String path = sp.getString(I.STORAGE_PATH,"/");
                final EditText et = new EditText(this);
                et.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                et.setText(path);
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.set_path))
                        .setView(et)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String newPath = et.getText().toString();
                                if (!newPath.equals(path) && !newPath.isEmpty()) sp.edit().putString(I.STORAGE_PATH,newPath).apply();
                                dialog.cancel();}
                        })
                        .setNeutralButton(getString(R.string.reset), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                et.setText(Environment.getExternalStorageDirectory().getAbsolutePath());}
                        })
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();}
                        })
                        .setCancelable(true)
                        .create().show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    class Listner implements View.OnClickListener, AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener { // упс, listEner
        boolean box=false;
        boolean nobox=false; // пусть так, я не хочу ((Button)v).getText().toString().equals()...
        @Override
        public void onClick(View v) {
            I.Log("onClick()");
            String symbol;
            switch (v.getId()) {
                case R.id.go:
                    needShowResults=true;
                    String regex = regexText.getText().toString();
                    if (!simpleBox.isChecked())
                        try { Pattern.compile(regex);
                        } catch (Exception ignored) {
                            I.Snack(fab,getString(R.string.bad_ex),false);
                            return;
                        }
                    if (regexText.getText().length()>0 && checkPerm(I.REQUEST_FOR_SEARCH)) checkList();
                    return;
                case R.id.box:
                    if (box) {
                        symbol="]";
                        ((Button)v).setText("[");
                    } else {
                        symbol="[";
                        ((Button)v).setText("]");
                    }
                    box=!box;
                    break;
                case R.id.nobox:
                    if (nobox) {
                        symbol="}";
                        ((Button)v).setText("{");
                    } else {
                        symbol="{";
                        ((Button)v).setText("}");
                    }
                    nobox=!nobox;
                    break;
                default:
                    String s = ((Button)v).getText().toString();
                    if (s==null || s.length()!=1) return; // ну мало ли
                    symbol = s;
                    break;
            }
            regexText.getText().insert(regexText.getSelectionStart(),symbol);
        }
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            listAdapter.remove(position);
            return false;
        }
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            I.Log("MainActivity(): onItemClick()");
            listAdapter.onItemClick(position); }
    }
    void checkList() {
        if (listAdapter.getCount()==0) {
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.warning))
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {search();}
                    })
                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {dialog.dismiss();}
                    })
                    .create().show();
        } else search();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0]==PackageManager.PERMISSION_GRANTED)
            if (requestCode==I.REQUEST_FOR_SEARCH) checkList();
            else select();
    }

    void select() {
        startActivityForResult(new Intent(this,SelectActivity.class), I.OK);
    }

    boolean checkPerm(int code) {
        if (I.granted(this,I.RES_PERM)) return true;
        if (Build.VERSION.SDK_INT >= 23) {
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE))
                new AlertDialog.Builder(this)
                        ///
                        .create().show();
            requestPermissions(new String[]{I.RES_PERM}, code);
        }
        else I.Snack(fab, getString(R.string.storage_err), false);
        return false;
    }
    void search() {
        alertDialog = new AlertDialog.Builder(this)
                .setView(R.layout.searching)
                .setCancelable(false)
                .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        needShowResults=false;
                        getBaseContext().stopService(new Intent(getBaseContext(),SearchService.class));
                    }
                })
                .setNegativeButton(R.string.stop, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getBaseContext().stopService(new Intent(getBaseContext(),SearchService.class));
                    }
                })
                .create();
        alertDialog.show();
        startService(new Intent(this,SearchService.class)
                .putExtra(I.CASE_SENSE, caseBox.isChecked())
                .putExtra(I.SEARCH_LIST,listAdapter.getPathArray())
                .putExtra(I.REGEX, regexText.getText().toString())
                .putExtra(I.SEARCH_IN_FILES, filesBox.isChecked())
                .putExtra(I.SEARCH_SIMPLE, simpleBox.isChecked()));
    }

    class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int i = intent.getIntExtra(I.SEARCH_CODE,I.SEARCH_ERROR);
            I.Log("onReceive(): "+i);
            alertDialog.cancel();
            switch (i) {
                case I.SEARCH_ERROR:
                    I.Snack(fab,getString(R.string.error),false);
                    break;
                case I.SEARCH_NOTHING:
                    if (needShowResults) I.Snack(fab,getString(R.string.nothing),false);
                    break;
                default:
                    if (needShowResults) {
                        I.Toast(context,getString(R.string.results,i),0);
                        startActivity(new Intent(context,ResultsActivity.class).putExtras(intent.getExtras()));
                    }
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(dirReceiver);
        stopService(new Intent(this,SearchService.class));
    }
}
