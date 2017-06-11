package com.games.malcolm.graphgame;

import android.graphics.PointF;
import android.util.Log;

import java.util.Comparator;

/**
 * Created by Thomas on 5/24/17.
 */

public class Point extends PointF {

    private String TAG = "Point";

    Point(float x, float y) {
        super(x, y);
    }
//    public void minus(Point p) {
//        x -= p.x;
//        y -= p.y;
//    }
    private void plus(Point p) {
        x += p.x;
        y += p.y;
    }
    public void scale(float f) {
        x *= f;
        y *= f;
    }

    public static Point average(Point... ps) {
        Point avg = new Point(0, 0);
        for (Point p : ps) {
            avg.plus(p);
        }
        avg.scale(1.0F/ps.length);
        return avg;
    }

    public Point copyMinus(Point p) {
        return new Point(x - p.x, y - p.y);
    }

    public boolean isInCircle(Point center, float radius) {
        Point p = copyMinus(center);
        return p.dot(p) < radius * radius; // dot(this) = length^2
    }

    public float dot(Point p) {
        return x * p.x + y * p.y;
    }
    public float cross(Point p) {
        return x * p.y - y * p.x;
    }

    public static boolean segmentsIntersect(Point s1p1, Point s1p2, Point s2p1, Point s2p2) {
        Point v12 = s1p2.copyMinus(s1p1); // s1 moved to origin
        float cross1 = v12.cross(s2p1.copyMinus(s1p1));
        float cross2 = v12.cross(s2p2.copyMinus(s1p1));
        if (cross1 * cross2 >= 0) {
            return false;
        }
        Point v22 = s2p2.copyMinus(s2p1); // s2 moved to origin
        cross1 = v22.cross(s1p1.copyMinus(s2p1));
        cross2 = v22.cross(s1p2.copyMinus(s2p1));
        return cross1 * cross2 < 0;
    }

    public static Point lineIntersection(Point s1p1, Point s1p2, Point s2p1, Point s2p2) {
//        if (!Point.segmentsIntersect(s1p1, s1p2, s2p1, s2p2)) {
//            return null;
//        }
        float a1 = s1p2.y - s1p1.y;
        float b1 = s1p1.x - s1p2.x;
        float c1 = a1*s1p1.x + b1*s1p1.y;
        float a2 = s2p2.y - s2p1.y;
        float b2 = s2p1.x - s2p2.x;
        float c2 = a2*s2p1.x + b2*s2p1.y;
        float det = a1 * b2 - a2 * b1;
        if (det == 0) {
            return null; // parallel lines
        }
        return new Point(
                (b2 * c1 - b1 * c2) / det,
                (a1 * c2 - a2 * c1) / det
        );
    }

    public float angleBetweenPoints(Point p1, Point p2) {
//        Log.i(TAG, "ANGLE: this: " + this.toString());
//        Log.i(TAG, "ANGLE: p1: " + p1.toString());
//        Log.i(TAG, "ANGLE: p2: " + p2.toString());
        Point v1 = p1.copyMinus(this);
        Point v2 = p2.copyMinus(this);
//        Log.i(TAG, "ANGLE: v1: " + v1.toString());
//        Log.i(TAG, "ANGLE: v2: " + v2.toString());
        float cos = v1.dot(v2) / (v1.length() * v2.length());
//        Log.i(TAG, "ANGLE: cos: " + cos);
//        double a = Math.acos(cos) * 180 / Math.PI;
        float angle = (float) (Math.acos(cos) * 180 / Math.PI);
//        Log.i(TAG, "ANGLE: angle: " + angle);
        return v1.cross(v2) < 0 ? 360 - angle : angle;
    }

    @Override
    public String toString() {
        return "(x: " + x + ", y: " + y + ")";
    }
}
