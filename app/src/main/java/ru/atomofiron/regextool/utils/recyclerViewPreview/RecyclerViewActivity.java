package ru.atomofiron.regextool.utils.recyclerViewPreview;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewActivity extends AppCompatActivity {

    private RecyclerViewAdapterImpl adapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e("recyclerViewPreview", "onCreate");
        super.onCreate(savedInstanceState);

        RecyclerView recyclerView = new RecyclerView(this);
        setContentView(recyclerView);

        ArrayList<DataItem> items = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            items.add(new DataItem());
        }

        adapter = new RecyclerViewAdapterImpl();
        adapter.setItems(items);
        adapter.itemListener = new ItemListenerImpl();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private class ItemListenerImpl implements ItemListener {

        public void onItemClick(DataItem item) {
            Log.e("recyclerViewPreview", "onItemClick " + item.title);
        }

        public void onItemLongClick(DataItem item) {
            Log.e("recyclerViewPreview", "onItemLongClick " + item.title);
        }
    }
}