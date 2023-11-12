package com.cradleshyft.dslrcamera.view;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.cradleshyft.dslrcamera.R;
import com.cradleshyft.dslrcamera.controller.SettingsController;
import com.cradleshyft.dslrcamera.databinding.ActivitySettingsBinding;
import com.cradleshyft.dslrcamera.util.SharedPrefsSettings;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        setListeners();
        SettingsController.setMemory(binding.txtMemory, this);
        SettingsController.setVideoResolution(binding.layoutVideoResolution.chipGroupVideoResolution, this);
        SettingsController.setImageSize(binding.layoutImageSize.chipGroupImageSize, this);
        SettingsController.setStatusCBSound(binding.checkBoxSound, this);
        SettingsController.setStatusGridLines(binding.checkBoxGridLines, this);
        SettingsController.setImageMaxQuality(binding.checkBoxImageMaxQuality, this);
    }

    private void setListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.layoutVideoResolution.chipGroupVideoResolution.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int checkedId = group.getCheckedChipId();
            if (checkedId == R.id.vResolution1440p) {
                SharedPrefsSettings.setVideoSize(2160, getBaseContext());
            } else if (checkedId == R.id.vResolution1080p) {
                SharedPrefsSettings.setVideoSize(1080, getBaseContext());
            } else if (checkedId == R.id.vResolution720p) {
                SharedPrefsSettings.setVideoSize(720, getBaseContext());
            } else if (checkedId == R.id.vResolution480p) {
                SharedPrefsSettings.setVideoSize(480, getBaseContext());
            }
        });

        binding.layoutImageSize.chipGroupImageSize.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int checkedId = group.getCheckedChipId();
            if (checkedId == R.id.imgSizeFull) {
                SharedPrefsSettings.setImageSize(1, getBaseContext());
            } else if (checkedId == R.id.imgSize169_4032) {
                SharedPrefsSettings.setImageSize(2, getBaseContext());
            } else if (checkedId == R.id.imgSize169_2560) {
                SharedPrefsSettings.setImageSize(3, getBaseContext());
            } else if (checkedId == R.id.imgSize43_4032) {
                SharedPrefsSettings.setImageSize(4, getBaseContext());
            } else if (checkedId == R.id.imgSize43_2880) {
                SharedPrefsSettings.setImageSize(5, getBaseContext());
            }
        });

        binding.checkBoxSound.setOnCheckedChangeListener((buttonView, isChecked) ->
                SharedPrefsSettings.setSoundStatus(isChecked, getBaseContext()));

        binding.checkBoxGridLines.setOnCheckedChangeListener((buttonView, isChecked) ->
                SharedPrefsSettings.setGridLinesStatus(isChecked, getBaseContext()));

        binding.checkBoxImageMaxQuality.setOnCheckedChangeListener((buttonView, isChecked) ->
                SharedPrefsSettings.setImageMaxQuality(isChecked, getBaseContext()));
    }

}