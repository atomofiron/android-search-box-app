package ru.atomofiron.regextool;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

public class SelectActivity extends AppCompatActivity {

    ListView Listview;

    ListAdapter listAdapter;
    Listner listner;
    SelectActivity ac;

    ArrayList<File> parents;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        parents = new ArrayList<>();
        ac = this;

        Listview = (ListView)findViewById(R.id.listview);
        listAdapter = new ListAdapter(this);
        Listview.setAdapter(listAdapter);
        listner = new Listner();
        Listview.setOnItemClickListener(listner);
        Listview.setOnItemLongClickListener(listner);
        I.Log("Environment.MEDIA_MOUNTED: "+Environment.MEDIA_MOUNTED);
        String path = getSharedPreferences(I.PREFS,MODE_PRIVATE).getString(I.STORAGE_PATH,Environment.getExternalStorageDirectory().getAbsolutePath());
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && !path.isEmpty()) {
            parents.add(new File(path));
            showList(parents.get(0));
        } else {
            I.Toast(this,getString(R.string.storage_err),0);
            finish();
        }
    }
    void showList(File file) {
        I.Log("showList()");
        if (!I.granted(this,I.RES_PERM)) {
            I.Toast(this, getString(R.string.storage_err), 0);
            finish();
        } else if (file.isDirectory() && file.listFiles()!=null && file.listFiles().length>0) {
            listAdapter.setList(file.listFiles());
            Listview.scrollTo(0,0);
        }
    }

    @Override
    public void onBackPressed() {
        if (parents.size()<=1) {
            setResult(I.NO_OK);
            super.onBackPressed();
        } else {
            int size = parents.size();
            parents.remove(size - 1);
            File file = parents.get(size - 2);
            showList(file);
        }
    }


    class Listner implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            File file = listAdapter.getItem(position);
            if (file.isDirectory() && file.listFiles()!=null && file.listFiles().length>0) {
                parents.add(file);
                showList(file);
            }
        }
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (!listAdapter.getItem(position).isDirectory()) return false;
            ac.setResult(61,new Intent().putExtra("path",listAdapter.getItem(position).getAbsolutePath()));
            finish();
            return false;
        }
    }

}
