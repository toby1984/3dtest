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

	private int vertexBufferHandle=-1;
	
	private final ProgramAttribute ATTR_VERTEX_POSITION = new ProgramAttribute("vVertex",AttributeType.VERTEX_POSITION);
	private final ProgramAttribute UNIFORM_VERTEX_COLOR = new ProgramAttribute("vColorValue",AttributeType.VERTEX_COLOR );	
	private final ProgramAttribute UNIFORM_MVP_MATRIX = new ProgramAttribute("mvpMatrix",AttributeType.MVP_MATRIX );	
	
	public OpenGLRenderer(World world) {
		this.world = world;
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
		if ( vertexBufferHandle != -1 ) {
			gl.glDeleteBuffers(1 , new int[] { vertexBufferHandle } , 0 );
			vertexBufferHandle = -1;
		}
	}
	
	private void allocateBuffers(GL3 gl) {
		vertexBufferHandle = BufferManager.allocateBufferHandle( gl );
	}
	
	private void loadShaders(GL3 gl) 
	{
		System.out.println("Loading shaders...");
		shader = shaderManager.loadFromFile( "default" , "basic.vshader" , "flat.fshader" , 
				new ProgramAttribute[] {ATTR_VERTEX_POSITION,UNIFORM_VERTEX_COLOR,UNIFORM_MVP_MATRIX} , gl );
	}
	
	public synchronized void render(GLAutoDrawable drawable) 
	{
		final GL3 gl = drawable.getGL().getGL3();
		
		// use our shader
		shader.use( gl );
		
		// enable depth buffer
		gl.glEnable( GL.GL_COLOR_BUFFER_BIT );
//		gl.glEnable( GL.GL_DEPTH_TEST );
//		gl.glDepthFunc(GL.GL_LESS);
		
		gl.glClearColor( 1f,1f,1f,1.0f );
		gl.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );
		
		final Matrix viewProjection = world.getViewMatrix().multiply( world.getProjectionMatrix() );
//		final Matrix viewProjection = world.getProjectionMatrix();
		
		for ( Object3D obj : world.getObjects() ) 
		{
			Matrix matrix =  obj.getModelMatrix().multiply( world.getViewMatrix() ); // Matrix.identity(); // obj.getModelMatrix().multiply( viewProjection );
			shader.setUniform( UNIFORM_MVP_MATRIX , matrix  , gl );
			render( obj , gl );
		}
		
		// cleanup
		gl.glUseProgram( 0 );
		gl.glDisable( GL.GL_COLOR_BUFFER_BIT );
	}	

	private void render(Object3D obj,GL3 gl) 
	{
		// setup VBO
        final FloatBuffer buffer = Buffers.newDirectFloatBuffer( obj.getVertices() );
        final int sizeInBytes = buffer.capacity() * BYTES_PER_FLOAT; // one float = 32 bit = 4 bytes
        
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBufferHandle );
        gl.glBufferData(GL.GL_ARRAY_BUFFER, sizeInBytes, buffer , GL3.GL_STREAM_DRAW );
        
        final int vertexPositionAttribute = shader.getVertexAttributeHandle( ATTR_VERTEX_POSITION , gl );
        
		gl.glVertexAttribPointer(
				vertexPositionAttribute , // attribute
			    4,                 // number of elements per vertex, here (x,y,z,w)
			    GL.GL_FLOAT,          // the type of each element
			    false,          // take our values as-is
			    0,                 // no extra data between each position
			    0                  // offset of first element
		);		
		
		gl.glEnableVertexAttribArray( vertexPositionAttribute );   
		
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER , 0 );
		
		// setup index buffer object
		final int vertexCount=obj.getEdges().length;
        final IntBuffer indexBuffer= Buffers.newDirectIntBuffer( vertexCount );
        indexBuffer.put( obj.getEdges() );
        indexBuffer.rewind();
        
		// render
		gl.glDrawElements( GL.GL_TRIANGLES, vertexCount  , GL.GL_UNSIGNED_INT, indexBuffer );
		
		// clean up
		gl.glDisableVertexAttribArray( vertexPositionAttribute );
	}
}