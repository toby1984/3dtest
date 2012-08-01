package de.codesourcery.engine;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;

import javax.media.opengl.DebugGL2;
import javax.media.opengl.DebugGL3;
import javax.media.opengl.DebugGL3bc;
import javax.media.opengl.DebugGL4;
import javax.media.opengl.DebugGL4bc;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3;
import javax.media.opengl.GL3bc;
import javax.media.opengl.GL4;
import javax.media.opengl.GL4bc;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;

import com.jogamp.opengl.util.FPSAnimator;

import de.codesourcery.engine.geom.TerrainGenerator;
import de.codesourcery.engine.objectmodifiers.ExtractTranslationModifier;
import de.codesourcery.engine.objectmodifiers.RotationModifier;
import de.codesourcery.engine.objectmodifiers.StaticScalingModifier;
import de.codesourcery.engine.objectmodifiers.StaticTranslationModifier;
import de.codesourcery.engine.opengl.OpenGLRenderer;
import de.codesourcery.engine.opengl.TextureManager;
import de.codesourcery.engine.render.Object3D;
import de.codesourcery.engine.util.PLYReader;

/**
 * A minimal program that draws with JOGL in a Swing JFrame using the AWT GLCanvas.
 *
 * @author Wade Walker
 */
public class JOGLTest extends AbstractTest
{
	private final TextureManager textureManager = new TextureManager();
    private final OpenGLRenderer renderer = new OpenGLRenderer( textureManager  , world );	
	
    private GLCanvas glcanvas; 
    private JFrame jframe; 
    private FPSAnimator animator; 
    
    static {
        // setting this true causes window events not to get sent on Linux if you run from inside Eclipse
        GLProfile.initSingleton( false );
    }
    
    public void run() throws Exception
    {
    	setupJOGL();
    	
    	setupWorld();
		
    	animator = new FPSAnimator( glcanvas , 60);
    	animator.setUpdateFPSFrames( 300 , new PrintStream(System.out) );
    	animator.start();    	
    }
    
    protected void setupWorld(GL3 gl) 
    {
        world.removeAllObjects();
        
//      final Object3D earth = new PLYReader().readFromClasspath( "/models/sphere.ply" );
//      earth.setTextureName("earth.png");
//      earth.setRenderWireframe( true );
//      earth.addObjectModifier( new RotationModifier( RotationModifier.Y_AXIS , 1f , 1f , 0.5f ) );
//      
//      final Object3D moon = new PLYReader().readFromClasspath( "/models/sphere.ply" );
//      moon.setTextureName("moon.png");
//      moon.setRenderWireframe( true );
//      
//      moon.addObjectModifier( new ExtractTranslationModifier() );     
//      moon.addObjectModifier( new StaticScalingModifier( 0.35f , 0.35f , 0.35f) );
//      moon.addObjectModifier( new StaticTranslationModifier( 1.5f ,  0 , 0 ) );  
//      moon.addObjectModifier( new RotationModifier( RotationModifier.Y_AXIS  , 1f , 1f , 1f) );
//      
//      earth.addChild( moon );
        
        final long seed = System.currentTimeMillis();
        Object3D terrain = new TerrainGenerator( textureManager ).generateTerrain( 512 ,  10 , 1 , true , seed );
        
        world.addRootObject(  terrain  );
    }
    
    public void setupJOGL() 
    {
        final GLProfile glprofile = GLProfile.getDefault();
        final GLCapabilities glcapabilities = new GLCapabilities( glprofile );
        glcapabilities.setRedBits( 8 );
        glcapabilities.setGreenBits( 8 );
        glcapabilities.setBlueBits( 8 );
        glcapabilities.setAlphaBits( 8 );
        
        glcanvas = new GLCanvas( glcapabilities );
    	
        glcanvas.addGLEventListener( new GLEventListener() {
            
            @Override
            public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height ) 
            {
            	System.out.println("*** Reshape "+width+" x "+height);
            	world.setupPerspectiveProjection( fov.get() , width / (float) height, Z_NEAR , Z_FAR );
            	drawable.getGL().glViewport( 0 , 0 , width , height );
            }
            
            @Override
            public void init( GLAutoDrawable drawable ) 
            {
            	if (drawable.getGL().isGL4bc()) {
            	    final GL4bc gl4bc = drawable.getGL().getGL4bc();
            	    drawable.setGL(new DebugGL4bc(gl4bc));
            	} 
            	else {
            	    if (drawable.getGL().isGL4()) {
            	        final GL4 gl4 = drawable.getGL().getGL4();
            	        drawable.setGL(new DebugGL4(gl4));
            	    }
            	    else {
            	          if (drawable.getGL().isGL3bc()) {
            	              final GL3bc gl3bc = drawable.getGL().getGL3bc();
            	              drawable.setGL(new DebugGL3bc(gl3bc));
            	          }
            	          else {
            	              if (drawable.getGL().isGL3()) {
            	                  final GL3 gl3 = drawable.getGL().getGL3();
            	                  drawable.setGL(new DebugGL3(gl3));
            	              }
            	              else {
            	                  if (drawable.getGL().isGL2()) {
            	                      final GL2 gl2 = drawable.getGL().getGL2();
            	                      drawable.setGL(new DebugGL2(gl2));
            	                  }
            	              }
            	          }
            	     }
            	}            
            	
            	setupWorld( drawable.getGL().getGL3() );
            	renderer.setup( drawable.getGL().getGL3() );
            }
            
            @Override
            public void dispose( GLAutoDrawable glautodrawable ) {
            	renderer.cleanUp( glautodrawable.getGL().getGL3() );
            	if ( animator != null ) {
            		animator.stop();
            	}
            }
            
            @Override
            public void display( GLAutoDrawable glautodrawable ) 
            {
            	renderer.render( glautodrawable );
            }
        });

        //  Setup JFrame.
        jframe = new JFrame( "One Triangle Swing GLCanvas" ); 
        jframe.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
        jframe.addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent windowevent ) 
            {
                jframe.dispose();
            }
        });

        jframe.getContentPane().add( glcanvas, BorderLayout.CENTER );
        jframe.setSize( INITIAL_CANVAS_WIDTH, INITIAL_CANVAS_HEIGHT);
        jframe.setVisible( true );
        
        registerInputListeners( glcanvas );
    }
    
    public static void main( String [] args ) throws Exception 
    {
    	new JOGLTest().run();
    }

	@Override
	protected void forceRepaint() {
		glcanvas.repaint();
	}

	@Override
	protected void setMouseCursor(Cursor cursor) {
		jframe.setCursor( cursor );
	}
	
	@Override
	protected void toggleAnisotopicFiltering() 
	{
		final boolean newState = ! renderer.isUseAnisotropicFiltering();
		renderer.setUseAnisotropicFiltering( newState );
		System.out.println("Anisotropic filtering is now "+(newState?"ON":"off" ) );
	}
    
}