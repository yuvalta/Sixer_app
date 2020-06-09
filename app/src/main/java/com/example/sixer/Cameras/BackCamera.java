package com.example.sixer.Cameras;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.example.sixer.CameraFrame;
import com.example.sixer.FrameAnalyzer;
import com.example.sixer.MainActivity;

import java.io.IOException;

public class BackCamera extends SurfaceView implements SurfaceHolder.Callback {

    public static String TAG = "BackCamera";

    public Camera.Size _size;
    Camera _camera;
    SurfaceHolder surfaceHolder;
    MainActivity _context;

    CameraFrame cameraFrame;
    FrameAnalyzer frameAnalyzer;
//    CentroidCalculate centroidCalculate;

    Bitmap thresholdBackFrame;

    public BackCamera(MainActivity context, android.hardware.Camera backCamera) {
        super(context);

        _context = context;
        _camera = backCamera;
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        cameraFrame = new CameraFrame(context); // threshold image
        frameAnalyzer = new FrameAnalyzer(context); // analyze image
//        centroidCalculate = new CentroidCalculate(); // centroid
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        try {
            setOrientation();
        } catch (Exception exp) {
            Log.d(TAG, "Error");
            return;
        }


        try {
            _camera.setPreviewDisplay(holder);
            _camera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (surfaceHolder.getSurface() == null) {
            return;
        }

        try {
            _camera.stopPreview();
            _camera.setPreviewDisplay(surfaceHolder);
            _camera.startPreview();

            _camera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {

                    Bitmap frameBitmap = cameraFrame.createBitmapFromFrame(data, camera);

                    try {
                        cameraFrame.setFaceCrop(cameraFrame.getResizedBitmap(frameBitmap, 400, 200));
                        cameraFrame.setSizeOfCroppedFrame(200 * 400);

                    } catch (Exception e) {
                        Toast.makeText(_context, "Error on cropping face!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    try {
                        thresholdBackFrame = cameraFrame.Threshold();

                    } catch (Exception e) {

                        Toast.makeText(_context, "Error on threshold!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    ((Activity) (_context)).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            _context.sandBoxBack.setImageBitmap(thresholdBackFrame);
                        }
                    });
                }
            });

        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        _camera.stopPreview();
    }

    public void setOrientation() {
        Camera.Parameters parameters = _camera.getParameters();

        _size = parameters.getSupportedPictureSizes().get(2);

        parameters.setPictureSize(_size.width, _size.height);

        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            parameters.set("orientation", "portrait");
            _camera.setDisplayOrientation(90);
            parameters.setRotation(90);
        } else {
            parameters.set("orientation", "landscape");
            _camera.setDisplayOrientation(0);
            parameters.setRotation(0);
        }

        _camera.setParameters(parameters);
    }
}
