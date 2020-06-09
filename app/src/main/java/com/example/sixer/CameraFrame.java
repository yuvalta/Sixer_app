package com.example.sixer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;

import com.example.sixer.Activity.MainActivity;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class CameraFrame {

    private static final int FRAME_OPTIMIZER = 10;
    private static final int MAX_THRESHOLD = 150;
    private static final int MIN_THRESHOLD = 90;
    public static String TAG = "UV";
    public static int THRESHOLD = 100;

    private Context _context;

    private double adaptiveThreshold;
    private int width;
    private int height;
    private int sizeOfCroppedFrame;
    private int framesCounter = 2;
    private int lastThresholdValue = 0;

    Bitmap faceCrop;
    Bitmap fullFrame;

    Camera _camera;

    Point startPoint = new Point(0, 0);

    FrameAnalyzer frameAnalyzer;

    public CameraFrame(MainActivity context) {
        _context = context;
    }

    public Bitmap createBitmapFromFrame(byte[] data, Camera camera) {
        Camera.Parameters parameters = camera.getParameters();

        width = parameters.getPreviewSize().width;
        height = parameters.getPreviewSize().height;

        YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(), width, height, null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuv.compressToJpeg(new Rect(0, 0, width, height), 10, out);

        byte[] bytes = out.toByteArray();

        return fullFrame = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public boolean validateOverflowFrame(Point startPoint) {
        return (startPoint.x < fullFrame.getWidth() - 1 && startPoint.y < fullFrame.getHeight() - 1 && startPoint.x > 0 && startPoint.y > 0);
    }

    public Bitmap Threshold() throws ThresholdException {
        // get all image pixels and iterate on it locally
        int pixelValue;
        int pixThresh;
        int faceCropPixelsArray[] = new int[sizeOfCroppedFrame];

        faceCrop.getPixels(faceCropPixelsArray, 0, faceCrop.getWidth(), 0, 0, faceCrop.getWidth() - 1, faceCrop.getHeight() - 1);

        adaptiveThreshold = Math.floor(calcAdaptiveThreshold(faceCropPixelsArray)); // create a buffer for saving last 10 or 30 last values. then average

        for (int i = 0; i < sizeOfCroppedFrame - 1; i++) {
            pixelValue = faceCropPixelsArray[i];

            int grayLevel = convertToGrayScale(pixelValue);

            if (grayLevel <= adaptiveThreshold) {
                pixThresh = Color.BLACK;
            } else if (grayLevel > adaptiveThreshold && grayLevel < adaptiveThreshold + adaptiveThreshold / 5) {
                pixThresh = Color.GRAY;
            } else {
                pixThresh = Color.WHITE;
            }
            faceCropPixelsArray[i] = pixThresh;
        }
        faceCrop.setPixels(faceCropPixelsArray, 0, faceCrop.getWidth(), 0, 0, faceCrop.getWidth(), faceCrop.getHeight());

        return faceCrop;
    }

    private int convertToGrayScale(int pixelValue) {
        int R = (pixelValue & 0xff0000) >> 16;
        int G = (pixelValue & 0x00ff00) >> 8;
        int B = (pixelValue & 0x0000ff) >> 0;

        return (R + G + B) / 3;
    }

    private double calcAdaptiveThreshold(int[] faceCropPixelsArray) {
        double threshValTemp = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            threshValTemp = (Arrays.stream(faceCropPixelsArray).average()).getAsDouble();
        } else {
            for (int pixel : faceCropPixelsArray) {
                threshValTemp += pixel;

            }
            threshValTemp /= faceCropPixelsArray.length;
        }

        return quantizeThreshold(convertToGrayScale((int) threshValTemp));
    }

    private double quantizeThreshold(int value) {
        if (framesCounter++ >= FRAME_OPTIMIZER) {
            if (value > MIN_THRESHOLD && value < MAX_THRESHOLD) {
                framesCounter = 0;
                lastThresholdValue = (value / 10) * 10;
                return lastThresholdValue;
            }
        }
        return lastThresholdValue;
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    public Bitmap defaultFrame() {
        return fullFrame;
    }

    public void setStartPoint(Point startPoint) {
        this.startPoint = startPoint;
    }


    public void cropFace(int faceRectDimWidth, int faceRectDimHeight) {
        faceCrop = Bitmap.createBitmap(fullFrame, startPoint.x, startPoint.y, faceRectDimWidth, faceRectDimHeight);
    }

    public void setSizeOfCroppedFrame(int sizeOfCroppedFrame) {
        this.sizeOfCroppedFrame = sizeOfCroppedFrame;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setFaceCrop(Bitmap faceCrop) {
        this.faceCrop = faceCrop;
    }

    public double getAdaptiveThreshold() {
        return adaptiveThreshold;
    }

    private class ThresholdException extends Exception {

    }
}
