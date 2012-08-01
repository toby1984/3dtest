package de.codesourcery.engine.opengl;

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

import de.codesourcery.engine.linalg.Matrix;
import de.codesourcery.engine.linalg.Vector4;
import de.codesourcery.engine.opengl.ProgramAttribute.AttributeType;

public class ShaderProgram {

	private static final boolean FAIL_ON_UNKNOWN_SHADER_PARAMETER = true;
	
	private final int programId;
	private final String identifier;
	
	private final List<ProgramAttribute> uniformAttributes = new ArrayList<ProgramAttribute>();
	private final List<ProgramAttribute> vertexAttributes = new ArrayList<ProgramAttribute>();
	
	public ShaderProgram(String identifier, int programId,List<ProgramAttribute> attributes) {
		this.identifier = identifier;
		this.programId = programId;
		for( ProgramAttribute a : attributes ) {
			if ( a.getType().isUniform() ) {
				uniformAttributes.add( a );
			} else {
				vertexAttributes.add( a );
			}
		}
	}
	
	public void use(GL3 gl) {
		gl.glUseProgram( programId );
	}
	
	public void setupUniforms(IUniformAttributeProvider provider ) 
	{
		for ( ProgramAttribute attr : uniformAttributes ) {
			provider.setUniform( this , attr );
		}
	}
	
	public void setupVertexAttributes(IVertexAttributeProvider provider ) 
	{
		for ( ProgramAttribute attr : vertexAttributes ) {
			provider.setVertexAttribute( this , attr ); 
		}
	}	
	
	public int getProgramId() {
		return programId;
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
		
		gl.glVertexAttribPointer(
			    attrId , // attribute
			    4,                 // number of elements per vertex, here (x,y,z,w)
			    GL.GL_FLOAT,          // the type of each element
			    false,          // take our values as-is
			    0,                 // no extra data between each position
			    0                  // offset of first element
			  );		
	}
	
	public void setUniform(ProgramAttribute attr,Matrix matrix,GL3 gl) 
	{
		final int attribute = getUniformHandle( attr , gl );
		if ( FAIL_ON_UNKNOWN_SHADER_PARAMETER || attribute != -1 ) {
			gl.glUniformMatrix4fv(attribute, 1 , false , matrix.getData() , 0 );
		}
	}
	
	public void setUniform(ProgramAttribute attr,float value,GL3 gl) 
	{
		final int attribute = getUniformHandle( attr , gl );
		if ( FAIL_ON_UNKNOWN_SHADER_PARAMETER || attribute != -1 ) {
			gl.glUniform1f(attribute, value );
		}
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
	
	private int getUniformHandle(ProgramAttribute attr,GL3 gl) 
	{
		final int handle = gl.glGetUniformLocation(programId,attr.getIdentifier());
		if ( handle == -1 && FAIL_ON_UNKNOWN_SHADER_PARAMETER ) {
			throw new RuntimeException("Failed to get handle for uniform '"+attr.getIdentifier()+"'");
		}	
		return handle;
	}
	
	public void setUniform(ProgramAttribute attr,Vector4 vector,GL3 gl) {
		final int attributeHandle = getUniformHandle( attr, gl);
		if ( FAIL_ON_UNKNOWN_SHADER_PARAMETER || attributeHandle != -1 ) {
			gl.glUniform4fv(attributeHandle, 1, vector.getDataArray() , 0 );
		}
	}

	public void delete(GL3 gl) {
		gl.glDeleteProgram( programId );
	}

	public void setUniform(ProgramAttribute attr, int value, GL3 gl) {
		final int attributeHandle = getUniformHandle( attr, gl);
		if ( FAIL_ON_UNKNOWN_SHADER_PARAMETER || attributeHandle != -1 ) {
			gl.glUniform1i( attributeHandle , value );
		}
	}	
}
