package ru.atomofiron.regextool.utils.recyclerViewPreview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ru.atomofiron.regextool.R;

public class RecyclerViewAdapterImpl extends RecyclerView.Adapter<ViewHolderImpl> {
    private List<DataItem> items = new ArrayList<>();
    ItemListener itemListener = null;

    @NonNull
    @Override
    public ViewHolderImpl onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        //View view = inflater.inflate(R.layout.item_data, parent, false);
        View view = createTextView(parent.getContext());
        return new ViewHolderImpl(view, itemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderImpl holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<DataItem> items) {
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    private View createTextView(Context context) {
        TextView view = new TextView(context);
        view.setPadding(40, 40, 40, 40);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(layoutParams);
        int[][] states = new int[1][1];
        int[] colors = new int[] { Color.RED };
        states[0] = new int[] {};
        ColorStateList list = new ColorStateList(states, colors);
        GradientDrawable shape = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] { Color.BLACK });
        view.setBackground(new RippleDrawable(list, null, shape));
        view.setId(R.id.item_tv_title);
        return view;
    }
}
