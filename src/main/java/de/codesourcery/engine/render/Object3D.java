package de.codesourcery.engine.render;

import static de.codesourcery.engine.LinAlgUtils.*;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import de.codesourcery.engine.geom.ITriangle;
import de.codesourcery.engine.geom.Vector4;
import de.codesourcery.engine.linalg.Matrix;

public final class Object3D implements Iterable<ITriangle> {
    
    private Matrix modelMatrix = identity();
    
    private Matrix translation = identity();
    private Matrix scaling = identity();
    private Matrix rotation = identity();
    
    /* Vertices - Vector components stored in x,y,z,w order. */
    private double[] vertices; //
    
    /* Edges - pointers into the vertices array , each element pair
     * edge[i]/edge[i+1] describes one edge.
     */
    private int[] edges;
    
    public Object3D() {
    }
    
    public Object3D createCopy() 
    {
        Object3D result = new Object3D();
        result.translation = translation;
        result.scaling = scaling;
        result.rotation = rotation;
        result.vertices = vertices;
        result.edges = edges;
        result.updateModelMatrix();
        return result;
    }
    
    public void setTriangles(List<ITriangle> triangles) 
    {
        System.out.println("Adding "+triangles.size()+" triangles...");
        
        final double[] tmpVertices = new double[ triangles.size() * 3 * 4 ]; // 3 vertices per triangle with 4 components each
        final int[] tmpEdges = new int[ triangles.size() * 3 ]; // 3 edges per triangle with 2 vertices each

        int currentVertex = 0;
        int currentEdge = 0;
        int duplicateVertices = 0;
        
        for ( ITriangle t : triangles ) 
        {
            System.out.println("Adding triangle: "+t);
            
            Vector4 p = t.p1();
            
            // TODO: PERFORMANCE - findVertex() uses a linear / O(n) search 
            int vertex1 = findVertex( p , tmpVertices , currentVertex );
            if ( vertex1 == -1 ) { // store new vertex
                System.out.println("Adding vertex: "+p);
                vertex1 = currentVertex;
                tmpVertices[ currentVertex++ ] = p.x();
                tmpVertices[ currentVertex++ ] = p.y();
                tmpVertices[ currentVertex++ ] = p.z();
                tmpVertices[ currentVertex++ ] = p.w();
            } else {
                System.out.println("Duplicate vertex: "+p);
                duplicateVertices++;
            }
            
            p = t.p2();
            int vertex2 = findVertex( p , tmpVertices , currentVertex );
            if ( vertex2 == -1 ) { // store new vertex
                System.out.println("Adding vertex: "+p);
                vertex2 = currentVertex;
                tmpVertices[ currentVertex++ ] = p.x();
                tmpVertices[ currentVertex++ ] = p.y();
                tmpVertices[ currentVertex++ ] = p.z();
                tmpVertices[ currentVertex++ ] = p.w();
            } else {
                System.out.println("Duplicate vertex: "+p);
                duplicateVertices++;
            }
            
            p = t.p3();
            int vertex3 = findVertex( p , tmpVertices , currentVertex );
            if ( vertex3 == -1 ) { // store new vertex
                System.out.println("Adding vertex: "+p);
                vertex3 = currentVertex;
                tmpVertices[ currentVertex++ ] = p.x();
                tmpVertices[ currentVertex++ ] = p.y();
                tmpVertices[ currentVertex++ ] = p.z();
                tmpVertices[ currentVertex++ ] = p.w();
            } else {
                System.out.println("Duplicate vertex: "+p);
                duplicateVertices++;
            }
            
            // store edges
            tmpEdges[ currentEdge++ ] = vertex1;
            tmpEdges[ currentEdge++ ] = vertex2;
            tmpEdges[ currentEdge++ ] = vertex3;
        }
        this.vertices = ArrayUtils.subarray( tmpVertices , 0 , currentVertex );
        this.edges = tmpEdges;
        
        System.out.println("Vertices: "+(vertices.length/4)+" (duplicates: "+duplicateVertices+")");
    }
    
    public int getPointCount() {
        return vertices != null ? vertices.length / 4 : 0;
    }

    public double[] getVertices()
    {
        return vertices;
    }
    
    public int[] getEdges()
    {
        return edges;
    }
    
    private int findVertex(Vector4 p,double[] array,int maxIndex) 
    {
        final double x = p.x();
        final double y = p.y();
        final double z = p.z();
        final double w = p.w();
        
        for ( int i = 0 ; i < maxIndex ; i +=4 ) 
        {
            if ( array[i] == x &&
                 array[i+1] == y &&
                 array[i+2] == z &&
                 array[i+3] == w ) 
            {
                return i;
            }
        }
        return -1;
    }
    
    public void updateModelMatrix() 
    {
//        this.modelMatrix = scaling.multiply( rotation ).multiply( translation );
        this.modelMatrix = translation.multiply( scaling ).multiply( rotation );
    }        
    
    public Matrix getModelMatrix() 
    {
        if ( modelMatrix == null ) {
            updateModelMatrix();
        }
        return modelMatrix;
    }
    
    public Matrix getRotation()
    {
        return rotation;
    }
    
    public Matrix getTranslation()
    {
        return translation;
    }
    
    public Matrix getScaling()
    {
        return scaling;
    }
    
    public void setRotation(Matrix rotation) {
        this.rotation = rotation;
    }
    
    public void setTranslation(double x , double y , double z )
    {
        this.translation = translationMatrix(x , y , z );
    }
    
    public void setScaling(double x , double y , double z)
    {
        this.scaling = scalingMatrix(x,y,z);
    }
    
    public Object3D(Matrix translation ) {
        this.translation = translation;
    }        
    
    public Object3D(Matrix translation , Matrix scaling ) {
        this.translation = translation;
        this.scaling = scaling;
    }          
    
    private final class MyTriangle implements ITriangle 
    {
        private final Vector4 p1=new Vector4();
        private final Vector4 p2=new Vector4();
        private final Vector4 p3=new Vector4();
        
        @Override
        public Vector4 p1()
        {
            return p1;
        }

        @Override
        public Vector4 p2()
        {
            return p2;
        }

        @Override
        public Vector4 p3()
        {
            return p3;
        }
        
        public void setVertices(int firstVerticeIndex) 
        {
            p1.setData( vertices , edges[ firstVerticeIndex ] );
            p2.setData( vertices , edges[ firstVerticeIndex + 1 ]);
            p3.setData( vertices , edges[ firstVerticeIndex + 2 ] );
        }
        
        @Override
        public String toString()
        {
            return p1+" -> "+p2+" -> "+p3+" -> "+p1;
        }
    };    
    
    @Override
    public Iterator<ITriangle> iterator()
    {
        return new Iterator<ITriangle>() {

            private int currentTriangle = 0;
            private final int edgeCount = edges.length;
            
            private final MyTriangle t = new MyTriangle();
            
            @Override
            public boolean hasNext()
            {
                return currentTriangle < edgeCount;
            }

            @Override
            public ITriangle next()
            {
                t.setVertices( currentTriangle );
                currentTriangle+=3;// 3 edges per triangle
                return t;
            }

            @Override
            public void remove()
            {
                throw new UnsupportedOperationException("remove() not supported");
            }};
    }
}