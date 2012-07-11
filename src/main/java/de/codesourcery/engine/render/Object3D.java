package de.codesourcery.engine.render;

import static de.codesourcery.engine.LinAlgUtils.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.ejml.data.DenseMatrix64F;

import de.codesourcery.engine.geom.ITriangle;
import de.codesourcery.engine.geom.Quad;
import de.codesourcery.engine.geom.Vector4;

public final class Object3D implements Iterable<ITriangle> {
    
    private DenseMatrix64F translation = identity();
    private DenseMatrix64F scaling = identity();
    private DenseMatrix64F rotation = identity();
    
    private DenseMatrix64F modelMatrix = identity();
    
    private double[] vertices; // Vector4 components
    private int[] edges;
    
    private final List<ITriangle> triangles = new ArrayList<>();
    
    public Object3D() {
    }
    
    public Object3D createCopy() 
    {
        Object3D result = new Object3D();
        result.translation = translation;
        result.scaling = scaling;
        result.rotation = rotation;
        result.triangles.addAll( this.triangles );
        result.recalculateModelMatrix();
        return result;
    }
    
    public void compile() 
    {
        int currentVertex = 0;
        int currentEdge = 0;
        double[] tmpVertices = new double[ triangles.size() ];
        int[] tmpEdges = new int[ triangles.size() * 3 ];
        
        for ( ITriangle t : triangles ) 
        {
            Vector4 p = t.p1();
            
            // TODO: PERFORMANCE - findVertex() uses a linear / O(n) search 
            int vertex1 = findVertex( p , tmpVertices );
            if ( vertex1 == -1 ) { // store new vertex
                vertex1 = currentVertex;
                tmpVertices[ currentVertex++ ] = p.x();
                tmpVertices[ currentVertex++ ] = p.y();
                tmpVertices[ currentVertex++ ] = p.z();
                tmpVertices[ currentVertex++ ] = p.w();
            }
            
            p = t.p2();
            int vertex2 = findVertex( p , tmpVertices );
            if ( vertex2 == -1 ) { // store new vertex
                vertex2 = currentVertex;
                tmpVertices[ currentVertex++ ] = p.x();
                tmpVertices[ currentVertex++ ] = p.y();
                tmpVertices[ currentVertex++ ] = p.z();
                tmpVertices[ currentVertex++ ] = p.w();
            }  
            
            p = t.p3();
            int vertex3 = findVertex( p , tmpVertices );
            if ( vertex3 == -1 ) { // store new vertex
                vertex3 = currentVertex;
                tmpVertices[ currentVertex++ ] = p.x();
                tmpVertices[ currentVertex++ ] = p.y();
                tmpVertices[ currentVertex++ ] = p.z();
                tmpVertices[ currentVertex++ ] = p.w();
            }             
            
            // add edges
            tmpEdges[ currentEdge++ ] = vertex1;
            tmpEdges[ currentEdge++ ] = vertex2;
            
            tmpEdges[ currentEdge++ ] = vertex2;
            tmpEdges[ currentEdge++ ] = vertex3;      
            
            tmpEdges[ currentEdge++ ] = vertex3;
            tmpEdges[ currentEdge++ ] = vertex1;             
        }
        this.vertices = ArrayUtils.subarray( tmpVertices , 0 , currentVertex );
        this.edges = tmpEdges;
        triangles.clear();
    }
    
    public int getPointCount() {
        return vertices != null ? vertices.length / 4 : triangles.size();
    }

    public double[] getVertices()
    {
        return vertices;
    }
    
    public int[] getEdges()
    {
        return edges;
    }
    
    private int findVertex(Vector4 p,double[] array) 
    {
        final double x = p.x();
        final double y = p.y();
        final double z = p.z();
        final double w = p.w();
        
        for ( int i = 0 ; i < array.length ; i +=4 ) 
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
    
    public void add(ITriangle t) {
        this.triangles.add( t );
    }
    
    public void add(Quad quad) 
    {
        this.triangles.add( quad.t1 );
        this.triangles.add( quad.t2 );
    }     
    
    public void add(List<Quad> quads) 
    {
        for ( Quad q : quads ) {
            add( q );
        }
    }          
    
    public Object3D(DenseMatrix64F translation ) {
        this.translation = translation;
    }        
    
    public Object3D(DenseMatrix64F translation , DenseMatrix64F scaling ) {
        this.translation = translation;
        this.scaling = scaling;
    }          
    
    public List<ITriangle> getTriangles() {
        return triangles;
    }

    @Override
    public Iterator<ITriangle> iterator()
    {
        return triangles.iterator();
    }
}