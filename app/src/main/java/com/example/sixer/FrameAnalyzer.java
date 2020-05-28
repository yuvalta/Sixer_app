package com.example.sixer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.view.View;

import com.example.sixer.View.MainActivity;

import java.util.stream.IntStream;

import static com.example.sixer.CentroidCalculate.*;

public class FrameAnalyzer {
    private static final String TAG = "UV";

    private MainActivity _context;

    private Bitmap imageToAnalyze;

    private int oneThirdFaceRectDimWidth;
    private int oneThirdFaceRectDimHeight;

    CentroidCalculate centroidCalculate;

    Bitmap[] faceGridArray = new Bitmap[9];
    int[] faceGridWeights = new int[9];


    public FrameAnalyzer(MainActivity context) {
        _context = context;
        centroidCalculate = new CentroidCalculate(); // centroid
    }

    public Bitmap[] splitBitmap(Bitmap picture) { // split image according to face grid (0-8)
        int index = 0;
        for (int i = 2; i >= 0; i--) {
            for (int j = 0; j < 3; j++) {
                faceGridArray[index++] = Bitmap.createBitmap(picture, i * oneThirdFaceRectDimWidth, j * oneThirdFaceRectDimHeight, oneThirdFaceRectDimWidth, oneThirdFaceRectDimHeight);
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

    private int calcCellWeight(Bitmap cell) { //TODO: think how to weight each cell
        int[] pixelArray = new int[cell.getWidth() * cell.getHeight()];
        cell.getPixels(pixelArray, 0, cell.getWidth(), 0, 0, cell.getWidth() - 1, cell.getHeight() - 1);


//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            return convertToGrayScale(IntStream.of(pixelArray).sum());
//        } else {
        int sum = 0;
        for (int pixel : pixelArray) {

            sum += convertToGrayScale(pixel);

        }
        return sum / 1000; // TODO: improve!
//        }
    }

    private int convertToGrayScale(int pixelValue) {

        if (pixelValue == Color.BLACK) {
            return 0;
        } else if (pixelValue == Color.GRAY) {
            return 2;
        } else {
            return 5;
        }
    }

    public Bitmap getImageToAnalyze() {
        return imageToAnalyze;
    }

    public void setImageToAnalyze(Bitmap imageToAnalyze) {
        this.imageToAnalyze = imageToAnalyze;
    }

    public void setFaceRectDimWidth(int faceRectDimWidth) {
        this.oneThirdFaceRectDimWidth = faceRectDimWidth / 3;
    }

    public void setFaceRectDimHeight(int faceRectDimHeight) {
        this.oneThirdFaceRectDimHeight = faceRectDimHeight / 3;
    }

    public void analyze(Bitmap thresholdCropOrDefault) {

        int[] weightsArray = calcWeightsOfFaceGrid(splitBitmap(thresholdCropOrDefault));

        for (DIRECTIONS directions : DIRECTIONS.values()) { // update the values of the square
            centroidCalculate.updatePointValue(directions, weightsArray[directions.ordinal()]);
        }

        int centerX = centroidCalculate.findCenterPoint().x;
        int centerY = centroidCalculate.findCenterPoint().y;


        ((Activity) (_context)).runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });


    }
}
