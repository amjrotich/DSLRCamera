package com.cradleshyft.dslrcamera.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.SystemClock;
import android.util.Size;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.camera.core.Camera;
import androidx.camera.core.ExposureState;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.SurfaceOrientedMeteringPointFactory;
import androidx.camera.core.ZoomState;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.utils.widget.ImageFilterView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.google.mlkit.nl.translate.TranslateLanguage;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.cradleshyft.dslrcamera.R;
import com.cradleshyft.dslrcamera.analyzer.BarcodeAnalyzer;
import com.cradleshyft.dslrcamera.analyzer.ImageLabelingAnalyzer;
import com.cradleshyft.dslrcamera.analyzer.TextRecognizerAnalyzer;
import com.cradleshyft.dslrcamera.model.Language;
import com.cradleshyft.dslrcamera.util.AnimUtils;
import com.cradleshyft.dslrcamera.util.SharedPrefsSettings;

public class MainController {

    /**
     * @return titles
     */
    public static ArrayList<String> getTitles() {
        ArrayList<String> titles = new ArrayList<>();
        titles.add("Photo");
        titles.add("Video");
        titles.add("QR & Barcode");
        titles.add("Labeling");
        titles.add("Text recognition");
        titles.add("Translate text");
        return titles;
    }

    public static ArrayList<String> getPreviewScale() {
        ArrayList<String> titles = new ArrayList<>();
        titles.add("Fill");
        titles.add("Top");
        titles.add("Center");
        titles.add("Bottom");
        return titles;
    }

    public static ArrayList<String> getExposure() {
        ArrayList<String> titles = new ArrayList<>();
        titles.add("-4");
        titles.add("-3");
        titles.add("-2");
        titles.add("-1");
        titles.add("0");
        titles.add("1");
        titles.add("2");
        titles.add("3");
        titles.add("4");
        return titles;
    }

    public static ArrayList<String> getModes() {
        ArrayList<String> titles = new ArrayList<>();
        titles.add("None");
        titles.add("Auto");
        titles.add("Bokeh");
        titles.add("HDR");
        titles.add("Night");
        titles.add("Face Retouch");
        return titles;
    }

    /**
     * change visibility and start chronometer
     */
    public static void startChronometer(Chronometer chronometer) {
        if (chronometer != null) {
            chronometer.setVisibility(View.VISIBLE);
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
        }
    }

    /**
     * set spinner adapter and change selection
     */
    public static void configLanguagesAdapter(Spinner spinner, Context context) {
        ArrayAdapter<Language> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, getLanguages());
        spinner.setAdapter(arrayAdapter);
        spinner.setSelection(1);
    }

    /**
     * @return languages list
     */
    private static ArrayList<Language> getLanguages() {
        ArrayList<Language> languages = new ArrayList<>();
        languages.add(new Language(TranslateLanguage.SPANISH));
        languages.add(new Language(TranslateLanguage.ENGLISH));
        languages.add(new Language(TranslateLanguage.ITALIAN));
        languages.add(new Language(TranslateLanguage.ROMANIAN));
        languages.add(new Language(TranslateLanguage.RUSSIAN));
        languages.add(new Language(TranslateLanguage.BULGARIAN));
        languages.add(new Language(TranslateLanguage.FRENCH));
        languages.add(new Language(TranslateLanguage.GERMAN));
        languages.add(new Language(TranslateLanguage.POLISH));
        languages.add(new Language(TranslateLanguage.HUNGARIAN));
        languages.add(new Language(TranslateLanguage.PORTUGUESE));
        languages.add(new Language(TranslateLanguage.GEORGIAN));
        languages.add(new Language(TranslateLanguage.CATALAN));
        languages.add(new Language(TranslateLanguage.CHINESE));
        languages.add(new Language(TranslateLanguage.KOREAN));
        languages.add(new Language(TranslateLanguage.JAPANESE));
        languages.add(new Language(TranslateLanguage.SWEDISH));
        languages.add(new Language(TranslateLanguage.TURKISH));
        languages.add(new Language(TranslateLanguage.SLOVAK));
        languages.add(new Language(TranslateLanguage.PERSIAN));
        languages.add(new Language(TranslateLanguage.SLOVENIAN));
        languages.add(new Language(TranslateLanguage.DUTCH));
        languages.add(new Language(TranslateLanguage.GREEK));
        languages.add(new Language(TranslateLanguage.ESPERANTO));
        languages.add(new Language(TranslateLanguage.ESTONIAN));
        return languages;
    }

    /**
     * stop chronometer and change visibility
     */
    public static void stopChronometer(Chronometer chronometer) {
        if (chronometer != null) {
            chronometer.setVisibility(View.INVISIBLE);
            chronometer.stop();
        }
    }

    /**
     * start animation
     */
    public static void startRecAnimation(ImageView imgVideoRec, Animation animationRecVideo) {
        if (imgVideoRec != null && animationRecVideo != null) {
            imgVideoRec.startAnimation(animationRecVideo);
        }
    }

    /**
     * stop animation
     */
    public static void stopRecAnimation(Animation animationRecVideo) {
        if (animationRecVideo != null) {
            animationRecVideo.cancel();
        }
    }

    /**
     * show-hide ui views
     */
    public static void showUIViews(boolean show, View switchAction, View layoutButtonsTop,
                                   ImageButton btnSwitchCamera, ImageFilterView imgLastAction) {
        switchAction.setVisibility(show ? View.VISIBLE : View.GONE);
        layoutButtonsTop.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSwitchCamera.setVisibility(show ? View.VISIBLE : View.GONE);
        imgLastAction.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * @return scale gesture
     */
    public static ScaleGestureDetector.SimpleOnScaleGestureListener getZoomGesture(Camera camera) {
        return new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                MainController.setZoom(camera, detector.getScaleFactor());
                return true;
            }

        };
    }

    /**
     * set new camera zoom
     */
    public static void setZoom(Camera camera, float scaleFactor) {
        if (camera != null) {
            ZoomState zoomState = camera.getCameraInfo().getZoomState().getValue();
            if (zoomState != null) {
                float currentZoom = zoomState.getZoomRatio();
                float newZoom = currentZoom * scaleFactor;
                camera.getCameraControl().setZoomRatio(newZoom);
            }
        }
    }

    /**
     * tap to focus and show animation
     */
    public static void tapToFocus(Camera camera, PreviewView cameraPreview, ImageView imgFocus, MotionEvent event) {
        if (camera != null && cameraPreview != null && event.getAction() == MotionEvent.ACTION_UP) {
            float x = event.getX(), y = event.getY();
            MeteringPointFactory pointFactory = new SurfaceOrientedMeteringPointFactory((float) cameraPreview.getWidth(), (float) cameraPreview.getHeight());
            MeteringPoint point = pointFactory.createPoint(x, y);
            FocusMeteringAction action = new FocusMeteringAction
                    .Builder(point, FocusMeteringAction.FLAG_AF)
                    .setAutoCancelDuration(2, TimeUnit.SECONDS).build();
            camera.getCameraControl().startFocusAndMetering(action);
            //
            AnimUtils.moveImgFocus(imgFocus, x, y);
            AnimUtils.animateImgFocus(imgFocus);
        }
    }

    /**
     * change preview scale type
     */
    public static void setPreviewScaleType(PreviewView cameraPreview, Context context) {
        int scaleType = SharedPrefsSettings.getPreviewScaleType(context);
        switch (scaleType) {
            case 1:
                cameraPreview.setScaleType(PreviewView.ScaleType.FILL_CENTER);
                break;
            case 3:
                cameraPreview.setScaleType(PreviewView.ScaleType.FIT_START);
                break;
            case 4:
                cameraPreview.setScaleType(PreviewView.ScaleType.FIT_CENTER);
                break;
            case 5:
                cameraPreview.setScaleType(PreviewView.ScaleType.FIT_END);
                break;
        }
    }

    /**
     * @return ImageAnalysis (BarcodeAnalyzer, ImageLabelingAnalyzer or TextRecognizerAnalyzer) depending of the position
     */
    public static ImageAnalysis getImageAnalysis(int position,
                                                 TextRecognizerAnalyzer.TextRecognizerListener textRecognizerListener,
                                                 BarcodeAnalyzer.BarcodeListener barcodeListener,
                                                 ImageLabelingAnalyzer.ImageLabelingListener imageLabelingListener,
                                                 Context context) {
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        switch (position) {
            case 2:
                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context), new BarcodeAnalyzer(barcodeListener));
                break;
            case 3:
                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context), new ImageLabelingAnalyzer(imageLabelingListener));
                break;
            case 4:
            case 5:
                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context), new TextRecognizerAnalyzer(textRecognizerListener));
                break;
        }
        return imageAnalysis;
    }

    /**
     * change camera exposure
     */
    @SuppressLint("UnsafeOptInUsageError")
    public static void setExposureCompensation(int index, Camera camera) {
        if (camera != null) {
            ExposureState exposureState = camera.getCameraInfo().getExposureState();
            if (exposureState.isExposureCompensationSupported()) {
                camera.getCameraControl().setExposureCompensationIndex(index);
            }
        }
    }

}
