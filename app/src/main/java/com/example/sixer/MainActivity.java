package com.example.sixer;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sixer.Cameras.BackCamera;
import com.example.sixer.Cameras.FrontCamera;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;

import static com.example.sixer.R.id.face_detected;


public class MainActivity extends AppCompatActivity {
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static final String TAG = "UV";

    public FrameLayout backCameraFrame;
    public FrameLayout frontCameraFrame;
    public RelativeLayout faceRect;

    public Camera backCamera;
    public Camera frontCamera;

    public TextView thresholdTextView;

    public ImageView leftArrow;
    public ImageView rightArrow;
    public ImageView upArrow;
    public ImageView downArrow;
    public ImageView sandBoxFront;
    public ImageView sandBoxBack;

    public CheckBox faceDetectedCheckBox;
    public ImageButton resetButton;

    BackCamera backCameraActivity;
    FrontCamera frontCameraActivity;

    FloatingActionButton takePicFab;

    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sandBoxFront = findViewById(R.id.threshold_front);
        sandBoxBack = findViewById(R.id.threshold_back);

        backCameraFrame = findViewById(R.id.back_camera);
        frontCameraFrame = findViewById(R.id.front_camera);

        leftArrow = findViewById(R.id.left_arrow);
        rightArrow = findViewById(R.id.right_arrow);
        upArrow = findViewById(R.id.up_arrow);
        downArrow = findViewById(R.id.down_arrow);

        faceDetectedCheckBox = findViewById(face_detected);

        resetButton = findViewById(R.id.reset);

        faceRect = findViewById(R.id.face_detector_rect);

        takePicFab = findViewById(R.id.take_picture_fab);

        takePicFab.setOnClickListener(pictureOnClick);

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        } else {
            showCameras();
        }
    }

    private View.OnClickListener pictureOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(MainActivity.this, "click!", Toast.LENGTH_SHORT).show();

            Bitmap imageBitmap = frontCameraActivity.getDefaultFrame(); // open an activity with intent that shows the picture with x or v button
            sandBoxBack.setImageBitmap(imageBitmap);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            sandBoxBack.setImageBitmap(imageBitmap);
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

    public void resetBestPosition(View view) {
        faceRect.setVisibility(View.INVISIBLE);
        frontCameraActivity.foundCenter = false;
    }
}


