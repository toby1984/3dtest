package de.codesourcery.engine.objectmodifiers;

import de.codesourcery.engine.render.Object3D;

public interface IObjectModifier {

	public void apply(Object3D object);
	
	public boolean isStatic();
	
	/**
	 * 
	 * @return <code>true</code> if calling {@link #apply(Object3D)} after this
	 * method returns would modify the Object3D
	 */
	public boolean tick();
}
