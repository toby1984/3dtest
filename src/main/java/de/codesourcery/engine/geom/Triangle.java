package de.codesourcery.engine.geom;

import de.codesourcery.engine.linalg.Vector4;

public class Triangle implements IConvexPolygon {
    
	private final Vector4[] points = new Vector4[3];
    
    private int color;
    
    public Triangle(Vector4 p1,Vector4 p2,Vector4 p3) {
        points[0] = p1;
        points[1] = p2;
        points[2] = p3;
    }
    
    public Triangle(int color, Vector4 p1,Vector4 p2,Vector4 p3) {
    	this.color = color;
        points[0] = p1;
        points[1] = p2;
        points[2] = p3;    	
    }    
    
    public void setColor(int color) {
		this.color = color;
	}
    
    public int getColor() {
    	return color;
    }
    
    public Vector4 p1() {
        return points[0];
    }
    
    public Vector4 p2() {
        return points[1];
    }
    
    public Vector4 p3() {
        return points[2];
    }    
    
    @Override
    public String toString()
    {
        return p1()+" -> "+p2()+" -> "+p3()+" -> "+p1();
    }

	@Override
	public Vector4[] getAllPoints() {
		return points;
	}

}