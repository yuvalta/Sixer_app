package com.example.sixer.ViewModel;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.sixer.View.MainActivity;

import java.io.IOException;

public class BackCamera extends SurfaceView implements SurfaceHolder.Callback {

    public static String TAG = "BackCamera";

    public Camera.Size _size;
    Camera _camera;
    SurfaceHolder surfaceHolder;
    Context _context;

    public BackCamera(MainActivity context, android.hardware.Camera backCamera) {
        super(context);

        _context = context;
        _camera = backCamera;
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
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
