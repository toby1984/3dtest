package de.codesourcery.engine.render;

public abstract class MouseMotionTracker {

	private float sensitity = 0.5f;
	
	private int xRef = -1;
	private int yRef = -1;
	
	private boolean invertY = false;
	
	private boolean trackingEnabled = false;
	private final Camera camera;
	
	public MouseMotionTracker(Camera camera) {
		this.camera = camera;
	}
	
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
	
	public void setInvertY(boolean invertY) {
		this.invertY = invertY;
	}
	
	public boolean isTrackingEnabled() {
		return trackingEnabled;
	}
	
	public void reset() {
		xRef = -1;
		yRef = -1;
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
		
		float rotY = (x - xRef)*sensitity;
		float rotX = (y - yRef)*sensitity;

		if ( invertY ) {
			rotX *= -1;
		}
		camera.rotate(rotY, rotX);
		
		xRef = x;
		yRef = y;
	}
}