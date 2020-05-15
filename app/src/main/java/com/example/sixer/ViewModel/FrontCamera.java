package com.example.sixer;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class FrontCamera extends SurfaceView implements SurfaceHolder.Callback {

    public static String TAG = "UV";
    public static int THRESHOLD = 100;
    public static int FACE_OFFSET = 1000;

    Camera _camera;
    SurfaceHolder surfaceHolder;
    public Camera.Size _size;
    MainActivity _context;

    int faceRectDimWidth = 1;
    int faceRectDimHeight = 1;

    int widthOfFrame = 1;
    int heightOfFrame = 1;

    double facePositionFracWidth = 1;
    double facePositionFracHeight = 1;

    CameraFrame cameraFrame;

    public FrontCamera(MainActivity context, android.hardware.Camera frontCamera) {
        super(context);

        _context = context;
        _camera = frontCamera;
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        cameraFrame = new CameraFrame();
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

//                    Camera.Parameters parameters = camera.getParameters();
//                    int width = parameters.getPreviewSize().width;
//                    int height = parameters.getPreviewSize().height;
//
//                    Bitmap faceCrop;
//
//                    int pixelValue;
//                    int pixThresh;
//
//                    YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(), width, height, null);
//
//                    ByteArrayOutputStream out = new ByteArrayOutputStream();
//                    yuv.compressToJpeg(new Rect(0, 0, width, height), 10, out);
//
//                    byte[] bytes = out.toByteArray();
//
//                    final Bitmap fullFrame = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                    Point startPoint = new Point((int) (facePositionFracWidth * width) - (faceRectDimWidth / 2),
                            (int) (facePositionFracHeight * height) - (faceRectDimHeight / 2));

                    if (startPoint.x < fullFrame.getWidth() - 1 && startPoint.y < fullFrame.getHeight() - 1
                            && startPoint.x > 0 && startPoint.y > 0) {
                        faceCrop = Bitmap.createBitmap(fullFrame, startPoint.x, startPoint.y, faceRectDimWidth, faceRectDimHeight);

                        // get all image pixels and iterate on it locally
                        int faceCropPixelsArray[] = new int[faceRectDimWidth * faceRectDimHeight];
                        faceCrop.getPixels(faceCropPixelsArray, 0, faceCrop.getWidth(), 0, 0, faceCrop.getWidth() - 1, faceCrop.getHeight() - 1);

                        for (int i = 0; i < faceRectDimWidth * faceRectDimHeight - 1; i++) {
                            pixelValue = faceCropPixelsArray[i];

                            int R = (pixelValue & 0xff0000) >> 16;
                            int G = (pixelValue & 0x00ff00) >> 8;
                            int B = (pixelValue & 0x0000ff) >> 0;

                            int grayLevel = (R + G + B) / 3;

                            if (grayLevel < THRESHOLD) {
                                pixThresh = Color.BLUE;
                            } else {
                                pixThresh = Color.WHITE;
                            }
                            faceCropPixelsArray[i] = pixThresh;
                        }
                        faceCrop.setPixels(faceCropPixelsArray, 0, faceCrop.getWidth(), 0, 0, faceCrop.getWidth(), faceCrop.getHeight());

                    } else {
                        faceCrop = fullFrame;
                    }

                    ((Activity) (_context)).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            _context.sandBox.setImageBitmap(faceCrop);
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

                int left, right, top, bottom;

                for (Camera.Face face : faces) {
                    _context.faceRect.setVisibility(VISIBLE);

                    left = face.rect.left + FACE_OFFSET;
                    right = face.rect.right + FACE_OFFSET;
                    top = face.rect.top + FACE_OFFSET;
                    bottom = face.rect.bottom + FACE_OFFSET;

                    double faceFracWidth = (right - left) / (2 * FACE_OFFSET); // size of face
                    double faceFracHeight = (bottom - top) / (2 * FACE_OFFSET);

                    faceRectDimWidth = (int) (heightOfFrame * faceFracWidth) * 4; // size of face in the camera preview
                    faceRectDimHeight = (int) (widthOfFrame * faceFracHeight) * 4;

                    facePositionFracWidth = ((left + right) / 2.0) / (2 * FACE_OFFSET);
                    facePositionFracHeight = ((top + bottom) / 2.0) / (2 * FACE_OFFSET);
                }
            }
        }
    }
}
