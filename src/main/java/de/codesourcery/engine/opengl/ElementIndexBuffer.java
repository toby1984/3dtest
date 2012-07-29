package de.codesourcery.engine.opengl;

import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

import com.jogamp.common.nio.Buffers;

public class ElementIndexBuffer extends Buffer {

	public ElementIndexBuffer(int bufferId) {
		super(BufferType.ELEMENT_INDICES, bufferId);
	}
	
	public void load(int[] indices,GL3 gl) 
	{
        final IntBuffer buffer = Buffers.newDirectIntBuffer( indices.length );
        
        buffer.put( indices );
        
        buffer.rewind();

        // transfer data to VBO
        final int numBytes = buffer.capacity() * 4; // one float = 32 bit = 4 bytes
        bind( gl );
        gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, numBytes, buffer , GL3.GL_STREAM_DRAW );
	}
	
	public void bind(GL3 gl) {
		gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, bufferId );
	}	

	@Override
	public void unbind(GL3 gl) {
		gl.glBindBuffer( GL.GL_ELEMENT_ARRAY_BUFFER , 0 );
	}
}