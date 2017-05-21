package com.games.malcolm.graphgame;

import android.content.Context;
import android.graphics.Canvas;
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

    private MeshGraph mGraph;

    /**
     * Default constructor
     *
     * @param ct {@link android.content.Context}
     */
    public InteractiveCircleView(final Context ct) {
        super(ct);
        mGraph = new MeshGraph();
        mVertexPointer = new SparseIntArray(CIRCLES_LIMIT);
    }
    public InteractiveCircleView(final Context ct, final AttributeSet attrs) {
        super(ct, attrs);
        mGraph = new MeshGraph();
        mVertexPointer = new SparseIntArray(CIRCLES_LIMIT);
    }

    public InteractiveCircleView(final Context ct, final AttributeSet attrs, final int defStyle) {
        super(ct, attrs, defStyle);
        mGraph = new MeshGraph();
        mVertexPointer = new SparseIntArray(CIRCLES_LIMIT);
    }

    @Override
    public void onDraw(final Canvas canv) {
        mGraph.draw(canv);
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
                touchedVertex = mGraph.pointOnAnyVertex(xTouch, yTouch);
                if (touchedVertex > -1) {
                    mVertexPointer.put(event.getPointerId(0), touchedVertex);
                } else {
                    int newVertInd = mGraph.addGraphVertex(xTouch, yTouch);
                    for (int i = 0; i < newVertInd; i++) {
                        mGraph.addEdge(i, newVertInd);
                    }
                }

                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                Log.w(TAG, "Pointer down");
                // It secondary pointers, so obtain their ids and check circles
                pointerId = event.getPointerId(actionIndex);

                xTouch = (int) event.getX(actionIndex);
                yTouch = (int) event.getY(actionIndex);

                touchedVertex = mGraph.pointOnAnyVertex(xTouch, yTouch);
                if (touchedVertex > -1) {
                    mVertexPointer.put(event.getPointerId(pointerId), touchedVertex);
                } else {
                    int newVertInd = mGraph.addGraphVertex(xTouch, yTouch);
                    for (int i = 0; i < newVertInd; i++) {
                        mGraph.addEdge(i, newVertInd);
                    }
                }
                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_MOVE:
                final int pointerCount = event.getPointerCount();

//                Log.w(TAG, "Move");

                for (actionIndex = 0; actionIndex < pointerCount; actionIndex++) {
                    // Some pointer has moved, search it by pointer id
                    pointerId = event.getPointerId(actionIndex);

                    xTouch = (int) event.getX(actionIndex);
                    yTouch = (int) event.getY(actionIndex);
                    Integer touchedVertexInd = mVertexPointer.get(pointerId);
                    if (touchedVertexInd != null) {
                        mGraph.moveVertex(touchedVertexInd, xTouch, yTouch);
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
                mVertexPointer.delete(pointerId);
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
        Log.w(TAG, "clearVertexPointer");
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
