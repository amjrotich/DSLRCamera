package com.cradleshyft.dslrcamera.view.custom;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.Nullable;

import com.cradleshyft.dslrcamera.util.SharedPrefsSettings;

public class CameraGridLinesView extends View {

    private Paint paint;
    private boolean show;

    public CameraGridLinesView(Context context) {
        super(context);
        init();
        configPaint();
    }

    public CameraGridLinesView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
        configPaint();
    }

    public CameraGridLinesView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        configPaint();
    }

    private void init() {
        this.show = SharedPrefsSettings.getGridLinesStatus(getContext());
        this.paint = new Paint();
    }

    private void configPaint() {
        this.paint.setAntiAlias(true);
        this.paint.setStrokeWidth(1);
        this.paint.setStyle(Paint.Style.STROKE);
        this.paint.setColor(Color.argb(255, 255, 255, 255));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (show) {
            drawGridLines(canvas);
        } else {
            //remove grid lines
            canvas.save();
            canvas.restore();
        }
    }

    /**
     * change grid lines visibility
     */
    public void showGrid(boolean show) {
        this.show = show;
        invalidate();
    }

    /**
     * draw 3x3 grid lines
     */
    private void drawGridLines(Canvas canvas) {
        //get screen size
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;
        int navigationBarHeight = 200;

        // vertical line
        float verticalStartStopX = (float) (screenWidth / 3) * 2;
        float verticalStopY = screenHeight + navigationBarHeight;
        // horizontal line
        float horizontalStartStopYBottom = (float) (screenHeight / 3) * 2;
        float horizontalStartStopYTOP = (float) (screenHeight / 3);
        //draw vertical lines
        canvas.drawLine(verticalStartStopX, 0, verticalStartStopX, verticalStopY, paint);
        canvas.drawLine((float) (screenWidth / 3), 0, (float) (screenWidth / 3), screenHeight + navigationBarHeight, paint);
        //draw horizontal lines
        canvas.drawLine(0, horizontalStartStopYTOP, screenWidth, horizontalStartStopYTOP, paint);
        canvas.drawLine(0, horizontalStartStopYBottom, screenWidth, horizontalStartStopYBottom, paint);
    }

}
