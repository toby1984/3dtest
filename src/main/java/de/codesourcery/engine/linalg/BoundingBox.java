package de.codesourcery.engine.linalg;

import java.util.Arrays;

import de.codesourcery.engine.geom.Quad;
import de.codesourcery.engine.render.Object3D;

public class BoundingBox {

	private Vector4 center;
	private Vector4 xAxis, yAxis, zAxis;	
	private double width;
	private double height;
	private double depth;
	private final double[] points;
	private final boolean isAxisAligned;
	
	public BoundingBox(Vector4 center, Vector4 xAxis, Vector4 yAxis, Vector4 zAxis,double width,double height,double depth,boolean isAxisAligned) 
	{
		this.center = center;
		
		this.xAxis = xAxis.normalize();
		this.yAxis = yAxis.normalize();
		this.zAxis = zAxis.normalize();
		
		this.width = width;
		this.height = height;
		this.depth = depth;
		
		this.points = calcPoints();
		this.isAxisAligned = isAxisAligned;
	}
	
	public boolean isAxisAligned() {
		return isAxisAligned;
	}
	
	private double[] calcPoints() 
	{
		double xLeft = center.minus( xAxis.multiply( width/2 ) ).x();
		double xRight = center.plus( xAxis.multiply( width/2 ) ).x();
		
		double yTop = center.plus( yAxis.multiply( height/2 ) ).y();
		double yBottom = center.minus( yAxis.multiply( height/2 ) ).y();
		
		double zNear = center.plus( zAxis.multiply( depth/2 ) ).z();
		double zFar = center.minus( zAxis.multiply( depth/2 ) ).z();
	
		double[] result = new double[8*4];
		
		result[ 0 ] = xLeft;
		result[ 1 ] = yTop;
		result[ 2 ] = zNear;
		
		result[ 4 ] = xLeft;
		result[ 5 ] = yBottom;
		result[ 6 ] = zNear;
		
		result[ 8 ] = xRight;
		result[ 9  ] = yBottom;
		result[ 10 ] = zNear;

		result[ 12 ] = xRight;
		result[ 13 ] = yTop;
		result[ 14 ] = zNear;

		result[ 16 ] = xRight;
		result[ 17 ] = yTop;
		result[ 18 ] = zFar;

		result[ 20 ] = xRight;
		result[ 21 ] = yBottom;
		result[ 22 ] = zFar;

		result[ 24 ] = xLeft;
		result[ 25 ] = yBottom;
		result[ 26 ] = zFar;

		result[ 28 ] = xLeft;
		result[ 29 ] = yTop;
		result[ 30 ] = zFar;
		
		return result;
	}
	
	public Vector4[] getPoints() 
	{
		final Vector4[] result = new Vector4[8];
		int ptr = 0;
		for ( int i = 0 ; i < this.points.length ; i+= 4 ) {
			result[ptr++] = new Vector4( points[i] , points[i+1] ,points[i+2] );
		}
		return result;
	}
	
	public double[] getVertices() {
		return this.points;
	}
	
	private Quad[] createQuads() 
	{
		return createQuads( getPoints() );
	}
	
	public Vector4[] getMinMax() {
		
		double xMin = this.points[0];
		double xMax = xMin;

		double yMin = this.points[1];
		double yMax = yMin;

		double zMin = this.points[2];
		double zMax = zMin;

		for ( int i = 1 ; i < this.points.length ; i+=4 ) 
		{
			final double x = this.points[i];
			final double y = this.points[i+1];
			final double z = this.points[i+2];
			
			if ( x < xMin ) {
				xMin = x;
			}
			if ( y < yMin ) {
				yMin = y;
			}
			if ( z < zMin ) {
				zMin = z;
			}			
			if ( x > xMax ) {
				xMax = x;
			}		
			if ( y > yMax ) {
				yMax = y;
			}
			if ( z > zMax ) {
				zMax = z;
			}			
		}		
		
		return new Vector4[] { new Vector4( xMin,yMin,zMin ) , new Vector4( xMax,yMax,zMax ) }; 
	}
	
	private Quad[] createQuads(Vector4[] points) 
	{
		Vector4 p1 = points[0];
		Vector4 p2 = points[1];
		Vector4 p3 = points[2];
		Vector4 p4 = points[3];
		
		Vector4 p5 = points[4];
		Vector4 p6 = points[5];
		Vector4 p7 = points[6];
		Vector4 p8 = points[7];
		
		System.out.println("p1 = "+p1);
		System.out.println("p2 = "+p2);
		System.out.println("p3 = "+p3);
		System.out.println("p4 = "+p4);
		
		System.out.println("p5 = "+p5);
		System.out.println("p6 = "+p6);
		System.out.println("p7 = "+p7);
		System.out.println("p8 = "+p8);
		
		Quad[] result = new Quad[6];
		
		result[0] = new Quad( p1,p2,p3,p4); // front
		result[1] = new Quad( p5,p6,p7,p8); // back
		
		result[2] = new Quad(p4,p3,p6,p5); // right
		result[3] = new Quad(p8,p7,p2,p1); // left
		
		result[4] = new Quad(p8,p1,p4,p5); // top
		result[5] = new Quad(p2,p7,p6,p3); // bottom
		return result;
	}
	
	public Vector4 getXAxis()
    {
        return xAxis;
    }
	
	public Vector4 getYAxis()
    {
        return yAxis;
    }
	
	public Vector4 getZAxis()
    {
        return zAxis;
    }
	
	public Object3D toObject3D() 
	{
		final Object3D result = new Object3D();
		result.setIdentifier("bounding box");
		result.setRenderWireframe( true );
		
		result.setPrimitives( Arrays.asList( createQuads() ) , false );
		
		final Matrix m = LinAlgUtils.translationMatrix( center.x() , center.y() , center.z() ); 
		
		result.setModelMatrix( m );
		
		return result;
	}
	
	public Vector4 getCenter() {
		return center;
	}
}
