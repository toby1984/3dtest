package de.codesourcery.engine.render;

import static de.codesourcery.engine.LinAlgUtils.*;

import java.util.ArrayList;
import java.util.List;

import org.ejml.data.DenseMatrix64F;

import de.codesourcery.engine.geom.Quad;
import de.codesourcery.engine.geom.Triangle;

public final class Object3D {
    
    private DenseMatrix64F translation = identity();
    private DenseMatrix64F scaling = identity();
    private DenseMatrix64F rotation = identity();
    
    private DenseMatrix64F viewMatrix = identity();
    
    private final List<Triangle> triangles = new ArrayList<>();
    
    public Object3D() {
    }
    
    public Object3D createCopy() 
    {
        Object3D result = new Object3D();
        result.translation = translation;
        result.scaling = scaling;
        result.rotation = rotation;
        result.triangles.addAll( this.triangles );
        result.updateViewMatrix();
        return result;
    }
    
    public void updateViewMatrix() {
        DenseMatrix64F transform = mult( translation , scaling );
        viewMatrix = mult(  transform , rotation );
    }        
    
    public DenseMatrix64F getModelMatrix() {
        return viewMatrix;
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
    
    public void add(Triangle t) {
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
    
    public List<Triangle> getTriangles() {
        return triangles;
    }
}