package com.rakshith.manthra.sathya.pickcolorfromcamera;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/**
 * Created by rakshith on 7/6/16.
 */
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    public int colorcode;
    private Camera mCamera;
    private boolean isSurfaceReady;
    private boolean isCameraClicked;
    private boolean isCameraPreview;
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
                getHolder().addCallback(this);
                safeCameraOpen(Camera.CameraInfo.CAMERA_FACING_BACK);
                if (mCamera != null) {
                    Camera.Parameters parameters = mCamera.getParameters();
                    List<String> focusModes = parameters.getSupportedFocusModes();
                    if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                    }
                    Camera.Size previewSize = getOptimalPreviewSize(parameters.getSupportedPreviewSizes(), getWidth(), getHeight());
                    parameters.setPreviewSize(previewSize.width, previewSize.height);
                    parameters.setPictureFormat(ImageFormat.JPEG);
                    Camera.Size pictureSize = getOptimalPreviewSize(parameters.getSupportedPictureSizes(), getWidth(), getHeight());
                    parameters.setPictureSize(pictureSize.width, pictureSize.height);
                    parameters.set("orientation", "portrait");
                    parameters.set("rotation", 90);
                    mCamera.setParameters(parameters);
                    mCamera.setDisplayOrientation(90);
                    mCamera.setPreviewDisplay(getHolder());
                    mCamera.startPreview();
                    isCameraPreview = true;
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

        try

        {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            isCameraPreview = false;
        } catch (Exception e)


        {
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try

        {
            mCamera.getParameters().setPictureSize(w, h);
            mCamera.getParameters().setPictureFormat(format);
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
            isCameraPreview = true;

            //pick color from camera
            mSelectedColor = new int[3];
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    colorcode = getColorCode(data, camera);
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
                isCameraPreview = false;
                isCameraClicked = false;
                mCamera.setPreviewCallback(null);
                mCamera.release();
                mCamera = null;
            }
        } catch (Exception e) {
        }
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE || size.width > 1920) continue;
            if (Math.abs(size.height - h) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - h);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - h);
                }
            }
        }
        return optimalSize;
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
