package com.example.sixer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;


public class MainActivity extends AppCompatActivity {
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static final String TAG = "UV";

    FrameLayout backCameraFrame;
    FrameLayout frontCameraFrame;
    public RelativeLayout faceRect;

    public Camera backCamera;
    public Camera frontCamera;

    BackCamera backCameraActivity;
    FrontCamera frontCameraActivity;

    public ImageView sandBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sandBox = findViewById(R.id.threshold);

        backCameraFrame = findViewById(R.id.back_camera);
        frontCameraFrame = findViewById(R.id.front_camera);
        faceRect = findViewById(R.id.face_detector_rect);

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        } else {
            showCameras();
        }


    }

    public static Camera getCameraInstance(int cameraId) {
        Camera c = null;
        try {
            c = Camera.open(cameraId); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            Log.e("UV", "Camera " + cameraId + " not available! " + e.toString());
        }
        return c;
    }

    public void showCameras() {

        frontCamera = getCameraInstance(1);
        if (frontCamera != null) {
            frontCameraActivity = new FrontCamera(this, frontCamera);
            frontCameraFrame.addView(frontCameraActivity);
        }

        backCamera = getCameraInstance(0);
        if (backCamera != null) {
            backCameraActivity = new BackCamera(this, backCamera);
            backCameraFrame.addView(backCameraActivity);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showCameras();
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
}


