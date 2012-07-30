package de.codesourcery.engine.opengl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GL3;
import javax.media.opengl.GLException;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class TextureManager {

	private int textureId = 0;
	private final Map<Integer,Texture> textures = new HashMap<>();
	
	public int loadTexture(File file) throws GLException, IOException {
		Texture texture = TextureIO.newTexture( file , false );
		final int result = textureId;
		textureId++;
		textures.put( result , texture );
		return result;
	}
	
	public Texture getTexture(int id) {
		Texture result = textures.get( id );
		if ( result == null ) {
			throw new IllegalArgumentException("No texture bound to ID "+id);
		}
		return result;
	}
	
	public void destroy(int id,GL3 gl) {
		Texture texture = getTexture( id );
		textures.remove( id );
		texture.destroy( gl );
	}
}
