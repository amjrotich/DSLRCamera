package com.cradleshyft.dslrcamera.util.selector;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cradleshyft.dslrcamera.adapter.SelectorAdapter;

public class RecyclerViewListener extends RecyclerView.OnScrollListener {

    private final CenterSnapHelper snapHelper;
    private final OnNewPositionListener onNewPositionListener;
    private int snapPosition = RecyclerView.NO_POSITION;

    public interface OnNewPositionListener {
        void notify(int position);
    }

    public RecyclerViewListener(CenterSnapHelper snapHelper,
                                OnNewPositionListener onNewPositionListener) {
        this.snapHelper = snapHelper;
        this.onNewPositionListener = onNewPositionListener;
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        modifyNextItemDesign(recyclerView);
    }

    private void modifyNextItemDesign(RecyclerView recyclerView) {
        recyclerView.post(() -> {
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager == null) return;
            if (recyclerView.getAdapter() instanceof SelectorAdapter) {
                View snapView = snapHelper.findSnapView(layoutManager);
                if (snapView == null) return;
                int newSnapPosition = layoutManager.getPosition(snapView);
                ((SelectorAdapter) recyclerView.getAdapter()).onScroll(newSnapPosition);
                //snapPosition = newSnapPosition;
            }
        });

    }

    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager == null) return;
            View snapView = snapHelper.findSnapView(layoutManager);
            if (snapView == null) return;
            int newSnapPosition = layoutManager.getPosition(snapView);
            onItemSelected(snapPosition, newSnapPosition);
            snapPosition = newSnapPosition;
        }
    }

    private void onItemSelected(int oldPosition, int newPosition) {
        if (oldPosition == newPosition || onNewPositionListener == null) return;
        onNewPositionListener.notify(newPosition);
    }

}
