package de.codesourcery.engine.objectmodifiers;

import de.codesourcery.engine.linalg.Matrix;
import de.codesourcery.engine.linalg.Vector4;
import de.codesourcery.engine.render.Object3D;

public class ExtractTranslationModifier implements IObjectModifier {

	private final Vector4 col0 = new Vector4(1,0,0,0);
	private final Vector4 col1 = new Vector4(0,1,0,0);
	private final Vector4 col2 = new Vector4(0,0,1,0);
	
	@Override
	public void apply(Object3D object) {
		
		Matrix m = new Matrix( object.getModelMatrix() );
		Vector4 col3 = m.getColumn( 3 );
		m.setColumns( col0 , col1 , col2 , col3 );
		object.setModelMatrix( m );
	}

	@Override
	public boolean isStatic() {
		return false;
	}

	@Override
	public boolean tick() 
	{
		return true;
	}

}
