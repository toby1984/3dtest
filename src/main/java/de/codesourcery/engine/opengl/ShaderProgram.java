package de.codesourcery.engine.opengl;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

import com.jogamp.common.nio.Buffers;

import de.codesourcery.engine.linalg.Matrix;
import de.codesourcery.engine.linalg.Vector4;
import de.codesourcery.engine.opengl.ProgramAttribute.AttributeType;

public class ShaderProgram {

	private final int programId;
	private final String identifier;
	
	public ShaderProgram(String identifier, int programId) {
		this.identifier = identifier;
		this.programId = programId;
	}

	public void use(GL3 gl) {
		gl.glUseProgram( programId );
		checkError( gl );
	}
	
	@Override
	public String toString() {
		return identifier;
	}
	
	public void bind(ProgramAttribute attr,VertexBuffer buffer,GL3 gl) 
	{
		if ( attr.getType() != AttributeType.VERTEX_POSITION ) {
			throw new IllegalArgumentException("Wrong attribute type - won't bind vertex buffer to attribute "+attr);
		}
		
		final int attrId = getVertexAttributeHandle( attr , gl );
		
		buffer.bind( gl );
		
		gl.glEnableVertexAttribArray( attrId );
//		gl.glEnableVertexAttribArray( buffer.bufferId );
		checkError( gl );
		
		gl.glVertexAttribPointer(
			    attrId , // attribute
			    4,                 // number of elements per vertex, here (x,y,z,w)
			    GL.GL_FLOAT,          // the type of each element
			    false,          // take our values as-is
			    0,                 // no extra data between each position
			    0                  // offset of first element
			  );		
		checkError( gl );
	}
	
	public void setUniform(ProgramAttribute attr,Matrix matrix,GL3 gl) 
	{
		final int attribute = getUniformHandle( attr , gl );
		gl.glUniformMatrix4fv(attribute, 1 , false , matrix.getData() , 0 );
	}
	
	public int getVertexAttributeHandle(ProgramAttribute attr,GL3 gl) 
	{
		final int handle = gl.glGetAttribLocation(programId,attr.getIdentifier());
		if ( handle == -1 ) {
			throw new RuntimeException("Failed to get handle for attribute '"+attr.getIdentifier()+"'");
		}
//		System.out.println( attr+" => handle "+handle);
		return handle;
	}	
	
	public int getUniformHandle(ProgramAttribute attr,GL3 gl) 
	{
		final int handle = gl.glGetUniformLocation(programId,attr.getIdentifier());
		if ( handle == -1 ) {
			throw new RuntimeException("Failed to get handle for uniform '"+attr.getIdentifier()+"'");
		}	
//		System.out.println( attr+" => handle "+handle);
		return handle;
	}
	
	private void checkError(GL3 gl) 
	{
		int errorCode = gl.glGetError();
		if ( errorCode != GL.GL_NO_ERROR ) {
			throw new RuntimeException("OpenGL error "+errorCode);
		}
	}		
	
	public void setUniform(ProgramAttribute attr,Vector4 vector,GL3 gl) {
		final int attributeHandle = getUniformHandle( attr, gl);
		gl.glUniform4fv(attributeHandle, 1, vector.getDataArray() , 0 );
		checkError( gl );
	}

	public void delete(GL3 gl) {
		gl.glDeleteProgram( programId );
	}	
}
