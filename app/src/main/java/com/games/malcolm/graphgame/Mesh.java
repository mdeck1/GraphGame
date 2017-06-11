package com.games.malcolm.graphgame;

import android.util.Log;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * Implementation of a 2D halfedge Mesh. Using this will simplify some visualization and operation logic
 *
 * Created by Thomas on 3/17/17.
 */

public class Mesh {
    private static final String TAG = "Mesh";

    protected static class Vertex {
        public Point mP;
        public int mId;
        public HalfEdge mHe;

        Vertex(Point p, int id) {
            mP = p;
            mId = id;
        }

        public ArrayList<HalfEdge> getOutEdges() {
            ArrayList<HalfEdge> hes = new ArrayList<>();
            if (mHe == null) return hes;
            return mHe.getOppositeNextLoop();
        }
        public ArrayList<HalfEdge> getInEdges() {
            ArrayList<HalfEdge> hes = new ArrayList<>();
            for (HalfEdge he : getOutEdges()) {
                hes.add(he.mOpposite);
            }
            return hes;
        }
        public ArrayList<Vertex> getNeighbors() {
            ArrayList<Vertex> vs = new ArrayList<>();
            for (HalfEdge he : getOutEdges()) {
                vs.add(he.mVertex);
            }
            return vs;
        }

        public boolean isIsolated() {
            return mHe == null;
        }

//        public void moveTo(int x, int y) {
//            mP.x = x;
//            mP.y = y;
//        }
        @Override
        public String toString() {
            String pStr = mP != null ? mP.toString() : "null";
            String heStr = mHe != null ? String.valueOf(mHe.mId) : "null";
            return "Vertex: [id: " + mId +", p: " + pStr + ", he: " + heStr + "]";
        }
    }
    protected static class Face {
        public int mId;
        public HalfEdge mHe;
        Face(int id) {
            mId = id;
        }

        public boolean isOuterFace() {
            return mId == 0;
        }
        public ArrayList<HalfEdge> getEdges() {
            ArrayList<HalfEdge> hes = new ArrayList<>();
            if (mHe == null) return hes;
            return mHe.getNextLoop();
        }
        public ArrayList<Vertex> getVertices() {
            ArrayList<Vertex> vs = new ArrayList<>();
            for (HalfEdge he : getEdges()) {
                vs.add(he.mVertex);
            }
            return vs;
        }

        // Use crossings method. Count number of crossings with horizontal ray to the right.
        // This is undefined when the point is on the edge of the face.
        public boolean containsPoint(Point p) {
            int crossings = 0;
            for (HalfEdge he : getEdges()) {
                Point p1 = he.mVertex.mP;
                Point p2 = he.mOpposite.mVertex.mP;
                boolean cond1 = (p1.y <= p.y) && (p2.y > p.y);
                boolean cond2 = (p2.y <= p.y) && (p1.y > p.y);
                if (!(cond1 || cond2)) continue; // no crossing
                // Is crossing to the right of p
                float m = (p1.y - p2.y) / (p1.x - p2.x);
                if (p1.x + (p.y - p1.y) / m > p.x) crossings++;
            }
            return crossings % 2 == 1; // odd number of crossings
        }
        public boolean containsFace(Face f) {
            Set<Vertex> vs = new HashSet<>(getVertices());
            Set<Vertex> fvs = new HashSet<>(f.getVertices());
            for (Vertex v : fvs) {
                if (vs.contains(v)) continue; // shared vertex not relevant
                if (!containsPoint(v.mP)) return false; // doesn't contain all of points of f
            }
            for (Vertex v : vs) {
                if (fvs.contains(v)) continue; // shared vertex not relevant
                if (f.containsPoint(v.mP)) return false; // at least partially contained by f
            }
            return true;
        }

        @Override
        public String toString() {
            String heStr = mHe != null ? String.valueOf(mHe.mId) : "null";
            return "Face: [id: " + mId +", he: "+ heStr + "]";
        }
    }
    protected static class HalfEdge {
        public int mId;
        public Vertex mVertex;
        public HalfEdge mNext;
        public HalfEdge mOpposite;
        public Face mFace;
        HalfEdge(Vertex vertex, Face face, int id) {
            mId = id;
            mVertex = vertex;
            mFace = face;
        }

        public ArrayList<HalfEdge> getNextLoop() {
            ArrayList<HalfEdge> hes = new ArrayList<>();
            HalfEdge he = this;
            do {
                hes.add(he);
                he = he.mNext;
            } while (he.mId != mId);
            return hes;
        }
        public ArrayList<HalfEdge> getOppositeNextLoop() {
            ArrayList<HalfEdge> hes = new ArrayList<>();
            HalfEdge he = this;
            do {
                hes.add(he);
                he = he.mOpposite.mNext;
            } while (he.mId != mId);
            return hes;
        }

        @Override
        public String toString() {
            String vStr = mVertex != null ? String.valueOf(mVertex.mId) : "null";
            String oStr = mOpposite != null ? String.valueOf(mOpposite.mId) : "null";
            String nStr = mNext != null ? String.valueOf(mNext.mId) : "null";
            String fStr = mFace != null ? String.valueOf(mFace.mId) : "null";
            return "HalfEdge: [id: " + mId +", v: "+ vStr + ", opposite: " + oStr
                    + ", next: "+ nStr + ", face: " + fStr + "]";
        }
    }

    public ArrayList<HalfEdge> mEdges;
    public ArrayList<Vertex> mVertices;
    public ArrayList<Face> mFaces;

    Mesh() {
        clear();
    }

    public Vertex addVertex(Point p) {
        Vertex vertex = new Vertex(p, mVertices.size());
        mVertices.add(vertex);
        return vertex;
    }

    public Face addFace() {
        Face face = new Face(mFaces.size());
        mFaces.add(face);
        return face;
    }

    public HalfEdge addHalfEdge(Vertex from, Vertex to, Face face) {
        HalfEdge he = new HalfEdge(to, face, mEdges.size());
        mEdges.add(he);
        from.mHe = he;
        return he;
    }

    protected Face faceBetweenPoints(Point... ps) {
        Point avg = Point.average(ps);
        for(Face f : mFaces) {
            if (f.mId == 0) continue; // skip outer face
            if (f.containsPoint(avg)) {
                return f;
            }
        }
        return mFaces.get(0);
    }

    public Face splitFace(Face f1, Vertex v1, Vertex v2) {
        validate();        Face f2 = addFace();
        HalfEdge he1 = addHalfEdge(v1, v2, f1);
        HalfEdge he2 = addHalfEdge(v2, v1, f2);
        he1.mOpposite = he2;
        he2.mOpposite = he1;
        // Find previous edges and set all next fields appropriately
        HalfEdge he1_prev = findPreviousEdgeOnFace(v1, v2.mP, f1);
        HalfEdge he2_prev = findPreviousEdgeOnFace(v2, v1.mP, f1);
        he2.mNext = he1_prev.mNext;
        he1.mNext = he2_prev.mNext;
        he1_prev.mNext = he1;
        he2_prev.mNext = he2;

        f1.mHe = he1;
        f2.mHe = he2;
        // Fix which edge the faces point to if necessary.
        // As far as I can see, we only care for drawing purposes...
        if (f1.isOuterFace() && !f1.containsFace(f2)) {
            f1.mHe = he2;
            f2.mHe = he1;
        }

        // set faces for all edges
        for (HalfEdge he : f1.getEdges()) {
            he.mFace = f1;
        }
        for (HalfEdge he : f2.getEdges()) {
            he.mFace = f2;
        }
        validate();
        return f2;
    }

    public Vertex splitEdge(Vertex v1, Vertex v2, Point p) {
        validate();
        Vertex v3 = addVertex(p);
        HalfEdge he1 = edgeBetweenVertices(v1, v2);
        HalfEdge he2 = he1.mOpposite;
        HalfEdge he3 = addHalfEdge(v3, v2, he1.mFace);
        HalfEdge he4 = addHalfEdge(v3, v1, he2.mFace);
        // Set Vertex for split edges
        he1.mVertex = v3;
        he2.mVertex = v3;
        // Set Next for edges
        he3.mNext = he1.mNext;
        he1.mNext = he3;
        he4.mNext = he2.mNext;
        he2.mNext = he4;
        // Set opposite for edges
        he1.mOpposite = he4;
        he4.mOpposite = he1;
        he3.mOpposite = he2;
        he2.mOpposite = he3;
        validate();
        return v3;
    }

    public HalfEdge edgeBetweenVertices(Vertex v1, Vertex v2) {
        for (HalfEdge he : v1.getOutEdges()) {
            if (he.mVertex.mId == v2.mId) return he;
        }
        return null; // No edge between v1 and v2
    }

    protected HalfEdge findPreviousEdgeOnFace(Vertex v, Point p, Face f) {
        ArrayList<HalfEdge> candidates = new ArrayList<>();
        for (HalfEdge he : f.getEdges()) {
            if (he.mVertex.mId == v.mId) {
//                return he;
                candidates.add(he);
            }
        }
        return pickCandidateByNearestAngle(v, p, candidates);
    }

    private HalfEdge pickCandidateByNearestAngle(Vertex v, Point p, ArrayList<HalfEdge> candidates) {
        HalfEdge best = null;
        float bestAngle = Integer.MAX_VALUE; // TODO: figure out which direction and validate
        for (HalfEdge he : candidates) {
            float angle = v.mP.angleBetweenPoints(he.mOpposite.mVertex.mP, p);
            if (angle < bestAngle) {
                best = he;
                bestAngle = angle;
            }
        }
        return best;
    }

    public void clear() {
        mEdges = new ArrayList<>();
        mVertices = new ArrayList<>();
        mFaces = new ArrayList<>();
        addFace(); // outerFace
    }

    /**
     * Run on mesh as a check that it is indeed valid. In the middle of some methods,
     * this is expected to not be true. However, at the beginning of any public facing function,
     * this should be true.
     * If the mesh is not valid, an AssertionError is thrown with message describing the first
     * problem encountered.
     */
    public void validate() {
        // Isolated validations
        validateIndividualHalfEdges();
        validateIndividualVertices();
        validateIndividualFaces();
        // Edge validations
        validateHalfEdgesNextUnique();
        validateHalfEdgeNextLoops();
        validateHalfEdgeOppositeNextLoops();
        validateNoEdgesCross();
        // Vertex validations
        validateNoVerticesTheSame();
        validateHalfEdgesAroundVertices();
        validateVerticesAllConnected();
        // Face validations
        validateHalfEdgesAroundFaces();
        validateFacesAllContainedByOuterFace();

        // 0 intersections ?
    }

    private void validateIndividualHalfEdges() {
        for (int i = 0; i < mEdges.size(); i++) {
            HalfEdge he = mEdges.get(i);
            if (he == null) throw new AssertionError("null halfedge");
            if (he.mId != i)
                throw new AssertionError(he.toString()
                        + " should have an id equal to its index: " + i);
            if (he.mVertex == null) throw new AssertionError(he.toString() + " has no vertex set");
            if (he.mOpposite == null)
                throw new AssertionError(he.toString() + " has no opposite edge set");
            if (he.mNext == null)
                throw new AssertionError(he.toString() + " has no next edge set");
            if (he.mFace == null) throw new AssertionError(he.toString() + " has no face set");
            if (he.mOpposite.mOpposite != he)
                throw new AssertionError(he.toString() + " opposite doesn't point back to itself."
                        + "\nopposite edge: " + he.mOpposite.toString());
            if (he.mNext.mOpposite == null) // Check this to avoid null reference
                throw new AssertionError(he.mNext.toString() + " has no opposite edge set");
            if (he.mNext.mOpposite.mVertex != he.mVertex)
                throw new AssertionError(he.toString() + " next edge doesn't come from same vertex."
                        + "\nopposite edge of next edge: " + he.mNext.mOpposite.toString());
        }
    }
    private void validateIndividualVertices() {
        for (int i = 0; i < mVertices.size(); i++) {
            Vertex v = mVertices.get(i);
            if (v == null) throw new AssertionError("null vertex");
            if (v.mId != i) throw new AssertionError(v.toString()
                    + " should have an id equal to its index: " + i);
            if (v.mP == null) throw new AssertionError(v.toString() + " has no point set");
            if (v.mP.x < 0 || v.mP.y < 0)
                throw new AssertionError(v.toString() + " has point outside of 1st quadrant: ");
            if (v.isIsolated()) continue; // No more checks needed if this vertex is isolated
            if (v.mHe.mOpposite == null) // Check this to avoid null reference
                throw new AssertionError(v.mHe.toString() + " has no opposite edge set");
            if (v.mHe.mOpposite.mVertex != v)
                throw new AssertionError(v.toString() + " edge opposite doesn't point back to "
                        + "itself.\nvertex edge: " + v.mHe.toString());
        }
    }
    private void validateIndividualFaces() {
        for (int i = 0; i < mFaces.size(); i++) {
            Face f = mFaces.get(i);
            if (f == null) throw new AssertionError("null face");
            if (f.mId != i)
                throw new AssertionError(f.toString()
                        + " should have an id equal to its index: " + i);
            if (mEdges.isEmpty()) continue;
            if (f.mHe == null) throw new AssertionError(f.toString() + " has no edge set");
            if (f.mHe.mFace != f)
                throw new AssertionError(f.toString() + " has edge that doesn't point back to "
                        + "it.\nface edge: " + f.mHe.toString());
        }
    }
    private void validateHalfEdgesNextUnique() {
        // Half edges well formed in mesh
        boolean hes[] = new boolean[mEdges.size()];
        // Validate that next is an injective function
        // (actually bijective since input and output sets are the same)
        for (HalfEdge he : mEdges) {
            if (hes[he.mNext.mId]) throw new AssertionError(he.mNext.toString() + " is next of "
                    + he.toString() + " and some other edge");
            hes[he.mNext.mId] = true;
        }
    }
    private void validateHalfEdgeNextLoops() {
        if (mEdges.isEmpty()) return;
        boolean[] hes = new boolean[mEdges.size()];
        boolean[] fs = new boolean[mFaces.size()];
        for (HalfEdge he : mEdges) {
            if (hes[he.mId]) { continue; }
            if (fs[he.mFace.mId])
                throw new AssertionError("Two separate loops point to the same face: "
                        + he.mFace.toString());
            fs[he.mFace.mId] = true;
            HalfEdge he_iter = he;
            String loopStr = "Loop:";
            do {
                loopStr += "\n" + he_iter.toString();
                if (hes[he_iter.mId])
                    throw new AssertionError("Found loop with starting tail " +
                            "(will result in infinite loop): " + loopStr);
                hes[he_iter.mId] = true;
                he_iter = he_iter.mNext;
            } while (he_iter.mId != he.mId);
        }
        // Check there is one loop per face
        for (int i = 0; i < mFaces.size(); i++) {
            if (!fs[i]) throw new AssertionError(mFaces.get(i).toString()
                    + " is not associated with any loop");
        }
    }
    private void validateHalfEdgeOppositeNextLoops() {
        if (mEdges.isEmpty()) return;
        boolean[] hes = new boolean[mEdges.size()];
        boolean[] vs = new boolean[mVertices.size()];
        for (HalfEdge he : mEdges) {
            if (hes[he.mId]) { continue; }
            if (vs[he.mOpposite.mVertex.mId])
                throw new AssertionError("Two separate loops point are around the same point: "
                        + he.mOpposite.mVertex.toString());
            vs[he.mOpposite.mVertex.mId] = true;
            HalfEdge he_iter = he;
            String loopStr = "Loop:";
            do {
                loopStr += "\n" + he_iter.toString();
                if (hes[he_iter.mId])
                    throw new AssertionError("Found loop with starting tail " +
                            "(will result in infinite loop): " + loopStr);
                hes[he_iter.mId] = true;
                he_iter = he_iter.mOpposite.mNext;
            } while (he_iter.mId != he.mId);
        }
        // Check there is one loop per vertex
        for (int i = 0; i < mVertices.size(); i++) {
            if (mVertices.get(i).isIsolated()) continue;
            if (!vs[i]) throw new AssertionError(mVertices.get(i).toString()
                    + " is not associated with any loop");
        }
    }
    private void validateHalfEdgesAroundFaces() {
        // This might be completely redundant with previous validations
        boolean hes[] = new boolean[mEdges.size()];
        for (Face f : mFaces) {
            for(HalfEdge he : f.getEdges()) {
                if (hes[he.mId]) throw new AssertionError("Loop around " + f.toString()
                        + " contains " + he.toString() + " which points to " + he.mFace.toString());
                hes[he.mId] = true;
                if (he.mFace != f) throw new AssertionError(he.toString()
                        + " is in loop associated with " + f.toString()
                        + " but points to " + he.mFace.toString());
            }
        }
    }
    private void validateHalfEdgesAroundVertices() {
        // This might be completely redundant with previous validations
        boolean hes[] = new boolean[mEdges.size()];
        for (Vertex v : mVertices) {
            for(HalfEdge he : v.getInEdges()) {
                if (hes[he.mId]) throw new AssertionError("Loop around " + v.toString()
                        + " contains " + he.toString() + " which points to "
                        + he.mVertex.toString());
                hes[he.mId] = true;
                if (he.mVertex != v) throw new AssertionError(he.toString()
                        + " is in loop associated with " + v.toString()
                        + " but points to " + he.mVertex.toString());
            }
        }
    }
    private void validateVerticesAllConnected() {
        if (mVertices.isEmpty()) return;
        // Use BFS to mark all vertices connected to first Vertex
        boolean vs[] = new boolean[mVertices.size()];
        ArrayList<Vertex> children = new ArrayList<>();
        for (Vertex v : mVertices) {
            if (!v.isIsolated()) {
                children.add(v);
                break;
            }
        }
        while (!children.isEmpty()) {
            Vertex v = children.remove(0);
            vs[v.mId] = true;
            for (Vertex child : v.getNeighbors()) {
                if (!vs[child.mId]) {
                    children.add(child);
                }
            }
        }
        for (int i = 0; i < vs.length; i++) {
            Vertex v = mVertices.get(i);
            if (v.isIsolated()) continue;
            if (!vs[i]) throw new AssertionError(v.toString()
                    + " is not connected to " + mVertices.get(0).toString());
        }
    }
    private void validateFacesAllContainedByOuterFace() {
        Face outerFace = mFaces.get(0);
        for (Face f : mFaces) {
            if (f.mId == 0) continue;
            if (!outerFace.containsFace(f)) throw new AssertionError(f.toString()
                    + " not contained by outer face.");
        }
    }
    private void validateNoVerticesTheSame() {
        for (Vertex v1 : mVertices) {
            for (Vertex v2 : mVertices) {
                if (v1.mId == v2.mId) continue;
                if ((v1.mP.x == v2.mP.x) && (v1.mP.y == v2.mP.y))
                    throw new AssertionError(v1.toString() + " is the same as " + v2.toString());
            }
        }
    }
    private void validateNoEdgesCross() {
        for (HalfEdge he1 : mEdges) {
            for (HalfEdge he2 : mEdges) {
                if (he1.mId == he2.mId) continue;
                if (Point.segmentsIntersect(he1.mVertex.mP,he1.mOpposite.mVertex.mP,
                        he2.mVertex.mP, he2.mOpposite.mVertex.mP))
                    throw new AssertionError(he1.toString() + " intersects with " + he2.toString());
            }
        }
    }

    @Override
    public String toString() {
        String str = "Mesh: [vertices: [";
        for (Vertex vertex : mVertices) {
            str += "\n" + vertex;
        }
        str += "]\n";
        if (!mEdges.isEmpty()) {
            str += "edges: [";
            for (HalfEdge he: mEdges) {
                str += "\n" + he;
            }
            str += "]\n";
        }
        if (!mFaces.isEmpty()) {
            str += "faces: [";
            for (Face face : mFaces) {
                str += "\n" + face;
            }
            str += "]\n";
        }
        str +=  "]";
        return str;
    }
}
