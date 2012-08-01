package de.codesourcery.engine.render;

import com.jogamp.graph.math.Quaternion;

import de.codesourcery.engine.linalg.Frustum;
import de.codesourcery.engine.linalg.LinAlgUtils;
import de.codesourcery.engine.linalg.Matrix;
import de.codesourcery.engine.linalg.Vector4;
import de.codesourcery.engine.math.Constants;

public class Camera {

    private Vector4 defaultEyePosition = new Vector4( 0 , 0, 100 );
    private Vector4 eyePosition = defaultEyePosition;
    
    private Vector4 viewOrientation = new Vector4(0,0,-1);
    private Vector4 up = new Vector4(0,1,0);
    
	private float rotAngleY = 0.0f; // rotation around Y axis, in degrees
	private float rotAngleX = 0.0f; // rotation around X axis
	
	private float defaultRotY = 0.0f;
	private float defaultRotX = 0.0f;
	
    private Vector4 eyeTarget = defaultEyePosition.plus( viewOrientation );
    
    private Matrix viewMatrix = Matrix.identity();
    
    private final Frustum frustum;
    
    public Camera(Frustum frustum) 
    {
    	this.frustum = frustum;
    }
    
    public Vector4 getUpVector() {
    	return up;
    }
    
    public void reset() 
    {
    	eyePosition = new Vector4( defaultEyePosition );
		rotAngleY = defaultRotY;
		rotAngleX = defaultRotX; 
    	updateViewMatrix();
    }
    
    public void moveUp(float increment) {
    	eyePosition = eyePosition.plus( up.multiply( increment ) );
    	updateViewMatrix();
    }
    
    public void moveDown(float increment) {
    	eyePosition = eyePosition.minus( up.multiply( increment ) );
    	updateViewMatrix();
    }
    
    public void strafeLeft(float increment) 
    {
        Vector4 zAxis = eyeTarget.minus( eyePosition ).normalize();
        Vector4 xAxis = zAxis.crossProduct( up ).normalize();
        
    	Vector4 xDirection = xAxis.normalize();
		eyePosition = eyePosition.minus( xDirection.multiply( increment ) );
		updateViewMatrix();
    }
    
    public void strafeRight(float increment) 
    {
        Vector4 zAxis = eyeTarget.minus( eyePosition ).normalize();
        Vector4 xAxis = zAxis.crossProduct( up ).normalize();
        
    	Vector4 xDirection = xAxis.normalize();
		eyePosition = eyePosition.plus( xDirection.multiply( increment ) );
		updateViewMatrix();
    }      
    
    public void moveForward(float increment) 
    {
    	eyePosition = eyePosition.plus( viewOrientation.normalize().multiply( increment ) );
    	updateViewMatrix();
    }
    
    public void moveBackward(float increment) {
    	eyePosition = eyePosition.minus( viewOrientation.normalize().multiply( increment ) );
    	updateViewMatrix();
    }    
    
    public Vector4 getEyePosition()
    {
        return eyePosition;
    }
    
    public void setEyePosition(Vector4 eyePosition,float rotY, float rotX)
    {
    	this.defaultEyePosition = new Vector4( eyePosition );
        this.eyePosition = new Vector4( eyePosition );
        
    	this.defaultRotY = rotY;
    	this.defaultRotX = rotX;
    	
    	updateViewMatrix();
    }
    
    public void rotate(float deltaY,float deltaX) 
    {
		rotAngleY += deltaY;
		
		// clamp angle to avoid loss of precision
		if ( rotAngleY > 360.0 ) {
			rotAngleY -= 360;
		}
		if ( rotAngleY < -360 ) {
			rotAngleY += 360;
		}
		
		rotAngleX -= deltaX;
		
		if ( rotAngleX > 360 ) {
			rotAngleX -= 360;
		} 
		else if ( rotAngleX < -360 ) {
			rotAngleX = +360;
		}		
		
		System.out.println("rotY = "+rotAngleY+" / rotX = "+rotAngleX);
		updateViewMatrix();
    }
    
    private Vector4 applyRotations(Vector4 v) 
    {
    	// X Rotation
        float x = v.x();
        float y  = (float) (Math.cos(rotAngleX * Constants.DEG_TO_RAD) * v.y() - Math.sin( rotAngleX * Constants.DEG_TO_RAD ) * v.z() );
        float z = (float) (Math.sin(rotAngleX * Constants.DEG_TO_RAD) * v.y() + Math.cos(rotAngleX * Constants.DEG_TO_RAD) * v.z());
    	
        // Y Rotation
        float x2 = (float) (Math.cos(rotAngleY * Constants.DEG_TO_RAD) * x - Math.sin(rotAngleY * Constants.DEG_TO_RAD) * z);
        float y2 = y;
        float z2 = (float) (Math.sin(rotAngleY * Constants.DEG_TO_RAD) * x + Math.cos(rotAngleY * Constants.DEG_TO_RAD) * z);
    	
//		final Matrix rot = LinAlgUtils.rotX( rotAngleX ).multiply( LinAlgUtils.rotY( rotAngleY ) );
//		return rot.multiply( v );
        return new Vector4(x2,y2,z2);
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
    
    private void updateViewMatrix()
    {
    	this.viewOrientation = applyRotations( new Vector4( 0,0,-1 ) );
    	this.up = applyRotations( new Vector4(0,1,0 ) );   
    	this.eyeTarget = eyePosition.plus( viewOrientation );
    	
        Matrix result = new Matrix();
        
        Vector4 zAxis = eyeTarget.minus( eyePosition ).normalize();
        Vector4 xAxis = zAxis.crossProduct( up ).normalize();
        Vector4 yAxis = xAxis.crossProduct( zAxis ).normalize();

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
		frustum.setEyePosition(  getEyePosition() , getEyeTarget() , getUpVector() );
    }

}
