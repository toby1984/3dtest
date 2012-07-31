package de.codesourcery.engine.objectmodifiers;

import de.codesourcery.engine.linalg.LinAlgUtils;
import de.codesourcery.engine.linalg.Matrix;
import de.codesourcery.engine.render.Object3D;

public class StaticScalingModifier implements IObjectModifier {

	private final Matrix matrix;
	
	public StaticScalingModifier(float x, float y, float z) {
		this.matrix = LinAlgUtils.scalingMatrix( x ,y , z );
	}

	@Override
	public boolean isStatic() {
		return true;
	}

	@Override
	public boolean tick() {
		return false;
	}

	@Override
	public void apply(Object3D object) 
	{
		object.setModelMatrix( matrix.multiply( object.getModelMatrix() ) );
	}
	
}
