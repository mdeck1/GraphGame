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
 **/

public class MeshGraph extends Mesh {
    private static final String TAG = "MeshGraph";
    private static final int VERTEX_RADIUS = 30;

    private class GraphVertex {
        int mId;
        Vertex mV;
        ArrayList<GraphEdge> mEdges;

        GraphVertex(Vertex v, int id) {
            mId = id;
            mV = v;
            mEdges = new ArrayList<>();
        }
        public boolean isNeighbor(GraphVertex v) {
            for (GraphEdge ge : mEdges) {
                if (ge.mV1 == v || ge.mV2 == v) return true;
            }
            return false;
        }
        public ArrayList<GraphVertex> getNeighbors() {
            ArrayList<GraphVertex> neighbors = new ArrayList<>();
            for (GraphEdge ge : mEdges) {
                neighbors.add(ge.mV1 == this ? ge.mV2 : ge.mV1);
            }
            return neighbors;
        }
        public GraphEdge getEdgeBetween(GraphVertex v) {
            for (GraphEdge ge : mEdges) {
                if (ge.mV1 == v || ge.mV2 == v) return ge;
            }
            return null;
        }
        public void draw(Canvas canv, boolean isSelected) {
            Paint paint = new Paint();
            paint.setStrokeWidth(8);
            paint.setStyle(Paint.Style.FILL);
            if (isSelected) {
                paint.setColor(Color.BLACK);
            } else {
                paint.setColor(Color.RED);
            }
            canv.drawCircle(mV.mP.x, mV.mP.y, VERTEX_RADIUS, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLACK);
            canv.drawCircle(mV.mP.x, mV.mP.y, VERTEX_RADIUS, paint);
        }

        @Override
        public String toString() {
            String vStr = mV != null ? String.valueOf(mV.mId) : "null";
            return "GraphVertex: [id: " + mId + ", v1: " + vStr +
                    ", num Edges: " + mEdges.size() + "]";
        }
    }
    private class GraphEdge {
        int mId;
        GraphVertex mV1;
        GraphVertex mV2;
        ArrayList<HalfEdge> mHes;
        ArrayList<Intersection> mIntersections;
        GraphEdge(GraphVertex v1, GraphVertex v2, int id) {
            mId = id;
            mV1 = v1;
            mV2 = v2;
            mHes = new ArrayList<>();
            mIntersections = new ArrayList<>();
        }
        public HalfEdge getIntersectingHalfEdge(GraphVertex v1, GraphVertex v2) {
            for (HalfEdge he : mHes) {
                if (Point.segmentsIntersect(
                        v1.mV.mP, v2.mV.mP,
                        he.mVertex.mP, he.mOpposite.mVertex.mP)) {
                    return he;
                }
            }
            return null;
        }
        public void updateSplitHalfEdge(HalfEdge heOriginal, Vertex vSlpit) {
            mHes.remove(heOriginal);
            for (HalfEdge he : vSlpit.getOutEdges()) {
                mHes.add(he);
            }
        }
        // TODO: misnommer. Only the first half of the condition checks this.
        public boolean isPartOfMinimumSpanningTree() {
            boolean inMinSpanningTree = false;
            for (HalfEdge he : mHes) {
                inMinSpanningTree |= he.mFace.mId == he.mOpposite.mFace.mId;
            }
            return inMinSpanningTree && (mV1.mEdges.size() > 1 && mV2.mEdges.size() > 1);
        }

        public void draw(Canvas canv, boolean isSelected) {
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            if (isSelected) {
                paint.setStrokeWidth(20);
            } else {
                paint.setStrokeWidth(10);
            }
            canv.drawLine(mV1.mV.mP.x, mV1.mV.mP.y, mV2.mV.mP.x,mV2.mV.mP.y, paint);
        }

        @Override
        public String toString() {
            String v1Str = mV1 != null ? String.valueOf(mV1.mId) : "null";
            String v2Str = mV2 != null ? String.valueOf(mV2.mId) : "null";
            return "GraphEdge: [id: " + mId + ", v1: " + v1Str + ", v2: " + v2Str +
                    ", num Edges: " + mHes.size() +
                    ", num Intersections: " + mIntersections.size() + "]";
        }
    }
    private class Intersection {
        int mId;
        Vertex mV;
        GraphEdge mGe1;
        GraphEdge mGe2;
        Point mP; // intersection point. Only used in transition, should be null when in graph
        HalfEdge mHe; // only used in transition should be null when in graph
        Intersection(Point intersection, HalfEdge he, GraphEdge ge) {
            mId = -1;
            mP = intersection;
            mGe1 = ge;
            mGe2 = ge;
            mHe = he;
        }
        Intersection(Vertex v, GraphEdge ge1, GraphEdge ge2, int id) {
            mId = id;
            mGe1 = ge1;
            mGe2 = ge2;
            mV = v;
        }
        public void draw(Canvas canv) {
            Paint paint = new Paint();
            paint.setStrokeWidth(8);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.WHITE);
            canv.drawCircle(mV.mP.x, mV.mP.y, VERTEX_RADIUS - 3, paint);
            paint.setColor(Color.BLACK);
            canv.drawCircle(mV.mP.x, mV.mP.y, VERTEX_RADIUS, paint);
        }

        @Override
        public String toString() {
            String vStr = mV != null ? mV.toString() : "null";
            String ge1Str = mGe1 != null ? String.valueOf(mGe1.mId) : "null";
            String ge2Str = mGe2 != null ? String.valueOf(mGe2.mId) : "null";
            return "Intersection: [id: " + mId + ", v: " + vStr +
                    ", ge1: " + ge1Str + ", ge2: " + ge2Str + "]";
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

    private ArrayList<GraphEdge> mGraphEdges;
    private ArrayList<GraphVertex> mGraphVertices;
    private ArrayList<Intersection> mGraphIntersections;


    MeshGraph() {
        clear();
    }
    @Override
    public void clear() {
        super.clear();
        mGraphEdges = new ArrayList<>();
        mGraphVertices = new ArrayList<>();
        mGraphIntersections = new ArrayList<>();
    }

    /*
    These functions pertain to adding elements to the MeshGraph
     */

    /**********   Vertices   ***********/
    private GraphVertex addGraphVertex(Vertex v) {
        GraphVertex vertex = new GraphVertex(v, mGraphVertices.size());
        mGraphVertices.add(vertex);
        return vertex;
    }
    private GraphVertex addGraphVertex(final int x, final int y, int skip) {
        validateGraph();
        Point p = new Point(x, y);
        if (pointOnAnyVertex(p, skip) >= 0) {
            Log.i(TAG, "Point already contained in Vertex. Not adding Vertex");
            return null;
        }
        Vertex v = addVertex(p);
        GraphVertex gv = addGraphVertex(v);
        validateGraph();
        return gv;
    }
    public int addGraphVertex(final int x, final int y) {
        validateGraph();
        GraphVertex v = addGraphVertex(x, y, -1);
        return v == null ? -1 : v.mId;
    }

    /**********   Edges   ***********/
    private GraphEdge addGraphEdge(GraphVertex v1, GraphVertex v2) {
        GraphEdge ge = new GraphEdge(v1, v2, mGraphEdges.size());
        mGraphEdges.add(ge);
        v1.mEdges.add(ge);
        v2.mEdges.add(ge);
        return ge;
    }
    private void addGraphEdge2(GraphVertex v1, GraphVertex v2) {
        validateGraph();
        if (v1.isNeighbor(v2)) return;
        ArrayList<Intersection> intersections = getIntersections(v1, v2);
        GraphEdge ge = addGraphEdge(v1, v2); // needs to be after getting intersections
        Collections.sort(intersections, new OrderByDistance(v1.mV.mP));
        Vertex v_prev = v1.mV;
        for (Intersection in: intersections) {
            Vertex v = splitEdge(in.mHe.mVertex, in.mHe.mOpposite.mVertex, in.mP);
            in.mGe1.updateSplitHalfEdge(in.mHe, v);
            HalfEdge he = addEdge(v_prev, v);
            ge.mHes.add(he);
            addIntersection(v, ge, in.mGe1);
            v_prev = v;
        }
        HalfEdge he = addEdge(v_prev, v2.mV);
        ge.mHes.add(he);
        validateGraph();
    }
    public void addGraphEdge(final int startVertexInd, final int endVertexInd) {
        GraphVertex v1 = mGraphVertices.get(startVertexInd);
        GraphVertex v2 = mGraphVertices.get(endVertexInd);
        validateGraph();
        addGraphEdge2(v1, v2);
        validateGraph();
    }


     /*
    End of section
     */



    private Intersection addIntersection(Vertex v, GraphEdge ge1, GraphEdge ge2) {
        Intersection gI = new Intersection(v, ge1, ge2, mGraphIntersections.size());
        mGraphIntersections.add(gI);
        ge1.mIntersections.add(gI);
        ge2.mIntersections.add(gI);
        return gI;
    }

    private ArrayList<Intersection> getIntersections(GraphVertex v1, GraphVertex v2) {
        ArrayList<Intersection> intersections = new ArrayList<>();
        for (GraphEdge ge : mGraphEdges) {
            HalfEdge he = ge.getIntersectingHalfEdge(v1, v2);
            if (he != null) {
                Point p = Point.lineIntersection(
                        v1.mV.mP, v2.mV.mP,
                        he.mVertex.mP, he.mOpposite.mVertex.mP);
                intersections.add(new Intersection(p, he, ge));
            }
        }
        return intersections;
    }

    public int pointOnAnyVertex(Point p) {
        return pointOnAnyVertex(p, -1);
    }
    private int pointOnAnyVertex(Point p, int skip) {
        for (int i = 0; i < mGraphVertices.size(); i++) {
            if (i == skip) continue;
            if (p.isInCircle(mGraphVertices.get(i).mV.mP, VERTEX_RADIUS)) {
                return i;
            }
        }
        return -1;
    }

    public void moveGraphVertex(final int vertexInd, int x, int y) {
        if (x < 0 || y < 0) return;
        GraphVertex v = mGraphVertices.get(vertexInd);
        GraphVertex vNew = addGraphVertex(x, y, vertexInd);
        if (vNew == null) return;
        for (GraphVertex vNeighbor : v.getNeighbors()) {
            addGraphEdge2(vNeighbor, vNew);
        }

//        ArrayList<GraphVertex> neighbors = v.getNeighbors();
        deleteGraphVertex(v);
//        if (!success) Log.i(TAG, "WHAT?"); // TODO: handle nicely even if it should never happen
//        int indNew = addGraphVertex(x, y);
//        if (indNew < 0) indNew = addGraphVertex((int)v.mV.mP.x, (int)v.mV.mP.y);
//        GraphVertex vNew = mGraphVertices.get(indNew);
//        for (GraphVertex vNeighbor : neighbors) {
//            addGraphEdge2(vNeighbor, vNew);
//        }
        // preserve index
        GraphVertex swapV = mGraphVertices.get(v.mId);
        swapV.mId = vNew.mId;
        mGraphVertices.set(vNew.mId, swapV);
        vNew.mId = v.mId;
        mGraphVertices.set(v.mId, vNew);
        validateGraph();
    }

    public void deleteGraphEdge(final int startVertexInd, final int endVertexInd) {
        validateGraph();
        GraphVertex v1 = mGraphVertices.get(startVertexInd);
        GraphVertex v2 = mGraphVertices.get(endVertexInd);
        GraphEdge ge = v1.getEdgeBetween(v2);
        deleteGraphEdge(ge);
        validateGraph();
    }
    private boolean deleteGraphEdge(GraphEdge ge) {
        validateGraph();
        if (ge == null || ge.isPartOfMinimumSpanningTree()) return false;
        ge.mV1.mEdges.remove(ge);
        ge.mV2.mEdges.remove(ge);
        for (HalfEdge he : ge.mHes) {
            deleteEdge(he);
        }
        for (Intersection in : ge.mIntersections) {
            GraphEdge geCross = in.mGe1 == ge ? in.mGe2 : in.mGe1;
            for (HalfEdge he : in.mV.getOutEdges()) {
                geCross.mHes.remove(he);
                geCross.mHes.remove(he.mOpposite);
            }
            HalfEdge he = contractVertex(in.mV);
            geCross.mHes.add(he);
            geCross.mIntersections.remove(in);
            removeIntersection(in);
        }
        removeGraphEdge(ge);
        validateGraph();
        return true;
    }
    public void deleteGraphVertex(final int vertexInd) {
        validateGraph();
        GraphVertex v = mGraphVertices.get(vertexInd);
        deleteGraphVertex(v);
    }
    private boolean deleteGraphVertex(GraphVertex v) {
        validateGraph();
        ArrayList<GraphEdge> toRemove = new ArrayList<>();
        for (GraphEdge ge : v.mEdges) {
            toRemove.add(ge);
        }
        boolean deleteAll = true;
        for (GraphEdge ge : toRemove) {
            deleteAll &= deleteGraphEdge(ge);
        }
        if (!deleteAll) {
            for (GraphEdge ge : toRemove) {
                addGraphEdge2(ge.mV1, ge.mV2);
            }
            return false;
        }
        deleteVertex(v.mV);
        removeGraphVertex(v);
        validateGraph();
        return true;
    }
    // TODO: add check that the element is still in the mesh
    private void removeGraphEdge(GraphEdge ge) {
        GraphEdge last = mGraphEdges.remove(mGraphEdges.size() - 1);
        if (last.mId == ge.mId) {
            return;
        }
        last.mId = ge.mId;
        mGraphEdges.set(last.mId, last);
    }
    private void removeIntersection(Intersection in) {
        Intersection last = mGraphIntersections.remove(mGraphIntersections.size() - 1);
        if (last.mId == in.mId) {
            return;
        }
        last.mId = in.mId;
        mGraphIntersections.set(last.mId, last);
    }
    private void removeGraphVertex(GraphVertex v) {
        GraphVertex last = mGraphVertices.remove(mGraphVertices.size() - 1);
        if (last.mId == v.mId) {
            return;
        }
        last.mId = v.mId;
        mGraphVertices.set(last.mId, last);
    }






    private HalfEdge addEdge(Vertex v1, Vertex v2) {
        validateMesh();
        if (edgeBetweenVertices(v1, v2) != null) {
            throw new AssertionError("THIS SHOULD NEVER GET HERE");
        }
        boolean v1Isolated = v1.isIsolated(); // Mark before adding edges
        boolean v2Isolated = v2.isIsolated(); // Mark before adding edges
        if (!v1Isolated && !v2Isolated) { // two connected vertices
            Face f = faceBetweenPoints(v1.mP, v2.mP);
            return splitFace(f, v1, v2); // validate is called before return inside function
        }
        // At least one vertex is isolated

        Face f = v1Isolated ? faceBetweenPoints(v1.mP) : faceBetweenPoints(v2.mP);
        Log.i(TAG, "Isolated edge is on face: " + f.toString());

        HalfEdge he1 = addHalfEdge(v1, v2, f);
        HalfEdge he2 = addHalfEdge(v2, v1, f);
        he1.mOpposite = he2;
        he2.mOpposite = he1;
        // Set next edges appropriately
        HalfEdge prev1 = v1Isolated ? he2 : findPreviousEdgeOnFace(v1, v2.mP, f);
        HalfEdge prev2 = v2Isolated ? he1 : findPreviousEdgeOnFace(v2, v1.mP, f);
        he2.mNext = prev1.mNext;
        prev1.mNext = he1;
        he1.mNext = prev2.mNext;
        prev2.mNext = he2;
        f.mHe = he1; // This step needs to be last be last
        validateMesh();
        return he1;
    }

    public void draw(final Canvas canv, int selectedVertexInd) {
        validateGraph();
        ArrayList<Integer> colors = new ArrayList<>(Arrays.asList(Color.YELLOW,
                Color.MAGENTA, Color.GREEN, Color.CYAN, Color.BLUE, Color.DKGRAY));
        for (Face f : mFaces) {
            int color = Color.LTGRAY;
            if (f.mId > 0) color = colors.get((f.mId-1)%colors.size());
            drawFace(canv, f, color);
        }
        for (GraphEdge ge : mGraphEdges) {
            ge.draw(canv, false);
        }
        for (GraphVertex gv : mGraphVertices) {
            gv.draw(canv, gv.mId == selectedVertexInd);
        }
        for (Intersection in : mGraphIntersections) {
            in.draw(canv);
        }
    }
    private void drawFace(final Canvas canv, Face f, int color) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        Path path = new Path();
        for (Vertex v : f.getVertices()) {
            if (path.isEmpty()) path.moveTo(v.mP.x, v.mP.y);
            else path.lineTo(v.mP.x, v.mP.y);
        }
        path.close();
        if (f.mId == 0) { // outer face
                path.toggleInverseFillType();
                RectF bounds = new RectF();
                path.computeBounds(bounds, true);
                canv.drawRect(0, bounds.bottom, canv.getWidth(), canv.getHeight(), paint);
                canv.drawRect(0, bounds.top, bounds.left, bounds.bottom, paint);
                canv.drawRect(0, 0, canv.getWidth(), bounds.top, paint);
                canv.drawRect(bounds.right, bounds.top, canv.getWidth(), bounds.bottom, paint);
        }
        canv.drawPath(path, paint);
    }

    /**
     * Validate that the GraphMesh is valid. If it isn't this will throw an AssertionError with
     * a message describing the problem encountered. In the middle of some methods, this is
     * expected to not be true. However, at the beginning and end of any public facing function,
     * this should always be true.
     */
    public void validateGraph() {
        try {
            validateMesh();
            // Isolated validations
            validateGraphEdges();
            validateGraphVertices();
            validateGraphIntersections();
            // Edge validations
            validateTwoEdgesPerIntersection();
            validateOneGraphEdgePerHalfEdge();
            validateHalfEdgesConnectIntersections();
            // Vertex validations
            validateTwoVerticesPerEdge();
            validateOneVertexPerGraphVertexOrIntersection();
        } catch (AssertionError e) {
            Log.i(TAG, toString());
            throw e;
        }
    }
    // Isolated validations
    private void validateGraphEdge(GraphEdge ge, String errorPrefix) {
        if (ge == null) throw new AssertionError(errorPrefix + "null graph edge");
        if (ge.mId < 0 || ge.mId >= mGraphEdges.size())
            throw new AssertionError(errorPrefix + ge.toString() +
                    " id is out of bounds of mGraphEdges (size: " + mGraphEdges.size() + ")");
        GraphEdge ge1 = mGraphEdges.get(ge.mId);
        if (ge != ge1) throw new AssertionError(errorPrefix + ge.toString() +
                " is not the halfedge found at index " + ge.mId + " in mEdges: " + ge1.toString());
        if (ge.mV1 == null)
            throw new AssertionError(errorPrefix + ge.toString() + " has no vertex 1 set");
        if (ge.mV2 == null)
            throw new AssertionError(errorPrefix + ge.toString() + " has no vertex 2 set");
        if (ge.mV1 == ge.mV2) throw new AssertionError(errorPrefix + ge.toString() + " has same " +
                "v1 and v2: " + ge.mV1.toString());
        if (ge.mHes.size() != ge.mIntersections.size() + 1)
            throw new AssertionError(errorPrefix + ge.toString() + " has " + ge.mHes.size() +
                    " halfedges and "+ ge.mIntersections.size() + " intersections. " +
                    "It should have exactly one less intersection than halfedge");
        for (HalfEdge he : ge.mHes) {
            validateHalfEdge(he, ge.toString() + " halfedge: ");
            validateHalfEdge(he.mOpposite, ge.toString() + " halfedge: ");
        }
        int vs[] = new int[mVertices.size()];
        String pathStr = "Edges:";
        for (HalfEdge he : ge.mHes) {
            pathStr += "\n" + he.toString();
            vs[he.mVertex.mId]++;
            vs[he.mOpposite.mVertex.mId]++;
        }
        if (vs[ge.mV1.mV.mId] != 1) throw new AssertionError(errorPrefix + ge.toString() +
                " has edges that loop back through v1" + ": " + pathStr);
        vs[ge.mV1.mV.mId]--; // don't validate on this for next step
        if (vs[ge.mV2.mV.mId] != 1) throw new AssertionError(errorPrefix + ge.toString() +
                " has edges that loop back through v2" + ": " + pathStr);
        vs[ge.mV2.mV.mId]--; // don't validate on this for next step
        for (int i = 0; i < mVertices.size(); i++) {
            if (vs[i] != 0 && vs[i] != 2)
                throw new AssertionError(errorPrefix + ge.toString() + " has \"line\" that " +
                        "passes " + vs[i] + " times through vertex " + i + ": " + pathStr);
        }
    }
    private void validateGraphVertex(GraphVertex v, String errorPrefix) {
        if (v == null) throw new AssertionError(errorPrefix + "null graph vertex");
        if (v.mId < 0 || v.mId >= mGraphVertices.size())
            throw new AssertionError(errorPrefix + v.toString() +
                    " id is out of bounds of mGraphVertices (size: " + mGraphVertices.size() + ")");
        GraphVertex v1 = mGraphVertices.get(v.mId);
        if (v != v1) throw new AssertionError(errorPrefix + v.toString() + " is not the vertex" +
                " found at index " + v.mId + " in mGraphVertices: " + v1.toString());
        validateVertex(v.mV, errorPrefix + v.toString() + " vertex: ");
    }
    private void validateIntersection(Intersection in, String errorPrefix) {
        if (in == null) throw new AssertionError(errorPrefix + "null intersection");
        if (in.mId < 0 || in.mId >= mGraphIntersections.size())
            throw new AssertionError(errorPrefix + in.toString() + " id is out of bounds of " +
                    "mGraphIntersections (size: " + mGraphIntersections.size() + ")");
        Intersection in1 = mGraphIntersections.get(in.mId);
        if (in != in1) throw new AssertionError(errorPrefix + in.toString() + " is not the " +
                "intersection found at index " + in.mId + " in mIntersections: " + in1.toString());
        if (in.mV == null)
            throw new AssertionError(errorPrefix + in.toString() + " has no vertex set");
        validateVertex(in.mV, errorPrefix + in.toString() + " vertex: ");
        if (in.mGe1 == null)
            throw new AssertionError(errorPrefix + in.toString() + " has no first edge set");
        if (in.mGe2 == null)
            throw new AssertionError(errorPrefix + in.toString() + " has no second edge set");
        if (in.mHe != null) throw new AssertionError(errorPrefix + in.toString() +
                " should not have a halfedge set but does: " + in.mHe.toString());
        if (in.mP != null)
            throw new AssertionError(errorPrefix + in.toString() +
                    " should not have a point set but does: " + in.mP.toString());
    }
    private void validateGraphEdges() {
        for (GraphEdge ge : mGraphEdges) {
            validateGraphEdge(ge, "");
            validateGraphVertex(ge.mV1, ge.toString() + " v1: ");
            validateGraphVertex(ge.mV2, ge.toString() + " v2: ");
            for (Intersection in : ge.mIntersections) {
                validateIntersection(in, ge.toString() + " intersection: ");
                if (in.mGe1 != ge && in.mGe2 != ge)
                    throw new AssertionError(ge.toString() + " contains intersection " + in.mId +
                            " but is not part of that intersection: " + in.toString());
            }
            if (!ge.mV1.mEdges.contains(ge)) throw new AssertionError(ge.toString() +
                    " v1 does not contain edge: " + ge.mV1.toString());
            if (!ge.mV2.mEdges.contains(ge)) throw new AssertionError(ge.toString() +
                    " v2 does not contain edge" + ge.mV2.toString());
        }
    }
    private void validateGraphVertices() {
        for (GraphVertex gv : mGraphVertices) {
            validateGraphVertex(gv, "");
            for (GraphEdge ge : gv.mEdges) {
                validateGraphEdge(ge, gv.toString() + " edge: ");
                if (ge.mV1 != gv && ge.mV2 != gv)
                    throw new AssertionError(gv.toString() + " contains edge " + ge.mId +
                            " but is not part of that edge: " + ge.toString());
            }
        }
    }
    private void validateGraphIntersections() {
        for (Intersection in : mGraphIntersections) {
            validateIntersection(in, "");
            validateGraphEdge(in.mGe1, in.toString() + " first edge: ");
            validateGraphEdge(in.mGe2, in.toString() + " second edge: ");
            if (!in.mGe1.mIntersections.contains(in)) throw new AssertionError(in.toString() +
                    " first edge does not contain intersection");
            if (!in.mGe2.mIntersections.contains(in)) throw new AssertionError(in.toString() +
                    " second edge does not contain intersection");
            if (!Point.segmentsIntersect(
                    in.mGe1.mV1.mV.mP, in.mGe1.mV2.mV.mP,
                    in.mGe2.mV1.mV.mP, in.mGe2.mV2.mV.mP))
                throw new AssertionError(in.toString() + " edges don't actually intersect");
            Point p = Point.lineIntersection(in.mGe1.mV1.mV.mP, in.mGe1.mV2.mV.mP,
                    in.mGe2.mV1.mV.mP, in.mGe2.mV2.mV.mP);
            if (!in.mV.mP.equals(p)) throw new AssertionError(in.toString() + " has point that " +
                    "is not actually the intersection of its two edges: " + p.toString());
        }
    }
    // Edge validations
    private void validateTwoEdgesPerIntersection() {
        int[] ins = new int[mGraphIntersections.size()];
        for (GraphEdge ge : mGraphEdges) {
            for (Intersection in : ge.mIntersections) {
                ins[in.mId]++;
            }
        }
        for (int i = 0; i < mGraphIntersections.size(); i++) {
            if (ins[i] != 2) throw new AssertionError(mGraphIntersections.get(i) + " should be " +
                    "associated with exactly 2 edges but instead is associated with " + ins[i]);
        }
    }
    private void validateOneGraphEdgePerHalfEdge() {
        int[] hes = new int[mEdges.size()];
        for (GraphEdge ge : mGraphEdges) {
            for (HalfEdge he : ge.mHes) {
                hes[he.mId]++;
                hes[he.mOpposite.mId]++;
            }
        }
        for (int i = 0; i < mEdges.size(); i++) {
            if (hes[i] != 1) throw new AssertionError(mEdges.get(i) + " should be associated " +
                    "with exactly 1 graph edge but instead is associated with " + hes[i]);
        }
    }
    private void validateHalfEdgesConnectIntersections() {
        for (GraphEdge ge : mGraphEdges) {
            Set<Vertex> vs = new HashSet<>();
            String edgesStr = "HalfEdges:";
            for (HalfEdge he : ge.mHes) {
                edgesStr += "\n" + he.toString();
                vs.add(he.mVertex);
                vs.add(he.mOpposite.mVertex);
            }
            vs.remove(ge.mV1.mV);
            vs.remove(ge.mV2.mV);
            if (vs.size() != ge.mIntersections.size())
                throw new AssertionError(ge.toString() + " has " + ge.mIntersections.size() +
                        " intersections but " + vs.size() + " intermediate vertices");
            for (Intersection in : ge.mIntersections) {
                if (!vs.contains(in.mV))
                    throw new AssertionError(ge.toString() + " has an intersection which doesn't " +
                            "correspond to an intermediate vertex: " + in.toString() + "\n" + edgesStr);
            }
        }
    }
    // Vertex validations
    private void validateTwoVerticesPerEdge() {
        int[] ges = new int[mGraphEdges.size()];
        for (GraphVertex gv : mGraphVertices) {
            for (GraphEdge ge : gv.mEdges) {
                ges[ge.mId]++;
            }
        }
        for (int i = 0; i < mGraphEdges.size(); i++) {
            if (ges[i] != 2) throw new AssertionError(mGraphEdges.get(i) + " should be " +
                    "associated with exactly 2 vertices but instead is associated with " + ges[i]);
        }
    }
    private void validateOneVertexPerGraphVertexOrIntersection() {
        int[] vs = new int[mVertices.size()];
        for (GraphVertex gv : mGraphVertices) {
            vs[gv.mV.mId]++;
        }
        for (Intersection in : mGraphIntersections) {
            vs[in.mV.mId]++;
        }
        for (int i = 0; i < mVertices.size(); i++) {
            if (vs[i] != 1)
                throw new AssertionError(mVertices.get(i) + " should be associated with exactly " +
                        "1 graph vertex or intersection but instead is associated with " + vs[i]);
        }
    }
}
