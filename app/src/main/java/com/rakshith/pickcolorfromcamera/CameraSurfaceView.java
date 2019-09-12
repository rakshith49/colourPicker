package com.rakshith.pickcolorfromcamera;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/**
 * Created by rakshith on 7/6/16.
 */
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    public int colorCode;
    private Camera mCamera;
    private boolean isSurfaceReady;
    private boolean isCameraClicked;
    private int[] mSelectedColor;
    private int POINTER_RADIUS = 5;
    private Camera.Size mPreviewSize;

    public CameraSurfaceView(Context context) {
        super(context);
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
    }

    public void startCameraPreview() {
        isCameraClicked = true;
        if (isSurfaceReady) {
            try {
                safeCameraOpen(Camera.CameraInfo.CAMERA_FACING_BACK);
                if (mCamera != null) {
                    Camera.Parameters parameters = mCamera.getParameters();
                    List<String> focusModes = parameters.getSupportedFocusModes();
                    if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                    }
                    parameters.set("orientation", "portrait");
                    parameters.set("rotation", 90);
                    mCamera.setParameters(parameters);
                    mCamera.setDisplayOrientation(90);
                    mCamera.setPreviewDisplay(getHolder());
                    mCamera.startPreview();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        isSurfaceReady = true;
        if (isCameraClicked) {
            startCameraPreview();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int w, int h) {

        try {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        // start preview with new settings
        try {
            mCamera.startPreview();
            //pick color from camera
            mSelectedColor = new int[3];
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    //color updated continuously
                    colorCode = getColorCode(data, camera);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        releaseCamera();
    }

    /**
     * Stops all running previews and releases the camera
     */

    public void releaseCamera() {
        if (getHolder() != null) {
            getHolder().removeCallback(this);
        }
        stopPreviewAndFreeCamera();

    }


    private void safeCameraOpen(int id) {
        try {
            stopPreviewAndFreeCamera();
            mCamera = Camera.open(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * When this function returns, mCamera will be null.
     */
    private void stopPreviewAndFreeCamera() {
        try {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);
                isCameraClicked = false;
                mCamera.setPreviewCallback(null);
                mCamera.release();
                mCamera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public int getColorCode(byte[] data, Camera camera) {
        mPreviewSize = camera.getParameters().getPreviewSize();
        final int midX = mPreviewSize.width / 2;
        final int midY = mPreviewSize.height / 2;

        // Reset the selected color.
        mSelectedColor[0] = 0;
        mSelectedColor[1] = 0;
        mSelectedColor[2] = 0;

        // Compute the average selected color.
        for (int i = 0; i <= POINTER_RADIUS; i++) {
            for (int j = 0; j <= POINTER_RADIUS; j++) {
                addColorFromYUV420(data, mSelectedColor, (i * POINTER_RADIUS + j + 1),
                        (midX - POINTER_RADIUS) + i, (midY - POINTER_RADIUS) + j,
                        mPreviewSize.width, mPreviewSize.height);
            }
        }
        return Color.rgb(mSelectedColor[0], mSelectedColor[1], mSelectedColor[2]);

    }

    protected void addColorFromYUV420(byte[] data, int[] averageColor, int count, int x, int y, int width, int height) {
        // The code converting YUV420 to rgb format is highly inspired from this post http://stackoverflow.com/a/10125048
        final int size = width * height;
        final int Y = data[y * width + x] & 0xff;
        final int xby2 = x / 2;
        final int yby2 = y / 2;

        final float V = (float) (data[size + 2 * xby2 + yby2 * width] & 0xff) - 128.0f;
        final float U = (float) (data[size + 2 * xby2 + 1 + yby2 * width] & 0xff) - 128.0f;

        // Do the YUV -> RGB conversion
        float Yf = 1.164f * ((float) Y) - 16.0f;
        int red = (int) (Yf + 1.596f * V);
        int green = (int) (Yf - 0.813f * V - 0.391f * U);
        int blue = (int) (Yf + 2.018f * U);

        // Clip rgb values to [0-255]
        red = red < 0 ? 0 : red > 255 ? 255 : red;
        green = green < 0 ? 0 : green > 255 ? 255 : green;
        blue = blue < 0 ? 0 : blue > 255 ? 255 : blue;

        averageColor[0] += (red - averageColor[0]) / count;
        averageColor[1] += (green - averageColor[1]) / count;
        averageColor[2] += (blue - averageColor[2]) / count;
    }

}
