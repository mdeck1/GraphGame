package com.games.malcolm.graphgame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Thomas on 3/17/17.
 *
 * Messy wrapper class for mesh to be able to use and develop forward.
 *
 */

public class MeshGraph extends Mesh {
    private static final String TAG = "MeshGraph";
    private int mClearingTouches = 0;

    private int mRadius = 30;

    private static final int VERTICES_LIMIT = 5;
    private static final int EDGES_LIMIT = 9; // with |V| = 5, |E|=10 is K5, so limit to 9

    private Face mOuterFace;

    MeshGraph() {
        super();
        mOuterFace = new Face(-1);
    }

    public int addGraphVertex(final int x, final int y) {
        if (mVertices.size() == VERTICES_LIMIT) {
            Log.i(TAG, "At Vertex Limit. Not adding more.");
            if (mClearingTouches == 2) {
                Log.i(TAG, "Clearing graph.");
                clear();
                mClearingTouches = 0;
            } else {
                Log.i(TAG, "Incrementing mClearingTouches counter.");
                mClearingTouches++;
            }
            return -1;
        }
        if (pointOnAnyVertex(x, y) >= 0) {
            Log.i(TAG, "Point already contained in Vertex. Not adding Vertex");
            return -1;
        }
        addVertex(x, y);
        Log.i(TAG, mVertices.get(mVertices.size() - 1).toString());
        Log.i(TAG, "mesh is valid: "  + isValid());
        return mVertices.size() - 1;
    }


    public float distSq(final Vertex v, final int x, final int y) {
        return (v.mX - x) * (v.mX - x) + (v.mY - y) * (v.mY - y);
    }

    public boolean isPointContained(final Vertex v, final int x, final int y) {
        return distSq(v, x, y) <= mRadius * mRadius;
    }

    public int pointOnAnyVertex(final int x, final int y) {
        for (int i = 0; i < mVertices.size(); i++) {
            if (isPointContained(mVertices.get(i), x, y)) {
                return i;
            }
        }
        return -1;
    }

    public void moveVertex(final int vertexInd, final int newX, final int newY) {
//        Log.i(TAG, "moveVertex not implemented.");
        mVertices.get(vertexInd).move(newX, newY);
//        updateIntersections();
        Log.i(TAG, "mesh is valid: "  + isValid());
    }

    public void addEdge(final int startVertexInd, final int endVertexInd) {
        if (mEdges.size() == EDGES_LIMIT) {
            Log.i(TAG, "At Edge Limit. Not adding more.");
            return;
        }
        Vertex v1 = mVertices.get(startVertexInd);
        Vertex v2 = mVertices.get(endVertexInd);
        Log.i(TAG, v1.toString());
        Log.i(TAG, v2.toString());


        Log.i(TAG,"v1 ANGLES");
        for (HalfEdge he : getOutEdges(v1)) {
            Log.i(TAG, String.valueOf(angleBetweenEdges(v2, he)));
        }
        Log.i(TAG,"v2 ANGLES");
        for (HalfEdge he : getOutEdges(v2)) {
            Log.i(TAG, String.valueOf(angleBetweenEdges(v1, he)));
        }
        HalfEdge he1 = addHalfEdge(v1, v2, mOuterFace); // TODO: fix face
        HalfEdge he2 = addHalfEdge(v2, v1, mOuterFace); // TODO: fix face
        mOuterFace.mHe = he1;
        he1.mOpposite = he2;
        he2.mOpposite = he1;
        if (mEdges.size() < 4) {
            he1.mNext = he2;
            he2.mNext = he1;
        } else {
            he1.mNext = he2;
            he2.mNext = mEdges.get(0);
            mEdges.get(mEdges.size() - 3).mNext = he1;
        }
//        updateIntersections();
        Log.i(TAG, "mesh is valid: "  + isValid());
    }

    private double angleBetweenEdges(Vertex v1, HalfEdge he2) {
        Vertex v = he2.mVertex;
        if (v.mId != he2.mVertex.mId) throw new AssertionError("These should be the same");
        PointF zV1 = new PointF(
                v1.mX - v.mX,
                v1.mY - v.mY);
        PointF zV2 = new PointF(
                he2.mOpposite.mVertex.mX - v.mX,
                he2.mOpposite.mVertex.mY - v.mY);
        double cos = (double)(zV1.x * zV2.x + zV1.y * zV2.y) // dot product
                / (zV1.length() + zV2.length());
        return cos;
    }

    public void draw(final Canvas canv) {
//        mOuterFace.draw(canv);
        Paint ePaint = new Paint();
        ePaint.setColor(Color.CYAN);
        ePaint.setStrokeWidth(10);
        for (HalfEdge edge : mEdges) {
            canv.drawLine(
                    edge.mVertex.mX,
                    edge.mVertex.mY,
                    edge.mOpposite.mVertex.mX,
                    edge.mOpposite.mVertex.mY,
                    ePaint);
        }
        Paint vPaint = new Paint();
        vPaint.setColor(Color.RED);
        vPaint.setStrokeWidth(5);
        vPaint.setStyle(Paint.Style.FILL);
        for (Vertex vertex : mVertices) {
            canv.drawCircle(vertex.mX, vertex.mY, 30, vPaint);
        }
//        for (Vertex intersection : mVirtualVertices) {
//            intersection.draw(canv);
//        }
    }
}
