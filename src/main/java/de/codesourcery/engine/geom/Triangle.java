package de.codesourcery.engine.geom;

import java.awt.Color;

import de.codesourcery.engine.LinAlgUtils;

public final class Triangle implements ITriangle {
    
    private final Vector4 p1;
    private final Vector4 p2;
    private final Vector4 p3;
    
    private Color color;
    
    public Triangle(Vector4 p1,Vector4 p2,Vector4 p3) {
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
    }
    
    public Triangle(Color color, Vector4 p1,Vector4 p2,Vector4 p3) {
    	this.color = color;
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
    }    
    
    public Color getColor() {
    	return color;
    }
    
    public Vector4 p1() {
        return p1;
    }
    
    public Vector4 p2() {
        return p2;
    }
    
    public Vector4 p3() {
        return p3;
    }    
    
    @Override
    public String toString()
    {
        return p1+" -> "+p2+" -> "+p3+" -> "+p1;
    }

	public double getMaxW() 
	{
		return ( p1.w() + p2.w() + p3.w() ) / 3;
	}    
	
	public Vector4 findFarestVertex(Vector4 reference) {
	    return LinAlgUtils.findFarestVertex( reference ,p1 ,p2,p3);
	}
	
}