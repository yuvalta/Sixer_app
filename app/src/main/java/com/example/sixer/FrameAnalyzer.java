package com.example.sixer;

import android.app.Activity;
import android.graphics.Bitmap;
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
        int[] pixelArray = new int[cell.getWidth() * cell.getHeight()];
        cell.getPixels(pixelArray, 0, cell.getWidth(), 0, 0, cell.getWidth() - 1, cell.getHeight() - 1);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return convertToGrayScale(IntStream.of(pixelArray).sum());
        } else {
            int sum = 0;
            for (int pixel : pixelArray) {
                sum += pixel;

            }
            return convertToGrayScale(sum);
        }
    }

    private int convertToGrayScale(int pixelValue) {
        int R = (pixelValue & 0xff0000) >> 16;
        int G = (pixelValue & 0x00ff00) >> 8;
        int B = (pixelValue & 0x0000ff);

        return (R + G + B) / 3;
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

        for (DIRECTIONS directions : DIRECTIONS.values()) {
            centroidCalculate.updatePointValue(directions, weightsArray[directions.ordinal()]);
        }

//        centroidCalculate.updatePointValue(DIRECTIONS.LEFT, weightsArray[DIRECTIONS.LEFT.ordinal()]);
//        centroidCalculate.updatePointValue(DIRECTIONS.TOP, weightsArray[DIRECTIONS.TOP.ordinal()]);
//        centroidCalculate.updatePointValue(DIRECTIONS.BOTTOM, weightsArray[DIRECTIONS.BOTTOM.ordinal()]);
//        centroidCalculate.updatePointValue(DIRECTIONS.RIGHT, weightsArray[DIRECTIONS.RIGHT.ordinal()]);

        ((Activity) (_context)).runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });


    }
}
