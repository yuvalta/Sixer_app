package com.example.sixer;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;
import android.view.View;

import com.example.sixer.Activity.MainActivity;

import static com.example.sixer.CentroidCalculate.*;

public class FrameAnalyzer {
    private static final String TAG = "UV";
    private static final int NORMALIZE_FACTOR = 1000;

    private MainActivity _context;

    private double oneThirdFaceRectDimWidth;
    private double oneThirdFaceRectDimHeight;

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
                faceGridArray[index++] = Bitmap.createBitmap(picture, i * (int) oneThirdFaceRectDimWidth, j * (int) oneThirdFaceRectDimHeight, (int) oneThirdFaceRectDimWidth, (int) oneThirdFaceRectDimHeight);
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

    /**
     * calculate each cell weight according to the pixel color
     * black == 0, gray == 5, white == 10
     **/
    private int calcCellWeight(Bitmap cell) {
        int[] pixelArray = new int[cell.getWidth() * cell.getHeight()];
        cell.getPixels(pixelArray, 0, cell.getWidth(), 0, 0, cell.getWidth() - 1, cell.getHeight() - 1);

        int sum = 0;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            sum = (IntStream.of(convertToGrayScale(pixelArray)).sum());
//        } else {
        for (int pixel : pixelArray) {
            sum += convertToGrayScale(pixel);
        }
        return sum / NORMALIZE_FACTOR; // normalize by factor because values are too high and it should represent points
    }

    private int convertToGrayScale(int pixelValue) {

        if (pixelValue == Color.BLACK) {
            return 0;
        } else if (pixelValue == Color.GRAY) {
            return 5;
        } else {
            return 10;
        }
    }

    public void setFaceRectDimWidth(double faceRectDimWidth) {
        this.oneThirdFaceRectDimWidth = faceRectDimWidth / 3;
    }

    public void setFaceRectDimHeight(double faceRectDimHeight) {
        this.oneThirdFaceRectDimHeight = faceRectDimHeight / 3;
    }

    public boolean analyze(Bitmap thresholdCropOrDefault) {

        int[] weightsArray = calcWeightsOfFaceGrid(splitBitmap(thresholdCropOrDefault));

        for (DIRECTIONS directions : DIRECTIONS.values()) { // update the values of the square
            centroidCalculate.updatePointValue(directions, weightsArray[directions.ordinal()]);
        }

        Point centerPoint = centroidCalculate.findCenterPoint();
        Log.i("analyze", centerPoint.x + ",," + centerPoint.y);

        DIRECTIONS correctionArrow = centroidCalculate.findCorrectionArrow(centerPoint);

        _context.upArrow.setVisibility(View.INVISIBLE);
        _context.downArrow.setVisibility(View.INVISIBLE);
        _context.rightArrow.setVisibility(View.INVISIBLE);
        _context.leftArrow.setVisibility(View.INVISIBLE);
        _context.faceRect.setVisibility(View.INVISIBLE);

        switch (correctionArrow) {

            case TOP_LEFT:
                _context.rightArrow.setVisibility(View.VISIBLE);
                _context.downArrow.setVisibility(View.VISIBLE);
                break;
            case TOP:
                _context.downArrow.setVisibility(View.VISIBLE);
                break;
            case TOP_RIGHT:
                _context.leftArrow.setVisibility(View.VISIBLE);
                _context.downArrow.setVisibility(View.VISIBLE);
                break;
            case LEFT:
                _context.rightArrow.setVisibility(View.VISIBLE);
                break;
            case CENTER:
                Log.i("UV", "center!");
                if (centerPoint.equals(0, 0)) {
                    return false;
                }
                _context.faceRect.setVisibility(View.VISIBLE);
                return true;
            case RIGHT:
                _context.leftArrow.setVisibility(View.VISIBLE);
                break;
            case BOTTOM_LEFT:
                _context.rightArrow.setVisibility(View.VISIBLE);
                _context.upArrow.setVisibility(View.VISIBLE);
                break;
            case BOTTOM:
                _context.upArrow.setVisibility(View.VISIBLE);
                break;
            case BOTTOM_RIGHT:
                _context.leftArrow.setVisibility(View.VISIBLE);
                _context.upArrow.setVisibility(View.VISIBLE);
                break;
        }

        return false;
    }
}
