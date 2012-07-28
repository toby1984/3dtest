package de.codesourcery.engine.render;

import java.util.ArrayList;
import java.util.List;

import de.codesourcery.engine.linalg.Frustum;
import de.codesourcery.engine.linalg.LinAlgUtils;
import de.codesourcery.engine.linalg.Matrix;
import de.codesourcery.engine.linalg.Vector4;
import de.codesourcery.engine.math.Constants;

public final class World
{
    private static final float CLIP_X_OFFSET = 0.5f;
    private static final float CLIP_Y_OFFSET = 0.5f;
    
    private Camera camera = new Camera() {
    	public void updateViewMatrix() {
    		super.updateViewMatrix();
    		frustum.setEyePosition(  getEyePosition() , getEyeTarget() , getUpVector() );
    	};
    	
    	protected void updateEyeTarget() {
    		super.updateEyeTarget();
    		frustum.setEyePosition(  getEyePosition() , getEyeTarget() , getUpVector() );
    	};
    };
    
    private List<Object3D> objects = new ArrayList<>();
    
    private final Frustum frustum = new Frustum();
    
    private Matrix projectionMatrix;    
    
    // view volume
    private float yTop;
    private float yBottom;
    private float zNear;
    private float zFar;
    private float xLeft;
    private float xRight;
    
    public Matrix getViewMatrix()
    {
        return camera.getViewMatrix();
    }

    public Matrix getProjectionMatrix()
    {
        return projectionMatrix;
    }
    
    public void setupPerspectiveProjection(float fieldOfView, float aspectRatio ,float zNear, float zFar) 
    {
    	System.out.println("setupPerspectiveProjection(): FoV: "+fieldOfView+" , aspect_ratio="+aspectRatio+", zNear="+zNear+",zFar="+zFar);
        final float rad = fieldOfView * Constants.DEG_TO_RAD;

        float size = zNear * (float) Math.tan( rad / 2.0f ); 

        float xLeft = -size;
		float xRight = size;
		float yBottom = -size / aspectRatio;
		float yTop = size / aspectRatio;

		setupPerspectiveProjection( xLeft , xRight , yBottom , yTop , zNear , zFar );
		frustum.setPerspective( fieldOfView, aspectRatio, zNear,zFar );
    }
    
    private void setupPerspectiveProjection(float left, float right, float bottom, float top, float near,float far) 
    {    
    	this.xLeft = left;
    	this.xRight = right;
    	
    	this.yBottom = bottom;
    	this.yTop = top;
    	
    	this.zNear = near;
    	this.zFar = far;

    	this.projectionMatrix = LinAlgUtils.makeFrustum(xLeft, xRight, yBottom,yTop, zNear, zFar);
    }
    
    public Frustum getFrustum() {
		return frustum;
	}
    
    public boolean isInClipSpace(Vector4[] points) {
    	for ( int i = 0 ; i < points.length ; i++ ) {
    		if ( ! isInClipSpace( points[i] ) ) {
    			return false;
    		}
    	}
    	return true;
    }
    
    public boolean isInClipSpace(Vector4 p1,Vector4 p2,Vector4 p3) {
    	return isInClipSpace( p1 ) && isInClipSpace( p2 ) && isInClipSpace( p3 );
    }
    
    public boolean isInClipSpace(Vector4 v) {

    	// TODO: Clipping is currently disabled !!!!
    	if ( 1 != 2 ) {
    		return true;
    	}
    	return v.z() > -1 && v.z() < 1 &
    		   v.x() > ( -1 - CLIP_X_OFFSET ) && v.x() < ( 1 + CLIP_X_OFFSET ) &&
    		   v.y() < ( 1 + CLIP_Y_OFFSET ) && v.y() > ( -1 - CLIP_Y_OFFSET );
    }
    
    public void addObject(Object3D object) {
        this.objects.add( object );
    }
    
    public List<Object3D> getObjects()
    {
        return objects;
    }
    
    public Camera getCamera() {
		return camera;
	}
}
