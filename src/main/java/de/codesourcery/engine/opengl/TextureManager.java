package de.codesourcery.engine.opengl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GL3;
import javax.media.opengl.GLException;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class TextureManager {

	private final Map<String,Texture> texturesByName = new HashMap<>();	

	private Texture loadTexture(String textureName) 
	{
		final String path ="/textures/"+textureName;
		final InputStream in = getClass().getResourceAsStream( path );
		if ( in == null ) {
			throw new RuntimeException("Unable to find texture "+path);
		}
		try {
			return loadTexture( textureName , in );
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Texture loadTexture(String textureName , InputStream in) throws GLException, IOException 
	{
		try { 
			Texture texture = TextureIO.newTexture( in, false , null );
			texturesByName.put( textureName , texture );
			return texture;
		} 
		finally 
		{
			try { 
				in.close(); 
			} 
			catch(Exception e) {
			}
		}	
	}

	public Texture getTexture(String name) 
	{
		return getTexture(name,true);
	}
	
	private Texture getTexture(String name,boolean loadOnDemand) 
	{
		Texture result = texturesByName.get( name );
		if ( result == null && loadOnDemand ) {
			result = loadTexture( name );
		}
		return result;
	}

	public void destroy(String name,GL3 gl) {
		Texture texture = getTexture( name , false );
		if ( texture != null ) {
			texturesByName.remove( name );
			texture.destroy( gl );
		}
	}
}