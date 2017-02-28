package com.games.malcolm.graphgame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashSet;
import java.util.Random;

/**
 * Created by Thomas on 2/27/17.
 *
 * Taken pretty much directly from:
 * http://stackoverflow.com/questions/17826358/drag-and-move-a-circle-drawn-on-canvas
 * Good starting point to interact with our objects.
 */

public class InteractiveCircleView extends View {

    private static final String TAG = "InteractiveCircleView";

    /** Main bitmap */
//    private Bitmap mBitmap = null;

    private Rect mMeasuredRect;

    /** Stores data about single circle */
    private static class CircleArea {
        int radius;
        int centerX;
        int centerY;

        CircleArea(int centerX, int centerY, int radius) {
            this.radius = radius;
            this.centerX = centerX;
            this.centerY = centerY;
        }

        @Override
        public String toString() {
            return "Circle[" + centerX + ", " + centerY + ", " + radius + "]";
        }
    }

    /** Paint to draw circles */
    private Paint mCirclePaint;

    private final Random mRadiusGenerator = new Random();
    // Radius limit in pixels
    private final static int RADIUS_LIMIT = 100;

    private static final int CIRCLES_LIMIT = 3;

    /** All available circles */
    private HashSet<CircleArea> mCircles = new HashSet<>(CIRCLES_LIMIT);
    private SparseArray<CircleArea> mCirclePointer;

    /**
     * Default constructor
     *
     * @param ct {@link android.content.Context}
     */
    public InteractiveCircleView(final Context ct) {
        super(ct);
        init(ct);
        mCirclePointer = new SparseArray<>(CIRCLES_LIMIT);
    }
    public InteractiveCircleView(final Context ct, final AttributeSet attrs) {
        super(ct, attrs);
        init(ct);
        mCirclePointer = new SparseArray<>(CIRCLES_LIMIT);
    }

    public InteractiveCircleView(final Context ct, final AttributeSet attrs, final int defStyle) {
        super(ct, attrs, defStyle);
        init(ct);
        mCirclePointer = new SparseArray<>(CIRCLES_LIMIT);
    }

    private void init(final Context ct) {
        // Generate bitmap used for background
//        mBitmap = BitmapFactory.decodeResource(ct.getResources(), R.drawable.up_image);

        mCirclePaint = new Paint();

        mCirclePaint.setColor(Color.BLUE);
        mCirclePaint.setStrokeWidth(40);
        mCirclePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void onDraw(final Canvas canv) {
        // background bitmap to cover all area
//        canv.drawBitmap(mBitmap, null, mMeasuredRect, null);
//        canv.drawRect((SCREEN_WIDTH / 4) - 120, 0, (SCREEN_WIDTH / 4) + 120, SCREEN_HEIGHT, mRectPaintLeft);
//        canv.drawRect((3 * SCREEN_WIDTH / 4) - 120, 0, (3 * SCREEN_WIDTH / 4) + 120, SCREEN_HEIGHT, mRectPaintRight);

        for (CircleArea circle : mCircles) {
            canv.drawCircle(circle.centerX, circle.centerY, circle.radius, mCirclePaint);
        }
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        boolean handled = false;

        CircleArea touchedCircle;
        int xTouch;
        int yTouch;
        int pointerId;
        int actionIndex = event.getActionIndex();

        // get touch event coordinates and make transparent circle from it
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                // it's the first pointer, so clear all existing pointers data
                clearCirclePointer();

                xTouch = (int) event.getX(0);
                yTouch = (int) event.getY(0);

                // check if we've touched inside some circle
                touchedCircle = obtainTouchedCircle(xTouch, yTouch);
                touchedCircle.centerX = xTouch;
                touchedCircle.centerY = yTouch;
                mCirclePointer.put(event.getPointerId(0), touchedCircle);

                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                Log.w(TAG, "Pointer down");
                // It secondary pointers, so obtain their ids and check circles
                pointerId = event.getPointerId(actionIndex);

                xTouch = (int) event.getX(actionIndex);
                yTouch = (int) event.getY(actionIndex);

                // check if we've touched inside some circle
                touchedCircle = obtainTouchedCircle(xTouch, yTouch);

                mCirclePointer.put(pointerId, touchedCircle);
                touchedCircle.centerX = xTouch;
                touchedCircle.centerY = yTouch;
                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_MOVE:
                final int pointerCount = event.getPointerCount();

                Log.w(TAG, "Move");

                for (actionIndex = 0; actionIndex < pointerCount; actionIndex++) {
                    // Some pointer has moved, search it by pointer id
                    pointerId = event.getPointerId(actionIndex);

                    xTouch = (int) event.getX(actionIndex);
                    yTouch = (int) event.getY(actionIndex);

                    touchedCircle = mCirclePointer.get(pointerId);

                    if (null != touchedCircle) {
                        touchedCircle.centerX = xTouch;
                        touchedCircle.centerY = yTouch;
                    }
                }
                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_UP:
                clearCirclePointer();
                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                // not general pointer was up
                pointerId = event.getPointerId(actionIndex);

                mCirclePointer.remove(pointerId);
                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_CANCEL:
                handled = true;
                break;

            default:
                // do nothing
                break;
        }
        return super.onTouchEvent(event) || handled;
    }

    /**
     * Clears all CircleArea - pointer id relations
     */
    private void clearCirclePointer() {
        Log.w(TAG, "clearCirclePointer");
        mCirclePointer.clear();
    }
    /**
     * Search and creates new (if needed) circle based on touch area
     *
     * @param xTouch int x of touch
     * @param yTouch int y of touch
     *
     * @return obtained {@link CircleArea}
     */
    private CircleArea obtainTouchedCircle(final int xTouch, final int yTouch) {
        CircleArea touchedCircle = getTouchedCircle(xTouch, yTouch);

        if (null == touchedCircle) {
            touchedCircle = new CircleArea(xTouch, yTouch, mRadiusGenerator.nextInt(RADIUS_LIMIT) + RADIUS_LIMIT);

            if (mCircles.size() == CIRCLES_LIMIT) {
                Log.w(TAG, "Clear all circles, size is " + mCircles.size());
                // remove first circle
                mCircles.clear();
            }

            Log.w(TAG, "Added circle " + touchedCircle);
            mCircles.add(touchedCircle);
        }

        return touchedCircle;
    }

    /**
     * Determines touched circle
     *
     * @param xTouch int x touch coordinate
     * @param yTouch int y touch coordinate
     *
     * @return {@link CircleArea} touched circle or null if no circle has been touched
     */
    private CircleArea getTouchedCircle(final int xTouch, final int yTouch) {
        CircleArea touched = null;

        for (CircleArea circle : mCircles) {
            if ((circle.centerX - xTouch) * (circle.centerX - xTouch) + (circle.centerY - yTouch) * (circle.centerY - yTouch) <= circle.radius * circle.radius) {
                touched = circle;
                break;
            }
        }

        return touched;
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mMeasuredRect = new Rect(0, 0, getMeasuredWidth(), getMeasuredHeight());
    }
}
