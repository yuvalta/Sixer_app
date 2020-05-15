package com.example.sixer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;

import com.example.sixer.View.MainActivity;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.NavigableMap;
import java.util.OptionalDouble;
import java.util.TreeMap;

public class CameraFrame {
    public static NavigableMap<Integer, Integer> map = new TreeMap<Integer, Integer>();

    public static int THRESHOLD = 100;

    Context _context;

    double adaptiveThreshold;
    int width;
    int height;

    int sizeOfCroppedFrame;

    Bitmap faceCrop;
    Bitmap fullFrame;

    Camera _camera;

    Point startPoint;


    public CameraFrame(MainActivity context) {
        _context = context;

//        initQuantMap();
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

    private void initQuantMap() { // quantize the adaptive threshold for better results
        for (int i = 0; i < 256 ; i++) {
            map.put(i % 10, i % 10);    // i...i+10 => i
        }
    }

    public Bitmap Threshold() {
        // get all image pixels and iterate on it locally
        int pixelValue;
        int pixThresh;
        int faceCropPixelsArray[] = new int[sizeOfCroppedFrame];

        faceCrop.getPixels(faceCropPixelsArray, 0, faceCrop.getWidth(), 0, 0, faceCrop.getWidth() - 1, faceCrop.getHeight() - 1);

        adaptiveThreshold = Math.floor(calcAdaptiveThreshold(faceCropPixelsArray)); // create a buffer for saving last 10 or 30 last values. then average

        for (int i = 0; i < sizeOfCroppedFrame - 1; i++) {
            pixelValue = faceCropPixelsArray[i];

            int R = (pixelValue & 0xff0000) >> 16;
            int G = (pixelValue & 0x00ff00) >> 8;
            int B = (pixelValue & 0x0000ff) >> 0;

            int grayLevel = (R + G + B) / 3;

            if (grayLevel < adaptiveThreshold) {
                pixThresh = Color.BLUE;
            } else {
                pixThresh = Color.WHITE;
            }
            faceCropPixelsArray[i] = pixThresh;
        }
        faceCrop.setPixels(faceCropPixelsArray, 0, faceCrop.getWidth(), 0, 0, faceCrop.getWidth(), faceCrop.getHeight());

        return faceCrop;
    }

    private double calcAdaptiveThreshold(int[] faceCropPixelsArray) {
        double threshValTemp;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // can be more efficient
            threshValTemp = (Arrays.stream(faceCropPixelsArray).average()).getAsDouble();
        } else { // for lower versions then 24
            double sum = 0;
            for (int pixelVal : faceCropPixelsArray) {
                sum += pixelVal;
            }
            threshValTemp = (sum / faceCropPixelsArray.length);
        }

        int R = ((int) threshValTemp & 0xff0000) >> 16;
        int G = ((int) threshValTemp & 0x00ff00) >> 8;
        int B = ((int) threshValTemp & 0x0000ff) >> 0;

        return /*quantizeThreshold*/((R + G + B) / 3);
    }

    private double quantizeThreshold(int value) {


        return map.floorEntry(value).getValue();
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

    public double getAdaptiveThreshold() {
        return adaptiveThreshold;
    }
}
