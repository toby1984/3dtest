package de.codesourcery.engine.linalg;

import de.codesourcery.engine.math.Constants;
import de.codesourcery.engine.render.Object3D;


public final class Frustum  
{  
	private static final int TOP=0, BOTTOM =1, LEFT=2,RIGHT=3, NEARP=4, FARP=5;

	public static enum TestResult {
		OUTSIDE, INTERSECT, INSIDE
	}

	private Plane[] pl = new Plane[6];

	Vector4 ntl,ntr,nbl,nbr,ftl,ftr,fbl,fbr;
	float nearD, farD, ratio, angle,tang;
	float nw,nh,fw,fh;	
	
	private final Vector4 eyePosition = new Vector4(0,0,0);
	private final Vector4 eyeTarget = new Vector4(0,0,-1);
	private final Vector4 upVector = new Vector4(0,1,0);

	private volatile boolean needsPlaneRecalculation = true;
	
	public Frustum() {
		for ( int i = 0 ; i < pl.length ; i++ ) {
			pl[i] = new Plane(new Vector4(),new Vector4());
		}
	}
	
	public synchronized void setCamInternals(float angle, float ratio, float nearD, float farD) {

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
		String result = "Plane TOP    = "+pl[TOP]+"\n"+
						"Plane BOTTOM = "+pl[BOTTOM]+"\n"+
						"Plane LEFT   = "+pl[LEFT]+"\n"+
						"Plane RIGHT  = "+pl[RIGHT]+"\n"+
						"Plane NEAR   = "+pl[NEARP]+"\n"+
						"Plane FAR    = "+pl[FARP];
		return result;
	}
	
	
	public synchronized void setCamDef(Vector4 eyePosition, Vector4 eyeTarget, Vector4 upVector) {
		eyePosition.copyInto( this.eyePosition.getDataArray() , 0 );
		eyeTarget.copyInto( this.eyeTarget.getDataArray() , 0 );
		upVector.copyInto( this.upVector.getDataArray()  , 0 );
		needsPlaneRecalculation = true;
	}
	
	public TestResult testContains(Object3D object) {
		if ( needsPlaneRecalculation ) {
			recalculatePlaneDefinitions();
		}
		return TestResult.INSIDE;
	}
	
	public TestResult testContains(Vector4 point) {
		if ( needsPlaneRecalculation ) {
			recalculatePlaneDefinitions();
		}
		return TestResult.INSIDE;
	}
	
	public void forceRecalculatePlaneDefinitions() {
		recalculatePlaneDefinitions();
	}
	
	private synchronized void recalculatePlaneDefinitions() 
	{
		Vector4 nc,fc,X,Y,Z;

		// compute the Z axis of camera
		// this axis points in the opposite direction from 
		// the looking direction
		Z = eyePosition.minus( eyeTarget );
		Z.normalizeInPlace();

		// X axis of camera with given "up" vector and Z axis
		X = upVector.crossProduct( Z );
		X.normalizeInPlace();

		// the real "up" vector is the cross product of Z and X
		Y = Z.crossProduct( X );

		// compute the centers of the near and far planes
		nc = eyePosition.minus( Z.multiply( nearD ) );
		fc = eyePosition.minus( Z.multiply( farD ) ); 

		pl[NEARP].setNormalAndPoint( Z.multiply(-1f) , nc);
		pl[FARP].setNormalAndPoint(  Z               , fc);

		Vector4 aux,normal;

		aux = ( nc.plus(Y.multiply(nh) ) ).minus( eyePosition );
		aux.normalizeInPlace();
		normal = aux.crossProduct( X );
		pl[TOP].setNormalAndPoint(normal, nc.plus( Y.multiply( nh ) ) );

		aux = (nc.minus( Y.multiply(nh) ) ).minus( eyePosition );
		aux.normalizeInPlace();
		normal = X.crossProduct( aux );
		pl[BOTTOM].setNormalAndPoint(normal,nc.minus( Y.multiply( nh ) ));
		
		aux = (nc.minus( X.multiply(nw) ) ).minus(eyePosition);
		aux.normalizeInPlace();
		normal = aux.crossProduct( Y );
		pl[LEFT].setNormalAndPoint(normal, nc.minus( X.multiply( nw) ) );

		aux = (nc.plus( X.multiply(nw) )).minus( eyePosition );
		aux.normalizeInPlace();
		normal = Y.crossProduct( aux );
		pl[RIGHT].setNormalAndPoint(normal,nc.plus( X.multiply(nw)));	
		
		needsPlaneRecalculation = false;
		System.out.println( this );
	}
}
