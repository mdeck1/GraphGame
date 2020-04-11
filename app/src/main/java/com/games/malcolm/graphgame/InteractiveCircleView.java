package com.games.malcolm.graphgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Thomas on 2/27/17.
 */

public class InteractiveCircleView extends View {

    private static final String TAG = "InteractiveCircleView";

    // TODO: Move these things into the graph class. It should handle how things move around.
    private static final int CIRCLES_LIMIT = 6;
    private SparseIntArray mVertexPointer;
    private MODE mMode = MODE.CREATE_V;
    private int mSelectedVertex = -1;
    private int debugStep = 0;

    private enum MODE {
        SELECT_V,
        CREATE_V,
        CREATE_E,
        DELETE_E,
        DELETE_V,
        MOVE_V,
        DEBUG,
    }

    public void toggleMode() {
        mSelectedVertex = -1;
        int ind = (mMode.ordinal() + 1) % MODE.values().length;
        mMode = MODE.values()[ind];
        Log.i(TAG, "Mode: " + String.valueOf(mMode));
        invalidate();
    }

    public String getMode() {
        return mMode.toString();
    }

    private Graph mGraph;

    /**
     * Default constructor
     *
     * @param ct {@link android.content.Context}
     */
    public InteractiveCircleView(final Context ct) {
        super(ct);
        mGraph = new Graph();
        Log.i(TAG, "Mode: " + String.valueOf(mMode));
        mVertexPointer = new SparseIntArray(CIRCLES_LIMIT);
    }
    public InteractiveCircleView(final Context ct, final AttributeSet attrs) {
        super(ct, attrs);
        mGraph = new Graph();
        Log.i(TAG, "Mode: " + String.valueOf(mMode));
        mVertexPointer = new SparseIntArray(CIRCLES_LIMIT);
    }

    public InteractiveCircleView(final Context ct, final AttributeSet attrs, final int defStyle) {
        super(ct, attrs, defStyle);
        mGraph = new Graph();
        Log.i(TAG, "Mode: " + String.valueOf(mMode));
        mVertexPointer = new SparseIntArray(CIRCLES_LIMIT);
    }

    @Override
    public void onDraw(final Canvas canv) {
        mGraph.draw(canv, mSelectedVertex);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        boolean handled = false;

        int xTouch;
        int yTouch;
        int pointerId;
        int actionIndex = event.getActionIndex();
        int touchedVertex;

        // get touch event coordinates and make transparent circle from it
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                // it's the first pointer, so clear all existing pointers data
                clearCirclePointer();

                xTouch = (int) event.getX(0);
                yTouch = (int) event.getY(0);
                touchedVertex = mGraph.pointOnAnyVertex(new Point(xTouch, yTouch));
                switch (mMode) {
                    case SELECT_V:
                        mSelectedVertex =
                                ((touchedVertex > -1) && (touchedVertex != mSelectedVertex)) ?
                                touchedVertex : -1;
                        break;
                    case CREATE_V:
                        if (touchedVertex <= -1) {
                            mGraph.addGraphVertex(xTouch, yTouch);
                        }
                        break;
                    case CREATE_E:
                        if (touchedVertex == -1) {
                            mSelectedVertex = -1;
                            break;
                        }
                        if ((mSelectedVertex > -1) && (touchedVertex != mSelectedVertex)) {
                            mGraph.addGraphEdge(mSelectedVertex, touchedVertex);
                            mSelectedVertex = touchedVertex;
                        } else {
                            mSelectedVertex =
                                    ((touchedVertex > -1) && (touchedVertex != mSelectedVertex)) ?
                                            touchedVertex : -1;
                        }
                        break;
                    case DELETE_E:
                        if (touchedVertex == -1) {
                            mSelectedVertex = -1;
                            break;
                        }
                        if ((mSelectedVertex > -1) && (touchedVertex != mSelectedVertex)) {
                            mGraph.deleteGraphEdge(mSelectedVertex, touchedVertex);
                            mSelectedVertex = -1;
                        } else {
                            mSelectedVertex =
                                    ((touchedVertex > -1) && (touchedVertex != mSelectedVertex)) ?
                                            touchedVertex : -1;
                        }
                        break;
                    case DELETE_V:
                        if (touchedVertex == -1) {
                            mSelectedVertex = -1;
                            break;
                        }
                        if ((mSelectedVertex > -1) && (touchedVertex == mSelectedVertex)) {
                            mGraph.deleteGraphVertex(mSelectedVertex);
                            mSelectedVertex = -1;
                        } else {
                            mSelectedVertex =
                                    ((touchedVertex > -1) && (touchedVertex != mSelectedVertex)) ?
                                            touchedVertex : -1;
                        }
                        break;
                    case MOVE_V:
                        pointerId = event.getPointerId(actionIndex);
                        mSelectedVertex =
                                ((touchedVertex > -1) && (touchedVertex != mSelectedVertex)) ?
                                        touchedVertex : -1;
                        if (touchedVertex > -1) {
                            Log.i(TAG, "move pointer set");
                            mVertexPointer.put(event.getPointerId(pointerId), touchedVertex);
                        }
                        invalidate();
                        handled = true;
                        break;
                    case DEBUG:
                        Log.i(TAG, "debug step: " + debugStep);
                        switch (debugStep) {
                            case 0:
                                mGraph.addGraphVertex(402, 925);
                                break;
                            case 1:
                                mGraph.addGraphVertex(492, 1155);
                                break;
                            case 2:
                                mGraph.addGraphVertex(765, 987);
                                break;
                            case 3:
                                mGraph.addGraphVertex(501, 1178);
                                break;
                            case 4:
                                mGraph.addGraphEdge(1, 2);
                                break;
                            case 5:
                                mGraph.addGraphEdge(0, 2);
                                break;
                            case 6:
                                mGraph.addGraphEdge(1, 0);
                                break;
                            case 7:
                                mGraph.addGraphEdge(0, 3);
                                break;
                            default:
                                break;
                        }
                        debugStep++;
                        }
//                Log.i(TAG, "Num Vertices: " + mGraph.mGraphVertices.size());
//                Log.i(TAG, "Touched vertex: " + String.valueOf(touchedVertex));
//                if (touchedVertex > -1) {
//                    mVertexPointer.put(event.getPointerId(0), touchedVertex);
//                } else {
//                    int newVertInd = mGraph.addGraphVertex(xTouch, yTouch);
////                    if (newVertInd > 0) {
////                        mGraph.addGraphEdge(0, newVertInd);
////                    }
//                    for (int i = 0; i < newVertInd; i++) {
//                        mGraph.addGraphEdge(i, newVertInd);
//                    }
//                }

                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
//                if (mMode != MODE.MOVE_V) break;
//                Log.w(TAG, "Pointer down");
//                // It secondary pointers, so obtain their ids and check circles
//                pointerId = event.getPointerId(actionIndex);
//
//                xTouch = (int) event.getX(actionIndex);
//                yTouch = (int) event.getY(actionIndex);
//
//                touchedVertex = mGraph.pointOnAnyVertex(new Point(xTouch, yTouch));
//                mSelectedVertex =
//                        ((touchedVertex > -1) && (touchedVertex != mSelectedVertex)) ?
//                                touchedVertex : -1;
//                if (touchedVertex > -1) {
//                    mVertexPointer.put(event.getPointerId(pointerId), touchedVertex);
//                }
//                invalidate();
//                handled = true;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mMode != MODE.MOVE_V) break;

                final int pointerCount = event.getPointerCount();

//                Log.w(TAG, "Move");

                for (actionIndex = 0; actionIndex < pointerCount; actionIndex++) {
                    // Some pointer has moved, search it by pointer id
                    pointerId = event.getPointerId(actionIndex);

                    xTouch = (int) event.getX(actionIndex);
                    yTouch = (int) event.getY(actionIndex);
                    int touchedVertexInd = mVertexPointer.get(pointerId, -1);
                    if (touchedVertexInd != -1) {
                        mGraph.moveVertex(touchedVertexInd, xTouch, yTouch);
                    }
                }
                invalidate();
                handled = true;
                break;


            case MotionEvent.ACTION_UP:
                if (mMode != MODE.MOVE_V) break;
                mSelectedVertex = -1;
                clearCirclePointer();
                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                if (mMode != MODE.MOVE_V) break;
                mSelectedVertex = -1;
                // not general pointer was up
                clearCirclePointer();
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
     * Clears all Vertex - pointer id relations
     */
    private void clearCirclePointer() {
//        Log.w(TAG, "clearVertexPointer");
        mVertexPointer.clear();
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void clear() {
        mGraph.clear();
        invalidate();
    }
}
