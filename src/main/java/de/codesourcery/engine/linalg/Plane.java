package de.codesourcery.engine.linalg;

public class Plane {

	private Vector4 normal;
	private Vector4 point;
	private float d;
	
	public Plane( Vector4 normal,Vector4 point) {
		this.normal = new Vector4( normal );
		this.point = new Vector4( point );
		/*
		 * D = – n . p0 (dot product).
		 */
		d = this.normal.multiply( -1 ).dotProduct( this.point );
	}

	public Vector4 getNormal() {
		return normal;
	}
	
	public Vector4 getPoint() {
		return point;
	}
	
	public float distance(Vector4 point) 
	{
		/*
		 *  dist = A * px + B * py + C * pz + D 
		 *       = n . p  + D
		 */
		return this.normal.dotProduct( point ) + d;
	}
	
	public void setNormalAndPoint(Vector4 normal,Vector4 point) {
		this.normal = new Vector4( normal );
		this.point = new Vector4(point);
		
		/*
		 * D = – n . p0 (dot product).
		 */
		d = this.normal.multiply( -1 ).dotProduct( this.point );		
	}
	
	@Override
	public String toString() {
		return "normal vector = "+normal+" , point = "+point;
	}
}
