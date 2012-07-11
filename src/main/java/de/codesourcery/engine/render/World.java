package de.codesourcery.engine.render;

import static de.codesourcery.engine.LinAlgUtils.*;

import java.util.ArrayList;
import java.util.List;

import de.codesourcery.engine.geom.Vector4;
import de.codesourcery.engine.linalg.Matrix;

public class World
{
    private Vector4 viewVector = vector( 0 , 0 , -100 );
    
    private Matrix translation = Matrix.identity();
    private Matrix scaling = Matrix.identity();
    private Matrix rotation = Matrix.identity();
    
    private Matrix viewMatrix = Matrix.identity();
    
    private List<Object3D> objects = new ArrayList<>(); 
    
    public Matrix getViewMatrix()
    {
        return viewMatrix;
    }
    
    public Vector4 getViewVector()
    {
        return viewVector;
    }
    
    public void setViewVector(Vector4 viewVector)
    {
        this.viewVector = viewVector;
    }
    
    public void updateViewMatrix() {
        
        Matrix transform = mult( scaling , rotation );
        viewMatrix = mult(  translation , transform );
        
//        Matrix transform = mult( translation , scaling );
//        viewMatrix = mult(  transform ,  rotation );
    }
    
    public void addObject(Object3D object) {
        this.objects.add( object );
    }
    
    public List<Object3D> getObjects()
    {
        return objects;
    }
    
    public void setViewMatrix(Matrix viewMatrix)
    {
        this.viewMatrix = viewMatrix;
    }

    public Matrix getTranslation()
    {
        return translation;
    }

    public void setTranslation(Matrix translation)
    {
        this.translation = translation;
    }

    public Matrix getScaling()
    {
        return scaling;
    }

    public void setScaling(Matrix scaling)
    {
        this.scaling = scaling;
    }

    public Matrix getRotation()
    {
        return rotation;
    }

    public void setRotation(Matrix rotation)
    {
        this.rotation = rotation;
    }
}
