package com.cradleshyft.dslrcamera.viewholder;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cradleshyft.dslrcamera.R;

public class ItemSelectorViewHolder extends RecyclerView.ViewHolder {

    private final TextView txtSelector;

    public ItemSelectorViewHolder(@NonNull View itemView) {
        super(itemView);
        this.txtSelector = itemView.findViewById(R.id.txtSelector);
    }

    public void setTitle(String title) {
        this.txtSelector.setText(title);
    }

    public void modifyDesign(boolean selected) {
        int res = selected ? R.drawable.item_selected_bg : R.drawable.item_not_selected_bg;
        int color = selected ? Color.BLACK : Color.WHITE;
        this.txtSelector.setBackgroundResource(res);
        this.txtSelector.setTextColor(color);
    }

}
