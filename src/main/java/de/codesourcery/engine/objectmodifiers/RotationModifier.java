package de.codesourcery.engine.objectmodifiers;

import de.codesourcery.engine.linalg.LinAlgUtils;
import de.codesourcery.engine.linalg.Matrix;
import de.codesourcery.engine.render.Object3D;

public class RotationModifier implements IObjectModifier {

	public static final byte X_AXIS = 1;
	public static final byte Y_AXIS = 2;
	public static final byte Z_AXIS = 4;
	
	private final int axisMask;
	
	private final float xInc;
	private final float yInc;
	private final float zInc;
	
	private float currentX;
	private float currentY;
	private float currentZ;
	
	public RotationModifier(int axisMask, float xInc, float yInc, float zInc) {
		this.axisMask = axisMask;
		this.xInc = xInc;
		this.yInc = yInc;
		this.zInc = zInc;
	}

	@Override
	public void apply(Object3D object) 
	{
		Matrix m = Matrix.identity();
		if ( ( axisMask & X_AXIS ) != 0 ) {
			m = m.multiply( LinAlgUtils.rotX( currentX ) );
		}
		if ( ( axisMask & Y_AXIS ) != 0 ) {
			m = m.multiply( LinAlgUtils.rotY( currentY ) );
		}
		if ( ( axisMask & Z_AXIS ) != 0 ) {
			m = m.multiply( LinAlgUtils.rotZ( currentZ ) );
		}		
		object.setModelMatrix( m.multiply( object.getModelMatrix() ) );
	}

	@Override
	public boolean isStatic() {
		return false;
	}

	@Override
	public boolean tick() 
	{
		currentX=clampAngle(currentX+xInc);
		currentY=clampAngle(currentY+yInc);
		currentZ=clampAngle(currentZ+zInc);
		return true;
	}

	private static float clampAngle(float f) 
	{
		return f;
	}	
}