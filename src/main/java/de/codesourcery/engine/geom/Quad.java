package de.codesourcery.engine.geom;

import de.codesourcery.engine.linalg.Vector4;

public final class Quad implements IConvexPolygon {

	private final Vector4[] points = new Vector4[4];
	private int color;
	
    public Quad(Vector4 p1,Vector4 p2,Vector4 p3,Vector4 p4) {
    	points[0] = p1;
    	points[1] = p2;
    	points[2] = p3;
    	points[3] = p4;
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

	@Override
	public final byte getVertexCount() {
		return 4;
	}
}
