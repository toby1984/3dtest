package de.codesourcery.engine.render;

import static de.codesourcery.engine.linalg.LinAlgUtils.rotY;
import static de.codesourcery.engine.linalg.LinAlgUtils.translationMatrix;
import de.codesourcery.engine.linalg.Matrix;
import de.codesourcery.engine.linalg.Vector4;

public class Camera {

	private Vector4 viewOrientation = new Vector4(0,0,-50);

    private Vector4 defaultEyePosition = new Vector4( 0 , 0, 100 );
    
	private Vector4 eyePosition = new Vector4( 0 , 0, 100 );
    private Vector4 up = new Vector4(0,1,0);
    private Vector4 eyeTarget = defaultEyePosition.plus( viewOrientation );
    
    private double rotateY; // degrees
    
    private Matrix viewMatrix;
    
    private double clipAngle(double angleInDegrees) 
    {
    	if ( angleInDegrees > 360 ) {
    		return angleInDegrees - 360.0;
    	} else if ( angleInDegrees < 0 ) {
    		return angleInDegrees + 360;
    	}
    	return angleInDegrees;
    }
    
    private void rotateY(double angleInDegrees) 
    {
        rotateY = clipAngle( rotateY + angleInDegrees );
    }
    
    public Camera() {
    }
    
    public void reset() {
    	rotateY = 0;
    	eyePosition = new Vector4( defaultEyePosition );
    	up = new Vector4(0,1,0);
    	updateViewMatrix();
    }
    
    public void moveUp(double increment) {
    	eyePosition.y( eyePosition.y() + increment );
    }
    
    public void moveDown(double increment) {
      	eyePosition.y( eyePosition.y() - increment );
    }
    
    public void strafeLeft(double increment) {
    	eyePosition.x( eyePosition.x() - increment );
    }
    
    public void strafeRight(double increment) {
    	eyePosition.x( eyePosition.x() + increment );
    }      
    
    public void moveForward(double increment) {
    	eyePosition.z( eyePosition.z() - increment );
    }
    
    public void moveBackward(double increment) {
    	eyePosition.z( eyePosition.z() + increment );
    }    
    
    public void rotateLeft(double angleInDegrees) {
    	rotateY( -angleInDegrees );
    }    
    
    public void rotateRight(double angleInDegrees) {
    	rotateY( angleInDegrees );
    }     
    
    public Vector4 getEyePosition()
    {
        return eyePosition;
    }
    
    public void setEyePosition(Vector4 eyePosition,Vector4 viewOrientation)
    {
    	this.viewOrientation = new Vector4( viewOrientation );
    	this.defaultEyePosition = new Vector4( eyePosition );
        this.eyePosition = new Vector4( eyePosition );
        updateEyeTarget();
    }
    
    private void updateEyeTarget()
    {
        Matrix translation = translationMatrix( -eyePosition.x() ,  -eyePosition.y(), -eyePosition.z() );
        Matrix rotation = rotY( rotateY );
        
        final Vector4 defaultEyeTarget = eyePosition.plus( viewOrientation );
        Vector4 tmp = translation.multiply( rotation ) .multiply( defaultEyeTarget );
        tmp = translationMatrix( eyePosition.x() ,  eyePosition.y(), eyePosition.z() ).multiply( tmp );
        this.eyeTarget = tmp;    	
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
