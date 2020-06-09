package com.example.sixer.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.sixer.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.io.FileInputStream;

public class PicturePreviewActivity extends AppCompatActivity {

    ImageView picturePreview;
    ImageButton shareButton;
    ImageButton deleteButton;
    ImageButton saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_preview);

        picturePreview = findViewById(R.id.picture_preview);

        shareButton = findViewById(R.id.share_button);
        deleteButton = findViewById(R.id.delete_button);
        saveButton = findViewById(R.id.save_button);

        shareButton.setOnClickListener(shareButtonClickListener);
        deleteButton.setOnClickListener(saveButtonClickListener);
        saveButton.setOnClickListener(deleteButtonClickListener);

        Bitmap previewBitmap = getBitmapFromIntent();

        picturePreview.setImageBitmap(previewBitmap);

    }

    View.OnClickListener shareButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(PicturePreviewActivity.this, "share", Toast.LENGTH_SHORT).show();
        }
    };

    View.OnClickListener saveButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(PicturePreviewActivity.this, "save", Toast.LENGTH_SHORT).show();
        }
    };

    View.OnClickListener deleteButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(PicturePreviewActivity.this, "delete", Toast.LENGTH_SHORT).show();
        }
    };

    private Bitmap getBitmapFromIntent() {
        Bitmap bmp = null;
        String filename = getIntent().getStringExtra("image");
        try {
            FileInputStream is = this.openFileInput(filename);
            bmp = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bmp;
    }

}
