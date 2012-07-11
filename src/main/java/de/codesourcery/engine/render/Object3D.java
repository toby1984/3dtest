package de.codesourcery.engine.render;

import static de.codesourcery.engine.LinAlgUtils.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.ejml.data.DenseMatrix64F;

import de.codesourcery.engine.geom.ITriangle;
import de.codesourcery.engine.geom.Vector4;

public final class Object3D implements Iterable<ITriangle> {
    
    private DenseMatrix64F modelMatrix = identity();
    
    private DenseMatrix64F translation = identity();
    private DenseMatrix64F scaling = identity();
    private DenseMatrix64F rotation = identity();
    
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
        result.recalculateModelMatrix();
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
    
    public void recalculateModelMatrix() {
        DenseMatrix64F transform = mult( translation , scaling );
        modelMatrix = mult(  transform , rotation );
    }        
    
    public DenseMatrix64F getModelMatrix() 
    {
        if ( modelMatrix == null ) {
            recalculateModelMatrix();
        }
        return modelMatrix;
    }
    
    public void setRotation(DenseMatrix64F rotation) {
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
    
    public Object3D(DenseMatrix64F translation ) {
        this.translation = translation;
    }        
    
    public Object3D(DenseMatrix64F translation , DenseMatrix64F scaling ) {
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
        
        public void copyCoords(int firstEdgeIndex) 
        {
            p1.setData( vertices , edges[ firstEdgeIndex ] );
            p2.setData( vertices , edges[ firstEdgeIndex + 1 ]);
            p3.setData( vertices , edges[ firstEdgeIndex + 2 ] );
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
                t.copyCoords( currentTriangle );
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