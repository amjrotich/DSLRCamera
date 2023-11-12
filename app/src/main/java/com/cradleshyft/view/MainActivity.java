package com.cradleshyft.dslrcamera.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.extensions.ExtensionMode;
import androidx.camera.extensions.ExtensionsManager;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Size;
import android.view.KeyEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.nl.translate.TranslateLanguage;

import java.util.Locale;
import java.util.concurrent.ExecutionException;

import com.cradleshyft.dslrcamera.viewmodel.LibraryViewModel;
import com.cradleshyft.dslrcamera.R;
import com.cradleshyft.dslrcamera.adapter.SelectorAdapter;
import com.cradleshyft.dslrcamera.analyzer.BarcodeAnalyzer;
import com.cradleshyft.dslrcamera.analyzer.ImageLabelingAnalyzer;
import com.cradleshyft.dslrcamera.analyzer.TextRecognizerAnalyzer;
import com.cradleshyft.dslrcamera.controller.LanguageController;
import com.cradleshyft.dslrcamera.controller.MainController;
import com.cradleshyft.dslrcamera.databinding.ActivityMainBinding;
import com.cradleshyft.dslrcamera.model.Language;
import com.cradleshyft.dslrcamera.model.MediaItemObj;
import com.cradleshyft.dslrcamera.util.AnimUtils;
import com.cradleshyft.dslrcamera.util.AudioUtils;
import com.cradleshyft.dslrcamera.util.CameraUtils;
import com.cradleshyft.dslrcamera.util.ClipboardUtils;
import com.cradleshyft.dslrcamera.util.FileUtil;
import com.cradleshyft.dslrcamera.util.SharedPrefsSettings;
import com.cradleshyft.dslrcamera.util.TextUtils;
import com.cradleshyft.dslrcamera.view.viewer.PhotoActivity;
import com.cradleshyft.dslrcamera.view.viewer.VideoActivity;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener,
        TextRecognizerAnalyzer.TextRecognizerListener, BarcodeAnalyzer.BarcodeListener,
        ImageLabelingAnalyzer.ImageLabelingListener, LanguageController.TranslateListener {

    public static final int PERMISSIONS_REQ_CODE = 111;
    private Camera camera;
    private ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;
    private int lensFacingType = CameraSelector.LENS_FACING_BACK,
            flashMode = ImageCapture.FLASH_MODE_OFF,
            timer = -1,
            extensionMode = ExtensionMode.NONE,
            actionPosition = 0;
    private Animation animationRecVideo;
    private ScaleGestureDetector zoomGesture;
    private String resultBarcode,
            resultTextRecognition,
            languageCodeDetected;
    private boolean translateEnabled = false,
            barcodeScanComplete = false,
            textRecognitionComplete = false,
            translateComplete = false;
    private ActivityMainBinding binding;
    private MediaItemObj media = null;
    private LibraryViewModel libraryViewModel;
    private VideoCapture<Recorder> videoCapture;
    private Recording currentRecording = null;
    private VideoRecordEvent recordingState = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        checkPermissions();
        setListeners();
        setObservers();
        setPreviewScaleType();
        MainController.configLanguagesAdapter(binding.views.layoutTranslateText.spinnerLanguages, this);

    }

    private void setObservers() {
        libraryViewModel.mediaItem.observe(this, item -> {
            media = item;
            Glide.with(getBaseContext()).load(media.getUri()).into(binding.views.layoutButtonsCamera.imgLastAction);
        });
    }

    private void init() {
        this.animationRecVideo = AnimationUtils.loadAnimation(getBaseContext(), R.anim.anim_recording);
        this.libraryViewModel = new LibraryViewModel(getApplication());
        //CAMERA ACTIONS
        SelectorAdapter adapterActions = new SelectorAdapter(
                MainController.getTitles(),
                view -> binding.views.rvActions.scrollTo(view)
        );
        binding.views.rvActions.setAdapter(adapterActions);
        //SCALE TYPE
        SelectorAdapter adapterScaleType = new SelectorAdapter(
                MainController.getPreviewScale(),
                view -> binding.views.layoutCameraSettings.rvScaleType.scrollTo(view)
        );
        binding.views.layoutCameraSettings.rvScaleType.setAdapter(adapterScaleType);
        //EXPOSURE
        SelectorAdapter adapterExposure = new SelectorAdapter(
                MainController.getExposure(),
                view -> binding.views.layoutCameraSettings.rvExposure.scrollTo(view)
        );
        binding.views.layoutCameraSettings.rvExposure.setAdapter(adapterExposure);
        binding.views.layoutCameraSettings.rvExposure.snapToDefaultPosition(4);
        //MODES
        SelectorAdapter adapterModes = new SelectorAdapter(
                MainController.getModes(),
                view -> binding.views.layoutCameraSettings.rvFilters.scrollTo(view)
        );
        binding.views.layoutCameraSettings.rvFilters.setAdapter(adapterModes);
    }

    /**
     * init zoom gesture after camera is created
     */
    private void initZoomGesture() {
        this.zoomGesture = new ScaleGestureDetector(this, MainController.getZoomGesture(camera));
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setListeners() {
        SharedPrefsSettings.getPrefsSettings(this).registerOnSharedPreferenceChangeListener(this);

        binding.cameraPreview.getRootView().setOnTouchListener((v, event) -> {
            zoomGesture.onTouchEvent(event);
            MainController.tapToFocus(camera, binding.cameraPreview, binding.imgFocus, event);
            return false;
        });

        binding.views.layoutButtonsTop.btnTimer.setOnClickListener(v -> setTimer());

        binding.views.layoutButtonsTop.btnFlash.setOnClickListener(v -> {
            setFlash();
            enableFlashForVideo();
        });

        binding.views.layoutButtonsCamera.btnTakeAction.setOnClickListener(v -> takeAction());

        binding.views.layoutButtonsCamera.btnSwitchCamera.setOnClickListener(v -> switchCamera());

        binding.views.layoutButtonsCamera.imgLastAction.setOnClickListener(v -> previewLastAction());

        binding.views.layoutButtonsTop.checkboxCameraSettings.setOnCheckedChangeListener((buttonView, isChecked) ->
                binding.views.layoutCameraSettings.getRoot().setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE));

        binding.views.layoutButtonsTop.btnSettings.setOnClickListener(v ->
                startActivity(new Intent(getBaseContext(), SettingsActivity.class)));

        binding.views.layoutBarcode.btnRestartBarcode.setOnClickListener(v -> {
            barcodeScanComplete = false;
            binding.views.layoutBarcode.layoutBarcode.setVisibility(View.GONE);
            binding.views.progressIndicator.setVisibility(View.VISIBLE);
        });
        binding.views.layoutBarcode.btnCopyBarcode.setOnClickListener(v ->
                ClipboardUtils.copyToClipboard(resultBarcode, this));
        binding.views.layoutBarcode.btnShareBarcode.setOnClickListener(v ->
                TextUtils.shareText(resultBarcode, this));

        binding.views.layoutTextRecognition.btnRestartTextRecognition.setOnClickListener(v -> {
            textRecognitionComplete = false;
            binding.views.layoutTextRecognition.layoutTextRecognition.setVisibility(View.GONE);
            binding.views.progressIndicator.setVisibility(View.VISIBLE);
        });
        binding.views.layoutTextRecognition.btnCopyTextRecognition.setOnClickListener(v ->
                ClipboardUtils.copyToClipboard(resultTextRecognition, this));
        binding.views.layoutTextRecognition.btnShareTextRecognition.setOnClickListener(v ->
                TextUtils.shareText(resultTextRecognition, this));

        binding.views.layoutTranslateText.btnRestartTranslateText.setOnClickListener(v -> {
            translateComplete = false;
            binding.views.layoutTranslateText.layoutTranslateText.setVisibility(View.GONE);
            binding.views.progressIndicator.setVisibility(View.VISIBLE);
        });

        binding.views.layoutTranslateText.spinnerLanguages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //change textview to white
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                //get language from spinner
                Language language = (Language) binding.views.layoutTranslateText.spinnerLanguages.getSelectedItem();
                //download language model if is necessary and translate text
                LanguageController.downloadLanguage(resultTextRecognition, languageCodeDetected,
                        language.getCode(), MainActivity.this);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.views.layoutCameraSettings.rvFilters.setOnNewPositionListener(position -> {

            switch (position) {
                case 0:
                    extensionMode = ExtensionMode.NONE;
                    break;
                case 1:
                    extensionMode = ExtensionMode.AUTO;
                    break;
                case 2:
                    extensionMode = ExtensionMode.BOKEH;
                    break;
                case 3:
                    extensionMode = ExtensionMode.HDR;
                    break;
                case 4:
                    extensionMode = ExtensionMode.NIGHT;
                    break;
                case 5:
                    extensionMode = ExtensionMode.FACE_RETOUCH;
                    break;
            }
            startPreviewCamera();
        });

        binding.views.layoutCameraSettings.rvExposure.setOnNewPositionListener(position -> {
            String exp = MainController.getExposure().get(position);
            int expInt = Integer.parseInt(exp);
            MainController.setExposureCompensation(expInt, camera);
        });

        binding.views.layoutCameraSettings.rvScaleType.setOnNewPositionListener(position -> {
            switch (position) {
                case 0:
                    SharedPrefsSettings.setPreviewScaleType(1, getBaseContext());
                    break;
                case 1:
                    SharedPrefsSettings.setPreviewScaleType(3, getBaseContext());
                    break;
                case 2:
                    SharedPrefsSettings.setPreviewScaleType(4, getBaseContext());
                    break;
                case 3:
                    SharedPrefsSettings.setPreviewScaleType(5, getBaseContext());
                    break;
            }
        });

        binding.views.rvActions.setOnNewPositionListener(position -> {
            actionPosition = position;
            //show-hide rec img
            AnimUtils.scaleView(binding.views.layoutButtonsCamera.imgVideoRec, position == 1 ? 1f : 0f);
            //restart preview camera, necessary to set image-video capture
            new Handler().postDelayed(this::startPreviewCamera, 200);
            //change alpha
            AnimUtils.setAlpha(binding.views.layoutButtonsTop.btnTimer, position == 0 ? 1f : 0.5f);
            AnimUtils.setAlpha(binding.views.layoutButtonsCamera.layoutBottom, position == 0 || position == 1 ? 1f : 0.5f);
            //enable-disable buttons, depending of the position
            binding.views.layoutButtonsTop.btnTimer.setEnabled(position == 0);
            binding.views.layoutButtonsCamera.btnTakeAction.setEnabled(position == 0 || position == 1);
            binding.views.layoutButtonsCamera.btnSwitchCamera.setEnabled(position == 0 || position == 1);
            binding.views.layoutButtonsCamera.imgLastAction.setEnabled(position == 0 || position == 1);
            //hide layouts and set complete task as false
            binding.views.layoutBarcode.layoutBarcode.setVisibility(View.GONE);
            barcodeScanComplete = false;
            //
            binding.views.txtResultLabeling.setVisibility(View.GONE);
            //
            binding.views.layoutTextRecognition.layoutTextRecognition.setVisibility(View.GONE);
            textRecognitionComplete = false;
            //
            binding.views.layoutTranslateText.layoutTranslateText.setVisibility(View.GONE);
            translateComplete = false;
            //hide progress when is in photo-video mode
            binding.views.progressIndicator.setVisibility(position == 0 || position == 1 ? View.GONE : View.VISIBLE);

        });
    }


    /**
     * show photo or video
     */
    private void previewLastAction() {
        if (media == null) return;
        int type = media.getMediaType();
        Intent intent;
        if (type == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
            intent = new Intent(this, PhotoActivity.class);
            intent.putExtra(PhotoActivity.PHOTO_KEY, media.getUri().toString());
        } else {
            intent = new Intent(this, VideoActivity.class);
            intent.putExtra(VideoActivity.VIDEO_KEY, media.getUri().toString());
        }
        startActivity(intent);
    }

    /**
     * take photo or start-stop recording video
     * 0 = photo
     * 1 = video
     */
    @SuppressLint("RestrictedApi")
    private void takeAction() {
        if (actionPosition == 0) {
            if (timer == -1) {
                takePhoto();
            } else {
                binding.views.layoutButtonsCamera.btnTakeAction.setVisibility(View.GONE);
                binding.views.layoutButtonsCamera.imgCenterTakeAction.setVisibility(View.GONE);
                MainController.showUIViews(false, binding.views.rvActionsParent, binding.views.layoutButtonsTop.layoutTop,
                        binding.views.layoutButtonsCamera.btnSwitchCamera, binding.views.layoutButtonsCamera.imgLastAction);
                startCountDown();
            }
        } else {
            if (recordingState == null || recordingState instanceof VideoRecordEvent.Finalize) {
                recordVideo();
                MainController.startRecAnimation(binding.views.layoutButtonsCamera.imgVideoRec, animationRecVideo);
                MainController.startChronometer(binding.views.chronometerVideo);
                MainController.showUIViews(false,
                        binding.views.rvActionsParent,
                        binding.views.layoutButtonsTop.layoutTop,
                        binding.views.layoutButtonsCamera.btnSwitchCamera,
                        binding.views.layoutButtonsCamera.imgLastAction);
            } else {
                if (currentRecording != null) {
                    currentRecording.stop();
                    currentRecording = null;
                }
                disableFlashForVideo();
                AudioUtils.playSound(R.raw.stop_recording_sound, getBaseContext());
                MainController.stopRecAnimation(animationRecVideo);
                MainController.stopChronometer(binding.views.chronometerVideo);
                MainController.showUIViews(true,
                        binding.views.rvActionsParent,
                        binding.views.layoutButtonsTop.layoutTop,
                        binding.views.layoutButtonsCamera.btnSwitchCamera,
                        binding.views.layoutButtonsCamera.imgLastAction);
            }
        }
    }

    /**
     * show count down and take photo after
     */
    private void startCountDown() {
        binding.views.txtCountDownTakePhoto.setVisibility(View.VISIBLE);
        new CountDownTimer(timer * 1000L, 1000) {
            public void onTick(long millisUntilFinished) {
                AudioUtils.playSound(R.raw.beep_timer_sound, getBaseContext());
                binding.views.txtCountDownTakePhoto.setText(String.valueOf((millisUntilFinished / 1000) + 1));
            }

            public void onFinish() {
                takePhoto();
                binding.views.txtCountDownTakePhoto.setVisibility(View.INVISIBLE);
                binding.views.layoutButtonsCamera.btnTakeAction.setVisibility(View.VISIBLE);
                binding.views.layoutButtonsCamera.imgCenterTakeAction.setVisibility(View.VISIBLE);
                MainController.showUIViews(true, binding.views.rvActionsParent, binding.views.layoutButtonsTop.layoutTop,
                        binding.views.layoutButtonsCamera.btnSwitchCamera, binding.views.layoutButtonsCamera.imgLastAction);
            }
        }.start();
    }

    /**
     * record video and save into gallery
     */
    private void recordVideo() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            ContentValues contentValues = FileUtil.getVideoContentValues();

            MediaStoreOutputOptions mediaStoreOutputOptions = new MediaStoreOutputOptions.Builder(
                    getContentResolver(),
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            ).setContentValues(contentValues).build();

            try {
                currentRecording = videoCapture
                        .getOutput()
                        .prepareRecording(this, mediaStoreOutputOptions)
                        .withAudioEnabled()
                        .start(ContextCompat.getMainExecutor(this), event -> recordingState = event);
            } catch (IllegalStateException e) {
                Toast.makeText(getBaseContext(), "Error recording video: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }

        }
    }

    /**
     * take photo and save into gallery
     */
    private void takePhoto() {
        AudioUtils.playSound(R.raw.take_photo_sound, this);
        binding.views.layoutButtonsCamera.btnTakeAction.setEnabled(false);
        AnimUtils.animateBtnTakePhoto(binding.views.layoutButtonsCamera.btnTakeAction);

        ContentResolver resolver = getContentResolver();
        ContentValues values = FileUtil.getPicturesContentValues();
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.
                Builder(resolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values).build();

        this.imageCapture.setTargetRotation(CameraUtils.getDisplayRotation(this));
        this.imageCapture.setFlashMode(flashMode);
        this.imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        binding.views.layoutButtonsCamera.btnTakeAction.setEnabled(true);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        binding.views.layoutButtonsCamera.btnTakeAction.setEnabled(true);
                        binding.views.txtCountDownTakePhoto.setVisibility(View.INVISIBLE);
                        Toast.makeText(getBaseContext(), "Error taking photo: " + exception.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    /**
     * change lens type
     * stop and start preview camera
     * animate switch button
     */
    private void switchCamera() {
        lensFacingType = lensFacingType == CameraSelector.LENS_FACING_BACK
                ? CameraSelector.LENS_FACING_FRONT
                : CameraSelector.LENS_FACING_BACK;
        startPreviewCamera();
        AnimUtils.rotateSwitchCameraButton(binding.views.layoutButtonsCamera.btnSwitchCamera);
    }

    private void setPreviewScaleType() {
        MainController.setPreviewScaleType(binding.cameraPreview,
                this);
    }

    /**
     * change flash mode
     * animate flash button
     */
    private void setFlash() {
        switch (flashMode) {
            case ImageCapture.FLASH_MODE_OFF:
                flashMode = ImageCapture.FLASH_MODE_ON;
                AnimUtils.changeButtonIcon(binding.views.layoutButtonsTop.btnFlash, R.drawable.ic_flash_on, getBaseContext());
                break;
            case ImageCapture.FLASH_MODE_ON:
                flashMode = ImageCapture.FLASH_MODE_AUTO;
                AnimUtils.changeButtonIcon(binding.views.layoutButtonsTop.btnFlash, R.drawable.ic_flash_auto, getBaseContext());
                break;
            case ImageCapture.FLASH_MODE_AUTO:
                flashMode = ImageCapture.FLASH_MODE_OFF;
                AnimUtils.changeButtonIcon(binding.views.layoutButtonsTop.btnFlash, R.drawable.ic_flash_off, getBaseContext());
                break;
        }
    }

    /**
     * change timer
     * animate timer button
     * -1 = timer disabled
     */
    private void setTimer() {
        switch (timer) {
            case -1:
                timer = 3;
                AnimUtils.changeButtonIcon(binding.views.layoutButtonsTop.btnTimer, R.drawable.ic_timer_3, getBaseContext());
                break;
            case 3:
                timer = 10;
                AnimUtils.changeButtonIcon(binding.views.layoutButtonsTop.btnTimer, R.drawable.ic_timer_10, getBaseContext());
                break;
            case 10:
                timer = -1;
                AnimUtils.changeButtonIcon(binding.views.layoutButtonsTop.btnTimer, R.drawable.ic_timer_off, getBaseContext());
                break;
        }
    }

    /**
     * start preview camera with image-video capture
     */
    @SuppressLint("RestrictedApi")
    private void startPreviewCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProvideFuture = ProcessCameraProvider.getInstance(this);

        cameraProvideFuture.addListener(() -> {
            try {
                initImageCapture();
                initVideoCapture();
                //
                cameraProvider = cameraProvideFuture.get();
                cameraProvider.unbindAll();
                //
                ListenableFuture<ExtensionsManager> future = ExtensionsManager.getInstanceAsync(this, cameraProvider);
                ExtensionsManager extensionsManager = future.get();
                //
                initCamera(extensionsManager);
                enableFlashForVideo();
                initZoomGesture();

            } catch (ExecutionException | InterruptedException | NullPointerException | IllegalStateException e) {
                e.getCause();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void initCamera(ExtensionsManager extensionsManager) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(binding.cameraPreview.getSurfaceProvider());
        enableTranslateMode(actionPosition);
        if (actionPosition == 2 || actionPosition == 3 || actionPosition == 4 || actionPosition == 5) {
            CameraSelector backCameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

            ImageAnalysis imageAnalysis = MainController.getImageAnalysis(actionPosition,
                    this, this, this, this);

            camera = cameraProvider.bindToLifecycle(this, backCameraSelector,
                    imageCapture, imageAnalysis, preview);

        } else {
            CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacingType).build();
            boolean isAvailable = extensionsManager.isExtensionAvailable(cameraSelector, extensionMode);
            if (!isAvailable)
                Toast.makeText(this, "This extension mode is not available in your device...", Toast.LENGTH_SHORT).show();

            CameraSelector cameraSelectorExtension = extensionsManager.getExtensionEnabledCameraSelector(
                    cameraSelector, isAvailable ? extensionMode : ExtensionMode.NONE);

            camera = cameraProvider.bindToLifecycle(this, cameraSelectorExtension,
                    actionPosition == 0 ? imageCapture : videoCapture, preview);
        }

    }

    /**
     * enable - disable translate mode
     * 5 = position of "Translate text" in TabLayout
     */
    private void enableTranslateMode(int viewPagerPosition) {
        translateEnabled = viewPagerPosition == 5;
    }

    @SuppressLint("RestrictedApi")
    private void initVideoCapture() {
        int videoSize = SharedPrefsSettings.getVideoSize(getBaseContext());
        Quality quality = Quality.FHD;

        switch (videoSize) {
            case 2160:
                quality = Quality.UHD;
                break;
            case 720:
                quality = Quality.HD;
                break;
            case 480:
                quality = Quality.SD;
                break;
        }

        Recorder recorder = new Recorder.Builder()
                .setQualitySelector(QualitySelector.from(quality))
                .build();

        videoCapture = VideoCapture.withOutput(recorder);
    }

    @SuppressLint("RestrictedApi")
    private void initImageCapture() {
        int maxQuality = SharedPrefsSettings.getImageMaxQuality(this) ?
                ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY : ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY;
        int[] imageWidthHeight = SharedPrefsSettings.getImageSize(this);
        int imageSizeType = SharedPrefsSettings.getImageSizeType(this);
        ImageCapture.Builder builder = new ImageCapture.Builder();

        if (imageWidthHeight == null) {
            builder.setCaptureMode(maxQuality);
            imageCapture = builder.build();
        } else {
            int aspectRatio = imageSizeType == 2 || imageSizeType == 3 ? AspectRatio.RATIO_16_9 : AspectRatio.RATIO_4_3;
            imageCapture = builder
                    .setMaxResolution(new Size(imageWidthHeight[0], imageWidthHeight[1]))
                    .setTargetAspectRatio(aspectRatio)
                    .setCaptureMode(maxQuality).build();
        }
    }

    /**
     * enable-disable flash for video, barcode scanner, labeling, text recognition & translate text
     */
    private void enableFlashForVideo() {
        if (camera != null && (actionPosition == 1 || actionPosition == 2 || actionPosition == 3 || actionPosition == 4 || actionPosition == 5)) {
            camera.getCameraControl().enableTorch(flashMode == ImageCapture.FLASH_MODE_ON || flashMode == ImageCapture.FLASH_MODE_AUTO);
        }
    }

    /**
     * disable flash after finish recording video
     */
    private void disableFlashForVideo() {
        if (camera != null && (flashMode == ImageCapture.FLASH_MODE_ON || flashMode == ImageCapture.FLASH_MODE_AUTO)) {
            camera.getCameraControl().enableTorch(false);
            flashMode = ImageCapture.FLASH_MODE_OFF;
            AnimUtils.changeButtonIcon(binding.views.layoutButtonsTop.btnFlash, R.drawable.ic_flash_off, getBaseContext());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        SharedPrefsSettings.getPrefsSettings(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * check camera, record audio and storage permissions
     * start preview camera or request permissions
     */
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                startPreviewCamera();
                libraryViewModel.loadMediaItems();
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.RECORD_AUDIO},
                        MainActivity.PERMISSIONS_REQ_CODE);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                startPreviewCamera();
                libraryViewModel.loadMediaItems();
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                        MainActivity.PERMISSIONS_REQ_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MainActivity.PERMISSIONS_REQ_CODE) {
            if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startPreviewCamera();
                libraryViewModel.loadMediaItems();
            } else {
                Toast.makeText(this, "Permissions necessary...", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    /**
     * take photo, start-stop recording from volume buttons
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if (actionPosition == 0 || actionPosition == 1) {
                        takeAction();
                    }
                    return true;
                default:
                    return super.dispatchKeyEvent(event);
            }
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * when user change some settings, this will be trigger some action and depending of the key.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case SharedPrefsSettings.GRID_LINES:
                boolean showGrid = sharedPreferences.getBoolean(SharedPrefsSettings.GRID_LINES, true);
                binding.gridLinesView.showGrid(showGrid);
                break;
            case SharedPrefsSettings.PREVIEW_SCALE_TYPE:
                setPreviewScaleType();
                break;
            case SharedPrefsSettings.IMAGE_SIZE:
            case SharedPrefsSettings.VIDEO_RESOLUTION:
                startPreviewCamera();
                break;
        }
    }

    @Override
    public void onBarcodeScanComplete(String result) {
        if (!barcodeScanComplete) {
            //hide progress
            binding.views.progressIndicator.setVisibility(View.GONE);
            //assign result...used for share & copy action
            resultBarcode = result;
            //set result
            binding.views.layoutBarcode.txtResultBarcode.setText(result);
            //show layout
            binding.views.layoutBarcode.layoutBarcode.setVisibility(View.VISIBLE);
        }
        //notify as task complete
        barcodeScanComplete = true;
    }

    @Override
    public void onBarcodeScanFailure(Exception e) {
        Toast.makeText(getBaseContext(), "Barcode scan error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onImageLabelingComplete(String result) {
        //hide progress
        binding.views.progressIndicator.setVisibility(View.GONE);
        //set result
        binding.views.txtResultLabeling.setText(result);
        //show layout
        binding.views.txtResultLabeling.setVisibility(View.VISIBLE);
    }

    @Override
    public void onImageLabelingFailure(Exception e) {
        Toast.makeText(getBaseContext(), "Image labeling error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onTextRecognizerComplete(String text, String languageCode) {
        if (translateEnabled) {
            //5 = translate text
            if (!languageCode.equals("und") && !translateComplete && actionPosition == 5) {
                resultTextRecognition = text;
                this.languageCodeDetected = languageCode;
                //set language
                binding.views.layoutTranslateText.txtTranslateFrom.setText("From " + Locale.forLanguageTag(languageCode).getDisplayName() + " to ");
                //set text
                binding.views.layoutTranslateText.txtTextDetected.setText(text);
                //download language model and translate text
                LanguageController.downloadLanguage(text, languageCode, TranslateLanguage.ENGLISH, this);
            }
        } else {
            //4 = position text recognition
            if (!textRecognitionComplete && actionPosition == 4) {
                //get language
                String language = languageCode.equals("und") ? "Unknown" : Locale.forLanguageTag(languageCode).getDisplayName();
                //hide progress
                binding.views.progressIndicator.setVisibility(View.GONE);
                //set language
                binding.views.layoutTextRecognition.txtLanguageTextRecognition.setText("Language: " + language);
                //set result
                binding.views.layoutTextRecognition.txtResultTextRecognition.setText(text);
                //show layout
                binding.views.layoutTextRecognition.layoutTextRecognition.setVisibility(View.VISIBLE);
            }
            //notify as task complete
            textRecognitionComplete = true;
        }
    }

    @Override
    public void onTextRecognizerFailure(Exception e) {
        Toast.makeText(getBaseContext(), "Error recognize text: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTranslateComplete(String translatedText) {
        //5 = translate text
        if (actionPosition == 5) {
            //hide progress
            binding.views.progressIndicator.setVisibility(View.GONE);
            //set result
            binding.views.layoutTranslateText.txtResultTranslation.setText(translatedText);
            //show layout translate text
            binding.views.layoutTranslateText.layoutTranslateText.setVisibility(View.VISIBLE);
            //notify as task complete
            translateComplete = true;
        }
    }

    @Override
    public void onTranslateFailure(Exception e) {
        Toast.makeText(getBaseContext(), "Error translating: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }

}