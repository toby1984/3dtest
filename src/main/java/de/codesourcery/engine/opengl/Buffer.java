package de.codesourcery.engine.opengl;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

public abstract class Buffer {

	protected final int bufferId;
	protected final BufferType type;
	
	public static enum BufferType {
		VERTEX,
		ELEMENT_INDICES;
	}
	
	public Buffer(BufferType type, int bufferId) {
		this.type = type;
		this.bufferId = bufferId;
	}
	
	public abstract void bind(GL3 gl);
	
	public abstract void unbind(GL3 gl);
	
	protected final void checkError(GL3 gl) 
	{
		int errorCode = gl.glGetError();
		if ( errorCode != GL.GL_NO_ERROR ) {
			throw new RuntimeException("OpenGL error "+errorCode);
		}
	}		
}
