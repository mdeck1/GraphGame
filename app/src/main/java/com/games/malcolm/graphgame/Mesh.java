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
        public int id;
        public int x;
        public int y;
        public HalfEdge he;

        Vertex(int x, int y, int id) {
            this.id = id;
            this.x = x;
            this.y = y;
        }
        public void move(int x, int y) {
            this.x = x;
            this.y = y;
        }
        @Override
        public String toString() {
            String str = "Vertex: [id: " + id +", x: "+ x + ", y: " + y;
            if (he != null) {
                str += ", he: "+ he.id;
            }
            str +=  "]";
            return str;
        }
    }
    protected class Face {
        public int id;
        public HalfEdge he;
        Face(int id) {
            this.id = id;
            //this.he = he;
        }
        @Override
        public String toString() {
            return "Face: [id: " + id +", he: "+ he.id + "]";
        }
    }
    protected class HalfEdge {
        public int id;
        public Vertex v;
        public HalfEdge next;
        public HalfEdge opposite;
        public Face face;
        HalfEdge(Vertex v, Face face, int id) {
            this.id = id;
            this.v = v;
            this.face = face;
        }

        @Override
        public String toString() {
            String str = "HalfEdge: [id: " + id +", v: "+ v.id + ", opposite: " + opposite.id;
            if (next != null) {
                str += ", next: "+ next.id;
            }
            if (face != null) {
                str += ", face: " + face.id;
            }
            str +=  "]";
            return str;
        }
    }

    public ArrayList<HalfEdge> edges;
    public ArrayList<Vertex> vertices;
    public ArrayList<Face> faces;
    Mesh() {
        clear();
    }
    public Vertex addVertex(int x, int y) {
        Vertex vertex = new Vertex(x, y, vertices.size());
        vertices.add(vertex);
        return vertex;
    }
    public Face addFace() {
        Face face = new Face(faces.size());
        faces.add(face);
        return face;
    }
    public HalfEdge addHalfEdge(Vertex from, Vertex to, Face face) {
        HalfEdge he = new HalfEdge(to, face, edges.size());
        edges.add(he);
        from.he = he;
        return he;
    }

    public Face splitFace(Face f1, Vertex v1, Vertex v2) {
        Face f2 = addFace();
        HalfEdge he1 = addHalfEdge(v1, v2, f1);
        HalfEdge he2 = addHalfEdge(v2, v1, f2);
        he1.opposite = he2;
        he2.opposite = he1;

        // set next edge for all relevant edges
        HalfEdge he = f1.he;
        HalfEdge he1_prev = new HalfEdge(null, null, 0);
        HalfEdge he2_prev = new HalfEdge(null, null, 0);
        do {
            if (he.v == v1) { he1_prev = he; }
            if (he.v == v2) { he2_prev = he; }
            if (he.opposite.v == v1) { he2.next = he; }
            if (he.opposite.v == v2) { he1.next = he; }
            he = he.next;
        } while (he != f1.he);
        he1_prev.next = he1;
        he2_prev.next = he2;

        // set faces for all edges
        f1.he = he1;
        he = he1;
        do {
            he.face = f1;
            he = he.next;
        } while (he != f1.he);
        f2.he = he2;
        he = he2;
        do {
            he.face = f2;
            he = he.next;
        } while (he != f2.he);
        return f2;
    }

    public HalfEdge splitEdge(Vertex v1, Vertex v2, Vertex v3) {
        HalfEdge he1 = edgeBetweenVertices(v1, v2);
        HalfEdge he2 = he1.opposite;
        HalfEdge he3 = addHalfEdge(v3, v2, he1.face);
        he1.v = v3;
        he3.next = he1.next;
        he1.next = he3;
        he1.opposite = he3;
        he3.opposite = he1;
        HalfEdge he4 = addHalfEdge(v3, v1, he2.face);
        he2.v = v3;
        he4.next = he2.next;
        he2.next = he4;
        he2.opposite = he4;
        he4.opposite = he2;
        return he3;
    }

    public HalfEdge edgeBetweenVertices(Vertex v1, Vertex v2) {
        HalfEdge he = v1.he;
        while (he.v != v2) {
            he = he.opposite.next;
        }
        return he;
    }

    public void clear() {
        edges = new ArrayList<>();
        vertices = new ArrayList<>();
        faces = new ArrayList<>();
    }

    public boolean isValid() {
        Log.i(TAG, "validating");
        boolean validVertices = true;
        Log.i(TAG, "number of vertices: " + vertices.size());
        for (Vertex v : vertices) {
            if (v.he != null) { // possible for isolated vertex
                boolean vertexValid = v.he.opposite.v == v;
                validVertices &= vertexValid;
            }
        }
        Log.i(TAG, "vertices valid: " + validVertices);
        boolean validEdges = true;
        Log.i(TAG, "number of halfedges: " + edges.size());
        for (HalfEdge he : edges) {
            validEdges &= he.opposite.opposite == he;
            validEdges &= he.face != null;
        }
        Log.i(TAG, "halfedges valid: " + validEdges);
        boolean validFaces = true;
        Log.i(TAG, "number of faces: " + faces.size());
        for (Face f : faces) {
            validFaces &= f.he.face == f;
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
        for (Vertex v: vertices) {
            str += "\n" + v;
        }
        str += "]\n";
        if (!edges.isEmpty()) {
            str += "edges: [";
            for (HalfEdge he: edges) {
                str += "\n" + he;
            }
            str += "]\n";
        }
        if (!faces.isEmpty()) {
            str += "faces: [";
            for (Face f: faces) {
                str += "\n" + f;
            }
            str += "]\n";
        }
        str +=  "]";
        return str;
    }
}
