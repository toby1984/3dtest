package de.codesourcery.engine.render;


public abstract class MouseMotionTracker {

	private double sensitity = 0.001;
	
	private int xRef = -1;
	private int yRef = -1;
	
	private double angleXZ = 0.0; // angle in XZ plane, in degrees
	private double angleY = 0.0; // angle in Y plane

	private boolean trackingEnabled = true;
	
	public void setTrackingEnabled(boolean trackingEnabled) {
		this.trackingEnabled = trackingEnabled;
		if ( ! trackingEnabled ) {
			xRef = -1;
			yRef = -1;
		} 
	}
	
	public void setSensitity(double sensitity) {
		this.sensitity = sensitity;
	}
	
	public double getSensitity() {
		return sensitity;
	}
	
	public boolean isTrackingEnabled() {
		return trackingEnabled;
	}
	
	public void reset() {
		xRef = -1;
		yRef = -1;
		angleXZ = 0.0;
		angleY = 0.0;
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
		
		double deltaX = (x - xRef)*sensitity;
		double deltaY = (yRef-y)*sensitity;

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
			angleY = 89.999;
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
		final double y = Math.sin( angleY * 0.5 * Math.PI );
		final double x = Math.sin( angleXZ * 0.5 * Math.PI  );
		final double z = -Math.cos( angleXZ * 0.5 * Math.PI );
		updateEyeTarget( x,y,z );
	}
	
	protected abstract void updateEyeTarget(double x,double y, double z);	
}