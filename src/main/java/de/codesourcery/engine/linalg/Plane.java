package de.codesourcery.engine.linalg;

public class Plane {

	private Vector4 normal;
	private Vector4 point;
	
	public Plane( Vector4 normal,Vector4 point) {
		this.normal = normal;
		this.point = point;
	}

	public Vector4 getNormal() {
		return normal;
	}
	
	public Vector4 getPoint() {
		return point;
	}
	
	public void setNormalAndPoint(Vector4 normal,Vector4 point) {
		this.normal = new Vector4( normal );
		this.point = new Vector4(point);
	}
	
	@Override
	public String toString() {
		return "normal vector = "+normal+" , point = "+point;
	}
}
