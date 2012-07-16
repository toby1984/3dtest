package de.codesourcery.engine.linalg;

import java.util.Arrays;

import de.codesourcery.engine.geom.Quad;
import de.codesourcery.engine.render.Object3D;

public class BoundingBox {

	private Vector4 center;
	
	private Quad front;
	private Quad back;
	
	private Quad left;
	private Quad right;
	
	private Quad top;
	private Quad bottom;

	private Vector4 xAxis, yAxis, zAxis;	
	
	public BoundingBox(Vector4 center, Vector4 xAxis, Vector4 yAxis, Vector4 zAxis,double width,double height,double depth) 
	{
		this.center = center;
		
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		this.zAxis = zAxis;
		
		double xLeft = center.minus( xAxis.normalize().multiply( width/2 ) ).x();
		double xRight = center.plus( xAxis.normalize().multiply( width/2 ) ).x();
		
		double yTop = center.plus( yAxis.normalize().multiply( height/2 ) ).y();
		double yBottom = center.minus( yAxis.normalize().multiply( height/2 ) ).y();
		
		double zNear = center.plus( zAxis.normalize().multiply( depth/2 ) ).z();
		double zFar = center.minus( zAxis.normalize().multiply( depth/2 ) ).z();
		
		Vector4 p1 = new Vector4( xLeft , yTop , zNear );
		Vector4 p2 = new Vector4( xLeft , yBottom , zNear );
		Vector4 p3 = new Vector4( xRight , yBottom , zNear );
		Vector4 p4 = new Vector4( xRight , yTop , zNear );
		
		Vector4 p5 = new Vector4( xRight , yTop , zFar );
		Vector4 p6 = new Vector4( xRight , yBottom , zFar );
		Vector4 p7 = new Vector4( xLeft , yBottom , zFar );
		Vector4 p8 = new Vector4( xLeft , yTop , zFar );		
		
		System.out.println("p1 = "+p1);
		System.out.println("p2 = "+p2);
		System.out.println("p3 = "+p3);
		System.out.println("p4 = "+p4);
		
		System.out.println("p5 = "+p5);
		System.out.println("p6 = "+p6);
		System.out.println("p7 = "+p7);
		System.out.println("p8 = "+p8);
		
		front = new Quad( p1,p2,p3,p4);
		back = new Quad( p5,p6,p7,p8);
		
		right = new Quad(p4,p3,p6,p5);
		left = new Quad(p8,p7,p2,p1);
		
		top = new Quad(p8,p1,p4,p5);
		bottom = new Quad(p2,p7,p6,p3);
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
	
	public Object3D toObject3D() {
		
		final Object3D result = new Object3D();
		result.setIdentifier("bounding box");
		result.setRenderWireframe( true );
		result.setPrimitives( Arrays.asList( front,back,right,left,top,bottom ) , false );
		
		final Matrix m = LinAlgUtils.translationMatrix( center.x() , center.y() , center.z() ); 
		result.setModelMatrix( m );
		
		return result;
	}
	
	public Vector4 getCenter() {
		return center;
	}
}
