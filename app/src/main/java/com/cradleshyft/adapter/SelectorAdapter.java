package com.cradleshyft.dslrcamera.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.cradleshyft.dslrcamera.R;
import com.cradleshyft.dslrcamera.viewholder.ItemSelectorViewHolder;

public class SelectorAdapter extends RecyclerView.Adapter<ItemSelectorViewHolder> {

    private final List<String> items;
    private int positionSelected = 0;
    private final View.OnClickListener onClick;

    public SelectorAdapter(List<String> items, View.OnClickListener onClick) {
        this.items = items;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public ItemSelectorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemSelectorViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selector, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ItemSelectorViewHolder holder, int position) {
        String txt = items.get(holder.getBindingAdapterPosition());
        holder.setTitle(txt);
        holder.modifyDesign(positionSelected == position);
        holder.itemView.setOnClickListener(onClick);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void onScroll(int position) {
        positionSelected = position;
        notifyDataSetChanged();
    }
}
