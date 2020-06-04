package com.example.sixer;

import android.graphics.Point;
import android.util.Log;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;

import static com.example.sixer.CentroidCalculate.DIRECTIONS.BOTTOM;
import static com.example.sixer.CentroidCalculate.DIRECTIONS.BOTTOM_LEFT;
import static com.example.sixer.CentroidCalculate.DIRECTIONS.BOTTOM_RIGHT;
import static com.example.sixer.CentroidCalculate.DIRECTIONS.CENTER;
import static com.example.sixer.CentroidCalculate.DIRECTIONS.LEFT;
import static com.example.sixer.CentroidCalculate.DIRECTIONS.RIGHT;
import static com.example.sixer.CentroidCalculate.DIRECTIONS.TOP;
import static com.example.sixer.CentroidCalculate.DIRECTIONS.TOP_LEFT;
import static com.example.sixer.CentroidCalculate.DIRECTIONS.TOP_RIGHT;

public class CentroidCalculate {

    private static final int AXIS_LENGTH = 200;
    private static final int GRAPH_THRESHOLD = 5;


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

    public DIRECTIONS findCorrectionArrow(Point centerPoint) {

        Log.i("findCorrectionArrow", centerPoint.x + "," + centerPoint.y);
        if (centerPoint.x > GRAPH_THRESHOLD && centerPoint.y > GRAPH_THRESHOLD) {
            return TOP_RIGHT;
        } else if (centerPoint.x > GRAPH_THRESHOLD && centerPoint.y < -GRAPH_THRESHOLD) {
            return BOTTOM_RIGHT;
        } else if (centerPoint.x < -GRAPH_THRESHOLD && centerPoint.y < -GRAPH_THRESHOLD) {
            return BOTTOM_LEFT;
        } else if (centerPoint.x < -GRAPH_THRESHOLD && centerPoint.y > GRAPH_THRESHOLD) {
            return TOP_LEFT;
        } else if (centerPoint.x > -GRAPH_THRESHOLD && centerPoint.x < GRAPH_THRESHOLD && centerPoint.y > GRAPH_THRESHOLD) {
            return TOP;
        } else if (centerPoint.x > -GRAPH_THRESHOLD && centerPoint.x < GRAPH_THRESHOLD && centerPoint.y < -GRAPH_THRESHOLD) {
            return BOTTOM;
        } else if (centerPoint.y > -GRAPH_THRESHOLD && centerPoint.y < GRAPH_THRESHOLD && centerPoint.x > GRAPH_THRESHOLD) {
            return RIGHT;
        } else if (centerPoint.y > -GRAPH_THRESHOLD && centerPoint.y < GRAPH_THRESHOLD && centerPoint.x < -GRAPH_THRESHOLD) {
            return LEFT;
        }

        return CENTER;
    }

    public Point findCenterPoint() {
        return polygon.center();
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
            int calcPoint = (cellValue); // calc only x value for top && down, and calc y for left && right

            switch (type) {
                case BOTTOM:
                    addingPoint.x = 0;
                    addingPoint.y = -(calcPoint) * 2;
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
                    addingPoint.x = 100;
                    addingPoint.y = 100;
                    break;
            }

            pointHashMap.put(type, addingPoint); // update point value
        }

        public Point center() { // this is good, now, TODO: think about a way to go to (0,0), maybe check in which Q we are and fix accordingly
            Point centerPoint = new Point();

            centerPoint.x = (pointHashMap.get(RIGHT).x + pointHashMap.get(LEFT).x) / 2;
            centerPoint.y = (pointHashMap.get(TOP).y + pointHashMap.get(BOTTOM).y) / 2;

            Log.i("UV", centerPoint.x + "," + centerPoint.y);

            return centerPoint;
        }
    }
}
