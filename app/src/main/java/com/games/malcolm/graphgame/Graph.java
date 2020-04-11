package com.games.malcolm.graphgame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by Thomas on 3/4/17.
 */

public class Graph {

    private final static int VERTEX_RADIUS = 30;
    private final static int EDGE_WIDTH = 10;
//    private int mClearingTouches = 3;

    private static final String TAG = "Graph";


    private static class Vertex {

        private int mRadius = 30;

        int mId;
        Point mP;
        ArrayList<Edge> mEdges;

        Vertex(float x, float y, int id) {
            mP = new Point(x, y);
            mId = id;
            mEdges = new ArrayList<>();
        }
        Vertex(Point p, int id) {
            this(p.x, p.y, id);
        }
        Vertex(float x, float y, int id, int radius) {
            this(x, y, id);
            mRadius = radius;
        }

        public void move(int newX, int newY) {
            mP.x = newX;
            mP.y = newY;
        }

        public boolean isNeighbor(Vertex v) {
            for (Edge ge : mEdges) {
                if (ge.mV1 == v || ge.mV2 == v) return true;
            }
            return false;
        }
        public ArrayList<Vertex> getNeighbors() {
            ArrayList<Vertex> neighbors = new ArrayList<>();
            for (Edge ge : mEdges) {
                neighbors.add(ge.mV1 == this ? ge.mV2 : ge.mV1);
            }
            return neighbors;
        }
        public Edge getEdgeBetween(Vertex v) {
            for (Edge ge : mEdges) {
                if (ge.mV1 == v || ge.mV2 == v) return ge;
            }
            return null;
        }


        public void draw(final Canvas canv, boolean isSelected) {

            Paint paint = new Paint();
            paint.setStrokeWidth(8);
            paint.setStyle(Paint.Style.FILL);
            if (isSelected) {
                paint.setColor(Color.BLACK);
            } else {
                paint.setColor(Color.RED);
            }
            canv.drawCircle(mP.x, mP.y, mRadius, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLACK);
            canv.drawCircle(mP.x, mP.y, mRadius, paint);
        }

//        public float distSq(final int x, final int y) {
//            return (mP.x - x) * (mP.x - x) + (mP.y - y) * (mP.y - y);
//        }
//        public boolean isPointContained(final int x, final int y) {
//            return distSq(x, y) <= mRadius * mRadius;
//        }

        @Override
        public String toString() {
            return "GraphVertex: [id: " + mId +
                    ", num Edges: " + mEdges.size() + "]";
        }
    }

    private static class Edge {
        int mId;
        Vertex mV1;
        Vertex mV2;
        ArrayList<Intersection> mIntersections;


        Edge(Vertex v1, Vertex v2, int id) {
            mV1 = v1;
            mV2 = v2;
            mId = id;
            mIntersections = new ArrayList<>();
        }

        public void draw(final Canvas canv, boolean isSelected) {
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            if (isSelected) {
                paint.setStrokeWidth(20);
            } else {
                paint.setStrokeWidth(10);
            }
            canv.drawLine(mV1.mP.x, mV1.mP.y, mV2.mP.x,mV2.mP.y, paint);
        }

        private float slope() {
            float deltaX = mV1.mP.x - mV2.mP.x;
            float deltaY = mV1.mP.y - mV2.mP.y;
            return deltaY / deltaX;
        }

        private float shift() {
            return mV1.mP.y - mV1.mP.x * slope();
        }

        private boolean containsVertex(Vertex v) {
            return v.mP.x > Math.min(mV1.mP.x, mV2.mP.x) && v.mP.x < Math.max(mV1.mP.x, mV2.mP.x)
                    && v.mP.y > Math.min(mV1.mP.y, mV2.mP.y) && v.mP.y < Math.max(mV1.mP.y, mV2.mP.y);
        }

//        public Vertex intersect(Edge edge) {
//            Paint virtualPaint = new Paint();
//            virtualPaint.setColor(Color.BLACK);
//            virtualPaint.setStyle(Paint.Style.FILL);
//            float x = Math.round(edge.shift() - shift()) / (slope() - edge.slope());
//            float y = Math.round(slope() * x + shift());
//            Log.i(TAG, "Possible Intersection: " + x + ", " + y);
//            int rX = (int) (x);
//            int rY = (int) (y);
//            Log.i(TAG, "Rounded Intersection: " + rX + ", " + rY);
//            Vertex intersect = new Vertex(rX, rY, 15, virtualPaint);
//            if (containsVertex(intersect) && edge.containsVertex(intersect)) {
//                return intersect;
//            }
//            return null;
//        }

        @Override
        public String toString() {
            String v1Str = mV1 != null ? String.valueOf(mV1.mId) : "null";
            String v2Str = mV2 != null ? String.valueOf(mV2.mId) : "null";
            return "GraphEdge: [id: " + mId + ", v1: " + v1Str + ", v2: " + v2Str + "]";
        }
    }

    private static class Intersection {
        int mId;
//        Vertex mV;
        Edge mGe1;
        Edge mGe2;
        Point mP; // intersection point. Only used in transition, should be null when in graph
        Intersection(Point p, Edge ge1, Edge ge2) {
            mId = -1;
            mGe1 = ge1;
            mGe2 = ge2;
            mP = p;
        }
        public void draw(Canvas canv) {
            Paint paint = new Paint();
            paint.setStrokeWidth(8);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.WHITE);
            canv.drawCircle(mP.x, mP.y, VERTEX_RADIUS - 3, paint);
            paint.setColor(Color.BLACK);
            canv.drawCircle(mP.x, mP.y, VERTEX_RADIUS, paint);
        }

        @Override
        public String toString() {
            String pStr = mP != null ? mP.toString() : "null";
            String ge1Str = mGe1 != null ? String.valueOf(mGe1.mId) : "null";
            String ge2Str = mGe2 != null ? String.valueOf(mGe2.mId) : "null";
            return "Intersection: [id: " + mId + ", p: " + pStr +
                    ", ge1: " + ge1Str + ", ge2: " + ge2Str + "]";
        }
    }


    private static class Face {
        private boolean mIsOuterFace; // There can only be one per Graph
        private ArrayList<Vertex> mVertices;
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
                path.moveTo(lastVertex.mP.x, lastVertex.mP.y);
                for (Vertex vertex : mVertices) {
                    path.lineTo(vertex.mP.x, vertex.mP.y);
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

    private static final int EDGES_LIMIT = 9; // with |V| = 5, |E|=10 is K5, so limit to 9


    /** All available vertices */
    private ArrayList<Vertex> mVertices = new ArrayList<>();
//    private ArrayList<Vertex> mVirtualVertices = new ArrayList<>();
    /** All available edges */
    private ArrayList<Edge> mEdges = new ArrayList<>();
    /** All available faces */
//    private HashSet<Face> mInnerFaces = new HashSet<>();
//    private Face outerFace = new Face(true);
    private ArrayList<Intersection> mIntersections = new ArrayList<>();


    Graph() {
//        outerFace = new Face(true);
    }

    public int addGraphVertex(final int x, final int y) {
//        if (pointOnAnyVertex(x, y) >= 0) {
//            Log.i(TAG, "Point already contained in Vertex. Not adding Vertex");
//            return -1;
//        }

        Vertex newVertex = new Vertex(x, y, mVertices.size());
        mVertices.add(newVertex);
//        outerFace.addVertex(newVertex);
        return newVertex.mId;
    }
    public void addGraphEdge(final int v1Ind, final int v2Ind) {
//        if (mEdges.size() == EDGES_LIMIT) {
//            Log.i(TAG, "At Edge Limit. Not adding more.");
//            return;
//        }
        Vertex v1 = mVertices.get(v1Ind);
        Vertex v2 = mVertices.get(v2Ind);
        if (v1.isNeighbor(v2)) return;

        Edge ge = new Edge(v1, v2, mEdges.size());
        mEdges.add(ge);
        v1.mEdges.add(ge);
        v2.mEdges.add(ge);
        setIntersections();
    }


    private ArrayList<Intersection> getIntersections() {
        ArrayList<Intersection> intersections = new ArrayList<>();
        for (int i = 0; i < mEdges.size(); i++) {
            for (int j = i+1; j < mEdges.size(); j++) {
                Edge ge1 = mEdges.get(i);
                Edge ge2 = mEdges.get(j);
                if (Point.segmentsIntersect(
                        ge1.mV1.mP, ge1.mV2.mP,
                        ge2.mV1.mP, ge2.mV2.mP)) {

                    Point p = Point.lineIntersection(
                            ge1.mV1.mP, ge1.mV2.mP,
                            ge2.mV1.mP, ge2.mV2.mP);
                    intersections.add(new Intersection(p, ge1, ge2));
                }
            }
        }
        return intersections;
    }
    private void setIntersections() {
        for (Edge ge: mEdges) {
            ge.mIntersections = new ArrayList<>();
        }
        mIntersections = getIntersections();
        for (Intersection in: mIntersections) {
            in.mGe1.mIntersections.add(in);
            in.mGe2.mIntersections.add(in);
        }
    }


    public int pointOnAnyVertex(final Point p) {
        return pointOnAnyVertex(p, -1);
    }
    private int pointOnAnyVertex(final Point p, int skip) {
        for (int i = 0; i < mVertices.size(); i++) {
            if (i == skip) continue;
            if (p.isInCircle(mVertices.get(i).mP, VERTEX_RADIUS * 2)) {
                return i;
            }
        }
        return -1;
    }



    public void moveVertex(final int vertexInd, final int x, final int y) {
        mVertices.get(vertexInd).move(x, y);
        setIntersections();
    }

    public void draw(final Canvas canv, int selectedVertexInd) {
        ArrayList<Integer> colors = new ArrayList<>(Arrays.asList(Color.YELLOW,
                Color.MAGENTA, Color.GREEN, Color.CYAN, Color.BLUE, Color.DKGRAY));
//        for (Face f : mFaces) {
//            int color = Color.LTGRAY;
//            if (f.mId > 0) color = colors.get((f.mId-1)%colors.size());
//            drawFace(canv, f, color);
//        }
        for (Edge ge : mEdges) {
            ge.draw(canv, false);
        }
        for (Vertex gv : mVertices) {
            gv.draw(canv, gv.mId == selectedVertexInd);
        }
        for (Intersection in : mIntersections) {
            in.draw(canv);
        }
    }


//    private void updateIntersections() {
//        mVirtualVertices.clear();
//        for (int i = 0; i < mEdges.size(); i++){
//            for (int j = i + 1; j < mEdges.size(); j++) {
//                Vertex intersection = mEdges.get(i).intersect(mEdges.get(j));
//                if (intersection != null) {
//                    Log.i(TAG, "Intersecting edges:\n" + mEdges.get(i) + "\n" + mEdges.get(j));
//                    Log.i(TAG, "Intersection: " + intersection);
//                    mVirtualVertices.add(intersection);
//                }
//            }
//        }
//        Log.i(TAG, "Number of intersections: " + mVirtualVertices.size());
//    }

    public void deleteGraphEdge(final int startVertexInd, final int endVertexInd) {
        Vertex v1 = mVertices.get(startVertexInd);
        Vertex v2 = mVertices.get(endVertexInd);
        Edge ge = v1.getEdgeBetween(v2);
        deleteGraphEdge(ge);
    }
    private void deleteGraphEdge(Edge ge) {
        if (ge == null) return;
        ge.mV1.mEdges.remove(ge);
        ge.mV2.mEdges.remove(ge);

        Edge last = mEdges.remove(mEdges.size() - 1);
        if (last.mId == ge.mId) {
            return;
        }
        last.mId = ge.mId;
        mEdges.set(last.mId, last);
        setIntersections();
    }
    public void deleteGraphVertex(final int vertexInd) {
        Vertex v = mVertices.get(vertexInd);
        deleteGraphVertex(v);
    }
    private void deleteGraphVertex(Vertex v) {
        ArrayList<Edge> toRemove = (ArrayList<Edge>)v.mEdges.clone();
//                new ArrayList<>();
//        for (Edge ge : v.mEdges) {
//            toRemove.add(ge);
//        }
        for (Edge ge : toRemove) {
            deleteGraphEdge(ge);
        }
        Vertex last = mVertices.remove(mVertices.size() - 1);
        if (last.mId == v.mId) {
            return;
        }
        last.mId = v.mId;
        mVertices.set(last.mId, last);
    }

    public void clear() {
        mEdges.clear();
        mVertices.clear();
        mIntersections.clear();
//        mVirtualVertices.clear();
//        outerFace = new Face(true);
    }
}
