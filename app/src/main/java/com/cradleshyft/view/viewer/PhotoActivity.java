package com.cradleshyft.dslrcamera.view.viewer;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.viven.imagezoom.ImageZoomHelper;
import com.cradleshyft.dslrcamera.R;

public class PhotoActivity extends AppCompatActivity {

    public static final String PHOTO_KEY = "photo_key";
    //
    private String imagePath;
    //
    private ImageView imageView;
    private ImageZoomHelper imageZoomHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        getExtras();
        init();
        Glide.with(this).load(imagePath).into(imageView);

    }

    private void init() {
        this.imageView = findViewById(R.id.img);
        this.imageZoomHelper = new ImageZoomHelper(this);
        ImageZoomHelper.setViewZoomable(imageView);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return imageZoomHelper.onDispatchTouchEvent(ev) || super.dispatchTouchEvent(ev);
    }

    private void getExtras() {
        try {
            this.imagePath = getIntent().getExtras().getString(PhotoActivity.PHOTO_KEY, "");
        } catch (NullPointerException ignored) {
        }
    }

}