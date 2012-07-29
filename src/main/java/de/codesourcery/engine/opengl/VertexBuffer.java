package de.codesourcery.engine.opengl;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

import com.jogamp.common.nio.Buffers;

import de.codesourcery.engine.linalg.Vector4;

public class VertexBuffer extends Buffer {

	public VertexBuffer(int bufferId) 
	{
		super(BufferType.VERTEX, bufferId);
	}

	public void load(Vector4[] data, GL3 gl) 
	{
        final FloatBuffer buffer = Buffers.newDirectFloatBuffer( data.length * 4 );
        for (Vector4 vertex : data )
        {
        	buffer.put( vertex.getDataArray() , vertex.getDataOffset() , 4 );
        }
        
        buffer.rewind();

        // transfer data to VBO
        final int numBytes = buffer.capacity() * 4; // one float = 32 bit = 4 bytes
        bind( gl );
        
        gl.glBufferData(GL.GL_ARRAY_BUFFER, numBytes, buffer , GL3.GL_STREAM_DRAW );
        checkError( gl );
	}
	
	public void bind(GL3 gl) {
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufferId );
		checkError( gl );
	}
	
	public void load(float[] data, GL3 gl) 
	{
		// input is assumed to be in homogenous coordinates ( x,y,z,w)
        final FloatBuffer buffer = Buffers.newDirectFloatBuffer( data.length );
        buffer.put( data );
        buffer.rewind();

        // transfer data to VBO
        final int numBytes = buffer.capacity() * 4; // one float = 32 bit = 4 bytes
        bind( gl );
        
        gl.glBufferData(GL.GL_ARRAY_BUFFER, numBytes, buffer , GL3.GL_STREAM_DRAW );
        checkError( gl );
	}	
	
	@Override
	public void unbind(GL3 gl) {
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER , 0 );
	}	
}