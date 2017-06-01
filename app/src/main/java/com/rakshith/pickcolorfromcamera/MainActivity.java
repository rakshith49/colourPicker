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

import com.rakshith.pickcolorfromcamera.databinding.ActivityMainBinding;


/**
 * Created by Rakshith
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mBinding;
    public static final int GET_CAMERA = 102;
    private static final int SETTING_RESULT = 103;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        checkPermissionOPenCamera();
        mBinding.click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setColorCode(mBinding.searchCameraView.colorcode);
            }

        });


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

    private void setColorCode(int colorcode) {
        if (colorcode != 0) {
            String hexColor = "#" + Integer.toHexString(colorcode).substring(2);
            mBinding.textView.setTextColor(Color.parseColor(hexColor));
        }
    }
}
