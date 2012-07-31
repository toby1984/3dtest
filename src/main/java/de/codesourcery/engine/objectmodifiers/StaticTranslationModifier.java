package de.codesourcery.engine.objectmodifiers;

import de.codesourcery.engine.linalg.LinAlgUtils;
import de.codesourcery.engine.linalg.Matrix;
import de.codesourcery.engine.render.Object3D;

public class StaticTranslationModifier implements IObjectModifier {

	private final Matrix matrix;
	
	public StaticTranslationModifier(float x, float y, float z) {
		this.matrix = LinAlgUtils.translationMatrix( x ,y , z );
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
