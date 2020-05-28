package com.example.sixer.ViewModel;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.example.sixer.CameraFrame;
import com.example.sixer.CentroidCalculate;
import com.example.sixer.FrameAnalyzer;
import com.example.sixer.View.MainActivity;

import java.io.IOException;

public class FrontCamera extends SurfaceView implements SurfaceHolder.Callback{

    public static String TAG = "UV";
    public static int FACE_OFFSET = 1000;

    Camera _camera;
    SurfaceHolder surfaceHolder;
    public Camera.Size _size;
    MainActivity _context;

    int faceRectDimWidth = 1;
    int faceRectDimHeight = 1;

    int widthOfFrame = 1;
    int heightOfFrame = 1;

    Bitmap thresholdCropOrDefault;

    double facePositionFracWidth = 1;
    double facePositionFracHeight = 1;

    CameraFrame cameraFrame;
    FrameAnalyzer frameAnalyzer;

    boolean isFaceDetected = false;

    public FrontCamera(MainActivity context, android.hardware.Camera frontCamera) {
        super(context);

        _context = context;
        _camera = frontCamera;
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        cameraFrame = new CameraFrame(context); // threshold image
        frameAnalyzer = new FrameAnalyzer(context); // analyze image
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        setOrientation();

        _camera.setFaceDetectionListener(new FaceDetectionListener());

        try {
            _camera.setPreviewDisplay(holder);
            _camera.startPreview();
            startFaceDetection();

        } catch (IOException e) {
            Log.d("UV", "Error setting camera preview: " + e.getMessage());
        }
    }

    private void setViewParameters() {
        widthOfFrame = pxToDp(_context.frontCameraFrame.getWidth());
        heightOfFrame = pxToDp(_context.frontCameraFrame.getHeight());
    }

    public int pxToDp(int px) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
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
            startFaceDetection();

            setViewParameters();

            _camera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {

                    cameraFrame.createBitmapFromFrame(data, camera);

                    Point startPoint = new Point((int) (facePositionFracWidth * cameraFrame.getWidth()) - (faceRectDimWidth / 2),
                            (int) (facePositionFracHeight * cameraFrame.getHeight()) - (faceRectDimHeight / 2));

                    cameraFrame.setStartPoint(startPoint);
                    frameAnalyzer.setFaceRectDimHeight(faceRectDimHeight);
                    frameAnalyzer.setFaceRectDimWidth(faceRectDimWidth);

                    if (cameraFrame.validateOverflowFrame(startPoint)) {

                        try {
                            cameraFrame.cropFace(faceRectDimWidth, faceRectDimHeight);
                            cameraFrame.setSizeOfCroppedFrame(faceRectDimWidth * faceRectDimHeight);

                        } catch (Exception e) {
                            Toast.makeText(_context, "Error on cropping face!", Toast.LENGTH_LONG).show();
                            return;
                        }

                        try {
                            thresholdCropOrDefault = cameraFrame.Threshold();
                            frameAnalyzer.analyze(thresholdCropOrDefault);

                        } catch (Exception e) {
                            Toast.makeText(_context, "Error on threshold!", Toast.LENGTH_LONG).show();
                            return;
                        }

                    } else {
                        thresholdCropOrDefault = cameraFrame.defaultFrame();
                    }

                    ((Activity) (_context)).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            _context.sandBoxFront.setImageBitmap(thresholdCropOrDefault);
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
        _camera.setFaceDetectionListener(null);
    }

    public void setOrientation() {
        Camera.Parameters parameters = _camera.getParameters();

        _size = parameters.getSupportedPictureSizes().get(1);

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

    public void startFaceDetection() {
        Camera.Parameters params = _camera.getParameters();

        if (params.getMaxNumDetectedFaces() > 0) {
            _camera.startFaceDetection();
        }
    }

    class FaceDetectionListener implements Camera.FaceDetectionListener {
        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {

            if (faces.length > 0) {

                isFaceDetected = true;
                ((Activity) (_context)).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        _context.faceDetectedCheckBox.setChecked(true);
                    }
                });


                int left, right, top, bottom;

                for (Camera.Face face : faces) {

                    left = face.rect.left + FACE_OFFSET;
                    right = face.rect.right + FACE_OFFSET;
                    top = face.rect.top + FACE_OFFSET;
                    bottom = face.rect.bottom + FACE_OFFSET;

                    double faceFracWidth = (right - left) / 2000.0; // size of face
                    double faceFracHeight = (bottom - top) / 2000.0;

                    faceRectDimWidth = (int) (heightOfFrame * faceFracWidth) * 4; // size of face in the camera preview
                    faceRectDimHeight = (int) (widthOfFrame * faceFracHeight) * 4;

                    facePositionFracWidth = ((left + right) / 2.0) / 2000.0;
                    facePositionFracHeight = ((top + bottom) / 2.0) / 2000.0;
                }
            } else {
                ((Activity) (_context)).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        _context.faceDetectedCheckBox.setChecked(false);
                    }
                });
                isFaceDetected = false;
            }
        }
    }


}
