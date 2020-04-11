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
        public int mId;
        public Point mP;
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

        public boolean touchesEdge(HalfEdge he) {
            return getEdges().contains(he);
        }
        public boolean touchesVertex(Vertex v) {
            return getVertices().contains(v);
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
    public void clear() {
        mEdges = new ArrayList<>();
        mVertices = new ArrayList<>();
        mFaces = new ArrayList<>();
        addFace(); // outerFace
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

    public HalfEdge splitFace(Face f1, Vertex v1, Vertex v2) {
        /*** Assertions ***/
        validateMesh(); // valid mesh
        if (!f1.touchesVertex(v1))
            throw new AssertionError(v1.toString() + " does not touch " + f1.toString());
        if (!f1.touchesVertex(v2))
            throw new AssertionError(v2.toString() + " does not touch " + f1.toString());
        // edge(v1, v2) does not intersect any existing edges
        if (edgeBetweenVertices(v1, v2) != null)
            throw new AssertionError(v1.toString() + " and " + v2.toString() + " are already " +
                    "connected by an edge: " + edgeBetweenVertices(v1, v2).toString());
        /*** ***/

        Face f2 = addFace();
        HalfEdge he1 = addHalfEdge(v1, v2, f1);
        HalfEdge he2 = addHalfEdge(v2, v1, f2);
        he1.mOpposite = he2;
        he2.mOpposite = he1;
        // Find previous edges and set all next fields appropriately
        HalfEdge he1_prev = findPreviousEdgeOnFace(v1, v2.mP, f1);
        HalfEdge he2_prev = findPreviousEdgeOnFace(v2, v1.mP, f1);
        if (he1_prev == null)
            throw new AssertionError("he1_prev is null. Nothing was caught in the assumptions so " +
                    "there is probably a bug in the splitFace function.");
        if (he2_prev == null)
            throw new AssertionError("he2_prev is null. Nothing was caught in the assumptions so " +
                    "there is probably a bug in the splitFace function.");

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
        validateMesh();
        return he1;
    }
    public Vertex splitEdge(Vertex v1, Vertex v2, Point p) {
        validateMesh();
        if (edgeBetweenVertices(v1, v2) == null) { return null; }
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
        validateMesh();
        return v3;
    }

    public void deleteEdge(HalfEdge he1) {
        if (he1 == null) {
            return;
        }
        validateMesh();
        if (he1.mFace.mId > he1.mOpposite.mFace.mId) {
            he1 = he1.mOpposite; // switch so that f1 < f2 (i.e f2 not outer face)
        }
        HalfEdge he2 = he1.mOpposite;
        Face f1 = he1.mFace;
        Face f2 = he2.mFace;
        Vertex v1 = he1.mVertex;
        Vertex v2 = he2.mVertex;

        HalfEdge he1_prev = findPreviousEdgeOnFace(he1, f1);
        HalfEdge he2_prev = findPreviousEdgeOnFace(he2, f2);

        // Nothing has been changed at this point

        if (f1.mId != f2.mId) { // Edge used to split two faces
            // Reset all edge faces and remove f2
            for (HalfEdge he : f2.getEdges()) {
                he.mFace = f1;
            }
            removeFace(f2);
        } else if (v1.getOutEdges().size() > 1 && v2.getOutEdges().size() > 1) {
            Log.i(TAG, "Not creating two connected components. Skip delete Edge");
            return;
        }

        // remove potential pointers from vertex
        v1.mHe = he1.mNext.mId == he2.mId ? null : he1.mNext;
        v2.mHe = he2.mNext.mId == he1.mId ? null : he2.mNext;

        for (HalfEdge he : f1.getEdges()) {
            if (he.mId != he1.mId && he.mId != he2.mId) {
                f1.mHe = he;
                break;
            } else {
                f1.mHe = null;
            }
        }

        // reset next edges and potential pointers to edges, then remove edges
        he1_prev.mNext = he2.mNext;
        he2_prev.mNext = he1.mNext;

        removeEdge(he1);
        removeEdge(he2);
        validateMesh();
    }
    public void deleteVertex(Vertex v) {
        if (v.getOutEdges().size() == 0) {
            removeVertex(v);
            return;
        }
    }
    public HalfEdge contractVertex(Vertex v) {
        ArrayList<HalfEdge> hes = v.getOutEdges();
        if (hes.size() != 2) return null;
        validateMesh();
        HalfEdge he1 = hes.get(0);
        HalfEdge he2 = hes.get(1);

        HalfEdge he1_opp_prev = findPreviousEdgeOnFace(he1.mOpposite, he1.mOpposite.mFace);
        HalfEdge he2_opp_prev = findPreviousEdgeOnFace(he2.mOpposite, he2.mOpposite.mFace);

        removeEdge(he1.mOpposite);
        removeEdge(he2.mOpposite);
        he1_opp_prev.mNext = he2;
        he2_opp_prev.mNext = he1;

        he1.mOpposite = he2;
        he2.mOpposite = he1;
        he1.mVertex.mHe = he2;
        he2.mVertex.mHe = he1;

        he1.mFace.mHe = he1;
        he2.mFace.mHe = he2;

        removeVertex(v);
        validateMesh();
        return he1;
    }

    // TODO: add check that the element is still in the mesh
    private void removeFace(Face f) {
        Face last = mFaces.remove(mFaces.size() - 1);
        if (last.mId == f.mId) {
            return;
        }
        last.mId = f.mId;
        mFaces.set(last.mId, last);
    }
    private void removeEdge(HalfEdge he) {
        HalfEdge last = mEdges.remove(mEdges.size() - 1);
        if (last.mId == he.mId) {
            return;
        }
        last.mId = he.mId;
        mEdges.set(last.mId, last);
    }
    private void removeVertex(Vertex v) {
        Vertex last = mVertices.remove(mVertices.size() - 1);
        if (last.mId == v.mId) {
            return;
        }
        last.mId = v.mId;
        mVertices.set(last.mId, last);
    }


    public HalfEdge edgeBetweenVertices(Vertex v1, Vertex v2) {
        for (HalfEdge he : v1.getOutEdges()) {
            if (he.mVertex.mId == v2.mId) return he;
        }
        return null; // No edge between v1 and v2
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

    protected HalfEdge findPreviousEdgeOnFace(HalfEdge next, Face f) {
        /*** Assertions ***/
        validateMesh();
        if (!f.touchesEdge(next))
            throw new AssertionError(next.toString() + " does not touch " + f.toString());
        /*** ***/
        for (HalfEdge he : f.getEdges()) {
            if (he.mNext.mId == next.mId) {
                return he;
            }
        }
        return null;
    }
    protected HalfEdge findPreviousEdgeOnFace(Vertex v, Point p, Face f) {
        /*** Assertions ***/
        validateMesh(); // valid mesh
        if (!f.touchesVertex(v))
            throw new AssertionError(v.toString() + " does not touch " + f.toString());
//        if (!f.containsPoint(p))
//            throw new AssertionError(f.toString() + " does not contain " + p.toString());
        /*** ***/
        ArrayList<HalfEdge> candidates = new ArrayList<>();
        for (HalfEdge he : f.getEdges()) {
            if (he.mVertex.mId == v.mId) {
                candidates.add(he);
            }
        }
        if (candidates.isEmpty())
            throw new AssertionError("candidates list is empty. " +
                    "There is a bug in FindPreviousEdgeOnFace");
        return pickCandidateByNearestAngle(v, p, candidates);
    }
    private HalfEdge pickCandidateByNearestAngle(Vertex v, Point p, ArrayList<HalfEdge> candidates) {
        Log.i(TAG, "Number of candidates: " + String.valueOf(candidates.size()));
        HalfEdge best = null;
        float bestAngle = Float.MAX_VALUE;
        for (HalfEdge he : candidates) {
            float angle = v.mP.angleBetweenPoints(he.mOpposite.mVertex.mP, p);
            if (angle < bestAngle) {
                best = he;
                bestAngle = angle;
            }
        }
        return best;
    }

    /**
     * Validate that the Mesh is valid. If it isn't this will throw an AssertionError with
     * a message describing the problem encountered. In the middle of some methods, this is
     * expected to not be true. However, at the beginning and end of any public facing function,
     * this should always be true.
     */
    public void validateMesh() {
        try {
            // Isolated validations
            validateMeshHalfEdges();
            validateMeshVertices();
            validateMeshFaces();
            // Edge validations
            validateHalfEdgesNextUnique();
            validateHalfEdgeNextLoops();
            validateHalfEdgeOppositeNextLoops();
            validateNoEdgesCross();
            // Vertex validations
//        validateNoVerticesTheSame();
            validateHalfEdgesAroundVertices();
            validateVerticesAllConnected();
            // Face validations
            validateHalfEdgesAroundFaces();
            validateFacesAllContainedByOuterFace();
        } catch (AssertionError e) {
            Log.i(TAG, toString());
            throw e;
        }
    }
    // Isolated validations
    void validateHalfEdge(HalfEdge he, String errorPrefix) {
        if (he == null) throw new AssertionError(errorPrefix + "null halfedge");
        if (he.mId < 0 || he.mId >= mEdges.size())
            throw new AssertionError(errorPrefix + he.toString() +
                    " id is out of bounds of mEdges (size: " + mEdges.size() + ")");
        HalfEdge he1 = mEdges.get(he.mId);
        if (he != he1) throw new AssertionError(errorPrefix + he.toString() +
                " is not the halfedge found at index " + he.mId + " in mEdges: " + he1.toString());
        if (he.mVertex == null)
            throw new AssertionError(errorPrefix + he.toString() + " has no vertex set");
        if (he.mOpposite == null)
            throw new AssertionError(errorPrefix + he.toString() + " has no opposite edge set");
        if (he.mNext == null)
            throw new AssertionError(errorPrefix + he.toString() + " has no next edge set");
        if (he.mFace == null)
            throw new AssertionError(errorPrefix + he.toString() + " has no face set");
    }
    void validateVertex(Vertex v, String errorPrefix) {
        if (v == null) throw new AssertionError(errorPrefix + "null vertex");
        if (v.mId < 0 || v.mId >= mVertices.size())
            throw new AssertionError(errorPrefix + v.toString() +
                    " id is out of bounds of mVertices (size: " + mVertices.size() + ")");
        Vertex v1 = mVertices.get(v.mId);
        if (v != v1) throw new AssertionError(errorPrefix + v.toString() + " is not the vertex" +
                " found at index " + v.mId + " in mVertices: " + v1.toString());
        if (v.mP == null)
            throw new AssertionError(errorPrefix + v.toString() + " has no point set");
    }
    void validateFace(Face f, String errorPrefix) {
        if (f == null) throw new AssertionError(errorPrefix + "null face");
        if (f.mId < 0 || f.mId >= mFaces.size())
            throw new AssertionError(errorPrefix + f.toString() +
                    " id is out of bounds of mFaces (size: " + mFaces.size() + ")");
        Face f1 = mFaces.get(f.mId);
        if (f != f1) throw new AssertionError(errorPrefix + f.toString() + " is not the face" +
                " found at index " + f.mId + " in mFaces: " + f1.toString());
    }
    private void validateMeshHalfEdges() {
        for (HalfEdge he : mEdges) {
            validateHalfEdge(he, "");
            validateHalfEdge(he.mOpposite, he.toString() + " opposite: ");
            validateHalfEdge(he.mNext, he.toString() + " next: ");
            validateVertex(he.mVertex, he.toString() + " vertex: ");
            validateFace(he.mFace, he.toString() + " face: ");
            if (he.mVertex.isIsolated())
                throw new AssertionError(he.toString() + " vertex claims to be isolated: " +
                        he.mVertex.toString());
            if (he.mOpposite.mOpposite != he)
                throw new AssertionError(he.toString() + " opposite doesn't point back to itself."
                        + "\nopposite edge: " + he.mOpposite.toString());
            if (he.mNext.mOpposite.mVertex != he.mVertex)
                throw new AssertionError(he.toString() + " next edge doesn't come from same vertex."
                        + "\nopposite edge of next edge: " + he.mNext.mOpposite.toString());
        }
    }
    private void validateMeshVertices() {
        for (Vertex v : mVertices) {
            validateVertex(v, "");
            if (v.mP.x < 0 || v.mP.y < 0)
                throw new AssertionError(v.toString() + " has point outside of 1st quadrant: ");
            if (v.isIsolated()) continue; // No more checks needed if this vertex is isolated
            validateHalfEdge(v.mHe, v.toString() + " halfedge: ");
            if (v.mHe.mOpposite.mVertex != v)
                throw new AssertionError(v.toString() + " edge opposite doesn't point back to "
                        + "itself.\nvertex edge: " + v.mHe.toString());
        }
    }
    private void validateMeshFaces() {
        for (Face f : mFaces) {
            validateFace(f, "");
            if (mEdges.isEmpty()) continue;
            validateHalfEdge(f.mHe, f.toString() + " halfedge: ");
            if (f.mHe.mFace != f)
                throw new AssertionError(f.toString() + " has edge that doesn't point back to "
                        + "it. Face edge: " + f.mHe.toString());
        }
    }
    // Edge validations
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
    // Vertex validations
    private void validateNoVerticesTheSame() {
        for (Vertex v1 : mVertices) {
            for (Vertex v2 : mVertices) {
                if (v1.mId == v2.mId) continue;
                if (v1.mP.equals(v2.mP))
                    throw new AssertionError(v1.toString() + " is the same as " + v2.toString());
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
    // Face validations
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
    private void validateFacesAllContainedByOuterFace() {
        Face outerFace = mFaces.get(0);
        for (Face f : mFaces) {
            if (f.mId == 0) continue;
            if (!outerFace.containsFace(f)) throw new AssertionError(f.toString()
                    + " not contained by outer face.");
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