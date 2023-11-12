package com.cradleshyft.dslrcamera.util;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.core.content.ContextCompat;

public class AnimUtils {

    /**
     * change icon with animation
     *
     */
    public static void changeButtonIcon(ImageButton button, int img, Context context) {
        button.animate().scaleY(0.5f).scaleX(0.5f).alpha(0f).setDuration(100).withEndAction(() -> {
            button.setImageDrawable(ContextCompat.getDrawable(context, img));
            button.animate().scaleY(1.0f).scaleX(1.0f).alpha(1f).setDuration(100);
        });

    }

    /**
     * rotate button
     *
     */
    public static void rotateSwitchCameraButton(ImageButton button) {
        button.animate().rotation(360).setDuration(300)
                .withEndAction(() -> button.setRotation(0));
    }

    /**
     * change scale X-Y of a view with animation
     *
     */
    public static void scaleView(View view, float scale) {
        view.animate().scaleX(scale).scaleY(scale).setDuration(150);
    }

    /**
     * change alpha of a view with animation
     *
     */
    public static void setAlpha(View view, float alpha) {
        view.animate().alpha(alpha).setDuration(150);
    }

    /**
     * change scale X-Y of a image button with animation
     * from 1 to 0.8 and after 50 ms, back to 1
     *
     */
    public static void animateBtnTakePhoto(ImageButton button) {
        button.animate().scaleX(0.8f).scaleY(0.8f).setDuration(50)
                .withEndAction(() -> button.animate().scaleX(1.0f).scaleY(1.0f));
    }

    /**
     * animate scale, alpha with 150 ms duration
     * end action: change visibility, set default scale and set alpha
     *
     */
    public static void animateImgFocus(ImageView imgFocus) {
        imgFocus.animate().scaleX(1f).scaleY(1f).setDuration(150).alpha(0f).withEndAction(() -> {
            imgFocus.setVisibility(View.INVISIBLE);
            imgFocus.setScaleY(1.3f);
            imgFocus.setScaleX(1.3f);
            imgFocus.setAlpha(1f);
        });
    }

    /**
     * change translation
     * change visibility
     *
     */
    public static void moveImgFocus(ImageView imgFocus, float x, float y) {
        imgFocus.setTranslationX(x - 156);
        imgFocus.setTranslationY(y + 156);
        imgFocus.setVisibility(View.VISIBLE);
    }

}
