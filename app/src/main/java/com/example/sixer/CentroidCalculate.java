package com.example.sixer;

import android.graphics.Point;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;

public class CentroidCalculate {

    private static final int AXIS_LENGTH = 200; // axis from -100 to 100

    public static enum DIRECTIONS {
        TOP_LEFT, TOP, TOP_RIGHT, LEFT, CENTER, RIGHT, BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT
    }

    protected HashMap<DIRECTIONS ,Point> pointHashMap;

    public CentroidCalculate() {
        pointHashMap = new HashMap<DIRECTIONS ,Point>();

        pointHashMap.put(DIRECTIONS.RIGHT, new Point(0,0));
        pointHashMap.put(DIRECTIONS.LEFT, new Point(0,0));
        pointHashMap.put(DIRECTIONS.TOP, new Point(0,0));
        pointHashMap.put(DIRECTIONS.BOTTOM, new Point(0,0));
    }

    public void updatePointValue(DIRECTIONS type, int cellValue) {

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
        }

        pointHashMap.put(type, addingPoint); // update point value
    }

    private int calculatePoint(int cellValue) {
        return cellValue % AXIS_LENGTH;
    }




    //this class inherit from Point class and acts the same, but also has a "type" field
    private class PointType extends Point {
        DIRECTIONS type;

        public PointType(DIRECTIONS type) {
            this.type = type;
        }
    }
}
