package com.example.sixer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import com.example.sixer.View.MainActivity;

import java.util.stream.IntStream;

public class FrameAnalyzer {

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

    public int[] CalcWeightsOfFaceGrid(Bitmap[] faceGridArray) {
        for (int i = 0; i < faceGridArray.length; i++) {
            faceGridWeights[i] = CalcCellWeight(faceGridArray[i]);
        }

        return faceGridWeights;
    }

    private int CalcCellWeight(Bitmap cell) {
        int pixelArray[] = new int[cell.getWidth() * cell.getHeight()];
        cell.getPixels(pixelArray, 0, cell.getWidth(), 0, 0, cell.getWidth() - 1, cell.getHeight() - 1);

        return IntStream.of(pixelArray).sum();
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

    public void Analyze(Bitmap thresholdCropOrDefault) {

        int[] weightsArray = CalcWeightsOfFaceGrid(splitBitmap(thresholdCropOrDefault));

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
