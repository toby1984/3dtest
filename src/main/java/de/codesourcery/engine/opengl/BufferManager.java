package de.codesourcery.engine.opengl;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

public class BufferManager {

	public static VertexBuffer allocateVertexBuffer(GL3 gl) 
	{
		return new VertexBuffer( allocateBufferHandle( gl ));
	}
	
	public static int allocateBufferHandle(GL3 gl) {
        final int[] bufferHandle = new int[1];
        gl.glGenBuffers(1, bufferHandle , 0);
        checkError(gl);
        if ( bufferHandle[0] < 0 ) {
        	throw new RuntimeException("Failed to allocate buffer");
        }        
        
		return bufferHandle[0];
	}
	
	public static ElementIndexBuffer allocateElementIndexBuffer(GL3 gl) 
	{
		return new ElementIndexBuffer( allocateBufferHandle( gl ) );
	}	
	
	private static void checkError(GL3 gl) 
	{
		int errorCode = gl.glGetError();
		if ( errorCode != GL.GL_NO_ERROR ) {
			throw new RuntimeException("OpenGL error "+errorCode);
		}
	}	
}