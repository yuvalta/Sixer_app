package com.example.sixer;

import android.graphics.Point;
import android.util.Log;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;

import static com.example.sixer.CentroidCalculate.DIRECTIONS.BOTTOM;
import static com.example.sixer.CentroidCalculate.DIRECTIONS.LEFT;
import static com.example.sixer.CentroidCalculate.DIRECTIONS.RIGHT;
import static com.example.sixer.CentroidCalculate.DIRECTIONS.TOP;

public class CentroidCalculate {

    private static final int AXIS_LENGTH = 200; // axis from -100 to 100

    public static enum DIRECTIONS {
        TOP_LEFT, TOP, TOP_RIGHT, LEFT, CENTER, RIGHT, BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT
    }

    protected Polygon polygon;

    public CentroidCalculate() {
        polygon = new Polygon(); // currently support only 4 corners
    }

    public void updatePointValue(DIRECTIONS type, int cellValue) {
        polygon.updateCorners(type, cellValue);
    }

    public Point findCenterPoint() {
        return polygon.center();
    }

    private int calculatePoint(int cellValue) {
        return cellValue % AXIS_LENGTH;
    }

    private class Polygon {

        HashMap<DIRECTIONS, Point> pointHashMap;

        Point centroid;

        public Polygon() {
            pointHashMap = new HashMap<>();

            pointHashMap.put(RIGHT, new Point(0, 0));
            pointHashMap.put(LEFT, new Point(0, 0));
            pointHashMap.put(TOP, new Point(0, 0));
            pointHashMap.put(BOTTOM, new Point(0, 0));
        }

        public void updateCorners(DIRECTIONS type, int cellValue) {
            Point addingPoint = new Point();
            int calcPoint = calculatePoint(cellValue); // calc only x value for top && down, and calc y for left && right

            switch (type) {
                case BOTTOM:
                    addingPoint.x = 0;
                    addingPoint.y = -(calcPoint);
                    break;
                case LEFT:
                    addingPoint.x = -(calcPoint);
                    addingPoint.y = 0;
                    break;
                case RIGHT:
                    addingPoint.x = calcPoint;
                    addingPoint.y = 0;
                    break;
                case TOP:
                    addingPoint.x = 0;
                    addingPoint.y = calcPoint;
                    break;
                default:
                    addingPoint.x = 0;
                    addingPoint.y = 0;
                    break;
            }

            pointHashMap.put(type, addingPoint); // update point value
        }

        public Point center() {
            Point centerPoint = new Point();

            centerPoint.x = (int) (Math.sqrt(Math.pow(pointHashMap.get(LEFT).x - pointHashMap.get(RIGHT).x, 2) + Math.pow(pointHashMap.get(LEFT).y - pointHashMap.get(RIGHT).y, 2)) / 2);
            centerPoint.y = (int) (Math.sqrt(Math.pow(pointHashMap.get(TOP).x - pointHashMap.get(BOTTOM).x, 2) + Math.pow(pointHashMap.get(TOP).y - pointHashMap.get(BOTTOM).y, 2)) / 2);

            Log.i("UV", centerPoint.x + "," + centerPoint.y);

            return centerPoint;
        }
    }
}
