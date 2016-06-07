package com.rakshith.manthra.sathya.pickcolorfromcamera;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.rakshith.manthra.sathya.pickcolorfromcamera.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.searchCameraView.startCameraPreview();
        binding.click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendColorCode(binding.searchCameraView.colorcode);
            }

        });


    }

    private void sendColorCode(int colorcode) {
        if (colorcode != 0) {
            String hexColor = "#" + Integer.toHexString(colorcode).substring(2);
            binding.textView.setTextColor(Color.parseColor(hexColor));
        }
    }
}
