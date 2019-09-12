package com.rakshith.pickcolorfromcamera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.rakshith.pickcolorfromcamera.databinding.ActivityMainBinding;


/**
 * Created by Rakshith
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mBinding;
    public static final int GET_CAMERA = 102;
    private static final int SETTING_RESULT = 103;
    int randomColor = -1;
    private ColorUtil colorUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        checkPermissionOPenCamera();
        mBinding.selectColorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setColorCode(mBinding.searchCameraView.colorCode);

            }

        });

        colorUtil = new ColorUtil();
        colorUtil.addValuestoMap();
        randomColor = colorUtil.getRandomColor();
        mBinding.shownColor.setBackgroundColor(randomColor);


    }

    /**
     * checking for camera permission
     */
    private void checkPermissionOPenCamera() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale
                    (this, Manifest.permission.CAMERA)) {
                //If permission is denied first time... then this method will be called
                showSnackBar();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission
                                .CAMERA},
                        GET_CAMERA);
            }

        } else {
            mBinding.searchCameraView.startCameraPreview();

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTING_RESULT) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                this.finish();
            } else {
                showSnackBar();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case GET_CAMERA: {
                if ((grantResults.length > 0) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mBinding.searchCameraView.startCameraPreview();
                } else {
                    showSnackBar();
                }
            }
            break;
        }
    }

    private void showSnackBar() {
        Snackbar.make(mBinding.getRoot(),
                getResources().getString(R.string.permission_required_camera),
                Snackbar.LENGTH_INDEFINITE).setAction(getResources().getString(R.string.permission_enable),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, SETTING_RESULT);
                    }
                }).setDuration(Snackbar.LENGTH_LONG).show();

    }

    private void setColorCode(int colorCode) {
        if (colorCode != 0) {
            String hexColor = "#" + Integer.toHexString(colorCode).substring(2);
            mBinding.selectedColor.setBackgroundColor(colorCode);

            int redValue = Color.red(Color.parseColor(hexColor));
            int blueValue = Color.blue(Color.parseColor(hexColor));
            int greenValue = Color.green(Color.parseColor(hexColor));

            int redPer = (redValue * 100) / 255;
            int bluePer = (blueValue * 100) / 255;
            int greenPer = (greenValue * 100) / 255;


            int shownColorRedVal = Color.red(randomColor);
            int shownColorBlueVal = Color.blue(randomColor);

            int shownColorGreenVal = Color.green(randomColor);

            int shownRedPer = (shownColorRedVal * 100) / 255;
            int shownBluePer = (shownColorBlueVal * 100) / 255;
            int shownGreenPer = (shownColorGreenVal * 100) / 255;

            int diffRed = Math.abs(shownRedPer - redPer);
            int diffBlue = Math.abs(shownBluePer - bluePer);
            int diffGreen = Math.abs(shownGreenPer - greenPer);


            if (diffRed <= 15 && diffBlue <= 15 && diffGreen <= 15) {
                Toast.makeText(this, "Yeah!!!, Colour is matched", Toast.LENGTH_LONG).show();
                randomColor = colorUtil.getRandomColor();
                mBinding.shownColor.setBackgroundColor(randomColor);

            } else {
                Toast.makeText(this, "Oh Ohhhh!!!, Try again", Toast.LENGTH_LONG).show();
            }


        }
    }
}
