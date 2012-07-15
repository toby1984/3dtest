package de.codesourcery.engine.linalg;

public class Plane {

	private Vector4 p1;
	private Vector4 p2;
	
	public Plane( Vector4 p1,Vector4 p2) {
		this.p1 = p1;
		this.p2 = p2;
	}
	
	public Vector4 getP1() {
		return p1;
	}
	
	public Vector4 getP2() {
		return p2;
	}
}
