package com.cradleshyft.dslrcamera.util.selector;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CenterItemDecoration extends RecyclerView.ItemDecoration {

    @Override
    public void getItemOffsets(@NonNull Rect outRect,
                               @NonNull View view,
                               @NonNull RecyclerView parent,
                               @NonNull RecyclerView.State state) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            int position = layoutManager.getPosition(view);

            if (position == 0 || position == layoutManager.getItemCount() - 1) {
                measureChild(parent, view);
                int width = view.getMeasuredWidth();
                int center = (parent.getWidth() - width) / 2;

                if (position == 0) {
                    outRect.left = center;
                    outRect.right = 0;
                } else if (position == layoutManager.getItemCount() - 1) {
                    outRect.left = 0;
                    outRect.right = center;
                } else {
                    outRect.left = 0;
                    outRect.right = 0;
                }

            }
        }
    }

    private void measureChild(RecyclerView parent, View child) {
        if (ViewCompat.isLaidOut(child)) return;
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            ViewGroup.LayoutParams lp = child.getLayoutParams();

            int widthSpec = RecyclerView.LayoutManager.getChildMeasureSpec(
                    layoutManager.getWidth(),
                    layoutManager.getWidthMode(),
                    layoutManager.getPaddingLeft() + layoutManager.getPaddingRight(),
                    lp.width,
                    layoutManager.canScrollHorizontally()
            );

            int heightSpec = RecyclerView.LayoutManager.getChildMeasureSpec(
                    layoutManager.getHeight(),
                    layoutManager.getHeightMode(),
                    layoutManager.getPaddingTop() + layoutManager.getPaddingBottom(),
                    lp.height,
                    layoutManager.canScrollVertically()
            );
            child.measure(widthSpec, heightSpec);
        }
    }

}
