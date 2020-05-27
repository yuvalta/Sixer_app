package com.example.sixer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.view.View;

import com.example.sixer.View.MainActivity;

import java.util.stream.IntStream;

public class FrameAnalyzer {
    private static final String TAG = "UV";

    private MainActivity _context;

    private Bitmap imageToAnalyze;

    private int faceRectDimWidth;
    private int faceRectDimHeight;
    private int oneThirdFaceRectDimWidth;
    private int oneThirdFaceRectDimHeight;

    Bitmap[] faceGridArray = new Bitmap[9];
    int[] faceGridWeights = new int[9];


    public FrameAnalyzer(MainActivity context) {
        _context = context;
    }

    public Bitmap[] splitBitmap(Bitmap picture) { // split image according to face grid (0-8)
        int index = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 2; j >= 0; j--) {
                faceGridArray[index++] = Bitmap.createBitmap(picture, j * oneThirdFaceRectDimWidth, i * oneThirdFaceRectDimHeight, oneThirdFaceRectDimWidth, oneThirdFaceRectDimHeight);
            }
        }

        return faceGridArray;
    }

    public int[] calcWeightsOfFaceGrid(Bitmap[] faceGridArray) {
        for (int i = 0; i < faceGridArray.length; i++) {
            faceGridWeights[i] = calcCellWeight(faceGridArray[i]);
        }

        return faceGridWeights;
    }

    private int calcCellWeight(Bitmap cell) {
        int pixelArray[] = new int[cell.getWidth() * cell.getHeight()];
        cell.getPixels(pixelArray, 0, cell.getWidth(), 0, 0, cell.getWidth() - 1, cell.getHeight() - 1);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return convertToGrayScale(IntStream.of(pixelArray).sum());
        }
        else {
            int sum = 0;
            for (int pixel: pixelArray) {
                sum+=pixel;

            }
            return convertToGrayScale(sum);
        }
    }

    private int convertToGrayScale(int pixelValue) {
        int R = (pixelValue & 0xff0000) >> 16;
        int G = (pixelValue & 0x00ff00) >> 8;
        int B = (pixelValue & 0x0000ff) >> 0;

        return (R + G + B) / 3;
    }

    public Bitmap getImageToAnalyze() {
        return imageToAnalyze;
    }

    public void setImageToAnalyze(Bitmap imageToAnalyze) {
        this.imageToAnalyze = imageToAnalyze;
    }

    public void setFaceRectDimWidth(int faceRectDimWidth) {
        this.faceRectDimWidth = faceRectDimWidth;
        this.oneThirdFaceRectDimWidth = faceRectDimWidth / 3;
    }

    public void setFaceRectDimHeight(int faceRectDimHeight) {
        this.faceRectDimHeight = faceRectDimHeight;
        this.oneThirdFaceRectDimHeight = faceRectDimHeight / 3;
    }

    public void analyze(Bitmap thresholdCropOrDefault) {

        int[] weightsArray = calcWeightsOfFaceGrid(splitBitmap(thresholdCropOrDefault));

        for (int i = 0; i < weightsArray.length; i++) {
            Log.d(TAG, String.valueOf(i + " " + weightsArray[i]));
        }
        if (weightsArray[1] > weightsArray[7]) {

            ((Activity) (_context)).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    _context.leftArrow.setVisibility(View.INVISIBLE);
                    _context.rightArrow.setVisibility(View.VISIBLE);
                }
            });

        } else {
            ((Activity) (_context)).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    _context.leftArrow.setVisibility(View.VISIBLE);
                    _context.rightArrow.setVisibility(View.INVISIBLE);
                }
            });
        }

        if (weightsArray[3] > weightsArray[5]) {

            ((Activity) (_context)).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    _context.upArrow.setVisibility(View.INVISIBLE);
                    _context.downArrow.setVisibility(View.VISIBLE);
                }
            });

        } else {
            ((Activity) (_context)).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    _context.upArrow.setVisibility(View.VISIBLE);
                    _context.downArrow.setVisibility(View.INVISIBLE);
                }
            });
        }

    }
}
