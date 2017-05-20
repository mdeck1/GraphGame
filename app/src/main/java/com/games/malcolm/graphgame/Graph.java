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
    private int clearingTouches = 3;

    private static final String TAG = "Graph";


    private static class Vertex {

        private int RADIUS = 30;

        float x;
        float y;
        private Paint paint;

        Vertex(float x, float y) {
            this.x = x;
            this.y = y;
            initPaint();
        }

        Vertex(float x, float y, int radius, Paint paint) {
            this(x, y);
            this.paint = paint;
            this.RADIUS = radius;
        }

        private void initPaint() {
            paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStrokeWidth(5);
            paint.setStyle(Paint.Style.FILL);
        }

        public void move(int newX, int newY) {
            this.x = newX;
            this.y = newY;
        }

        public void draw(final Canvas canv) {
            canv.drawCircle(x, y, RADIUS, paint);
        }

        public float distSq(final int x, final int y) {
            return (this.x - x) * (this.x - x) + (this.y - y) * (this.y - y);
        }

        public boolean isPointContained(final int x, final int y) {
            return distSq(x, y) <= RADIUS * RADIUS;
        }

        @Override
        public String toString() {
            return "Vertex[" + x + ", " + y + "]";
        }
    }

    private static class Edge {
        Vertex startVertex;
        Vertex endVertex;
        private Paint paint;

        Edge(Vertex startVertex, Vertex endVertex) {
            this.startVertex = startVertex;
            this.endVertex = endVertex;
            initPaint();
        }

        private void initPaint() {
            paint = new Paint();
            paint.setColor(Color.CYAN);
            paint.setStrokeWidth(10);
        }

        public void draw(final Canvas canv) {
            canv.drawLine(startVertex.x, startVertex.y, endVertex.x, endVertex.y, paint);
        }

        private float slope() {
            float deltaX = startVertex.x - endVertex.x;
            float deltaY = startVertex.y - endVertex.y;
            return deltaY/deltaX;
        }

        private float shift() {
            return startVertex.y - startVertex.x * slope();
        }

        private boolean containsVertex(Vertex vertex) {
            return vertex.x > Math.min(startVertex.x, endVertex.x) && vertex.x < Math.max(startVertex.x, endVertex.x)
                    && vertex.y > Math.min(startVertex.y, endVertex.y) && vertex.y < Math.max(startVertex.y, endVertex.y);
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
            return "Edge[" + startVertex + ", " + endVertex +"]";
        }
    }

    private static class Face {
        private boolean isOuterFace; // There can only be one per Graph
        private ArrayList<Vertex> vertices;
        private Path facePath;
        private Paint paint;

        Face(boolean isOuterFace) {
            this(isOuterFace, new ArrayList<Vertex>());
        }

        Face(ArrayList<Vertex> vertices) {
            this(true, vertices);
        }

        Face(boolean isOuterFace, ArrayList<Vertex> vertices) {
            this.isOuterFace = isOuterFace;
            this.vertices = vertices;
            initPaint();
        }

        private void initPaint() {
            paint = new Paint();
            paint.setColor(Color.GRAY);
            paint.setStyle(Paint.Style.FILL);
        }

        public void addVertex(Vertex vertex) {
            vertices.add(vertex);
        }

        public void draw(final Canvas canv) {
            Path path = new Path();
            if (vertices.size() > 0) {
                Vertex lastVertex = vertices.get(vertices.size() - 1);
                path.moveTo(lastVertex.x, lastVertex.y);
                for (Vertex vertex : vertices) {
                    path.lineTo(vertex.x, vertex.y);
                }
            }
            if (isOuterFace) {
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
            if (clearingTouches == 2) {
                Log.i(TAG, "Clearing graph.");
                clear();
                clearingTouches = 0;
            } else {
                Log.i(TAG, "Incrementing clearingTouches counter.");
                clearingTouches++;
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
