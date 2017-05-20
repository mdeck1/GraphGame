package com.games.malcolm.graphgame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Thomas on 3/4/17.
 */

public class Graph {

//    private final static int VERTEX_RADIUS = 30;
//    private final static int EDGE_WIDTH = 10;
    private int mClearingTouches = 3;

    private static final String TAG = "Graph";


    private static class Vertex {

        private int mRadius = 30;

        float mX;
        float mY;
        private Paint mPaint;

        Vertex(float x, float y) {
            mX = x;
            mY = y;
            initPaint();
        }

        Vertex(float x, float y, int radius, Paint paint) {
            this(x, y);
            mPaint = paint;
            mRadius = radius;
        }

        private void initPaint() {
            mPaint = new Paint();
            mPaint.setColor(Color.RED);
            mPaint.setStrokeWidth(5);
            mPaint.setStyle(Paint.Style.FILL);
        }

        public void move(int newX, int newY) {
            mX = newX;
            mY = newY;
        }

        public void draw(final Canvas canv) {
            canv.drawCircle(mX, mY, mRadius, mPaint);
        }

        public float distSq(final int x, final int y) {
            return (mX - x) * (mX - x) + (mY - y) * (mY - y);
        }

        public boolean isPointContained(final int x, final int y) {
            return distSq(x, y) <= mRadius * mRadius;
        }

        @Override
        public String toString() {
            return "Vertex[" + mX + ", " + mY + "]";
        }
    }

    private static class Edge {
        Vertex mStartVertex;
        Vertex mEndVertex;
        private Paint mPaint;

        Edge(Vertex startVertex, Vertex endVertex) {
            mStartVertex = startVertex;
            mEndVertex = endVertex;
            initPaint();
        }

        private void initPaint() {
            mPaint = new Paint();
            mPaint.setColor(Color.CYAN);
            mPaint.setStrokeWidth(10);
        }

        public void draw(final Canvas canv) {
            canv.drawLine(mStartVertex.mX, mStartVertex.mY, mEndVertex.mX, mEndVertex.mY, mPaint);
        }

        private float slope() {
            float deltaX = mStartVertex.mX - mEndVertex.mX;
            float deltaY = mStartVertex.mY - mEndVertex.mY;
            return deltaY / deltaX;
        }

        private float shift() {
            return mStartVertex.mY - mStartVertex.mX * slope();
        }

        private boolean containsVertex(Vertex vertex) {
            return vertex.mX > Math.min(mStartVertex.mX, mEndVertex.mX) && vertex.mX < Math.max(mStartVertex.mX, mEndVertex.mX)
                    && vertex.mY > Math.min(mStartVertex.mY, mEndVertex.mY) && vertex.mY < Math.max(mStartVertex.mY, mEndVertex.mY);
        }

        public Vertex intersect(Edge edge) {
            Paint virtualPaint = new Paint();
            virtualPaint.setColor(Color.BLACK);
            virtualPaint.setStyle(Paint.Style.FILL);
            float x = Math.round(edge.shift() - shift()) / (slope() - edge.slope());
            float y = Math.round(slope() * x + shift());
            Log.i(TAG, "Possible Intersection: " + x + ", " + y);
            int rX = (int) (x);
            int rY = (int) (y);
            Log.i(TAG, "Rounded Intersection: " + rX + ", " + rY);
            Vertex intersect = new Vertex(rX, rY, 15, virtualPaint);
            if (containsVertex(intersect) && edge.containsVertex(intersect)) {
                return intersect;
            }
            return null;
        }

        @Override
        public String toString() {
            return "Edge[" + mStartVertex + ", " + mEndVertex +"]";
        }
    }

    private static class Face {
        private boolean mIsOuterFace; // There can only be one per Graph
        private ArrayList<Vertex> mVertices;
        private Path mFacePath;
        private Paint mPaint;

        Face(boolean isOuterFace) {
            this(isOuterFace, new ArrayList<Vertex>());
        }

        Face(ArrayList<Vertex> vertices) {
            this(true, vertices);
        }

        Face(boolean isOuterFace, ArrayList<Vertex> vertices) {
            mIsOuterFace = isOuterFace;
            mVertices = vertices;
            initPaint();
        }

        private void initPaint() {
            mPaint = new Paint();
            mPaint.setColor(Color.GRAY);
            mPaint.setStyle(Paint.Style.FILL);
        }

        public void addVertex(Vertex vertex) {
            mVertices.add(vertex);
        }

        public void draw(final Canvas canv) {
            Path path = new Path();
            if (mVertices.size() > 0) {
                Vertex lastVertex = mVertices.get(mVertices.size() - 1);
                path.moveTo(lastVertex.mX, lastVertex.mY);
                for (Vertex vertex : mVertices) {
                    path.lineTo(vertex.mX, vertex.mY);
                }
            }
            if (mIsOuterFace) {
                if (mVertices.size() < 3) {
                    canv.drawRect(0, 0, canv.getWidth(), canv.getHeight(), mPaint);
                } else {
                    path.toggleInverseFillType();
                    RectF bounds = new RectF();
                    path.computeBounds(bounds, true);
                    canv.drawRect(0, bounds.bottom, canv.getWidth(), canv.getHeight(), mPaint);
                    canv.drawRect(0, bounds.top, bounds.left, bounds.bottom, mPaint);
                    canv.drawRect(0, 0, canv.getWidth(), bounds.top, mPaint);
                    canv.drawRect(bounds.right, bounds.top, canv.getWidth(), bounds.bottom, mPaint);
                }
            }
            canv.drawPath(path, mPaint);
        }
    }

    private static final int VERTICES_LIMIT = 5;
    private static final int EDGES_LIMIT = 9; // with |V| = 5, |E|=10 is K5, so limit to 9


    /** All available vertices */
    private ArrayList<Vertex> mVertices = new ArrayList<>(VERTICES_LIMIT);
    private ArrayList<Vertex> mVirtualVertices = new ArrayList<>();
    /** All available edges */
    private ArrayList<Edge> mEdges = new ArrayList<>(EDGES_LIMIT);
    /** All available faces */
    private HashSet<Face> mInnerFaces = new HashSet<>();
    private Face outerFace = new Face(true);

    Graph() {
        outerFace = new Face(true);
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

        Vertex newVertex = new Vertex(x, y);
        mVertices.add(newVertex);
        outerFace.addVertex(newVertex);
        return mVertices.size() - 1;
    }

    public int pointOnAnyVertex(final int x, final int y) {
        for (int i = 0; i < mVertices.size(); i++) {
            if (mVertices.get(i).isPointContained(x, y)) {
                return i;
            }
        }
        return -1;
    }

    public void moveVertex(final int vertexInd, final int newX, final int newY) {
        mVertices.get(vertexInd).move(newX, newY);
        updateIntersections();
    }

    public void addEdge(final int startVertexInd, final int endVertexInd) {
        if (mEdges.size() == EDGES_LIMIT) {
            Log.i(TAG, "At Edge Limit. Not adding more.");
            return;
        }
        mEdges.add(new Edge(mVertices.get(startVertexInd), mVertices.get(endVertexInd)));
        updateIntersections();
    }

    public void draw(final Canvas canv) {
        outerFace.draw(canv);
        for (Edge edge : mEdges) {
            edge.draw(canv);
        }
        for (Vertex vertex : mVertices) {
            vertex.draw(canv);
        }
        for (Vertex intersection : mVirtualVertices) {
            intersection.draw(canv);
        }
    }

    private void updateIntersections() {
        mVirtualVertices.clear();
        for (int i = 0; i < mEdges.size(); i++){
            for (int j = i + 1; j < mEdges.size(); j++) {
                Vertex intersection = mEdges.get(i).intersect(mEdges.get(j));
                if (intersection != null) {
                    Log.i(TAG, "Intersecting edges:\n" + mEdges.get(i) + "\n" + mEdges.get(j));
                    Log.i(TAG, "Intersection: " + intersection);
                    mVirtualVertices.add(intersection);
                }
            }
        }
        Log.i(TAG, "Number of intersections: " + mVirtualVertices.size());
    }

    public void clear() {
        mEdges.clear();
        mVertices.clear();
        mVirtualVertices.clear();
        outerFace = new Face(true);
    }
}
