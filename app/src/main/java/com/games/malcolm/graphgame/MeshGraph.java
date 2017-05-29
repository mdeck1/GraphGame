package com.games.malcolm.graphgame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Thomas on 3/17/17.
 *
 * Messy wrapper class for mesh to be able to use and develop forward.
 *
 */

public class MeshGraph extends Mesh {
    private static final String TAG = "MeshGraph";

    private int mRadius = 30;

    private static final int VERTICES_LIMIT = 5;
    private static final int EDGES_LIMIT = 10; // with |V| = 5, |E|=10 is K5, so limit to 9

    private ArrayList<Point> mIntersections;

    private class GraphEdge {
        Vertex mV1;
        Vertex mV2;
        GraphEdge(Vertex v1, Vertex v2) {
            mV1 = v1;
            mV2 = v2;
        }
        @Override
        public String toString() {
            return "GraphEdge: [v1: " + mV1.mId + ", v2: " + mV2.mId + "]";
        }
    }

    private class Intersection {
        Point mP; // intersection point
        HalfEdge mHe;
        Intersection(Point intersection, HalfEdge he) {
            mP = intersection;
            mHe = he;
        }
    }


    private static class OrderByDistance implements Comparator<Intersection> {
        Point mOrigin;
        OrderByDistance(Point p) {
            mOrigin = p;
        }
        @Override
        public int compare(Intersection p1, Intersection p2) {
            Point v1 = p1.mP.copyMinus(mOrigin);
            Point v2 = p2.mP.copyMinus(mOrigin);
            return Float.compare(v1.dot(v1), v2.dot(v2));
        }
    }

    ArrayList<GraphEdge> mGraphEdges;
    ArrayList<Vertex> mGraphVertices;

    MeshGraph() {
        clear();
    }

    // TODO: API currently allows for isolated vertices
    public int addGraphVertex(final int x, final int y) {
        validate();
        if (mGraphVertices.size() >= VERTICES_LIMIT) {
            return -1;
        }
        Point p = new Point(x, y);
        if (pointOnAnyVertex(p) >= 0) {
            Log.i(TAG, "Point already contained in Vertex. Not adding Vertex");
            return -1;
        }
        Vertex v = addVertex(p);
        mGraphVertices.add(v);
        validate();
        return mGraphVertices.size() - 1;
    }

    public int pointOnAnyVertex(Point p) {
        for (int i = 0; i < mVertices.size(); i++) {
            if (p.isInCircle(mVertices.get(i).mP, mRadius)) {
                return i;
            }
        }
        return -1;
    }

    private ArrayList<Intersection> getIntersections(GraphEdge ge1) {
//        Log.i(TAG, "Looking for Intersections for " + ge1.mV1.mP.toString() + " and " + ge1.mV2.mP.toString());
        ArrayList<Intersection> intersections = new ArrayList<>();
        for (HalfEdge he : mEdges) {
            if (he.mId > he.mOpposite.mId) continue; // dedupe
//            Log.i(TAG, "Look at edge:\n" + he.mVertex.toString() + " and\n" + he.mOpposite.mVertex.toString());

            if (Point.segmentsIntersect(
                    ge1.mV1.mP, ge1.mV2.mP,
                    he.mVertex.mP, he.mOpposite.mVertex.mP)) {
                Log.i(TAG, "Intersecting Edge: " + he.toString());
                Point p = Point.lineIntersection(
                        ge1.mV1.mP, ge1.mV2.mP,
                        he.mVertex.mP, he.mOpposite.mVertex.mP);
                intersections.add(new Intersection(p, he));
            }
        }
        return intersections;
    }

    public void addGraphEdge(final int startVertexInd, final int endVertexInd) {
        if (mGraphEdges.size() >= EDGES_LIMIT) {
            Log.i(TAG, "At Edge Limit. Not adding more.");
            return;
        }
        Vertex v1 = mGraphVertices.get(startVertexInd);
        Vertex v2 = mGraphVertices.get(endVertexInd);

        GraphEdge ge = new GraphEdge(v1, v2);
        mGraphEdges.add(ge);

        ArrayList<Intersection> intersections = getIntersections(ge);
        Collections.sort(intersections, new OrderByDistance(v1.mP));
        ArrayList<Vertex> vs = new ArrayList<>(Arrays.asList(v1));
        for (Intersection in: intersections) {
            Vertex v = splitEdge(in.mHe.mVertex, in.mHe.mOpposite.mVertex, in.mP);
            vs.add(v);
        }
        vs.add(v2);
        for (int i = 1; i < vs.size(); i++) {
            addEdge(vs.get(i-1), vs.get(i));
        }
        validate();
    }

    private void addEdge(Vertex v1, Vertex v2) {
        if (edgeBetweenVertices(v1, v2) != null) {
            Log.i(TAG, "GOOD CATCH");
            return; // Needs improvement
        }
        validate();
        Face f = faceBetweenPoints(v1.mP, v2.mP);
        if (!v1.isIsolated() && !v2.isIsolated()) { // two connected vertices
            splitFace(f, v1, v2);
        } else { // At least one vertex is isolated
            boolean v1Isolated = v1.isIsolated(); // Mark before adding edges
            boolean v2Isolated = v2.isIsolated(); // Mark before adding edges
            HalfEdge he1 = addHalfEdge(v1, v2, f);
            HalfEdge he2 = addHalfEdge(v2, v1, f);
            he1.mOpposite = he2;
            he2.mOpposite = he1;
            // Set next edges appropriately
            HalfEdge prev1 = v1Isolated ? he2 : findPreviousEdgeOnFace(v1, f);
            HalfEdge prev2 = v2Isolated ? he1 : findPreviousEdgeOnFace(v2, f);
            he2.mNext = prev1.mNext;
            prev1.mNext = he1;
            he1.mNext = prev2.mNext;
            prev2.mNext = he2;
            f.mHe = he1; // This step should be last
        }
        validate();
    }

    public void draw(final Canvas canv) {
        validate();
        ArrayList<Integer> colors = new ArrayList<>(Arrays.asList(Color.YELLOW,
                Color.MAGENTA, Color.GREEN, Color.CYAN, Color.BLUE, Color.DKGRAY));
        Paint fPaint = new Paint();
        fPaint.setStyle(Paint.Style.FILL);
        for (Face f : mFaces) {
            if (f.mId == 0) fPaint.setColor(Color.LTGRAY);
            else fPaint.setColor(colors.get((f.mId-1)%colors.size()));
            drawFace(canv, fPaint, f);
        }
        Paint ePaint = new Paint();
        ePaint.setColor(Color.BLACK);
        ePaint.setStrokeWidth(10);
        for (HalfEdge edge : mEdges) {
            canv.drawLine(
                    edge.mVertex.mP.x,
                    edge.mVertex.mP.y,
                    edge.mOpposite.mVertex.mP.x,
                    edge.mOpposite.mVertex.mP.y,
                    ePaint);
        }
        Paint vPaint = new Paint();
        vPaint.setStrokeWidth(8);
        for (Vertex v : mVertices) {
            if (mGraphVertices.contains(v)) {
                vPaint.setStyle(Paint.Style.FILL);
                vPaint.setColor(Color.RED);
                canv.drawCircle(v.mP.x, v.mP.y, 30, vPaint);
                vPaint.setStyle(Paint.Style.STROKE);
                vPaint.setColor(Color.BLACK);
                canv.drawCircle(v.mP.x, v.mP.y, 30, vPaint);
            } else {
                vPaint.setStyle(Paint.Style.STROKE);
                vPaint.setColor(Color.WHITE);
                canv.drawCircle(v.mP.x, v.mP.y, 30, vPaint);
                vPaint.setColor(Color.BLACK);
                canv.drawCircle(v.mP.x, v.mP.y, 33, vPaint);
            }
        }

        for (Point p : mIntersections) {
            vPaint.setStyle(Paint.Style.STROKE);
            vPaint.setColor(Color.BLACK);
            canv.drawCircle(p.x, p.y, 30, vPaint);
        }
    }

    private void drawFace(final Canvas canv, Paint paint, Face face) {
        Path path = new Path();
        if (face.mHe == null) {
            canv.drawRect(0, 0, canv.getWidth(), canv.getHeight(), paint);
            return;
        }
        ArrayList<Vertex> vertices = face.getVertices();
        if (vertices.size() > 0) {
            Vertex lastVertex = vertices.get(vertices.size() - 1);
            path.moveTo(lastVertex.mP.x, lastVertex.mP.y);
            for (Vertex vertex : vertices) {
                path.lineTo(vertex.mP.x, vertex.mP.y);
            }
        }
        if (face.mId == 0) { // outer face
            if (vertices.size() < 3) {
                canv.drawRect(0, 0, canv.getWidth(), canv.getHeight(), paint);
            } else {
                path.toggleInverseFillType();
                RectF bounds = new RectF();
                path.computeBounds(bounds, true);
                canv.drawRect(0, bounds.bottom, canv.getWidth(), canv.getHeight(), paint);
                canv.drawRect(0, bounds.top, bounds.left, bounds.bottom, paint);
                canv.drawRect(0, 0, canv.getWidth(), bounds.top, paint);
                canv.drawRect(bounds.right, bounds.top, canv.getWidth(), bounds.bottom, paint);
            }
        }
        canv.drawPath(path, paint);
    }

    @Override
    public void clear() {
        super.clear();
        mGraphEdges = new ArrayList<>();
        mGraphVertices = new ArrayList<>();
        mIntersections = new ArrayList<>();
    }
}
