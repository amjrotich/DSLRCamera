package com.cradleshyft.dslrcamera.util.selector;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

public class CenterSnapHelper extends LinearSnapHelper {

    private static final float MILLISECONDS_PER_INCH = 100f;
    private static final int MAX_SCROLL_ON_FLING_DURATION_MS = 1000;

    private Context context = null;
    private RecyclerView recyclerView = null;
    private Scroller scroller = null;
    private int maxScrollDistance = 0;

    @Override
    public void attachToRecyclerView(@Nullable RecyclerView recyclerView) {
        if (recyclerView == null) {
            this.context = null;
            this.recyclerView = null;
            this.scroller = null;
        } else {
            this.context = recyclerView.getContext();
            this.recyclerView = recyclerView;
            this.scroller = new Scroller(context, new DecelerateInterpolator());
        }
        super.attachToRecyclerView(recyclerView);
    }

    @Override
    public View findSnapView(RecyclerView.LayoutManager layoutManager) {
        return findMiddleView(layoutManager);
    }

    @Nullable
    @Override
    protected RecyclerView.SmoothScroller createScroller(@NonNull RecyclerView.LayoutManager layoutManager) {
        if (layoutManager instanceof RecyclerView.SmoothScroller.ScrollVectorProvider) {
            return new LinearSmoothScroller(context) {
                @Override
                protected void onTargetFound(View targetView, RecyclerView.State state, Action action) {
                    int[] snapDistance = calculateDistanceToFinalSnap(layoutManager, targetView);
                    if (snapDistance == null) return;
                    int dx = snapDistance[0];
                    int dy = snapDistance[1];
                    int dt = calculateTimeForDeceleration(Math.abs(dx));
                    int time = max(1, min(MAX_SCROLL_ON_FLING_DURATION_MS, dt));
                    action.update(dx, dy, time, mDecelerateInterpolator);
                }

                @Override
                protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                    return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
                }
            };
        } else {
            return null;
        }
    }

    @Override
    public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager, @NonNull View targetView) {
        int[] out = new int[2];
        out[0] = distanceToMiddleView(layoutManager, targetView);
        return out;
    }

    @Override
    public int[] calculateScrollDistance(int velocityX, int velocityY) {
        int[] out = new int[2];
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

        if (maxScrollDistance == 0 && layoutManager != null) {
            maxScrollDistance = layoutManager.getWidth() / 2;
        }

        scroller.fling(0, 0, velocityX, velocityY, -maxScrollDistance, maxScrollDistance, 0, 0);
        out[0] = scroller == null ? 0 : scroller.getFinalX();
        out[1] = scroller == null ? 0 : scroller.getFinalY();
        return out;
    }

    private int distanceToMiddleView(RecyclerView.LayoutManager layoutManager, View targetView) {
        int middle = layoutManager.getWidth() / 2;
        int targetMiddle = targetView.getLeft() + targetView.getWidth() / 2;
        return targetMiddle - middle;
    }

    private View findMiddleView(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager == null) return null;

        int childCount = layoutManager.getChildCount();
        if (childCount == 0) return null;

        int absClosest = Integer.MAX_VALUE;
        View closestView = null;
        int middle = layoutManager.getWidth() / 2;

        for (int i = 0; i < childCount; i++) {
            View child = layoutManager.getChildAt(i);
            if (child == null) continue;
            int absDistanceToMiddle = abs((child.getLeft() + child.getWidth() / 2) - middle);
            if (absDistanceToMiddle < absClosest) {
                absClosest = absDistanceToMiddle;
                closestView = child;
            }
        }
        return closestView;
    }
}
