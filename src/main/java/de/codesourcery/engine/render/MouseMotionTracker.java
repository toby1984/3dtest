package de.codesourcery.engine.render;

import de.codesourcery.engine.linalg.Vector4;


public abstract class MouseMotionTracker {

	private float sensitity = 0.001f;
	
	private int xRef = -1;
	private int yRef = -1;
	
	private float angleXZ = 0.0f; // angle in XZ plane, in degrees
	private float angleY = 0.0f; // angle in Y plane

	private boolean trackingEnabled = true;
	
	private float defaultAngleXZ = 0.0f;
	private float defaultAngleY = 0.0f;
	
	public void setTrackingEnabled(boolean trackingEnabled) {
		this.trackingEnabled = trackingEnabled;
		if ( ! trackingEnabled ) {
			xRef = -1;
			yRef = -1;
		} 
	}
	
	public void setSensitity(float sensitity) {
		this.sensitity = sensitity;
	}
	
	public float getSensitity() {
		return sensitity;
	}
	
	public boolean isTrackingEnabled() {
		return trackingEnabled;
	}
	
	public void reset() {
		xRef = -1;
		yRef = -1;
		angleXZ = defaultAngleXZ;
		angleY = defaultAngleY; 
	}
	
	public void mouseMoved(int x , int y) 
	{
		if ( ! trackingEnabled ) {
			return;
		}
		
		if ( xRef == -1 ) {
			xRef = x;
			yRef = y;
			return;
		}
		
		float deltaX = (x - xRef)*sensitity;
		float deltaY = (yRef-y)*sensitity;

		angleXZ += deltaX;
		
		// clamp angle to 0..360 degrees to avoid loss of precision
		if ( angleXZ >= 360.0 ) {
			angleXZ -= 360;
		}
		if ( angleXZ < 0 ) {
			angleXZ += 360;
		}
		
		angleY += deltaY;
		
		// restrict Y angle to 0 .. 89.999 degrees
		if ( (angleY >= 90.0) && (angleY <= 270.0 ) ) {
			angleY = 89.999f;
		} 
		else if ( angleY <= 0 ) {
			angleY += 360;
		}			
		
		xRef = x;
		yRef = y;
		
		moveEyeTarget();
	}
	
	private void moveEyeTarget() {
		
		// Calculate X/Y/Z on unit sphere (=view vector)
		final float y = (float) Math.sin( angleY * 0.5f * Math.PI );
		final float x = (float) Math.sin( angleXZ * 0.5f * Math.PI  );
		final float z = (float) -Math.cos( angleXZ * 0.5f * Math.PI );
		updateEyeTarget( x,y,z );
	}
	
	public void setViewOrientation(Vector4 vector) 
	{
	    angleY = (float) ( (2f*Math.asin( vector.y() )) / Math.PI);
	    angleXZ = (float) ( (2f*Math.asin( vector.x() )) / Math.PI );
	    
	    this.defaultAngleY = angleY;
	    this.defaultAngleXZ = angleXZ;
	}
	
	protected abstract void updateEyeTarget(float x,float y, float z);	
}