package de.codesourcery.engine.render;

import de.codesourcery.engine.linalg.Matrix;
import de.codesourcery.engine.linalg.Vector4;

public class Camera {

    private Vector4 defaultViewOrientation = new Vector4(0,0,-50);
    private Vector4 defaultEyePosition = new Vector4( 0 , 0, 100 );
    
    private Vector4 eyePosition = defaultEyePosition;
    private Vector4 viewOrientation = defaultViewOrientation;
    
    private Vector4 up = new Vector4(0,1,0);
    private Vector4 eyeTarget = defaultEyePosition.plus( viewOrientation );
    
    private Matrix viewMatrix;
    
    public Camera() {
    }
    
    public void reset() {
    	eyePosition = new Vector4( defaultEyePosition );
    	viewOrientation = new Vector4( defaultViewOrientation );
    	up = new Vector4(0,1,0);
    	updateViewMatrix();
    }
    
    public void moveUp(double increment) {
    	eyePosition.y( eyePosition.y() + increment );
    }
    
    public void moveDown(double increment) {
      	eyePosition.y( eyePosition.y() - increment );
    }
    
    public void strafeLeft(double increment) 
    {
        Vector4 zAxis = eyeTarget.minus( eyePosition ).normalize();
        Vector4 xAxis = zAxis.crossProduct( up ).normalize();
        
    	Vector4 xDirection = xAxis.normalize();
		eyePosition = eyePosition.minus( xDirection.multiply( increment ) );        
    }
    
    public void strafeRight(double increment) 
    {
        Vector4 zAxis = eyeTarget.minus( eyePosition ).normalize();
        Vector4 xAxis = zAxis.crossProduct( up ).normalize();
        
    	Vector4 xDirection = xAxis.normalize();
		eyePosition = eyePosition.plus( xDirection.multiply( increment ) );       	
    }      
    
    public void moveForward(double increment) 
    {
    	eyePosition = eyePosition.plus( viewOrientation.normalize().multiply( increment ) );
    }
    
    public void moveBackward(double increment) {
    	eyePosition = eyePosition.minus( viewOrientation.normalize().multiply( increment ) );
    }    
    
    public Vector4 getEyePosition()
    {
        return eyePosition;
    }
    
    public void setViewOrientation(Vector4 viewOrientation) 
    {
    	this.viewOrientation = new Vector4( viewOrientation ).normalize();
    	updateEyeTarget();
    }
    
    public void setEyePosition(Vector4 eyePosition,Vector4 viewOrientation)
    {
    	this.defaultEyePosition = new Vector4( eyePosition );
    	this.defaultViewOrientation = new Vector4( viewOrientation ).normalize();
    	
    	this.viewOrientation = new Vector4( defaultViewOrientation );
        this.eyePosition = new Vector4( eyePosition );
        
        updateEyeTarget();
    }
    
    private void updateEyeTarget()
    {
        this.eyeTarget = eyePosition.plus( viewOrientation );
    }
    
    public Vector4 getViewOrientation() {
		return viewOrientation;
	}
    
    public Vector4 getEyeTarget()
    {
        return eyeTarget;
    }
    
    public Matrix getViewMatrix() {
		return viewMatrix;
	}
    
    public void updateViewMatrix()
    {
    	updateEyeTarget();
    	
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
}
