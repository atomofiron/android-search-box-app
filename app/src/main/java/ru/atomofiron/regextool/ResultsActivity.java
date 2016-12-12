package ru.atomofiron.regextool;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ScrollView;

import java.io.File;
import java.util.ArrayList;

public class ResultsActivity extends AppCompatActivity {

    ListView listView;

    ListAdapter listAdapter;
    Listner listener;
    ResultsActivity context;

    ArrayList<String> resultsList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        context=this;

        listView = (ListView)findViewById(R.id.listview);
        listAdapter = new ListAdapter(this);
        listView.setAdapter(listAdapter);
        listAdapter.
                setLinesList(getIntent().getStringArrayListExtra(I.RESULT_LIST_LINES));
        resultsList = getIntent().getStringArrayListExtra(I.RESULT_LIST);
        listAdapter.setList(resultsList);
        listener = new Listner();
        listView.setOnItemClickListener(listener);
        listView.setOnItemLongClickListener(listener);
    }

    class Listner implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            File file = listAdapter.getItem(position);
            String name = file.getName();

            if (name.contains(".")) name = name.substring(name.lastIndexOf('.')+1);
            String format = name;
            name = MimeTypeMap.getSingleton().getMimeTypeFromExtension(name);
            Intent intent = new Intent()
                .setAction(android.content.Intent.ACTION_VIEW)
                .setDataAndType(Uri.fromFile(file), name);
            if (I.isTxtFile(format))
                startActivity(new Intent(context,TextActivity.class)
                        .putExtra(I.RESULT_PATH,resultsList.get(position))
                        .putExtra(I.TARGET,getIntent().getStringExtra(I.TARGET))
                        .putExtra(I.RESULT_LINE_COUNTS,getIntent().getStringArrayListExtra(I.RESULT_LIST_LINE_COUNTS).get(position)));
            else if (intent.resolveActivity(getPackageManager()) != null) startActivityForResult(intent, 10);
            else I.Toast(context,getString(R.string.no_activity),1);
        }
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            I.Toast(context,listAdapter.getItem(position).getAbsolutePath(),1);
            return false;
        }
    }

}
