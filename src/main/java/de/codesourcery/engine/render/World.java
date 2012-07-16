package de.codesourcery.engine.render;

import java.util.ArrayList;
import java.util.List;

import de.codesourcery.engine.linalg.LinAlgUtils;
import de.codesourcery.engine.linalg.Matrix;
import de.codesourcery.engine.linalg.Vector4;

public final class World
{
    private Matrix translation = Matrix.identity();
    private Matrix rotation = Matrix.identity();
    private Matrix scaling = Matrix.identity();
    
    private Camera camera = new Camera();
    
    private List<Object3D> objects = new ArrayList<>();
    
    private Matrix projectionMatrix;    
    
    // view volume
    private double yTop;
    private double yBottom;
    private double zNear;
    private double zFar;
    private double xLeft;
    private double xRight;
    
    public Matrix getViewMatrix()
    {
        return camera.getViewMatrix();
    }

    public Matrix getProjectionMatrix()
    {
        return projectionMatrix;
    }
    
    public void setupPerspectiveProjection(double fieldOfView, double aspectRatio ,double zNear, double zFar) 
    {
        final double rad = fieldOfView * 0.5 * (Math.PI/180.0d);

        double size = zNear * Math.tan( rad / 2.0f); 

        double xLeft = -size;
		double xRight = size;
		double yBottom = -size / aspectRatio;
		double yTop = size / aspectRatio;

		setupPerspectiveProjection( xLeft , xRight , yBottom , yTop , zNear , zFar );
    }
    
    private void setupPerspectiveProjection(double left, double right, double bottom, double top, double near,double far) 
    {    
    	this.xLeft = left;
    	this.xRight = right;
    	
    	this.yBottom = bottom;
    	this.yTop = top;
    	
    	this.zNear = near;
    	this.zFar = far;
    	
		System.out.println("View volume:\n\n");
		System.out.println("X: ("+xLeft+","+xRight+")");
		System.out.println("Y: ("+yBottom+","+yTop+")");
		System.out.println("Z: ("+zNear+","+zFar+")");    
    	this.projectionMatrix = LinAlgUtils.makeFrustum(xLeft, xRight, yBottom,yTop, zNear, zFar);
    }
    
    public boolean isPointVisible(Vector4 v) {
        
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
    
    private static final double CLIP_X_OFFSET = 0.5;
    private static final double CLIP_Y_OFFSET = 0.5;
    
    public boolean isInClipSpace(Vector4 v) {
    	
    	// TODO: I currently only clip -Z / +Z and let AWT do the X/Y clipping , otherwise
    	// TODO: I would have to calculate intersections of the lines with clip space planes myself... 
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
    
    public void setCamera(Camera camera) {
		this.camera = camera;
	}
    
    public Camera getCamera() {
		return camera;
	}
}
