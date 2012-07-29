package de.codesourcery.engine.opengl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;

import com.jogamp.common.nio.Buffers;

import de.codesourcery.engine.linalg.Matrix;
import de.codesourcery.engine.linalg.Vector4;
import de.codesourcery.engine.opengl.ProgramAttribute.AttributeType;
import de.codesourcery.engine.render.Object3D;
import de.codesourcery.engine.render.World;

public class OpenGLRenderer {

	private final ShaderManager shaderManager = new ShaderManager();
	
	private ShaderProgram shader;
	
	private static final int BYTES_PER_INT = Integer.SIZE / Byte.SIZE;
	private static final int BYTES_PER_FLOAT= Float.SIZE / Byte.SIZE;
	
	private final World world;

	private int vertexNormalsBufferHandle=-1;
	private int vertexPositionBufferHandle=-1;
	
	private Vector4 diffuseColor = new Vector4(0.5f,0.0f,0.0f,0);
	private Vector4 lightPosition = new Vector4( 0 , 10 , 0 );
	
	private final ProgramAttribute ATTR_VERTEX_POSITION = new ProgramAttribute("vVertex",AttributeType.VERTEX_POSITION);
	private final ProgramAttribute ATTR_VERTEX_NORMAL = new ProgramAttribute("vNormal",AttributeType.VERTEX_NORMAL);
	
	private final ProgramAttribute UNIFORM_NORMAL_MATRIX = new ProgramAttribute("normalMatrix",AttributeType.NORMAL_MATRIX );	
	private final ProgramAttribute UNIFORM_MV_MATRIX = new ProgramAttribute("mvMatrix",AttributeType.MV_MATRIX );
	private final ProgramAttribute UNIFORM_MVP_MATRIX = new ProgramAttribute("mvpMatrix",AttributeType.MVP_MATRIX );
	
	private final ProgramAttribute UNIFORM_DIFFUSE_COLOR = new ProgramAttribute("diffuseColor",AttributeType.DIFFUSE_COLOR );	
	private final ProgramAttribute UNIFORM_LIGHT_POSITION = new ProgramAttribute("vLightPosition",AttributeType.LIGHT_POSITION);	
	private final ProgramAttribute UNIFORM_EYE_POSITION = new ProgramAttribute("vEyePosition",AttributeType.EYE_POSITION);
	
	public OpenGLRenderer(World world) {
		this.world = world;
	}
	
	public void setDiffuseColor(Vector4 diffuseColor) {
		this.diffuseColor = diffuseColor;
	}
	
	public void setLightPosition(Vector4 lightPosition) {
		this.lightPosition = lightPosition;
	}
	
	public void setup(GL3 gl) 
	{
		loadShaders(gl);
		allocateBuffers(gl);
		System.out.println("Renderer initialized.");
	}	
	
	public void cleanUp(GL3 gl) {
		if ( shader != null ) {
			shader.delete( gl );
		}
		if ( vertexPositionBufferHandle != -1 ) {
			gl.glDeleteBuffers(1 , new int[] { vertexPositionBufferHandle } , 0 );
			vertexPositionBufferHandle = -1;
		}
		if ( vertexNormalsBufferHandle != -1 ) {
			gl.glDeleteBuffers(1 , new int[] { vertexNormalsBufferHandle } , 0 );
			vertexNormalsBufferHandle = -1;
		}		
	}
	
	private void allocateBuffers(GL3 gl) {
		vertexPositionBufferHandle = BufferManager.allocateBufferHandle( gl );
		vertexNormalsBufferHandle = BufferManager.allocateBufferHandle( gl );		
	}
	
	private void loadShaders(GL3 gl) 
	{
		System.out.println("Loading shaders...");
		shader = shaderManager.loadFromFile( "default" , "basic.vshader" , "flat.fshader" , 
				new ProgramAttribute[] {
					ATTR_VERTEX_POSITION,
					ATTR_VERTEX_NORMAL,
					UNIFORM_NORMAL_MATRIX,
					UNIFORM_MV_MATRIX,
					UNIFORM_MVP_MATRIX,
					UNIFORM_DIFFUSE_COLOR,
					UNIFORM_LIGHT_POSITION,
					UNIFORM_EYE_POSITION
		} , gl );
	}
	
	public synchronized void render(GLAutoDrawable drawable) 
	{
		final GL3 gl = drawable.getGL().getGL3();
		
		// use our shader
		shader.use( gl );
		
		// enable depth buffer
		gl.glEnable( GL.GL_COLOR_BUFFER_BIT );
		gl.glEnable( GL.GL_DEPTH_TEST );
		gl.glEnable( GL.GL_CULL_FACE );
		
		gl.glFrontFace( GL3.GL_CCW );
		gl.glCullFace( GL3.GL_BACK );
		gl.glDepthFunc(GL.GL_LESS);
		
		gl.glClearColor( 1f,1f,1f,1.0f );
		gl.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );
		
		for ( Object3D obj : world.getObjects() ) 
		{
			Matrix mvMatrix = world.getViewMatrix().multiply( obj.getModelMatrix() );
			Matrix normalMatrix = mvMatrix.invert().transpose();
			Matrix mvpMatrix =  world.getProjectionMatrix().multiply( mvMatrix);
			
			shader.setUniform( UNIFORM_NORMAL_MATRIX , normalMatrix  , gl );
			shader.setUniform( UNIFORM_MV_MATRIX , mvMatrix  , gl );
			shader.setUniform( UNIFORM_MVP_MATRIX , mvpMatrix  , gl );
			
			shader.setUniform( UNIFORM_DIFFUSE_COLOR , diffuseColor , gl );
			shader.setUniform( UNIFORM_LIGHT_POSITION, lightPosition, gl );
			
			render( obj , gl );
		}
		
		// cleanup
		gl.glUseProgram( 0 );
		gl.glDisable( GL.GL_COLOR_BUFFER_BIT );
	}	

	private void render(Object3D obj,GL3 gl) 
	{
		// setup VBO
		final int handle1 = setupVertextBufferObject( ATTR_VERTEX_POSITION , vertexPositionBufferHandle , obj.getVertices() , gl );
		final int handle2 = setupVertextBufferObject( ATTR_VERTEX_NORMAL , vertexNormalsBufferHandle , obj.getNormalVectors() , gl );
		
		// setup index buffer object
		final int vertexCount=obj.getEdges().length;
        final IntBuffer indexBuffer= Buffers.newDirectIntBuffer( vertexCount );
        indexBuffer.put( obj.getEdges() );
        indexBuffer.rewind();
        
		// render
		gl.glDrawElements( GL.GL_TRIANGLES, vertexCount  , GL.GL_UNSIGNED_INT, indexBuffer );
		
		// clean up
		gl.glDisableVertexAttribArray( handle1 );
		gl.glDisableVertexAttribArray( handle2 );
	}
	
	private int setupVertextBufferObject(ProgramAttribute attribute , int bufferHandle , float[] data,GL3 gl) 
	{
        final FloatBuffer buffer = Buffers.newDirectFloatBuffer( data );
        final int sizeInBytes = buffer.capacity() * BYTES_PER_FLOAT; // one float = 32 bit = 4 bytes
        
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufferHandle );
        gl.glBufferData(GL.GL_ARRAY_BUFFER, sizeInBytes, buffer , GL3.GL_STREAM_DRAW );
        
        final int attributeHandle = shader.getVertexAttributeHandle( attribute , gl );
        
		gl.glVertexAttribPointer(
				attributeHandle , // attribute
			    4,                 // number of elements per vertex, here (x,y,z,w)
			    GL.GL_FLOAT,          // the type of each element
			    false,          // take our values as-is
			    0,                 // no extra data between each position
			    0                  // offset of first element
		);		
		
		gl.glEnableVertexAttribArray( attributeHandle );   
		
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER , 0 );		
		return attributeHandle;
	}
}