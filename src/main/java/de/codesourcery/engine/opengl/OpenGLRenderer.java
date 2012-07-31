package de.codesourcery.engine.opengl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

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
	private final TextureManager textureManager;
	
	private ShaderProgram phongShader;	
	private ShaderProgram phongShaderWithTextures;
	private ShaderProgram wireframeShader;
	
	private static final int BYTES_PER_INT = Integer.SIZE / Byte.SIZE;
	private static final int BYTES_PER_FLOAT= Float.SIZE / Byte.SIZE;
	
	private final World world;

	private int vertexNormalsBufferHandle=-1;
	private int vertexPositionBufferHandle=-1;
	private int texture2DCoordsBufferHandle=-1;	
	
	private volatile boolean useAnisotropicFiltering = true;
	
	private Vector4 diffuseColor = new Vector4(0.3f,0.3f,0.3f,1);
	private Vector4 specularColor = new Vector4(1f,1f,1f,1);
	private Vector4 ambientColor = new Vector4(0.0f,0f,0.0f,1);	
	
	private Vector4 lightPosition = new Vector4( 0 , 50 , -50 );
	
	private final ProgramAttribute ATTR_VERTEX_POSITION = new ProgramAttribute("vVertex",AttributeType.VERTEX_POSITION);
	private final ProgramAttribute ATTR_VERTEX_NORMAL = new ProgramAttribute("vNormal",AttributeType.VERTEX_NORMAL);
	private final ProgramAttribute ATTR_VERTEX_TEXTURE2D_COORDS = new ProgramAttribute("vTexCoords",AttributeType.VERTEX_TEXTURE2D_COORDS);
	
	private final ProgramAttribute UNIFORM_NORMAL_MATRIX = new ProgramAttribute("normalMatrix",AttributeType.NORMAL_MATRIX );	
	private final ProgramAttribute UNIFORM_MV_MATRIX = new ProgramAttribute("mvMatrix",AttributeType.MV_MATRIX );
	private final ProgramAttribute UNIFORM_MVP_MATRIX = new ProgramAttribute("mvpMatrix",AttributeType.MVP_MATRIX );
	
	@SuppressWarnings("unused")
	private final ProgramAttribute UNIFORM_EYE_POSITION = new ProgramAttribute("vEyePosition",AttributeType.EYE_POSITION);
	
	private final ProgramAttribute UNIFORM_COLORMAP = new ProgramAttribute("colorMap",AttributeType.COLORMAP);	
	
	// lighting
	private final ProgramAttribute UNIFORM_DIFFUSE_COLOR = new ProgramAttribute("diffuseColor",AttributeType.DIFFUSE_COLOR );
	private final ProgramAttribute UNIFORM_AMBIENT_COLOR = new ProgramAttribute("ambientColor",AttributeType.AMBIENT_COLOR );
	private final ProgramAttribute UNIFORM_SPECULAR_COLOR = new ProgramAttribute("specularColor",AttributeType.SPECULAR_COLOR );	
	private final ProgramAttribute UNIFORM_LIGHT_POSITION = new ProgramAttribute("vLightPosition",AttributeType.LIGHT_POSITION);	
	
	public OpenGLRenderer( TextureManager texManager , World world) {
		this.world = world;
		this.textureManager = texManager;
	}
	
	public void setAmbientColor(Vector4 ambientColor) {
		this.ambientColor = ambientColor;
	}
	
	public void setSpecularColor(Vector4 specularColor) {
		this.specularColor = specularColor;
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
		if ( phongShaderWithTextures != null ) {
			phongShaderWithTextures.delete( gl );
		}
		if ( phongShader != null ) {
			phongShader.delete( gl );
		}		
		if ( wireframeShader != null ) {
			wireframeShader.delete(gl );
		}
		if ( vertexPositionBufferHandle != -1 ) {
			gl.glDeleteBuffers(1 , new int[] { vertexPositionBufferHandle } , 0 );
			vertexPositionBufferHandle = -1;
		}
		if ( vertexNormalsBufferHandle != -1 ) {
			gl.glDeleteBuffers(1 , new int[] { vertexNormalsBufferHandle } , 0 );
			vertexNormalsBufferHandle = -1;
		}	
		if ( texture2DCoordsBufferHandle != -1 ) {
			gl.glDeleteBuffers(1 , new int[] { texture2DCoordsBufferHandle } , 0 );
			texture2DCoordsBufferHandle = -1;
		}		
	}
	
	private void allocateBuffers(GL3 gl) {
		vertexPositionBufferHandle = BufferManager.allocateBufferHandle( gl );
		vertexNormalsBufferHandle = BufferManager.allocateBufferHandle( gl );	
		texture2DCoordsBufferHandle = BufferManager.allocateBufferHandle( gl ); 
	}
	
	private void loadShaders(GL3 gl) 
	{
		System.out.println("Loading shaders...");
		
		// phong shader
		phongShaderWithTextures = shaderManager.loadFromFile( "phong_texture" , "phong_texture.vshader" , "phong_texture.fshader" , 
				new ProgramAttribute[] {
					ATTR_VERTEX_POSITION,
					ATTR_VERTEX_NORMAL,
					ATTR_VERTEX_TEXTURE2D_COORDS,
					UNIFORM_NORMAL_MATRIX,
					UNIFORM_MV_MATRIX,
					UNIFORM_MVP_MATRIX,
					UNIFORM_DIFFUSE_COLOR,
					UNIFORM_AMBIENT_COLOR,
					UNIFORM_LIGHT_POSITION,
					UNIFORM_COLORMAP
		} , gl );
		
		// phong shader
		phongShader = shaderManager.loadFromFile( "phong" , "phong.vshader" , "phong.fshader" , 
				new ProgramAttribute[] {
					ATTR_VERTEX_POSITION,
					ATTR_VERTEX_NORMAL,
					UNIFORM_NORMAL_MATRIX,
					UNIFORM_MV_MATRIX,
					UNIFORM_MVP_MATRIX,
					UNIFORM_DIFFUSE_COLOR,
					UNIFORM_AMBIENT_COLOR,
					UNIFORM_LIGHT_POSITION
		} , gl );		
		
		
		wireframeShader = shaderManager.loadFromFile( "wireframe" , "wireframe.vshader" , "flat.fshader" , 
				new ProgramAttribute[] {
					ATTR_VERTEX_POSITION,
					UNIFORM_MVP_MATRIX
		} , gl );		
	}
	
	public synchronized void render(GLAutoDrawable drawable) 
	{
		final GL3 gl = drawable.getGL().getGL3();
		
		gl.glEnable( GL.GL_COLOR_BUFFER_BIT );
		gl.glClearColor( 0f,0f,0f,1.0f );
		gl.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );
		
		if ( useAnisotropicFiltering ) 
		{
			gl.glTexParameterf( GL.GL_TEXTURE_2D , GL.GL_TEXTURE_MAG_FILTER , GL.GL_NEAREST );
			gl.glTexParameterf( GL.GL_TEXTURE_2D , GL.GL_TEXTURE_MIN_FILTER , GL.GL_NEAREST );
			
			final float[] maxSupportedAmount = new float[1];
			gl.glGetFloatv(GL.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT , maxSupportedAmount , 0 );
			gl.glTexParameterf(GL.GL_TEXTURE_2D , GL.GL_TEXTURE_MAX_ANISOTROPY_EXT , maxSupportedAmount[0] );
		} else {
			gl.glTexParameterf(GL.GL_TEXTURE_2D , GL.GL_TEXTURE_MAX_ANISOTROPY_EXT , 1.0f );			
		}		
		
		final Matrix[] mvMatrix = new Matrix[1];
		final Matrix[] normalMatrix = new Matrix[1];
		final Matrix[] mvpMatrix = new Matrix[1];
		
		final IUniformAttributeProvider uniformProvider = new IUniformAttributeProvider() {
			
			@Override
			public void setUniform(ShaderProgram program, ProgramAttribute uniform) 
			{
				switch( uniform.getType() ) {
					case MV_MATRIX:
						program.setUniform( uniform , mvMatrix[0] , gl );
						break;
					case NORMAL_MATRIX:
						program.setUniform( uniform , normalMatrix[0] , gl );
						break;
					case MVP_MATRIX:
						program.setUniform( uniform , mvpMatrix[0] , gl );
						break;
					case EYE_POSITION:
						program.setUniform( uniform , world.getCamera().getEyePosition() , gl );
						break;		
					case COLORMAP:
						program.setUniform( uniform , 0 , gl );
						break;
						// lighting
					case LIGHT_POSITION:
						program.setUniform( uniform , lightPosition , gl );
						break;
					case DIFFUSE_COLOR:
						program.setUniform( uniform , diffuseColor , gl );
						break;	
					case SPECULAR_COLOR:
						 // TODO: No support for per-surface materials , this should really be vSpecularMaterial * vSpecularLight
						program.setUniform( uniform , specularColor , gl );
						break;
					case AMBIENT_COLOR:
						 // TODO: No support for per-surface materials , this should really be vAmbientMaterial * vAmbientLight
						program.setUniform( uniform , ambientColor , gl );
						break;							
					default:
						throw new RuntimeException("Shader program "+program+" requested unknown uniform "+uniform);
				}
			}
		};
		
		for ( Object3D obj : world.getRootObjects() ) 
		{
			// send tick to object attribute modifiers
			// (will be broadcasted to all child objects automatically)
			obj.tick();
			
			renderObjectGraph(obj, mvMatrix, normalMatrix, mvpMatrix, uniformProvider, gl);
		}
		
		// cleanup
		gl.glDisable( GL.GL_COLOR_BUFFER_BIT );
	}

	private void renderObjectGraph(Object3D obj, final Matrix[] mvMatrix,
			final Matrix[] normalMatrix, final Matrix[] mvpMatrix,
			final IUniformAttributeProvider uniformProvider, final GL3 gl) 
	{
		final ShaderProgram currentShader;
		int textureObjectHandle = -1;
		if ( obj.isRenderWireframe() ) 
		{
			currentShader = wireframeShader;
			
			gl.glDisable( GL.GL_DEPTH_TEST );
			gl.glDisable( GL.GL_CULL_FACE );
		} 
		else 
		{
			if ( obj.getTextureName() != null && ! obj.isTexturesDisabled() ) {
				currentShader = phongShaderWithTextures;
				textureObjectHandle = textureManager.getTexture( obj.getTextureName() ).getTextureObject( gl );
				gl.glBindTexture( GL.GL_TEXTURE_2D , textureObjectHandle );
			} else {
				currentShader = phongShader;
			}
			
			gl.glEnable( GL.GL_DEPTH_TEST );
			gl.glDepthFunc( GL.GL_LEQUAL );
			
			gl.glEnable( GL.GL_CULL_FACE );
			gl.glFrontFace( GL3.GL_CCW );
			gl.glCullFace( GL3.GL_BACK );
		}
		
		mvMatrix[0] = world.getViewMatrix().multiply( obj.getModelMatrix() );
		normalMatrix[0] = mvMatrix[0].invert().transpose();
		mvpMatrix[0]=  world.getProjectionMatrix().multiply( mvMatrix[0] );

		// setup shader
		currentShader.use( gl );
		currentShader.setupUniforms( uniformProvider );
		
		renderOneObject( obj , currentShader , gl );
		
		if ( textureObjectHandle != -1 ) {
			gl.glDisable( GL.GL_TEXTURE_2D );
		}
		
		gl.glUseProgram( 0 );
		
		// render child objects
		for ( Object3D child : obj.getChildren() ) 
		{
			renderObjectGraph(child, mvMatrix, normalMatrix, mvpMatrix, uniformProvider, gl);
		}
	}	

	private void renderOneObject(final Object3D obj,ShaderProgram currentShader , final GL3 gl) 
	{
		// setup VBO
		final IVertexAttributeProvider attributeProvider = new IVertexAttributeProvider() {
			
			private final List<Integer> enabledVertexAttributeArrays = new ArrayList<Integer>();
			
			@Override
			public void setVertexAttribute(ShaderProgram program,ProgramAttribute attribute) 
			{
				switch( attribute.getType() ) {
					case VERTEX_POSITION:
						enabledVertexAttributeArrays.add(
								setupVertextBufferObject( ATTR_VERTEX_POSITION , vertexPositionBufferHandle , obj.getVertices() , 4, gl )
								);
						break;
					case VERTEX_TEXTURE2D_COORDS:
						enabledVertexAttributeArrays.add(
								setupVertextBufferObject( ATTR_VERTEX_TEXTURE2D_COORDS , texture2DCoordsBufferHandle , obj.getTexture2DCoords() , 2 , gl )
								);
						break;						
					case VERTEX_NORMAL:
						enabledVertexAttributeArrays.add(
								setupVertextBufferObject( ATTR_VERTEX_NORMAL , vertexNormalsBufferHandle , obj.getNormalVectors() , 4 , gl )								
								);
						break;
					default:
						throw new RuntimeException("Internal error, unhandled per-vertex attribute "+attribute+" in program "+program);
				}
			}
			
			@Override
			public void cleanup() 
			{
				for ( int id : enabledVertexAttributeArrays ) {
					gl.glDisableVertexAttribArray( id );
				}
			}
		};
		
		// set per-vertex shader attributes
		currentShader.setupVertexAttributes( attributeProvider);
		
		// setup index buffer
		if ( ! obj.isRenderWireframe() ) 
		{
			final int vertexCount=obj.getEdges().length;
	        final IntBuffer indexBuffer= Buffers.newDirectIntBuffer( vertexCount );
	        indexBuffer.put( obj.getEdges() );
	        indexBuffer.rewind();
	        
			// render
			gl.glDrawElements( GL.GL_TRIANGLES, vertexCount  , GL.GL_UNSIGNED_INT, indexBuffer );
		} 
		else 
		{
			final int[] edges = obj.getEdges();
			final int triangleCount = edges.length / 3;
			
	        final IntBuffer indexBuffer= Buffers.newDirectIntBuffer( triangleCount * 3 *2 ); // 3 lines per triangle with 2 vertices each
	        
	        for ( int currentTriangle = 0 ; currentTriangle < triangleCount ; currentTriangle++ )
	        {
	        	final int index = currentTriangle*3;
	        	indexBuffer.put( edges[index] );
	        	indexBuffer.put( edges[index+1] );
	        	
	        	indexBuffer.put( edges[index+1] );
	        	indexBuffer.put( edges[index+2] );
	        	
	        	indexBuffer.put( edges[index+2] );
	        	indexBuffer.put( edges[index] );	        	
	        }
	        indexBuffer.rewind();
			gl.glDrawElements( GL3.GL_LINES , triangleCount*6, GL.GL_UNSIGNED_INT, indexBuffer );			
		}
		
		// clean up
		attributeProvider.cleanup();
	}
	
	private int setupVertextBufferObject(ProgramAttribute attribute , int bufferHandle , float[] data,int elementCountPerVertex, GL3 gl) 
	{
        final FloatBuffer buffer = Buffers.newDirectFloatBuffer( data );
        final int sizeInBytes = buffer.capacity() * BYTES_PER_FLOAT; // one float = 32 bit = 4 bytes
        
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufferHandle );
        gl.glBufferData(GL.GL_ARRAY_BUFFER, sizeInBytes, buffer , GL3.GL_STREAM_DRAW );
        
        final int attributeHandle = phongShaderWithTextures.getVertexAttributeHandle( attribute , gl );
        
		gl.glVertexAttribPointer(
				attributeHandle , // attribute
				elementCountPerVertex,                 // number of elements per vertex, here (x,y,z,w)
			    GL.GL_FLOAT,          // the type of each element
			    false,          // take our values as-is
			    0,                 // no extra data between each position
			    0                  // offset of first element
		);		
		
		gl.glEnableVertexAttribArray( attributeHandle );   
		
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER , 0 );		
		return attributeHandle;
	}

	public void setUseAnisotropicFiltering(boolean useAnisotropicFiltering) {
		this.useAnisotropicFiltering = useAnisotropicFiltering;
	}
	
	public boolean isUseAnisotropicFiltering() {
		return useAnisotropicFiltering;
	}
}