package com.cradleshyft.dslrcamera.util.selector;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewSelector extends RecyclerView {

    private RecyclerViewListener.OnNewPositionListener onNewPositionListener;
    private CenterSnapHelper snapHelper;

    public void setOnNewPositionListener(RecyclerViewListener.OnNewPositionListener onNewPositionListener) {
        this.onNewPositionListener = onNewPositionListener;
    }

    public RecyclerViewSelector(@NonNull Context context) {
        super(context);
        init();
    }

    public RecyclerViewSelector(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RecyclerViewSelector(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        snapHelper = new CenterSnapHelper();
        addItemDecoration(new CenterItemDecoration());
        addOnScrollListener(new RecyclerViewListener(snapHelper, position -> {
            if (onNewPositionListener != null)
                onNewPositionListener.notify(position);
        }));
        snapHelper.attachToRecyclerView(this);

    }

    public void snapToDefaultPosition(int position) {
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager == null || snapHelper == null) return;
        this.scrollToPosition(position);
        this.post(() -> {
            View view = layoutManager.findViewByPosition(position);
            if (view == null) return;
            int[] snapDistance = snapHelper.calculateDistanceToFinalSnap(layoutManager, view);
            if (snapDistance == null) return;
            if (snapDistance[0] != 0 || snapDistance[1] != 0) {
                this.scrollBy(snapDistance[0], snapDistance[1]);
            }
        });
    }

    public void scrollTo(View itemClicked) {
        if (itemClicked == null) return;
        LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
        if (layoutManager == null) return;
        int viewMiddle = itemClicked.getLeft() + itemClicked.getWidth() / 2;
        int middle = layoutManager.getWidth() / 2;
        int dx = viewMiddle - middle;
        this.smoothScrollBy(dx, 0);
    }

}
