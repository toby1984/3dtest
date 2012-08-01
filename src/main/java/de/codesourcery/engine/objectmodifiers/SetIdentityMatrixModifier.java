package de.codesourcery.engine.objectmodifiers;

import de.codesourcery.engine.linalg.Matrix;
import de.codesourcery.engine.render.Object3D;

public class SetIdentityMatrixModifier implements IObjectModifier {

	@Override
	public void apply(Object3D object) {
		object.setModelMatrix( Matrix.identity() );
	}

	@Override
	public boolean isStatic() {
		return false;
	}

	@Override
	public boolean tick() {
		return true;
	}

}
