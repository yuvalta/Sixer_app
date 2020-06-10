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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
import static com.example.sixer.R.id.face_detected;
import static com.example.sixer.R.id.fill;
import static com.example.sixer.R.id.show_centroid;


public class MainActivity extends AppCompatActivity {
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static final String TAG = "UV";

    public FrameLayout backCameraFrame;
    public FrameLayout frontCameraFrame;
    public RelativeLayout faceRect;

    public LinearLayout thresholdLayout;
    public LinearLayout afterPicMenu;

    public GridLayout arrowGrid;

    public Camera backCamera;
    public Camera frontCamera;

    public ImageView leftArrow;
    public ImageView rightArrow;
    public ImageView upArrow;
    public ImageView downArrow;
    public ImageView sandBoxFront;
    public ImageView sandBoxBack;

    public CheckBox faceDetectedCheckBox;

    public ImageButton resetButton;
    ImageButton shareButton;
    ImageButton deleteButton;
    ImageButton saveButton;


    BackCamera backCameraActivity;
    FrontCamera frontCameraActivity;

    FloatingActionButton takePicFab;

    Uri imageUri;
    private boolean backPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sandBoxFront = findViewById(R.id.threshold_front);
        sandBoxBack = findViewById(R.id.threshold_back);

        thresholdLayout = findViewById(R.id.threshold_layout);
        afterPicMenu = findViewById(R.id.bottom_bar);
        arrowGrid = findViewById(R.id.arrows_grid);

        backCameraFrame = findViewById(R.id.back_camera);
        frontCameraFrame = findViewById(R.id.front_camera);

        leftArrow = findViewById(R.id.left_arrow);
        rightArrow = findViewById(R.id.right_arrow);
        upArrow = findViewById(R.id.up_arrow);
        downArrow = findViewById(R.id.down_arrow);

        shareButton = findViewById(R.id.share_button);
        deleteButton = findViewById(R.id.delete_button);
        saveButton = findViewById(R.id.save_button);
        shareButton.setOnClickListener(shareButtonClickListener);
        saveButton.setOnClickListener(saveButtonClickListener);
        deleteButton.setOnClickListener(deleteButtonClickListener);

        faceDetectedCheckBox = findViewById(face_detected);

        faceRect = findViewById(R.id.face_detector_rect);

        takePicFab = findViewById(R.id.take_picture_fab);
        takePicFab.setOnClickListener(pictureOnClick);

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_CAMERA_REQUEST_CODE);
        } else {
            showCameras();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_show_hide:
                if (thresholdLayout.getVisibility() == View.VISIBLE) {
                    thresholdLayout.setVisibility(View.GONE);
                } else {
                    thresholdLayout.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.option_reset:
                resetBestPosition();
                break;

            case R.id.show_centroid:
                Toast.makeText(this, "not implemented yet", Toast.LENGTH_SHORT).show();
                break;
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        int itemTextID = (thresholdLayout.getVisibility() == View.VISIBLE) ?
                (R.string.option_hide_thresh) :
                (R.string.option_show_thresh);

        menu.getItem(0).setTitle(itemTextID);

        return true;
    }

    private View.OnClickListener pictureOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showHideViews(true); // remove fab and show after picture menu

            frontCameraActivity.takePicture();
        }
    };

    private void showHideViews(boolean condition) {

        takePicFab.setVisibility(condition ? View.INVISIBLE : View.VISIBLE); // remove FAB
        afterPicMenu.setVisibility(!condition ? View.INVISIBLE : View.VISIBLE); // show after picture menu
        arrowGrid.setVisibility(condition ? View.INVISIBLE : View.VISIBLE); // remove all arrows if there is any
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

    public void resetBestPosition() {
        faceRect.setVisibility(View.INVISIBLE);
        frontCameraActivity.foundCenter = false;
    }

    View.OnClickListener shareButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(MainActivity.this, "share", Toast.LENGTH_SHORT).show();
        }
    };

    View.OnClickListener saveButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(MainActivity.this, "Saved to storage", Toast.LENGTH_SHORT).show();
        }
    };

    View.OnClickListener deleteButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showHideViews(false); // back to analyse mode
            frontCameraActivity.discardPicture();
            resetBestPosition();
        }
    };

    @Override
    public void onBackPressed() {
        if (backPressed) {
            super.onBackPressed();
        } else {
            backPressed = true;
            Toast.makeText(this, "Press again to exit", Toast.LENGTH_LONG).show();
        }
    }


}


