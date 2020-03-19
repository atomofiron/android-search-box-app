package ru.atomofiron.regextool.utils.recyclerViewPreview;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ru.atomofiron.regextool.R;

public class ViewHolderImpl extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
    private DataItem dataItem = null;
    private ItemListener itemListener;

    private TextView tvTitle;

    public ViewHolderImpl(@NonNull View itemView, ItemListener listener) {
        super(itemView);
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
        itemListener = listener;
        tvTitle = itemView.findViewById(R.id.item_tv_title);
    }

    public void bind(DataItem item) {
        dataItem = item;
        tvTitle.setText(item.title);
    }

    @Override
    public void onClick(View v) {
        itemListener.onItemClick(dataItem);
    }

    @Override
    public boolean onLongClick(View v) {
        itemListener.onItemLongClick(dataItem);
        return false;
    }
}
