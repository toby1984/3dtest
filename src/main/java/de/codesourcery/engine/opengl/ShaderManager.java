package de.codesourcery.engine.opengl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

import com.jogamp.common.nio.Buffers;

public class ShaderManager 
{
	private final Map<String,ShaderProgram> programs = new HashMap<>();

	public static enum ShaderType {
		FRAGMENT(GL3.GL_FRAGMENT_SHADER),
		VERTEX(GL3.GL_VERTEX_SHADER);

		private final int glTypeCode;

		private ShaderType(int glTypeCode) {
			this.glTypeCode = glTypeCode;
		}

		public int getOpenGLTypeCode() {
			return glTypeCode;
		}
	}

	public ShaderProgram getProgram(String identifier) {
		ShaderProgram program = programs.get( identifier );
		if ( program == null ) {
			throw new RuntimeException("No shader program with identifier '"+identifier+"' loaded");
		}
		return program;
	}
	
	public ShaderProgram loadFromFile(String identifier , String vertexShaderFilename, String fragmentShaderFilename,ProgramAttribute[] attributes , GL3 gl) 
	{
		if ( programs.containsKey( identifier ) ) {
			throw new IllegalStateException("Shader program '"+identifier+"' is already loaded?");
		}

		final int vert = setupShader( ShaderType.VERTEX, vertexShaderFilename , gl );
		final int frag = setupShader( ShaderType.FRAGMENT, fragmentShaderFilename , gl );

		final int programId = gl.glCreateProgram();

		gl.glAttachShader(programId, vert);
		checkError(gl);

		gl.glAttachShader(programId, frag);
		checkError(gl);
		
		if ( attributes != null ) {
			for ( ProgramAttribute attr : attributes ) {
				gl.glBindAttribLocation( programId , attr.getType().getId() , attr.getIdentifier() );
				checkError( gl );
			}
		}

		gl.glLinkProgram(programId);
		
		checkLinkerErrors( programId ,gl);

		gl.glValidateProgram(programId);
		checkError(gl);

		gl.glDeleteShader(vert);
		checkError(gl);

		gl.glDeleteShader(frag);
		checkError(gl);

		final ShaderProgram program = new ShaderProgram(identifier, programId);
		programs.put( identifier , program );
		return program;
	}

	private int setupShader(ShaderType type , String sourceFileName , GL3 gl ) 
	{
		final String source = loadShaderSource( type , sourceFileName );

		final int shader = gl.glCreateShader( type.getOpenGLTypeCode() );
		checkError(gl);

		gl.glShaderSource(shader, 1, new String[] { source } , new int[] { source.length() } , 0);
		checkError(gl);

		gl.glCompileShader(shader);
		checkCompilationErrors(type,sourceFileName , shader , gl );

		return shader;
	}

	private void checkLinkerErrors(int programId,GL3 gl) 
	{
		ByteBuffer buffer = ByteBuffer.allocateDirect( 4 );
		gl.glGetProgramiv( programId , GL3.GL_LINK_STATUS , buffer.asIntBuffer() );
		if ( buffer.asIntBuffer().get( 0 ) == GL.GL_FALSE ) 
		{
			final String errors = getProgramInfoLog( programId ,gl);
			throw new RuntimeException("Failed to link program: \n\n"+errors );
		}
	}

	private void checkCompilationErrors(ShaderType type  , String shaderFileName , int shader,GL3 gl) 
	{
		ByteBuffer buffer = ByteBuffer.allocateDirect( 4 );
		gl.glGetShaderiv( shader , GL3.GL_COMPILE_STATUS , buffer.asIntBuffer() );
		if ( buffer.asIntBuffer().get( 0 ) == GL.GL_FALSE ) 
		{
			final String errors = getCompilationErrors( shader , gl );
			throw new RuntimeException("Failed to compile "+type+" shader from file '"+shaderFileName+"':\n\n"+errors );
		}
	}

	private String getCompilationErrors(int shader,GL3 gl) 
	{
		final ByteBuffer buffer = Buffers.newDirectByteBuffer( 1024 * 10);
		final IntBuffer intBuffer = Buffers.newDirectIntBuffer(1);

		gl.glGetShaderInfoLog(shader, 1024 * 10, intBuffer, buffer);

		final int numBytes = intBuffer.get(0);
		if ( numBytes <= 1 ) {
			return "";
		}

		final byte[] bytes = new byte[numBytes];
		buffer.get(bytes);
		return new String(bytes);
	}	

	private String getProgramInfoLog (int programId,GL3 gl) 
	{
		final ByteBuffer buffer = Buffers.newDirectByteBuffer( 1024 * 10);
		final IntBuffer intBuffer = Buffers.newDirectIntBuffer(1);

		gl.glGetProgramInfoLog(programId, 1024 * 10, intBuffer, buffer);

		final int numBytes = intBuffer.get(0);
		if ( numBytes <= 1 ) {
			return "";
		}

		final byte[] bytes = new byte[numBytes];
		buffer.get(bytes);
		return new String(bytes);
	}	

	private void checkError(GL3 gl) 
	{
		int errorCode = gl.glGetError();
		if ( errorCode != GL.GL_NO_ERROR ) {
			throw new RuntimeException("OpenGL error "+errorCode);
		}
	}

	private String loadShaderSource(ShaderType type , String fileName) 
	{
		final String path;
		switch( type ) {
			case FRAGMENT:
				path ="/fragmentshaders/"+fileName;
				break;
			case VERTEX:
				path ="/vertexshaders/"+fileName;
				break;
			default:
				throw new RuntimeException("Unhandled shader type: "+type);
		}

		final InputStream in = getClass().getResourceAsStream( path );
		if ( in == null ) {
			throw new RuntimeException( type+" shader not found using path '"+path+"'");
		}

		try {
			final BufferedReader reader = new BufferedReader(new InputStreamReader( in ) );
			final StringBuilder shaderSource = new StringBuilder();

			String line = null;
			while( ( line = reader.readLine() ) != null ) 
			{
				shaderSource.append( line +"\n" );
			}

			return shaderSource.toString();
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to load shader source from '"+fileName+"'",e);
		} 
		finally 
		{
			try {
				in.close();
			} catch(IOException e) {
				// OK
			}
		}
	}
}