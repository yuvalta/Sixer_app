package com.example.sixer.Cameras;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.example.sixer.CameraFrame;
import com.example.sixer.FrameAnalyzer;
import com.example.sixer.Activity.MainActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class FrontCamera extends SurfaceView implements SurfaceHolder.Callback {

    public static String TAG = "UV";
    public static int FACE_OFFSET = 1000;

    public Camera _camera;
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

    File pictureCapture;

    boolean isFaceDetected = false;
    public boolean foundCenter = false;

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

            _camera.setPreviewCallback((Camera.PreviewCallback) (data, camera) -> {

                cameraFrame.createBitmapFromFrame(data, camera);

                Point startPoint = new Point((int) (facePositionFracWidth * cameraFrame.getWidth()) - (faceRectDimWidth / 2),
                        (int) (facePositionFracHeight * cameraFrame.getHeight()) - (faceRectDimHeight / 2));

                cameraFrame.setStartPoint(startPoint);
                frameAnalyzer.setFaceRectDimHeight(faceRectDimHeight);
                frameAnalyzer.setFaceRectDimWidth(faceRectDimWidth);

                if (cameraFrame.validateOverflowFrame(startPoint)) {

                    try {
                        cameraFrame.cropFace(faceRectDimWidth, faceRectDimHeight); // manipulate the frame
                        cameraFrame.setSizeOfCroppedFrame(faceRectDimWidth * faceRectDimHeight); // manipulate the frame

                    } catch (Exception e) {
                        Toast.makeText(_context, "Error on cropping face!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    try {
                        if (isFaceDetected) {
                            thresholdCropOrDefault = cameraFrame.Threshold(); // manipulate the frame
                            if (!foundCenter) {
                                foundCenter = frameAnalyzer.analyze(thresholdCropOrDefault); // start analyze the frame
                            }
                        }

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

    public Bitmap getDefaultFrame() {
        return cameraFrame.defaultFrame();
    }

    public void takePicture() {
        _camera.autoFocus(new Camera.AutoFocusCallback() { // auto focus the image
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                _camera.takePicture(null, null, mPicture);
            }
        });
    }

    public void discardPicture() {
        pictureCapture.delete();
        _camera.startPreview();
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

                    double faceFracWidth = (right - left) / ((double) FACE_OFFSET * 2); // size of face
                    double faceFracHeight = (bottom - top) / ((double) FACE_OFFSET * 2); // divided by FACE_OFFSET * 2 because 'face.rect' returns values between -1000 to 1000

                    faceRectDimWidth = (int) (heightOfFrame * faceFracWidth) * 4; // size of face in the camera preview
                    faceRectDimHeight = (int) (widthOfFrame * faceFracHeight) * 3;

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

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            pictureCapture = getOutputMediaFile(MEDIA_TYPE_IMAGE);

            if (pictureCapture == null) {
                Log.d(TAG, "Error creating media file, check storage permissions");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureCapture);
                fos.write(data);
                fos.close();
                Log.d(TAG, Uri.fromFile(pictureCapture).toString());
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }

        }
    };

    private static File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

}
