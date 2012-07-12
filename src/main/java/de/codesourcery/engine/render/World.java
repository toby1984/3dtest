package de.codesourcery.engine.render;

import java.util.ArrayList;
import java.util.List;

import de.codesourcery.engine.geom.Vector4;
import de.codesourcery.engine.linalg.Matrix;

public class World
{
    private Vector4 eyePosition = new Vector4( 0 , 0, 100 );
    private Vector4 eyeTarget = new Vector4( 0 , 0 , 0 );
    private Vector4 up = new Vector4(0,1,0);
    
    private Matrix translation = Matrix.identity();
    private Matrix rotation = Matrix.identity();
    private Matrix scaling = Matrix.identity();
    
    private Matrix viewMatrix = Matrix.identity();
    
    private Matrix projectionMatrix;
    
    private List<Object3D> objects = new ArrayList<>(); 
    
    public Matrix getViewMatrix()
    {
        return viewMatrix;
    }

    public Matrix getProjectionMatrix()
    {
        return projectionMatrix;
    }
    
    public void setProjectionMatrix(Matrix projectionMatrix)
    {
        this.projectionMatrix = projectionMatrix;
    }    
    
    public Vector4 getEyePosition()
    {
        return eyePosition;
    }
    
    public void setEyePosition(Vector4 eyePosition)
    {
        this.eyePosition = eyePosition;
    }
    
    public Vector4 getEyeTarget()
    {
        return eyeTarget;
    }
    
    public void setEyeTarget(Vector4 eyeTarget)
    {
        this.eyeTarget = eyeTarget;
    }
    
    public void addObject(Object3D object) {
        this.objects.add( object );
    }
    
    public List<Object3D> getObjects()
    {
        return objects;
    }
    
    public void updateLookAtMatrix()
    {
        updateLookAtMatrix1();
    }    
    
    private void updateLookAtMatrix2() {
        
        /*
zaxis = normal(At - Eye)
xaxis = normal(cross(Up, zaxis))
yaxis = cross(zaxis, xaxis)         
         */
        
        Vector4 zaxis = eyeTarget.minus( eyePosition ).normalize();
        Vector4 xaxis = zaxis.crossProduct( up ).normalize();
        Vector4 yaxis = zaxis.crossProduct( xaxis );
/*
 * [         xaxis.x          yaxis.x          zaxis.x  0 ]
   [         xaxis.y          yaxis.y          zaxis.y  0 ]
   [         xaxis.z          yaxis.z          zaxis.z  0 ]
   [ dot(xaxis,-eye)  dot(yaxis,-eye)  dot(zaxis,-eye)  1 ]        
 */
        
        Vector4 eyeMinus = new Vector4( -eyePosition.x() , -eyePosition.y() , -eyePosition.z() );
        
        Vector4 col0 = new Vector4( xaxis.x() , xaxis.y() , xaxis.z() , -1*xaxis.dotProduct( eyeMinus ) );
        Vector4 col1 = new Vector4( yaxis.x() , yaxis.y() , yaxis.z() , -1*yaxis.dotProduct( eyeMinus ) );
        Vector4 col2 = new Vector4( zaxis.x() , zaxis.y() , zaxis.z(), zaxis.dotProduct( eyeMinus ) );
        
        Vector4 col3 = new Vector4( 0 , 0 , 0 , 1);
        
        this.viewMatrix = new Matrix( col0,col1,col2,col3);
    }
    
    private void updateLookAtMatrix1()
    {
        Matrix result = new Matrix();
        
        Vector4 zAxis = eyeTarget.minus( eyePosition ).normalize();
        Vector4 xAxis = zAxis.crossProduct( up ).normalize();
        Vector4 yAxis = xAxis.crossProduct( zAxis ).normalize();

        // Matrix#set(col,row,value)
        result.set( 0 , 0 , xAxis.x() );
        result.set( 1 , 0 , xAxis.y() );
        result.set( 2 , 0 , xAxis.z() );

        result.set( 0 , 1 , yAxis.x() );
        result.set( 1 , 1 , yAxis.y() );
        result.set( 2 , 1 , yAxis.z() );
        
        result.set( 0 , 2 , -zAxis.x() );
        result.set( 1 , 2 , -zAxis.y() );
        result.set( 2 , 2 , -zAxis.z() );
        
        result.set( 3 , 0 , -1 * xAxis.dotProduct( eyePosition ) );
        result.set( 3 , 1 , -1 * yAxis.dotProduct( eyePosition ) );
        result.set( 3 , 2 , zAxis.dotProduct( eyePosition ) );
        result.set( 3 , 3 , 1 );  

        this.viewMatrix =  result;
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

    public Matrix getRotation()
    {
        return rotation;
    }

    public void setRotation(Matrix rotation)
    {
        this.rotation = rotation;
    }

    public Matrix getScaling()
    {
        return scaling;
    }

    public void setScaling(Matrix scaling)
    {
        this.scaling = scaling;
    }
}
