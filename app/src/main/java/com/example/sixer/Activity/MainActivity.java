package com.example.sixer.Activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import com.example.sixer.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import static com.example.sixer.R.id.face_detected;
import static com.example.sixer.R.id.fill;


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

            String fileName = saveBitmapToStorage(imageBitmap);

            Intent in1 = new Intent(MainActivity.this, PicturePreviewActivity.class);
            in1.putExtra("image", fileName);
            startActivity(in1);
        }
    };

    public String saveBitmapToStorage(Bitmap bitmap) {

        String fileName = "bitmap.png";

        try {
            //Write file
            FileOutputStream stream = this.openFileOutput(fileName, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

            //Cleanup
            stream.close();
            bitmap.recycle();

        } catch (Exception e) {
            Toast.makeText(this, "error in saving picture!", Toast.LENGTH_SHORT).show();
        }

        return fileName;
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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


