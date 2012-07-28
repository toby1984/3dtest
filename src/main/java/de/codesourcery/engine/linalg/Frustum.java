package de.codesourcery.engine.linalg;

import de.codesourcery.engine.math.Constants;
import de.codesourcery.engine.render.Object3D;
import de.codesourcery.engine.render.World;


public final class Frustum  
{  
	private static final String[] PLANE_NAMES = {"TOP","BOTTOM","LEFT","RIGHT","NEAR","FAR"};
	private static final int TOP=0, BOTTOM =1, LEFT=2,RIGHT=3, NEAR=4, FAR=5;

	private static final String planeIndexToName(int index) {
		return PLANE_NAMES[ index ];
	}
	public static enum TestResult {
		OUTSIDE, INTERSECT, INSIDE
	}

	private Plane[] planes = new Plane[6];

	Vector4 ntl,ntr,nbl,nbr,ftl,ftr,fbl,fbr;
	float nearD, farD, ratio, angle,tang;
	float nw,nh,fw,fh;	
	
	private final Vector4 eyePosition = new Vector4(0,0,0);
	private final Vector4 eyeTarget = new Vector4(0,0,-1);
	private final Vector4 upVector = new Vector4(0,1,0);

	private volatile boolean needsPlaneRecalculation = true;
	
	public Frustum() {
		for ( int i = 0 ; i < planes.length ; i++ ) {
			planes[i] = new Plane(new Vector4(),new Vector4());
		}
	}
	
	public synchronized void setPerspective(float angle, float ratio, float nearD, float farD) {

		// store the information
		this.ratio = ratio;
		this.angle = angle;
		this.nearD = nearD;
		this.farD = farD;

		// compute width and height of the near and far plane sections
		tang = (float) Math.tan( Constants.DEG_TO_RAD * angle * 0.5f) ;
		nh = nearD * tang;
		nw = nh * ratio; 
		fh = farD  * tang;
		fw = fh * ratio;
		needsPlaneRecalculation = true;
	}
	
	@Override
	public String toString() {
		
//		String result = "Frustrum[ left X="+pl[LEFT].getPoint().x()+" , "+
//				                 " right X="+pl[RIGHT].getPoint().x()+" , "+
//				                 " near Z="+pl[NEAR].getPoint().z()+" , "+
//				                 " far Z="+pl[FAR].getPoint().z()+" , "+
//				                 " top Y="+pl[TOP].getPoint().y()+" , "+
//				                 " bottom Y="+pl[BOTTOM].getPoint().y()+" , ";			                 
		String result = "Plane TOP    = "+planes[TOP]+"\n"+
						"Plane BOTTOM = "+planes[BOTTOM]+"\n"+
						"Plane LEFT   = "+planes[LEFT]+"\n"+
						"Plane RIGHT  = "+planes[RIGHT]+"\n"+
						"Plane NEAR   = "+planes[NEAR]+"\n"+
						"Plane FAR    = "+planes[FAR];
		return result;
	}
	
	
	public synchronized void setEyePosition(Vector4 eyePosition, Vector4 eyeTarget, Vector4 upVector) {
//		System.out.println("new frustum eye position: "+this.eyePosition+" -> "+eyePosition);
//		System.out.println("new frustum eye target: "+this.eyeTarget+" -> "+eyeTarget);
//		System.out.println("new frustum up vector: "+this.upVector+" -> "+upVector);
//		System.out.println("***");
		this.eyePosition.copyFrom( eyePosition );
		this.eyeTarget.copyFrom( eyeTarget );
		this.upVector.copyFrom( upVector );
		needsPlaneRecalculation = true;
	}
	
	public TestResult testContains(Matrix modelViewMatrix, Object3D object) 
	{
		if ( needsPlaneRecalculation ) {
			recalculatePlaneDefinitions();
		}
		
		final Vector4[] points;
		final BoundingBox boundingBox = object.getOrientedBoundingBox();
		if ( boundingBox != null ) {
			points = boundingBox.getPoints();
		} 
		else 
		{
			final float[] vertices = object.getVertices();
			points = new Vector4[ vertices.length / 4 ];
			final int len = points.length;
			for ( int ptr = 0 , target = 0 ; target < len ; ptr+=4 , target++) 
			{
				points[ target ] = new Vector4( vertices[ ptr ] , vertices[ptr+1] , vertices[ptr+2] );
			}
		}
		
		modelViewMatrix.multiplyInPlace( points );
		
		TestResult result = TestResult.OUTSIDE;
		for ( Vector4 point : points ) 
		{
			switch ( testContains( point ) ) 
			{
				case INSIDE:
				case INTERSECT:
					return TestResult.INSIDE;
				case OUTSIDE:
					break;
					default:
						throw new RuntimeException("Unreachable code reached");
			}
			
		}
		return result;
	}
	
	public TestResult testContains(Vector4 point) 
	{
		if ( needsPlaneRecalculation ) {
			recalculatePlaneDefinitions();
		}
		
		for ( int index = 0 ; index < planes.length ; index++ ) 
		{
			final Plane plane = planes[ index ];
			/* plane normal vectors point outside the frustum , 
			 * negative distance means the point is on the opposite side of the plane's normal vector
			 * and thus inside the view frustum
			 */
			if ( plane.distance( point ) < 0 ) { 
//				System.out.println("Point "+point+" is outside "+planeIndexToName( index)+" : "+plane);				
				return TestResult.OUTSIDE;
			}
		}
		return TestResult.INSIDE;
	}
	
	public void forceRecalculatePlaneDefinitions() {
		recalculatePlaneDefinitions();
	}
	
	private synchronized void recalculatePlaneDefinitions() 
	{
		// compute the Z axis of camera
		// this axis points in the opposite direction from 
		// the looking direction
		Vector4 Z = eyePosition.minus( eyeTarget );
		Z.normalizeInPlace();

		// X axis of camera with given "up" vector and Z axis
		Vector4 X = upVector.crossProduct( Z );
		X.normalizeInPlace();

		// the real "up" vector is the cross product of Z and X
		Vector4 Y = Z.crossProduct( X );

		// compute the centers of the near and far planes
		final Vector4 nc = eyePosition.minus( Z.multiply( nearD ) );
		final Vector4 fc = eyePosition.minus( Z.multiply( farD ) ); 

		planes[NEAR].setNormalAndPoint( Z.multiply(-1f) , nc);
		planes[FAR].setNormalAndPoint(  Z               , fc);

		Vector4 aux = ( nc.plus(Y.multiply(nh) ) ).minus( eyePosition );
		aux.normalizeInPlace();
		Vector4 normal = aux.crossProduct( X );
		planes[TOP].setNormalAndPoint(normal, nc.plus( Y.multiply( nh ) ) );

		aux = (nc.minus( Y.multiply(nh) ) ).minus( eyePosition );
		aux.normalizeInPlace();
		normal = X.crossProduct( aux );
		planes[BOTTOM].setNormalAndPoint(normal,nc.minus( Y.multiply( nh ) ));
		
		aux = (nc.minus( X.multiply(nw) ) ).minus(eyePosition);
		aux.normalizeInPlace();
		normal = aux.crossProduct( Y );
		planes[LEFT].setNormalAndPoint(normal, nc.minus( X.multiply( nw) ) );

		aux = (nc.plus( X.multiply(nw) )).minus( eyePosition );
		aux.normalizeInPlace();
		normal = Y.crossProduct( aux );
		planes[RIGHT].setNormalAndPoint(normal,nc.plus( X.multiply(nw)));	
		
		needsPlaneRecalculation = false;
		System.out.println( this );
	}
}
