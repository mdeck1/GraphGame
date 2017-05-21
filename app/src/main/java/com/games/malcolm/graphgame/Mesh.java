package com.games.malcolm.graphgame;

import android.nfc.Tag;
import android.util.Log;

import java.util.ArrayList;

/**
 *
 * Implementation of a 2D halfedge Mesh. Using this will simplify some visualization and operation logic
 *
 * Created by Thomas on 3/17/17.
 */

public class Mesh {
    private static final String TAG = "Mesh";

    protected class Vertex {
        public int mId;
        public int mX;
        public int mY;
        public HalfEdge mHe;

        Vertex(int x, int y, int id) {
            mId = id;
            mX = x;
            mY = y;
        }
        public void move(int x, int y) {
            mX = x;
            mY = y;
        }
        @Override
        public String toString() {
            String str = "Vertex: [id: " + mId +", x: "+ mX + ", y: " + mY;
            if (mHe != null) {
                str += ", he: "+ mHe.mId;
            }
            str +=  "]";
            return str;
        }
    }
    protected class Face {
        public int mId;
        public HalfEdge mHe;
        Face(int id) {
            mId = id;
            //this.he = he;
        }
        @Override
        public String toString() {
            return "Face: [id: " + mId +", he: "+ mHe.mId + "]";
        }
    }
    protected class HalfEdge {
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

        @Override
        public String toString() {
            String str = "HalfEdge: [id: " + mId +", v: "+ mVertex.mId
                    + ", opposite: " + mOpposite.mId;
            if (mNext != null) {
                str += ", next: "+ mNext.mId;
            }
            if (mFace != null) {
                str += ", face: " + mFace.mId;
            }
            str +=  "]";
            return str;
        }
    }

    public ArrayList<HalfEdge> mEdges;
    public ArrayList<Vertex> mVertices;
    public ArrayList<Face> mFaces;
    Mesh() {
        clear();
    }

    public Vertex addVertex(int x, int y) {
        Vertex vertex = new Vertex(x, y, mVertices.size());
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

    public Face splitFace(Face f1, Vertex v1, Vertex v2) {
        Face f2 = addFace();
        HalfEdge he1 = addHalfEdge(v1, v2, f1);
        HalfEdge he2 = addHalfEdge(v2, v1, f2);
        he1.mOpposite = he2;
        he2.mOpposite = he1;

        // set next edge for all relevant edges
        HalfEdge he = f1.mHe;
        HalfEdge he1_prev = new HalfEdge(null, null, 0);
        HalfEdge he2_prev = new HalfEdge(null, null, 0);
        do {
            if (he.mVertex == v1) { he1_prev = he; }
            if (he.mVertex == v2) { he2_prev = he; }
            if (he.mOpposite.mVertex == v1) { he2.mNext = he; }
            if (he.mOpposite.mVertex == v2) { he1.mNext = he; }
            he = he.mNext;
        } while (he != f1.mHe);
        he1_prev.mNext = he1;
        he2_prev.mNext = he2;

        // set faces for all edges
        f1.mHe = he1;
        he = he1;
        do {
            he.mFace = f1;
            he = he.mNext;
        } while (he != f1.mHe);
        f2.mHe = he2;
        he = he2;
        do {
            he.mFace = f2;
            he = he.mNext;
        } while (he != f2.mHe);
        return f2;
    }

    public HalfEdge splitEdge(Vertex v1, Vertex v2, Vertex v3) {
        HalfEdge he1 = edgeBetweenVertices(v1, v2);
        HalfEdge he2 = he1.mOpposite;
        HalfEdge he3 = addHalfEdge(v3, v2, he1.mFace);
        he1.mVertex = v3;
        he3.mNext = he1.mNext;
        he1.mNext = he3;
        he1.mOpposite = he3;
        he3.mOpposite = he1;
        HalfEdge he4 = addHalfEdge(v3, v1, he2.mFace);
        he2.mVertex = v3;
        he4.mNext = he2.mNext;
        he2.mNext = he4;
        he2.mOpposite = he4;
        he4.mOpposite = he2;
        return he3;
    }

    public HalfEdge edgeBetweenVertices(Vertex v1, Vertex v2) {
        HalfEdge he = v1.mHe;
        while (he.mVertex != v2) {
            he = he.mOpposite.mNext;
        }
        return he;
    }

    public ArrayList<Vertex> getNeighbors(Vertex v) {
        ArrayList<Vertex> neighbors = new ArrayList<>();
        HalfEdge he = v.mHe;
        do {
            neighbors.add(he.mVertex);
            he = he.mOpposite.mNext;
        } while (he.mId != v.mHe.mId);
        return neighbors;
    }

    public ArrayList<HalfEdge> getOutEdges(Vertex v) {
        ArrayList<HalfEdge> edges = new ArrayList<>();
        HalfEdge he = v.mHe;
        if (he == null) { return edges; }
        do {
//            Log.i(TAG, he.toString());
            edges.add(he);
            he = he.mOpposite.mNext;
        } while (he.mId != v.mHe.mId);
        return edges;
    }

    public void clear() {
        mEdges = new ArrayList<>();
        mVertices = new ArrayList<>();
        mFaces = new ArrayList<>();
    }

    public boolean isValid() {
        Log.i(TAG, "validating");
        boolean validVertices = true;
        Log.i(TAG, "number of vertices: " + mVertices.size());
        for (Vertex vertex : mVertices) {
            if (vertex.mHe != null) { // possible for isolated vertex
                boolean vertexValid = vertex.mHe.mOpposite.mVertex == vertex;
                validVertices &= vertexValid;
            }
        }
        Log.i(TAG, "vertices valid: " + validVertices);
        boolean validEdges = true;
        Log.i(TAG, "number of halfedges: " + mEdges.size());
        for (HalfEdge he : mEdges) {
            validEdges &= he.mOpposite.mOpposite == he; // opposite exists and is valid
            validEdges &= he.mFace != null; // face exists
            HalfEdge nHe = he.mNext;
            for (int i = 0; i < mEdges.size(); i++) { // Next circles back to original edge
                if (nHe.mId == he.mId) { break; }
                nHe = nHe.mNext;
                if (i == mEdges.size() - 1) { validEdges = false; }
            }
        }
        Log.i(TAG, "halfedges valid: " + validEdges);
        boolean validFaces = true;
        Log.i(TAG, "number of faces: " + mFaces.size());
        for (Face face : mFaces) {
            HalfEdge nHe = face.mHe;
            do {
                validFaces &= nHe.mFace == face;
                nHe = nHe.mNext;
            } while (nHe.mId == face.mHe.mId);
        }
        boolean isValid = validVertices && validEdges & validFaces;
        if (!isValid) {
            Log.i(TAG, "Invalid mesh:\n" + this);
        }
        return isValid;
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
