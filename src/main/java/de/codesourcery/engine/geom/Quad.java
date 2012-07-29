package de.codesourcery.engine.geom;

import java.util.ArrayList;
import java.util.List;

import de.codesourcery.engine.linalg.Vector4;

public final class Quad implements IConvexPolygon {

	private Vector4[] points = new Vector4[4];
	private int color=0xffffff;
	
    public Quad(Vector4[] points) {
    	this.points = points;
    }
    
    public Quad(Vector4 p1,Vector4 p2,Vector4 p3,Vector4 p4) {
    	points[0] = p1;
    	points[1] = p2;
    	points[2] = p3;
    	points[3] = p4;
    }
    
    public Triangle[] toTriangles() 
    {
    	final Vector4 p1 = points[0];
    	final Vector4 p2 = points[1];
    	final Vector4 p3 = points[2];
    	final Vector4 p4 = points[3];
    	
    	return new Triangle[] {new Triangle(p1,p2,p3) , 
    			new Triangle( p1,p3,p4 ) };
    }
    
    public static List<Triangle> toTriangles(List<Quad> quads) 
    {
    	List<Triangle> result = new ArrayList<>();
    	for( Quad q : quads ) 
    	{
    		final Triangle[] triangles = q.toTriangles();
    		result.add( triangles[0] );
    		result.add( triangles[1] );
    	}
    	return result;
    }
    
    public void reverseVertices() 
    {
    	// swap 0 <-> 3
		Vector4 tmp = points[0];
		points[0] = points[3];
		points[3] = tmp;
		
		// swap 1 <-> 2
		tmp = points[1];
		points[1] = points[2];
		points[2] = tmp;
    }
    
	@Override
	public Vector4 p1() {
		return points[0];
	}

	@Override
	public Vector4 p2() {
		return points[1];
	}

	@Override
	public Vector4 p3() {
		return points[2];
	}

	@Override
	public Vector4[] getAllPoints() {
		return points;
	}

	public void setColor(int color) {
		this.color = color;
	}
	
	@Override
	public int getColor() {
		return color;
	}

}
